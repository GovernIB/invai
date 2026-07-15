import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, ParamMap, provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { BehaviorSubject, of, throwError } from 'rxjs';
import {
  APPLICATIONS_ROUTES_LABELS,
  APPLICATIONS_ROUTES_LOC,
} from '../../applications.routes.i18n';
import { Application, ApplicationOutput } from '../../applications.model';
import { ApplicationDetailFormGroup } from '../../forms/application-form.factory';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationDetail } from './application-detail';

const APPLICATION_OUTPUT: ApplicationOutput = {
  id: 1,
  code: '0001',
  prefix: 'CVF',
  name: 'Invai',
  category: { id: 1, name: 'DRASSANA' },
  systemType: { id: 2, name: 'Instrumental' },
  field: { id: 3, name: 'Departamental' },
  admUnit: { id: 5, code: 'DGEDOT', name: 'Direcció General' },
  csCommission: { id: 4, name: 'Equip directiu' },
  description: 'Aplicació interna',
  status: { id: 1, name: 'Activa' },
  expirationDate: null,
  createdAt: '2026-01-01T10:00:00',
  createdBy: null,
  updatedAt: null,
  updatedBy: null,
  loadUser: null,
  loadDate: null,
};

const APPLICATION: Application = {
  id: '1',
  code: '0001',
  prefix: 'CVF',
  name: 'Invai',
  category: 'DRASSANA',
  informationSystem: 'Instrumental',
  scope: 'Departamental',
  commission: 'Equip directiu',
  administrativeUnit: 'Direcció General',
  status: 'Activa',
  description: 'Aplicació interna',
  creationDate: '2026-01-01T10:00:00',
  modificationDate: '',
  withdrawalDate: '',
  categoryId: 1,
  informationSystemId: 2,
  scopeId: 3,
  commissionId: 4,
  administrativeUnitId: 5,
  statusId: 1,
};

