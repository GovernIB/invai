import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Observable } from 'rxjs';

import { ApplicationDocumentMimeTypeCatalogService } from './application-document-mime-type-catalog.service';
import { ApplicationDocumentSectionCatalogService } from './application-document-section-catalog.service';
import { ApplicationDocumentStatusCatalogService } from './application-document-status-catalog.service';

type Catalog = { getAll(): Observable<unknown[]> };

const CATALOGS: { name: string; url: string; service: new (...args: never[]) => Catalog }[] = [
  { name: 'sections', url: '/invaiback/app-section', service: ApplicationDocumentSectionCatalogService },
  { name: 'statuses', url: '/invaiback/document-status', service: ApplicationDocumentStatusCatalogService },
  { name: 'file formats', url: '/invaiback/mime-type', service: ApplicationDocumentMimeTypeCatalogService },
];

describe('Application document catalogs', () => {
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  for (const catalog of CATALOGS) {
    it(`reads the ${catalog.name} once and shares them`, () => {
      const service = TestBed.inject(catalog.service);
      const first = vi.fn();
      const second = vi.fn();

      service.getAll().subscribe(first);
      service.getAll().subscribe(second);
      httpTesting.expectOne(catalog.url).flush([{ id: 1 }]);
      service.getAll().subscribe(second);

      httpTesting.expectNone(catalog.url);
      expect(first).toHaveBeenCalledWith([{ id: 1 }]);
      expect(second).toHaveBeenCalledTimes(2);
    });

    it(`reads the ${catalog.name} again after a failure`, () => {
      const service = TestBed.inject(catalog.service);
      const failed = vi.fn();

      service.getAll().subscribe({ error: failed });
      httpTesting.expectOne(catalog.url).flush(null, { status: 500, statusText: 'Error' });
      service.getAll().subscribe();
      httpTesting.expectOne(catalog.url).flush([]);

      expect(failed).toHaveBeenCalled();
    });
  }
});
