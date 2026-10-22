import AxeBuilder from '@axe-core/playwright';
import { expect, test, type Page, type Request, type Route } from '@playwright/test';

import { selectOption } from './support/ui';

test.use({ storageState: { cookies: [], origins: [] } });

// The documents and the links of the applications come from fakes of application/documentation,
// application/documentation/link and their catalogs that keep their changes during the test.
const CATEGORIES = [
  {
    id: 1,
    name: 'Tècnica',
    nameEs: 'Técnica',
    parentId: null,
    deletedAt: null,
    subcategories: [
      { id: 11, name: 'Manuals', nameEs: 'Manuales', parentId: 1, deletedAt: null, subcategories: [] },
      { id: 12, name: 'API', nameEs: 'API', parentId: 1, deletedAt: '2026-10-01T10:00:00', subcategories: [] },
    ],
  },
  { id: 2, name: 'Normativa', nameEs: 'Normativa', parentId: null, deletedAt: null, subcategories: [] },
];

interface Entry {
  id: number;
  code?: string;
  name: string;
  nameEs: string;
  parentId?: number | null;
}

const SECTIONS: Entry[] = [
  { id: 1, code: 'GENERAL', name: 'General', nameEs: 'General' },
  { id: 2, code: 'RESPONSIBLES', name: 'Responsables', nameEs: 'Responsables' },
  { id: 3, code: 'SYSTEMS', name: 'Sistemes', nameEs: 'Sistemas' },
  { id: 4, code: 'DATABASE', name: 'Base de dades', nameEs: 'Base de datos' },
  { id: 5, code: 'DEVELOPMENT', name: 'Desenvolupament', nameEs: 'Desarrollo' },
  { id: 6, code: 'ACCESSIBILITY', name: 'Accessibilitat', nameEs: 'Accesibilidad' },
  { id: 7, code: 'SECURITY', name: 'Seguretat', nameEs: 'Seguridad' },
  { id: 8, code: 'DATA', name: 'Dades', nameEs: 'Datos' },
  { id: 9, code: 'INTEGRATIONS', name: 'Integracions', nameEs: 'Integraciones' },
];

const STATUSES: Entry[] = [
  { id: 1, code: 'ACTIVE', name: 'Vigent', nameEs: 'Vigente' },
  { id: 2, code: 'IN_REVIEW', name: 'En revisió', nameEs: 'En revisión' },
  { id: 3, code: 'OBSOLETE', name: 'Obsolet', nameEs: 'Obsoleto' },
];

