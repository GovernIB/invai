import { PageParams } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

// Category used to classify the documentation of the applications. Unrelated to the general
// application categories: a category has no parent, a subcategory belongs to one category, and
// there is no third level.
export interface DocumentCategory {
  id: number;
  name: string | null;
  nameEs: string | null;
  parentId: number | null;
  // Subcategories of a category, sorted by name; empty for a subcategory. A category read by its
  // ID has all of them, active and inactive; a page has only the ones its filters match.
  subcategories: DocumentCategory[] | null;
  deletedAt: string | null;
}

export interface DocumentCategoryInput {
  name: string;
  nameEs: string;
  // null registers a category; the ID of an active category registers one of its subcategories.
  parentId: number | null;
}

export interface DocumentCategoryOption {
  id: number;
  label: string;
}

// Active category with its active subcategories, to classify a document.
export interface DocumentCategoryTreeOption extends DocumentCategoryOption {
  subcategories: DocumentCategoryOption[];
}

export interface DocumentCategoryFilters {
  name: string | null;
  status: SoftDeleteStatus | null;
}

// `name` and `search` match the Catalan or Spanish name of a category or of any of its
// subcategories; `statusId` filters both the categories and their subcategories. Of the
// subcategories in that state, a category keeps all of them if its own name matches the text, and
// otherwise only those whose name does.
export interface DocumentCategoryPageParams extends PageParams {
  name?: string;
  statusId?: SoftDeleteStatus;
  search?: string;
}

// What a role can do with the documents of a category. The permissions are only set on
// categories: the documents of a subcategory follow those of its category.
export const DOCUMENT_PERMISSIONS = ['read', 'upload', 'modify', 'delete'] as const;

export type DocumentPermission = (typeof DOCUMENT_PERMISSIONS)[number];

export interface DocumentCategoryRolePermissions extends Record<DocumentPermission, boolean> {
  roleId: number;
  roleName: string;
}
