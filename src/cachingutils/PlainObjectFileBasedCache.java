package cachingutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlainObjectFileBasedCache<T> {

	private T obj;
	private final File cacheFile;
	public PlainObjectFileBasedCache(File cacheFile, T obj) {
		this.obj = obj;
		this.cacheFile = cacheFile;
	}
	
	private static File getBackupFileFrom(File f)
	{
		return new File(f.getAbsolutePath()+"_bk");
	}

	public static<T> PlainObjectFileBasedCache<T> loadFromFile(File cacheFile, Supplier<T> defaultSupplier) {
		try {
			if(cacheFile.exists()) {
				FileInputStream fileIn = new FileInputStream(cacheFile);
				ObjectInputStream objectIn = new ObjectInputStream(fileIn);

				T obj = (T)objectIn.readObject();
				objectIn.close();
				return new PlainObjectFileBasedCache<T>(cacheFile,obj);
			}
			else 
			{
				return new PlainObjectFileBasedCache<T>(cacheFile, defaultSupplier.get());
			}
		} catch (Exception ex) {

			if(cacheFile.getName().endsWith("_bk"))
			{
				ex.printStackTrace();
				cacheFile.delete();
				System.out.println("Main and backup failed, replacing by the default object:"+cacheFile+" "+cacheFile);
			}
			else
			{
				System.out.println("Issue reading the file, loading the backup version"+cacheFile.getAbsolutePath());
				cacheFile.delete();
				return loadFromFile(getBackupFileFrom(cacheFile), defaultSupplier);
			}
			
			
			
		}
		return new PlainObjectFileBasedCache<T>(cacheFile,defaultSupplier.get());
	}

	public T get() {
		return obj;
	}

	public synchronized void doAndUpdate(Consumer<T> cons) {
		cons.accept(obj);
		updateLocalFile();
	}

	public void updateLocalFile() {
		try {
			if(!this.cacheFile.exists())
				cacheFile.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(cacheFile);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(obj);
			objectOut.flush();
			objectOut.close(); 
			
			fileOut = new FileOutputStream(getBackupFileFrom(cacheFile));
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(obj);
			objectOut.flush();
			objectOut.close(); 
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Error();
		}
	}
}
