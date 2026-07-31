import { DestroyRef, Injectable, InjectionToken, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  Event as RouterEvent,
  NavigationCancel,
  NavigationCancellationCode,
  NavigationEnd,
  NavigationError,
  NavigationSkipped,
  Router,
} from '@angular/router';
import { filter, forkJoin, take, timer } from 'rxjs';

const MINIMUM_INITIAL_LOADER_MS = 600;
const APPLICATION_STARTED_AT = Date.now();

export const INITIAL_LOADER_STARTED_AT = new InjectionToken<number>('INITIAL_LOADER_STARTED_AT', {
  providedIn: 'root',
  factory: () => APPLICATION_STARTED_AT,
});

@Injectable({ providedIn: 'root' })
export class InitialNavigationLoadingService {
  private readonly _isLoading = signal(true);
  readonly isLoading = this._isLoading.asReadonly();

  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly startedAt = inject(INITIAL_LOADER_STARTED_AT);

  constructor() {
    const remainingMinimumTime = Math.max(
      0,
      MINIMUM_INITIAL_LOADER_MS - (Date.now() - this.startedAt),
    );

    forkJoin([
      this.router.events.pipe(filter(isTerminalNavigationEvent), take(1)),
      timer(remainingMinimumTime),
    ])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this._isLoading.set(false));
  }
}

function isTerminalNavigationEvent(event: RouterEvent): boolean {
  if (
    event instanceof NavigationEnd ||
    event instanceof NavigationError ||
    event instanceof NavigationSkipped
  ) {
    return true;
  }

  return (
    event instanceof NavigationCancel &&
    event.code !== NavigationCancellationCode.Redirect &&
    event.code !== NavigationCancellationCode.SupersededByNewNavigation
  );
}
