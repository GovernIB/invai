import type { Page, TestInfo } from '@playwright/test';

import type { DisposableApplication } from './application.fixture';
import type { CleanupRegistry } from './cleanup-registry';
import { uniqueToken } from './data';
import { apiUrl, expectApiJsonResponse } from './http';
import { registerSoftDeleteCleanup } from './systems';

export const DOCUMENTATION_API = '/invaiback/application/documentation';
const CATEGORIES_API = '/invaiback/documentation/category';

interface Entry {
  id: number;
  code: string;
  name: string;
}

export interface MimeType {
  id: number;
  extension: string;
  mimeType: string;
  name: string;
}

export interface SeededCategory {
  id: number;
  name: string;
}

interface DocumentOutput {
  id: number;
  attachments: Array<{ id: number; versionLabel: string; current: boolean }>;
}

export interface Documentation {
  statuses: Record<string, number>;
  mimeTypes: MimeType[];
  categories: {
    technical: SeededCategory;
    manuals: SeededCategory;
    api: SeededCategory;
    regulations: SeededCategory;
    protection: SeededCategory;
    impact: SeededCategory;
    activities: SeededCategory;
    procurement: SeededCategory;
  };
  /** Document identifiers by title. */
  documents: Record<string, number>;
}

type CategoryKey = keyof Documentation['categories'];

// Category of each seeded subcategory: a document filed under a subcategory is sent with both.
const PARENT_CATEGORIES: Partial<Record<CategoryKey, CategoryKey>> = {
  manuals: 'technical',
  api: 'technical',
  impact: 'protection',
  activities: 'protection',
};

// Title, file, category or subcategory, version and state of each document, as the fake had them.
const DOCUMENTS: [string, string, CategoryKey, string, string][] = [
  ['Manual API v2', 'manual-api-v2.pdf', 'manuals', '2.1', 'ACTIVE'],
  ["Guia d'instal·lació del programari", 'guia-installacio.docx', 'manuals', '2.1', 'IN_REVIEW'],
  ['Procediment de còpia de seguretat i recuperació', 'backup.pdf', 'manuals', '2.1', 'ACTIVE'],
  ['Manual de seguretat informàtica', 'manual-seguretat.pdf', 'manuals', '2.1', 'ACTIVE'],
  ['Documentació per als usuaris finals', 'usuaris-finals.xlsx', 'manuals', '2.1', 'OBSOLETE'],
  ["Protocol d'actualització de sistemes", 'protocol-actualitzacio.png', 'manuals', '2.1', 'ACTIVE'],
  ['Manual de configuració de xarxes', 'configuracio-xarxes.pdf', 'manuals', '2.1', 'ACTIVE'],
  ["Avaluació d'impacte", 'avaluacio-impacte.pdf', 'impact', '1.0', 'IN_REVIEW'],
  ["Registre d'activitats de tractament", 'registre-activitats.xlsx', 'activities', '3.2', 'ACTIVE'],
  ['Plec de prescripcions tècniques', 'plec-prescripcions.pdf', 'procurement', '1.0', 'OBSOLETE'],
  ["Declaració d'accessibilitat", 'declaracio-accessibilitat.pdf', 'regulations', '1.1', 'ACTIVE'],
  ['Diagrama de la infraestructura', 'infraestructura.png', 'technical', '4.0', 'ACTIVE'],
];

// Title, description, category and URL of each link; a link is filed under an active category.
const LINKS: [string, string, CategoryKey, string][] = [
  [
    'GitHub Actions',
    'Fluxos de CI/CD i automatització',
    'technical',
    'https://github.com/features/actions',
  ],
  ['Postman', "Proves i col·laboració d'API", 'technical', 'https://www.postman.com'],
  ['Swagger', "Documentació de l'API REST", 'technical', 'https://swagger.io'],
  ['SonarQube', 'Qualitat i seguretat del codi', 'regulations', 'https://www.sonarsource.com'],
  ['Jira', 'Seguiment de les incidències', 'regulations', 'https://www.atlassian.com/software/jira'],
  ['Grafana', 'Monitoratge dels entorns', 'technical', 'https://grafana.com'],
];

async function getJson<T>(page: Page, pathname: string): Promise<T> {
  return expectApiJsonResponse<T>(
    await page.request.get(apiUrl(page, pathname)),
    `GET ${pathname}`,
  );
}

export function fileOf(mimeTypes: MimeType[], name: string, content: string) {
  const extension = name.slice(name.lastIndexOf('.') + 1);
  const mimeType = mimeTypes.find((candidate) => candidate.extension === extension);
  if (!mimeType) throw new Error(`The back-end does not accept .${extension} files`);
  return { name, mimeType: mimeType.mimeType, buffer: Buffer.from(content) };
}

