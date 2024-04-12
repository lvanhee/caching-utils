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

public class FilesInOneFolderBasedCache<I,O> implements Cache<I, O>{
	
	private final Function<I, String> fileLocator;
	private final Function<String,I> fileNameToObject;
	private final Function<String, O> stringTranslator;
	private final Function<O,String> objectToStringTranslator;
	private final File targetFolder;
	
	private final boolean isEmptyFilesAccepted;
	
	private final Set<I> existingObjects;
	
	private FilesInOneFolderBasedCache(
			Function<I, String> fileLocator, 
			Function<String, I> filenameToObjectLocator, 
			Function<O,String> objectToStringTranslator, 
			Function<String, O> stringTranslator,
			boolean isEmptyFilesAccepted,
			File targetFolder) {
		this.fileLocator = fileLocator;
		this.objectToStringTranslator = objectToStringTranslator;
		this.stringTranslator = stringTranslator;
		this.fileNameToObject = filenameToObjectLocator;
		this.targetFolder = targetFolder;
		this.isEmptyFilesAccepted = isEmptyFilesAccepted;
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
			writer = new BufferedWriter(new FileWriter(cacheFileName,StandardCharsets.UTF_8));

			writer.write(translated);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Trying to save at:"+cacheFileName);
		}
		
	}

	@Override
	public boolean has(I i) {
		boolean doFileExist = existingObjects.contains(i);
		if(!doFileExist)return false;
		/*if(!isEmptyFilesAccepted && getPathToObject(i).toFile().length()==0)
		{
			getPathToObject(i).toFile().delete();
			return false;
		}*/
		return true;
	}

	@Override
	public O get(I i) {
		if(!has(i))
			throw new Error();
		Path p = getPathToObject(i);
		String s = null;
		try {
			s = Files.readString(p,StandardCharsets.UTF_8);
			
			if(!isEmptyFilesAccepted && s!=null&&s.isEmpty()) {
				this.delete(i);
				throw new Error();
			}
			
			O res = stringTranslator.apply(Files.readString(p,StandardCharsets.UTF_8));
			
			//System.out.println(i+" worked ok!");
			return res;
		} catch (IOException e) {
			//e.printStackTrace();
			if(s!=null&&s.isEmpty())
				this.delete(i);
		}
		
		//quickfix for returning old values one last time and then deleting them
		try {
			O res = stringTranslator.apply(Files.readString(p,StandardCharsets.ISO_8859_1));
			
			
			System.out.println("Deleting:"+i+" "+res);
			this.delete(i);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	private Path getPathToObject(I i) {
		return Paths.get(targetFolder.getAbsolutePath()+"/"+fileLocator.apply(i)).toAbsolutePath();
	}

	public static<I,O> FilesInOneFolderBasedCache<I, O> newInstance(
			Function<I, String> fileLocator, 
			Function<String, I> filenameToObjectLocator, 
			Function<O,String> objectToStringTranslator, 
			Function<String, O> stringTranslator,
			File targetFolder) {
		return new FilesInOneFolderBasedCache<>(fileLocator, filenameToObjectLocator, objectToStringTranslator, stringTranslator, false, targetFolder);
	}
	
	public static<I,O> FilesInOneFolderBasedCache<I, O> newInstance(
			Function<I, String> fileLocator, 
			Function<String, I> filenameToObjectLocator, 
			Function<O,String> objectToStringTranslator, 
			Function<String, O> stringTranslator,
			boolean isEmptyFileAccepted,
			File targetFolder) {
		return new FilesInOneFolderBasedCache<>(fileLocator, filenameToObjectLocator, objectToStringTranslator, stringTranslator, isEmptyFileAccepted, targetFolder);
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
