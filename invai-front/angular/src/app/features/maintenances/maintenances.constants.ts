import { PrimeIcons } from 'primeng/api';

import {
  MAINTENANCES_ROUTES_LABELS,
  MAINTENANCES_ROUTES_LOC,
} from './maintenances.routes.i18n';
import { MAINTENANCE_PANEL_DESCRIPTIONS } from './maintenances.i18n';

export interface MaintenanceTab {
  id: string;
  icon: string;
  label: string;
  path: string;
  routerLink: string;
}

export interface MaintenancePanel {
  id: string;
  title: string;
  description: string;
}

export const MAINTENANCE_TABS: MaintenanceTab[] = [
  {
    id: 'maintenance-general',
    icon: PrimeIcons.COG,
    label: MAINTENANCES_ROUTES_LABELS.GENERAL,
    path: MAINTENANCES_ROUTES_LOC.GENERAL,
    routerLink: `/${MAINTENANCES_ROUTES_LOC.BASE}/${MAINTENANCES_ROUTES_LOC.GENERAL}`,
  },
  {
    id: 'maintenance-responsibles',
    icon: PrimeIcons.USERS,
    label: MAINTENANCES_ROUTES_LABELS.RESPONSIBLES,
    path: MAINTENANCES_ROUTES_LOC.RESPONSIBLES,
    routerLink: `/${MAINTENANCES_ROUTES_LOC.BASE}/${MAINTENANCES_ROUTES_LOC.RESPONSIBLES}`,
  },
  {
    id: 'maintenance-development',
    icon: PrimeIcons.CODE,
    label: MAINTENANCES_ROUTES_LABELS.DEVELOPMENT,
    path: MAINTENANCES_ROUTES_LOC.DEVELOPMENT,
    routerLink: `/${MAINTENANCES_ROUTES_LOC.BASE}/${MAINTENANCES_ROUTES_LOC.DEVELOPMENT}`,
  },
];

export const DEVELOPMENT_MAINTENANCE_PANELS: MaintenancePanel[] = [
  {
    id: 'provider-roles',
    title: MAINTENANCES_ROUTES_LABELS.ROLES,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.roles,
  },
  {
    id: 'layers',
    title: MAINTENANCES_ROUTES_LABELS.LAYERS,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.layers,
  },
  {
    id: 'technologies',
    title: MAINTENANCES_ROUTES_LABELS.TECHNOLOGIES,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.technologies,
  },
];

export const GENERAL_MAINTENANCE_PANELS: MaintenancePanel[] = [
  {
    id: 'categories',
    title: MAINTENANCES_ROUTES_LABELS.CATEGORIES,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.categories,
  },
  {
    id: 'system-types',
    title: MAINTENANCES_ROUTES_LABELS.SYSTEM_TYPES,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.systemTypes,
  },
  {
    id: 'fields',
    title: MAINTENANCES_ROUTES_LABELS.FIELDS,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.fields,
  },
  {
    id: 'commissions',
    title: MAINTENANCES_ROUTES_LABELS.COMMISSIONS,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.commissions,
  },
];
