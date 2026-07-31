import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { of } from 'rxjs';

import {
  Application,
  ApplicationOutput,
  ApplicationStatus,
  ApplicationStatusCode,
} from '../../applications.model';
import { ApplicationDevelopmentService } from '../../services/application-development.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationDetail } from './application-detail';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
} from './application-detail.resolver';

const APPLICATION_OUTPUT: ApplicationOutput = {
  id: 7,
  code: 'INVAI',
  prefix: 'INV',
  name: 'Inventari',
  category: { id: 1, name: 'Categoria', deletedAt: null },
  systemType: { id: 2, name: 'Sistema', deletedAt: null },
  field: { id: 3, name: 'Àmbit', deletedAt: null },
  admUnit: { id: 4, code: 'UT', name: 'Unitat' },
  csCommission: null,
  description: '',
  status: ApplicationStatusCode.ACTIVE,
  expirationDate: null,
  createdAt: null,
  createdBy: null,
  updatedAt: null,
  updatedBy: null,
  loadUser: null,
  loadDate: null,
  appInformationSystemDbId: 70,
  appDevelopmentId: 90,
};

describe('ApplicationDetail', () => {
  let fixture: ComponentFixture<ApplicationDetail>;
  let component: ApplicationDetail;
  let breadcrumbs: { setCustomBreadcrumbs: ReturnType<typeof vi.fn>; clear: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    breadcrumbs = {
      setCustomBreadcrumbs: vi.fn(),
      clear: vi.fn(),
    };
    const resolved: ApplicationDetailResolvedData = {
      application: APPLICATION_OUTPUT,
      loadFailed: false,
    };
    const paramMap = convertToParamMap({ id: '7' });

    await TestBed.configureTestingModule({
      imports: [ApplicationDetail],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(paramMap),
            data: of({ [APPLICATION_DETAIL_RESOLVE_KEY]: resolved }),
            snapshot: {
              paramMap,
              data: { [APPLICATION_DETAIL_RESOLVE_KEY]: resolved },
            },
          },
        },
        { provide: BreadcrumbService, useValue: breadcrumbs },
        {
          provide: ApplicationsService,
          useValue: {
            toApplication,
            update: vi.fn(),
            reactivate: vi.fn(),
            delete: vi.fn(),
          },
        },
        {
          provide: ApplicationDevelopmentService,
          useValue: { create: vi.fn(), update: vi.fn() },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders the four tabs without a global edit toolbar', () => {
    const tabs = [
      ...fixture.nativeElement.querySelectorAll('.application-detail-tab'),
    ] as HTMLAnchorElement[];

    expect(tabs.map((tab) => tab.textContent?.trim())).toEqual([
      'General',
      'Responsables',
      'Sistemes i BD',
      'Desenvolupament',
    ]);
    expect(
      fixture.nativeElement.querySelector('.application-detail-toolbar'),
    ).toBeNull();
  });

  it('initializes the application and breadcrumbs', () => {
    const access = component as unknown as {
      detailState: {
        application: () => Application | null;
      };
    };

    expect(access.detailState.application()?.name).toBe('Inventari');
    expect(breadcrumbs.setCustomBreadcrumbs).toHaveBeenCalled();
  });

  it('allows leaving when there are no dirty tabs', () => {
    expect(component.canDeactivate()).toBe(true);
  });

  it('blocks leaving and lists the dirty tabs', () => {
    const access = component as unknown as {
      detailState: {
        form: {
          controls: { description: { setValue: (value: string) => void } };
          markAsDirty: () => void;
        };
        isUnsavedChangesDialogVisible: () => boolean;
      };
    };
    access.detailState.form.controls.description.setValue('Changed');
    access.detailState.form.markAsDirty();

    expect(component.canDeactivate()).toBe(false);
    expect(access.detailState.isUnsavedChangesDialogVisible()).toBe(true);

    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('General');
  });

  it('requests the native browser warning only for dirty tabs', () => {
    const event = {
      preventDefault: vi.fn(),
      returnValue: undefined,
    } as unknown as BeforeUnloadEvent;
    const access = component as unknown as {
      onBeforeUnload: (event: BeforeUnloadEvent) => void;
      detailState: {
        developmentForm: { markAsDirty: () => void };
      };
    };

    access.onBeforeUnload(event);
    expect(event.preventDefault).not.toHaveBeenCalled();

    access.detailState.developmentForm.markAsDirty();
    access.onBeforeUnload(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(event.returnValue).toBe('');
  });

  it('clears breadcrumbs on destroy', () => {
    fixture.destroy();
    expect(breadcrumbs.clear).toHaveBeenCalled();
  });
});

function toApplication(response: ApplicationOutput): Application {
  return {
    id: String(response.id),
    code: response.code ?? '',
    prefix: response.prefix ?? '',
    name: response.name ?? '',
    category: response.category?.name ?? '',
    informationSystem: response.systemType?.name ?? '',
    scope: response.field?.name ?? '',
    commission: '',
    administrativeUnit: response.admUnit?.name ?? '',
    status: ApplicationStatus.ACTIVE,
    description: response.description ?? '',
    creationDate: response.createdAt ?? '',
    modificationDate: response.updatedAt ?? '',
    withdrawalDate: response.expirationDate ?? '',
    categoryId: response.category?.id,
    informationSystemId: response.systemType?.id,
    scopeId: response.field?.id,
    administrativeUnitId: response.admUnit?.id,
    statusId: ApplicationStatus.ACTIVE,
  };
}
