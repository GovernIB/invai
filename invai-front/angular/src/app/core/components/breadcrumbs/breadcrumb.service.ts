import { computed, inject, Injectable, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { filter } from 'rxjs/internal/operators/filter';
import { BreadcrumbData } from './breadcrumb.model';

@Injectable({
  providedIn: 'root',
})
export class BreadcrumbService {
  constructor() {}

  private readonly _router = inject(Router);
  private readonly _activatedRoute = inject(ActivatedRoute);

  private readonly _navigationEnd = toSignal(
    this._router.events.pipe(filter((event) => event instanceof NavigationEnd)),
  );

  private _customBreadcrumbs = signal<MenuItem[]>([]);

  breadcrumbs = computed(() => {
    if (this._customBreadcrumbs().length) {
      return this._customBreadcrumbs();
    } else if (this._navigationEnd()) {
      return this._buildBreadCrumb(this._activatedRoute.root);
    }
    return [];
  });

  // If set custom breadcrumbs, the default breadcrumbs will be ignored, remember clear() to onReset the breadcrumbs when component is destroyed
  setCustomBreadcrumbs(items: MenuItem[]) {
    this._customBreadcrumbs.set(items);
  }

  clear() {
    this._customBreadcrumbs.set([]);
  }

  // TODO: Refactor to pure function¿?
  private _buildBreadCrumb(
    route: ActivatedRoute,
    url: string = '',
    breadcrumbs: MenuItem[] = [],
  ): MenuItem[] {
    const label = route?.routeConfig?.data?.['breadcrumb'] ?? null;
    const isNavigable = route?.routeConfig?.data?.['navigable'] ?? false;
    const path = route?.routeConfig?.path ?? null;
    const nextUrl = path ? `${url}/${path}` : url;

    if (label) {
      this._addBreadcrumb({ breadcrumbs, label, url: nextUrl, isNavigable });
    }

    if (route.firstChild) {
      this._buildBreadCrumb(route.firstChild, nextUrl, breadcrumbs);
    } else {
      this._removeRouterLinkFromLastBreadcrumb(breadcrumbs);
    }

    return breadcrumbs;
  }

  private _addBreadcrumb(breadcrumbData: BreadcrumbData): void {
    const { breadcrumbs, label, url, isNavigable } = breadcrumbData;
    const breadcrumb: MenuItem = {
      label,
      ...(isNavigable && { routerLink: url }),
    };

    breadcrumbs.push(breadcrumb);
  }

  private _removeRouterLinkFromLastBreadcrumb(breadcrumbs: MenuItem[]): void {
    if (breadcrumbs.length) {
      delete breadcrumbs[breadcrumbs.length - 1].routerLink;
    }
  }
}
