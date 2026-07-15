import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FULL_ROUTES } from '@core/components/main-layout/routes/full.routes';
import { PrimeIcons } from 'primeng/api';
import { Breadcrumb } from 'primeng/breadcrumb';
import { BreadcrumbService } from './breadcrumb.service';
import { BREADCRUMBS_HOME_ARIA_LABEL } from './breadcrumbs.i18n';

@Component({
  selector: 'app-breadcrumbs',
  standalone: true,
  imports: [Breadcrumb, RouterLink],
  templateUrl: './breadcrumbs.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Breadcrumbs {
  styleClass = input<string | undefined>();

  private breadcrumbService = inject(BreadcrumbService);

  protected readonly HOME = {
    icon: PrimeIcons.HOME,
    label: BREADCRUMBS_HOME_ARIA_LABEL,
    ariaLabel: BREADCRUMBS_HOME_ARIA_LABEL,
    routerLink: FULL_ROUTES.HOME,
  };

  items = this.breadcrumbService.breadcrumbs;
}
