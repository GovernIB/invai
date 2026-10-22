import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, convertToParamMap, RouterStateSnapshot } from '@angular/router';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';
import { ApplicationDocumentDetailLoader } from './application-document-detail.loader';
import { APPLICATIONS_ROUTES } from '../../applications.routes';
import { applicationDocumentDetailResolver, ApplicationDocumentDetailResolvedData, DOCUMENT_DETAIL_RESOLVE_KEY } from './application-document-detail.resolver';

describe('applicationDocumentDetailResolver', () => {
  const load = vi.fn();
  beforeEach(() => {
    load.mockReset().mockReturnValue(of({ document: { id: 9 } }));
    TestBed.configureTestingModule({
      providers: [{ provide: ApplicationDocumentDetailLoader, useValue: { load } }],
    });
  });

  function resolve(id: string, documentId: string) {
    return firstValueFrom(TestBed.runInInjectionContext(() =>
      applicationDocumentDetailResolver(
        { paramMap: convertToParamMap({ id, documentId }) } as ActivatedRouteSnapshot,
        {} as RouterStateSnapshot,
      ) as Observable<ApplicationDocumentDetailResolvedData>,
    ));
  }

  it('resolves one complete snapshot for the standalone document route', async () => {
    expect(await resolve('7', '9')).toEqual({ applicationId: 7, documentId: 9, detail: { document: { id: 9 } } });
    expect(load).toHaveBeenCalledExactlyOnceWith(7, 9);
    const route = APPLICATIONS_ROUTES.find(({ path }) => path === ':id/documentation/:documentId');
    expect(route?.resolve?.[DOCUMENT_DETAIL_RESOLVE_KEY]).toBe(applicationDocumentDetailResolver);
    expect(route?.canDeactivate).toHaveLength(1);
    expect(APPLICATIONS_ROUTES.indexOf(route!)).toBeLessThan(APPLICATIONS_ROUTES.findIndex(({ path }) => path === ':id'));
  });

  it('returns a stable error state when the document is missing', async () => {
    load.mockReturnValue(throwError(() => new Error('missing')));
    expect(await resolve('7', '9999')).toEqual({ applicationId: 7, documentId: 9999, detail: null });
  });

  it('does not query data for invalid route identifiers', async () => {
    expect(await resolve('0', '9')).toEqual({ applicationId: null, documentId: null, detail: null });
    expect(await resolve('7', 'invalid')).toEqual({ applicationId: null, documentId: null, detail: null });
    expect(load).not.toHaveBeenCalled();
  });
});
