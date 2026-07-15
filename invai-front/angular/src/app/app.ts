import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { Breadcrumbs } from '@core/components/breadcrumbs';
import { FULL_ROUTES } from '@core/components/main-layout/routes/full.routes';
import HelpModalComponent from '@core/components/user-menu/components/help-modal/help-modal';
import { UserMenuComponent } from '@core/components/user-menu/user-menu.component';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { ToastModule } from 'primeng/toast';
import { APP_HOME_ARIA_LABEL, APP_NOTIFICATIONS_ARIA_LABEL } from './app.i18n';
import { MainLayout } from './core/components/main-layout/main-layout';

@Component({
  selector: 'app-root',
  imports: [
    ButtonModule,
    DividerModule,
    HelpModalComponent,
    MainLayout,
    RouterLink,
    RouterOutlet,
    ToastModule,
    UserMenuComponent,
    Breadcrumbs,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = 'INVAI';
  protected readonly homeRoute = FULL_ROUTES.HOME;
  protected readonly homeAriaLabel = APP_HOME_ARIA_LABEL;
  protected readonly notificationsAriaLabel = APP_NOTIFICATIONS_ARIA_LABEL;

  filteredMenuItems = signal<MenuItem[]>([]);
  isBreadcrumbsHidden = signal<boolean>(false);

  protected get isLoggedIn(): boolean {
    /* return this._oAuthService.hasValidAccessToken(); */
    return true;
  }
}
