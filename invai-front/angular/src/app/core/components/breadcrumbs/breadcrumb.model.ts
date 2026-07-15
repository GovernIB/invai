import { MenuItem } from 'primeng/api';

export interface BreadcrumbData {
  breadcrumbs: MenuItem[];
  label: string;
  url: string;
  isNavigable: boolean;
}
