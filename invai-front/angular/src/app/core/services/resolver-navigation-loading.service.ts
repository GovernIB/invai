import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  Event as RouterEvent,
  NavigationCancel,
  NavigationCancellationCode,
  NavigationEnd,
  NavigationError,
  NavigationSkipped,
  ResolveEnd,
  ResolveStart,
  Router,
} from '@angular/router';

const RESOLVER_LOADING_DELAY_MS = 150;

@Injectable({ providedIn: 'root' })
export class ResolverNavigationLoadingService {
  private readonly _isLoading = signal(false);
  readonly isLoading = this._isLoading.asReadonly();

  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  private activeNavigationId: number | null = null;
  private isWaitingForContinuedNavigation = false;
  private showTimer: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    this.router.events
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event) => this.handleRouterEvent(event));

    this.destroyRef.onDestroy(() => this.clearShowTimer());
  }

  private handleRouterEvent(event: RouterEvent): void {
    if (event instanceof ResolveStart) {
      this.startResolving(event.id);
      return;
    }

    if (event instanceof ResolveEnd) {
      this.finishResolving(event.id);
      return;
    }

    if (event instanceof NavigationCancel) {
      this.handleNavigationCancel(event);
      return;
    }

    if (
      event instanceof NavigationEnd ||
      event instanceof NavigationError ||
      event instanceof NavigationSkipped
    ) {
      this.finishTerminalNavigation(event.id);
    }
  }

  private startResolving(navigationId: number): void {
    const isContinuation =
      this.activeNavigationId !== null || this.isWaitingForContinuedNavigation;

    this.activeNavigationId = navigationId;
    this.isWaitingForContinuedNavigation = false;

    if (isContinuation) return;

    this.clearShowTimer();
    this._isLoading.set(false);
    this.showTimer = setTimeout(() => {
      this.showTimer = null;
      if (this.activeNavigationId !== null || this.isWaitingForContinuedNavigation) {
        this._isLoading.set(true);
      }
    }, RESOLVER_LOADING_DELAY_MS);
  }

  private finishResolving(navigationId: number): void {
    if (navigationId !== this.activeNavigationId) return;

    this.reset();
  }

  private handleNavigationCancel(event: NavigationCancel): void {
    if (event.id !== this.activeNavigationId && !this.isWaitingForContinuedNavigation) return;

    if (
      event.code === NavigationCancellationCode.Redirect ||
      event.code === NavigationCancellationCode.SupersededByNewNavigation
    ) {
      this.activeNavigationId = null;
      this.isWaitingForContinuedNavigation = true;
      return;
    }

    this.reset();
  }

  private finishTerminalNavigation(navigationId: number): void {
    if (
      navigationId !== this.activeNavigationId &&
      !this.isWaitingForContinuedNavigation
    ) {
      return;
    }

    this.reset();
  }

  private reset(): void {
    this.clearShowTimer();
    this.activeNavigationId = null;
    this.isWaitingForContinuedNavigation = false;
    this._isLoading.set(false);
  }

  private clearShowTimer(): void {
    if (this.showTimer === null) return;

    clearTimeout(this.showTimer);
    this.showTimer = null;
  }
}
