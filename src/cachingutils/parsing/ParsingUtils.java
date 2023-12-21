package cachingutils.parsing;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ParsingUtils {
	
	private static final String DEFAULT_STRING_SEPARATOR = ",";

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
		return listOfStringToParsableString(x,DEFAULT_STRING_SEPARATOR);
	}
	
	public static  String listOfStringToParsableString(List<String> x, String separator) {
		return toParsableString(x, y->y.toString(),separator);
	}
	
	
	public static<O> String toParsableString(List<O> l, Function<O, String>individualParser) {
		return toParsableString(l, individualParser,DEFAULT_STRING_SEPARATOR);
	}
	
	public static<O> String toParsableString(List<O> l, Function<O, String>individualParser, String separator) {
		if(l.isEmpty())return "";
		return l.stream().map(
				x->{
					String res = individualParser.apply(x);
					if(res.contains(separator)) 
						throw new Error("Parsed string contains the separator");
					return res;
				}).reduce((x,y)->(x+separator+y)).get();
	}

	public static List<String> fromParsableStringToList(String s, String separator)
	{
		return Arrays.asList(s.split(separator));
	}
	
	public static List<String> fromParsableStringToList(String s)
	{
		return Arrays.asList(s.split(DEFAULT_STRING_SEPARATOR));
	}

	public static<T> String collectionToParsableString(Collection<T> s, Function<T, String> parser) {
		return collectionToParsableString(s, parser,",");
	}

	public static<T> Set<T> fromParsableStringToSet(String input, Function<String, T> fromParsableString, String separator) {
		List<String> parsedStrings = fromParsableStringToList(input, separator);
		return parsedStrings.stream().map(x->fromParsableString.apply(x)).collect(Collectors.toSet());
	}

	public static<T> String collectionToParsableString(Collection<T> s, Function<T, String> parser,
			String separator) {
		return listOfStringToParsableString(s.stream().map(x->parser.apply(x)).collect(Collectors.toList()),separator);
	}

	public static Map<String, String> parseStringToMapOfStringToString(String s, String itemSeparator, String itemValueSeparator) {
		itemSeparator = itemSeparator.replaceAll("\\|", "\\\\|");
		List<String> str =  Arrays.asList(s.split(itemSeparator));
		Map<String, String> res = str.stream().collect(Collectors.toMap(
				x->{
					if(!x.contains(itemValueSeparator))
						throw new Error();
					return x.substring(0, x.indexOf(itemValueSeparator));},
				x->x.substring(x.indexOf(itemValueSeparator)+itemValueSeparator.length())));
		return res;
	}

}
