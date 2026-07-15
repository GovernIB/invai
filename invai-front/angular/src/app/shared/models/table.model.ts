import { MenuItem } from 'primeng/api';
import { Subject } from 'rxjs/internal/Subject';

export interface KeyLabel {
  key: string;
  label: string;
  sortBy?: string;
  width?: string;
  minWidth?: string;
}

export interface PaginatedList<TItem> {
  items: TItem[];
  total: number;
}

// TODO: to be deleted and changed for KeyLabel
export interface TableColumn {
  key: string;
  label: string;
  sort?: boolean;
  sortBy?: string;
  class?: string;
  checkbox?: boolean;
  tag?: boolean;
  actions?: RowActionToBeDeleted[];
}

// TODO: to be deleted
export interface RowActionToBeDeleted {
  label: string;
  class?: string;
  tooltip: string;
  icon: string;
  command$: Subject<ActionParamsToBeDeleted>;
  method: string;
  id?: string;
  severity?: string;
}

// TODO: to be deleted
export interface ActionParamsToBeDeleted {
  id?: string | number;
  method: string;
}

export interface ActionParams<TItem> {
  action: number;
  params: TItem;
}

export interface TableAction extends MenuItem {
  actionId: number;
}

export interface PaginatedBodyInfo {
  firstResult: number;
  maxResult: number;
  orderBy?: string | string[] | null;
  sortDirection?: 'asc' | 'desc' | null;
}
