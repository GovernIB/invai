import { expect } from '@playwright/test';
import type { APIResponse, Page, Response } from '@playwright/test';

export function waitForApiResponse(
  page: Page,
  method: string,
  pathname: string,
  matchesUrl: (url: URL) => boolean = () => true,
): Promise<Response> {
  return page.waitForResponse(
    (response) => {
      const url = new URL(response.url());
      return (
        response.request().method() === method &&
        url.pathname === pathname &&
        matchesUrl(url)
      );
    },
    { timeout: 30_000 },
  );
}

export async function expectJsonResponse<T>(response: Response): Promise<T> {
  const body = await response.text();
  expect(
    response.ok(),
    `${response.request().method()} ${response.url()} returned ${response.status()}: ${body}`,
  ).toBeTruthy();

  return JSON.parse(body) as T;
}

export async function expectSuccessfulResponse(response: Response): Promise<void> {
  const body = await response.text();
  expect(
    response.ok(),
    `${response.request().method()} ${response.url()} returned ${response.status()}: ${body}`,
  ).toBeTruthy();
}

export async function expectApiJsonResponse<T>(
  response: APIResponse,
  request: string,
): Promise<T> {
  const body = await response.text();
  expect(response.ok(), `${request} returned ${response.status()}: ${body}`).toBeTruthy();

  return JSON.parse(body) as T;
}

export function apiUrl(page: Pick<Page, 'url'>, pathname: string): string {
  return new URL(pathname, page.url()).toString();
}
