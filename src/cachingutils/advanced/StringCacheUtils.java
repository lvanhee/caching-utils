package cachingutils.advanced;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
	
	public static String listOfLongToString(List<Long> l)
	{
		return l.toString();
	}
	
	public static List<Long> stringToListOfLong(String s)
	{
		if(s.equals("[]"))return new ArrayList<>();
		String removeHeaders = s.substring(1,s.length()-1);
		
		return Arrays.asList(removeHeaders.split(",")).stream().map(x->Long.parseLong(x.trim())).collect(Collectors.toList());
	}
	
	public static String separatorUrlSet()
	{
		return "\t";
	}

	public static String listOfStringToString(List<String> l) {
		if(l.isEmpty()) return "[]";
		if(l.stream().anyMatch(x->x.contains(",")))throw new Error();
		String res = l.stream().reduce("", (x,y)->x+","+y).substring(1);
		return res;
	}

	public static List<String> stringToListOfString(String x) {
		if(x.equals("[]"))return new ArrayList<>();
		return Arrays.asList(x.split(","));
	}

}
