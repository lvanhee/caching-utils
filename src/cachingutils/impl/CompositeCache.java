package cachingutils.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
	
	private Cache<I,O> mainCache;
	private final List<Cache<I, O>> allCaches;
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
			Set<O> res = allCaches.stream().filter(x->x.has(i)).map(x->x.get(i)).collect(Collectors.toSet());
			O o = res.iterator().next();
			allCaches.stream().filter(x->!x.has(i)).forEach(x->x.add(i, o));
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

}
