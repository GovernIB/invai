import { Observable, Subject, of } from 'rxjs';
import { cachedRequest } from './service-cache.utils';

describe('cachedRequest', () => {
  let cache: Map<string, Observable<string>>;

  beforeEach(() => {
    cache = new Map();
    vi.useFakeTimers();
    vi.setSystemTime(0);
  });

  afterEach(() => vi.useRealTimers());

  it('preserves unlimited reuse when no TTL is configured', () => {
    const factory = vi.fn(() => of('value'));
    cachedRequest(cache, 'one', factory).subscribe();
    vi.advanceTimersByTime(24 * 60 * 60 * 1000);
    const result = vi.fn();
    cachedRequest(cache, 'one', factory).subscribe(result);
    expect(factory).toHaveBeenCalledOnce();
    expect(result).toHaveBeenCalledWith('value');
  });

  it('starts the TTL at reception and does not renew it on cache reads', () => {
    const source = new Subject<string>();
    const factory = vi.fn(() => source);
    const first = cachedRequest(cache, 'one', factory, 300000);
    const result = vi.fn();
    first.subscribe(result);
    vi.advanceTimersByTime(600000);
    expect(cachedRequest(cache, 'one', factory, 300000)).toBe(first);
    source.next('value');
    source.complete();

    vi.advanceTimersByTime(299999);
    cachedRequest(cache, 'one', factory, 300000).subscribe(result);
    expect(result).toHaveBeenCalledTimes(2);
    expect(factory).toHaveBeenCalledOnce();

    vi.advanceTimersByTime(1);
    expect(factory).toHaveBeenCalledOnce();
    expect(cachedRequest(cache, 'one', factory, 300000)).not.toBe(first);
    expect(factory).toHaveBeenCalledTimes(2);
  });

  it('evicts only the failed entry and permits a retry', () => {
    cachedRequest(cache, 'other', () => of('other')).subscribe();
    const source = new Subject<string>();
    cachedRequest(cache, 'one', () => source, 300000).subscribe({ error: () => undefined });
    source.error(new Error('failed'));
    expect(cache.has('one')).toBe(false);
    expect(cache.has('other')).toBe(true);
    const result = vi.fn();
    cachedRequest(cache, 'one', () => of('retried'), 300000).subscribe(result);
    expect(result).toHaveBeenCalledWith('retried');
  });

  it.each(['success', 'error'])('isolates a replaced request that finishes with %s', (outcome) => {
    const old = new Subject<string>();
    cachedRequest(cache, 'one', () => old, 300000).subscribe({ error: () => undefined });
    cache.delete('one');
    const current = cachedRequest(cache, 'one', () => of('fresh'), 300000);
    current.subscribe();
    vi.advanceTimersByTime(200000);
    if (outcome === 'success') {
      old.next('stale');
      old.complete();
    } else {
      old.error(new Error('stale failure'));
    }

    const result = vi.fn();
    const factory = vi.fn(() => of('next'));
    expect(cachedRequest(cache, 'one', factory, 300000)).toBe(current);
    current.subscribe(result);
    expect(result).toHaveBeenCalledWith('fresh');
    expect(factory).not.toHaveBeenCalled();
    vi.advanceTimersByTime(100000);
    cachedRequest(cache, 'one', factory, 300000).subscribe(result);
    expect(factory).toHaveBeenCalledOnce();
    expect(result).toHaveBeenLastCalledWith('next');
  });
});
