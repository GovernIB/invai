import { PrimeIcons } from 'primeng/api';
import { SYSTEMS_ROUTES_LABELS, SYSTEMS_ROUTES_LOC } from '@features/systems/systems.routes.i18n';

import { MAINTENANCES_ROUTES_LABELS, MAINTENANCES_ROUTES_LOC } from './maintenances.routes.i18n';
import { MAINTENANCE_PANEL_DESCRIPTIONS } from './maintenances.i18n';
import {
  RESPONSIBLE_AUTHORIZATION_COPY,
  RESPONSIBLE_COMPANY_COPY,
  RESPONSIBLE_PERSON_COPY,
  ROLE_TRANSFER_COPY,
} from './responsibles/responsibles.i18n';

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
    id: 'maintenance-systems',
    icon: PrimeIcons.SERVER,
    label: SYSTEMS_ROUTES_LABELS.BASE,
    path: SYSTEMS_ROUTES_LOC.BASE,
    routerLink: `/${MAINTENANCES_ROUTES_LOC.BASE}/${SYSTEMS_ROUTES_LOC.BASE}`,
  },
  {
    id: 'maintenance-development',
    icon: PrimeIcons.CODE,
    label: MAINTENANCES_ROUTES_LABELS.DEVELOPMENT,
    path: MAINTENANCES_ROUTES_LOC.DEVELOPMENT,
    routerLink: `/${MAINTENANCES_ROUTES_LOC.BASE}/${MAINTENANCES_ROUTES_LOC.DEVELOPMENT}`,
  },
  {
    id: 'maintenance-accessibility',
    icon: PrimeIcons.EYE,
    label: MAINTENANCES_ROUTES_LABELS.ACCESSIBILITY,
    path: MAINTENANCES_ROUTES_LOC.ACCESSIBILITY,
    routerLink: `/${MAINTENANCES_ROUTES_LOC.BASE}/${MAINTENANCES_ROUTES_LOC.ACCESSIBILITY}`,
  },
  {
    id: 'maintenance-security',
    icon: PrimeIcons.SHIELD,
    label: MAINTENANCES_ROUTES_LABELS.SECURITY,
    path: MAINTENANCES_ROUTES_LOC.SECURITY,
    routerLink: `/${MAINTENANCES_ROUTES_LOC.BASE}/${MAINTENANCES_ROUTES_LOC.SECURITY}`,
  },
];

export const DEVELOPMENT_MAINTENANCE_PANELS: MaintenancePanel[] = [
  {
    id: 'provider-roles',
    title: MAINTENANCES_ROUTES_LABELS.ROLES,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.roles,
  },
  {
    id: 'technologies',
    title: MAINTENANCES_ROUTES_LABELS.TECHNOLOGIES,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.technologies,
  },
  {
    id: 'layers',
    title: MAINTENANCES_ROUTES_LABELS.LAYERS,
    description: MAINTENANCE_PANEL_DESCRIPTIONS.layers,
  },
];

export const RESPONSIBLES_MAINTENANCE_PANELS: MaintenancePanel[] = [
  {
    id: 'role-transfer',
    title: ROLE_TRANSFER_COPY.title,
    description: ROLE_TRANSFER_COPY.description,
  },
  {
    id: 'people',
    title: RESPONSIBLE_PERSON_COPY.title,
    description: RESPONSIBLE_PERSON_COPY.description,
  },
  {
    id: 'authorizations',
    title: RESPONSIBLE_AUTHORIZATION_COPY.title,
    description: RESPONSIBLE_AUTHORIZATION_COPY.description,
  },
  {
    id: 'companies',
    title: RESPONSIBLE_COMPANY_COPY.title,
    description: RESPONSIBLE_COMPANY_COPY.description,
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