describe('ApplicationDetail', () => {
  let component: ApplicationDetail;
  let fixture: ComponentFixture<ApplicationDetail>;
  let breadcrumbService: BreadcrumbService;
  let messageService: MessageService;
  let getById: ReturnType<typeof vi.fn>;
  let update: ReturnType<typeof vi.fn>;
  let toApplication: ReturnType<typeof vi.fn>;
  let paramMap$: BehaviorSubject<ParamMap>;
  let activatedRoute: {
    paramMap: BehaviorSubject<ParamMap>;
    snapshot: { paramMap: ParamMap };
    firstChild: { snapshot: { routeConfig: { path: string } } };
  };

  beforeEach(async () => {
    getById = vi.fn((id: number) =>
      id === 1 ? of(APPLICATION_OUTPUT) : throwError(() => new Error('Not found')),
    );
    update = vi.fn(() => of(APPLICATION_OUTPUT));
    toApplication = vi.fn(() => APPLICATION);
    paramMap$ = new BehaviorSubject(convertToParamMap({ id: '1' }));
    activatedRoute = {
      paramMap: paramMap$,
      snapshot: { paramMap: paramMap$.value },
      firstChild: { snapshot: { routeConfig: { path: 'systems-databases' } } },
    };

    await TestBed.configureTestingModule({
      imports: [ApplicationDetail],
      providers: [
        BreadcrumbService,
        MessageService,
        provideRouter([]),
        { provide: ApplicationsService, useValue: { getById, update, toApplication } },
        { provide: ActivatedRoute, useValue: activatedRoute },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDetail);
    component = fixture.componentInstance;
    breadcrumbService = TestBed.inject(BreadcrumbService);
    messageService = TestBed.inject(MessageService);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show the application name in the header and code in the breadcrumb', () => {
    const detailComponent = component as unknown as { header: () => string };

    expect(getById).toHaveBeenCalledWith(1);
    expect(detailComponent.header()).toBe('Invai');
    expect(breadcrumbService.breadcrumbs()).toEqual([
      {
        label: APPLICATIONS_ROUTES_LABELS.BASE,
        routerLink: ['/', APPLICATIONS_ROUTES_LOC.BASE],
      },
      {
        label: 'Codi: 0001',
        routerLink: ['/', APPLICATIONS_ROUTES_LOC.BASE, '1', 'general'],
      },
    ]);
  });

  it('should fallback to "Aplicació {id}" when the application does not exist', async () => {
    const detailComponent = component as unknown as { header: () => string };

    activatedRoute.snapshot.paramMap = convertToParamMap({ id: '999' });
    paramMap$.next(activatedRoute.snapshot.paramMap);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(detailComponent.header()).toBe('Aplicació 999');
    expect(breadcrumbService.breadcrumbs()).toEqual([
      {
        label: APPLICATIONS_ROUTES_LABELS.BASE,
        routerLink: ['/', APPLICATIONS_ROUTES_LOC.BASE],
      },
      {
        label: 'Aplicació 999',
        routerLink: ['/', APPLICATIONS_ROUTES_LOC.BASE, '999', 'general'],
      },
    ]);
  });

  it('should save general changes through the applications service', () => {
    const addSpy = vi.spyOn(messageService, 'add');
    const detailComponent = component as unknown as {
      detailState: {
        form: ApplicationDetailFormGroup;
        startEditing: () => void;
      };
      save: () => void;
    };

    detailComponent.detailState.startEditing();
    detailComponent.detailState.form.patchValue({
      application: 'Invai actualitzada',
      prefix: 'INV',
      category: 1,
      informationSystem: 2,
      scope: 3,
      commission: 9,
      administrativeUnit: 5,
      description: 'Nova descripció',
    });
    detailComponent.save();

    expect(update).toHaveBeenCalledWith(1, {
      name: 'Invai actualitzada',
      prefix: 'INV',
      code: '0001',
      categoryId: 1,
      systemTypeId: 2,
      fieldId: 3,
      admUnitId: 5,
      commissionId: 9,
      description: 'Nova descripció',
      statusId: 1,
    });
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'success',
      summary: 'Aplicació actualitzada',
      detail: "Els canvis s'han desat correctament.",
    });
  });

  it('should show an error when saving general changes fails', () => {
    const addSpy = vi.spyOn(messageService, 'add');
    update.mockReturnValueOnce(throwError(() => new Error('Request failed')));
    const detailComponent = component as unknown as {
      detailState: {
        form: ApplicationDetailFormGroup;
        startEditing: () => void;
      };
      save: () => void;
    };

    detailComponent.detailState.startEditing();
    detailComponent.detailState.form.patchValue({
      application: 'Invai actualitzada',
      prefix: 'INV',
      category: 1,
      informationSystem: 2,
      scope: 3,
      commission: 4,
      administrativeUnit: 5,
    });
    detailComponent.save();

    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'han pogut desar els canvis.",
    });
  });

  it('should clear custom breadcrumbs on destroy', () => {
    fixture.destroy();

    expect(breadcrumbService.breadcrumbs()).toEqual([]);
  });

  it('should expose the systems and databases detail tab', () => {
    const detailComponent = component as unknown as {
      tabs: { label: string; route: string }[];
    };

    expect(detailComponent.tabs).toContainEqual({
      label: 'Sistemes i BBDD',
      route: 'systems-databases',
    });
  });

  it('should show cancel and save actions in the systems and databases section', () => {
    const buttons = fixture.debugElement.queryAll(By.directive(Button));

    expect(buttons.map((button) => button.componentInstance.label)).toEqual([
      'Cancel·lar',
      'Desar',
    ]);
  });

  it('should show pending messages for systems and databases actions', () => {
    const addSpy = vi.spyOn(messageService, 'add');
    const detailComponent = component as unknown as {
      cancelSystemsDatabasesChanges: () => void;
      saveSystemsDatabasesChanges: () => void;
    };

    detailComponent.cancelSystemsDatabasesChanges();
    detailComponent.saveSystemsDatabasesChanges();

    expect(addSpy).toHaveBeenNthCalledWith(1, {
      severity: 'info',
      summary: 'Informació',
      detail:
        'La cancel·lació dels canvis de sistemes i bases de dades encara no està implementada.',
    });
    expect(addSpy).toHaveBeenNthCalledWith(2, {
      severity: 'info',
      summary: 'Informació',
      detail:
        'El desament dels canvis de sistemes i bases de dades encara no està implementat.',
    });
  });
});
