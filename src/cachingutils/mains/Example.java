package cachingutils.mains;

import cachingutils.ExactEqualCache;

public class Example {
	
	public static void main(String args[])
	{
		String s = new String("input");
		String s2 = new String("input");
		ExactEqualCache<String, Object> exactCache = ExactEqualCache.newInstance(10);
		
		exactCache.add(s, 123);
		
		System.out.println(exactCache.has(s2));
	}

}