const MIME_TYPES = [
  { id: 1, extension: 'pdf', mimeType: 'application/pdf', name: 'PDF' },
  { id: 2, extension: 'docx', mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', name: 'Word' },
  { id: 3, extension: 'xlsx', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', name: 'Excel' },
  { id: 4, extension: 'txt', mimeType: 'text/plain', name: 'Text' },
  { id: 5, extension: 'png', mimeType: 'image/png', name: 'PNG' },
];

// Every category a seeded document uses, active or not.
const DOCUMENT_CATEGORIES: Entry[] = [
  { id: 1, name: 'Tècnica', nameEs: 'Técnica', parentId: null },
  { id: 11, name: 'Manuals', nameEs: 'Manuales', parentId: 1 },
  { id: 2, name: 'Normativa', nameEs: 'Normativa', parentId: null },
  { id: 3, name: 'Protecció de dades', nameEs: 'Protección de datos', parentId: null },
  { id: 31, name: "Avaluació d'impacte", nameEs: 'Evaluación de impacto', parentId: 3 },
  { id: 32, name: "Registre d'activitats de tractament", nameEs: 'Registro de actividades de tratamiento', parentId: 3 },
  { id: 4, name: 'Contractació', nameEs: 'Contratación', parentId: null },
];

const SEED: [string, string, string, number, string, string][] = [
  ['Manual API v2', 'GENERAL', 'manual-api-v2.pdf', 11, '2.1', 'ACTIVE'],
  ["Guia d'instal·lació del programari", 'INTEGRATIONS', 'guia-installacio.docx', 11, '2.1', 'IN_REVIEW'],
  ['Procediment de còpia de seguretat i recuperació', 'DEVELOPMENT', 'backup.pdf', 11, '2.1', 'ACTIVE'],
  ['Manual de seguretat informàtica', 'INTEGRATIONS', 'manual-seguretat.pdf', 11, '2.1', 'ACTIVE'],
  ['Documentació per als usuaris finals', 'DEVELOPMENT', 'usuaris-finals.xlsx', 11, '2.1', 'OBSOLETE'],
  ["Protocol d'actualització de sistemes", 'RESPONSIBLES', 'protocol-actualitzacio.png', 11, '2.1', 'ACTIVE'],
  ['Manual de configuració de xarxes', 'SECURITY', 'configuracio-xarxes.pdf', 11, '2.1', 'ACTIVE'],
  ["Avaluació d'impacte", 'SECURITY', 'avaluacio-impacte.pdf', 31, '1.0', 'IN_REVIEW'],
  ["Registre d'activitats de tractament", 'DATA', 'registre-activitats.xlsx', 32, '3.2', 'ACTIVE'],
  ['Plec de prescripcions tècniques', 'GENERAL', 'plec-prescripcions.pdf', 4, '1.0', 'OBSOLETE'],
  ["Declaració d'accessibilitat", 'ACCESSIBILITY', 'declaracio-accessibilitat.pdf', 2, '1.1', 'ACTIVE'],
  ['Diagrama de la infraestructura', 'SYSTEMS', 'infraestructura.png', 1, '4.0', 'ACTIVE'],
];

interface Version {
  id: number;
  versionLabel: string;
  fileName: string;
  mimeType: { id: number; name: string };
  fileSize: number;
  uploadedAt: string;
  uploadedBy: string;
  current: boolean;
  changeNote: string | null;
  metadata: never[];
}

interface StoredDocument {
  id: number;
  applicationId: number;
  title: string;
  section: Entry;
  categoryId: number;
  status: Entry;
  deletedAt: string | null;
  attachments: Version[];
}

class DocumentationBackend {
  readonly requests: { method: string; path: string; data?: Record<string, unknown> }[] = [];
  private readonly documents: StoredDocument[];
  // Content of every version file, by version ID.
  private readonly files = new Map<number, Buffer>();
  private nextId = 100;

  constructor() {
    this.documents = SEED.map(([title, section, fileName, categoryId, versionLabel, status], index) => ({
      id: index + 1,
      applicationId: 1,
      title,
      section: SECTIONS.find(({ code }) => code === section)!,
      categoryId,
      status: STATUSES.find(({ code }) => code === status)!,
      deletedAt: null,
      attachments: [
        this.version(fileName, versionLabel, '2025-11-18T10:00:00', Buffer.from(`${fileName} ${versionLabel}`)),
        { ...this.version(fileName, '2.0', '2025-09-03T10:00:00', Buffer.from(`${fileName} 2.0`)), current: false },
      ].slice(0, index === 0 ? 2 : 1),
    }));
  }

  async handle(route: Route, path: string, url: URL): Promise<void> {
    const request = route.request();
    const method = request.method();
    const [, id, attachment, attachmentId, action] =
      /^\/application\/documentation(?:\/(\d+)(?:\/(attachment)\/(\d+)\/(download|view))?)?$/.exec(path) ?? [];
    const document = id ? this.documents.find((item) => item.id === Number(id)) : undefined;

    if (attachment) {
      const version = document?.attachments.find((item) => item.id === Number(attachmentId));
      if (!document || !version) return route.fulfill({ status: 400, json: { error: 'Bad Request', message: 'No trobat' } });
      const mimeType = MIME_TYPES.find(({ extension }) => version.fileName.endsWith('.' + extension))!.mimeType;
      return route.fulfill({
        body: this.files.get(version.id),
        contentType: mimeType,
        headers: {
          'Content-Disposition': `${action === 'view' ? 'inline' : 'attachment'}; filename*=UTF-8''${encodeURIComponent(version.fileName)}`,
        },
      });
    }
    if (!id && method === 'GET') return route.fulfill({ json: this.page(url) });
    if (!id && method === 'POST') {
      const { data, file } = parseMultipart(request);
      this.requests.push({ method, path, data });
      const created: StoredDocument = {
        id: this.nextId++,
        applicationId: data['applicationId'] as number,
        title: data['title'] as string,
        section: SECTIONS.find(({ id }) => id === data['sectionId'])!,
        categoryId: data['categoryId'] as number,
        status: STATUSES.find(({ id }) => id === data['statusId'])!,
        deletedAt: null,
        attachments: [this.version(file.name, data['versionLabel'] as string, new Date().toISOString(), file.content)],
      };
      this.documents.push(created);
      return route.fulfill({ status: 201, json: this.detail(created) });
    }
    if (!document) return route.fulfill({ status: 400, json: { error: 'Bad Request', message: 'No trobat' } });
    if (method === 'GET') return route.fulfill({ json: this.detail(document) });
    if (method === 'PUT') {
      const data = request.postDataJSON() as Record<string, number | string>;
      this.requests.push({ method, path, data });
      Object.assign(document, {
        title: data['title'],
        section: SECTIONS.find(({ id }) => id === data['sectionId']),
        categoryId: data['categoryId'],
        status: STATUSES.find(({ id }) => id === data['statusId']),
      });
      return route.fulfill({ json: this.detail(document) });
    }
    if (method === 'POST') {
      const { data, file } = parseMultipart(request);
      this.requests.push({ method, path, data });
      document.attachments.forEach((version) => (version.current = false));
      document.attachments.unshift(
        this.version(file.name, data['versionLabel'] as string, new Date().toISOString(), file.content),
      );
      return route.fulfill({ status: 201, json: this.detail(document) });
    }
    if (method === 'DELETE') {
      this.requests.push({ method, path });
      document.deletedAt = new Date().toISOString();
      return route.fulfill({ status: 204 });
    }
    return route.fulfill({ status: 405 });
  }

  private version(fileName: string, versionLabel: string, uploadedAt: string, content: Buffer): Version {
    const mimeType = MIME_TYPES.find(({ extension }) => fileName.endsWith('.' + extension))!;
    const id = this.nextId++;
    this.files.set(id, content);
    return {
      id,
      versionLabel,
      fileName,
      mimeType: { id: mimeType.id, name: mimeType.name },
      fileSize: content.length,
      uploadedAt,
      uploadedBy: 'jgarcia@gov.com',
      current: true,
      changeNote: null,
      metadata: [],
    };
  }

  private categories(document: StoredDocument): { category: Entry; subcategory: Entry | null } {
    const stored = DOCUMENT_CATEGORIES.find(({ id }) => id === document.categoryId)!;
    const parent = DOCUMENT_CATEGORIES.find(({ id }) => id === stored.parentId);
    return parent ? { category: parent, subcategory: stored } : { category: stored, subcategory: null };
  }

  private detail(document: StoredDocument) {
    const { id, applicationId, title, section, status, deletedAt, attachments } = document;
    return { id, applicationId, title, section, ...this.categories(document), status, deletedAt, attachments };
  }

  // Filters like the back-end: the search matches the title only, without ignoring accents; a
  // category also matches the documents of its subcategories; the format is that of the current
  // version.
  private page(url: URL) {
    const params = url.searchParams;
    const search = params.get('search')?.toLowerCase();
    const categoryId = params.has('categoryId') ? Number(params.get('categoryId')) : null;
    const label = (entry: Entry | null) => entry?.name ?? '';
    const current = (item: StoredDocument) => item.attachments.find((version) => version.current)!;
    const rows = this.documents
      .filter((item) => item.applicationId === Number(params.get('applicationId')))
      .filter((item) => params.get('deleted') !== 'false' || !item.deletedAt)
      .filter((item) => !search || item.title.toLowerCase().includes(search))
      .filter((item) => !params.has('sectionId') || item.section.id === Number(params.get('sectionId')))
      .filter((item) => !params.has('statusId') || item.status.id === Number(params.get('statusId')))
      .filter((item) => !params.has('mimeTypeId') || current(item).mimeType.id === Number(params.get('mimeTypeId')))
      .filter((item) => {
        const stored = DOCUMENT_CATEGORIES.find(({ id }) => id === item.categoryId)!;
        return categoryId == null || stored.id === categoryId || stored.parentId === categoryId;
      })
      .map((item) => {
        const version = current(item);
        return {
          id: item.id,
          title: item.title,
          section: item.section,
          mimeType: version.mimeType,
          ...this.categories(item),
          versionLabel: version.versionLabel,
          currentAttachmentId: version.id,
          status: item.status,
          deletedAt: item.deletedAt,
        };
      });
    const [field = 'title', direction] = (params.get('sort') ?? 'title').split(',');
    const value = (row: (typeof rows)[number]): string =>
      field === 'section' || field === 'status' || field === 'category' || field === 'subcategory'
        ? label(row[field])
        : field === 'mimeType'
          ? row.mimeType.name
          : String(row[field as 'title' | 'versionLabel']);
    rows.sort((left, right) => (direction === 'desc' ? -1 : 1) * value(left).localeCompare(value(right), 'ca'));
    const size = Number(params.get('size') ?? 10);
    const number = Number(params.get('page') ?? 0);
    const content = rows.slice(number * size, (number + 1) * size);
    const totalPages = Math.max(1, Math.ceil(rows.length / size));
    return { content, totalElements: rows.length, totalPages, number, size, last: number >= totalPages - 1 };
  }
}

interface StoredLink {
  id: number;
  applicationId: number;
  title: string;
  description: string | null;
  section: Entry;
  url: string;
  deletedAt: string | null;
}

const LINK_SEED: [string, string, string, string][] = [
  ['GitHub Actions', 'Fluxos de CI/CD i automatització', 'GENERAL', 'https://github.com/features/actions'],
  ['Postman', "Proves i col·laboració d'API", 'DEVELOPMENT', 'https://www.postman.com'],
  ['Swagger', "Documentació de l'API REST", 'INTEGRATIONS', 'https://swagger.io'],
  ['SonarQube', 'Qualitat i seguretat del codi', 'SECURITY', 'https://www.sonarsource.com'],
  ['Jira', 'Seguiment de les incidències', 'GENERAL', 'https://www.atlassian.com/software/jira'],
  ['Grafana', 'Monitoratge dels entorns', 'SYSTEMS', 'https://grafana.com'],
];

class LinksBackend {
  private readonly links: StoredLink[] = LINK_SEED.map(([title, description, section, url], index) => ({
    id: index + 1,
    applicationId: 1,
    title,
    description,
    section: SECTIONS.find(({ code }) => code === section)!,
    url,
    deletedAt: null,
  }));
  private nextId = 100;

  async handle(route: Route, path: string, url: URL): Promise<void> {
    const request = route.request();
    const method = request.method();
    const id = /^\/application\/documentation\/link(?:\/(\d+))?$/.exec(path)?.[1];
    if (!id && method === 'GET') return route.fulfill({ json: this.page(url) });
    const data = method === 'DELETE' ? undefined : (request.postDataJSON() as Record<string, unknown>);
    if (!id && method === 'POST') {
      const created: StoredLink = {
        id: this.nextId++,
        applicationId: data!['applicationId'] as number,
        deletedAt: null,
        ...this.fields(data!),
      };
      this.links.push(created);
      return route.fulfill({ status: 201, json: this.output(created) });
    }
    const link = this.links.find((item) => item.id === Number(id) && !item.deletedAt);
    if (!link) return route.fulfill({ status: 400, json: { error: 'Bad Request', message: 'No trobat' } });
    if (method === 'PUT') {
      Object.assign(link, this.fields(data!));
      return route.fulfill({ json: this.output(link) });
    }
    if (method === 'DELETE') {
      link.deletedAt = new Date().toISOString();
      return route.fulfill({ status: 204 });
    }
    return route.fulfill({ status: 405 });
  }

  private fields(data: Record<string, unknown>): Pick<StoredLink, 'title' | 'description' | 'section' | 'url'> {
    return {
      title: data['title'] as string,
      description: (data['description'] as string | null) ?? null,
      section: SECTIONS.find(({ id }) => id === data['sectionId'])!,
      url: data['url'] as string,
    };
  }

  // The back-end does not send the application of a link.
  private output({ id, title, description, section, url, deletedAt }: StoredLink) {
    return { id, title, description, section, url, deletedAt };
  }

  // Filters like the back-end: the quick search also matches the name of the section.
  private page(url: URL) {
    const params = url.searchParams;
    const search = params.get('quickSearch')?.toLowerCase();
    const rows = this.links
      .filter((item) => item.applicationId === Number(params.get('applicationId')))
      .filter((item) => params.get('deleted') !== 'false' || !item.deletedAt)
      .filter((item) => !params.has('sectionId') || item.section.id === Number(params.get('sectionId')))
      .filter((item) =>
        !search ||
        [item.title, item.description ?? '', item.url, item.section.name].some((text) =>
          text.toLowerCase().includes(search),
        ),
      )
      .map((item) => this.output(item));
    const [field = 'title', direction] = (params.get('sort') ?? 'title').split(',');
    const value = (row: (typeof rows)[number]): string =>
      field === 'section' ? row.section.name : String(row[field as 'title' | 'description' | 'url'] ?? '');
    rows.sort((left, right) => (direction === 'desc' ? -1 : 1) * value(left).localeCompare(value(right), 'ca'));
    const size = Number(params.get('size') ?? 10);
    const number = Number(params.get('page') ?? 0);
    const content = rows.slice(number * size, (number + 1) * size);
    const totalPages = Math.max(1, Math.ceil(rows.length / size));
    return { content, totalElements: rows.length, totalPages, number, size, last: number >= totalPages - 1 };
  }
}

// Reads the `data` JSON part and the `file` part of a multipart request.
function parseMultipart(request: Request): {
  data: Record<string, unknown>;
  file: { name: string; content: Buffer };
} {
  const boundary = /boundary=(.+)$/.exec(request.headers()['content-type'])![1];
  const parts = request.postDataBuffer()!.toString('latin1').split('--' + boundary).slice(1, -1);
  let data: Record<string, unknown> = {};
  let file = { name: '', content: Buffer.alloc(0) };
  for (const part of parts) {
    const separator = part.indexOf('\r\n\r\n');
    const head = part.slice(0, separator);
    const content = Buffer.from(part.slice(separator + 4, -2), 'latin1');
    const name = /name="([^"]+)"/.exec(head)![1];
    if (name === 'data') {
      expect(head).toMatch(/Content-Type: application\/json/i);
      data = JSON.parse(content.toString('utf8'));
    } else {
      file = { name: /filename="([^"]*)"/.exec(head)![1], content };
    }
  }
  return { data, file };
}

