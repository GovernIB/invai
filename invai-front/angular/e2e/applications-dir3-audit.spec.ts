import { expect, test, type Page, type Route } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

const POLL_INTERVAL_MS = 3000;
const AUDIT_LABEL = 'Validar DIR3 de totes';
const AUDIT_ANNOUNCEMENT = 'Validant el DIR3 de totes les aplicacions…';
const AUDIT_SUMMARY = 'Validació DIR3 completada';
const AUDIT_ERROR = "No s'ha pogut completar la validació DIR3.";

const pageOf = (content: unknown[]) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  last: true,
  number: 0,
});

const APPLICATION = {
  id: 1,
  code: 'INVAI',
  prefix: 'INV',
  name: 'Inventari',
  status: 'ACTIVE',
  admUnit: { code: 'A1', name: 'Unitat' },
};

interface AuditStatus {
  state: 'IDLE' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  startedBy: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  totalApplications: number;
  processedApplications: number;
  mismatchedApplications: { id: number; name: string; dir3Mismatch: boolean }[];
  failureReason: string | null;
  failedApplicationIds: number[];
}

const audit = (overrides: Partial<AuditStatus> = {}): AuditStatus => ({
  state: 'IDLE',
  startedBy: null,
  startedAt: null,
  finishedAt: null,
  totalApplications: 0,
  processedApplications: 0,
  mismatchedApplications: [],
  failureReason: null,
  failedApplicationIds: [],
  ...overrides,
});

const running = (processed: number, total = 3) =>
  audit({
    state: 'RUNNING',
    startedBy: 'dir3-audit-test',
    totalApplications: total,
    processedApplications: processed,
  });

interface AuditMock {
  // Answer to GET application/dir3/check-all/status.
  status: AuditStatus | { status: number; json: unknown };
  // Answer to PUT application/dir3/check-all.
  start: AuditStatus | { status: number; json: unknown };
  starts: number;
  statusReads: number;
  listRequests: URL[];
}

async function answer(route: Route, reply: AuditMock['status'], okStatus = 200) {
  if ('state' in reply) return route.fulfill({ status: okStatus, json: reply });
  return route.fulfill(reply);
}

async function openList(page: Page): Promise<AuditMock> {
  const mock: AuditMock = {
    status: audit(),
    start: running(0),
    starts: 0,
    statusReads: 0,
    listRequests: [],
  };
  // Time keeps flowing so the page renders; fastForward jumps straight to the next poll.
  await page.clock.install();
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();
    if (path.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'dir3-audit-test' } });
    }
    if (path.endsWith('/application/dir3/check-all') && method === 'PUT') {
      mock.starts++;
      return answer(route, mock.start, 202);
    }
    if (path.endsWith('/application/dir3/check-all/status') && method === 'GET') {
      mock.statusReads++;
      return answer(route, mock.status);
    }
    if (path.endsWith('/application') && method === 'GET') {
      mock.listRequests.push(url);
      return route.fulfill({ json: pageOf([APPLICATION]) });
    }
    return route.fulfill({ json: pageOf([]) });
  });
  await page.goto('/aplicacions');
  await expect(page.locator('app-applications-table tbody tr')).toHaveCount(1);
  return mock;
}

const auditButton = (page: Page) =>
  page.locator('app-section-actions').getByRole('button', { name: /^Valid(ar|ant) DIR3/ });
const toast = (page: Page) => page.locator('.p-toast-message');

async function expectToast(
  page: Page,
  severity: 'success' | 'warn' | 'error',
  summary: string,
  detail: string,
): Promise<void> {
  await expect(toast(page)).toHaveCount(1);
  await expect(toast(page)).toHaveClass(new RegExp(`p-toast-message-${severity}`));
  await expect(toast(page).locator('.p-toast-summary')).toHaveText(summary);
  await expect(toast(page).locator('.p-toast-detail')).toHaveText(detail);
}
const announcement = (page: Page) => page.locator('app-section-container [role="status"]');

async function launchAudit(page: Page, mock: AuditMock, total = 3): Promise<void> {
  mock.start = running(0, total);
  mock.status = running(0, total);
  await auditButton(page).click();
  await expect(auditButton(page)).toHaveAccessibleName(`Validant DIR3: 0 de ${total}`);
  expect(mock.starts).toBe(1);
}

