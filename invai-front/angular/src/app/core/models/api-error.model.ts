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

// Statuses whose structured body carries a localized message meant for the user.
const USER_MESSAGE_STATUSES = new Set([400, 504]);

/** Reads the localized backend message of a structured 400 or 504 (timeout) response. */
export function readApiErrorMessage(error: unknown): string | null {
  return error instanceof HttpErrorResponse &&
    USER_MESSAGE_STATUSES.has(error.status) &&
    isApiErrorResponse(error.error)
    ? error.error.message
    : null;
}
