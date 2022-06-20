package cachingutils.advanced;

import cachingutils.Cache;
import cachingutils.advanced.failable.AttemptOutcome;

public interface FailableCache<I,O> extends Cache<I, AttemptOutcome<O>> {

}