async function openDocumentationTab(
  page: Page,
  status = 'ACTIVE',
  { mimeTypesAvailable = true } = {},
): Promise<DocumentationBackend> {
  const backend = new DocumentationBackend();
  const linksBackend = new LinksBackend();
  await page.route('**/invaiback/**', async (route: Route) => {
    const url = new URL(route.request().url());
    const path = url.pathname.replace(/^.*\/invaiback/, '');
    if (path === '/auth/me') {
      return route.fulfill({ json: { authenticated: true, username: 'documentation-test' } });
    }
    if (path.startsWith('/application/documentation/link')) return linksBackend.handle(route, path, url);
    if (path.startsWith('/application/documentation')) return backend.handle(route, path, url);
    if (path === '/application/1') {
      return route.fulfill({ json: { id: 1, code: 'DOCS', name: 'Documentació test', status } });
    }
    if (path === '/documentation/category') {
      return route.fulfill({
        json: { content: CATEGORIES, totalElements: 2, totalPages: 1, last: true, number: 0 },
      });
    }
    if (path === '/app-section') return route.fulfill({ json: SECTIONS });
    if (path === '/document-status') return route.fulfill({ json: STATUSES });
    if (path === '/mime-type') {
      return mimeTypesAvailable ? route.fulfill({ json: MIME_TYPES }) : route.fulfill({ status: 500 });
    }
    return route.fulfill({
      json: { content: [], totalElements: 0, totalPages: 1, last: true, number: 0 },
    });
  });
  await page.goto('/aplicacions/1/documentation');
  await expect(page.getByRole('heading', { level: 2, name: 'Documentació' })).toBeVisible();
  return backend;
}

