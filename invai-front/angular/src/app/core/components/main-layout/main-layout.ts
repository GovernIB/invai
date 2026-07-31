import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ResolverNavigationLoadingService } from '@core/services/resolver-navigation-loading.service';
import { ButtonModule } from 'primeng/button';
import { Drawer } from 'primeng/drawer';
import { Sidebar } from './components/sidebar/sidebar';
import {
  SIDEBAR_COLLAPSE_CLOSE,
  SIDEBAR_COLLAPSE_OPEN,
  SIDEBAR_NAVIGATION_ARIA_LABEL,
} from './components/sidebar/sidebar.i18n';
import { SidebarService } from './components/sidebar/sidebar.service';
import { TopMenu } from './components/top-menu/top-menu';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-main-layout',
  imports: [ButtonModule, Drawer, Sidebar, TopMenu],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
})
export class MainLayout {
  private readonly _sidebarService = inject(SidebarService);

  protected readonly isResolverLoading = inject(ResolverNavigationLoadingService).isLoading;
  protected readonly isDrawerVisible = this._sidebarService.isDrawerVisible;
  protected readonly isMobile = this._sidebarService.isMobile;
  protected readonly isSidebarVisible = this._sidebarService.isVisible;
  protected readonly drawerCloseAriaLabel = SIDEBAR_COLLAPSE_CLOSE;
  protected readonly drawerOpenAriaLabel = SIDEBAR_COLLAPSE_OPEN;
  protected readonly drawerNavigationLabel = SIDEBAR_NAVIGATION_ARIA_LABEL;
  protected readonly drawerBrandName = 'INVAI';
  protected readonly drawerStyle = { width: '14rem' };
  protected readonly resolverLoadingAriaLabel = $localize`Carregant la pàgina`;

  protected readonly openDrawer = () => this._sidebarService.openDrawer();
  protected readonly setDrawerVisibility = (isVisible: boolean) =>
    this._sidebarService.setDrawerVisibility(isVisible);
}
