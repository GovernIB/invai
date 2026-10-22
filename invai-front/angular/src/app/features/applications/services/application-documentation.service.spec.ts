import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import {
  ApplicationDocumentationService,
  contentDispositionFileName,
} from './application-documentation.service';

const DOCUMENTATION_URL = '/invaiback/application/documentation';

// jsdom's Blob has no text().
function readText(blob: Blob): Promise<string> {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.readAsText(blob);
  });
}

describe('ApplicationDocumentationService', () => {
  let service: ApplicationDocumentationService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApplicationDocumentationService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('lists the documents of an application with its filters, paging and sorting', () => {
    service
      .getAttachments(7, {
        page: 2,
        size: 10,
        sort: 'versionLabel,desc',
        search: '  manual ',
        sectionId: 3,
        statusId: 1,
        deleted: false,
      })
      .subscribe();

    const request = httpTesting.expectOne((req) => req.url === DOCUMENTATION_URL);
    const params = request.request.params;
    expect(request.request.method).toBe('GET');
    expect(params.keys().sort()).toEqual(
      ['applicationId', 'deleted', 'page', 'search', 'sectionId', 'size', 'sort', 'statusId'],
    );
    expect([params.get('applicationId'), params.get('search'), params.get('deleted')]).toEqual(['7', 'manual', 'false']);
    expect([params.get('page'), params.get('size'), params.get('sort')]).toEqual(['2', '10', 'versionLabel,desc']);
    request.flush({ content: [] });
  });

  it('reads, updates and deletes a document', () => {
    service.getDocument(4).subscribe();
    httpTesting.expectOne({ method: 'GET', url: `${DOCUMENTATION_URL}/4` }).flush({ id: 4 });

    const input = { title: 'Manual', sectionId: 1, categoryId: 12, statusId: 2 };
    service.updateDocument(4, input).subscribe();
    const update = httpTesting.expectOne({ method: 'PUT', url: `${DOCUMENTATION_URL}/4` });
    expect(update.request.body).toEqual(input);
    update.flush({ id: 4 });

    service.deleteDocument(4).subscribe();
    httpTesting.expectOne({ method: 'DELETE', url: `${DOCUMENTATION_URL}/4` }).flush(null);
  });

  it('sends the data as a JSON part and the file as a second part', async () => {
    const file = new File(['%PDF'], 'manual.pdf', { type: 'application/pdf' });
    const input = {
      applicationId: 7,
      title: 'Manual',
      sectionId: 1,
      categoryId: 12,
      statusId: 2,
      versionLabel: '1.0',
    };

    service.createDocument(input, file).subscribe();
    const create = httpTesting.expectOne({ method: 'POST', url: DOCUMENTATION_URL });
    const body = create.request.body as FormData;
    const data = body.get('data') as Blob;
    expect(data.type).toBe('application/json');
    expect(JSON.parse(await readText(data))).toEqual(input);
    expect((body.get('file') as File).name).toBe('manual.pdf');
    expect(create.request.headers.has('Content-Type')).toBe(false);
    create.flush({ id: 9 });

    service.addDocumentVersion(9, { versionLabel: '2.0' }, file).subscribe();
    const version = httpTesting.expectOne({ method: 'POST', url: `${DOCUMENTATION_URL}/9` });
    expect(JSON.parse(await readText((version.request.body as FormData).get('data') as Blob))).toEqual({
      versionLabel: '2.0',
    });
    version.flush({ id: 9 });
  });

  it('downloads or shows a version with the name the back-end gives it', () => {
    const received = vi.fn();
    service.getDocumentVersionFile(9, 3, 'Manual').subscribe(received);
    const download = httpTesting.expectOne(`${DOCUMENTATION_URL}/9/attachment/3/download`);
    expect(download.request.responseType).toBe('blob');
    download.flush(new Blob(['%PDF']), {
      headers: { 'Content-Disposition': "attachment; filename*=UTF-8''guia%20d%27%C3%BAs.pdf" },
    });

    service.getDocumentVersionFile(9, 3, 'Manual', true).subscribe(received);
    httpTesting.expectOne(`${DOCUMENTATION_URL}/9/attachment/3/view`).flush(new Blob(['%PDF']));

    expect(received.mock.calls.map(([file]) => file.fileName)).toEqual(["guia d'ús.pdf", 'Manual']);
  });

  it('reads the file name of every Content-Disposition form', () => {
    expect(contentDispositionFileName('attachment; filename="manual.pdf"')).toBe('manual.pdf');
    expect(contentDispositionFileName('inline; filename=manual.pdf')).toBe('manual.pdf');
    expect(
      contentDispositionFileName(
        `attachment; filename="=?UTF-8?Q?=C3=BAs.pdf?="; filename*=UTF-8''%C3%BAs.pdf`,
      ),
    ).toBe('ús.pdf');
    expect(contentDispositionFileName('attachment; filename="=?UTF-8?Q?=C3=BAs.pdf?="')).toBeNull();
    expect(contentDispositionFileName(null)).toBeNull();
  });
});