async function nextPoll(page: Page, mock: AuditMock, status: AuditMock['status']) {
  mock.status = status;
  const reads = mock.statusReads;
  await page.clock.fastForward(POLL_INTERVAL_MS);
  await expect.poll(() => mock.statusReads).toBeGreaterThan(reads);
}

test('launches the audit, shows its progress and reloads the list when it ends', async ({
  page,
}, testInfo) => {
  const mock = await openList(page);
  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
  await expect(auditButton(page)).toBeEnabled();
  await expect(announcement(page)).toHaveText('');

  await launchAudit(page, mock);
  await expect(auditButton(page)).toBeDisabled();
  await expect(announcement(page)).toHaveText(AUDIT_ANNOUNCEMENT);
  await page.screenshot({ path: testInfo.outputPath('dir3-audit-running.png') });

  await nextPoll(page, mock, running(2));
  await expect(auditButton(page)).toHaveAccessibleName('Validant DIR3: 2 de 3');
  await expect(announcement(page)).toHaveText(AUDIT_ANNOUNCEMENT);
  expect(mock.starts).toBe(1);

  const listRequests = mock.listRequests.length;
  await nextPoll(
    page,
    mock,
    audit({
      state: 'COMPLETED',
      totalApplications: 3,
      processedApplications: 3,
      mismatchedApplications: [
        { id: 1, name: 'Inventari', dir3Mismatch: true },
        { id: 2, name: 'Portal', dir3Mismatch: true },
      ],
    }),
  );
  await expectToast(page, 'warn', AUDIT_SUMMARY, 'Aplicacions amb discrepàncies DIR3: 2.');
  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
  await expect(auditButton(page)).toBeEnabled();
  await expect(announcement(page)).toHaveText('');

  await expect.poll(() => mock.listRequests.length).toBe(listRequests + 1);
  const reload = mock.listRequests.at(-1)!.searchParams;
  expect(reload.get('page')).toBe('0');
  expect(reload.get('size')).toBe('10');
  expect(reload.get('statusId')).toBe('1');

  // The finished audit is no longer polled.
  const reads = mock.statusReads;
  await page.clock.fastForward(POLL_INTERVAL_MS * 2);
  expect(mock.statusReads).toBe(reads);
});

const completedOutcomes = [
  {
    title: 'reports a completed audit without mismatches as a success',
    status: audit({ state: 'COMPLETED', totalApplications: 3, processedApplications: 3 }),
    severity: 'success' as const,
    detail: 'Aplicacions amb discrepàncies DIR3: 0.',
  },
  {
    title: 'reports the applications a completed audit could not check as a warning',
    status: audit({
      state: 'COMPLETED',
      totalApplications: 3,
      processedApplications: 3,
      mismatchedApplications: [{ id: 2, name: 'Portal', dir3Mismatch: true }],
      failedApplicationIds: [5, 6],
    }),
    severity: 'warn' as const,
    detail:
      "Aplicacions amb discrepàncies DIR3: 1. Aplicacions que no s'han pogut validar: 2.",
  },
];

for (const outcome of completedOutcomes) {
  test(outcome.title, async ({ page }) => {
    const mock = await openList(page);
    await launchAudit(page, mock);

    await nextPoll(page, mock, outcome.status);
    await expectToast(page, outcome.severity, AUDIT_SUMMARY, outcome.detail);
    await expect(auditButton(page)).toBeEnabled();
  });
}

const failureReasons = [
  {
    reason: 'exception.dir3audit.session',
    detail:
      "La validació DIR3 s'ha aturat perquè la teva sessió ha caducat o no es pot renovar. " +
      'Tanca la sessió, torna a entrar i torna-ho a provar.',
  },
  {
    reason: 'exception.dir3audit.soffid',
    detail:
      "La validació DIR3 no ha pogut començar perquè no s'han pogut consultar les dades de " +
      'Soffid. Torna-ho a provar més tard.',
  },
  { reason: 'exception.dir3audit.unknown', detail: AUDIT_ERROR },
];

