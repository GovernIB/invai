import { expect, test, type Page, type Route } from '@playwright/test';

import { selectOption, selectRowAction } from './support/ui';

test.use({ storageState: { cookies: [], origins: [] } });

const pageOf = (content: unknown[]) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  last: true,
  number: 0,
});

const PLEXUS = { id: 3, name: 'Plexus Tech', nif: 'B00000000', deletedAt: null };
const SOFFID = { id: 1, name: 'Soffid', company: PLEXUS, deletedAt: null };

interface ExternalSystemsMock {
  systems: Array<typeof SOFFID>;
  posts: unknown[];
  deletes: string[];
}

async function openExternalSystems(page: Page): Promise<ExternalSystemsMock> {
  const mock: ExternalSystemsMock = { systems: [SOFFID], posts: [], deletes: [] };
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const method = request.method();
    if (path.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'external-systems-test' } });
    }
    if (path.endsWith('/company')) return route.fulfill({ json: pageOf([PLEXUS]) });
    if (path.endsWith('/external-system') && method === 'POST') {
      const body = request.postDataJSON();
      mock.posts.push(body);
      const created = { id: 5, name: body.name, company: PLEXUS, deletedAt: null };
      mock.systems = [...mock.systems, created];
      return route.fulfill({ status: 201, json: created });
    }
    if (/\/external-system\/\d+$/.test(path) && method === 'DELETE') {
      mock.deletes.push(path);
      mock.systems = mock.systems.filter((system) => !path.endsWith(`/${system.id}`));
      return route.fulfill({ status: 204 });
    }
    if (/\/external-system\/\d+$/.test(path)) {
      const id = Number(path.split('/').pop());
      return route.fulfill({ json: mock.systems.find((system) => system.id === id) });
    }
    if (path.endsWith('/external-system')) return route.fulfill({ json: pageOf(mock.systems) });
    return route.fulfill({ json: pageOf([]) });
  });
  await page.goto('/manteniments/integracions#external-systems');
  return mock;
}

const rows = (page: Page) => page.locator('app-external-systems-table tbody tr');

test('adds the Integracions maintenance tab with the external systems panel', async ({ page }) => {
  await openExternalSystems(page);

  await expect(page.locator('.maintenances-tabs a').last()).toHaveText('Integracions');
  await expect(page.getByRole('button', { name: /Sistemes externs/ })).toHaveAttribute('aria-expanded', 'true');
  await expect(rows(page)).toHaveCount(1);
  await expect(rows(page).first()).toContainText('Soffid');
  await expect(rows(page).first()).toContainText('Plexus Tech');
});

test('creates and deactivates an external system', async ({ page }, testInfo) => {
  const mock = await openExternalSystems(page);
  await expect(rows(page)).toHaveCount(1);

  await page.getByRole('button', { name: 'Afegeix un sistema extern' }).click();
  await page.getByRole('button', { name: 'Afegeix el sistema extern' }).click();
  await expect(page.getByText('Camp obligatori').first()).toBeVisible();
  expect(mock.posts).toHaveLength(0);

  await page.locator('#external-system-dialog-name').fill('  DIR3CAIB  ');
  await selectOption(page, 'external-system-dialog-company', 'Plexus Tech');
  await page.screenshot({ path: testInfo.outputPath('external-systems-create.png'), fullPage: true });
  await page.getByRole('button', { name: 'Afegeix el sistema extern' }).click();

  await expect.poll(() => mock.posts).toEqual([{ name: 'DIR3CAIB', companyId: 3 }]);
  await expect(page.getByText("El sistema extern s'ha afegit correctament.")).toBeVisible();
  await expect(rows(page)).toHaveCount(2);

  await selectRowAction(page, rows(page).first(), 'Dona de baixa', 'Obrir les accions del sistema extern');
  await page.getByRole('button', { name: 'Confirma la baixa del sistema extern' }).click();
  await expect.poll(() => mock.deletes).toEqual(['/invaiback/external-system/1']);
});