const attachments = (page: Page) => page.getByRole('table', { name: "Adjunts de l'aplicació" });
const links = (page: Page) => page.getByRole('table', { name: "Enllaços de l'aplicació" });

async function rowAction(page: Page, kind: "l'adjunt" | "l'enllaç", title: string, action: string) {
  await page.getByRole('button', { name: `Accions de ${kind} «${title}»` }).click();
  await page.getByRole('menuitem', { name: action, exact: true }).click();
}

test('shows the attachments and links of an application in its Documentació tab', async ({
  page,
}, testInfo) => {
  await openDocumentationTab(page);

  const tab = page.locator('.application-detail-tabs').getByRole('link', { name: 'Documentació' });
  await expect(tab).toHaveAttribute('aria-current', 'page');
  await expect(page.getByRole('heading', { level: 3, name: 'Adjunts' })).toBeVisible();
  await expect(page.getByRole('heading', { level: 3, name: 'Enllaços' })).toBeVisible();

  await expect(attachments(page).locator('thead th')).toHaveText([
    'Títol',
    'Secció',
    'Tipus doc.',
    'Categoria',
    'Subcategoria',
    'Versió',
    'Estat',
    'Accions',
  ]);
  await expect(attachments(page).locator('tbody tr')).toHaveCount(10);
  const manual = attachments(page).getByRole('row', { name: /Manual API v2/ });
  await expect(manual).toContainText('General');
  await expect(manual).toContainText('PDF');
  await expect(manual).toContainText('Manuals');
  await expect(manual).toContainText('Vigent');
  await expect(page.getByText('Mostrant 1 a 10 de 12 registres')).toBeVisible();

  await expect(links(page).locator('tbody tr')).toHaveCount(6);
  const postman = links(page).getByRole('link', { name: "https://www.postman.com (s'obre en una pestanya nova)" });
  await expect(postman).toHaveAttribute('target', '_blank');

  await page.screenshot({ path: testInfo.outputPath('application-documentation.png'), fullPage: true });
  const axe = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(axe.violations.map(({ id, nodes }) => `${id}: ${nodes.map(({ target }) => target).join(', ')}`)).toEqual([]);
});

test('searches by title, filters by state and sorts the attachments', async ({ page }) => {
  await openDocumentationTab(page);

  await expect(page.getByRole('textbox', { name: "Cerca ràpida d'adjunts" })).toHaveAttribute('placeholder', 'Cerca per títol');
  await page.getByRole('textbox', { name: "Cerca ràpida d'adjunts" }).fill('manual');
  await expect(attachments(page).locator('tbody tr')).toHaveCount(3);
  await page.getByRole('textbox', { name: "Cerca ràpida d'adjunts" }).fill('');
  await expect(attachments(page).locator('tbody tr')).toHaveCount(10);

  await page.getByRole('button', { name: /Filtres dels adjunts/ }).click();
  await selectOption(page, 'application-attachments-filter-state', 'Obsolet');
  await page.getByRole('button', { name: /^\W*Cerca$/ }).click();
  await expect(attachments(page).locator('tbody tr')).toHaveCount(2);

  await attachments(page).getByRole('columnheader', { name: /Títol/ }).click();
  await expect(attachments(page).locator('tbody tr').first()).toContainText('Documentació per als usuaris finals');
  await attachments(page).getByRole('columnheader', { name: /Versió/ }).click();
  await expect(attachments(page).locator('tbody tr').first()).toContainText('1.0');
});

test('filters the attachments by type of document, category and subcategory', async ({ page }) => {
  await openDocumentationTab(page);
  const filters = page.getByRole('button', { name: /Filtres dels adjunts/ });
  const search = page.getByRole('button', { name: /^\W*Cerca$/ });
  const rows = attachments(page).locator('tbody tr');
  const subcategory = page.locator('#application-attachments-filter-subcategory');

  await filters.click();
  await expect(subcategory).toHaveAttribute('aria-disabled', 'true');

  // A category also lists the documents of its subcategories.
  await selectOption(page, 'application-attachments-filter-category', 'Tècnica');
  await search.click();
  await expect(rows).toHaveCount(8);

  await selectOption(page, 'application-attachments-filter-subcategory', 'Manuals');
  await selectOption(page, 'application-attachments-filter-type', 'PDF (.pdf)');
  await search.click();
  await expect(rows).toHaveCount(4);
  await expect(filters).toHaveAccessibleName(/3 filtres aplicats/);
  const axe = await new AxeBuilder({ page })
    .include('.application-documentation-list')
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(axe.violations.map(({ id, nodes }) => `${id}: ${nodes.map(({ target }) => target).join(', ')}`)).toEqual([]);

  // A category without subcategories leaves none to choose.
  await selectOption(page, 'application-attachments-filter-category', 'Normativa');
  await expect(subcategory).toHaveAttribute('aria-disabled', 'true');
  await search.click();
  await expect(rows).toHaveCount(1);
  await expect(rows.first()).toContainText("Declaració d'accessibilitat");

  await page.getByRole('button', { name: /Neteja/ }).click();
  await expect(rows).toHaveCount(10);
  await expect(subcategory).toHaveAttribute('aria-disabled', 'true');
});

