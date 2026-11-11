import { KeyLabel } from '@models/table.model';

export const APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH = 150;
export const APPLICATION_ATTACHMENT_VERSION_MAX_LENGTH = 20;
// Largest file the back-end accepts for a document version.
export const APPLICATION_ATTACHMENT_MAX_FILE_BYTES = 5 * 1024 * 1024;
// Longest values the back-end accepts for a link.
export const APPLICATION_LINK_TITLE_MAX_LENGTH = 250;
export const APPLICATION_LINK_DESCRIPTION_MAX_LENGTH = 1000;
export const APPLICATION_LINK_URL_MAX_LENGTH = 500;
// Limits the back-end sets on the sending of documents by e-mail.
export const APPLICATION_EMAIL_MAX_RECIPIENTS = 20;
export const APPLICATION_EMAIL_RECIPIENT_MAX_LENGTH = 320;
export const APPLICATION_EMAIL_SUBJECT_MAX_LENGTH = 250;
export const APPLICATION_EMAIL_MESSAGE_MAX_LENGTH = 4000;
// Largest total size of the files attached to one e-mail.
export const APPLICATION_EMAIL_MAX_ATTACHMENTS_BYTES = 15 * 1024 * 1024;

export const APPLICATION_ATTACHMENTS_COLUMNS: KeyLabel[] = [
  {
    key: 'title',
    label: $localize`:@@applicationAttachmentsColumnTitle:Títol`,
    sortBy: 'title',
    minWidth: '14rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'mimeType',
    label: $localize`:@@applicationAttachmentsColumnDocumentType:Tipus doc.`,
    sortBy: 'mimeType',
    minWidth: '7rem',
  },
  {
    key: 'category',
    label: $localize`:@@applicationAttachmentsColumnCategory:Categoria`,
    sortBy: 'category',
    minWidth: '9rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'subcategory',
    label: $localize`:@@applicationAttachmentsColumnSubcategory:Subcategoria`,
    sortBy: 'subcategory',
    minWidth: '10rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'versionLabel',
    label: $localize`:@@applicationAttachmentsColumnVersion:Versió`,
    sortBy: 'versionLabel',
    minWidth: '6rem',
  },
  {
    // Not `status`: the search base hides that key, kept for the deactivation state of entities.
    key: 'state',
    label: $localize`:@@applicationAttachmentsColumnState:Estat`,
    sortBy: 'status',
    minWidth: '8rem',
  },
];

export const APPLICATION_LINKS_COLUMNS: KeyLabel[] = [
  {
    key: 'title',
    label: $localize`:@@applicationLinksColumnTitle:Títol`,
    sortBy: 'title',
    minWidth: '12rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'description',
    label: $localize`:@@applicationLinksColumnDescription:Descripció`,
    sortBy: 'description',
    minWidth: '14rem',
    wrap: true,
    maxWidth: '32rem',
  },
  {
    key: 'category',
    label: $localize`:@@applicationLinksColumnCategory:Categoria`,
    sortBy: 'category',
    minWidth: '9rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'url',
    label: $localize`:@@applicationLinksColumnUrl:URL`,
    sortBy: 'url',
    minWidth: '14rem',
    wrap: true,
    maxWidth: '24rem',
  },
];
