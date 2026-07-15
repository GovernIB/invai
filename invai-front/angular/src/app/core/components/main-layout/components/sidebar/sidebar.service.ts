import { computed, Injectable, signal } from '@angular/core';
import { MenuItem } from 'primeng/api';

@Injectable({
  providedIn: 'root',
})
export class SidebarService {
  private readonly _isCollapsed = signal(false);
  private readonly _isVisible = signal(true);
  private readonly _menuItems = signal([] as MenuItem[]);

  readonly isCollapsed = computed(() => this._isCollapsed());
  readonly isVisible = computed(() => this._isVisible());
  readonly menuItems = computed(() => this._menuItems());

  setCollapsed(isCollapsed: boolean): void {
    this._isCollapsed.set(isCollapsed);
  }

  setVisibility(isVisible: boolean) {
    this._isVisible.set(isVisible);
  }

  setMenuItems(menuItems: MenuItem[]) {
    this._menuItems.set(menuItems);
  }

  toggleCollapsed() {
    this._isCollapsed.update((isCollapsed) => !isCollapsed);
  }

  toggleVisibility() {
    this._isVisible.update((isVisible) => !isVisible);
  }
}
