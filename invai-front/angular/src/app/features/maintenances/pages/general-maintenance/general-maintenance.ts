import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoriesList } from '@features/categories/pages/list/categories-list';
import { CommissionsList } from '@features/commissions/pages/list/commissions-list';
import { FieldsList } from '@features/fields/pages/list/fields-list';
import { SystemTypesList } from '@features/system-types/pages/list/system-types-list';
import {
  Accordion,
  AccordionContent,
  AccordionHeader,
  AccordionPanel,
} from 'primeng/accordion';

import { GENERAL_MAINTENANCE_PANELS } from '../../maintenances.constants';

type AccordionValue = string | number | string[] | number[] | null | undefined;

@Component({
  selector: 'app-general-maintenance',
  standalone: true,
  imports: [
    Accordion,
    AccordionContent,
    AccordionHeader,
    AccordionPanel,
    CategoriesList,
    CommissionsList,
    FieldsList,
    SystemTypesList,
  ],
  templateUrl: './general-maintenance.html',
  styleUrl: '../../../../shared/styles/maintenance-accordion.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GeneralMaintenance {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly panelIds = new Set(GENERAL_MAINTENANCE_PANELS.map(({ id }) => id));

  protected readonly panels = GENERAL_MAINTENANCE_PANELS;
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
