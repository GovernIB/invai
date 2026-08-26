import type { Page } from '@playwright/test';

import { apiUrl, expectApiJsonResponse } from './http';

const APPLICATION_API_PATH = '/invaiapi/interna/application';
const ACTIVE_STATUS_ID = 1;
const INACTIVE_STATUS_ID = 2;
const PREFIX_RADIX = 36;
const PREFIX_LENGTH = 3;
const PREFIX_SPACE = PREFIX_RADIX ** PREFIX_LENGTH;

interface ApplicationPrefixPage {
  content: Array<{ prefix: string | null }>;
}

export async function findAvailableApplicationPrefix(
  page: Page,
  token: string,
): Promise<string> {
  const seed = Number.parseInt(token, PREFIX_RADIX) % PREFIX_SPACE;

  for (let offset = 0; offset < PREFIX_SPACE; offset += 1) {
    const prefix = ((seed + offset) % PREFIX_SPACE)
      .toString(PREFIX_RADIX)
      .padStart(PREFIX_LENGTH, '0')
      .toUpperCase();

    if (await isApplicationPrefixAvailable(page, prefix)) return prefix;
  }

  throw new Error('No application prefix is available for the E2E fixture.');
}

async function isApplicationPrefixAvailable(page: Page, prefix: string): Promise<boolean> {
  for (const statusId of [ACTIVE_STATUS_ID, INACTIVE_STATUS_ID]) {
    const response = await page.request.get(apiUrl(page, APPLICATION_API_PATH), {
      params: {
        page: 0,
        size: 1,
        prefix,
        statusId,
      },
    });
    const body = await expectApiJsonResponse<ApplicationPrefixPage>(
      response,
      `GET ${APPLICATION_API_PATH}?prefix=${prefix}&statusId=${statusId}`,
    );

    if (body.content.length > 0) return false;
  }

  return true;
}
