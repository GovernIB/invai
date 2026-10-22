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
  }
});
