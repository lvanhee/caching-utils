package cachingutils.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cachingutils.Cache;

public class HashMapBasedCache<I,O> implements Cache<I, O> {

	private  final Map<Object,Object> m = new HashMap<>();
	@Override
	public synchronized void add(I i, O o) {
	//	System.out.println(m.size());
		m.put(i,o);
	}

	@Override
	public synchronized boolean has(I i) {
		return m.containsKey(i);
	}

	@Override
	public synchronized O get(I i) {
		return (O)m.get(i);
	}

	public synchronized static<I,O> Cache<I,O> newInstance() {
		return new HashMapBasedCache<>();
	}
	
	@Override
	public Set<I> getAllCached() {
		throw new Error();
	}

	@Override
	public void delete(I i) {
		throw new Error();
	}
	

}
