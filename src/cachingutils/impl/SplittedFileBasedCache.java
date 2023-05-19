package cachingutils.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import cachingutils.Cache;

public class SplittedFileBasedCache<I,O> implements Cache<I, O> {
	
	private final Function<I, File> fileLocator;
	private final Function<String, O> stringTranslator;
	private final Function<O,String> objectToStringTranslator;
	
	private SplittedFileBasedCache(Function<I, File> fileLocator, Function<O,String> objectToStringTranslator, Function<String, O> stringTranslator) {
		this.fileLocator = fileLocator;
		this.objectToStringTranslator = objectToStringTranslator;
		this.stringTranslator = stringTranslator;
	}

	@Override
	public synchronized void add(I i, O o) {
		File cacheFileName = fileLocator.apply(i);
		if(has(i))
			throw new Error();
		
		BufferedWriter writer;
		try {
			if(!cacheFileName.getParentFile().exists())
				cacheFileName.getParentFile().mkdirs();
			String translated = objectToStringTranslator.apply(o);
			cacheFileName.createNewFile();
			writer = new BufferedWriter(new FileWriter(cacheFileName,StandardCharsets.ISO_8859_1));

			writer.write(translated);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Trying to save at:"+cacheFileName);
		}
		
	}

	@Override
	public boolean has(I i) {
		return fileLocator.apply(i).exists();
	}

	@Override
	public O get(I i) {
		try {
			Path p = fileLocator.apply(i).toPath().toAbsolutePath();
			return stringTranslator.apply(Files.readString(p,StandardCharsets.ISO_8859_1));
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	public static<I,O> SplittedFileBasedCache<I, O> newInstance(Function<I, File> fileLocator, Function<O,String> objectToStringTranslator, Function<String, O> stringTranslator) {
		return new SplittedFileBasedCache<>(fileLocator, objectToStringTranslator, stringTranslator);
	}

	public void delete(I inputPair) {
		fileLocator.apply(inputPair).delete();
	}

	public static SplittedFileBasedCache<String, String> getStringToStringCache(File file) {
		
		return SplittedFileBasedCache.newInstance(x->
		{File res = new File(file.getAbsolutePath()+"/"+x);
		return res;
		}
		,
				Function.identity(),
				Function.identity());
	}

}
