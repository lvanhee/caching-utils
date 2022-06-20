package cachingutils.advanced;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StringCacheUtils {
	public static String urlSetToString(Set<URL> urls)
	{
		if(urls.isEmpty())return "[]";
		String res = "";
		for(URL u: urls)
			if(u.toString().contains("\\|"))
			{
				throw new Error();
			}
			else res+=u.toString()+"||";
		return res.substring(0,res.length()-2);
	}
	
	public static Set<URL> stringToUrlSet(String s){
		if(s.equals("[]"))return new HashSet<URL>();
		return Arrays.asList(s.split("\\|\\|")).stream().map(x->{
			try {
				return new URL(s);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new Error();
			}
		}).collect(Collectors.toSet());
	}
	
	public static String separatorUrlSet()
	{
		return "\t";
	}

}
