package cachingutils.parsing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParsingUtils {

	public static Map<String, String> toStringMap(String s, String itemSeparator, String itemKeySeparator) {
		return Arrays.asList(s.split(itemSeparator)).stream().collect(Collectors.toMap(x->x.substring(0, x.indexOf(itemKeySeparator)), x->x.substring(x.indexOf(itemKeySeparator)+1)));
	}

	public static String mapToParseableString(Map<String, String> input, String itemSeparator, String itemToKeySeparator) {
		if(input.keySet().stream().anyMatch(x->x.contains(itemSeparator)||x.contains(itemToKeySeparator)))
			throw new Error();
		
		if(input.values().stream().anyMatch(x->x.contains(itemSeparator)||x.contains(itemToKeySeparator)))
			throw new Error();
		
		return input.keySet().stream().map(x->x+itemToKeySeparator+input.get(x))
				.reduce("", (x,y)->x+itemSeparator+y).substring(itemSeparator.length());
	}

	public static  String listOfStringToParsableString(List<String> x) {
		return toParsableString(x, y->y.toString());
	}
	public static<O> String toParsableString(List<O> l, Function<O, String>individualParser) {
		return l.stream().map(
				x->{
					String res = individualParser.apply(x);
					if(res.contains(",")) 
						throw new Error("Parsed string contains the separator");
					return res;
				}).reduce((x,y)->(x+","+y)).get();
	}

	public static List<String> fromParsableStringToList(String s)
	{
		return Arrays.asList(s.split(","));
	}

}
