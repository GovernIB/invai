import { DOCUMENT } from '@angular/common';
import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { MenuItem } from 'primeng/api';

export type SidebarViewport = 'desktop' | 'tablet' | 'mobile';

const MOBILE_MEDIA_QUERY = '(max-width: 767px)';
const TABLET_MEDIA_QUERY = '(min-width: 768px) and (max-width: 991px)';

@Injectable({
  providedIn: 'root',
})
export class SidebarService {
  private readonly _destroyRef = inject(DestroyRef);
  private readonly _window = inject(DOCUMENT).defaultView;
  private readonly _mobileMediaQuery = this._window?.matchMedia(MOBILE_MEDIA_QUERY);
  private readonly _tabletMediaQuery = this._window?.matchMedia(TABLET_MEDIA_QUERY);

  private readonly _isDesktopCollapsed = signal(false);
  private readonly _isDrawerVisible = signal(false);
  private readonly _isVisible = signal(true);
  private readonly _menuItems = signal([] as MenuItem[]);
  private readonly _viewport = signal<SidebarViewport>(this._resolveViewport());

  readonly viewport = computed(() => this._viewport());
  readonly isDesktop = computed(() => this._viewport() === 'desktop');
  readonly isTablet = computed(() => this._viewport() === 'tablet');
  readonly isMobile = computed(() => this._viewport() === 'mobile');
  readonly isCollapsed = computed(() => {
    if (this.isTablet()) return true;
    if (this.isMobile()) return false;

    return this._isDesktopCollapsed();
  });
  readonly isDrawerVisible = computed(
    () => this._isDrawerVisible() && !this.isDesktop() && this._isVisible(),
  );
  readonly isVisible = computed(() => this._isVisible());
  readonly menuItems = computed(() => this._menuItems());

  constructor() {
    const updateViewport = () => this._updateViewport();

    this._mobileMediaQuery?.addEventListener('change', updateViewport);
    this._tabletMediaQuery?.addEventListener('change', updateViewport);

    this._destroyRef.onDestroy(() => {
      this._mobileMediaQuery?.removeEventListener('change', updateViewport);
      this._tabletMediaQuery?.removeEventListener('change', updateViewport);
    });
  }

  setCollapsed(isCollapsed: boolean): void {
    this._isDesktopCollapsed.set(isCollapsed);
  }

  setDrawerVisibility(isVisible: boolean): void {
    if (isVisible) {
      this.openDrawer();
      return;
    }

    this.closeDrawer();
  }

  setVisibility(isVisible: boolean): void {
    this._isVisible.set(isVisible);
    if (!isVisible) this.closeDrawer();
  }

  setMenuItems(menuItems: MenuItem[]): void {
    this._menuItems.set(menuItems);
  }

  toggleCollapsed(): void {
    if (this.isDesktop()) {
      this._isDesktopCollapsed.update((isCollapsed) => !isCollapsed);
      return;
    }

    this.toggleDrawer();
  }

  toggleVisibility(): void {
    this._isVisible.update((isVisible) => !isVisible);
    if (!this._isVisible()) this.closeDrawer();
  }

  revealMenu(): void {
    if (this.isDesktop()) {
      this.setCollapsed(false);
    } else if (this.isMobile()) {
      this.openDrawer();
    }
  }

  openDrawer(): void {
    if (!this.isDesktop() && this.isVisible()) this._isDrawerVisible.set(true);
  }

  closeDrawer(): void {
    this._isDrawerVisible.set(false);
  }

  toggleDrawer(): void {
    if (this.isDrawerVisible()) {
      this.closeDrawer();
    } else {
      this.openDrawer();
    }
  }

  private _resolveViewport(): SidebarViewport {
    if (this._mobileMediaQuery?.matches) return 'mobile';
    if (this._tabletMediaQuery?.matches) return 'tablet';

    return 'desktop';
  }

  private _updateViewport(): void {
    const viewport = this._resolveViewport();
    if (viewport === this._viewport()) return;

    this._viewport.set(viewport);
    this.closeDrawer();
  }
}