test('adds an attachment classified in an active category', async ({ page }, testInfo) => {
  const backend = await openDocumentationTab(page);

  await page.getByRole('button', { name: 'Afegeix un adjunt' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('heading', { name: 'Afegeix un adjunt' })).toBeVisible();
  await expect(dialog.getByText('Tots els camps són obligatoris, excepte els indicats com a opcionals.')).toBeVisible();
  await expect(dialog.getByText('Arrossega el fitxer aquí per carregar-lo.')).toBeVisible();
  await expect(dialog.getByText('Mida màxima: 5 MB.')).toBeVisible();
  await expect(page.locator('#application-attachment-file')).toHaveAttribute('accept', '.pdf,.docx,.xlsx,.txt,.png');
  await page.screenshot({ path: testInfo.outputPath('application-attachment-empty.png') });
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Camp obligatori')).toHaveCount(5);

  await dialog.getByLabel('Títol', { exact: true }).fill('Pla de proves');
  await dialog.getByLabel('Versió', { exact: true }).fill('1.0');
  const upload = dialog.locator('.application-attachment-upload');
  const drop = async (name: string, type: string) => {
    const dataTransfer = await page.evaluateHandle(
      ([fileName, fileType]) => {
        const transfer = new DataTransfer();
        transfer.items.add(new File(['mock'], fileName, { type: fileType }));
        return transfer;
      },
      [name, type],
    );
    await upload.dispatchEvent('dragover', { dataTransfer });
    await expect(upload).toHaveClass(/application-attachment-upload--dragging/);
    await upload.dispatchEvent('drop', { dataTransfer });
    await dataTransfer.dispose();
  };
  await drop('provisional.pdf', 'application/pdf');
  await expect(dialog.getByRole('status')).toContainText('provisional.pdf');
  // Dropping ignores the filter of the picker: a file of a format not accepted is refused and the
  // chosen one is kept.
  await drop('provisional.exe', 'application/octet-stream');
  await expect(page.locator('#application-attachment-file-error')).toHaveText(
    "No s'ha adjuntat «provisional.exe»: el format del fitxer no està admès.",
  );
  await expect(dialog.getByRole('status')).toContainText('provisional.pdf');
  await dialog.getByRole('button', { name: 'Elimina el fitxer seleccionat' }).click();
  await expect(page.locator('#application-attachment-file-error')).toHaveText('Camp obligatori');
  await expect(dialog.getByRole('button', { name: 'Adjunta', exact: true })).toBeFocused();
  const [fileChooser] = await Promise.all([
    page.waitForEvent('filechooser'),
    page.keyboard.press('Enter'),
  ]);
  await fileChooser.setFiles({
    name: 'pla-de-proves.docx',
    mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    buffer: Buffer.from('mock'),
  });
  await expect(dialog.getByRole('status')).toContainText('pla-de-proves.docx');
  await expect(dialog.getByRole('status')).toContainText('Tipus de document: Word');
  await selectOption(page, 'application-attachment-section', 'Desenvolupament');
  await selectOption(page, 'application-attachment-category', 'Tècnica');
  await page.locator('#application-attachment-subcategory').click();
  // Only the active subcategories of the chosen category are offered.
  await expect(page.getByRole('option')).toHaveText(['Manuals']);
  await page.getByRole('option', { name: 'Manuals' }).click();
  await page.screenshot({ path: testInfo.outputPath('application-attachment-dialog.png') });
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();

  await expect(dialog).toHaveCount(0);
  expect(backend.requests).toContainEqual({
    method: 'POST',
    path: '/application/documentation',
    data: { applicationId: 1, title: 'Pla de proves', sectionId: 5, categoryId: 11, statusId: 1, versionLabel: '1.0' },
  });
  await page.getByRole('textbox', { name: "Cerca ràpida d'adjunts" }).fill('pla de proves');
  const added = attachments(page).getByRole('row', { name: /Pla de proves/ });
  await expect(added).toContainText('Desenvolupament');
  await expect(added).toContainText('Word');
  await expect(added).toContainText('Manuals');
  await expect(added).toContainText('Vigent');
});

test('reflows the new attachment design and preserves keyboard access and focus return', async ({ page }, testInfo) => {
  await openDocumentationTab(page);
  const open = page.getByRole('button', { name: 'Afegeix un adjunt' });
  await open.click();
  const dialog = page.getByRole('dialog');

  for (const width of [1440, 768, 320]) {
    await page.setViewportSize({ width, height: 900 });
    const title = await dialog.getByLabel('Títol', { exact: true }).boundingBox();
    const section = await page.locator('#application-attachment-section').boundingBox();
    const category = await page.locator('#application-attachment-category').boundingBox();
    const subcategory = await page.locator('#application-attachment-subcategory').boundingBox();
    expect(title).not.toBeNull();
    expect(section).not.toBeNull();
    expect(category).not.toBeNull();
    expect(subcategory).not.toBeNull();
    if (width >= 640) {
      expect(Math.abs(title!.y - section!.y)).toBeLessThan(3);
      expect(Math.abs(category!.y - subcategory!.y)).toBeLessThan(3);
      expect(section!.x).toBeGreaterThan(title!.x + title!.width);
    } else {
      expect(section!.y).toBeGreaterThan(title!.y + title!.height);
      expect(subcategory!.y).toBeGreaterThan(category!.y + category!.height);
    }
    expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth + 1)).toBe(true);
    await page.screenshot({ path: testInfo.outputPath('attachment-design-' + width + '.png') });
  }

  await dialog.getByLabel('Títol', { exact: true }).focus();
  for (const field of ['section', 'category', 'subcategory', 'version']) {
    await page.keyboard.press('Tab');
    await expect(page.locator('#application-attachment-' + field)).toBeFocused();
  }
  await page.keyboard.press('Tab');
  await expect(dialog.getByRole('button', { name: 'Adjunta', exact: true })).toBeFocused();
  const axe = await new AxeBuilder({ page })
    .include('[role="dialog"]')
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(axe.violations.map(({ id, nodes }) => id + ': ' + nodes.map(({ target }) => target).join(', '))).toEqual([]);
  await page.addStyleTag({ content: 'html { font-size: 200%; }' });
  expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth + 1)).toBe(true);
  const overflow = await dialog.locator('.p-dialog-content').evaluate((element) => ({
    clientWidth: element.clientWidth,
    scrollWidth: element.scrollWidth,
    overflowing: [...element.querySelectorAll<HTMLElement>('*')]
      .filter((child) => child.getBoundingClientRect().right > element.getBoundingClientRect().right)
      .map((child) => ({ tag: child.tagName, className: child.className, width: child.getBoundingClientRect().width })),
  }));
  expect(overflow, JSON.stringify(overflow)).toMatchObject({ scrollWidth: overflow.clientWidth });
  await page.screenshot({ path: testInfo.outputPath('attachment-design-320-text-200.png') });
  await page.keyboard.press('Escape');
  await expect(dialog).toHaveCount(0);
  await expect(open).toBeFocused();
});

