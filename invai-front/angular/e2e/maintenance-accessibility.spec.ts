import { expect, test } from './support/auth.fixture';

import {
  expectPanelFragments,
  type NamedResourceCase,
  runNamedResourceLifecycle,
} from './support/maintenance-resources';

const ACCESSIBILITY_ROUTE = '/manteniments/accessibilitat';

interface AccessibilityResourceCopy {
  panelId: string;
  title: string;
  key: string;
  singular: string;
  plural: string;
}

const ACCESSIBILITY_RESOURCES: AccessibilityResourceCopy[] = [
  {
    panelId: 'classification-segments',
    title: 'Segments de classificació',
    key: 'classification-segment',
    singular: 'segment de classificació',
    plural: 'segments de classificació',
  },
  {
    panelId: 'compliance-situations',
    title: 'Situacions de compliment',
    key: 'compliance-situation',
    singular: 'situació de compliment',
    plural: 'situacions de compliment',
  },
];

function accessibilityCase(copy: AccessibilityResourceCopy): NamedResourceCase {
  const { singular, plural } = copy;
  return {
    route: ACCESSIBILITY_ROUTE,
    panelId: copy.panelId,
    title: copy.title,
    apiPath: `/invaiback/${copy.key}`,
    idPrefix: `accessibility-${copy.key}`,
    bilingual: true,
    singular,
    quickSearchLabel: `Cerca ràpida de ${plural}`,
    filtersButtonLabel: `Mostra o amaga els filtres de ${plural}`,
    addButtonLabel: `Afegeix: ${singular}`,
    actions: {
      accept: `Accepta la consulta: ${singular}`,
      add: `Afegeix: ${singular}`,
      cancel: `Cancel·la els canvis: ${singular}`,
      close: `Tanca el formulari: ${singular}`,
      deactivate: `Dona de baixa: ${singular}`,
      edit: `Edita: ${singular}`,
      restore: `Restaura: ${singular}`,
      save: `Desa els canvis: ${singular}`,
    },
  };
}

test.describe('accessibility maintenances', () => {
  test('the Accessibilitat tab keeps its panels synchronized with the URL fragment', async ({
    page,
  }) => {
    test.setTimeout(120_000);
    await expectPanelFragments(
      page,
      ACCESSIBILITY_ROUTE,
      ACCESSIBILITY_RESOURCES.map(({ title, panelId }) => ({ title, fragment: panelId })),
    );
    await expect(
      page
        .locator('.maintenances-tabs')
        .getByRole('link', { name: 'Accessibilitat', exact: true }),
    ).toHaveAttribute('aria-current', 'page');
  });

  for (const copy of ACCESSIBILITY_RESOURCES) {
    test(`${copy.key}: create, consult, edit, deactivate, inactive filter and restore`, async ({
      page,
    }, testInfo) => {
      test.setTimeout(240_000);
      await runNamedResourceLifecycle(page, testInfo, accessibilityCase(copy));
    });
  }
});
