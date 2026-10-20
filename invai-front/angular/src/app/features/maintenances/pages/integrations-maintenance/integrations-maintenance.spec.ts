import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { ExternalSystemsList } from '@features/external-systems/pages/list/external-systems-list';
import { ResponsibleCompaniesService } from '@features/maintenances/responsibles/services/responsible-companies.service';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { MessageService } from 'primeng/api';
import { BehaviorSubject, firstValueFrom, Observable, of, throwError } from 'rxjs';

import { MAINTENANCES_ROUTES } from '../../maintenances.routes';
import { MAINTENANCES_ROUTES_LOC } from '../../maintenances.routes.i18n';
import { IntegrationsMaintenance } from './integrations-maintenance';
import {
  INTEGRATIONS_MAINTENANCE_RESOLVE_KEY,
  IntegrationsMaintenanceResolvedData,
  integrationsMaintenanceResolver,
} from './integrations-maintenance.resolver';

const ACTIVE = [{ id: 3, label: 'Plexus Tech' }];
const ALL = [...ACTIVE, { id: 9, label: 'Empresa antiga' }];

describe('integrations maintenance', () => {
  const getOptions = vi.fn();

  beforeEach(() => {
    getOptions.mockReset().mockImplementation((activeOnly: boolean) => of(activeOnly ? ACTIVE : ALL));
  });

  describe('resolver', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [{ provide: ResponsibleCompaniesService, useValue: { getOptions } }],
      });
    });

    it('is registered on the integrations route after security', () => {
      const children = MAINTENANCES_ROUTES[0].children!;
      const index = children.findIndex(({ path }) => path === MAINTENANCES_ROUTES_LOC.INTEGRATIONS);
      const securityIndex = children.findIndex(({ path }) => path === MAINTENANCES_ROUTES_LOC.SECURITY);

      expect(index).toBe(securityIndex + 1);
      expect(children[index].resolve?.[INTEGRATIONS_MAINTENANCE_RESOLVE_KEY]).toBe(
        integrationsMaintenanceResolver,
      );
    });

    it('loads active companies for the dialog and every company for the filters', async () => {
      await expect(resolve()).resolves.toEqual({
        activeCompanyOptions: ACTIVE,
        allCompanyOptions: ALL,
        companyOptionsLoadFailed: false,
      });
      expect(getOptions).toHaveBeenCalledWith(true);
      expect(getOptions).toHaveBeenCalledWith(false);
    });

    it('degrades a failed company catalog', async () => {
      getOptions.mockImplementation((activeOnly: boolean) =>
        activeOnly ? throwError(() => new Error('offline')) : of(ALL),
      );
      await expect(resolve()).resolves.toEqual({
        activeCompanyOptions: [],
        allCompanyOptions: ALL,
        companyOptionsLoadFailed: true,
      });
    });

    function resolve() {
      return firstValueFrom(
        TestBed.runInInjectionContext(
          () =>
            integrationsMaintenanceResolver(
              {} as ActivatedRouteSnapshot,
              {} as RouterStateSnapshot,
            ) as Observable<IntegrationsMaintenanceResolvedData>,
        ),
      );
    }
  });

  describe('page', () => {
    let fixture: ComponentFixture<IntegrationsMaintenance>;
    let messageService: MessageService;
    const companiesChanged = new BehaviorSubject<void>(undefined);
    const navigate = vi.fn();

    async function create(resolved: IntegrationsMaintenanceResolvedData, fragment: string | null) {
      await TestBed.configureTestingModule({
        imports: [IntegrationsMaintenance],
        providers: [
          MessageService,
          { provide: ResponsibleCompaniesService, useValue: { getOptions } },
          { provide: ResponsibleDataChangesService, useValue: { companies: companiesChanged.asObservable() } },
          { provide: Router, useValue: { navigate } },
          {
            provide: ActivatedRoute,
            useValue: {
              snapshot: { fragment, data: { [INTEGRATIONS_MAINTENANCE_RESOLVE_KEY]: resolved } },
              fragment: of(fragment),
            },
          },
        ],
      })
        .overrideComponent(ExternalSystemsList, { set: { template: '' } })
        .compileComponents();
      messageService = TestBed.inject(MessageService);
      vi.spyOn(messageService, 'add');
      fixture = TestBed.createComponent(IntegrationsMaintenance);
      fixture.detectChanges();
    }

    it('lists the external systems panel with its description and opens it from the fragment', async () => {
      await create(
        { activeCompanyOptions: ACTIVE, allCompanyOptions: ALL, companyOptionsLoadFailed: false },
        'external-systems',
      );
      const root = fixture.nativeElement as HTMLElement;

      expect(root.querySelector('.maintenance-panel-title')?.textContent?.trim()).toBe('Sistemes externs');
      expect(root.querySelector('.maintenance-panel-description')?.textContent).toContain('sistemes aliens');
      expect((fixture.componentInstance as unknown as { activePanel: () => string | null }).activePanel()).toBe(
        'external-systems',
      );
    });

    it('reports a failed company catalog', async () => {
      getOptions.mockImplementation(() => throwError(() => new Error('offline')));
      await create({ activeCompanyOptions: [], allCompanyOptions: [], companyOptionsLoadFailed: true }, null);

      expect(messageService.add).toHaveBeenCalledWith(
        expect.objectContaining({
          severity: 'error',
          detail: "No s'han pogut carregar les empreses disponibles per als sistemes externs.",
        }),
      );
    });
  });
});
