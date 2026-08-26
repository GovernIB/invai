import { test as setup } from '@playwright/test';
import path from 'node:path';

import { authenticateAgainstRealIdp } from './support/auth';

const authFile = path.join(__dirname, '.auth/user.json');

setup('authenticate against the real IdP', async ({ page, baseURL }) => {
  setup.setTimeout(90_000);

  if (!baseURL) {
    throw new Error('Playwright baseURL is not configured.');
  }

  await authenticateAgainstRealIdp(page, baseURL);

  await page.context().storageState({ path: authFile });
});
