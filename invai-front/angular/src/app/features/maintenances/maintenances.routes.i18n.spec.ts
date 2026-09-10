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
    ]).toEqual(['General', 'Responsables', 'Desenvolupament', 'Seguretat']);
  });

  it('exposes localized canonical paths independently from their labels', () => {
    expect([
      MAINTENANCES_ROUTES_LOC.GENERAL,
      MAINTENANCES_ROUTES_LOC.RESPONSIBLES,
      MAINTENANCES_ROUTES_LOC.DEVELOPMENT,
      MAINTENANCES_ROUTES_LOC.SECURITY,
    ]).toEqual(['general', 'responsables', 'desenvolupament', 'seguretat']);
  });
});
