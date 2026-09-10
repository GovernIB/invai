import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { Accordion, AccordionContent, AccordionHeader, AccordionPanel } from 'primeng/accordion';

import { SECURITY_MAINTENANCE_PANELS } from '../../security.constants';
import { SecurityResourceKey } from '../../security.model';
import { SecurityResourceList } from '../security-resource-list/security-resource-list';
import {
  SECURITY_MAINTENANCE_RESOLVE_KEY,
  SecurityMaintenanceResolvedData,
  SecurityResolvedResource,
} from './security-maintenance.resolver';

type AccordionValue = string | number | string[] | number[] | null | undefined;

const EMPTY_RESOURCE: SecurityResolvedResource = { page: null, loadFailed: false };

@Component({
  selector: 'app-security-maintenance',
  standalone: true,
  imports: [
    Accordion,
    AccordionContent,
    AccordionHeader,
    AccordionPanel,
    SecurityResourceList,
  ],
  templateUrl: './security-maintenance.html',
  styleUrl: '../../../../../shared/styles/maintenance-accordion.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecurityMaintenance {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly panelIds = new Set(SECURITY_MAINTENANCE_PANELS.map(({ id }) => id));

  protected readonly panels = SECURITY_MAINTENANCE_PANELS;
  protected readonly resolved = this.route.snapshot.data?.[
    SECURITY_MAINTENANCE_RESOLVE_KEY
  ] as SecurityMaintenanceResolvedData | undefined;
  protected readonly activePanel = signal<string | null>(
    this.validPanelId(this.route.snapshot.fragment),
  );

  constructor() {
    this.route.fragment.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((fragment) => {
      this.activePanel.set(this.validPanelId(fragment));
    });
  }

  protected resourceState(resource: SecurityResourceKey): SecurityResolvedResource {
    return this.resolved?.resources[resource] ?? EMPTY_RESOURCE;
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
}
