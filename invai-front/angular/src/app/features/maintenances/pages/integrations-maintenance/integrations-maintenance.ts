import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { ExternalSystemsList } from '@features/external-systems/pages/list/external-systems-list';
import { ResponsibleCompanyOption } from '@features/maintenances/responsibles/responsibles.model';
import { ResponsibleCompaniesService } from '@features/maintenances/responsibles/services/responsible-companies.service';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { MessageService } from 'primeng/api';
import {
  Accordion,
  AccordionContent,
  AccordionHeader,
  AccordionPanel,
} from 'primeng/accordion';
import { EMPTY, catchError, forkJoin, switchMap } from 'rxjs';

import { INTEGRATIONS_MAINTENANCE_PANELS } from '../../maintenances.constants';
import { INTEGRATIONS_COMPANY_OPTIONS_LOAD_ERROR } from '../../maintenances.i18n';
import {
  INTEGRATIONS_MAINTENANCE_RESOLVE_KEY,
  IntegrationsMaintenanceResolvedData,
} from './integrations-maintenance.resolver';

type AccordionValue = string | number | string[] | number[] | null | undefined;

@Component({
  selector: 'app-integrations-maintenance',
  standalone: true,
  imports: [Accordion, AccordionContent, AccordionHeader, AccordionPanel, ExternalSystemsList],
  templateUrl: './integrations-maintenance.html',
  styleUrl: '../../../../shared/styles/maintenance-accordion.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IntegrationsMaintenance {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly companiesService = inject(ResponsibleCompaniesService);
  private readonly panelIds = new Set(INTEGRATIONS_MAINTENANCE_PANELS.map(({ id }) => id));

  protected readonly panels = INTEGRATIONS_MAINTENANCE_PANELS;
  protected readonly activePanel = signal<string | null>(
    this.validPanelId(this.route.snapshot.fragment),
  );
  protected readonly activeCompanyOptions = signal<ResponsibleCompanyOption[]>([]);
  protected readonly allCompanyOptions = signal<ResponsibleCompanyOption[]>([]);

  constructor() {
    const resolved = this.route.snapshot.data[
      INTEGRATIONS_MAINTENANCE_RESOLVE_KEY
    ] as IntegrationsMaintenanceResolvedData | undefined;
    this.activeCompanyOptions.set(resolved?.activeCompanyOptions ?? []);
    this.allCompanyOptions.set(resolved?.allCompanyOptions ?? []);
    if (!resolved || resolved.companyOptionsLoadFailed) this.showCompaniesError();

    this.route.fragment.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((fragment) => {
      this.activePanel.set(this.validPanelId(fragment));
    });

    // Companies are maintained under Responsables; keep both option lists in sync.
    inject(ResponsibleDataChangesService)
      .companies.pipe(
        switchMap(() =>
          forkJoin({
            active: this.companiesService.getOptions(true),
            all: this.companiesService.getOptions(false),
          }).pipe(
            catchError(() => {
              this.showCompaniesError();
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ active, all }) => {
        this.activeCompanyOptions.set(active);
        this.allCompanyOptions.set(all);
      });
  }

  protected onPanelChange(value: AccordionValue): void {
    const panelId = typeof value === 'string' ? this.validPanelId(value) : null;
    this.activePanel.set(panelId);
    void this.router.navigate([], {
      relativeTo: this.route,
      fragment: panelId ?? undefined,
      queryParamsHandling: 'preserve',
      replaceUrl: true,
    });
  }

  private validPanelId(value: string | null): string | null {
    return value && this.panelIds.has(value) ? value : null;
  }

  private showCompaniesError(): void {
    this.messageService.add({
      severity: 'error',
      summary: $localize`Error`,
      detail: INTEGRATIONS_COMPANY_OPTIONS_LOAD_ERROR,
    });
  }
}
