import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ResponsiblePeopleService } from '@features/maintenances/responsibles/services/responsible-people.service';
import { ApplicationAuthorizedService } from './application-authorized.service';
import { ApplicationResponsiblesService } from './application-responsibles.service';
import { ApplicationDir3ValidationService } from './application-dir3-validation.service';

describe('DIR3 HTTP contracts', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('issues a fresh check every time, independently of the people cache', () => {
    const people = TestBed.inject(ResponsiblePeopleService);
    people.getPage().subscribe();
    http.expectOne((r) => r.url.endsWith('/person/database-search')).flush({ content: [] });
    for (let attempt = 0; attempt < 2; attempt++) {
      people.checkDir3('name+alias@caib.es', 'A1').subscribe();
      const request = http.expectOne((r) => r.url.endsWith('/person/dir3-check'));
      expect(request.request.params.get('emailAddress')).toBe('name+alias@caib.es');
      expect(request.request.params.get('admUnitCode')).toBe('A1');
      request.flush({ personGroup: 'group', groupDir3: 'A1', admUnitCode: 'A1', matches: true });
    }
  });

  it('uses the validation id and invalidates both assignment caches only on success', () => {
    const responsibles = vi.spyOn(TestBed.inject(ApplicationResponsiblesService), 'clearCache');
    const authorized = vi.spyOn(TestBed.inject(ApplicationAuthorizedService), 'clearCache');
    const service = TestBed.inject(ApplicationDir3ValidationService);
    service.validateManually(501, 'Motiu').subscribe({ error: () => {} });
    http
      .expectOne('/invaiback/application/dir3-validation/manual-validate/501')
      .flush(null, { status: 500, statusText: 'Error' });
    expect(responsibles).not.toHaveBeenCalled();
    expect(authorized).not.toHaveBeenCalled();
    service.validateManually(501, 'Motiu').subscribe();
    const request = http.expectOne(
      '/invaiback/application/dir3-validation/manual-validate/501',
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ reason: 'Motiu' });
    request.flush({ id: 501, dir3Status: 'MANUAL' });
    expect(responsibles).toHaveBeenCalledOnce();
    expect(authorized).toHaveBeenCalledOnce();
  });
});
