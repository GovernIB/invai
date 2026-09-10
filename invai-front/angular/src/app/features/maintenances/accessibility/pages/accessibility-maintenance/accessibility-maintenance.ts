import { MessageService } from 'primeng/api';
import {
  ACCESSIBILITY_FORBIDDEN,
  ACCESSIBILITY_LOAD_ERROR_SUMMARY,
} from '../../accessibility.i18n';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { Accordion, AccordionContent, AccordionHeader, AccordionPanel } from 'primeng/accordion';

import { ACCESSIBILITY_MAINTENANCE_PANELS } from '../../accessibility.constants';
import { AccessibilityResourceKey } from '../../accessibility.model';
import { AccessibilityResourceList } from '../accessibility-resource-list/accessibility-resource-list';
import {
  ACCESSIBILITY_MAINTENANCE_RESOLVE_KEY,
  AccessibilityMaintenanceResolvedData,
  AccessibilityResolvedResource,
} from './accessibility-maintenance.resolver';

type AccordionValue = string | number | string[] | number[] | null | undefined;

const EMPTY_RESOURCE: AccessibilityResolvedResource = {
  page: null,
  loadFailed: false,
  forbidden: false,
};

@Component({
  selector: 'app-accessibility-maintenance',
  standalone: true,
  imports: [
    Accordion,
    AccordionContent,
    AccordionHeader,
    AccordionPanel,
    AccessibilityResourceList,
  ],
  templateUrl: './accessibility-maintenance.html',
  styleUrl: '../../../../../shared/styles/maintenance-accordion.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccessibilityMaintenance {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly panelIds = new Set(ACCESSIBILITY_MAINTENANCE_PANELS.map(({ id }) => id));

  protected readonly panels = ACCESSIBILITY_MAINTENANCE_PANELS;
  protected readonly resolved = this.route.snapshot.data?.[
    ACCESSIBILITY_MAINTENANCE_RESOLVE_KEY
  ] as AccessibilityMaintenanceResolvedData | undefined;
  protected readonly activePanel = signal<string | null>(
    this.validPanelId(this.route.snapshot.fragment),
  );

  constructor() {
    if (Object.values(this.resolved?.resources ?? {}).some((resource) => resource.forbidden)) {
      inject(MessageService).add({
        severity: 'error',
        summary: ACCESSIBILITY_LOAD_ERROR_SUMMARY,
        detail: ACCESSIBILITY_FORBIDDEN,
      });
    }
    this.route.fragment.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((fragment) => {
      this.activePanel.set(this.validPanelId(fragment));
    });
  }

  protected resourceState(resource: AccessibilityResourceKey): AccessibilityResolvedResource {
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
