import { ChangeDetectionStrategy, Component, computed, effect, inject } from '@angular/core';
import { MenuService } from '@core/services/menu.service';
import { MenuItem, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { SidebarService } from '../sidebar/sidebar.service';
import { TOP_MENU_RETRY } from './top-menu.i18n';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Button, Skeleton],
  selector: 'app-top-menu',
  templateUrl: './top-menu.html',
})
export class TopMenu {
  private readonly _messageService = inject(MessageService);
  private readonly _menuService = inject(MenuService);
  private readonly _sidebarService = inject(SidebarService);

  protected readonly menuItems = this._menuService.menuItems;
  protected readonly menuOptionsResource = this._menuService.menuOptionsResource;
  protected readonly retryLabel = TOP_MENU_RETRY;

  private _selectedSidebarItems = computed(() => this._sidebarService.menuItems());

  constructor() {
    this._registerMenuErrorToast();
  }

  protected toggleSidebarMenu(menuItem: MenuItem) {
    const isSameMenu = this._selectedSidebarItems() === menuItem.items;
    if (isSameMenu) {
      this._sidebarService.setMenuItems([]);
      this._sidebarService.setVisibility(false);
    } else {
      this._sidebarService.setMenuItems(menuItem.items ?? []);
      this._sidebarService.setVisibility(true);
      this._sidebarService.setCollapsed(false);
    }
  }

  protected getSeverity(menuItem: MenuItem) {
    return this._isSelected(menuItem) ? 'primary' : 'secondary';
  }

  protected isExpanded(menuItem: MenuItem): boolean {
    return this._isSelected(menuItem);
  }

  // TODO: No se usa, consultar si quieren poner el estilo activo en el botón con [styleClass]="getActiveStyle(menuItem)"
  protected getActiveStyle(menuItem: MenuItem) {
    return this._isSelected(menuItem)
      ? {
          background: 'var(--p-button-text-primary-active-background)',
          color: 'var(--p-button-text-primary-color)',
        }
      : {};
  }

  private _isSelected(menuItem: MenuItem) {
    return this._selectedSidebarItems() === menuItem.items;
  }

  private _registerMenuErrorToast(): void {
    effect(() => {
      const error = this.menuOptionsResource.error();
      if (!error) return;

      this._messageService.add({
        severity: 'error',
        summary: $localize`Error`,
        detail: $localize`No s'ha pogut carregar el menú.`,
      });
    });
  }
}
