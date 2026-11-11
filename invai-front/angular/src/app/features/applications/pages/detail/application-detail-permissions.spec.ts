import { Component, WritableSignal, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import {
  ALL_USER_PERMISSIONS,
  NO_USER_PERMISSIONS,
  UserPermissions,
} from '@core/auth/user-permissions.model';
import { OAuthService } from '@core/services/auth.service';

import { APPLICATIONS_ROUTES } from '../../applications.routes';
import { applicationTabGuard, canOpenApplicationTab } from './application-detail-permissions';

@Component({ template: '' })
class EmptyTab {}

const TABS = [
  'general',
  'responsible',
  'systems-databases',
  'development',
  'accessibility',
  'security',
  'data',
  'integrations',
  'documentation',
];

describe('canOpenApplicationTab', () => {
  it('opens every tab to a user with every permission', () => {
    expect(TABS.every((tab) => canOpenApplicationTab(ALL_USER_PERMISSIONS, tab))).toBe(true);
  });

  it('opens only General and Documentation to a user without area permissions', () => {
    expect(TABS.filter((tab) => canOpenApplicationTab(NO_USER_PERMISSIONS, tab))).toEqual([
      'general',
      'documentation',
    ]);
  });

  it.each([
    ['responsible', 'responsibleRead'],
    ['systems-databases', 'systems'],
    ['development', 'development'],
    ['accessibility', 'accessibility'],
    ['security', 'security'],
    ['data', 'data'],
    ['integrations', 'integration'],
  ] as const)('opens %s with the %s permission', (tab, permission) => {
    expect(canOpenApplicationTab({ ...NO_USER_PERMISSIONS, [permission]: true }, tab)).toBe(true);
    expect(canOpenApplicationTab({ ...ALL_USER_PERMISSIONS, [permission]: false }, tab)).toBe(
      false,
    );
  });

  it('does not restrict a path that is not a tab', () => {
    expect(canOpenApplicationTab(NO_USER_PERMISSIONS, '')).toBe(true);
    expect(canOpenApplicationTab(NO_USER_PERMISSIONS, undefined)).toBe(true);
  });
});

describe('applicationTabGuard', () => {
  let permissions: WritableSignal<UserPermissions>;

  beforeEach(() => {
    permissions = signal<UserPermissions>(ALL_USER_PERMISSIONS);
    TestBed.configureTestingModule({
      providers: [
        { provide: OAuthService, useValue: { permissions } },
        provideRouter([
          {
            path: 'aplicacions',
            children: [
              {
                path: ':id',
                canActivateChild: [applicationTabGuard],
                children: TABS.map((path) => ({ path, component: EmptyTab })),
              },
            ],
          },
        ]),
      ],
    });
  });

  it('is registered on the detail route of an application', () => {
    const detailRoute = APPLICATIONS_ROUTES.find(({ path }) => path === ':id');

    expect(detailRoute?.canActivateChild).toEqual([applicationTabGuard]);
  });

  it('lets a user open a tab of their area', async () => {
    permissions.set({ ...NO_USER_PERMISSIONS, security: true });
    const harness = await RouterTestingHarness.create();

    await harness.navigateByUrl('/aplicacions/7/security');

    expect(TestBed.inject(Router).url).toBe('/aplicacions/7/security');
  });

  it('sends to the General tab a user who opens a tab outside their areas by its URL', async () => {
    permissions.set({ ...NO_USER_PERMISSIONS, security: true });
    const harness = await RouterTestingHarness.create();

    await harness.navigateByUrl('/aplicacions/7/systems-databases');

    expect(TestBed.inject(Router).url).toBe('/aplicacions/7/general');
  });

  it('lets every user open General and Documentation', async () => {
    permissions.set(NO_USER_PERMISSIONS);
    const harness = await RouterTestingHarness.create();

    await harness.navigateByUrl('/aplicacions/7/documentation');
    expect(TestBed.inject(Router).url).toBe('/aplicacions/7/documentation');

    await harness.navigateByUrl('/aplicacions/7/general');
    expect(TestBed.inject(Router).url).toBe('/aplicacions/7/general');
  });
});
