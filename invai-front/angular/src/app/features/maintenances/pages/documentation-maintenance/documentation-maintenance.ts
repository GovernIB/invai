import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentCategoriesList } from '@features/document-categories/pages/list/document-categories-list';
import {
  Accordion,
  AccordionContent,
  AccordionHeader,
  AccordionPanel,
} from 'primeng/accordion';

import { DOCUMENTATION_MAINTENANCE_PANELS } from '../../maintenances.constants';

type AccordionValue = string | number | string[] | number[] | null | undefined;

@Component({
  selector: 'app-documentation-maintenance',
  standalone: true,
  imports: [Accordion, AccordionContent, AccordionHeader, AccordionPanel, DocumentCategoriesList],
  templateUrl: './documentation-maintenance.html',
  styleUrl: '../../../../shared/styles/maintenance-accordion.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentationMaintenance {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly panelIds = new Set(DOCUMENTATION_MAINTENANCE_PANELS.map(({ id }) => id));

  protected readonly panels = DOCUMENTATION_MAINTENANCE_PANELS;
  protected readonly activePanel = signal<string | null>(
    this.validPanelId(this.route.snapshot.fragment),
  );

  constructor() {
    this.route.fragment.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe((fragment) => {
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
