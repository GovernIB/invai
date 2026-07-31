import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { LayerOption } from '@features/layers/layers.model';
import {
  LAYER_CATALOG_RESOLVE_KEY,
  LayerCatalogResolvedData,
} from '@features/layers/services/layer-catalog.resolver';
import { LayerCatalogService } from '@features/layers/services/layer-catalog.service';
import { LayersList } from '@features/layers/pages/list/layers-list';
import { RolesList } from '@features/roles/pages/list/roles-list';
import { TechnologiesList } from '@features/technologies/pages/list/technologies-list';
import { MessageService } from 'primeng/api';
import {
  Accordion,
  AccordionContent,
  AccordionHeader,
  AccordionPanel,
} from 'primeng/accordion';
import { finalize } from 'rxjs';

import { DEVELOPMENT_MAINTENANCE_PANELS } from '../../maintenances.constants';
import { DEVELOPMENT_LAYER_OPTIONS_LOAD_ERROR } from '../../maintenances.i18n';

type AccordionValue = string | number | string[] | number[] | null | undefined;

@Component({
  selector: 'app-development-maintenance',
  standalone: true,
  imports: [
    Accordion,
    AccordionContent,
    AccordionHeader,
    AccordionPanel,
    LayersList,
    RolesList,
    TechnologiesList,
  ],
  templateUrl: './development-maintenance.html',
  styleUrl: '../../../../shared/styles/maintenance-accordion.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DevelopmentMaintenance {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly layerCatalogService = inject(LayerCatalogService);
  private readonly panelIds = new Set(DEVELOPMENT_MAINTENANCE_PANELS.map(({ id }) => id));

  protected readonly panels = DEVELOPMENT_MAINTENANCE_PANELS;
  protected readonly activePanel = signal<string | null>(
    this.validPanelId(this.route.snapshot.fragment),
  );
  protected readonly layerOptions = signal<LayerOption[]>([]);
  protected readonly isLayerCatalogLoading = signal(false);

  constructor() {
    const resolved = this.route.snapshot.data[
      LAYER_CATALOG_RESOLVE_KEY
    ] as LayerCatalogResolvedData;
    this.layerOptions.set(resolved?.options ?? []);
    if (resolved?.loadFailed) this.showLayerCatalogError();

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

  protected reloadLayerOptions(): void {
    if (this.isLayerCatalogLoading()) return;
    this.isLayerCatalogLoading.set(true);
    this.layerCatalogService
      .getActiveOptions()
      .pipe(
        finalize(() => this.isLayerCatalogLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (options) => this.layerOptions.set(options),
        error: () => this.showLayerCatalogError(),
      });
  }

  private validPanelId(value: string | null): string | null {
    return value && this.panelIds.has(value) ? value : null;
  }

  private showLayerCatalogError(): void {
    this.messageService.add({
      severity: 'error',
      summary: $localize`Error`,
      detail: DEVELOPMENT_LAYER_OPTIONS_LOAD_ERROR,
    });
  }
}
