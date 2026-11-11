import { DocumentCategoryTreeOption } from '@features/document-categories/document-categories.model';
import { PageParams } from '@models/page.model';

// Documentation of an application: the documents attached to it, served by
// application/documentation, and links to external resources, served by
// application/documentation/link. Both are filed under a document category, on which the
// permissions of the roles are set.

// Entry of the document-status catalog.
export interface ApplicationDocumentationCatalogEntry {
  id: number;
  code: string;
  name: string | null;
  nameEs: string | null;
}

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
  category: ApplicationDocumentCategory;
  subcategory: ApplicationDocumentCategory | null;
  status: ApplicationDocumentStatus;
  deletedAt: string | null;
  attachments: ApplicationDocumentVersion[];
}

// `categoryId` is always a category, never a subcategory; `subcategoryId` is one of its
// subcategories, or null when the document only has a category.
export interface ApplicationDocumentInput {
  applicationId: number;
  title: string;
  categoryId: number;
  subcategoryId: number | null;
  statusId: number;
  versionLabel: string;
  changeNote?: string | null;
  metadata?: ApplicationDocumentMetadata[];
}

export interface ApplicationDocumentUpdateInput {
  title: string;
  categoryId: number;
  subcategoryId: number | null;
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

// A file attached to an e-mail: one version of a document of the application.
export interface ApplicationDocumentationEmailAttachment {
  documentId: number;
  versionId: number;
}

// Body of POST application/documentation/email. The back-end sends it from the address of
// INVAI with the logged-in user as Reply-To, and only with documents the user can read.
export interface ApplicationDocumentationEmailInput {
  applicationId: number;
  recipients: string[];
  subject: string;
  message: string | null;
  attachments: ApplicationDocumentationEmailAttachment[];
}

// `search` matches the title of a document; `deleted` false keeps the documents not deleted.
// The back-end only lists the documents of the categories the user can read.
export interface ApplicationAttachmentPageParams extends PageParams {
  search?: string;
  statusId?: number;
  categoryId?: number;
  subcategoryId?: number;
  mimeTypeId?: number;
  deleted?: boolean;
}

export interface ApplicationLink {
  id: number;
  title: string;
  description: string | null;
  category: ApplicationDocumentCategory;
  url: string;
  deletedAt: string | null;
}

// Body of PUT application/documentation/link/{id}; POST adds the `applicationId` of the link.
// `categoryId` is always a category: a link has no subcategory.
export interface ApplicationLinkInput {
  title: string;
  description: string | null;
  categoryId: number;
  url: string;
}

// `quickSearch` matches the title, the description, the URL or the category of a link;
// `deleted` false keeps the links not deleted.
export interface ApplicationLinkPageParams extends PageParams {
  quickSearch?: string;
  categoryId?: number;
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
