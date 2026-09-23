import { computed, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, Subscription } from 'rxjs';
import { PersonDir3Check } from '../../../../application-dir3.model';

export type Dir3CheckPhase = 'idle' | 'not-applicable' | 'loading' | 'ready' | 'error';

/** One dialog's live check; search/list caches are deliberately not consulted. */
export class ApplicationDir3CheckState {
  readonly phase = signal<Dir3CheckPhase>('not-applicable');
  readonly result = signal<PersonDir3Check | null>(null);
  readonly denied = signal(false);
  readonly canSubmit = computed(() => ['ready', 'not-applicable'].includes(this.phase()));
  readonly mismatch = computed(() => this.phase() === 'ready' && !this.result()?.matches);
  private key = '';
  private request: Subscription | undefined;

  constructor(
    private readonly check: (email: string, unit: string) => Observable<PersonDir3Check>,
  ) {}

  update(personalCaib: boolean, email: string, unit: string, force = false): boolean {
    const key = JSON.stringify([personalCaib, email, unit]);
    if (!force && this.key === key) return false;
    this.reset();
    this.key = key;
    if (!personalCaib || !unit) return true;
    if (!email) {
      this.phase.set('idle');
      return true;
    }
    this.phase.set('loading');
    this.request = this.check(email, unit).subscribe({
      next: (result) => {
        this.result.set(result);
        this.phase.set('ready');
      },
      error: (error: unknown) => {
        this.denied.set(error instanceof HttpErrorResponse && error.status === 403);
        this.phase.set('error');
      },
    });
    return true;
  }

  reset(): void {
    this.request?.unsubscribe();
    this.request = undefined;
    this.key = '';
    this.result.set(null);
    this.denied.set(false);
    this.phase.set('not-applicable');
  }
}