// Creates, for the application, the documents and links the tests work with, classified in
// document categories of their own. "Tècnica" (with the active subcategory "Manuals" and the
// inactive "API") and "Normativa" stay active; "Protecció de dades" (with "Avaluació d'impacte" and
// "Registre d'activitats") and "Contractació" are deactivated once their documents exist.
export async function seedDocumentation(
  page: Page,
  cleanup: CleanupRegistry,
  application: DisposableApplication,
  testInfo: TestInfo,
): Promise<Documentation> {
  const token = uniqueToken(testInfo);
  // Only the subcategories carry it, so they never collide within their category.
  const key = token.split('').reverse().join('').toLowerCase();
  const [statuses, mimeTypes] = await Promise.all([
    getJson<Entry[]>(page, '/invaiback/document-status'),
    getJson<MimeType[]>(page, '/invaiback/mime-type'),
  ]);
  const byCode = (entries: Entry[]) =>
    Object.fromEntries(entries.map(({ code, id }) => [code, id]));

  const createCategory = async (name: string, parentId: number | null = null) => {
    const created = await expectApiJsonResponse<SeededCategory>(
      await page.request.post(apiUrl(page, CATEGORIES_API), {
        data: { name, nameEs: `${name} ES`, parentId },
      }),
      `POST ${CATEGORIES_API} ${name}`,
    );
    registerSoftDeleteCleanup<SeededCategory & { deletedAt: string | null }>({
      page,
      cleanup,
      label: `document category ${name}`,
      pathname: CATEGORIES_API,
      target: { id: created.id, matches: (item) => item.id === created.id },
    });
    return { id: created.id, name };
  };
  const technical = await createCategory(`E2E ${token} Tècnica`);
  const regulations = await createCategory(`E2E ${token} Normativa`);
  const protection = await createCategory(`E2E ${token} Protecció de dades`);
  const procurement = await createCategory(`E2E ${token} Contractació`);
  const categories = {
    technical,
    regulations,
    protection,
    procurement,
    manuals: await createCategory(`Manuals ${key}`, technical.id),
    api: await createCategory(`API ${key}`, technical.id),
    impact: await createCategory(`Avaluació d'impacte ${key}`, protection.id),
    activities: await createCategory(`Registre d'activitats ${key}`, protection.id),
  };
  const classification = (key: CategoryKey) => {
    const parent = PARENT_CATEGORIES[key];
    return parent
      ? { categoryId: categories[parent].id, subcategoryId: categories[key].id }
      : { categoryId: categories[key].id, subcategoryId: null };
  };

  const documents: Record<string, number> = {};
  for (const [index, [title, fileName, category, versionLabel, status]] of DOCUMENTS.entries()) {
    // The first document starts at 2.0 and gets 2.1 as a second version.
    const firstLabel = index === 0 ? '2.0' : versionLabel;
    const created = await expectApiJsonResponse<DocumentOutput>(
      await page.request.post(apiUrl(page, DOCUMENTATION_API), {
        multipart: {
          data: {
            name: 'data.json',
            mimeType: 'application/json',
            buffer: Buffer.from(
              JSON.stringify({
                applicationId: application.id,
                title,
                ...classification(category),
                statusId: byCode(statuses)[status],
                versionLabel: firstLabel,
              }),
            ),
          },
          file: fileOf(mimeTypes, fileName, `${fileName} ${firstLabel}`),
        },
      }),
      `POST ${DOCUMENTATION_API} ${title}`,
    );
    documents[title] = created.id;
    if (index === 0) await addVersion(page, mimeTypes, created.id, versionLabel, fileName);
    // Removed before the categories, which cannot be deactivated while documents use them.
    cleanup.register(`document ${title}`, async () => {
      await page.request.delete(apiUrl(page, `${DOCUMENTATION_API}/${created.id}`));
    });
  }

  for (const category of [categories.api, categories.impact, categories.activities]) {
    await deactivateCategory(page, category);
  }
  await deactivateCategory(page, protection);
  await deactivateCategory(page, procurement);

  for (const [title, description, category, url] of LINKS) {
    await expectApiJsonResponse(
      await page.request.post(apiUrl(page, `${DOCUMENTATION_API}/link`), {
        data: {
          applicationId: application.id,
          title,
          description,
          categoryId: categories[category].id,
          url,
        },
      }),
      `POST ${DOCUMENTATION_API}/link ${title}`,
    );
  }

  return {
    statuses: byCode(statuses),
    mimeTypes,
    categories,
    documents,
  };
}

async function addVersion(
  page: Page,
  mimeTypes: MimeType[],
  documentId: number,
  versionLabel: string,
  fileName: string,
): Promise<void> {
  await expectApiJsonResponse(
    await page.request.post(apiUrl(page, `${DOCUMENTATION_API}/${documentId}`), {
      multipart: {
        data: {
          name: 'data.json',
          mimeType: 'application/json',
          buffer: Buffer.from(JSON.stringify({ versionLabel })),
        },
        file: fileOf(mimeTypes, fileName, `${fileName} ${versionLabel}`),
      },
    }),
    `POST ${DOCUMENTATION_API}/${documentId}`,
  );
}

async function deactivateCategory(page: Page, category: SeededCategory): Promise<void> {
  const response = await page.request.delete(apiUrl(page, `${CATEGORIES_API}/${category.id}`));
  if (!response.ok()) {
    throw new Error(
      `DELETE ${CATEGORIES_API}/${category.id} returned ${response.status()}: ${await response.text()}`,
    );
  }
}

export interface DocumentationWrite {
  method: string;
  path: string;
  data?: Record<string, unknown>;
}

// Records the writes of the tab. The browser does not expose the body of a multipart request, so
// only JSON writes carry their data: check what a multipart write saved in its response.
export function recordDocumentationWrites(page: Page): DocumentationWrite[] {
  const writes: DocumentationWrite[] = [];
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (request.method() === 'GET' || !url.pathname.startsWith(DOCUMENTATION_API)) return;
    const path = url.pathname.replace('/invaiback', '');
    const json = request.headers()['content-type']?.startsWith('application/json');
    writes.push(
      json
        ? { method: request.method(), path, data: request.postDataJSON() }
        : { method: request.method(), path },
    );
  });
  return writes;
}
