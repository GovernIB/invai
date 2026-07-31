import { TestBed } from '@angular/core/testing';
import {
  Event as RouterEvent,
  NavigationCancel,
  NavigationCancellationCode,
  NavigationEnd,
  ResolveEnd,
  ResolveStart,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { Subject } from 'rxjs';

import { ResolverNavigationLoadingService } from './resolver-navigation-loading.service';

describe('ResolverNavigationLoadingService', () => {
  let events: Subject<RouterEvent>;
  let service: ResolverNavigationLoadingService;

  beforeEach(() => {
    vi.useFakeTimers();
    events = new Subject<RouterEvent>();

    TestBed.configureTestingModule({
      providers: [
        ResolverNavigationLoadingService,
        { provide: Router, useValue: { events } },
      ],
    });

    service = TestBed.inject(ResolverNavigationLoadingService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should only show loading after the resolver has remained pending for 150 ms', () => {
    events.next(resolveStart(2));

    vi.advanceTimersByTime(149);
    expect(service.isLoading()).toBe(false);

    vi.advanceTimersByTime(1);
    expect(service.isLoading()).toBe(true);
  });

  it('should not show loading when the resolver completes before the delay', () => {
    events.next(resolveStart(2));
    vi.advanceTimersByTime(149);
    events.next(resolveEnd(2));
    vi.advanceTimersByTime(1);

    expect(service.isLoading()).toBe(false);
  });

  it('should hide loading when the active resolver completes', () => {
    events.next(resolveStart(2));
    vi.advanceTimersByTime(150);

    events.next(resolveEnd(2));

    expect(service.isLoading()).toBe(false);
  });

  it('should ignore stale completion events from a superseded navigation', () => {
    events.next(resolveStart(2));
    vi.advanceTimersByTime(150);
    events.next(
      new NavigationCancel(
        2,
        '/applications',
        'Superseded',
        NavigationCancellationCode.SupersededByNewNavigation,
      ),
    );
    events.next(resolveStart(3));

    events.next(resolveEnd(2));
    expect(service.isLoading()).toBe(true);

    events.next(resolveEnd(3));
    expect(service.isLoading()).toBe(false);
  });

  it('should keep loading continuous across resolver redirects', () => {
    events.next(resolveStart(2));
    vi.advanceTimersByTime(150);
    events.next(
      new NavigationCancel(
        2,
        '/applications/7',
        'Redirecting',
        NavigationCancellationCode.Redirect,
      ),
    );

    expect(service.isLoading()).toBe(true);

    events.next(resolveStart(3));
    events.next(resolveEnd(3));

    expect(service.isLoading()).toBe(false);
  });

  it('should clear a continued resolver load if the redirected navigation ends without resolving', () => {
    events.next(resolveStart(2));
    vi.advanceTimersByTime(150);
    events.next(
      new NavigationCancel(
        2,
        '/applications/7',
        'Redirecting',
        NavigationCancellationCode.Redirect,
      ),
    );

    events.next(new NavigationEnd(3, '/applications', '/applications'));

    expect(service.isLoading()).toBe(false);
  });

  it('should clear loading after a terminal cancellation', () => {
    events.next(resolveStart(2));
    vi.advanceTimersByTime(150);

    events.next(
      new NavigationCancel(
        2,
        '/applications',
        'Guard rejected',
        NavigationCancellationCode.GuardRejected,
      ),
    );

    expect(service.isLoading()).toBe(false);
  });
});

function resolveStart(id: number): ResolveStart {
  return new ResolveStart(id, '/applications', '/applications', {} as RouterStateSnapshot);
}

function resolveEnd(id: number): ResolveEnd {
  return new ResolveEnd(id, '/applications', '/applications', {} as RouterStateSnapshot);
}
