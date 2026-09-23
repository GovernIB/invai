import { TestBed } from '@angular/core/testing';
import { FieldsService } from '@features/fields/services/fields.service';
import { WebContextsService } from '@features/maintenances/security/services/security-resource.services';
import { EMPTY, firstValueFrom, of, throwError } from 'rxjs';
import { ApplicationWebContextsService } from '../../../../services/application-security.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { resolveDevelopmentWebContexts } from './application-development-web-contexts.resolver';

describe('resolveDevelopmentWebContexts', () => {
  const empty = { content: [], number: 0, totalPages: 1, totalElements: 0 };
  let getPage: ReturnType<typeof vi.fn>;
  let getCatalog: ReturnType<typeof vi.fn>;
  let getFields: ReturnType<typeof vi.fn>;
  let refresh: ReturnType<typeof vi.fn>;
  beforeEach(() => {
    getPage = vi.fn(() => of(empty));
    getCatalog = vi.fn(() => of(empty));
    getFields = vi.fn(() => of(empty));
    refresh = vi.fn(() => of({ appSecurityId: 9 }));
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationWebContextsService, useValue: { getPage } },
        { provide: WebContextsService, useValue: { getAll: getCatalog } },
        { provide: FieldsService, useValue: { getAll: getFields } },
        { provide: ApplicationsService, useValue: { refreshById: refresh } },
      ],
    });
  });
  const resolve = (anchor: number | null) =>
    firstValueFrom(TestBed.runInInjectionContext(() => resolveDevelopmentWebContexts(7, anchor)));

  it('uses the security anchor for the first page and does not refresh known identities', async () => {
    const result = await resolve(9);
    expect(refresh).not.toHaveBeenCalled();
    expect(getPage).toHaveBeenCalledExactlyOnceWith({
      appSecurityId: 9,
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: 1,
    });
    expect(result.appSecurityId).toBe(9);
    expect(result.loadFailed).toBe(false);
  });

  it('recovers a missing security anchor when entering Development directly', async () => {
    const result = await resolve(null);
    expect(refresh).toHaveBeenCalledExactlyOnceWith(7);
    expect(result.appSecurityId).toBe(9);
    expect(getPage).toHaveBeenCalledOnce();
  });

  it('avoids requests without an anchor if the refresh fails', async () => {
    refresh.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    const result = await resolve(null);
    expect(result.appSecurityId).toBeNull();
    expect(result.page).toBeNull();
    expect(getPage).not.toHaveBeenCalled();
  });

  it('loads all catalog pages and filters inactive fields', async () => {
    getCatalog.mockImplementation(({ page }: { page: number }) =>
      of({ ...empty, totalPages: 2, content: [{ id: page + 1, name: `Web ${page}` }] }),
    );
    getFields.mockReturnValueOnce(
      of({
        ...empty,
        content: [
          { id: 1, deletedAt: null },
          { id: 2, deletedAt: '2026-01-01' },
        ],
      }),
    );
    const result = await resolve(9);
    expect(result.webContextOptions.map((item) => item.id)).toEqual([1, 2]);
    expect(result.fieldOptions.map((item) => item.id)).toEqual([1]);
    expect(getCatalog).toHaveBeenCalledTimes(2);
  });

  it('finishes with a degraded state on failed and empty responses', async () => {
    getPage.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    getCatalog.mockReturnValueOnce(EMPTY);
    const result = await resolve(9);
    expect(result.loadFailed).toBe(true);
    expect(result.page).toBeNull();
    expect(result.webContextOptions).toEqual([]);
    expect(result.appSecurityId).toBe(9);
  });
});
