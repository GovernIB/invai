import { TestBed } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { SpringPage } from '@models/page.model';
import { Subject } from 'rxjs';
import { AdministrativeUnitSearchState } from './administrative-unit-search.state';
import { AdministrativeUnit } from './administrative-units.model';
import { AdministrativeUnitsService } from './services/administrative-units.service';

const original = { code: 'D1', name: 'Department', level: 1, parentCode: null };
const other = { code: 'U2', name: 'Another unit', level: 4, parentCode: 'D2' };
const page = (content: AdministrativeUnit[]) => ({ content, totalElements: 42 }) as SpringPage<AdministrativeUnit>;

describe('AdministrativeUnitSearchState', () => {
  let state: AdministrativeUnitSearchState;
  let control: FormControl<string | null>;
  let getPage: ReturnType<typeof vi.fn>;
  beforeEach(() => {
    vi.useFakeTimers();
    getPage = vi.fn();
    TestBed.configureTestingModule({ providers: [{ provide: AdministrativeUnitsService, useValue: { getPage } }] });
    control = new FormControl<string | null>('D1');
    state = TestBed.runInInjectionContext(() => new AdministrativeUnitSearchState(control));
    state.selectSnapshot(original);
  });
  afterEach(() => vi.useRealTimers());

  it('loads only on open and keeps selected labels outside the result page', () => {
    const response = new Subject<SpringPage<AdministrativeUnit>>();
    getPage.mockReturnValue(response);
    expect(getPage).not.toHaveBeenCalled();
    state.handle({ kind: 'open' });
    vi.advanceTimersByTime(0);
    expect(getPage).toHaveBeenCalledWith({ page: 0, size: 20, sort: 'name,asc', search: '' });
    response.next(page([other]));
    response.complete();
    expect(state.options()).toEqual([original, other]);
    expect(state.total()).toBe(42);
    expect(state.selectedLabel()).toBe('Department (D1)');
    expect(control.value).toBe('D1');
  });

  it('cancels immediately on new input and debounces trimmed queries for 300ms', () => {
    const oldResponse = new Subject<SpringPage<AdministrativeUnit>>();
    const newResponse = new Subject<SpringPage<AdministrativeUnit>>();
    getPage.mockReturnValueOnce(oldResponse).mockReturnValueOnce(newResponse);
    state.handle({ kind: 'open' });
    vi.advanceTimersByTime(0);
    state.handle({ kind: 'search', query: '  Another  ' });
    expect(oldResponse.observed).toBe(false);
    vi.advanceTimersByTime(299);
    expect(getPage).toHaveBeenCalledTimes(1);
    vi.advanceTimersByTime(1);
    expect(getPage).toHaveBeenLastCalledWith(expect.objectContaining({ search: 'Another' }));
    oldResponse.next(page([original]));
    newResponse.next(page([other]));
    expect(state.options()).toEqual([original, other]);
  });

  it('cancels pending searches on close and restores the saved selection without loading', () => {
    state.handle({ kind: 'search', query: 'pending' });
    control.setValue('U2');
    state.selectSnapshot(original);
    control.setValue('D1', { emitEvent: false });
    vi.advanceTimersByTime(300);
    expect(getPage).not.toHaveBeenCalled();
    expect(state.selectedLabel()).toBe('Department (D1)');
    expect(state.query()).toBe('');
    expect(state.loading()).toBe(false);
  });

  it('preserves selection through an error and retries the same query', () => {
    const failed = new Subject<SpringPage<AdministrativeUnit>>();
    const retry = new Subject<SpringPage<AdministrativeUnit>>();
    getPage.mockReturnValueOnce(failed).mockReturnValueOnce(retry);
    state.handle({ kind: 'search', query: 'Another' });
    vi.advanceTimersByTime(300);
    failed.error(new Error('Unavailable'));
    expect(state.failed()).toBe(true);
    expect(control.enabled).toBe(true);
    expect(control.value).toBe('D1');
    state.handle({ kind: 'retry' });
    vi.advanceTimersByTime(0);
    retry.next(page([other]));
    retry.complete();
    expect(state.failed()).toBe(false);
    expect(state.selectedLabel()).toBe('Department (D1)');
  });
});
