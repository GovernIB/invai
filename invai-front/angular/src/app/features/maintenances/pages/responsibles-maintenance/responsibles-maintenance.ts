import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PrimeIcons } from 'primeng/api';

import { RESPONSIBLES_MAINTENANCE_EMPTY_MESSAGE } from '../../maintenances.i18n';
import { MAINTENANCES_ROUTES_LABELS } from '../../maintenances.routes.i18n';

@Component({
  selector: 'app-responsibles-maintenance',
  standalone: true,
  templateUrl: './responsibles-maintenance.html',
  styleUrl: './responsibles-maintenance.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsiblesMaintenance {
  protected readonly sectionLabel = MAINTENANCES_ROUTES_LABELS.RESPONSIBLES;
  protected readonly emptyMessage = RESPONSIBLES_MAINTENANCE_EMPTY_MESSAGE;
  protected readonly emptyIcon = PrimeIcons.INFO_CIRCLE;
}
