import AxeBuilder from '@axe-core/playwright';
import type { Page, TestInfo } from '@playwright/test';

import { expect, test } from './support/auth.fixture';

// Automated checks only support the WCAG 2.2 AA target: keyboard, zoom, contrast of states and
// screen-reader passes stay manual.
const WCAG_TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'];

// Landmark rules are tagged best-practice, so the WCAG tags above never run them. They are run
// explicitly because the shared shell renders several nav landmarks (top menu, sidebar, breadcrumb,
// section tabs) that screen-reader users can only tell apart by a unique accessible name.
const LANDMARK_RULES = [
  'landmark-banner-is-top-level',
  'landmark-complementary-is-top-level',
  'landmark-contentinfo-is-top-level',
  'landmark-main-is-top-level',
  'landmark-no-duplicate-banner',
  'landmark-no-duplicate-contentinfo',
  'landmark-no-duplicate-main',
  'landmark-one-main',
  'landmark-unique',
];

// Known violations still to be fixed in the app; remove each entry together with its fix.
const KNOWN_VIOLATIONS = [
  // The disabled source-code link is a <span> with aria-label, which ARIA prohibits on generic
  // elements (application-development-section.html).
  { rule: 'aria-prohibited-attr', target: '.application-development__external-link' },
];

const DETAIL_TABS = [
  'general',
  'responsible',
  'systems-databases',
  'development',
  'accessibility',
  'security',
  'data',
  'integrations',
  'documentation',
];

const MAINTENANCE_TABS = [
  'general',
  'responsables',
  'sistemes',
  'desenvolupament',
  'accessibilitat',
  'seguretat',
  'integracions',
  'documentacio',
];

async function expectNoViolations(page: Page, testInfo: TestInfo, name: string): Promise<void> {
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  await page.waitForLoadState('networkidle');
  // A tooltip opens when its trigger renders under a resting pointer and fades in for 250 ms, so a
  // scan that caught it half transparent would report its text as low contrast. Parks the pointer
  // off the content and waits for any tooltip to close, so the page is scanned at rest.
  await page.mouse.move(0, 0);
  await expect(page.locator('.p-tooltip')).toHaveCount(0);
  const wcagResults = await new AxeBuilder({ page }).withTags(WCAG_TAGS).analyze();
  const landmarkResults = await new AxeBuilder({ page }).withRules(LANDMARK_RULES).analyze();
  const violations = [...wcagResults.violations, ...landmarkResults.violations];
  await testInfo.attach(`axe-${name}`, {
    body: JSON.stringify(violations, null, 2),
    contentType: 'application/json',
  });
  const summary = violations.flatMap((violation) =>
    violation.nodes
      .map((node) => node.target.join(' '))
      .filter(
        (target) =>
          !KNOWN_VIOLATIONS.some(
            (known) => known.rule === violation.id && target.includes(known.target),
          ),
      )
      .map((target) => `${violation.id} (${violation.impact}): ${target}`),
  );
  expect.soft(summary, `Axe violations on ${name}`).toEqual([]);
}

// A panel header changes its background under the pointer, so its text is also measured hovered:
// otherwise the result depends on where the pointer happens to rest during the scan.
async function expectHoveredPanelHeaderContrast(page: Page, name: string): Promise<void> {
  const header = page.locator('.maintenance-panel-header').first();
  await header.hover();
  // Waits for the hover transition to settle before measuring.
  const background = () =>
    header.evaluate((element) => {
      const trigger = element.closest('button, [role="button"]') ?? element;
      return getComputedStyle(trigger).backgroundColor;
    });
  await expect
    .poll(async () => {
      const before = await background();
      await page.waitForTimeout(150);
      return before === (await background());
    })
    .toBe(true);
  const results = await new AxeBuilder({ page })
    .include('.maintenance-panel-header')
    .withRules(['color-contrast'])
    .analyze();
  const summary = results.violations.flatMap((violation) =>
    violation.nodes.map((node) => `${violation.id}: ${node.target.join(' ')}`),
  );
  expect.soft(summary, `Contrast of the hovered panel header on ${name}`).toEqual([]);
  await page.mouse.move(0, 0);
}

test('the applications list has no automatically detectable WCAG violations', async ({
  page,
}, testInfo) => {
  await page.goto('/aplicacions');
  await expectNoViolations(page, testInfo, 'applications-list');
});

test('every application detail tab has no automatically detectable WCAG violations', async ({
  page,
}, testInfo) => {
  test.setTimeout(180_000);
  await page.goto('/');
  const response = await page.request.get('/invaiback/application', {
    params: { page: 0, size: 1, statusId: 1 },
  });
  expect(response.ok()).toBeTruthy();
  const { content } = (await response.json()) as { content: Array<{ id: number }> };
  expect(content.length).toBeGreaterThan(0);

  for (const tab of DETAIL_TABS) {
    await page.goto(`/aplicacions/${content[0].id}/${tab}`);
    await expectNoViolations(page, testInfo, `application-${tab}`);
  }
});

test('every maintenance tab has no automatically detectable WCAG violations', async ({
  page,
}, testInfo) => {
  test.setTimeout(180_000);
  for (const tab of MAINTENANCE_TABS) {
    await page.goto(`/manteniments/${tab}`);
    await expectNoViolations(page, testInfo, `maintenance-${tab}`);
    await expectHoveredPanelHeaderContrast(page, `maintenance-${tab}`);
  }
});

test('the help centre has no automatically detectable WCAG violations', async ({
  page,
}, testInfo) => {
  test.setTimeout(120_000);
  await page.goto('/');
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  await page.keyboard.press('Control+k');
  const help = page.getByRole('dialog', { name: "Centre d'ajuda" });
  // It opens on the topic of the screen behind it, the list of applications.
  await expect(help.getByRole('heading', { level: 3, name: "Llista d'aplicacions" })).toBeVisible();
  await expectNoViolations(page, testInfo, 'help-centre-screen-topic');

  const topics = help.getByRole('navigation', { name: "Temes d'ajuda" });
  await topics.getByRole('button', { name: 'Primers passos', exact: true }).click();
  await topics.getByRole('button', { name: "Com fer servir l'ajuda" }).click();
  await expect(help.getByRole('heading', { level: 3 })).toBeFocused();
  await expectNoViolations(page, testInfo, 'help-centre-topic');

  // The general topics use nested lists, keys and links, which the first topic does not.
  await topics.getByRole('button', { name: "Fitxa d'aplicació", exact: true }).click();
  for (const [topic, name] of [
    ["Capçalera i menú d'usuari", 'help-centre-header-topic'],
    ['Trucs de la interfície', 'help-centre-tips-topic'],
    ['Desenvolupament', 'help-centre-development-topic'],
    ['Accessibilitat', 'help-centre-accessibility-topic'],
    ['Seguretat', 'help-centre-security-topic'],
    ['Dades', 'help-centre-data-topic'],
    ['Integracions', 'help-centre-integrations-topic'],
    ['Documentació', 'help-centre-documentation-topic'],
    ["Detall d'un document", 'help-centre-document-detail-topic'],
  ]) {
    await topics.getByRole('button', { name: topic }).click();
    await expect(help.getByRole('heading', { level: 3, name: topic })).toBeFocused();
    await expectNoViolations(page, testInfo, name);
  }

  const applications = topics.getByRole('button', { name: 'Aplicacions', exact: true });
  await applications.click();
  await expect(applications).toHaveAttribute('aria-expanded', 'false');
  await expectNoViolations(page, testInfo, 'help-centre-collapsed-group');
});
