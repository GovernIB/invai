import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { FieldsService } from '@features/fields/services/fields.service';
import {
  EnsRequirementsService,
  IdentityProvidersService,
  PersonalDataProcessingService,
  SecurityMeasureTypesService,
  WebContextsService,
} from '@features/maintenances/security/services/security-resource.services';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { EMPTY, Observable, firstValueFrom, of } from 'rxjs';

import { APPLICATIONS_ROUTES } from '../../../../applications.routes';
import {
  ApplicationEnsClassificationsService,
  ApplicationSecurityMeasuresService,
  ApplicationSecurityRisksService,
  ApplicationSecurityRolesService,
  ApplicationSecurityService,
  ApplicationWebContextsService,
  EnsSubjectsService,
  SecurityLevelsService,
} from '../../../../services/application-security.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { APPLICATION_DETAIL_RESOLVE_KEY } from '../../application-detail.resolver';
import {
  APPLICATION_SECURITY_RESOLVE_KEY,
  ApplicationSecurityResolvedData,
  applicationSecurityResolver,
} from './application-security-section.resolver';

describe('applicationSecurityResolver', () => {
  const rolesPage = vi.fn(() => of(page([])));
  const webContextsPage = vi.fn(() => of(page([])));
  const classificationsPage = vi.fn(() => of(page([])));
  const risksPage = vi.fn(() => of(page([])));
  const measuresPage = vi.fn(() => of(page([])));
  const securityById = vi.fn(() =>
    of({ id: 8, observation: null, deletedAt: null, application: { id: 7 } }),
  );
  const refreshById = vi.fn(() => of({ id: 7, appSecurityId: 8 }));
  const catalog = { getAll: vi.fn(() => of(page([]))) };
  const listCatalog = { getAll: vi.fn(() => of([])) };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationsService, useValue: { refreshById } },
        { provide: ApplicationSecurityService, useValue: { getById: securityById } },
        { provide: ApplicationSecurityRolesService, useValue: { getPage: rolesPage } },
        { provide: ApplicationWebContextsService, useValue: { getPage: webContextsPage } },
        {
          provide: ApplicationEnsClassificationsService,
          useValue: { getPage: classificationsPage },
        },
        { provide: ApplicationSecurityRisksService, useValue: { getPage: risksPage } },
        { provide: ApplicationSecurityMeasuresService, useValue: { getPage: measuresPage } },
        { provide: SecurityLevelsService, useValue: listCatalog },
        { provide: IdentityProvidersService, useValue: catalog },
        { provide: EnsSubjectsService, useValue: listCatalog },
        { provide: PersonalDataProcessingService, useValue: catalog },
        { provide: WebContextsService, useValue: catalog },
        { provide: FieldsService, useValue: catalog },
        { provide: SecurityMeasureTypesService, useValue: catalog },
        { provide: EnsRequirementsService, useValue: catalog },
      ],
    });
  });

  it('resolves the first stable render with exact active page parameters', async () => {
    const result = await resolveSecurity(8);

    expect(result.appSecurityId).toBe(8);
    expect(securityById).toHaveBeenCalledWith(8);
    expect(rolesPage).toHaveBeenCalledWith({
      appSecurityId: 8,
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
    expect(result.failures.roles).toBe(false);
  });

  it('refreshes a stale parent detail only when its security anchor is missing', async () => {
    const result = await resolveSecurity(null);

    expect(refreshById).toHaveBeenCalledWith(7);
    expect(result.appSecurityId).toBe(8);
    expect(securityById).toHaveBeenCalledWith(8);
  });

  it('degrades an empty source instead of cancelling route navigation', async () => {
    rolesPage.mockReturnValueOnce(EMPTY);

    const result = await resolveSecurity(8);

    expect(result.rolesPage).toBeNull();
    expect(result.failures.roles).toBe(true);
  });

  it('is registered on the security child route', () => {
    const detailRoute = APPLICATIONS_ROUTES.find(({ path }) => path === ':id');
    const securityRoute = detailRoute?.children?.find(({ path }) => path === 'security');

    expect(securityRoute?.resolve?.[APPLICATION_SECURITY_RESOLVE_KEY]).toBe(
      applicationSecurityResolver,
    );
  });

  function resolveSecurity(appSecurityId: number | null): Promise<ApplicationSecurityResolvedData> {
    const route = {
      parent: {
        data: {
          [APPLICATION_DETAIL_RESOLVE_KEY]: {
            application: { id: 7, appSecurityId },
            loadFailed: false,
          },
        },
      },
    } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      applicationSecurityResolver(route, {} as RouterStateSnapshot),
    );
    return firstValueFrom(result as Observable<ApplicationSecurityResolvedData>);
  }
});

function page<T>(content: T[]): SpringPage<T> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 100,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 100,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: content.length ? 1 : 0,
  };
}
