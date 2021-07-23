package cachingutils;

import java.util.HashMap;
import java.util.Map;

public class HashMapBasedCache<I,O> implements Cache<I, O> {

	private  final Map<Object,Object> m = new HashMap<>();
	@Override
	public synchronized void add(I i, O o) {
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

}
