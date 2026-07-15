import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { httpErrorLoggingInterceptor } from './http-error-logging.interceptor';

describe('httpErrorLoggingInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    TestBed.configureTestingModule({
      providers: [
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
  });
});
