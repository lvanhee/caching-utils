package cachingutils.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cachingutils.Cache;

public class FileBasedStringSetCache<O> implements Cache<O, Boolean>,Set<O>{
	
	private final Path filePath;
	
	private final Set<O> allStrings;

	private final Function<String, O> parser;
	private final Function<O, String> toString;
	
	public FileBasedStringSetCache(Path path, 
			Function<String, O> parser,
			Function<O, String> toString) {
		this.parser = parser;
		this.toString = toString;
		this.filePath = path;
		
		if(!path.toFile().exists())
		{
			try {
				if(!path.getParent().toFile().exists())
					Files.createDirectories(path.getParent());
				path.toFile().createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
		}
		try {
			allStrings = Files.readAllLines(path, StandardCharsets.ISO_8859_1)
					.stream()
					.map(x->parser.apply(x))
					.collect(Collectors.toSet());
		} catch (IOException e) {
			throw new Error();
		}
	}

	@Override
	public void add(O i, Boolean o) {
		if(o)
		{
			allStrings.add(i);
		    try {
		    	FileWriter fw = new FileWriter(filePath.toFile(), true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    String translation = toString.apply(i);
			    assert(!translation.contains("\n"));
				bw.write(translation);
			    bw.newLine();
			    bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
		}
		else 
			{
			allStrings.remove(i);
			try {
				Files.writeString(filePath,
						allStrings.stream()
						.map(x->toString.apply(x))
						.reduce("", (x,y)->x+"\n"+y));
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
			}
		
	}

	@Override
	public boolean has(O i) {
		return allStrings.contains(i);
	}

	@Override
	public Boolean get(O i) {
		return allStrings.contains(i);
	}

	public static<O> FileBasedStringSetCache<O> loadCache(Path path,
			Function<String, O> parser,Function<O, String> toString) {
		return new FileBasedStringSetCache<>(path, parser,toString);
	}

	@Override
	public int size() {
		return allStrings.size();
	}

	@Override
	public boolean isEmpty() {
		throw new Error();
	}

	@Override
	public boolean contains(Object o) {
		return allStrings.contains(o);
	}

	@Override
	public Iterator<O> iterator() {
		return allStrings.iterator();
	}

	@Override
	public Object[] toArray() {
		throw new Error();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new Error();
	}

	@Override
	public boolean add(O e) {
		add(e,true);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		throw new Error();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new Error();
	}

	@Override
	public boolean addAll(Collection<? extends O> c) {
		c.stream().forEach(x->add(x));
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new Error();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new Error();
	}

	@Override
	public void clear() {
		throw new Error();
	}
	
	public String toString()
	{
		return this.size()+" "+this.filePath+" "+this.allStrings;
	}

}
