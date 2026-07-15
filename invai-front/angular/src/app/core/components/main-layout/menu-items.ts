import {
  APPLICATIONS_ROUTES_LABELS,
  APPLICATIONS_ROUTES_LOC,
} from '@features/applications/applications.routes.i18n';
import { DOCUMENTATION_ROUTES_LOC } from '@features/documentation/documentation.routes.i18n';
import { MenuItem, PrimeIcons } from 'primeng/api';

export const MENU_ITEMS: MenuItem[] = [
  {
    label: APPLICATIONS_ROUTES_LABELS.BASE,
    icon: PrimeIcons.TH_LARGE,
    routerLink: APPLICATIONS_ROUTES_LOC.BASE,
  },
  {
    label: 'Documentació',
    icon: PrimeIcons.FOLDER,
    routerLink: DOCUMENTATION_ROUTES_LOC.BASE,
  },
  {
    label: 'Ut. Orgànica',
    icon: PrimeIcons.DATABASE,
  },
  {
    label: 'Informes',
    icon: PrimeIcons.CHART_BAR,
  },
];

/* const FILE_MANAGEMENT_MENU: MenuItem = {
  label: $localize`Gestió d’expedients`,
  icon: PrimeIcons.BRIEFCASE,
  items: [
    {
      icon: PrimeIcons.FILE_EDIT,
      label: $localize`Safata d'entrada`,
      routerLink: `/${FILE_MANAGEMENT_ROUTES_LOC.BASE}/${FILE_MANAGEMENT_ROUTES_LOC.APPLICATION.BASE}`,
      -
      items: [
        {
          id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_ELECTRONIC_PROCEDURES,
          label: $localize`Safata de distribució`,
          routerLink: FULL_ROUTES.FILE_MANAGEMENT.APPLICATION.ELECTRONIC_PROCEDURES,
          icon: PrimeIcons.INBOX,
        },
        {
          id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_APPLICATION_INBOX,
          label: $localize`Safata de tràmits`,
          routerLink: FULL_ROUTES.FILE_MANAGEMENT.APPLICATION.INBOX,
          icon: PrimeIcons.ADDRESS_BOOK,
        },

        {
          id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_APPLICATION_NEW,
          label: $localize`Alta sol·licitud manual`,
          routerLink: FULL_ROUTES.FILE_MANAGEMENT.APPLICATION.NEW,
          icon: PrimeIcons.PLUS,
        },
      ],
    },
    {
      id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_INITIAL_PROCEDURES_RECORDING,
      icon: PrimeIcons.LIST_CHECK,
      label: $localize`Validació`,
      routerLink: FULL_ROUTES.FILE_MANAGEMENT.INITIAL_PROCEDURES.RECORDING,
    },
    {
      id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_PENDING_PRESENTATION_SEARCH,
      icon: PrimeIcons.BOOKMARK,
      label: $localize`Presentació`,
      routerLink: FULL_ROUTES.FILE_MANAGEMENT.PENDING_PRESENTATION.SEARCH,
    },
    {
      id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_MEETING_DEPENDENCY_ASSESSMENT_MEETINGS,
      icon: PrimeIcons.CALENDAR_CLOCK,
      label: $localize`Citació`,
      fragment: `/${FILE_MANAGEMENT_ROUTES_LOC.BASE}/${FILE_MANAGEMENT_ROUTES_LOC.MEETING.BASE}`,
      routerLink: FULL_ROUTES.FILE_MANAGEMENT.MEETING.DISABILITY_ASSESSMENT_MEETINGS,
    },
    {
      id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_DISABILITY_ASSESSMENTS_CONSULT_VALORATION,
      icon: PrimeIcons.BULLSEYE,
      label: $localize`Informes`,
      fragment: `/${FILE_MANAGEMENT_ROUTES_LOC.BASE}/${FILE_MANAGEMENT_ROUTES_LOC.DISABILITY_ASSESSMENTS.BASE}`,
      routerLink: FULL_ROUTES.FILE_MANAGEMENT.DISABILITY_ASSESSMENTS.REPORT_MANAGEMENT,
    },
    {
      icon: PrimeIcons.LIST,
      label: $localize`Requeriments`,
      id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_REQUIREMENTS_CONSULT,
      routerLink: FULL_ROUTES.FILE_MANAGEMENT.REQUIREMENTS.SEARCH,
    },

    {
      icon: PrimeIcons.BOX,
      label: $localize`Resolucions`,
      id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_DICTUMS_CONSULT,
      routerLink: FULL_ROUTES.FILE_MANAGEMENT.RESOLUTIONS.DEGREE,
    },

    {
      icon: PrimeIcons.FILE,
      label: $localize`Llistes`,
      fragment: `/${FILE_MANAGEMENT_ROUTES_LOC.BASE}/${FILE_MANAGEMENT_ROUTES_LOC.WAIT_LIST.BASE}`,
      items: [
        {
          id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_TELECARE_WAIT_LIST,
          label: $localize`Llista d'espera`,
          routerLink: FULL_ROUTES.FILE_MANAGEMENT.WAIT_LIST.GENERAL_LIST,
        },
        {
          id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_TELECARE_RESERVE_LIST,
          label: $localize`Llista de reserva`,
          routerLink: FULL_ROUTES.FILE_MANAGEMENT.WAIT_LIST.RESERVE_LIST,
        },

        {
          id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_TELECARE_ASSIGNMENT_LIST,
          label: $localize`Llista d'assignació`,
          routerLink: FULL_ROUTES.FILE_MANAGEMENT.WAIT_LIST.ASSIGNMENT_LIST,
        },
        {
          id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_TELECARE_OCCUPATION_LIST,
          label: $localize`Llista d'ocupació`,
          routerLink: FULL_ROUTES.FILE_MANAGEMENT.WAIT_LIST.OCCUPATION_LIST,
        },
      ],
    },

    {
      id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_FILE_CONSULTATION_CONSULT,
      icon: PrimeIcons.FOLDER_OPEN,
      label: $localize`Consulta expedients`,
      routerLink: FULL_ROUTES.FILE_MANAGEMENT.FILE_CONSULTATION.CONSULT,
    },

    {
      icon: PrimeIcons.USERS,
      label: $localize`Persones`,
      routerLink: FULL_ROUTES.FILE_MANAGEMENT.PERSONS.SEARCH,
      id: FILE_MANAGEMENT_ROUTES_LOC_IDS.FILE_MANAGEMENT_PERSONS_SEARCH,
    },
  ],
};

const MAINTENANCE_MENU: MenuItem = {
  label: $localize`Gestions`,
  icon: PrimeIcons.WRENCH,
  items: [
    {
      id: MAINTENANCES_ROUTES_LOC_IDS.MAINTENANCES_USER_MANAGEMENT,
      icon: PrimeIcons.USER_EDIT,
      label: $localize`Gestió d'usuaris, rols i perfils`,
      routerLink: FULL_ROUTES.MAINTENANCES.USER_MANAGEMENT,
    },
    {
      id: MAINTENANCES_ROUTES_LOC_IDS.MAINTENANCES_CENTERS_MANAGEMENT,
      icon: PrimeIcons.BUILDING_COLUMNS,
      label: $localize`Gestió de entitats`,
      routerLink: FULL_ROUTES.MAINTENANCES.CENTERS_MANAGEMENT,
    },
    {
      id: MAINTENANCES_ROUTES_LOC_IDS.MAINTENANCES_ASSOCIATION_MANAGEMENT,
      icon: PrimeIcons.BUILDING_COLUMNS,
      label: $localize`Gestió de servei`,
      routerLink: FULL_ROUTES.MAINTENANCES.ASSOCIATION_MANAGEMENT,
    },
    {
      id: MAINTENANCES_ROUTES_LOC_IDS.ADMINISTRATION_SQL_CONSOLE,
      icon: PrimeIcons.BUILDING_COLUMNS,
      label: $localize`SQL`,
      routerLink: FULL_ROUTES.ADMINISTRATION.SQL,
    },
  ],
};

export const MENU_ITEMS: MenuItem[] = [FILE_MANAGEMENT_MENU, MAINTENANCE_MENU]; */
