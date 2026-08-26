import { expect, test as base } from '@playwright/test';
import path from 'node:path';

import { ensureAuthenticatedSession } from './auth';

const authFile = path.join(__dirname, '../.auth/user.json');

interface AuthFixtures {
  authenticatedSession: void;
}

export const test = base.extend<AuthFixtures>({
  authenticatedSession: [
    async ({ page, baseURL }, use) => {
      if (!baseURL) throw new Error('Playwright baseURL is not configured.');

      const renewed = await ensureAuthenticatedSession(page, baseURL);
      if (renewed) await page.context().storageState({ path: authFile });

      await use();
    },
    { auto: true, timeout: 90_000 },
  ],
});

export { expect };
