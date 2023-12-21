package cachingutils.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cachingutils.Cache;
import scientigrapher.model.references.Reference;

public class FilesInOneFolderBasedCache<I,O> implements Cache<I, O>{
	
	private final Function<I, String> fileLocator;
	private final Function<String,I> fileNameToObject;
	private final Function<String, O> stringTranslator;
	private final Function<O,String> objectToStringTranslator;
	private final File targetFolder;
	
	private final Set<I> existingObjects;
	
	private FilesInOneFolderBasedCache(
			Function<I, String> fileLocator, 
			Function<String, I> filenameToObjectLocator, 
			Function<O,String> objectToStringTranslator, 
			Function<String, O> stringTranslator,
			File targetFolder) {
		this.fileLocator = fileLocator;
		this.objectToStringTranslator = objectToStringTranslator;
		this.stringTranslator = stringTranslator;
		this.fileNameToObject = filenameToObjectLocator;
		this.targetFolder = targetFolder;
		if(!targetFolder.exists())
			targetFolder.mkdirs();
		
		List<File> allFilesInFolder =Arrays.asList(targetFolder.listFiles()); 
		Set<String> allFileNamesInFolder = allFilesInFolder.stream().map(x->x.getName()).collect(Collectors.toSet());
		
		
		
		existingObjects = allFileNamesInFolder.stream().map(x->{
			//System.out.println("Applying on "+x);
			I res = filenameToObjectLocator.apply(x); 
			//System.out.println(x+"->"+res);
			return res;
		}).collect(Collectors.toSet());
		
		//System.out.println("Done!");
		
	//	if(existingObjects.toString().contains())
		
	}

	@Override
	public synchronized void add(I i, O o) {
		if(i.toString().contains("60019548"))
			System.out.print("");
		File cacheFileName = Paths.get(targetFolder.getAbsolutePath()+"/"+fileLocator.apply(i)).toFile();
		if(has(i))
			if(!o.equals(get(i)))
				throw new Error();
			else System.err.println("Duplicate add in cache:"+i);
			
		existingObjects.add(i);
		
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
		return existingObjects.contains(i);
	}

	@Override
	public O get(I i) {
		try {
			Path p = Paths.get(targetFolder.getAbsolutePath()+"/"+fileLocator.apply(i)).toAbsolutePath();
			return stringTranslator.apply(Files.readString(p,StandardCharsets.ISO_8859_1));
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	public static<I,O> FilesInOneFolderBasedCache<I, O> newInstance(
			Function<I, String> fileLocator, 
			Function<String, I> filenameToObjectLocator, 
			Function<O,String> objectToStringTranslator, 
			Function<String, O> stringTranslator,
			File targetFolder) {
		return new FilesInOneFolderBasedCache<>(fileLocator, filenameToObjectLocator, objectToStringTranslator, stringTranslator, targetFolder);
	}

	public void delete(I i) {
		Paths.get(targetFolder.getAbsolutePath()+"/"+fileLocator.apply(i)).toFile().delete();
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

	public Set<I> getAllCached() {
		return existingObjects;
	}

}
