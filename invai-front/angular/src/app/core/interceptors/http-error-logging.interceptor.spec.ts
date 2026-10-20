import { HttpClient, HttpContext, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ServerErrorDialogService } from '@core/services/server-error-dialog.service';

import { SKIP_SERVER_ERROR_DIALOG, httpErrorLoggingInterceptor } from './http-error-logging.interceptor';

describe('httpErrorLoggingInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;
  let errorDialog: { open: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    errorDialog = { open: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: ServerErrorDialogService, useValue: errorDialog },
        provideHttpClient(withInterceptors([httpErrorLoggingInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    consoleErrorSpy.mockRestore();
  });

  it('logs HTTP errors and propagates them', () => {
    const errorHandler = vi.fn();

    http.get('/api/applications', { params: { page: 0 } }).subscribe({ error: errorHandler });

    const request = httpTesting.expectOne('/api/applications?page=0');
    request.flush(
      { message: 'Request failed' },
      { status: 500, statusText: 'Internal Server Error' },
    );

    expect(consoleErrorSpy).toHaveBeenCalledWith('HTTP request failed', {
      method: 'GET',
      url: '/api/applications?page=0',
      status: 500,
      statusText: 'Internal Server Error',
      error: { message: 'Request failed' },
    });
    expect(errorHandler).toHaveBeenCalledOnce();
    expect(errorDialog.open).not.toHaveBeenCalled();
  });

  it('opens the global dialog for a structured internal API bad request', () => {
    const errorHandler = vi.fn();
    const payload = {
      error: 'Error de validació',
      message: 'El prefix ja està assignat a una altra aplicació.',
    };

    http.post('/invaiback/application', {}).subscribe({ error: errorHandler });

    const request = httpTesting.expectOne('/invaiback/application');
    request.flush(payload, { status: 400, statusText: 'Bad Request' });

    expect(errorDialog.open).toHaveBeenCalledWith(payload);
    expect(errorHandler).toHaveBeenCalledOnce();
  });

  it('lets a request opt out of the global dialog while still logging and propagating the error', () => {
    const errorHandler = vi.fn();
    const payload = { error: 'Error de validació', message: 'Documento no disponible.' };

    http
      .get('/invaiback/application/data/5', {
        context: new HttpContext().set(SKIP_SERVER_ERROR_DIALOG, true),
      })
      .subscribe({ error: errorHandler });

    const request = httpTesting.expectOne('/invaiback/application/data/5');
    request.flush(payload, { status: 400, statusText: 'Bad Request' });

    expect(errorDialog.open).not.toHaveBeenCalled();
    expect(consoleErrorSpy).toHaveBeenCalledOnce();
    expect(errorHandler).toHaveBeenCalledOnce();
  });

  it.each([
    {
      description: 'an incomplete payload',
      url: '/invaiback/application',
      payload: { message: 'Request failed' },
    },
    {
      description: 'an external endpoint',
      url: '/external/application',
      payload: { error: 'Error de validació', message: 'Request failed' },
    },
  ])('does not open the dialog for $description', ({ url, payload }) => {
    http.post(url, {}).subscribe({ error: vi.fn() });

    const request = httpTesting.expectOne(url);
    request.flush(payload, { status: 400, statusText: 'Bad Request' });

    expect(errorDialog.open).not.toHaveBeenCalled();
  });
});
