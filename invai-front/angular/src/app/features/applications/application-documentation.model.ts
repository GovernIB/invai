import { DocumentCategoryTreeOption } from '@features/document-categories/document-categories.model';
import { PageParams } from '@models/page.model';

// Documentation of an application: the documents attached to it, served by
// application/documentation, and links to external resources, served by
// application/documentation/link.

// Entry of the app-section or document-status catalog.
export interface ApplicationDocumentationCatalogEntry {
  id: number;
  code: string;
  name: string | null;
  nameEs: string | null;
}

// Section of the application a document or a link is filed under (app-section).
export type ApplicationDocumentSection = ApplicationDocumentationCatalogEntry;

// Lifecycle of a document (document-status), unrelated to its deletion.
export type ApplicationDocumentStatus = ApplicationDocumentationCatalogEntry;

// Fixed codes of the document-status catalog.
export const APPLICATION_DOCUMENT_STATUS_CODES = {
  active: 'ACTIVE',
  inReview: 'IN_REVIEW',
  obsolete: 'OBSOLETE',
} as const;

// File format accepted for a document version (mime-type).
export interface ApplicationDocumentMimeType {
  id: number;
  // Lower-case extension without the dot, e.g. pdf.
  extension: string;
  mimeType: string;
  // Label shown as the document type, e.g. PDF or Word.
  name: string;
}

// File format of a version as a document carries it.
export interface ApplicationDocumentFormat {
  id: number;
  name: string;
}

export interface ApplicationDocumentCategory {
  id: number;
  name: string | null;
  nameEs: string | null;
  parentId: number | null;
}

// Row of the document listing: the document with the data of its current version.
export interface ApplicationAttachment {
  id: number;
  title: string;
  section: ApplicationDocumentSection;
  mimeType: ApplicationDocumentFormat | null;
  category: ApplicationDocumentCategory;
  subcategory: ApplicationDocumentCategory | null;
  versionLabel: string | null;
  currentAttachmentId: number | null;
  status: ApplicationDocumentStatus;
  deletedAt: string | null;
}

export interface ApplicationDocumentMetadata {
  key: string;
  value: string;
}

// Version of a document, without its file.
export interface ApplicationDocumentVersion {
  id: number;
  versionLabel: string;
  fileName: string;
  mimeType: ApplicationDocumentFormat;
  // Size of the file in bytes.
  fileSize: number;
  uploadedAt: string;
  uploadedBy: string;
  current: boolean;
  changeNote: string | null;
  metadata: ApplicationDocumentMetadata[];
}

// Document with every version, the newest first.
export interface ApplicationDocument {
  id: number;
  applicationId: number;
  title: string;
  section: ApplicationDocumentSection;
  category: ApplicationDocumentCategory;
  subcategory: ApplicationDocumentCategory | null;
  status: ApplicationDocumentStatus;
  deletedAt: string | null;
  attachments: ApplicationDocumentVersion[];
}

// `categoryId` is the most specific category: the subcategory when there is one.
export interface ApplicationDocumentInput {
  applicationId: number;
  title: string;
  sectionId: number;
  categoryId: number;
  statusId: number;
  versionLabel: string;
  changeNote?: string | null;
  metadata?: ApplicationDocumentMetadata[];
}

export interface ApplicationDocumentUpdateInput {
  title: string;
  sectionId: number;
  categoryId: number;
  statusId: number;
}

export interface ApplicationDocumentVersionInput {
  versionLabel: string;
  changeNote?: string | null;
  metadata?: ApplicationDocumentMetadata[];
}

// File of a version as the back-end sends it.
export interface ApplicationDocumentFile {
  blob: Blob;
  fileName: string;
}

// `search` matches the title of a document; `deleted` false keeps the documents not deleted.
export interface ApplicationAttachmentPageParams extends PageParams {
  search?: string;
  sectionId?: number;
  statusId?: number;
  categoryId?: number;
  mimeTypeId?: number;
  deleted?: boolean;
}

export interface ApplicationLink {
  id: number;
  title: string;
  description: string | null;
  section: ApplicationDocumentSection;
  url: string;
  deletedAt: string | null;
}

// Body of PUT application/documentation/link/{id}; POST adds the `applicationId` of the link.
export interface ApplicationLinkInput {
  title: string;
  description: string | null;
  sectionId: number;
  url: string;
}

// `quickSearch` matches the title, the description, the URL or the section of a link;
// `deleted` false keeps the links not deleted.
export interface ApplicationLinkPageParams extends PageParams {
  quickSearch?: string;
  sectionId?: number;
  deleted?: boolean;
}

// Category a document can be classified in, with its active subcategories.
export type ApplicationDocumentCategoryOption = DocumentCategoryTreeOption;

export interface ApplicationDocumentDetailData {
  application: { id: number; code: string; name: string; canManage: boolean };
  document: ApplicationDocument;
  categories: ApplicationDocumentCategoryOption[];
  statuses: ApplicationDocumentStatus[];
}
