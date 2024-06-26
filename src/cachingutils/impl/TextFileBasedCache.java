package cachingutils.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import cachingutils.Cache;

public class TextFileBasedCache<I,O> implements Cache<I, O> {
	
	private enum ExportMode{IMMEDIATE, REGULAR}

	private static final String ITEM_SEPARATOR = "\n";

	private final File fileToSaveInto;
	private Map<I,O> m=null;
	private final Function<I, String> iToString;
	private final Function<String,I> parserStringToi;
	private final Function<String,O> parserStringToO;
	private final Function<O,String> oToString;
	private final String inlineSeparator;
	private final boolean outputBeforeInput;
	private final AtomicBoolean isLoadingOver = new AtomicBoolean(false);
	
	private final ExportMode exportMode;
	
	private final Map<I,O> toExportInNextFlush = new HashMap<>();

	private TextFileBasedCache(File fileToSaveInto, 
			Function<I, String> iToString,
			Function<String,I> parserStringToi,
			Function<O,String> oToString,
			Function<String,O> parserStringToO,
			String inputToOutputSeparator,
			boolean outputBeforeInput,
			ExportMode exportMode
			)
	{
		if(inputToOutputSeparator.equals(ITEM_SEPARATOR))throw new Error("Inline separator between input and output matches the separator between io-pairs");
		this.fileToSaveInto = fileToSaveInto;
		this.iToString = iToString;
		this.parserStringToi = parserStringToi;
		this.oToString = oToString;
		this.parserStringToO = parserStringToO;
		this.inlineSeparator = inputToOutputSeparator;
		this.outputBeforeInput = outputBeforeInput;
		this.exportMode = exportMode;
		
		int inputIndex = outputBeforeInput?1:0;
		int outputIndex = 1-inputIndex;

		try {
			if(!fileToSaveInto.exists())
			{
				m = new HashMap<>();
				isLoadingOver.set(true);
			}
			else
			{
				System.out.println("Loading:"+this.fileToSaveInto.getAbsolutePath());
				String allContents = Files.readString(this.fileToSaveInto.toPath(),StandardCharsets.UTF_8);
				
				Set<String> allItems;
				if(allContents.isBlank())
					allItems = new HashSet<>();
				else
					allItems= Arrays.asList(allContents.split(ITEM_SEPARATOR)).stream().collect(Collectors.toSet());
				String regex = java.util.regex.Pattern.quote(inputToOutputSeparator);

				Set<List<String>> splittedItems = allItems.stream().map(x->Arrays.asList(x.split(regex))).collect(Collectors.toSet());

				new Thread(()->{
					Thread.currentThread().setName("Loading:"+fileToSaveInto.getAbsolutePath());
					ConcurrentHashMap<I, O> tmpMap = new ConcurrentHashMap<>(splittedItems.size());
					long start = System.currentTimeMillis();
					splittedItems.stream().forEach(x->{
						I i = null;

						//synchronized (splittedItems) {
						//System.out.println(x);
						String left = x.get(inputIndex);
						i = parserStringToi.apply(left);

						O o = null;
						if(x.size()==1)
							o = parserStringToO.apply("");
						else o = parserStringToO.apply(x.get(outputIndex));

						tmpMap.put(i, o);


					});
					Map<I, O> tmp2 = new HashMap<>(tmpMap.size());
					tmp2.putAll(tmpMap);
					m = tmp2;
					/*m = splittedItems.parallelStream()
							//.parallelStream()
							.collect(Collectors.toMap(
									x->{
										//System.out.println(x);
										String left = x.get(inputIndex);

										return parserStringToi.apply(left);
									},
									x-> 
									{

										if(x.size()<2)
											throw new Error();
										return parserStringToO.apply(x.get(outputIndex));	
									}));*/
					
					System.out.println("Pre-processed:"+this.fileToSaveInto+" "+m.size()+", time: "+((System.currentTimeMillis()-start))/1000);
					synchronized (isLoadingOver) {
						isLoadingOver.set(true);
						isLoadingOver.notifyAll();
					}

				}).start();;


			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}
	}
	
	
	private Thread waitForCompletion = null; 
	@Override
	public synchronized void add(I i, O o) {
		if(m.containsKey(i))
			if(!m.get(i).equals(o))
				throw new Error();
		
		m.put(i, o);
		toExportInNextFlush.put(i, o);
		

		if(exportMode.equals(ExportMode.IMMEDIATE))
		{
			flushNewInput();
		}
		else {

			if(waitForCompletion==null || !waitForCompletion.isAlive()) {
				waitForCompletion = new Thread(()->{
					try {
						//System.out.println("Waiting before flushing");
						Thread.currentThread().setName("Waiting before flushing");
						Thread.sleep(5000);
						//System.out.println("Flushing");
						flushNewInput();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				});
				waitForCompletion.start();
			}
		}

	}

	private synchronized void flushNewInput() {
		StringBuilder strToExport = new StringBuilder();
		
		
		if(!fileToSaveInto.exists())
		{
			try {
				Files.createDirectories(Paths.get(fileToSaveInto.getParent()));
			fileToSaveInto.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
		}

		for(I i:toExportInNextFlush.keySet())
		{
			O o = toExportInNextFlush.get(i);
			String translatedI = iToString.apply(i);
			String translatedO = oToString.apply(o);

			if(translatedI.contains(inlineSeparator)||translatedO.contains(inlineSeparator)||
					translatedI.contains("\n")||translatedO.contains("\n"))
				throw new Error("Separator used for delimitation of input and output in the file is in use in the translated strings");

			String toAdd = iToString.apply(i)+inlineSeparator+oToString.apply(o)+ITEM_SEPARATOR; 
			if(outputBeforeInput)
				toAdd = oToString.apply(o)+inlineSeparator+iToString.apply(i)+ITEM_SEPARATOR;
			
			strToExport.append(toAdd);
 

		}
		try {
			FileWriter fw = new FileWriter(fileToSaveInto, StandardCharsets.UTF_8, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(strToExport.toString());
			bw.close();  
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}

		toExportInNextFlush.clear();
	}

	@Override
	public synchronized boolean has(I i) {
		synchronized (isLoadingOver) {
			while(!isLoadingOver.get())
				try {
					isLoadingOver.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new Error();
				}
		}
		return m.containsKey(i);
	}

	@Override
	public synchronized O get(I i) {
		return m.get(i);
	}
	public static  <I,O> TextFileBasedCache<I, O> newInstance(File fileToSaveInto, 
			Function<I, String> iToString,
			Function<String,I> parserStringToi,
			Function<O,String> oToString,
			Function<String,O> parserStringToO,
			String inlineSeparator
			) {
		return new TextFileBasedCache<>(fileToSaveInto, iToString, parserStringToi, oToString, parserStringToO, inlineSeparator,false, ExportMode.REGULAR);
	}
	
	public static <I,O> TextFileBasedCache<I, O> newInstance(
			File fileToSaveInto, 
			Function<I, String> iToString,
			Function<String,I> parserStringToi,
			Function<O,String> oToString,
			Function<String,O> parserStringToO,
			String inlineSeparator, boolean flipped
			) {
		return new TextFileBasedCache<>(fileToSaveInto, iToString, parserStringToi, oToString, parserStringToO, inlineSeparator,flipped, ExportMode.REGULAR);
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
	
	@Override
	public Set<I> getAllCached() {
		throw new Error();
	}

	@Override
	public void delete(I i) {
		throw new Error();
	}

	public File getFile() {
		return fileToSaveInto;
	}
	

}