for (const { reason, detail } of failureReasons) {
  test(`explains a failed audit with reason ${reason} without reloading the list`, async ({
    page,
  }) => {
    const mock = await openList(page);
    await launchAudit(page, mock);
    const listRequests = mock.listRequests.length;

    await nextPoll(
      page,
      mock,
      audit({ state: 'FAILED', totalApplications: 3, failureReason: reason }),
    );
    await expectToast(page, 'error', 'Error', detail);
    await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
    await expect(auditButton(page)).toBeEnabled();
    await expect(page.getByRole('dialog')).toHaveCount(0);
    // Nothing was audited, so the table is not read again.
    expect(mock.listRequests).toHaveLength(listRequests);
  });
}

test('reloads the list when a failed audit had already changed some applications', async ({
  page,
}) => {
  const mock = await openList(page);
  await launchAudit(page, mock);
  const listRequests = mock.listRequests.length;

  await nextPoll(
    page,
    mock,
    audit({
      state: 'FAILED',
      totalApplications: 3,
      processedApplications: 1,
      failureReason: 'exception.dir3audit.session',
    }),
  );
  await expectToast(
    page,
    'error',
    'Error',
    "La validació DIR3 s'ha aturat perquè la teva sessió ha caducat o no es pot renovar. " +
      'Tanca la sessió, torna a entrar i torna-ho a provar.',
  );
  await expect.poll(() => mock.listRequests.length).toBe(listRequests + 1);
});

test('does not read the audit status until the audit is launched', async ({ page }) => {
  const mock = await openList(page);

  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
  await expect(auditButton(page)).toBeEnabled();
  await page.clock.fastForward(POLL_INTERVAL_MS * 2);
  expect(mock.statusReads).toBe(0);
});

test('follows an audit already running on the server when it is launched again', async ({
  page,
}) => {
  const mock = await openList(page);
  // The back-end answers the launch with the status of the audit in progress.
  mock.start = running(3, 4);
  mock.status = running(3, 4);

  await auditButton(page).click();
  await expect(auditButton(page)).toHaveAccessibleName('Validant DIR3: 3 de 4');
  await expect(auditButton(page)).toBeDisabled();

  await nextPoll(
    page,
    mock,
    audit({ state: 'COMPLETED', totalApplications: 4, processedApplications: 4 }),
  );
  await expectToast(page, 'success', AUDIT_SUMMARY, 'Aplicacions amb discrepàncies DIR3: 0.');
  await expect(auditButton(page)).toBeEnabled();
  expect(mock.starts).toBe(1);
});

test('leaves a structured 400 on launch to the server error dialog only', async ({ page }) => {
  const mock = await openList(page);
  mock.start = {
    status: 400,
    json: { error: 'Sessió no renovable', message: 'La sessió no es pot renovar.' },
  };

  await auditButton(page).click();
  const dialog = page.getByRole('dialog', { name: 'Sessió no renovable' });
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('alert')).toHaveText('La sessió no es pot renovar.');
  await expect(toast(page)).toHaveCount(0);
  await dialog.getByRole('button', { name: "Tanca el missatge d'error" }).click();
  await expect(dialog).toBeHidden();

  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
  await expect(auditButton(page)).toBeEnabled();
  await page.clock.fastForward(POLL_INTERVAL_MS);
  expect(mock.statusReads).toBe(0);
});

test('reports any other launch failure with a toast', async ({ page }) => {
  const mock = await openList(page);
  mock.start = { status: 500, json: { message: 'Internal Server Error' } };

  await auditButton(page).click();
  await expectToast(page, 'error', 'Error', AUDIT_ERROR);
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(auditButton(page)).toBeEnabled();
});

test('reports a failure to read the progress without the server error dialog', async ({
  page,
}) => {
  const mock = await openList(page);
  await launchAudit(page, mock);

  await nextPoll(page, mock, {
    status: 400,
    json: { error: 'Error', message: 'No es pot consultar el progrés.' },
  });
  await expectToast(
    page,
    'error',
    'Error',
    "No s'ha pogut consultar el progrés de la validació DIR3.",
  );
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
  await expect(auditButton(page)).toBeEnabled();

  // The audit can be followed again from the button.
  mock.start = running(1);
  await auditButton(page).click();
  await expect(auditButton(page)).toHaveAccessibleName('Validant DIR3: 1 de 3');
  expect(mock.starts).toBe(2);
});
