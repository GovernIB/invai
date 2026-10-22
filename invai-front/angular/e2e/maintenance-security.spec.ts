import { expect, test } from './support/auth.fixture';

import {
  expectPanelFragments,
  type NamedResourceCase,
  runNamedResourceLifecycle,
} from './support/maintenance-resources';

const SECURITY_ROUTE = '/manteniments/seguretat';

interface SecurityResourceCopy {
  panelId: string;
  title: string;
  key: string;
  singular: string;
  plural: string;
  bilingual: boolean;
}

const SECURITY_RESOURCES: SecurityResourceCopy[] = [
  {
    panelId: 'web-contexts',
    title: 'Contexts web',
    key: 'web-context',
    singular: 'context web',
    plural: 'contexts web',
    bilingual: true,
  },
  {
    panelId: 'identity-providers',
    title: "Proveïdors d'identitat",
    key: 'identity-provider',
    singular: "proveïdor d'identitat",
    plural: "proveïdors d'identitat",
    bilingual: false,
  },
  {
    panelId: 'personal-data-processing',
    title: 'Tractaments de dades personals',
    key: 'personal-data-processing',
    singular: 'tractament de dades personals',
    plural: 'tractaments de dades personals',
    bilingual: true,
  },
  {
    panelId: 'security-measure-types',
    title: 'Tipus de mesura de seguretat',
    key: 'security-measure-type',
    singular: 'tipus de mesura de seguretat',
    plural: 'tipus de mesura de seguretat',
    bilingual: true,
  },
  {
    panelId: 'ens-requirements',
    title: 'Requisits ENS',
    key: 'ens-requirement',
    singular: 'requisit ENS',
    plural: 'requisits ENS',
    bilingual: true,
  },
];

function securityCase(copy: SecurityResourceCopy): NamedResourceCase {
  const { singular, plural } = copy;
  return {
    route: SECURITY_ROUTE,
    panelId: copy.panelId,
    title: copy.title,
    apiPath: `/invaiback/${copy.key}`,
    idPrefix: `security-${copy.key}`,
    bilingual: copy.bilingual,
    singular,
    quickSearchLabel: `Cerca ràpida de ${plural}`,
    filtersButtonLabel: `Mostra o amaga els filtres de ${plural}`,
    addButtonLabel: `Afegeix un ${singular}`,
    actions: {
      accept: `Accepta la consulta del ${singular}`,
      add: `Afegeix el ${singular}`,
      cancel: `Cancel·la els canvis del ${singular}`,
      close: `Tanca el formulari del ${singular}`,
      deactivate: `Dona de baixa el ${singular}`,
      edit: `Edita el ${singular}`,
      restore: `Restaura el ${singular}`,
      save: `Desa els canvis del ${singular}`,
    },
  };
}

test.describe('security maintenances', () => {
  test('the Seguretat tab keeps its five panels synchronized with the URL fragment', async ({
    page,
  }) => {
    test.setTimeout(120_000);
    await expectPanelFragments(
      page,
      SECURITY_ROUTE,
      SECURITY_RESOURCES.map(({ title, panelId }) => ({ title, fragment: panelId })),
    );
    await expect(
      page.locator('.maintenances-tabs').getByRole('link', { name: 'Seguretat', exact: true }),
    ).toHaveAttribute('aria-current', 'page');
  });

  for (const copy of SECURITY_RESOURCES) {
    test(`${copy.key}: create, consult, edit, deactivate, inactive filter and restore`, async ({
      page,
    }, testInfo) => {
      test.setTimeout(240_000);
      await runNamedResourceLifecycle(page, testInfo, securityCase(copy));
    });
  }
});