test('opens an attachment on its own page, downloads it and deletes it from the list', async ({
  page,
}) => {
  const backend = await openDocumentationTab(page);

  await rowAction(page, "l'adjunt", "Avaluació d'impacte", 'Consulta');
  await expect(page).toHaveURL(/\/aplicacions\/1\/documentation\/8$/);
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(page.getByLabel('Títol', { exact: true })).toHaveValue("Avaluació d'impacte");
  await expect(page.getByRole('table', { name: 'Versions del document' })).toBeVisible();
  // Protecció de dades is not an active category, but stays shown as the one of the document.
  await expect(page.locator('#document-detail-category-value')).toContainText('Protecció de dades');
  await expect(page.locator('#document-detail-subcategory-value')).toContainText("Avaluació d'impacte");
  await expect(page.locator('#document-detail-state-value')).toContainText('En revisió');
  await page.getByRole('button', { name: 'Tornar', exact: true }).click();

  const downloadPromise = page.waitForEvent('download');
  await rowAction(page, "l'adjunt", "Avaluació d'impacte", 'Descarrega');
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe('avaluacio-impacte.pdf');

  await rowAction(page, "l'adjunt", "Avaluació d'impacte", 'Elimina');
  await page.getByRole('button', { name: "Confirma l'eliminació de l'adjunt" }).click();
  await expect(attachments(page).getByRole('row', { name: /Avaluació d'impacte/ })).toHaveCount(0);
  expect(backend.requests).toContainEqual({ method: 'DELETE', path: '/application/documentation/8' });
});

test('saves the document, adds versions and opens and downloads their files', async ({ page, context }, testInfo) => {
  test.setTimeout(60_000);
  await page.setViewportSize({ width: 1440, height: 1000 });
  const backend = await openDocumentationTab(page);
  const row = attachments(page).getByRole('row', { name: /Manual API v2/ });
  await row.focus();
  await page.keyboard.press('Enter');
  await expect(page).toHaveURL(/\/documentation\/1$/);
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(page).toHaveTitle('Manual API v2 · Documentació test');
  await expect(page.getByRole('heading', { level: 1 })).toBeFocused();
  const versions = page.getByRole('table', { name: 'Versions del document' });
  await expect(versions.locator('tbody tr')).toHaveCount(2);
  await expect(versions).toContainText('18/11/25');
  await expect(versions).toContainText('jgarcia@gov.com');
  await page.screenshot({ path: testInfo.outputPath('document-detail-desktop.png'), fullPage: true });

  const title = page.getByLabel('Títol', { exact: true });
  await expect(title).toHaveAttribute('readonly', '');
  await page.getByRole('button', { name: 'Editar Document', exact: true }).click();
  await expect(title).toBeFocused();
  await title.fill('Manual revisat');
  await selectOption(page, 'document-detail-state', 'En revisió');
  await page.getByRole('button', { name: 'Desar els canvis de Document', exact: true }).click();
  await expect(page.getByText('Document desat', { exact: true })).toBeVisible();
  expect(backend.requests).toContainEqual({
    method: 'PUT',
    path: '/application/documentation/1',
    data: { title: 'Manual revisat', sectionId: 1, categoryId: 11, statusId: 2 },
  });
  await expect(page.getByRole('button', { name: 'Editar Document', exact: true })).toBeFocused();
  await expect(title).toHaveAttribute('readonly', '');
  await page.getByRole('button', { name: 'Nova versió', exact: true }).click();
  const dialog = page.getByRole('dialog');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Camp obligatori')).toHaveCount(2);
  await expect(dialog.getByText('Mida màxima: 5 MB.')).toBeVisible();
  await dialog.getByLabel('Versió', { exact: true }).fill('2.1');
  await dialog.getByLabel('Fitxer').setInputFiles({ name: 'programa.exe', mimeType: 'application/octet-stream', buffer: Buffer.from('MZ') });
  await expect(dialog.getByRole('alert').filter({ hasText: 'programa.exe' })).toHaveText(
    "No s'ha adjuntat «programa.exe»: el format del fitxer no està admès.",
  );
  // The picker does not keep showing the refused file.
  expect(await dialog.getByLabel('Fitxer').evaluate((input: HTMLInputElement) => input.files?.length)).toBe(0);
  await dialog.getByLabel('Fitxer').setInputFiles({
    name: 'manual-v3.txt', mimeType: 'text/plain', buffer: Buffer.from('version 3 document contents'),
  });
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Ja existeix aquesta versió.')).toBeVisible();
  await dialog.getByLabel('Versió', { exact: true }).fill('3.0');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(versions.locator('tbody tr')).toHaveCount(3);
  await expect(versions.locator('tbody tr').first()).toContainText('3.0');
  await page.getByRole('button', { name: 'Accions de la versió 3.0', exact: true }).click();
  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('menuitem', { name: 'Descarregar', exact: true }).click();
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe('manual-v3.txt');
  expect(await download.failure()).toBeNull();

  await page.getByRole('button', { name: 'Accions de la versió 3.0', exact: true }).click();
  const popupPromise = context.waitForEvent('page');
  await page.getByRole('menuitem', { name: 'Veure', exact: true }).click();
  const popup = await popupPromise;
  await expect(popup).toHaveURL(/^blob:/);
  await expect(popup.locator('body')).toContainText('version 3 document contents');
  await popup.close();
  await page.getByRole('button', { name: 'Tornar', exact: true }).click();
  await expect(attachments(page).getByRole('row', { name: /Manual revisat/ })).toContainText('3.0');
});

test('guards unsaved document changes and opens a document directly at mobile widths', async ({ page }, testInfo) => {
  await openDocumentationTab(page);
  await page.goto('/aplicacions/1/documentation/1');
  await expect(page.getByLabel('Títol', { exact: true })).toHaveValue('Manual API v2');
  await page.getByRole('button', { name: 'Editar Document', exact: true }).click();
  await page.getByLabel('Títol', { exact: true }).fill('Canvis pendents');
  await page.getByRole('button', { name: 'Tornar', exact: true }).click();
  const confirmation = page.getByRole('dialog');
  await expect(confirmation.getByText('Canvis sense desar', { exact: true })).toBeVisible();
  await confirmation.getByRole('button', { name: 'Continua editant', exact: true }).click();
  await expect(page.getByLabel('Títol', { exact: true })).toHaveValue('Canvis pendents');
  await page.getByRole('button', { name: 'Tornar', exact: true }).click();
  await page.getByRole('button', { name: 'Descarta els canvis', exact: true }).click();
  await expect(page).toHaveURL(/\/documentation$/);
  await expect(attachments(page)).toContainText('Manual API v2');
  await page.goto('/aplicacions/1/documentation/1');
  await expect(page.getByLabel('Títol', { exact: true })).toHaveValue('Manual API v2');
  for (const width of [768, 320]) {
    await page.setViewportSize({ width, height: 1000 });
    expect(await page.locator('body').evaluate((element) => element.scrollWidth <= innerWidth + 1)).toBe(true);
    await page.screenshot({ path: testInfo.outputPath('document-detail-' + width + '.png'), fullPage: true });
  }
  const axe = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']).analyze();
  expect(axe.violations.map(({ id, nodes }) => id + ': ' + nodes.map(({ target }) => target).join(', '))).toEqual([]);
  await page.addStyleTag({ content: 'html { font-size: 200%; }' });
  const overflow = await page.locator('body').evaluate((element) => ({
    width: innerWidth, scrollWidth: element.scrollWidth,
    overflowing: [...document.querySelectorAll<HTMLElement>('.document-detail-actions *, .document-detail-versions__header *, app-application-document-detail')]
      .filter((child) => child.getBoundingClientRect().right > innerWidth)
      .map((child) => ({ tag: child.tagName, className: child.className, right: child.getBoundingClientRect().right })),
  }));
  expect(overflow.scrollWidth, JSON.stringify(overflow)).toBeLessThanOrEqual(overflow.width + 1);
  await page.screenshot({ path: testInfo.outputPath('document-detail-320-text-200.png'), fullPage: true });
});

test('places document metadata in one desktop row and supports Edit and Cancel in the top toolbar', async ({ page }, testInfo) => {
  await openDocumentationTab(page);
  await page.goto('/aplicacions/1/documentation/1');
  const title = page.getByLabel('Títol', { exact: true });
  const edit = page.getByRole('button', { name: 'Editar Document', exact: true });
  await expect(title).toHaveAttribute('readonly', '');
  await expect(page.locator('.document-detail-form p-select')).toHaveCount(0);

  for (const mode of ['view', 'edit']) {
    if (mode === 'edit') {
      await edit.click();
      await expect(title).toBeFocused();
      await expect(page.locator('.document-detail-form p-select')).toHaveCount(3);
    }
    for (const width of [1440, 768, 320]) {
      await page.setViewportSize({ width, height: 1000 });
      const ids = ['title', 'category', 'subcategory', 'state']
        .map((field) => '#document-detail-' + field + (mode === 'view' && field !== 'title' ? '-value' : ''));
      const boxes = await Promise.all(ids.map((id) => page.locator(id).boundingBox()));
      expect(boxes.every(Boolean)).toBe(true);
      if (width === 1440) {
        expect(Math.max(...boxes.map((box) => box!.y)) - Math.min(...boxes.map((box) => box!.y))).toBeLessThan(3);
        const toolbar = await page.locator('.document-detail-toolbar').boundingBox();
        const actions = await page.locator('.document-detail-toolbar app-application-detail-section-actions').boundingBox();
        expect(Math.abs(toolbar!.x + toolbar!.width - actions!.x - actions!.width)).toBeLessThan(2);
      } else if (width === 768) {
        expect(Math.abs(boxes[0]!.y - boxes[1]!.y)).toBeLessThan(3);
        expect(boxes[2]!.y).toBeGreaterThan(boxes[0]!.y + boxes[0]!.height);
      } else {
        for (let index = 1; index < boxes.length; index++) {
          expect(boxes[index]!.y).toBeGreaterThan(boxes[index - 1]!.y + boxes[index - 1]!.height);
        }
      }
      expect(await page.locator('body').evaluate((element) => element.scrollWidth <= innerWidth + 1)).toBe(true);
      await page.screenshot({ path: testInfo.outputPath('document-detail-' + mode + '-' + width + '.png'), fullPage: true });
    }
  }

  await title.fill('Canvi cancel·lat');
  await page.getByRole('button', { name: 'Cancel·lar els canvis de Document', exact: true }).click();
  await expect(title).toHaveValue('Manual API v2');
  await expect(title).toHaveAttribute('readonly', '');
  await expect(edit).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(title).toBeFocused();
  await page.addStyleTag({ content: 'html { font-size: 200%; }' });
  const overflow = await page.locator('body').evaluate((element) => ({
    width: innerWidth, scrollWidth: element.scrollWidth,
    overflowing: [...document.querySelectorAll<HTMLElement>('.document-detail-toolbar *')]
      .filter((child) => child.getBoundingClientRect().right > innerWidth)
      .map((child) => ({ tag: child.tagName, className: child.className, right: child.getBoundingClientRect().right })),
  }));
  expect(overflow.scrollWidth, JSON.stringify(overflow)).toBeLessThanOrEqual(overflow.width + 1);
  const axe = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']).analyze();
  expect(axe.violations.map(({ id, nodes }) => id + ': ' + nodes.map(({ target }) => target).join(', '))).toEqual([]);
  await page.screenshot({ path: testInfo.outputPath('document-detail-edit-320-text-200.png'), fullPage: true });
});

test('does not let attach a file while the accepted formats cannot be read', async ({ page }) => {
  await openDocumentationTab(page, 'ACTIVE', { mimeTypesAvailable: false });
  await expect(page.getByText("No s'han pogut carregar les seccions, els estats i els formats dels documents.").first()).toBeVisible();

  await page.getByRole('button', { name: 'Afegeix un adjunt' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('button', { name: 'Adjunta', exact: true })).toBeDisabled();
  await expect(dialog.getByRole('status')).toHaveText(
    "No s'han pogut carregar els formats admesos: ara no es pot adjuntar cap fitxer.",
  );
  await dialog.getByRole('button', { name: /Cancel·la/ }).click();
  await expect(dialog).toHaveCount(0);

  await rowAction(page, "l'adjunt", 'Manual API v2', 'Consulta');
  await page.getByRole('button', { name: 'Nova versió', exact: true }).click();
  await expect(page.getByRole('dialog').getByLabel('Fitxer')).toBeDisabled();
  await expect(page.locator('#document-version-file-hint')).toHaveText(
    "No s'han pogut carregar els formats admesos: ara no es pot adjuntar cap fitxer.",
  );
});

test('keeps documents read-only when opening them from an inactive application', async ({ page }) => {
  await openDocumentationTab(page, 'INACTIVE');
  await rowAction(page, "l'adjunt", 'Manual API v2', 'Consulta');
  await expect(page).toHaveURL(/\/documentation\/1$/);
  await expect(page.getByLabel('Títol', { exact: true })).toHaveAttribute('readonly', '');
  await expect(page.getByText('Aquest document és de només lectura.')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Nova versió' })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Desa', exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Editar Document', exact: true })).toHaveCount(0);
});

test('adds, edits and deletes a link', async ({ page }) => {
  await openDocumentationTab(page);

  await page.getByRole('button', { name: 'Afegeix un enllaç' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('heading', { name: 'Afegeix un Enllaç', exact: true })).toBeVisible();
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Camp obligatori')).toHaveCount(3);
  await expect(page.locator('#application-link-section')).toHaveAttribute('aria-invalid', 'true');
  await expect(page.locator('#application-link-section')).toHaveAttribute(
    'aria-describedby', 'application-link-section-error',
  );
  await dialog.getByLabel('Títol', { exact: true }).fill(' ');
  await expect(page.locator('#application-link-title-error')).toHaveText('Camp obligatori');
  await dialog.getByLabel('Títol', { exact: true }).fill('Portal de proves');
  await selectOption(page, 'application-link-section', 'General');
  await dialog.getByLabel('URL').fill('portal.example.com');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Indica una URL que comenci per http:// o https://')).toBeVisible();
  await dialog.getByLabel('URL').fill('https://portal.example.com');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(links(page)).toContainText('Portal de proves');

  await rowAction(page, "l'enllaç", 'Portal de proves', 'Consulta');
  await dialog.getByRole('button', { name: "Edita l'enllaç" }).click();
  await dialog.getByLabel('Descripció (opcional)').fill('Entorn de proves');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(links(page).getByRole('row', { name: /Portal de proves/ })).toContainText('Entorn de proves');

  await rowAction(page, "l'enllaç", 'Portal de proves', 'Elimina');
  await page.getByRole('button', { name: "Confirma l'eliminació de l'enllaç" }).click();
  await expect(links(page)).not.toContainText('Portal de proves');
});

test('matches the new link dialog design across sizes with keyboard access and focus return', async ({ page }, testInfo) => {
  await openDocumentationTab(page);
  const open = page.getByRole('button', { name: 'Afegeix un enllaç' });
  await open.click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByText('Tots els camps són obligatoris, excepte els indicats com a opcionals.')).toBeVisible();
  await dialog.getByLabel('Títol', { exact: true }).fill('Manual API v2.1');
  await selectOption(page, 'application-link-section', 'Accessibilitat');
  await dialog.getByLabel('Descripció (opcional)').fill('CI/CD workflows and automation');
  await dialog.getByLabel('URL', { exact: true }).fill('https://www.example.com/integrations');

  for (const width of [1440, 768, 320]) {
    await page.setViewportSize({ width, height: 900 });
    const title = await page.locator('#application-link-title').boundingBox();
    const section = await page.locator('#application-link-section').boundingBox();
    const description = await page.locator('#application-link-description').boundingBox();
    const url = await page.locator('#application-link-url').boundingBox();
    expect(title).not.toBeNull();
    expect(section).not.toBeNull();
    expect(description).not.toBeNull();
    expect(url).not.toBeNull();
    if (width >= 640) {
      expect(Math.abs(title!.y - section!.y)).toBeLessThan(3);
      expect(section!.x).toBeGreaterThan(title!.x + title!.width);
      expect(description!.width).toBeGreaterThan(title!.width);
    } else {
      expect(section!.y).toBeGreaterThan(title!.y + title!.height);
    }
    expect(description!.y).toBeGreaterThan(section!.y + section!.height);
    expect(url!.y).toBeGreaterThan(description!.y + description!.height);
    expect(Math.abs(description!.width - url!.width)).toBeLessThan(2);
    expect(await dialog.locator('.p-dialog-content').evaluate((element) => element.scrollWidth <= element.clientWidth + 1)).toBe(true);
    await page.screenshot({ path: testInfo.outputPath('link-design-' + width + '.png'), animations: 'disabled' });
  }

  await page.locator('#application-link-title').focus();
  for (const field of ['section', 'description', 'url']) {
    await page.keyboard.press('Tab');
    await expect(page.locator('#application-link-' + field)).toBeFocused();
  }
  await page.keyboard.press('Tab');
  await expect(dialog.getByRole('button', { name: /Cancel·la/ })).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(dialog.getByRole('button', { name: 'Desa', exact: true })).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(dialog.getByRole('button', { name: "Tanca el formulari de l'enllaç" })).toBeFocused();
  const axe = await new AxeBuilder({ page })
    .include('[role="dialog"]')
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(axe.violations.map(({ id, nodes }) => id + ': ' + nodes.map(({ target }) => target).join(', '))).toEqual([]);
  await page.addStyleTag({ content: 'html { font-size: 200%; }' });
  expect(await dialog.locator('.p-dialog-content').evaluate((element) => element.scrollWidth <= element.clientWidth + 1)).toBe(true);
  await page.screenshot({ path: testInfo.outputPath('link-design-320-text-200.png'), animations: 'disabled' });
  await page.keyboard.press('Escape');
  await expect(dialog).toHaveCount(0);
  await expect(open).toBeFocused();
});

test('only lets consult and download the documentation of an inactive application', async ({ page }) => {
  await openDocumentationTab(page, 'INACTIVE');

  await expect(page.getByRole('button', { name: 'Afegeix un adjunt' })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Afegeix un enllaç' })).toHaveCount(0);
  await page.getByRole('button', { name: 'Accions de l\'adjunt «Manual API v2»' }).click();
  await expect(page.locator('.maintenance-row-menu').getByRole('menuitem')).toHaveText([
    'Consulta',
    'Descarrega',
  ]);
});
