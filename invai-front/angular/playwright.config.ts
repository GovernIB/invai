import 'dotenv/config';
import { defineConfig, devices } from '@playwright/test';

const baseURL = 'http://localhost:4200';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  reporter: 'html',
  webServer: {
    command: 'npm run local',
    url: baseURL,
    reuseExistingServer: true,
    timeout: 120_000,
  },
  use: {
    baseURL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts/,
      use: { trace: 'off', screenshot: 'off' },
    },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'], storageState: 'e2e/.auth/user.json' },
      dependencies: ['setup'],
    },
  ],
});
