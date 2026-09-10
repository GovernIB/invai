import { SECURITY_MAINTENANCE_PANELS, SECURITY_RESOURCE_DEFINITIONS } from './security.constants';

describe('security maintenance configuration', () => {
  it('keeps the five available maintenance panels in order', () => {
    expect(SECURITY_MAINTENANCE_PANELS.map(({ id }) => id)).toEqual([
      'web-contexts',
      'identity-providers',
      'personal-data-processing',
      'security-measure-types',
      'ens-requirements',
    ]);
  });

  it('models identity providers as monolingual and the other resources as bilingual', () => {
    expect(SECURITY_RESOURCE_DEFINITIONS['identity-provider'].bilingual).toBe(false);
    expect(
      Object.values(SECURITY_RESOURCE_DEFINITIONS)
        .filter(({ key }) => key !== 'identity-provider')
        .every(({ bilingual }) => bilingual),
    ).toBe(true);
  });
});
