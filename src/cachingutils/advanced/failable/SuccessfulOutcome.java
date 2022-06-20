package cachingutils.advanced.failable;


public class SuccessfulOutcome<O> implements AttemptOutcome<O> {
	private final O o;
	
	private SuccessfulOutcome(O o)
	{
		this.o = o;
	}
	public static<O> SuccessfulOutcome<O> newInstance(O o) {
		return new SuccessfulOutcome<>(o);
	}
	public O getResult() {
		return o;
	}
	
	public String toString() {
		return "SuccessfulOutcome:"+o;
	}
}
