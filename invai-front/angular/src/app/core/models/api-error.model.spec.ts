import { HttpErrorResponse } from '@angular/common/http';

import { isApiErrorResponse, isStructuredBadRequest } from './api-error.model';

describe('API error model', () => {
  it('accepts the structured API error contract', () => {
    expect(
      isApiErrorResponse({
        error: 'Error de validació',
        message: 'El prefix ja està assignat.',
      }),
    ).toBe(true);
  });

  it.each([
    null,
    'Request failed',
    {},
    { error: 'Error de validació' },
    { message: 'Request failed' },
    { error: '', message: 'Request failed' },
    { error: 'Error de validació', message: '   ' },
  ])('rejects an invalid API error payload: %o', (payload) => {
    expect(isApiErrorResponse(payload)).toBe(false);
  });

  it('identifies only structured HTTP 400 responses', () => {
    const badRequest = new HttpErrorResponse({
      status: 400,
      error: { error: 'Error de validació', message: 'Request failed' },
    });
    const serverError = new HttpErrorResponse({
      status: 500,
      error: { error: 'Error de validació', message: 'Request failed' },
    });

    expect(isStructuredBadRequest(badRequest)).toBe(true);
    expect(isStructuredBadRequest(serverError)).toBe(false);
    expect(isStructuredBadRequest(new Error('Request failed'))).toBe(false);
  });
});
