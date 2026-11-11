// Version of a document offered in the e-mail dialog; `fileSize` in bytes, null when unknown.
export interface ApplicationDocumentationEmailVersion {
  id: number;
  label: string;
  fileSize: number | null;
}

// A document chosen to be sent and the version that goes with it, the current one by default.
// `versions` is null while they are read; if they can't be read it keeps only the current one.
export interface ApplicationDocumentationEmailSelection {
  documentId: number;
  title: string;
  versionId: number;
  versions: ApplicationDocumentationEmailVersion[] | null;
}

export interface ApplicationDocumentationEmailVersionChange {
  documentId: number;
  versionId: number;
}
