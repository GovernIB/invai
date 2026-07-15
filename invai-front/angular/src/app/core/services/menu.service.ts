import { httpResource } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { MENU_ITEMS } from '@core/components/main-layout/menu-items';
import { OAuthService } from '@core/services/auth.service';
import { BaseApiService } from '@core/services/base-api.service';
import { MenuItem } from 'primeng/api';

interface MenuOptionsResponse {
  menuOptions: { idFront: string }[];
}

@Injectable({ providedIn: 'root' })
export class MenuService extends BaseApiService {
  protected override readonly ENTITY_URI = 'users';

  private readonly _oAuthService = inject(OAuthService);

  readonly menuOptionsResource = httpResource<MenuOptionsResponse>(
    () => {
      /* if (!this._oAuthService.isAuthenticated()) */ return undefined;

      return {
        url: this.url('create-user-and-get-menu-items'),
        method: 'POST',
      };
    },
    {
      defaultValue: {
        menuOptions: [
          {
            idFront: 'applications',
          },
        ],
      },
    },
  );

  readonly menuItems = computed(() => {
    if (!this.menuOptionsResource.hasValue()) return [];

    const menuIds = this.menuOptionsResource.value().menuOptions;
    const userMenuIds = menuIds.map((menu) => menu.idFront);
    if (userMenuIds.length === 0) return [];
    return this._filterMenusByIds(MENU_ITEMS, userMenuIds);
  });

  private _filterMenusByIds(menuItems: MenuItem[], ids: string[]): MenuItem[] {
    return menuItems.reduce<MenuItem[]>((filteredItems, currentItem) => {
      let filteredItem: MenuItem | undefined;

      if (currentItem.items) {
        // Recursive filtering of sub-items
        const filteredSubItems = this._filterMenusByIds(currentItem.items, ids);
        if (filteredSubItems.length > 0) {
          // Only create a new item if it has valid sub-items after filtering
          filteredItem = { ...currentItem, items: filteredSubItems };
        }
      } else if (ids.includes(currentItem.id!)) {
        // If the item has no sub-items but its ID is in the list, include it as is
        filteredItem = { ...currentItem };
      }

      // If filteredItem was assigned, add it to the accumulated array
      if (filteredItem) {
        filteredItems.push(filteredItem);
      }

      return filteredItems;
    }, []);
  }
}
