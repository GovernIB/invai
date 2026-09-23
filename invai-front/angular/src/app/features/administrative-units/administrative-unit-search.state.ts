import { computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl } from '@angular/forms';
import { catchError, EMPTY, finalize, Subject, switchMap, timer } from 'rxjs';
import { AdministrativeUnit } from './administrative-units.model';
import { AdministrativeUnitsService } from './services/administrative-units.service';

export type AdministrativeUnitSearchAction =
  | { kind: 'open' | 'close' | 'retry' }
  | { kind: 'search'; query: string };

/** Per-owner state. Opening/searching never changes the form's selected code. */
export class AdministrativeUnitSearchState {
  private readonly service = inject(AdministrativeUnitsService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly requests = new Subject<{ query: string; delay: number } | null>();
  private readonly knownUnits = signal(new Map<string, AdministrativeUnit>());
  private readonly results = signal<AdministrativeUnit[]>([]);
  readonly loading = signal(false);
  readonly failed = signal(false);
  readonly total = signal(0);
  readonly query = signal('');
  readonly selectedCode = signal<string | null>(null);
  readonly options = computed(() => {
    const selected = this.knownUnits().get(this.selectedCode() ?? '');
    const results = this.results();
    return selected && !results.some(unit => unit.code === selected.code)
      ? [selected, ...results] : results;
  });
  readonly selectedLabel = computed(() => {
    const code = this.selectedCode();
    const unit = this.knownUnits().get(code ?? '');
    return unit ? `${unit.name} (${unit.code})` : code ?? '—';
  });

  constructor(control: FormControl<string | null>) {
    this.selectedCode.set(control.value);
    control.valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(code => this.selectedCode.set(code));
    this.requests.pipe(
      switchMap(request => {
        if (!request) return EMPTY;
        this.loading.set(true);
        this.failed.set(false);
        return timer(request.delay).pipe(
          switchMap(() => this.service.getPage({ page: 0, size: 20, sort: 'name,asc', search: request.query })),
          catchError(() => { this.failed.set(true); return EMPTY; }),
          finalize(() => this.loading.set(false)),
        );
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(page => {
      this.results.set(page.content);
      this.total.set(page.totalElements);
      this.knownUnits.update(known => {
        const selected = known.get(this.selectedCode() ?? '');
        return new Map([
          ...(selected ? [[selected.code, selected] as const] : []),
          ...page.content.map(unit => [unit.code, unit] as const),
        ]);
      });
    });
  }

  selectSnapshot(unit: AdministrativeUnit | null): void {
    this.handle({ kind: 'close' });
    if (unit) this.knownUnits.update(known => new Map(known).set(unit.code, unit));
    this.selectedCode.set(unit?.code ?? null);
  }

  handle(action: AdministrativeUnitSearchAction): void {
    if (action.kind === 'close') {
      this.requests.next(null);
      this.query.set('');
      this.results.set([]);
      this.total.set(0);
      this.failed.set(false);
      return;
    }
    const query = action.kind === 'search' ? action.query.trim() : action.kind === 'open' ? '' : this.query();
    this.query.set(query);
    this.requests.next({ query, delay: action.kind === 'search' ? 300 : 0 });
  }
}
