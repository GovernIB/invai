import type { Page } from '@playwright/test';

import type { DisposableApplication } from './application.fixture';
import { apiUrl, expectApiJsonResponse } from './http';

export const DATA_API = '/invaiback/application/data';
// The back-end checks each URL for real: the CAIB home page answers, while the default URLs derived
// from the code of a new application and the reserved example.* domains never do.
export const ANSWERING_URL = 'https://www.caib.es/';

export interface DataRecord {
  id: number;
  observation: string | null;
  openDataUrl: string;
  reuseUrl: string;
}

export interface DataRequest {
  method: string;
  path: string;
  body: unknown;
}

// Sets the Open Data URL and the observation of the data anchor of the application and reads it
// back with the URL of each source.
export async function prepareDataAnchor(
  page: Page,
  application: DisposableApplication,
  {
    openDataUrl = ANSWERING_URL,
    observation = '<p>Notes de dades obertes</p>',
  }: { openDataUrl?: string; observation?: string } = {},
): Promise<DataRecord> {
  const id = application.appDataId;
  if (!id) throw new Error(`Application ${application.code} has no data anchor`);
  const path = `${DATA_API}/${id}`;
  await expectApiJsonResponse(
    await page.request.put(apiUrl(page, `${path}/open-data`), { data: { url: openDataUrl } }),
    `PUT ${path}/open-data`,
  );
  await expectApiJsonResponse(
    await page.request.put(apiUrl(page, path), {
      data: { applicationId: application.id, observation },
    }),
    `PUT ${path}`,
  );
  return expectApiJsonResponse<DataRecord>(await page.request.get(apiUrl(page, path)), `GET ${path}`);
}

// Requests of the data tab: `checks` holds the URLs asked with check-url, `requests` every other
// read or write of the anchor, with its path relative to /invaiback/.
export function recordDataRequests(page: Page): { requests: DataRequest[]; checks: string[] } {
  const recorded = { requests: [] as DataRequest[], checks: [] as string[] };
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (!url.pathname.startsWith(DATA_API)) return;
    if (url.pathname === `${DATA_API}/check-url`) {
      recorded.checks.push(url.searchParams.get('url')!);
      return;
    }
    recorded.requests.push({
      method: request.method(),
      path: url.pathname.replace('/invaiback/', ''),
      body: request.method() === 'GET' ? null : request.postDataJSON(),
    });
  });
  return recorded;
}
