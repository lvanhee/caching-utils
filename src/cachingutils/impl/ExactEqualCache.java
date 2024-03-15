package cachingutils.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cachingutils.Cache;

/**
 * 20210110
 * @author loisv
 *
 * @param <Key>
 * @param <Val>
 */
public class ExactEqualCache<Key, Val> implements Cache<Key, Val>{
	
	List<Key> keys = new ArrayList<>();
	List<Val> values = new ArrayList<>();
	
	private final int nbOfItems;
	
	private ExactEqualCache(int nbOfItems)
	{
		this.nbOfItems = nbOfItems;
	}

	public static<Key, Val> ExactEqualCache<Key, Val> newInstance(int i) {
		return new ExactEqualCache<>(i);
	}

	@Override
	public synchronized void add(Key i, Val o) {
		keys.add(i);
		values.add(o);
		if(keys.size()>nbOfItems) {keys.remove(0); values.remove(0);}
	}

	@Override
	public synchronized boolean has(Key i) {
		return keys.stream().anyMatch(x->i==x);
	}

	@Override
	public synchronized Val get(Key k) {
		
		for(int i = 0 ; i < keys.size(); i++)
		{
			if(keys.get(i)==k)
				return values.get(i);
		}
		throw new Error();
	}

	@Override
	public Set<Key> getAllCached() {
		throw new Error();
	}

	@Override
	public void delete(Key i) {
		throw new Error();
	}
	
	
	
	

}
