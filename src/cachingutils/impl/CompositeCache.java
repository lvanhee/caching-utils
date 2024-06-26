package cachingutils.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import cachingutils.Cache;

/**
 * Composite cache with main memory
 * @author loisv
 *
 * @param <I>
 * @param <O>
 */
public class CompositeCache<I,O> implements Cache<I,O> {
	
	private static enum CacheSynchronizationMode {
		//This cache synchronization mode is suited for caches of layered slowness, with the fastest cache being in position 0 and slower, but more exhaustive caches being in higher positions 
		UPWARDS}
	
	private Cache<I,O> mainCache;
	private List<Cache<I, O>> allCaches;
	private final CacheSynchronizationMode synchronizeCaches = CacheSynchronizationMode.UPWARDS;
	public CompositeCache(Cache<I,O>... caches) {
		allCaches=Arrays.asList(caches).stream().collect(Collectors.toList());
		mainCache = caches[0];
	}

	public static<I,O> CompositeCache<I, O> newInstance(Cache<I,O>...caches) {
		return new CompositeCache<I,O>(caches);
	}

	@Override
	public void add(I i, O o) {
		allCaches.stream().forEach(x->x.add(i, o));
	}

	@Override
	public boolean has(I i) {
		if(mainCache.has(i))return true;
		if(allCaches.stream().anyMatch(x->x.has(i)))
		{
			switch (synchronizeCaches) {
			case UPWARDS: {
				int a = 0;
				for(; a < allCaches.size(); a++)
				{
					if(allCaches.get(a).has(i)) break;
				}
				while(a>0)
				{
					allCaches.get(a-1).add(i,allCaches.get(a).get(i));
					a--;
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + synchronizeCaches);
			}
			/*if(synchronizeCaches)
			{
				Set<O> res = allCaches.stream().filter(x->x.has(i)).map(x->x.get(i)).collect(Collectors.toSet());
				if(res.size()!=1)
					throw new Error("Caches have diverging recollections");
				O o = res.iterator().next();
				allCaches.stream().filter(x->!x.has(i)).forEach(x->x.add(i, o));
			}*/

			return true;
		}
		return false;
	}

	@Override
	public O get(I i) {
		return mainCache.get(i);
		/*
		if(res.size()!=1) throw new Error();
		return res.iterator().next();*/
	}

	public void addCache(Cache<I, O> cache) {
		mainCache = cache;
		allCaches.add(0,cache);
	}

	public boolean hasInMainCache(I i) {
		return mainCache.has(i);
	}

	@Override
	public Set<I> getAllCached() {
		throw new Error();
	}

	@Override
	public void delete(I i) {
		throw new Error();
	}

	public void removeCache(Predicate<Cache<I,O>> toRemove) {
		allCaches = allCaches.stream().filter(x->!toRemove.test(x)).collect(Collectors.toList());
	}

}
