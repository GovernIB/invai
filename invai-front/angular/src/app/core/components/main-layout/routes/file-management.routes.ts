/* eslint-disable max-lines */

export const FILE_MANAGEMENT_ROUTES_LOC = {
  BASE: $localize`gestio-expedients`,
  PAYROLL: {
    BASE: $localize`nomina`,
    MANAGEMENT: $localize`partida-presupuestaria`,
    SUSPENSION_PERIODS: {
      BASE: $localize`periodes-suspensio`,
      DETAIL: $localize`detall`,
    },
  },
  APPLICATION: {
    BASE: $localize`solicitud`,
    ELECTRONIC_PROCEDURES: $localize`tramits-electronics`,
    INBOX: $localize`safata`,
    NEW: $localize`nova`,
    MODE: $localize`mode`,
    EDIT: $localize`editar`,
    DETAIL: $localize`detall`,
  },
  DEPENDENCY_ASSESSMENTS: {
    BASE: $localize`valoracions-dependencia`,
    ASSESSMENTS: {
      BASE: $localize`valoracions`,
      CREATE: $localize`crear`,
      EDIT: $localize`editar`,
      DETAIL: $localize`detall`,
    },
    CONSULT_AGENDA: {
      BASE: $localize`consultar-agenda`,
    },
    PENDING_REVIEWS: {
      BASE: $localize`revisio`,
      DIAGNOSIS: {
        BASE: $localize`gestio-diagnostic`,
        CREATE: $localize`crear`,
        EDIT: $localize`editar`,
      },
    },
  },
  DISABILITY_ASSESSMENTS: {
    BASE: $localize`valoracions-discapacitat`,
    URGENT_PROCEDURE_MANAGEMENT: {
      BASE: $localize`gestio-tramits-urgents-discapacitat`,
    },
    CONSULT_AGENDA: {
      BASE: $localize`consultar-agenda`,
    },
    PENDING_REVIEWS: {
      BASE: $localize`pendent-revisar`,
      DETAIL: $localize`detall`,
    },
    REPORT_MANAGEMENT: {
      BASE: $localize`consulta-informes`,
    },
    REPORTS: {
      BASE: $localize`informe`,
      MEDICAL_REPORT: {
        BASE: $localize`informe-sanitari`,
        EDIT: $localize`editar`,
        DETAIL: $localize`detall`,
      },
      PSYCHOLOGIST_REPORT: {
        BASE: $localize`informe-psicologic`,
        EDIT: $localize`editar`,
        DETAIL: $localize`detall`,
      },
      SOCIAL_REPORT: {
        BASE: $localize`informe-social`,
        EDIT: $localize`editar`,
        DETAIL: $localize`detall`,
      },
      BAREDI_REPORT: {
        BASE: $localize`informe-baredi`,
        EDIT: $localize`editar`,
        DETAIL: $localize`detall`,
      },

      CREATE_EDIT_REPORT: {
        BASE: $localize`crear-editar-informe`,
      },

      PROPOSAL_REPORT: {
        BASE: $localize`proposta-informe`,
      },
    },
  },
  FILE_CONSULTATION: {
    BASE: $localize`consulta-expedients`,
    CONSULT: $localize`consulta`,
    LIFE_CYCLE: $localize`cicle-de-vida`,
  },

  INITIAL_PROCEDURES: {
    BASE: $localize`tramits-inicials`,
    RECORDING: $localize`validacio`,
    PENDING_HEALTH_REPORT: $localize`pendent-informes-salut`,
    EDIT: $localize`editar`,
  },
  INCIDENT: {
    BASE: $localize`incidencies`,
    INBOX: $localize`safata`,
    CREATE: $localize`crear`,
    DETAIL: $localize`detall`,
    STATS: $localize`estadistiques`,
    MIGRATED: $localize`migrat`,
  },
  LIFE_CYCLE_REVIEWS: {
    BASE: $localize`tramits-ofici`,
  },
  MEETING: {
    BASE: $localize`citacio`,
    MAINTENANCES: {
      BASE: $localize`manteniments`,
      TECHNICAL_MANAGER: $localize`responsable-valorador`,
      TECHNICAL: $localize`tecnic-valorador`,
    },
    DEPENDENCY_ASSESSMENT_MEETINGS: {
      BASE: $localize`gestio-cites-dependencia`,
      DEPENDENCY_CALENDAR: $localize`cites-dependencia`,
    },
    DISABILITY_ASSESSMENT_MEETINGS: {
      BASE: $localize`gestio-cites`,
      DISABILITY_CALENDAR: $localize`cites`,
    },
    SOCIAL_WORKER_MEETINGS: {
      BASE: $localize`gestio-cites-ts`,
      SOCIAL_WORKER_CALENDAR: $localize`cites-ts`,
    },
    CONSULT_AGENDA: {
      BASE: $localize`consultar-agenda`,
    },
  },
  SOCIAL_WORKERS: {
    BASE: $localize`treballador-social`,
    INTERVENTION: {
      BASE: $localize`intervencio`,
      DETAIL: $localize`detall`,
      NEW: $localize`crear`,
    },

    TS_MANAGEMENT: {
      BASE: $localize`gestio-ts`,
    },
  },
  PERSONS: {
    BASE: $localize`persones`,
    SEARCH: $localize`cercador`,
    CREATE: $localize`crear`,
    EDIT: $localize`editar`,
    DETAIL: $localize`detall`,
    CARER: {
      BASE: $localize`cuidadores`,
      CREATE: $localize`crear`,
      EDIT: $localize`editar`,
      DETAIL: $localize`detall`,
    },
    REPRESENTATIVE: {
      BASE: $localize`representants`,
      CREATE: $localize`crear`,
      EDIT: $localize`editar`,
      DETAIL: $localize`detall`,
    },
  },
  REQUIREMENTS: {
    BASE: $localize`requeriments`,
    SEARCH: $localize`cerca`,
    DETAIL: $localize`detall`,
    CORRECTION: $localize`subsanar`,
  },
  COMMUNICATIONS: {
    BASE: $localize`comunicacio`,
    SEARCH: $localize`cerca`,
    DETAIL: $localize`detall`,
  },
  PENDING_PRESENTATION: {
    BASE: $localize`pendents-presentar`,
    SEARCH: $localize`cercador`,
    EDIT: $localize`editar`,
  },
  DICTUMS: {
    BASE: $localize`dictamen`,
    MANUAL: $localize`manual`,
    SEARCH: {
      BASE: $localize`cercador`,
      DETAIL: $localize`detall`,
      EDIT: $localize`editar`,
    },
    PENDING_ORGANISM: {
      BASE: $localize`pendents-organ`,
    },
  },
  HEARING_PROCEDURES: {
    BASE: $localize`gestio-tramits-audiencia`,
  },
  INSTRUCTION_PROCEDURES: {
    BASE: $localize`tramits-instruccio`,
    CORRECTION_PROCEDURE: {
      BASE: $localize`subsanacio`,
      DETAIL: $localize`detall`,
      EDIT: $localize`editar`,
      CREATE: $localize`crear`,
    },
    URGENCY_PROCEDURE_DISABILITY: {
      BASE: $localize`urgencia-discapacitat`,
      DETAIL: $localize`detall`,
      EDIT: $localize`editar`,
      CREATE: $localize`crear`,
    },

    URGENCY_PROCEDURE_DEPENDENCY: {
      BASE: $localize`urgencia-dependencia`,
      DETAIL: $localize`detall`,
      EDIT: $localize`editar`,
      CREATE: $localize`crear`,
    },
    CHANGE_CARER: {
      BASE: $localize`canvi-cuidador`,
      EDIT: $localize`editar`,
      CREATE: $localize`crear`,
    },
    CHANGE_CARER_DEDICATION: {
      BASE: $localize`canvi-jornada-cuidador`,
      EDIT: $localize`editar`,
      CREATE: $localize`crear`,
    },
  },
  TRANSFER_OUT_MANAGEMENT: {
    BASE: $localize`gestio-trasllats`,
    SEARCH: $localize`cerca`,
  },
  LINKED_TRANSFER_OUT_MANAGEMENT: {
    BASE: $localize`gestio-trasllats-vinculats`,
    SEARCH: $localize`cerca`,
  },

  RESOLUTIONS: {
    BASE: $localize`resolucions`,
    DEGREE: {
      BASE: $localize`grau`,
    },
  },
  PRINTERS: {
    BASE: $localize`impressions`,
    DEGREE_RESOLUTIONS: {
      BASE: $localize`resolucions-grau`,
    },
    REQUIREMENTS: {
      BASE: $localize`requeriments`,
    },
  },
  PIA: {
    BASE: $localize`pia`,
    PENDING: $localize`pendents`,
    DETAIL: $localize`detall`,
    BY_BENEFICIARY: {
      BASE: $localize`beneficiari`,
      DETAIL: $localize`detall`,
    },
  },
  SOCIAL_REPORT: {
    BASE: $localize`informe-social`,
    CREATE: $localize`crear`,
    EDIT: $localize`editar`,
    DETAIL: $localize`detall`,
  },
  ECONOMIC_CAPACITY: {
    BASE: $localize`capacitat-economica`,
    SEARCH: $localize`cerca`,
    OLD: $localize`antics`,
    NEW: $localize`nou`,
    PENDING_INFO: $localize`pendent-aportar-documentacio`,
  },
  PROCEDURE_AND_SERVICES: {
    BASE: $localize`tramits-prestacions-serveis`,

    SENIOR_BOOKING_CENTER: {
      BASE: $localize`central-de-reserves-de-gent-gran`,
      RESERVATION: {
        BASE: $localize`general`,
      },
      RESERVE_LIST: {
        BASE: $localize`reserva`,
      },
      OCCUPATION: {
        BASE: $localize`ocupacions`,
      },
      ASIGN: {
        BASE: $localize`asignacions`,
      },
      SUITABILITY_VALIDATION: {
        BASE: $localize`validacio-adequacio`,
        SEARCH: $localize`cercador`,
      },
      GENERAL_LIST: {
        BASE: $localize`general`,
        SEARCH: $localize`cercador`,
      },

      TRANSFER_LIST: {
        BASE: $localize`gestio-trasllats`,
        SEARCH: $localize`cercador`,
        DETAIL: $localize`detall`,
      },
    },

    AVS_SAD_LINKED: {
      BASE: $localize`avs-sad-vinculades`,
      LINKED: {
        BASE: $localize`llista-despera`,
        SEARCH: $localize`cerca`,
      },
      RESERVE_LIST: {
        BASE: $localize`llista-reserva`,
        SEARCH: $localize`cerca`,
      },
      INVOICE: {
        BASE: $localize`gestio-de-factures`,
        DETAIL: $localize`detall-de-factura`,
        SEARCH: $localize`cerca`,
      },
    },
  },
  CARERS_CHANGE_MANAGEMENT: {
    BASE: $localize`gestio-canvi-cuidadors`,
  },
  WAIT_LIST: {
    BASE: $localize`llista-espera`,
    GENERAL_LIST: {
      BASE: $localize`llista-general`,
    },
    RESERVE_LIST: {
      BASE: $localize`llista-reserva`,
    },
    ASSIGNMENT_LIST: {
      BASE: $localize`llista-d-assignacio`,
    },
    OCCUPATION_LIST: {
      BASE: $localize`llista-d-ocupacio`,
    },
  },
};

