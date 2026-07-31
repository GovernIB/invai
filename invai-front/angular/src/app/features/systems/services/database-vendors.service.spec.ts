import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';

import { DatabaseVendor, DatabaseVendorInput } from '../systems.model';
import { DatabaseVendorsService } from './database-vendors.service';

const URL = '/invaiapi/interna/database-vendor';
const VENDOR: DatabaseVendor = {
  id: 2,
  name: 'PostgreSQL',
  defaultPort: 5432,
  deletedAt: null,
};
const INPUT: DatabaseVendorInput = { name: 'PostgreSQL', defaultPort: 5432 };

describe('DatabaseVendorsService', () => {
  let service: DatabaseVendorsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DatabaseVendorsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('serializes criteria and caches non-search pages', () => {
    const params = { page: 0, name: 'Post', defaultPort: 5432, statusId: 1 };
    service.getAll(params).subscribe();
    service.getAll(params).subscribe();
    const request = http.expectOne((candidate) => candidate.url === URL);
    expect(request.request.params.get('name')).toBe('Post');
    expect(request.request.params.get('defaultPort')).toBe('5432');
    request.flush(page([VENDOR]));
    service.getAll(params).subscribe();
    http.expectNone((candidate) => candidate.url === URL);
  });

  it('invalidates after CRUD and restore while retaining cache on failure', () => {
    service.getAll().subscribe();
    http.expectOne(URL).flush(page([VENDOR]));
    service.create(INPUT).subscribe({ error: vi.fn() });
    http.expectOne(URL).flush('fail', { status: 500, statusText: 'Error' });
    service.getAll().subscribe();
    http.expectNone(URL);

    service.create(INPUT).subscribe();
    http.expectOne(URL).flush(VENDOR);
    service.update(2, INPUT).subscribe();
    http.expectOne(`${URL}/2`).flush(VENDOR);
    service.delete(2).subscribe();
    http.expectOne(`${URL}/2`).flush(null);
    service.reactivate(2).subscribe();
    http.expectOne(`${URL}/reactivate/2`).flush(VENDOR);
  });
});

function page<T>(content: T[]): SpringPage<T> {
  return {
    content,
    empty: !content.length,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: 1,
  };
}
