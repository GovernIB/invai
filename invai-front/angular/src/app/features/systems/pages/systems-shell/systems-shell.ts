import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { EnvironmentsList } from '@features/environments/pages/list/environments-list';
import { Accordion, AccordionContent, AccordionHeader, AccordionPanel } from 'primeng/accordion';

import { SYSTEMS_PANELS } from '../../systems.constants';
import { DatabaseVendorsList } from '../database-vendors-list/database-vendors-list';
import { DatabasesList } from '../databases-list/databases-list';
import { PhysicalServersList } from '../physical-servers-list/physical-servers-list';
import { ServersList } from '../servers-list/servers-list';

type AccordionValue = string | number | string[] | number[] | null | undefined;

@Component({
  selector: 'app-systems-shell',
  standalone: true,
  imports: [
    Accordion,
    AccordionContent,
    AccordionHeader,
    AccordionPanel,
    DatabasesList,
    DatabaseVendorsList,
    EnvironmentsList,
    PhysicalServersList,
    ServersList,
  ],
  templateUrl: './systems-shell.html',
  styleUrl: '../../../../shared/styles/maintenance-accordion.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemsShell {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly panelIds = new Set<string>(SYSTEMS_PANELS.map(({ id }) => id));

  protected readonly panels = SYSTEMS_PANELS;
  protected readonly activePanel = signal<string | null>(
    this.validPanelId(this.route.snapshot.fragment),
  );

  constructor() {
    this.route.fragment.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((fragment) => {
      this.activePanel.set(this.validPanelId(fragment));
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
}
