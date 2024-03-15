package cachingutils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import cachingutils.impl.CompositeCache;
import cachingutils.impl.FilesInOneFolderBasedCache;

public interface Cache<I, O>{
	public void add(I i, O o);
	public boolean has(I i);
	public O get(I i);
	public Set<I> getAllCached();
	public void delete(I i);

	public static<I,O> void transferCache(Cache<I, O> from,
			Cache<I, O> to, boolean deleteOldCache) {

		AtomicInteger remaining = new AtomicInteger(from.getAllCached().size());
		Set<I> allCached = from.getAllCached();
		for(I s:allCached)
		{
			if(!to.has(s))
			{
				try {
					O r = from.get(s);
					to.add(s, r);
				}
				catch (Error e) {
					if(!e.toString().contains("THE INPUT FILE CONTAINS AN AUTHOR DESCRIPTION INSTEAD OF A REFERENCE DESCRIPTION")&&
							!e.toString().contains("Not an author description")&&
							!e.toString().contains("EMPTY FILE")&&
							!e.toString().contains("Author description does not contain scopus id"))
					{
						e.printStackTrace();
						throw new Error();
					}

				}
			}
			from.delete(s);
			System.out.println("Done: "+s+"");
			System.out.println("Remaining:"+remaining.decrementAndGet());
		}
	}
}
