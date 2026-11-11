import 'dotenv/config';
import { defineConfig, devices } from '@playwright/test';

const baseURL = 'http://localhost:4200';

// The help screenshots rewrite files in public/help, so their project only exists when they are
// run through `npm run help:screenshots`; `npm run test:e2e` never takes them.
const takeHelpScreenshots = process.env['npm_lifecycle_event'] === 'help:screenshots';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  reporter: [['line'], ['html', { open: 'never' }]],
  webServer: {
    // Writing a capture must not reload the page and close the next dialog being captured.
    command: takeHelpScreenshots ? 'npm run local -- --live-reload=false --hmr=false' : 'npm run local',
    url: baseURL,
    reuseExistingServer: true,
    timeout: 120_000,
    // Shows the dev server output in the run, so a crash of ng serve can be told from a test failure.
    name: 'ng serve',
    stdout: 'pipe',
    stderr: 'pipe',
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
      testIgnore: '**/help-screenshots/**',
      use: { ...devices['Desktop Chrome'], storageState: 'e2e/.auth/user.json' },
      dependencies: ['setup'],
    },
    ...(takeHelpScreenshots
      ? [
          {
            name: 'help-screenshots',
            testDir: './e2e/help-screenshots',
            use: {
              ...devices['Desktop Chrome'],
              viewport: { width: 1280, height: 800 },
              storageState: 'e2e/.auth/user.json',
              trace: 'off' as const,
              screenshot: 'off' as const,
            },
            dependencies: ['setup'],
          },
        ]
      : []),
  ],
});
