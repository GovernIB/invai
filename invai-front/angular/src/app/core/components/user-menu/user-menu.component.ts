import { TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { OAuthService } from '@core/services/auth.service';
import { version } from '@pkg';
import { AvatarModule } from 'primeng/avatar';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { PopoverModule } from 'primeng/popover';
import { FontSizeSwitcher } from './components/font-size-switcher/font-size-switcher';
import { HelpModalService } from './components/help-modal/help-modal.service';
import { LanguageSwitcher } from './components/language-switcher/language-switcher';
import {
  USER_MENU_ARIA_LABEL,
  USER_MENU_HELP_TITLE,
  USER_MENU_LOGOUT,
  USER_MENU_OPEN_HELP,
} from './user-menu.i18n';

@Component({
  selector: 'app-user-menu',
  imports: [
    AvatarModule,
    ButtonModule,
    DividerModule,
    FontSizeSwitcher,
    LanguageSwitcher,
    PopoverModule,
    TitleCasePipe,
  ],
  templateUrl: './user-menu.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserMenuComponent {
  protected readonly appVersion = version;
  protected readonly userMenuAriaLabel = USER_MENU_ARIA_LABEL;
  protected readonly helpTitle = USER_MENU_HELP_TITLE;
  protected readonly openHelpLabel = USER_MENU_OPEN_HELP;
  protected readonly logoutLabel = USER_MENU_LOGOUT;

  private readonly _oAuthService = inject(OAuthService);
  private readonly _helpModalService = inject(HelpModalService);

  protected readonly username = computed(() => this._oAuthService.currentUser()?.username ?? '');
  protected readonly userInitial = computed(() => this.username().charAt(0).toUpperCase() || '?');

  protected logout() {
    this._oAuthService.logout();
  }

  protected openHelp() {
    this._helpModalService.open();
  }
}
