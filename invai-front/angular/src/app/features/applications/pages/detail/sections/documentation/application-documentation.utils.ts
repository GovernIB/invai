import { localizedName, LocalizedName } from '@shared/utils/localized-name.utils';

import {
  ApplicationDocumentFile,
  ApplicationDocumentMimeType,
} from '../../../../application-documentation.model';
import {
  applicationAttachmentFileRejection,
  fileExtensionOf,
} from '../../../../forms/application-documentation-form.factory';
import { APPLICATION_DOCUMENTATION_TEXTS } from './application-documentation.i18n';

// Option of a select built from the app-section or document-status catalog.
export interface ApplicationDocumentationOption {
  value: number;
  label: string;
}

// Name of a catalog entry or category in the language of the page, or a dash when missing.
export function documentationLabel(entry: LocalizedName | null | undefined, locale: string): string {
  return entry ? localizedName(entry, locale) : '—';
}

// Document type of a file as the back-end will name it: the label of its format when the
// extension is accepted, otherwise the extension in upper case.
export function documentTypeOf(
  fileName: string,
  mimeTypes: readonly ApplicationDocumentMimeType[],
): string {
  const extension = fileExtensionOf(fileName);
  if (!extension) return '—';
  return mimeTypes.find((item) => item.extension === extension)?.name ?? extension.toUpperCase();
}

// A file refused by a picker, with the reason to announce. `attempt` tells apart two refusals of
// the same file, so that each one is announced again.
export interface ApplicationDocumentFileRejection {
  message: string;
  attempt: number;
}

// Message that explains why a picked file is refused, or null when the back-end accepts it.
export function documentFileRejectionMessage(
  file: File,
  mimeTypes: readonly ApplicationDocumentMimeType[],
): string | null {
  const rejection = applicationAttachmentFileRejection(file, mimeTypes.map(({ extension }) => extension));
  if (rejection === 'fileTooLarge') return APPLICATION_DOCUMENTATION_TEXTS.fileTooLargeRejected(file.name);
  if (rejection === 'fileType') return APPLICATION_DOCUMENTATION_TEXTS.fileTypeRejected(file.name);
  return null;
}

// Hands a version file to the browser to be saved with its own name.
export function saveDocumentFile({ blob, fileName }: ApplicationDocumentFile): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.append(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url));
}
