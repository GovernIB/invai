import { Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { LayersList } from '@features/layers/pages/list/layers-list';
import {
  LAYERS_LIST_RESOLVE_KEY,
  LayersListResolvedData,
} from '@features/layers/pages/list/layers-list.resolver';
import { LayersService } from '@features/layers/services/layers.service';
import { RolesList } from '@features/roles/pages/list/roles-list';
import {
  ROLES_LIST_RESOLVE_KEY,
  RolesListResolvedData,
} from '@features/roles/pages/list/roles-list.resolver';
import { RolesService } from '@features/roles/services/roles.service';
import { TechnologiesList } from '@features/technologies/pages/list/technologies-list';
import {
  TECHNOLOGIES_LIST_RESOLVE_KEY,
  TechnologiesListResolvedData,
} from '@features/technologies/pages/list/technologies-list.resolver';
import { TechnologiesService } from '@features/technologies/services/technologies.service';

interface ListHarness {
  entityForm: FormGroup;
  isDialogVisible: () => boolean;
  openCreateDialog(): void;
  submitEntity(): void;
}

const EMPTY_PAGE = {
  content: [],
  totalElements: 0,
} as never;

describe('development maintenance list owners', () => {
  let rolesService: {
    create: ReturnType<typeof vi.fn>;
    getAll: ReturnType<typeof vi.fn>;
    getById: ReturnType<typeof vi.fn>;
    reactivate: ReturnType<typeof vi.fn>;
  };
  let layersService: {
    create: ReturnType<typeof vi.fn>;
    getAll: ReturnType<typeof vi.fn>;
    getById: ReturnType<typeof vi.fn>;
    reactivate: ReturnType<typeof vi.fn>;
  };
  let technologiesService: {
    create: ReturnType<typeof vi.fn>;
    getAll: ReturnType<typeof vi.fn>;
    getById: ReturnType<typeof vi.fn>;
    reactivate: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    rolesService = {
      create: vi.fn((value) => of({ id: 1, ...value, deletedAt: null })),
      getAll: vi.fn(() => of(EMPTY_PAGE)),
      getById: vi.fn(),
      reactivate: vi.fn(),
    };
    layersService = {
      create: vi.fn((value) => of({ id: 1, ...value, deletedAt: null })),
      getAll: vi.fn(() => of(EMPTY_PAGE)),
      getById: vi.fn(),
      reactivate: vi.fn(),
    };
    technologiesService = {
      create: vi.fn((value) =>
        of({
          id: 1,
          name: value.name,
          layer: { id: value.layerId, name: 'Frontend', deletedAt: null },
          deletedAt: null,
        }),
      ),
      getAll: vi.fn(() => of(EMPTY_PAGE)),
      getById: vi.fn(),
      reactivate: vi.fn(),
    };
    const rolesData: RolesListResolvedData = {
      page: EMPTY_PAGE,
      pageLoadFailed: false,
    };
    const layersData: LayersListResolvedData = {
      page: EMPTY_PAGE,
      pageLoadFailed: false,
    };
    const technologiesData: TechnologiesListResolvedData = {
      page: EMPTY_PAGE,
      pageLoadFailed: false,
    };

    await TestBed.configureTestingModule({
      imports: [LayersList, RolesList, TechnologiesList],
      providers: [
        MessageService,
        { provide: RolesService, useValue: rolesService },
        { provide: LayersService, useValue: layersService },
        { provide: TechnologiesService, useValue: technologiesService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                [ROLES_LIST_RESOLVE_KEY]: rolesData,
                [LAYERS_LIST_RESOLVE_KEY]: layersData,
                [TECHNOLOGIES_LIST_RESOLVE_KEY]: technologiesData,
              },
            },
          },
        },
      ],
    })
      .overrideComponent(RolesList, { set: { template: '' } })
      .overrideComponent(LayersList, { set: { template: '' } })
      .overrideComponent(TechnologiesList, { set: { template: '' } })
      .compileComponents();
  });

  it('marks invalid role fields and maps an empty Spanish name to null', () => {
    const fixture = createFixture(RolesList);
    const list = fixture.componentInstance as unknown as ListHarness;
    list.openCreateDialog();
    list.submitEntity();
    expect(list.entityForm.controls['name'].touched).toBe(true);
    expect(rolesService.create).not.toHaveBeenCalled();

    list.entityForm.setValue({ name: '  Desenvolupament  ', nameEs: '   ' });
    list.submitEntity();

    expect(rolesService.create).toHaveBeenCalledWith({
      name: 'Desenvolupament',
      nameEs: null,
    });
    expect(list.isDialogVisible()).toBe(false);
  });

  it('notifies the coordinator after a successful layer mutation', () => {
    const fixture = createFixture(LayersList);
    const list = fixture.componentInstance as unknown as ListHarness;
    const changed = vi.fn();
    (
      fixture.componentInstance as unknown as {
        catalogChanged: { subscribe: (callback: () => void) => void };
      }
    ).catalogChanged.subscribe(changed);

    list.openCreateDialog();
    list.entityForm.setValue({ name: '  Frontend  ' });
    list.submitEntity();

    expect(layersService.create).toHaveBeenCalledWith({ name: 'Frontend' });
    expect(changed).toHaveBeenCalledOnce();
  });

  it('submits technology catalog identifiers instead of raw labels', () => {
    const fixture = TestBed.createComponent(TechnologiesList);
    fixture.componentRef.setInput('layerOptions', [{ id: 4, label: 'Frontend' }]);
    fixture.detectChanges();
    const list = fixture.componentInstance as unknown as ListHarness;

    list.openCreateDialog();
    list.entityForm.setValue({ name: ' Angular ', layerId: 4 });
    list.submitEntity();

    expect(technologiesService.create).toHaveBeenCalledWith({
      name: 'Angular',
      layerId: 4,
    });
    expect(list.isDialogVisible()).toBe(false);
  });

  function createFixture<T>(component: Type<T>): ComponentFixture<T> {
    const fixture = TestBed.createComponent(component);
    fixture.detectChanges();
    return fixture;
  }
});
