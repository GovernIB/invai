import { HttpErrorResponse } from '@angular/common/http';

export interface ApiErrorResponse {
  error: string;
  message: string;
}

export function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  if (typeof value !== 'object' || value === null) return false;

  const candidate = value as Record<string, unknown>;

  return (
    typeof candidate['error'] === 'string' &&
    candidate['error'].trim().length > 0 &&
    typeof candidate['message'] === 'string' &&
    candidate['message'].trim().length > 0
  );
}

export function isStructuredBadRequest(
  error: unknown,
): error is HttpErrorResponse & { error: ApiErrorResponse } {
  return (
    error instanceof HttpErrorResponse &&
    error.status === 400 &&
    isApiErrorResponse(error.error)
  );
}
