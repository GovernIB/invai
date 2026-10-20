import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SoffidRolesService } from './soffid-roles.service';

const URL = '/invaiback/security-role/soffid-search';

describe('SoffidRolesService', () => {
  let service: SoffidRolesService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(SoffidRolesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('searches Soffid roles by trimmed name without caching', () => {
    service.search('  INV_ADMIN ').subscribe();
    service.search('  INV_ADMIN ').subscribe();

    const requests = http.match((req) => req.url === URL);
    expect(requests).toHaveLength(2);
    expect(requests[0].request.params.get('name')).toBe('INV_ADMIN');
    expect(requests[0].request.params.get('page')).toBe('0');
    expect(requests[0].request.params.get('size')).toBe('20');
    requests.forEach((request) => request.flush({ content: [] }));
  });

  it('omits the name for an empty query', () => {
    service.search('   ').subscribe();
    const request = http.expectOne((req) => req.url === URL);
    expect(request.request.params.has('name')).toBe(false);
    request.flush({ content: [] });
  });
});
