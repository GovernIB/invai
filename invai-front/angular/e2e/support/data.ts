import type { TestInfo } from '@playwright/test';

export function uniqueToken(testInfo: TestInfo, maxLength = 8): string {
  const token = [
    Date.now().toString(36),
    testInfo.workerIndex.toString(36),
    testInfo.retry.toString(36),
  ]
    .join('')
    .toUpperCase();

  return token.slice(-maxLength);
}
