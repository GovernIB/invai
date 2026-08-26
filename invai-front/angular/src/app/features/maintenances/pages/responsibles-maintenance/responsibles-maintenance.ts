import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { Accordion, AccordionContent, AccordionHeader, AccordionPanel } from 'primeng/accordion';

import { RESPONSIBLES_MAINTENANCE_PANELS } from '../../maintenances.constants';
import {
  ResponsiblePerson,
  RoleTransferSourceRequest,
} from '../../responsibles/responsibles.model';
import { ResponsibleAuthorizationsList } from '../../responsibles/pages/responsible-authorizations-list/responsible-authorizations-list';
import { ResponsibleCompaniesList } from '../../responsibles/pages/responsible-companies-list/responsible-companies-list';
import { ResponsiblePeopleList } from '../../responsibles/pages/responsible-people-list/responsible-people-list';
import { RoleTransfer } from '../../responsibles/pages/role-transfer/role-transfer';
import {
  RESPONSIBLES_MAINTENANCE_RESOLVE_KEY,
  ResponsiblesMaintenanceResolvedData,
} from './responsibles-maintenance.resolver';

type AccordionValue = string | number | string[] | number[] | null | undefined;

@Component({
  selector: 'app-responsibles-maintenance',
  standalone: true,
  imports: [
    Accordion,
    AccordionContent,
    AccordionHeader,
    AccordionPanel,
    ResponsibleAuthorizationsList,
    ResponsibleCompaniesList,
    ResponsiblePeopleList,
    RoleTransfer,
  ],
  templateUrl: './responsibles-maintenance.html',
  styleUrl: '../../../../shared/styles/maintenance-accordion.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsiblesMaintenance {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly panelIds = new Set(RESPONSIBLES_MAINTENANCE_PANELS.map(({ id }) => id));

  protected readonly panels = RESPONSIBLES_MAINTENANCE_PANELS;
  protected readonly resolved = (this.route.snapshot.data?.[
    RESPONSIBLES_MAINTENANCE_RESOLVE_KEY
  ] as ResponsiblesMaintenanceResolvedData | undefined) ?? {
    companiesPage: null,
    companiesLoadFailed: false,
    peoplePage: null,
    peopleLoadFailed: false,
    transferPeoplePage: null,
    transferPeopleLoadFailed: false,
    authorizationsPage: null,
    authorizationsLoadFailed: false,
    activeCompanyOptions: [],
    activeCompanyOptionsLoadFailed: false,
    allCompanyOptions: [],
    allCompanyOptionsLoadFailed: false,
  };
  protected readonly activePanel = signal<string | null>(
    this.validPanelId(this.route.snapshot.fragment),
  );
  protected readonly transferSourceRequest = signal<RoleTransferSourceRequest | null>(null);

  private transferRequestId = 0;

  constructor() {
    this.route.fragment.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((fragment) => {
      this.activePanel.set(this.validPanelId(fragment));
    });
  }

  protected onPanelChange(value: AccordionValue): void {
    const panelId = typeof value === 'string' ? this.validPanelId(value) : null;
    this.activatePanel(panelId);
  }

  protected onTransferRequested(person: ResponsiblePerson): void {
    this.transferRequestId += 1;
    this.transferSourceRequest.set({ requestId: this.transferRequestId, person });
    this.activatePanel('role-transfer');
  }

  private activatePanel(panelId: string | null): void {
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
}
