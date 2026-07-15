import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { FULL_ROUTES } from '@core/components/main-layout/routes/full.routes';

import { Breadcrumbs } from './breadcrumbs';

describe('Breadcrumbs', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Breadcrumbs],
      providers: [provideRouter([])],
    })
      .overrideComponent(Breadcrumbs, { set: { template: '' } })
      .compileComponents();
  });

  it('uses the Angular router for the home breadcrumb', () => {
    const component = TestBed.createComponent(Breadcrumbs)
      .componentInstance as unknown as {
      HOME: { routerLink?: string; url?: string };
    };

    expect(component.HOME.routerLink).toBe(FULL_ROUTES.HOME);
    expect(component.HOME.url).toBeUndefined();
  });
});
