import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ApplicationLinksService } from './application-links.service';

const LINKS_URL = '/invaiback/application/documentation/link';

describe('ApplicationLinksService', () => {
  let service: ApplicationLinksService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApplicationLinksService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('lists the links of an application with its filters, paging and sorting', () => {
    service
      .getLinks(7, { page: 2, size: 10, sort: 'url,desc', quickSearch: '  wiki ', sectionId: 3, deleted: false })
      .subscribe();

    const request = httpTesting.expectOne((req) => req.url === LINKS_URL);
    const params = request.request.params;
    expect(request.request.method).toBe('GET');
    expect(params.keys().sort()).toEqual(
      ['applicationId', 'deleted', 'page', 'quickSearch', 'sectionId', 'size', 'sort'],
    );
    expect([params.get('applicationId'), params.get('quickSearch'), params.get('sectionId')]).toEqual(['7', 'wiki', '3']);
    expect([params.get('deleted'), params.get('page'), params.get('size'), params.get('sort')]).toEqual(
      ['false', '2', '10', 'url,desc'],
    );
    request.flush({ content: [] });
  });

  it('leaves out the filters not given and a blank search', () => {
    service.getLinks(7, { quickSearch: '   ' }).subscribe();

    const request = httpTesting.expectOne((req) => req.url === LINKS_URL);
    expect(request.request.params.keys()).toEqual(['applicationId']);
    request.flush({ content: [] });
  });

  it('adds a link to an application, and changes and deletes it by its own id', () => {
    const input = { title: 'Wiki', description: null, sectionId: 2, url: 'https://wiki.test' };

    service.createLink(7, input).subscribe();
    const create = httpTesting.expectOne(LINKS_URL);
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual({ applicationId: 7, ...input });
    create.flush({ id: 9 });

    service.updateLink(9, { ...input, title: 'Wiki interna' }).subscribe();
    const update = httpTesting.expectOne(`${LINKS_URL}/9`);
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual({ ...input, title: 'Wiki interna' });
    update.flush({ id: 9 });

    service.deleteLink(9).subscribe();
    const remove = httpTesting.expectOne(`${LINKS_URL}/9`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
