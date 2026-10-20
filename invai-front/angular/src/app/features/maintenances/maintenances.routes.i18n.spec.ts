import {
  MAINTENANCES_ROUTES_LABELS,
  MAINTENANCES_ROUTES_LOC,
} from './maintenances.routes.i18n';

describe('maintenance section routes', () => {
  it('matches the application detail section labels', () => {
    expect([
      MAINTENANCES_ROUTES_LABELS.GENERAL,
      MAINTENANCES_ROUTES_LABELS.RESPONSIBLES,
      MAINTENANCES_ROUTES_LABELS.DEVELOPMENT,
      MAINTENANCES_ROUTES_LABELS.SECURITY,
      MAINTENANCES_ROUTES_LABELS.INTEGRATIONS,
    ]).toEqual(['General', 'Responsables', 'Desenvolupament', 'Seguretat', 'Integracions']);
  });

  it('exposes localized canonical paths independently from their labels', () => {
    expect([
      MAINTENANCES_ROUTES_LOC.GENERAL,
      MAINTENANCES_ROUTES_LOC.RESPONSIBLES,
      MAINTENANCES_ROUTES_LOC.DEVELOPMENT,
      MAINTENANCES_ROUTES_LOC.SECURITY,
      MAINTENANCES_ROUTES_LOC.INTEGRATIONS,
    ]).toEqual(['general', 'responsables', 'desenvolupament', 'seguretat', 'integracions']);
  });
});
