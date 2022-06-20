package cachingutils.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import cachingutils.Cache;
import textprocessing.TextProcessingUtils;

public class TextFileBasedCache<I,O> implements Cache<I, O> {

	private static final String ITEM_SEPARATOR = "\n";

	private final File fileToSaveInto;
	private final Map<I,O> m;
	private final Function<I, String> iToString;
	private final Function<String,I> parserStringToi;
	private final Function<String,O> parserStringToO;
	private final Function<O,String> oToString;
	private final String inlineSeparator;
	private final boolean outputBeforeInput;

	private TextFileBasedCache(File fileToSaveInto, 
			Function<I, String> iToString,
			Function<String,I> parserStringToi,
			Function<O,String> oToString,
			Function<String,O> parserStringToO,
			String inlineSeparator,
			boolean outputBeforeInput
			)
	{
		this.fileToSaveInto = fileToSaveInto;
		this.iToString = iToString;
		this.parserStringToi = parserStringToi;
		this.oToString = oToString;
		this.parserStringToO = parserStringToO;
		this.inlineSeparator = inlineSeparator;
		this.outputBeforeInput = outputBeforeInput;
		
		int inputIndex = outputBeforeInput?1:0;
		int outputIndex = 1-inputIndex;

		try {
			if(!fileToSaveInto.exists())
				m = new HashMap<>();
			else
			{
				String allContents = Files.readString(this.fileToSaveInto.toPath(),StandardCharsets.ISO_8859_1);
				
				Set<String> allItems;
				if(allContents.isBlank())
					allItems = new HashSet<>();
				else
					allItems= Arrays.asList(allContents.split(ITEM_SEPARATOR)).stream().collect(Collectors.toSet()); 
				m = allItems.stream().sorted()
						//.parallelStream()
						.collect(Collectors.toMap(
						x->{
							//System.out.println(x);
							String left = x.split(TextProcessingUtils.toRegex(inlineSeparator))[inputIndex];
							
							return parserStringToi.apply(left);
						},
						x-> 
						{
							String[] splitted = x.split(TextProcessingUtils.toRegex(inlineSeparator));
							if(splitted.length<2)
								throw new Error();
							return (O)parserStringToO.apply(splitted[outputIndex]);	
						}));
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}
	@Override
	public synchronized void add(I i, O o) {
		if(m.containsKey(i))
			if(!m.get(i).equals(o))
				throw new Error();

		try {
			if(!fileToSaveInto.exists())
			{
				Files.createDirectories(Paths.get(fileToSaveInto.getParent()));
				fileToSaveInto.createNewFile();
			}

			FileWriter fw;



			fw = new FileWriter(fileToSaveInto, StandardCharsets.ISO_8859_1, true);
			BufferedWriter bw = new BufferedWriter(fw);

			String translatedI = iToString.apply(i);
			String translatedO = oToString.apply(o);

			if(translatedI.contains(inlineSeparator)||translatedO.contains(inlineSeparator)||
					translatedI.contains("\n")||translatedO.contains("\n"))
				throw new Error("Character used for delimitation used by the translator");

			String toAdd = iToString.apply(i)+inlineSeparator+oToString.apply(o)+ITEM_SEPARATOR; 
			if(outputBeforeInput)
				toAdd = oToString.apply(o)+inlineSeparator+iToString.apply(i)+ITEM_SEPARATOR;
			bw.write(toAdd);
			bw.close();   
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
		
		m.put(i,o);

	}

	@Override
	public synchronized boolean has(I i) {
		return m.containsKey(i);
	}

	@Override
	public synchronized O get(I i) {
		return m.get(i);
	}
	public static <I,O> TextFileBasedCache<I, O> newInstance(File fileToSaveInto, 
			Function<I, String> iToString,
			Function<String,I> parserStringToi,
			Function<O,String> oToString,
			Function<String,O> parserStringToO,
			String inlineSeparator
			) {
		return new TextFileBasedCache<>(fileToSaveInto, iToString, parserStringToi, oToString, parserStringToO, inlineSeparator,false);
	}
	
	public static <I,O> TextFileBasedCache<I, O> newInstance(File fileToSaveInto, 
			Function<I, String> iToString,
			Function<String,I> parserStringToi,
			Function<O,String> oToString,
			Function<String,O> parserStringToO,
			String inlineSeparator, boolean flipped
			) {
		return new TextFileBasedCache<>(fileToSaveInto, iToString, parserStringToi, oToString, parserStringToO, inlineSeparator,flipped);
	}

	private static final AtomicBoolean needsToBeFlushedOut = new AtomicBoolean(false);

	public void replace(I i, O o) {
		m.put(i, o);

		synchronized (needsToBeFlushedOut) {

			if(!needsToBeFlushedOut.get())
			{


				needsToBeFlushedOut.set(true);
				new Thread(
						()->{
							Thread.currentThread().setName("Waiting and flushing text file based cache:"+fileToSaveInto);
							try {
								Thread.sleep(1000000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							fuseFileToM();
							needsToBeFlushedOut.set(false);
						}
						).start();
			}
		}
	}
	private synchronized void fuseFileToM() {
		try {

			if(!fileToSaveInto.exists())
				fileToSaveInto.createNewFile();

			fileToSaveInto.delete();

		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}

		Map<I, O> mCopy = new HashMap<>();
		mCopy.putAll(m);
		m.clear();

		for(I i: mCopy.keySet())
			add(i,mCopy.get(i));


	}
	
	public synchronized String toString()
	{
		return m.size()+" "+this.fileToSaveInto+": "+this.m;
	}
	public void flush() {
		fuseFileToM();
	}
	public void addOrReplace(I i, O o) {
		if(has(i))
			replace(i, o);
		else add(i, o);
	}

}
