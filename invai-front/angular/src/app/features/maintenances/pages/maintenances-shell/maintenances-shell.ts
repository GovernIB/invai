import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SectionContainerComponent } from '@components/section-container/section-container.component';

import { MAINTENANCE_TABS } from '../../maintenances.constants';
import { MAINTENANCES_ROUTES_LABELS } from '../../maintenances.routes.i18n';

@Component({
  selector: 'app-maintenances-shell',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, SectionContainerComponent],
  templateUrl: './maintenances-shell.html',
  styleUrl: './maintenances-shell.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MaintenancesShell {
  protected readonly header = MAINTENANCES_ROUTES_LABELS.BASE;
  protected readonly sectionsAriaLabel = $localize`Seccions de manteniments`;
  protected readonly tabs = MAINTENANCE_TABS;
}
