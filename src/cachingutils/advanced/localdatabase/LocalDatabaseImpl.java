package cachingutils.advanced.localdatabase;

import java.net.URL;
import java.util.function.Function;

import cachingutils.Cache;
import cachingutils.advanced.failable.AttemptOutcome;
import cachingutils.advanced.failable.FailedDatabaseProcessingOutcome;
import cachingutils.advanced.failable.SuccessfulOutcome;
import cachingutils.impl.TextFileBasedCache;

public class LocalDatabaseImpl<I,O> implements AutofillLocalDatabase<I, O> {
	private final Cache<I, O> cache;
	private final Function<I, AttemptOutcome<O>> keySearcher;
	
	private LocalDatabaseImpl(Cache<I, O> cache, Function<I, AttemptOutcome<O>>filler) {
		this.cache = cache;
		this.keySearcher = filler;
	}

	@Override
	public AttemptOutcome<O> get(I i) {
		if(!cache.has(i))
		{
			AttemptOutcome<O> out = keySearcher.apply(i);
			if(out instanceof FailedDatabaseProcessingOutcome)
				return out;
			cache.add(i, ((SuccessfulOutcome<O>)out).getResult());
		}
		
		return SuccessfulOutcome.newInstance(cache.get(i));
	}

	@Override
	public void add(I i, AttemptOutcome<O> o) {
		if(o instanceof SuccessfulOutcome)
			cache.add(i, ((SuccessfulOutcome<O>)o).getResult());
	}

	@Override
	public boolean has(I i) {
		return cache.has(i);
	}

	public static<I,O> AutofillLocalDatabase<I, O> newInstance(Cache<I, O> cache, Function<I, AttemptOutcome<O>>filler) {
		return new LocalDatabaseImpl<>(cache, filler);
	}
	
	
}
