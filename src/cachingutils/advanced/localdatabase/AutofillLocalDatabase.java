package cachingutils.advanced.localdatabase;

import cachingutils.advanced.FailableCache;
import cachingutils.advanced.autofilled.AutofilledCache;

public interface AutofillLocalDatabase<I,O> extends FailableCache<I, O>, AutofilledCache {
}
