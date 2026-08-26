import type { Page } from '@playwright/test';

import { apiUrl, expectApiJsonResponse } from './http';

interface ApiCatalogPage<T> {
  content: T[];
  totalElements: number;
  totalPages?: number;
}

type RequestParam = string | number | boolean;

export interface FindApiCatalogItemOptions<T> {
  page: Page;
  pathname: string;
  matches: (item: T) => boolean;
  params?: Record<string, RequestParam>;
  pageSize?: number;
  maxPages?: number;
}

export interface EnsuredApiPrerequisite<T> {
  item: T;
  created: boolean;
}

export async function findApiCatalogItem<T>({
  page,
  pathname,
  matches,
  params = {},
  pageSize = 100,
  maxPages = 100,
}: FindApiCatalogItemOptions<T>): Promise<T | undefined> {
  for (let pageIndex = 0; pageIndex < maxPages; pageIndex += 1) {
    const response = await page.request.get(apiUrl(page, pathname), {
      params: { ...params, page: pageIndex, size: pageSize },
    });
    const body = await expectApiJsonResponse<ApiCatalogPage<T>>(
      response,
      `GET ${pathname} page ${pageIndex}`,
    );
    const item = body.content.find(matches);
    if (item) return item;

    const totalPages = body.totalPages ?? Math.ceil(body.totalElements / pageSize);
    if (pageIndex + 1 >= totalPages) return undefined;
  }

  throw new Error(`GET ${pathname} exceeded the ${maxPages}-page E2E safety limit`);
}

export async function ensureApiPrerequisite<T>(
  options: FindApiCatalogItemOptions<T> & { create: () => Promise<T> },
): Promise<EnsuredApiPrerequisite<T>> {
  const existing = await findApiCatalogItem(options);
  if (existing) return { item: existing, created: false };

  return { item: await options.create(), created: true };
}