export const FILE_MANAGEMENT_ROUTES_LOC_IDS = {
  FILE_MANAGEMENT_REQUIREMENTS_CONSULT: 'FILE_MANAGEMENT_REQUIREMENTS_CONSULT',
  FILE_MANAGEMENT_PERSONS_SEARCH: 'FILE_MANAGEMENT_PERSONS_SEARCH',
  FILE_MANAGEMENT_PERSONS_CREATE: 'FILE_MANAGEMENT_PERSONS_CREATE',
  FILE_MANAGEMENT_PENDING_PRESENTATION_SEARCH: 'FILE_MANAGEMENT_PENDING_PRESENTATION_SEARCH',
  FILE_MANAGEMENT_PENDING_ORGANISM: 'FILE_MANAGEMENT_PENDING_ORGANISM',
  FILE_MANAGEMENT_MEETING_MAINTENANCES_TECHNICAL_MANAGER:
    'FILE_MANAGEMENT_MEETING_MAINTENANCES_TECHNICAL_MANAGER',
  FILE_MANAGEMENT_MEETING_MAINTENANCES_TECHNICAL: 'FILE_MANAGEMENT_MEETING_MAINTENANCES_TECHNICAL',
  FILE_MANAGEMENT_MEETING_DEPENDENCY_ASSESSMENT_MEETINGS:
    'FILE_MANAGEMENT_MEETING_DEPENDENCY_ASSESSMENT_MEETINGS',
  FILE_MANAGEMENT_MEETING_DISABILITY_ASSESSMENT_MEETINGS:
    'FILE_MANAGEMENT_MEETING_DISABILITY_ASSESSMENT_MEETINGS',
  FILE_MANAGEMENT_LIFE_CYCLE_REVIEWS: 'FILE_MANAGEMENT_LIFE_CYCLE_REVIEWS',
  FILE_MANAGEMENT_INITIAL_PROCEDURES_RECORDING: 'FILE_MANAGEMENT_INITIAL_PROCEDURES_RECORDING',
  FILE_MANAGEMENT_INITIAL_PROCEDURES_PENDING_HEALTH_REPORT:
    'FILE_MANAGEMENT_INITIAL_PROCEDURES_PENDING_HEALTH_REPORT',
  FILE_MANAGEMENT_FILE_CONSULTATION_CONSULT: 'FILE_MANAGEMENT_FILE_CONSULTATION_CONSULT',

  FILE_MANAGEMENT_INFORMATION_CONSULTATION: 'FILE_MANAGEMENT_INFORMATION_CONSULTATION',
  FILE_MANAGEMENT_ELECTRONIC_PROCEDURES: 'FILE_MANAGEMENT_ELECTRONIC_PROCEDURES',
  FILE_MANAGEMENT_DISABILITY_ASSESSMENTS_PENDING_REVIEWS:
    'FILE_MANAGEMENT_DISABILITY_ASSESSMENTS_PENDING_REVIEWS',
  FILE_MANAGEMENT_DISABILITY_ASSESSMENTS_CONSULT_VALORATION:
    'FILE_MANAGEMENT_DISABILITY_ASSESSMENTS_CONSULT_VALORATION',
  FILE_MANAGEMENT_DISABILITY_ASSESSMENTS_CONSULT_AGENDA:
    'FILE_MANAGEMENT_DISABILITY_ASSESSMENTS_CONSULT_AGENDA',
  FILE_MANAGEMENT_DICTUMS_CONSULT: 'FILE_MANAGEMENT_DICTUMS_CONSULT',
  FILE_MANAGEMENT_DEPENDENCY_ASSESSMENTS_PENDING_REVIEWS:
    'FILE_MANAGEMENT_DEPENDENCY_ASSESSMENTS_PENDING_REVIEWS',
  FILE_MANAGEMENT_DEPENDENCY_ASSESSMENTS_CONSULT: 'FILE_MANAGEMENT_DEPENDENCY_ASSESSMENTS_CONSULT',
  FILE_MANAGEMENT_DEPENDENCY_ASSESSMENTS_CONSULT_AGENDA:
    'FILE_MANAGEMENT_DEPENDENCY_ASSESSMENTS_CONSULT_AGENDA',
  FILE_MANAGEMENT_APPLICATION_NEW: 'FILE_MANAGEMENT_APPLICATION_NEW',
  FILE_MANAGEMENT_APPLICATION_INBOX: 'FILE_MANAGEMENT_APPLICATION_INBOX',
  FILE_MANAGEMENT_MEETING_SOCIAL_WORKER_MEETINGS: 'FILE_MANAGEMENT_MEETING_SOCIAL_WORKER_MEETINGS',
  FILE_MANAGEMENT_COMMUNICATIONS_CONSULT: 'FILE_MANAGEMENT_COMMUNICATIONS_CONSULT',
  FILE_MANAGEMENT_SOCIAL_WORKERS_CONSULT_AGENDA: 'FILE_MANAGEMENT_SOCIAL_WORKERS_CONSULT_AGENDA',
  FILE_MANAGEMENT_SOCIAL_WORKERS_CONSULT_INTERVENTION:
    'FILE_MANAGEMENT_SOCIAL_WORKERS_CONSULT_INTERVENTION',
  FILE_MANAGEMENT_HEARING_PROCEDURES_SEARCH: 'FILE_MANAGEMENT_HEARING_PROCEDURES_SEARCH',
  FILE_MANAGEMENT_TRANSFER_MANAGEMENT_SEARCH: 'FILE_MANAGEMENT_TRANSFER_MANAGEMENT_SEARCH',
  FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_CORRECTION_PROCEDURE:
    'FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_CORRECTION_PROCEDURE',
  FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_URGENCY_PROCEDURE:
    'FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_URGENCY_PROCEDURE',
  FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_CHANGE_CARER:
    'FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_CHANGE_CARER',
  FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_CHANGE_CARER_DEDICATION:
    'FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_CHANGE_CARER_DEDICATION',
  FILE_MANAGEMENT_PRINTERS_REQUIREMENTS: 'FILE_MANAGEMENT_PRINTERS_REQUIREMENTS',
  FILE_MANAGEMENT_PRINTERS_DEGREE_RESOLUTIONS: 'FILE_MANAGEMENT_PRINTERS_DEGREE_RESOLUTIONS',
  FILE_MANAGEMENT_LIFE_CYCLE: 'FILE_MANAGEMENT_LIFE_CYCLE',
  FILE_MANAGEMENT_URGENT_PROCEDURES_MANAGEMENT: 'FILE_MANAGEMENT_URGENT_PROCEDURES_MANAGEMENT',
  FILE_MANAGEMENT_INCIDENT_MANAGEMENT: 'FILE_MANAGEMENT_INCIDENT_MANAGEMENT',
  FILE_MANAGEMENT_INCIDENT_KPI: 'FILE_MANAGEMENT_INCIDENT_KPI',
  FILE_MANAGEMENT_INCIDENT_MIGRATED: 'FILE_MANAGEMENT_INCIDENT_MIGRATED',
  FILE_MANAGEMENT_ECONOMIC_CAPACITY: 'FILE_MANAGEMENT_ECONOMIC_CAPACITY',
  FILE_MANAGEMENT_ECONOMIC_CAPACITY_CREATE: 'FILE_MANAGEMENT_ECONOMIC_CAPACITY_CREATE',
  FILE_MANAGEMENT_PIA_PENDING: 'FILE_MANAGEMENT_PIA_PENDING',
  FILE_MANAGEMENT_PIA_BY_BENEFICIARY: 'FILE_MANAGEMENT_PIA_BY_BENEFICIARY',
  FILE_MANAGEMENT_PIA_DETAIL: 'FILE_MANAGEMENT_PIA_DETAIL',
  FILE_MANAGEMENT_SOCIAL_REPORT: 'FILE_MANAGEMENT_SOCIAL_REPORT',
  FILE_MANAGEMENT_CARERS_CHANGE: 'FILE_MANAGEMENT_CARERS_CHANGE',

  FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_URGENCY_DISABILITY:
    'FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_URGENCY_DISABILITY',
  FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_URGENCY_DEPENDENCY:
    'FILE_MANAGEMENT_INSTRUCTION_PROCEDURES_URGENCY_DEPENDENCY',
  FILE_MANAGEMENT_LINKED_AVS_SAD: 'FILE_MANAGEMENT_LINKED_AVS_SAD',
  FILE_MANAGEMENT_SENIOR_BOOKING_CENTER: 'FILE_MANAGEMENT_SENIOR_BOOKING_CENTER',
  FILE_MANAGEMENT_LINKED_AVS_SAD_WAITING_LIST: 'FILE_MANAGEMENT_LINKED_AVS_SAD_WAITING_LIST',
  FILE_MANAGEMENT_LINKED_AVS_SAD_RESERVE_LIST: 'FILE_MANAGEMENT_LINKED_AVS_SAD_RESERVE_LIST',
  FILE_MANAGEMENT_TELECARE_WAIT_LIST: 'FILE_MANAGEMENT_TELECARE_WAIT_LIST',
  FILE_MANAGEMENT_TELECARE_RESERVE_LIST: 'FILE_MANAGEMENT_TELECARE_RESERVE_LIST',
  FILE_MANAGEMENT_TELECARE_ASSIGNMENT_LIST: 'FILE_MANAGEMENT_TELECARE_ASSIGNMENT_LIST',
  FILE_MANAGEMENT_TELECARE_OCCUPATION_LIST: 'FILE_MANAGEMENT_TELECARE_OCCUPATION_LIST',
  FILE_MANAGEMENT_RESERVE_LIST: 'FILE_MANAGEMENT_RESERVE_LIST',
  FILE_MANAGEMENT_LINKED_INVOICE: 'FILE_MANAGEMENT_LINKED_INVOICE',

  FILE_MANAGEMENT_LINKED_AVS_SAD_TRANSFER_MANAGEMENT:
    'FILE_MANAGEMENT_LINKED_AVS_SAD_TRANSFER_MANAGEMENT',
  FILE_MANAGEMENT_SENIOR_BOOKING_OFFICE_GENERAL_LIST:
    'FILE_MANAGEMENT_SENIOR_BOOKING_OFFICE_GENERAL_LIST',

  FILE_MANAGEMENT_SUITABILITY_VALIDATION: 'FILE_MANAGEMENT_SUITABILITY_VALIDATION',
  FILE_MANAGEMENT_ASIGN_LIST: 'FILE_MANAGEMENT_ASIGN_LIST',
  FILE_MANAGEMENT_OCCUPATION_LIST: 'FILE_MANAGEMENT_OCCUPATION_LIST',
  FILE_MANAGEMENT_TRANSFER_LIST: 'FILE_MANAGEMENT_TRANSFER_LIST',
};
