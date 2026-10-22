import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { FULL_ROUTES } from '@core/components/main-layout/routes/full.routes';

import { BreadcrumbService } from './breadcrumb.service';
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

describe('Breadcrumbs landmark', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Breadcrumbs],
      providers: [
        provideRouter([]),
        {
          provide: BreadcrumbService,
          useValue: { breadcrumbs: signal([{ label: 'Aplicacions' }]) },
        },
      ],
    }).compileComponents();
  });

  it('should name the navigation landmark rendered by PrimeNG and mark the current page', () => {
    const fixture = TestBed.createComponent(Breadcrumbs);
    fixture.detectChanges();
    const nav = fixture.nativeElement.querySelector('nav') as HTMLElement;

    expect(nav.getAttribute('aria-label')).toBe('Ruta de navegació');
    expect(nav.querySelector('[aria-current="page"]')?.textContent?.trim()).toBe('Aplicacions');
  });
});
