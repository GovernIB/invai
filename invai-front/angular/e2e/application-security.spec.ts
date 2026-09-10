import type { ConsoleMessage } from '@playwright/test';

import { expect, test } from './support/auth.fixture';

interface ApplicationPageResponse {
  content: Array<{ id: number }>;
}

test('navigates from application detail to Security', async ({ page }) => {
  const pageErrors: string[] = [];
  const consoleErrors: string[] = [];
  page.on('pageerror', (error) => pageErrors.push(error.message));
  page.on('console', (message: ConsoleMessage) => {
    if (message.type() === 'error') consoleErrors.push(message.text());
  });

  await page.goto('/');
  const applicationsResponse = await page.request.get('/invaiapi/interna/application', {
    params: { page: 0, size: 1, statusId: 1 },
  });
  expect(applicationsResponse.ok()).toBeTruthy();
  const applications = (await applicationsResponse.json()) as ApplicationPageResponse;
  expect(applications.content.length).toBeGreaterThan(0);

  const applicationId = applications.content[0].id;
  await page.goto(`/aplicacions/${applicationId}/general`);
  await page.getByRole('link', { name: /Seguretat/ }).click();

  await expect(page).toHaveURL(new RegExp(`/aplicacions/${applicationId}/security$`), {
    timeout: 15_000,
  });
  await expect(page.getByRole('heading', { name: 'Seguretat', exact: true })).toBeVisible();
  expect(pageErrors).toEqual([]);
  expect(consoleErrors).toEqual([]);
});
