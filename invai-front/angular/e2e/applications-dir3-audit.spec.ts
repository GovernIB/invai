import type { Page } from '@playwright/test';

import { expect, test } from './support/auth.fixture';
import { apiUrl, expectApiJsonResponse, failApiRequest } from './support/http';

// The audit checks every active application against Soffid for real, which takes a few seconds;
// the page polls its status every 3 seconds.
const POLL_INTERVAL_MS = 3000;
const AUDIT_API = '/invaiback/application/dir3/check-all';
const STATUS_API = `${AUDIT_API}/status`;
const AUDIT_LABEL = 'Validar DIR3 de totes';
const AUDITING = /^Validant DIR3/;
const AUDIT_ANNOUNCEMENT = 'Validant el DIR3 de totes les aplicacions…';
const AUDIT_SUMMARY = 'Validació DIR3 completada';
const AUDIT_ERROR = "No s'ha pogut completar la validació DIR3.";

interface AuditStatus {
  state: 'IDLE' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  totalApplications: number;
  processedApplications: number;
  mismatchedApplications: { id: number; name: string; dir3Mismatch: boolean }[];
  failureReason: string | null;
  failedApplicationIds: number[];
}

interface AuditRequests {
  starts: number;
  statusReads: number;
  listRequests: URL[];
}

async function openList(page: Page): Promise<AuditRequests> {
  const requests: AuditRequests = { starts: 0, statusReads: 0, listRequests: [] };
  page.on('request', (request) => {
    const url = new URL(request.url());
    const method = request.method();
    if (url.pathname === AUDIT_API && method === 'PUT') requests.starts++;
    else if (url.pathname === STATUS_API && method === 'GET') requests.statusReads++;
    else if (url.pathname === '/invaiback/application' && method === 'GET') {
      requests.listRequests.push(url);
    }
  });
  await page.goto('/aplicacions');
  await expect(page.locator('app-applications-table tbody tr').first()).toBeVisible();
  return requests;
}

const auditButton = (page: Page) =>
  page.locator('app-section-actions').getByRole('button', { name: /^Valid(ar|ant) DIR3/ });
const toast = (page: Page) => page.locator('.p-toast-message');
const announcement = (page: Page) => page.locator('app-section-container [role="status"]');

async function expectToast(
  page: Page,
  severity: 'success' | 'warn' | 'error',
  summary: string,
  detail: string,
): Promise<void> {
  await expect(toast(page)).toHaveCount(1, { timeout: 60_000 });
  await expect(toast(page)).toHaveClass(new RegExp(`p-toast-message-${severity}`));
  await expect(toast(page).locator('.p-toast-summary')).toHaveText(summary);
  await expect(toast(page).locator('.p-toast-detail')).toHaveText(detail);
}

// The toast the page shows for the status the audit ended with.
async function expectCompletionToast(page: Page): Promise<void> {
  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL, { timeout: 60_000 });
  const status = await expectApiJsonResponse<AuditStatus>(
    await page.request.get(apiUrl(page, STATUS_API)),
    `GET ${STATUS_API}`,
  );
  expect(status.state).toBe('COMPLETED');
  const mismatched = status.mismatchedApplications.length;
  const failed = status.failedApplicationIds.length;
  await expectToast(
    page,
    mismatched > 0 || failed > 0 ? 'warn' : 'success',
    AUDIT_SUMMARY,
    `Aplicacions amb discrepàncies DIR3: ${mismatched}.` +
      (failed > 0 ? ` Aplicacions que no s'han pogut validar: ${failed}.` : ''),
  );
}

async function launchAudit(page: Page, requests: AuditRequests): Promise<void> {
  await auditButton(page).click();
  await expect(auditButton(page)).toHaveAccessibleName(AUDITING);
  expect(requests.starts).toBe(1);
}

// Answers the progress of the audit with a failed status; the audit itself runs for real.
async function failAudit(page: Page, status: Partial<AuditStatus>): Promise<void> {
  await failApiRequest(page, 'GET', STATUS_API, {
    status: 200,
    json: {
      state: 'FAILED',
      startedBy: null,
      startedAt: null,
      finishedAt: null,
      totalApplications: 3,
      processedApplications: 0,
      mismatchedApplications: [],
      failureReason: null,
      failedApplicationIds: [],
      ...status,
    },
  });
}

