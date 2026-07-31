import { TestBed } from '@angular/core/testing';
import {
  Event as RouterEvent,
  NavigationCancel,
  NavigationCancellationCode,
  NavigationEnd,
  Router,
} from '@angular/router';
import { Subject } from 'rxjs';

import {
  INITIAL_LOADER_STARTED_AT,
  InitialNavigationLoadingService,
} from './initial-navigation-loading.service';

describe('InitialNavigationLoadingService', () => {
  let events: Subject<RouterEvent>;
  let service: InitialNavigationLoadingService;

  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-07-17T10:00:00Z'));
    events = new Subject<RouterEvent>();

    TestBed.configureTestingModule({
      providers: [
        InitialNavigationLoadingService,
        { provide: Router, useValue: { events } },
        { provide: INITIAL_LOADER_STARTED_AT, useValue: Date.now() },
      ],
    });

    service = TestBed.inject(InitialNavigationLoadingService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should wait for both the first completed navigation and the minimum display time', () => {
    events.next(new NavigationEnd(1, '/applications', '/applications'));

    vi.advanceTimersByTime(599);
    expect(service.isLoading()).toBe(true);

    vi.advanceTimersByTime(1);
    expect(service.isLoading()).toBe(false);
  });

  it('should remain visible after the minimum time while the resolver navigation is pending', () => {
    vi.advanceTimersByTime(600);

    expect(service.isLoading()).toBe(true);

    events.next(new NavigationEnd(1, '/maintenances/categories', '/maintenances/categories'));

    expect(service.isLoading()).toBe(false);
  });

  it('should ignore redirect cancellations and wait for the redirected navigation', () => {
    vi.advanceTimersByTime(600);
    events.next(
      new NavigationCancel(
        1,
        '/maintenances',
        'Redirecting to categories',
        NavigationCancellationCode.Redirect,
      ),
    );

    expect(service.isLoading()).toBe(true);

    events.next(new NavigationEnd(2, '/maintenances/categories', '/maintenances/categories'));

    expect(service.isLoading()).toBe(false);
  });
});
