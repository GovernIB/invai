import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Observable } from 'rxjs';

import { DevelopmentModality, DevelopmentStandardAdaption } from '../applications.model';
import { ApplicationDatabasesService } from './application-databases.service';
import { ApplicationDevelopmentService } from './application-development.service';
import {
  ApplicationEnsClassificationsService,
  ApplicationSecurityMeasuresService,
  ApplicationSecurityRisksService,
  ApplicationSecurityService,
  ApplicationWebContextsService,
} from './application-security.service';
import { ApplicationSystemDatabaseService } from './application-system-database.service';
import { ApplicationSystemsService } from './application-systems.service';
import { ApplicationsService } from './applications.service';

const mutations: { name: string; run: () => Observable<unknown> }[] = [
  { name: 'systems', run: () => TestBed.inject(ApplicationSystemsService).delete(1) },
  { name: 'databases', run: () => TestBed.inject(ApplicationDatabasesService).delete(1) },
  { name: 'web contexts', run: () => TestBed.inject(ApplicationWebContextsService).delete(1) },
  { name: 'ENS', run: () => TestBed.inject(ApplicationEnsClassificationsService).delete(1) },
  { name: 'risks', run: () => TestBed.inject(ApplicationSecurityRisksService).delete(1) },
  { name: 'measures', run: () => TestBed.inject(ApplicationSecurityMeasuresService).delete(1) },
  {
    name: 'security',
    run: () => TestBed.inject(ApplicationSecurityService).create({ applicationId: 1, observation: '' }),
  },
  {
    name: 'systems and databases aggregate',
    run: () => TestBed.inject(ApplicationSystemDatabaseService).create({
      applicationId: 1, observation: '',
    }),
  },
  {
    name: 'development',
    run: () => TestBed.inject(ApplicationDevelopmentService).update(1, {
      applicationId: 1,
      environmentId: 3,
      modalityId: DevelopmentModality.INTERNAL,
      standardAdaptionId: DevelopmentStandardAdaption.CONFORMING,
      code: 'https://git.caib.es/invai',
      revisionDate: '2026-09-07T00:00:00',
      observation: '',
    }),
  },
];

describe('application completeness page cache after child mutations', () => {
  let http: HttpTestingController;
  let applications: ApplicationsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    applications = TestBed.inject(ApplicationsService);
  });

  afterEach(() => http.verify());

  it.each(mutations)('invalidates all completeness filters after saving $name, but keeps them on failure', ({ run }) => {
    for (const incomplete of [undefined, true, false]) {
      applications.getPage({ incomplete }).subscribe();
      http.expectOne((request) => request.method === 'GET').flush({ content: [], totalElements: 0 });
    }

    run().subscribe({ error: () => {} });
    http.expectOne((request) => request.method !== 'GET')
      .flush('Failed', { status: 500, statusText: 'Server Error' });

    for (const incomplete of [undefined, true, false]) {
      applications.getPage({ incomplete }).subscribe();
    }
    http.expectNone((request) => request.method === 'GET');

    run().subscribe();
    http.expectOne((request) => request.method !== 'GET').flush({});

    for (const incomplete of [undefined, true, false]) {
      applications.getPage({ incomplete }).subscribe();
      http.expectOne((request) => request.method === 'GET').flush({ content: [], totalElements: 0 });
    }
  });
});
