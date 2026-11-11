import { expect } from '@playwright/test';
import type { APIResponse, Page, Request, Response } from '@playwright/test';

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

export interface InterceptedRequests {
  /** Requests that received the simulated answer. */
  readonly hits: Request[];
}

// Answers the next `times` matching requests with an error and lets every other request reach the
// real back-end. Only for tests whose subject is that error answer.
export async function failApiRequest(
  page: Page,
  method: string,
  pathname: string | RegExp,
  response: { status: number; json?: unknown; body?: string },
  { times = 1, matchesUrl = () => true }: { times?: number; matchesUrl?: (url: URL) => boolean } = {},
): Promise<InterceptedRequests> {
  const intercepted: InterceptedRequests = { hits: [] };
  await page.route(
    (url) => matchesPath(url.pathname, pathname) && matchesUrl(url),
    async (route) => {
      const request = route.request();
      if (request.method() !== method || intercepted.hits.length >= times) {
        return route.fallback();
      }
      intercepted.hits.push(request);
      return route.fulfill(response);
    },
  );
  return intercepted;
}

function matchesPath(actual: string, expected: string | RegExp): boolean {
  return typeof expected === 'string' ? actual === expected : expected.test(actual);
}
