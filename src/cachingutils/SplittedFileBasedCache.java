package cachingutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Function;

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
	public void add(I i, O o) {
		File cacheFileName = fileLocator.apply(i);
		if(has(i))
			throw new Error();
		
		BufferedWriter writer;
		try {
			cacheFileName.createNewFile();
			writer = new BufferedWriter(new FileWriter(cacheFileName,StandardCharsets.ISO_8859_1));

			writer.write(objectToStringTranslator.apply(o));

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
			return stringTranslator.apply(Files.readString(fileLocator.apply(i).toPath(),StandardCharsets.ISO_8859_1));
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

}