test('launches the audit, shows its progress and reloads the list when it ends', async ({
  page,
}, testInfo) => {
  test.setTimeout(120_000);
  const requests = await openList(page);
  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
  await expect(auditButton(page)).toBeEnabled();
  await expect(announcement(page)).toHaveText('');

  const listRequests = requests.listRequests.length;
  await launchAudit(page, requests);
  await expect(auditButton(page)).toBeDisabled();
  await expect(announcement(page)).toHaveText(AUDIT_ANNOUNCEMENT);
  await page.screenshot({ path: testInfo.outputPath('dir3-audit-running.png') });

  await expectCompletionToast(page);
  await expect(auditButton(page)).toBeEnabled();
  await expect(announcement(page)).toHaveText('');
  expect(requests.starts).toBe(1);

  await expect.poll(() => requests.listRequests.length).toBe(listRequests + 1);
  const reload = requests.listRequests.at(-1)!.searchParams;
  expect(reload.get('page')).toBe('0');
  expect(reload.get('size')).toBe('10');
  expect(reload.get('statusId')).toBe('1');

  // The finished audit is no longer polled.
  const reads = requests.statusReads;
  await page.waitForTimeout(POLL_INTERVAL_MS * 2);
  expect(requests.statusReads).toBe(reads);
});

// Which toast a completed audit shows for its mismatches and unchecked applications depends on the
// data it finds; each combination is covered by the unit tests of ApplicationsList.

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
    const requests = await openList(page);
    await failAudit(page, { failureReason: reason });
    await launchAudit(page, requests);
    const listRequests = requests.listRequests.length;

    await expectToast(page, 'error', 'Error', detail);
    await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
    await expect(auditButton(page)).toBeEnabled();
    await expect(page.getByRole('dialog')).toHaveCount(0);
    // Nothing was audited, so the table is not read again.
    expect(requests.listRequests).toHaveLength(listRequests);
  });
}

test('reloads the list when a failed audit had already changed some applications', async ({
  page,
}) => {
  const requests = await openList(page);
  await failAudit(page, {
    processedApplications: 1,
    failureReason: 'exception.dir3audit.session',
  });
  await launchAudit(page, requests);
  const listRequests = requests.listRequests.length;

  await expectToast(
    page,
    'error',
    'Error',
    "La validació DIR3 s'ha aturat perquè la teva sessió ha caducat o no es pot renovar. " +
      'Tanca la sessió, torna a entrar i torna-ho a provar.',
  );
  await expect.poll(() => requests.listRequests.length).toBe(listRequests + 1);
});

test('does not read the audit status until the audit is launched', async ({ page }) => {
  const requests = await openList(page);

  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
  await expect(auditButton(page)).toBeEnabled();
  await page.waitForTimeout(POLL_INTERVAL_MS * 2);
  expect(requests.statusReads).toBe(0);
});

test('follows an audit already running on the server when it is launched again', async ({
  page,
}) => {
  test.setTimeout(120_000);
  const requests = await openList(page);
  // Launched by someone else just before: the back-end answers with the audit in progress.
  const running = await expectApiJsonResponse<AuditStatus>(
    await page.request.put(apiUrl(page, AUDIT_API)),
    `PUT ${AUDIT_API}`,
  );
  expect(running.state).toBe('RUNNING');

  await auditButton(page).click();
  await expect(auditButton(page)).toHaveAccessibleName(AUDITING);
  await expect(auditButton(page)).toBeDisabled();

  await expectCompletionToast(page);
  await expect(auditButton(page)).toBeEnabled();
  expect(requests.starts).toBe(1);
});

test('leaves a structured 400 on launch to the server error dialog only', async ({ page }) => {
  const requests = await openList(page);
  await failApiRequest(page, 'PUT', AUDIT_API, {
    status: 400,
    json: { error: 'Sessió no renovable', message: 'La sessió no es pot renovar.' },
  });

  await auditButton(page).click();
  const dialog = page.getByRole('dialog', { name: 'Sessió no renovable' });
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('alert')).toHaveText('La sessió no es pot renovar.');
  await expect(toast(page)).toHaveCount(0);
  await dialog.getByRole('button', { name: "Tanca el missatge d'error" }).click();
  await expect(dialog).toBeHidden();

  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL);
  await expect(auditButton(page)).toBeEnabled();
  await page.waitForTimeout(POLL_INTERVAL_MS);
  expect(requests.statusReads).toBe(0);
});

test('reports any other launch failure with a toast', async ({ page }) => {
  await openList(page);
  await failApiRequest(page, 'PUT', AUDIT_API, {
    status: 500,
    json: { message: 'Internal Server Error' },
  });

  await auditButton(page).click();
  await expectToast(page, 'error', 'Error', AUDIT_ERROR);
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(auditButton(page)).toBeEnabled();
});

test('reports a failure to read the progress without the server error dialog', async ({ page }) => {
  test.setTimeout(120_000);
  const requests = await openList(page);
  await failApiRequest(page, 'GET', STATUS_API, {
    status: 400,
    json: { error: 'Error', message: 'No es pot consultar el progrés.' },
  });
  await launchAudit(page, requests);

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
  await auditButton(page).click();
  await expect(auditButton(page)).toHaveAccessibleName(AUDITING);
  expect(requests.starts).toBe(2);
  await expect(auditButton(page)).toHaveAccessibleName(AUDIT_LABEL, { timeout: 60_000 });
});
