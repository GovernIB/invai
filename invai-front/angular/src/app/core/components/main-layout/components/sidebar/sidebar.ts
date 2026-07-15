import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import {
  IsActiveMatchOptions,
  Router,
  RouterLink,
  RouterLinkActive,
  UrlTree,
} from '@angular/router';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { TieredMenuModule } from 'primeng/tieredmenu';
import { MENU_ITEMS } from '../../menu-items';
import { SvgMenuCollapsedComponent } from './assets/svg-menu-collapsed.component';
import {
  SIDEBAR_COLLAPSE_CLOSE,
  SIDEBAR_COLLAPSE_OPEN,
  SIDEBAR_NAVIGATION_ARIA_LABEL,
} from './sidebar.i18n';
import { SidebarService } from './sidebar.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ButtonModule,
    RippleModule,
    RouterLink,
    RouterLinkActive,
    SvgMenuCollapsedComponent,
    TieredMenuModule,
  ],
  selector: 'app-sidebar',
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  private readonly _router = inject(Router);
  private readonly _sidebarService = inject(SidebarService);

  protected readonly menuItems = computed<MenuItem[]>(() => MENU_ITEMS);
  protected readonly isVisible = this._sidebarService.isVisible;
  protected readonly isCollapsed = this._sidebarService.isCollapsed;

  protected readonly sidebarCollapseOpenName = SIDEBAR_COLLAPSE_OPEN;
  protected readonly sidebarCollapseCloseName = SIDEBAR_COLLAPSE_CLOSE;
  protected readonly navigationAriaLabel = SIDEBAR_NAVIGATION_ARIA_LABEL;

  protected readonly toggleCollapsed = () => this._sidebarService.toggleCollapsed();

  protected readonly asideClass = computed(() => {
    if (!this.isVisible()) return 'shrink-0 w-0 border-r-0 overflow-hidden';

    const width = this.isCollapsed() ? 'w-14' : 'w-56';
    const state = this.isCollapsed() ? 'sidebar-collapsed' : 'sidebar-expanded';
    return ['shrink-0', width, state, 'overflow-visible'].join(' ');
  });

  protected readonly tieredMenuClass = computed(() => {
    return 'border-none w-full';
  });

  protected isFragmentActive(routePath?: string): boolean {
    if (!routePath) return false;

    const urlTree: UrlTree = this._router.parseUrl(routePath);
    const matchOptions: IsActiveMatchOptions = {
      paths: 'subset',
      queryParams: 'ignored',
      fragment: 'ignored',
      matrixParams: 'ignored',
    };

    return this._router.isActive(urlTree, matchOptions);
  }
}
