import { Component, input, output } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { LayerOption } from '@features/layers/layers.model';
import { LayersList } from '@features/layers/pages/list/layers-list';
import {
  LAYER_CATALOG_RESOLVE_KEY,
  LayerCatalogResolvedData,
} from '@features/layers/services/layer-catalog.resolver';
import { LayerCatalogService } from '@features/layers/services/layer-catalog.service';
import { RolesList } from '@features/roles/pages/list/roles-list';
import { TechnologiesList } from '@features/technologies/pages/list/technologies-list';
import { MessageService } from 'primeng/api';
import { AccordionPanel } from 'primeng/accordion';
import { BehaviorSubject, of } from 'rxjs';

import { DEVELOPMENT_MAINTENANCE_PANELS } from '../../maintenances.constants';
import { DevelopmentMaintenance } from './development-maintenance';

@Component({ selector: 'app-roles-list', standalone: true, template: '' })
class RolesListStub {}

@Component({ selector: 'app-layers-list', standalone: true, template: '' })
class LayersListStub {
  catalogChanged = output<void>();
}

@Component({ selector: 'app-technologies-list', standalone: true, template: '' })
class TechnologiesListStub {
  layerOptions = input.required<LayerOption[]>();
}

describe('DevelopmentMaintenance', () => {
  let fixture: ComponentFixture<DevelopmentMaintenance>;
  let fragment: BehaviorSubject<string | null>;
  let navigate: ReturnType<typeof vi.fn>;
  let getActiveOptions: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    fragment = new BehaviorSubject<string | null>(null);
    navigate = vi.fn(() => Promise.resolve(true));
    getActiveOptions = vi.fn(() => of([{ id: 2, label: 'Backend' }]));
    const resolved: LayerCatalogResolvedData = {
      options: [{ id: 1, label: 'Frontend' }],
      loadFailed: false,
    };

    await TestBed.configureTestingModule({
      imports: [DevelopmentMaintenance],
      providers: [
        MessageService,
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              fragment: null,
              data: { [LAYER_CATALOG_RESOLVE_KEY]: resolved },
            },
            fragment,
          },
        },
        { provide: Router, useValue: { navigate } },
        { provide: LayerCatalogService, useValue: { getActiveOptions } },
      ],
    })
      .overrideComponent(DevelopmentMaintenance, {
        remove: { imports: [LayersList, RolesList, TechnologiesList] },
        add: { imports: [LayersListStub, RolesListStub, TechnologiesListStub] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(DevelopmentMaintenance);
    fixture.detectChanges();
  });

  it('renders the three development panels with their descriptions', () => {
    const headers = fixture.nativeElement.querySelectorAll(
      '.maintenance-panel-header',
    ) as NodeListOf<HTMLElement>;

    expect(
      Array.from(headers).map((header) => ({
        title: header.querySelector('.maintenance-panel-title')?.textContent,
        description: header.querySelector('.maintenance-panel-description')?.textContent,
      })),
    ).toEqual(
      DEVELOPMENT_MAINTENANCE_PANELS.map(({ title, description }) => ({
        title,
        description,
      })),
    );
  });

  it('synchronizes the active panel with the URL fragment', () => {
    (
      fixture.componentInstance as unknown as {
        onPanelChange(value: string): void;
      }
    ).onPanelChange('layers');
    fixture.detectChanges();

    expect(activePanels()).toEqual(['layers']);
    expect(navigate).toHaveBeenCalledWith([], {
      relativeTo: TestBed.inject(ActivatedRoute),
      fragment: 'layers',
      queryParamsHandling: 'preserve',
      replaceUrl: true,
    });

    fragment.next('technologies');
    fixture.detectChanges();
    expect(activePanels()).toEqual(['technologies']);
  });

  it('refreshes technology layer options after a layer mutation', () => {
    const technology = fixture.debugElement.query(
      By.directive(TechnologiesListStub),
    ).componentInstance as TechnologiesListStub;
    expect(technology.layerOptions()).toEqual([{ id: 1, label: 'Frontend' }]);

    fixture.debugElement
      .query(By.directive(LayersListStub))
      .componentInstance.catalogChanged.emit();
    fixture.detectChanges();

    expect(getActiveOptions).toHaveBeenCalledOnce();
    expect(technology.layerOptions()).toEqual([{ id: 2, label: 'Backend' }]);
  });

  function activePanels(): string[] {
    return fixture.debugElement
      .queryAll(By.directive(AccordionPanel))
      .filter(({ componentInstance }) => componentInstance.active())
      .map(({ componentInstance }) => componentInstance.value());
  }
});
