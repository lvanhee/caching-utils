package cachingutils;


public interface Cache<I, O>{
	public void add(I i, O o);
	public boolean has(I i);
	public O get(I i);
}
