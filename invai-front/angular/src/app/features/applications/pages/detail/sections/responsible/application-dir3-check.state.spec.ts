import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';
import { PersonDir3Check } from '../../../../application-dir3.model';
import { ApplicationDir3CheckState } from './application-dir3-check.state';

const MATCH: PersonDir3Check = {
  personGroup: 'group',
  groupDir3: 'A1',
  admUnitCode: 'A1',
  matches: true,
};

describe('ApplicationDir3CheckState', () => {
  it('cancels stale checks on a different person or unit and on close', () => {
    const first = new Subject<PersonDir3Check>();
    const second = new Subject<PersonDir3Check>();
    const check = vi.fn().mockReturnValueOnce(first).mockReturnValueOnce(second);
    const state = new ApplicationDir3CheckState(check);
    state.update(true, 'first@caib.es', 'A1');
    expect(state.canSubmit()).toBe(false);
    state.update(true, 'second@caib.es', 'A2');
    first.next(MATCH);
    expect(state.result()).toBeNull();
    second.next({ ...MATCH, admUnitCode: 'A2', matches: false });
    expect(state.mismatch()).toBe(true);
    expect(state.canSubmit()).toBe(true);
    state.reset();
    second.next(MATCH);
    expect(state.result()).toBeNull();
  });

  it('rechecks on retry and reopening but not on unrelated form changes', () => {
    const check = vi.fn(() => of(MATCH));
    const state = new ApplicationDir3CheckState(check);
    state.update(true, 'person@caib.es', 'A1');
    state.update(true, 'person@caib.es', 'A1');
    expect(check).toHaveBeenCalledOnce();
    state.update(true, 'person@caib.es', 'A1', true);
    state.reset();
    state.update(true, 'person@caib.es', 'A1');
    expect(check).toHaveBeenCalledTimes(3);
  });

  it('blocks errors and distinguishes a successful inconclusive response', () => {
    const check = vi
      .fn()
      .mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 403 })))
      .mockReturnValueOnce(of({ ...MATCH, groupDir3: null, matches: false }));
    const state = new ApplicationDir3CheckState(check);
    state.update(true, 'person@caib.es', 'A1');
    expect(state.phase()).toBe('error');
    expect(state.denied()).toBe(true);
    expect(state.canSubmit()).toBe(false);
    state.update(true, 'person@caib.es', 'A1', true);
    expect(state.denied()).toBe(false);
    expect(state.mismatch()).toBe(true);
    expect(state.canSubmit()).toBe(true);
  });

  it('skips external people and absent units but waits for a valid CAIB selection', () => {
    const check = vi.fn(() => of(MATCH));
    const state = new ApplicationDir3CheckState(check);
    state.update(false, 'external@example.org', 'A1');
    expect(state.canSubmit()).toBe(true);
    state.update(true, 'person@caib.es', '');
    expect(state.canSubmit()).toBe(true);
    state.update(true, '', 'A1');
    expect(state.phase()).toBe('idle');
    expect(state.canSubmit()).toBe(false);
    expect(check).not.toHaveBeenCalled();
  });
});
