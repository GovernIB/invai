import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Type } from '@angular/core';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import {
  EnsRequirementsService,
  IdentityProvidersService,
  PersonalDataProcessingService,
  SecurityMeasureTypesService,
  SecurityResourceService,
  WebContextsService,
} from './security-resource.services';

describe('security resource HTTP services', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it.each([
    [EnsRequirementsService, 'ens-requirement'],
    [IdentityProvidersService, 'identity-provider'],
    [PersonalDataProcessingService, 'personal-data-processing'],
    [SecurityMeasureTypesService, 'security-measure-type'],
    [WebContextsService, 'web-context'],
  ])('uses the audited endpoint for %s', (serviceType, endpoint) => {
    const service = TestBed.inject(serviceType as Type<SecurityResourceService>);

    service.getAll({ page: 0, size: 10 }).subscribe();

    http.expectOne(`/invaiapi/interna/${endpoint}?page=0&size=10`).flush(page([]));
  });

  it('sends every bilingual criterion and reuses a cached page', () => {
    const service = TestBed.inject(EnsRequirementsService);
    const params = {
      page: 1,
      size: 25,
      sort: 'name,desc',
      name: 'Accés',
      nameEs: 'Acceso',
      statusId: SoftDeleteStatus.INACTIVE,
    };

    service.getAll(params).subscribe();
    service.getAll(params).subscribe();

    http
      .expectOne(
        '/invaiapi/interna/ens-requirement?page=1&size=25&sort=name,desc&name=Acc%C3%A9s&nameEs=Acceso&statusId=2',
      )
      .flush(page([]));
  });

  it('does not cache quick search and invalidates pages after a successful mutation', () => {
    const service = TestBed.inject(WebContextsService);
    service.getAll({ page: 0, size: 10, search: 'public' }).subscribe();
    service.getAll({ page: 0, size: 10, search: 'public' }).subscribe();
    const searches = http.match(
      (request) =>
        request.url === '/invaiapi/interna/web-context' &&
        request.params.get('search') === 'public',
    );
    expect(searches).toHaveLength(2);
    searches.forEach((request) => request.flush(page([])));

    service.getAll({ page: 0, size: 10 }).subscribe();
    http.expectOne('/invaiapi/interna/web-context?page=0&size=10').flush(page([]));
    service.create({ name: 'Públic', nameEs: 'Público' }).subscribe();
    const create = http.expectOne('/invaiapi/interna/web-context');
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual({ name: 'Públic', nameEs: 'Público' });
    create.flush({ id: 1, name: 'Públic', nameEs: 'Público', deletedAt: null });
    service.getAll({ page: 0, size: 10 }).subscribe();
    http.expectOne('/invaiapi/interna/web-context?page=0&size=10').flush(page([]));
  });

  it('uses the exact detail, update, deactivate and reactivate paths', () => {
    const service = TestBed.inject(IdentityProvidersService);
    service.getById(7).subscribe();
    http
      .expectOne('/invaiapi/interna/identity-provider/7')
      .flush({ id: 7, name: 'Cl@ve', deletedAt: null });

    service.update(7, { name: 'Cl@ve 2' }).subscribe();
    const update = http.expectOne('/invaiapi/interna/identity-provider/7');
    expect(update.request.method).toBe('PUT');
    update.flush({ id: 7, name: 'Cl@ve 2', deletedAt: null });
    service.deactivate(7).subscribe();
    const deactivate = http.expectOne('/invaiapi/interna/identity-provider/7');
    expect(deactivate.request.method).toBe('DELETE');
    deactivate.flush(null);
    service.reactivate(7).subscribe();
    const reactivate = http.expectOne('/invaiapi/interna/identity-provider/reactivate/7');
    expect(reactivate.request.method).toBe('PUT');
    expect(reactivate.request.body).toBeNull();
    reactivate.flush({ id: 7, name: 'Cl@ve 2', deletedAt: null });
  });
});

function page<T>(content: T[]) {
  return { content, totalElements: content.length };
}
