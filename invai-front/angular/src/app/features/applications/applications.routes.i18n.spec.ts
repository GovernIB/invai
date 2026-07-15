import {
  APPLICATIONS_ROUTES_LABELS,
  APPLICATIONS_ROUTES_LOC,
} from './applications.routes.i18n';

describe('APPLICATIONS_ROUTES_LOC', () => {
  it('should expose a lowercase base route independently from its visible label', () => {
    expect(APPLICATIONS_ROUTES_LOC.BASE).toBe('aplicacions');
    expect(APPLICATIONS_ROUTES_LABELS.BASE).toBe('Aplicacions');
  });
});
