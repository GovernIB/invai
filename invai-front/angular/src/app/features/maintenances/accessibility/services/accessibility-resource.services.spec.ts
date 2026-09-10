import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Type } from '@angular/core';
import { Observable } from 'rxjs';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
  AccessibilityResourceService,
} from './accessibility-resource.services';

describe('accessibility resource HTTP services', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('separates active, inactive and unfiltered pages and retries failed entries', () => {
    const service = TestBed.inject(ClassificationSegmentsService);
    const base = '/invaiapi/interna/classification-segment';
    for (const statusId of [1, 2, undefined]) {
      service.getAll({ page: 0, statusId }).subscribe();
      http
        .expectOne(statusId ? `${base}?page=0&statusId=${statusId}` : `${base}?page=0`)
        .flush(page([]));
    }
    service.getAll({ page: 0, statusId: 1 }).subscribe();
    http.expectNone(`${base}?page=0&statusId=1`);
    service.getAll({ page: 1 }).subscribe({ error: () => undefined });
    http.expectOne(`${base}?page=1`).flush(null, { status: 500, statusText: 'Failed' });
    service.getAll({ page: 1 }).subscribe();
    http.expectOne(`${base}?page=1`).flush(page([]));
  });

  it.each(['create', 'update', 'deactivate', 'reactivate'] as const)(
    'invalidates cached pages only after successful %s',
    (operation) => {
      const service = TestBed.inject(ComplianceSituationsService);
      const base = '/invaiapi/interna/compliance-situation';
      const load = () => service.getAll({ page: 0 }).subscribe();
      const mutate = (): Observable<unknown> =>
        operation === 'create'
          ? service.create({ name: 'Nou', nameEs: 'Nuevo' })
          : operation === 'update'
            ? service.update(17, { name: 'Nou', nameEs: 'Nuevo' })
            : service[operation](17);
      const url =
        operation === 'create'
          ? base
          : operation === 'reactivate'
            ? `${base}/reactivate/17`
            : `${base}/17`;
      load();
      http.expectOne(`${base}?page=0`).flush(page([]));
      mutate().subscribe({ error: () => undefined });
      http.expectOne(url).flush(null, { status: 403, statusText: 'Forbidden' });
      load();
      http.expectNone(`${base}?page=0`);
      mutate().subscribe();
      http
        .expectOne(url)
        .flush(
          operation === 'deactivate'
            ? null
            : { id: 17, name: 'Nou', nameEs: 'Nuevo', deletedAt: null },
        );
      load();
      http.expectOne(`${base}?page=0`).flush(page([]));
    },
  );

  it.each([
    [ClassificationSegmentsService, 'classification-segment'],
    [ComplianceSituationsService, 'compliance-situation'],
  ])('uses the audited endpoint for %s', (serviceType, endpoint) => {
    const service = TestBed.inject(serviceType as Type<AccessibilityResourceService>);

    service.getAll({ page: 0, size: 10 }).subscribe();

    http.expectOne(`/invaiapi/interna/${endpoint}?page=0&size=10`).flush(page([]));
  });

  it('sends every bilingual criterion and reuses a cached page', () => {
    const service = TestBed.inject(ClassificationSegmentsService);
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
        '/invaiapi/interna/classification-segment?page=1&size=25&sort=name,desc&name=Acc%C3%A9s&nameEs=Acceso&statusId=2',
      )
      .flush(page([]));
  });

  it('does not cache quick search and invalidates pages after a successful mutation', () => {
    const service = TestBed.inject(ComplianceSituationsService);
    service.getAll({ page: 0, size: 10, search: 'public' }).subscribe();
    service.getAll({ page: 0, size: 10, search: 'public' }).subscribe();
    const searches = http.match(
      (request) =>
        request.url === '/invaiapi/interna/compliance-situation' &&
        request.params.get('search') === 'public',
    );
    expect(searches).toHaveLength(2);
    searches.forEach((request) => request.flush(page([])));

    service.getAll({ page: 0, size: 10 }).subscribe();
    http.expectOne('/invaiapi/interna/compliance-situation?page=0&size=10').flush(page([]));
    service.create({ name: 'Públic', nameEs: 'Público' }).subscribe();
    const create = http.expectOne('/invaiapi/interna/compliance-situation');
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual({ name: 'Públic', nameEs: 'Público' });
    create.flush({ id: 1, name: 'Públic', nameEs: 'Público', deletedAt: null });
    service.getAll({ page: 0, size: 10 }).subscribe();
    http.expectOne('/invaiapi/interna/compliance-situation?page=0&size=10').flush(page([]));
  });

  it('uses the exact detail, update, deactivate and reactivate paths', () => {
    const service = TestBed.inject(ClassificationSegmentsService);
    service.getById(7).subscribe();
    http
      .expectOne('/invaiapi/interna/classification-segment/7')
      .flush({ id: 7, name: 'Cl@ve', deletedAt: null });

    service.update(7, { name: 'Cl@ve 2', nameEs: 'Cl@ve 2' }).subscribe();
    const update = http.expectOne('/invaiapi/interna/classification-segment/7');
    expect(update.request.method).toBe('PUT');
    update.flush({ id: 7, name: 'Cl@ve 2', deletedAt: null });
    service.deactivate(7).subscribe();
    const deactivate = http.expectOne('/invaiapi/interna/classification-segment/7');
    expect(deactivate.request.method).toBe('DELETE');
    deactivate.flush(null);
    service.reactivate(7).subscribe();
    const reactivate = http.expectOne('/invaiapi/interna/classification-segment/reactivate/7');
    expect(reactivate.request.method).toBe('PUT');
    expect(reactivate.request.body).toBeNull();
    reactivate.flush({ id: 7, name: 'Cl@ve 2', deletedAt: null });
  });
});

function page<T>(content: T[]) {
  return { content, totalElements: content.length };
}
