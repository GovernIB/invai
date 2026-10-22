import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { normalizeQuillHtml } from './rich-text.utils';

export const MAX_BYTES_ERROR = 'maxBytes';

export interface MaxBytesError {
  max: number;
  actual: number;
}

const encoder = new TextEncoder();

export function utf8ByteLength(value: string | null | undefined): number {
  return value ? encoder.encode(value).length : 0;
}

/**
 * Rejects values whose serialized form exceeds `max` UTF-8 bytes. `serialize` must return what is
 * actually sent to the back end, so the limit matches the stored value.
 */
export function maxUtf8BytesValidator(
  max: number,
  serialize: (value: string | null | undefined) => string = (value) => value?.trim() ?? '',
): ValidatorFn {
  return (control: AbstractControl<string | null | undefined>): ValidationErrors | null => {
    const actual = utf8ByteLength(serialize(control.value));
    return actual > max ? { [MAX_BYTES_ERROR]: { max, actual } satisfies MaxBytesError } : null;
  };
}

/** Same limit for Quill editors, measured on the normalized HTML that is persisted. */
export function richTextMaxBytesValidator(max: number): ValidatorFn {
  return maxUtf8BytesValidator(max, normalizeQuillHtml);
}
