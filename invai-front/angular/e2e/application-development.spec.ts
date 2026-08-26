import type { Locator, Page, Request, TestInfo } from '@playwright/test';

import {
  APPLICATION_DEVELOPMENT_API,
  type ApplicationDevelopmentAggregate,
  type ApplicationProviderRecord,
  type ApplicationTechnologyRecord,
  registerApplicationProviderCleanup,
  registerApplicationTechnologyCleanup,
  requireDevelopmentId,
} from './support/application-development';
import { expect, test } from './support/application.fixture';
import { findApiCatalogItem } from './support/catalog';
import { uniqueToken } from './support/data';
import {
  DEVELOPMENT_MAINTENANCE_API,
  type DevelopmentRoleRecord,
  type DevelopmentTechnologyRecord,
} from './support/development-maintenances';
import { expectJsonResponse, expectSuccessfulResponse, waitForApiResponse } from './support/http';
import { ACTIVE_STATUS_ID, type EnvironmentRecord, SYSTEMS_API } from './support/systems';
import { selectFilteredOption, selectOption, selectRowAction } from './support/ui';

const SECTION_TITLE = 'Configuració del desenvolupament';
const EDIT_SECTION = `Editar ${SECTION_TITLE}`;
const SAVE_SECTION = `Desar els canvis de ${SECTION_TITLE}`;
const CANCEL_SECTION = `Cancel·lar els canvis de ${SECTION_TITLE}`;

const PROVIDER_ACTIONS = {
  accept: 'Accepta la consulta del proveïdor',
  add: 'Afegeix el proveïdor',
  cancel: 'Cancel·la els canvis del proveïdor',
  deactivate: 'Dona de baixa el proveïdor',
  edit: 'Edita el proveïdor',
  save: 'Desa els canvis del proveïdor',
};

const TECHNOLOGY_ACTIONS = {
  accept: 'Accepta la consulta de la tecnologia',
  add: 'Afegeix la tecnologia',
  cancel: 'Cancel·la els canvis de la tecnologia',
  deactivate: 'Dona de baixa la tecnologia',
  edit: 'Edita la tecnologia',
  save: 'Desa els canvis de la tecnologia',
};

test.describe('application development', () => {
  test('loads the automatic empty aggregate and every initial contract once', async ({
    page,
    disposableApplication,
  }) => {
    test.setTimeout(180_000);
    const appDevelopmentId = requireDevelopmentId(disposableApplication.appDevelopmentId);
    const requests: string[] = [];
    const listener = (request: Request) => {
      if (request.method() !== 'GET') return;
      const url = new URL(request.url());
      if (isDevelopmentInitialRequest(url.pathname, disposableApplication.id, appDevelopmentId)) {
        requests.push(url.toString());
      }
    };
    const aggregatePromise = waitForApiResponse(
      page,
      'GET',
      `${APPLICATION_DEVELOPMENT_API.aggregate}/${appDevelopmentId}`,
    );

    page.on('request', listener);
    await page.goto(developmentRoute(disposableApplication.id));
    const aggregate = await expectJsonResponse<ApplicationDevelopmentAggregate>(
      await aggregatePromise,
    );
    await waitForDevelopmentSection(page);
    page.off('request', listener);

    expect(aggregate).toMatchObject({
      id: appDevelopmentId,
      application: { id: disposableApplication.id },
      environment: null,
      modality: null,
      code: null,
      standardAdaption: null,
      revisionDate: null,
      observation: null,
      deletedAt: null,
    });
    expect(duplicateUrls(requests), `Duplicate initial requests: ${requests.join(', ')}`).toEqual(
      [],
    );
    expect(
      countPath(
        requests,
        `${APPLICATION_DEVELOPMENT_API.applications}/${disposableApplication.id}`,
      ),
    ).toBe(1);
    expect(
      countPath(requests, `${APPLICATION_DEVELOPMENT_API.aggregate}/${appDevelopmentId}`),
    ).toBe(1);
    expect(
      countPath(requests, `${APPLICATION_DEVELOPMENT_API.providers}/${appDevelopmentId}`),
    ).toBe(1);
    expect(
      countPath(requests, `${APPLICATION_DEVELOPMENT_API.technologies}/${appDevelopmentId}`),
    ).toBe(1);
    expect(countPath(requests, APPLICATION_DEVELOPMENT_API.environments)).toBeGreaterThan(0);
    expect(countPath(requests, APPLICATION_DEVELOPMENT_API.roles)).toBeGreaterThan(0);
    expect(countPath(requests, APPLICATION_DEVELOPMENT_API.technologyCatalog)).toBeGreaterThan(0);

    await expect(page.getByRole('button', { name: 'Afegeix un proveïdor' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Afegeix una tecnologia' })).toHaveCount(0);
    await expect(page.locator('#application-development-code')).toHaveValue('');
    await expect(page.locator('#application-development-code')).toBeDisabled();
    await expect(providerPanel(page).getByText("No s'han trobat resultats")).toBeVisible();
    await expect(technologyPanel(page).getByText("No s'han trobat resultats")).toBeVisible();
  });

  test('validates, saves, reloads and updates the development aggregate', async ({
    page,
    disposableApplication,
  }) => {
    test.setTimeout(240_000);
    const appDevelopmentId = requireDevelopmentId(disposableApplication.appDevelopmentId);
    const environment = await requireActiveEnvironment(page);
    const initialCode = `https://example.test/${disposableApplication.code.toLowerCase()}`;
    const updatedCode = `${initialCode}/updated`;
    const initialObservation = `Observació de desenvolupament ${disposableApplication.code}`;
    const updatedObservation = `${initialObservation} actualitzada`;

    await page.goto(developmentRoute(disposableApplication.id));
    await waitForDevelopmentSection(page);
    await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
    await expect(page.getByRole('button', { name: CANCEL_SECTION, exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Afegeix un proveïdor' })).toBeEnabled();
    await expect(page.getByRole('button', { name: 'Afegeix una tecnologia' })).toBeEnabled();

    await expectNoMutation(page, APPLICATION_DEVELOPMENT_API.aggregate, async () => {
      await page.getByRole('button', { name: SAVE_SECTION, exact: true }).click();
    });
    await expect(page.getByText('Camp obligatori', { exact: true })).toHaveCount(5);

    await selectOption(
      page,
      'application-development-environment',
      environment.name ?? environment.code ?? `#${environment.id}`,
    );
    await selectOption(page, 'application-development-modality', 'Desenvolupament Intern');
    await page.locator('#application-development-code').fill('repositori-no-valid');
    await selectOption(page, 'application-development-standard', 'Conforme');
    await fillDate(page, 'application-development-revision-date', '04/08/2026');
    await developmentEditor(page).fill(initialObservation);

    await expectNoMutation(page, APPLICATION_DEVELOPMENT_API.aggregate, async () => {
      await page.getByRole('button', { name: SAVE_SECTION, exact: true }).click();
    });
    await expect(page.getByText('Introdueix una URL HTTP o HTTPS vàlida')).toBeVisible();

    await page.locator('#application-development-code').fill(initialCode);
    const savePromise = waitForApiResponse(
      page,
      'PUT',
      `${APPLICATION_DEVELOPMENT_API.aggregate}/${appDevelopmentId}`,
    );
    await page.getByRole('button', { name: SAVE_SECTION, exact: true }).click();
    const saveResponse = await savePromise;
    const initialPayload = saveResponse.request().postDataJSON() as Record<string, unknown>;
    expect(initialPayload).toMatchObject({
      applicationId: disposableApplication.id,
      environmentId: environment.id,
      modalityId: 1,
      code: initialCode,
      standardAdaptionId: 1,
      revisionDate: '2026-08-04T00:00:00',
    });
    expect(richTextVisible(String(initialPayload['observation']))).toBe(initialObservation);
    const saved = await expectJsonResponse<ApplicationDevelopmentAggregate>(saveResponse);
    expect(saved).toMatchObject({
      id: appDevelopmentId,
      application: { id: disposableApplication.id },
      environment: { id: environment.id },
      modality: { id: 1 },
      code: initialCode,
      standardAdaption: { id: 1 },
    });
    await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toBeVisible();
    await expect(page.locator('#application-development-code')).toBeDisabled();
    const sourceLink = page.getByRole('link', { name: 'Obrir el repositori de codi font' });
    await expect(sourceLink).toHaveAttribute('href', initialCode);
    await expect(sourceLink).toHaveAttribute('target', '_blank');
    await expect(sourceLink).toHaveAttribute('rel', 'noopener noreferrer');

    await page.goto(developmentRoute(disposableApplication.id));
    await waitForDevelopmentSection(page);
    await expect(page.locator('#application-development-code')).toHaveValue(initialCode);
    await expect(developmentEditor(page)).toContainText(initialObservation);

    await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
    await selectOption(page, 'application-development-modality', 'Desenvolupament Mixt');
    await selectOption(page, 'application-development-standard', 'Parcialment Conforme');
    await page.locator('#application-development-code').fill(updatedCode);
    await developmentEditor(page).fill(updatedObservation);
    const updatePromise = waitForApiResponse(
      page,
      'PUT',
      `${APPLICATION_DEVELOPMENT_API.aggregate}/${appDevelopmentId}`,
    );
    await page.getByRole('button', { name: SAVE_SECTION, exact: true }).click();
    const updateResponse = await updatePromise;
    expect(updateResponse.request().postDataJSON()).toMatchObject({
      applicationId: disposableApplication.id,
      modalityId: 3,
      code: updatedCode,
      standardAdaptionId: 2,
    });
    await expectJsonResponse<ApplicationDevelopmentAggregate>(updateResponse);

    await page.goto(developmentRoute(disposableApplication.id));
    await waitForDevelopmentSection(page);
    await expect(page.locator('#application-development-code')).toHaveValue(updatedCode);
    await expect(developmentEditor(page)).toContainText(updatedObservation);
    await expect(
      page.getByRole('link', { name: 'Obrir el repositori de codi font' }),
    ).toHaveAttribute('href', updatedCode);
  });

  test('provider supports validation, UI CRUD and isolated table refreshes', async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }, testInfo) => {
    test.setTimeout(240_000);
    const appDevelopmentId = requireDevelopmentId(disposableApplication.appDevelopmentId);
    const token = uniqueToken(testInfo, 8);
    const initialName = `E2E Proveïdor aplicació ${token}`;
    const updatedName = `${initialName} actualitzat`;
    const role = await requireActiveRole(page);
    const cleanupTarget: {
      id?: number;
      matches: (item: ApplicationProviderRecord) => boolean;
    } = {
      matches: (item) => item.companyName.includes(token),
    };
    registerApplicationProviderCleanup({
      page,
      cleanup: cleanupRegistry,
      appDevelopmentId,
      target: cleanupTarget,
    });

    await page.goto(developmentRoute(disposableApplication.id));
    await waitForDevelopmentSection(page);
    await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
    const providers = providerPanel(page);
    await page.getByRole('button', { name: 'Afegeix un proveïdor' }).click();
    await expect(page.getByRole('heading', { name: 'Afegir proveïdor' })).toBeVisible();

    await expectNoMutation(page, APPLICATION_DEVELOPMENT_API.providers, async () => {
      await page.getByRole('button', { name: PROVIDER_ACTIONS.add, exact: true }).click();
    });
    await expect(page.locator('#application-provider-dialog-company-name-error')).toBeVisible();
    await expect(page.locator('#application-provider-dialog-role-error')).toBeVisible();

    await page.locator('#application-provider-dialog-company-name').fill(initialName);
    await fillDate(page, 'application-provider-dialog-start-date', '10/08/2026');
    await fillDate(page, 'application-provider-dialog-expire-date', '09/08/2026');
    await expectNoMutation(page, APPLICATION_DEVELOPMENT_API.providers, async () => {
      await page.getByRole('button', { name: PROVIDER_ACTIONS.add, exact: true }).click();
    });
    await expect(
      page.getByText("La data de fi no pot ser anterior a la data d'inici"),
    ).toBeVisible();
    await fillDate(page, 'application-provider-dialog-expire-date', '11/08/2026');

    await expectNoMutation(page, APPLICATION_DEVELOPMENT_API.providers, async () => {
      await page.getByRole('button', { name: PROVIDER_ACTIONS.add, exact: true }).click();
    });
    const roleCombobox = page.locator('#application-provider-dialog-role');
    await expect(roleCombobox).toHaveAttribute('aria-invalid', 'true');
    await expect(roleCombobox.locator('..')).toHaveClass(/p-invalid/);
    await expect(roleCombobox).toHaveAttribute(
      'aria-describedby',
      'application-provider-dialog-role-error',
    );
    await expect(page.locator('#application-provider-dialog-role-error')).toContainText(
      'Camp obligatori',
    );
    await selectFilteredOption(page, 'application-provider-dialog-role', role.name!, role.name!);
    await expect(roleCombobox).toHaveAttribute('aria-invalid', 'false');
    await expect(roleCombobox.locator('..')).not.toHaveClass(/p-invalid/);
    await expect(page.locator('#application-provider-dialog-role-error')).toBeHidden();

    const createRequests: string[] = [];
    const stopCreateTracking = trackGetRequests(page, createRequests);
    const createPromise = waitForApiResponse(page, 'POST', APPLICATION_DEVELOPMENT_API.providers);
    await page.getByRole('button', { name: PROVIDER_ACTIONS.add, exact: true }).click();
    const createResponse = await createPromise;
    expect(createResponse.request().postDataJSON()).toEqual({
      appDevelopmentId,
      companyName: initialName,
      roleId: role.id,
      startDate: '2026-08-10T00:00:00',
      expireDate: '2026-08-11T00:00:00',
    });
    const created = await expectJsonResponse<ApplicationProviderRecord>(createResponse);
    expect(created).toMatchObject({
      id: expect.any(Number),
      companyName: initialName,
      role: expect.objectContaining({ id: role.id }),
      startDate: '2026-08-10T00:00:00',
      expireDate: '2026-08-11T00:00:00',
    });
    cleanupTarget.id = created.id;
    await expect(applicationRow(providers, initialName)).toBeVisible();
    stopCreateTracking();
    expect(
      countPath(createRequests, `${APPLICATION_DEVELOPMENT_API.providers}/${appDevelopmentId}`),
    ).toBe(1);
    expect(
      countPath(createRequests, `${APPLICATION_DEVELOPMENT_API.technologies}/${appDevelopmentId}`),
    ).toBe(0);
    const initialRow = applicationRow(providers, initialName);
    await initialRow.focus();
    await initialRow.dblclick();
    await expect(page.getByRole('heading', { name: 'Editar proveïdor' })).toBeVisible();
    await expect(page.locator('#application-provider-dialog-company-name')).toBeEnabled();
    await page.locator('#application-provider-dialog-company-name').fill('Canvi cancel·lat');
    await page.getByRole('button', { name: PROVIDER_ACTIONS.cancel, exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Consultar proveïdor' })).toBeVisible();
    await expect(page.locator('#application-provider-dialog-company-name')).toHaveValue(
      initialName,
    );
    await expect(page.locator('#application-provider-dialog-company-name')).toBeDisabled();
    await page.getByRole('button', { name: 'Tanca el formulari del proveïdor' }).click();
    await expectFocusReturnOrRecord(initialRow, testInfo, 'application provider');

    await selectRowAction(page, initialRow, 'Edita', 'Obre les accions del proveïdor');
    await expect(page.getByRole('heading', { name: 'Editar proveïdor' })).toBeVisible();
    await page.locator('#application-provider-dialog-company-name').fill(updatedName);
    await selectFilteredOption(page, 'application-provider-dialog-role', role.name!, role.name!);
    const updateRequests: string[] = [];
    const stopUpdateTracking = trackGetRequests(page, updateRequests);
    const updatePromise = waitForApiResponse(
      page,
      'PUT',
      `${APPLICATION_DEVELOPMENT_API.providers}/${created.id}`,
    );
    const updateRefreshPromise = waitForDevelopmentResourceRefresh(
      page,
      APPLICATION_DEVELOPMENT_API.providers,
      appDevelopmentId,
    );
    await page.getByRole('button', { name: PROVIDER_ACTIONS.save, exact: true }).click();
    const updateResponse = await updatePromise;
    expect(updateResponse.request().postDataJSON()).toMatchObject({
      appDevelopmentId,
      companyName: updatedName,
      roleId: role.id,
    });
    await expectJsonResponse<ApplicationProviderRecord>(updateResponse);
    await expectSuccessfulResponse(await updateRefreshPromise);
    stopUpdateTracking();
    expect(
      countPath(updateRequests, `${APPLICATION_DEVELOPMENT_API.providers}/${appDevelopmentId}`),
    ).toBe(1);
    expect(
      countPath(updateRequests, `${APPLICATION_DEVELOPMENT_API.technologies}/${appDevelopmentId}`),
    ).toBe(0);
    const updatedRow = applicationRow(providers, updatedName);
    await expect(updatedRow).toContainText(role.name!);

    await selectRowAction(page, updatedRow, 'Dona de baixa', 'Obre les accions del proveïdor');
    const deleteRequests: string[] = [];
    const stopDeleteTracking = trackGetRequests(page, deleteRequests);
    const deletePromise = waitForApiResponse(
      page,
      'DELETE',
      `${APPLICATION_DEVELOPMENT_API.providers}/${created.id}`,
    );
    const deleteRefreshPromise = waitForDevelopmentResourceRefresh(
      page,
      APPLICATION_DEVELOPMENT_API.providers,
      appDevelopmentId,
    );
    await page.getByRole('button', { name: 'Confirma la baixa del proveïdor' }).click();
    await expectSuccessfulResponse(await deletePromise);
    await expectSuccessfulResponse(await deleteRefreshPromise);
    stopDeleteTracking();
    expect(
      countPath(deleteRequests, `${APPLICATION_DEVELOPMENT_API.providers}/${appDevelopmentId}`),
    ).toBe(1);
    expect(
      countPath(deleteRequests, `${APPLICATION_DEVELOPMENT_API.technologies}/${appDevelopmentId}`),
    ).toBe(0);
    await expect(applicationRow(providers, updatedName)).toBeVisible();
    await applicationRow(providers, updatedName)
      .getByRole('button', { name: 'Obre les accions del proveïdor' })
      .click();
    await expect(page.getByRole('menuitem', { name: 'Edita', exact: true })).toHaveAttribute(
      'aria-disabled',
      'true',
    );
    await page.keyboard.press('Escape');
  });

  test('technology supports validation, UI CRUD and isolated table refreshes', async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }, testInfo) => {
    test.setTimeout(240_000);
    const appDevelopmentId = requireDevelopmentId(disposableApplication.appDevelopmentId);
    const token = uniqueToken(testInfo, 8);
    const initialVersion = `E2E-${token}`;
    const updatedVersion = `${initialVersion}-UPD`;
    const initialArchitecture = `Arquitectura E2E ${token}`;
    const updatedArchitecture = `${initialArchitecture} actualitzada`;
    const catalogTechnology = await requireActiveTechnology(page);
    const cleanupTarget: {
      id?: number;
      matches: (item: ApplicationTechnologyRecord) => boolean;
    } = {
      matches: (item) => item.version.includes(token) || item.architecture.includes(token),
    };
    registerApplicationTechnologyCleanup({
      page,
      cleanup: cleanupRegistry,
      appDevelopmentId,
      target: cleanupTarget,
    });

    await page.goto(developmentRoute(disposableApplication.id));
    await waitForDevelopmentSection(page);
    await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
    const technologies = technologyPanel(page);
    await page.getByRole('button', { name: 'Afegeix una tecnologia' }).click();
    await expect(page.getByRole('heading', { name: 'Afegir tecnologia' })).toBeVisible();

    await expectNoMutation(page, APPLICATION_DEVELOPMENT_API.technologies, async () => {
      await page.getByRole('button', { name: TECHNOLOGY_ACTIONS.add, exact: true }).click();
    });
    await expect(page.getByText('Camp obligatori', { exact: true })).toHaveCount(3);
    await selectFilteredOption(
      page,
      'application-technology-dialog-technology',
      catalogTechnology.name!,
      catalogTechnology.name!,
    );
    await expect(page.locator('#application-technology-dialog-layer')).toHaveValue(
      catalogTechnology.layer.name!,
    );
    await page.locator('#application-technology-dialog-version').fill(initialVersion);
    await page.locator('#application-technology-dialog-architecture').fill(initialArchitecture);

    const createRequests: string[] = [];
    const stopCreateTracking = trackGetRequests(page, createRequests);
    const createPromise = waitForApiResponse(
      page,
      'POST',
      APPLICATION_DEVELOPMENT_API.technologies,
    );
    const createRefreshPromise = waitForDevelopmentResourceRefresh(
      page,
      APPLICATION_DEVELOPMENT_API.technologies,
      appDevelopmentId,
    );
    await page.getByRole('button', { name: TECHNOLOGY_ACTIONS.add, exact: true }).click();
    const createResponse = await createPromise;
    expect(createResponse.request().postDataJSON()).toEqual({
      appDevelopmentId,
      layerId: catalogTechnology.layer.id,
      technologyId: catalogTechnology.id,
      version: initialVersion,
      architecture: initialArchitecture,
    });
    const created = await expectJsonResponse<ApplicationTechnologyRecord>(createResponse);
    cleanupTarget.id = created.id;
    await expectSuccessfulResponse(await createRefreshPromise);
    stopCreateTracking();
    expect(
      countPath(createRequests, `${APPLICATION_DEVELOPMENT_API.technologies}/${appDevelopmentId}`),
    ).toBe(1);
    expect(
      countPath(createRequests, `${APPLICATION_DEVELOPMENT_API.providers}/${appDevelopmentId}`),
    ).toBe(0);
    await expect(applicationRow(technologies, initialVersion)).toBeVisible();

    const initialRow = applicationRow(technologies, initialVersion);
    await initialRow.focus();
    await initialRow.press('Enter');
    await expect(page.getByRole('heading', { name: 'Editar tecnologia' })).toBeVisible();
    await expect(page.locator('#application-technology-dialog-version')).toBeEnabled();
    await expect(page.locator('#application-technology-dialog-layer')).toHaveAttribute(
      'readonly',
      '',
    );
    await page.locator('#application-technology-dialog-version').fill('Canvi cancel·lat');
    await page.getByRole('button', { name: TECHNOLOGY_ACTIONS.cancel, exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Consultar tecnologia' })).toBeVisible();
    await expect(page.locator('#application-technology-dialog-version')).toHaveValue(
      initialVersion,
    );
    await page.getByRole('button', { name: 'Tanca el formulari de la tecnologia' }).click();
    await expectFocusReturnOrRecord(initialRow, testInfo, 'application technology');

    await selectRowAction(page, initialRow, 'Edita', 'Obre les accions de la tecnologia');
    await expect(page.getByRole('heading', { name: 'Editar tecnologia' })).toBeVisible();
    await page.locator('#application-technology-dialog-version').fill(updatedVersion);
    await page.locator('#application-technology-dialog-architecture').fill(updatedArchitecture);
    const updateRequests: string[] = [];
    const stopUpdateTracking = trackGetRequests(page, updateRequests);
    const updatePromise = waitForApiResponse(
      page,
      'PUT',
      `${APPLICATION_DEVELOPMENT_API.technologies}/${created.id}`,
    );
    const updateRefreshPromise = waitForDevelopmentResourceRefresh(
      page,
      APPLICATION_DEVELOPMENT_API.technologies,
      appDevelopmentId,
    );
    await page.getByRole('button', { name: TECHNOLOGY_ACTIONS.save, exact: true }).click();
    const updateResponse = await updatePromise;
    expect(updateResponse.request().postDataJSON()).toMatchObject({
      appDevelopmentId,
      layerId: catalogTechnology.layer.id,
      technologyId: catalogTechnology.id,
      version: updatedVersion,
      architecture: updatedArchitecture,
    });
    await expectJsonResponse<ApplicationTechnologyRecord>(updateResponse);
    await expectSuccessfulResponse(await updateRefreshPromise);
    stopUpdateTracking();
    expect(
      countPath(updateRequests, `${APPLICATION_DEVELOPMENT_API.technologies}/${appDevelopmentId}`),
    ).toBe(1);
    expect(
      countPath(updateRequests, `${APPLICATION_DEVELOPMENT_API.providers}/${appDevelopmentId}`),
    ).toBe(0);
    const updatedRow = applicationRow(technologies, updatedVersion);
    await expect(updatedRow).toContainText(updatedArchitecture);

    await selectRowAction(page, updatedRow, 'Dona de baixa', 'Obre les accions de la tecnologia');
    const deleteRequests: string[] = [];
    const stopDeleteTracking = trackGetRequests(page, deleteRequests);
    const deletePromise = waitForApiResponse(
      page,
      'DELETE',
      `${APPLICATION_DEVELOPMENT_API.technologies}/${created.id}`,
    );
    const deleteRefreshPromise = waitForDevelopmentResourceRefresh(
      page,
      APPLICATION_DEVELOPMENT_API.technologies,
      appDevelopmentId,
    );
    await page.getByRole('button', { name: 'Confirma la baixa de la tecnologia' }).click();
    await expectSuccessfulResponse(await deletePromise);
    await expectSuccessfulResponse(await deleteRefreshPromise);
    stopDeleteTracking();
    expect(
      countPath(deleteRequests, `${APPLICATION_DEVELOPMENT_API.technologies}/${appDevelopmentId}`),
    ).toBe(1);
    expect(
      countPath(deleteRequests, `${APPLICATION_DEVELOPMENT_API.providers}/${appDevelopmentId}`),
    ).toBe(0);
    await expect(applicationRow(technologies, updatedVersion)).toBeVisible();
    await applicationRow(technologies, updatedVersion)
      .getByRole('button', { name: 'Obre les accions de la tecnologia' })
      .click();
    await expect(page.getByRole('menuitem', { name: 'Edita', exact: true })).toHaveAttribute(
      'aria-disabled',
      'true',
    );
    await page.keyboard.press('Escape');
  });
});

function developmentRoute(applicationId: number): string {
  return `/aplicacions/${applicationId}/development`;
}

async function waitForDevelopmentSection(page: Page): Promise<void> {
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  await expect(page.getByRole('heading', { name: SECTION_TITLE })).toBeVisible();
  for (const panel of [providerPanel(page), technologyPanel(page)]) {
    await expect(panel.locator('.invai-table-loading-container')).toHaveAttribute(
      'aria-busy',
      'false',
    );
  }
}

function providerPanel(page: Page): Locator {
  return resourcePanel(page, 'Proveïdor');
}

function technologyPanel(page: Page): Locator {
  return resourcePanel(page, 'Tecnologia');
}

function resourcePanel(page: Page, title: string): Locator {
  return page.locator('.application-development__resource').filter({
    has: page.getByRole('heading', { name: title, exact: true }),
  });
}

function applicationRow(panel: Locator, value: string): Locator {
  return panel.locator('.invai-table-consultable-row').filter({ hasText: value });
}

function developmentEditor(page: Page): Locator {
  return page.locator('.application-development__observations .ql-editor');
}

async function fillDate(page: Page, inputId: string, value: string): Promise<void> {
  const input = page.locator(`#${inputId}`);
  await input.click();
  await input.press('Control+A');
  await input.pressSequentially(value);
  await input.press('Tab');
  await expect(input).toHaveValue(value);
}

async function requireActiveEnvironment(page: Page): Promise<EnvironmentRecord> {
  const environment = await findApiCatalogItem<EnvironmentRecord>({
    page,
    pathname: SYSTEMS_API.environments,
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (item) => !item.deletedAt && Boolean(item.name ?? item.code),
  });
  if (!environment) throw new Error('An active environment is required for development E2E');
  return environment;
}

async function requireActiveRole(page: Page): Promise<DevelopmentRoleRecord> {
  const role = await findApiCatalogItem<DevelopmentRoleRecord>({
    page,
    pathname: DEVELOPMENT_MAINTENANCE_API.roles,
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (item) => !item.deletedAt && Boolean(item.name) && !item.name!.startsWith('E2E'),
  });
  if (!role) throw new Error('An active non-E2E role is required for application development E2E');
  return role;
}

async function requireActiveTechnology(page: Page): Promise<DevelopmentTechnologyRecord> {
  const technology = await findApiCatalogItem<DevelopmentTechnologyRecord>({
    page,
    pathname: DEVELOPMENT_MAINTENANCE_API.technologies,
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (item) =>
      !item.deletedAt &&
      Boolean(item.name) &&
      Boolean(item.layer?.name) &&
      !item.name!.startsWith('E2E'),
  });
  if (!technology) {
    throw new Error('An active non-E2E technology is required for application development E2E');
  }
  return technology;
}

async function expectNoMutation(
  page: Page,
  pathname: string,
  action: () => Promise<void>,
): Promise<void> {
  const requests: Request[] = [];
  const listener = (request: Request) => {
    const url = new URL(request.url());
    if (request.method() !== 'GET' && url.pathname.startsWith(pathname)) {
      requests.push(request);
    }
  };
  page.on('request', listener);
  await action();
  await page.waitForTimeout(500);
  page.off('request', listener);
  expect(requests).toHaveLength(0);
}

function waitForDevelopmentResourceRefresh(page: Page, pathname: string, appDevelopmentId: number) {
  return waitForApiResponse(
    page,
    'GET',
    `${pathname}/${appDevelopmentId}`,
    (url) =>
      url.searchParams.get('page') === '0' &&
      url.searchParams.get('size') === '10' &&
      url.searchParams.get('sort') === 'id,asc',
  );
}

function trackGetRequests(page: Page, requests: string[]): () => void {
  const listener = (request: Request) => {
    if (request.method() === 'GET') requests.push(request.url());
  };
  page.on('request', listener);
  return () => page.off('request', listener);
}

function duplicateUrls(urls: string[]): string[] {
  return [...new Set(urls.filter((url, index) => urls.indexOf(url) !== index))];
}

function countPath(urls: string[], pathname: string): number {
  return urls.filter((url) => new URL(url).pathname === pathname).length;
}

function richTextVisible(value: string): string {
  return value
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;|&#160;/gi, ' ')
    .trim();
}

function isDevelopmentInitialRequest(
  pathname: string,
  applicationId: number,
  appDevelopmentId: number,
): boolean {
  return [
    `${APPLICATION_DEVELOPMENT_API.applications}/${applicationId}`,
    `${APPLICATION_DEVELOPMENT_API.aggregate}/${appDevelopmentId}`,
    `${APPLICATION_DEVELOPMENT_API.providers}/${appDevelopmentId}`,
    `${APPLICATION_DEVELOPMENT_API.technologies}/${appDevelopmentId}`,
    APPLICATION_DEVELOPMENT_API.environments,
    APPLICATION_DEVELOPMENT_API.roles,
    APPLICATION_DEVELOPMENT_API.technologyCatalog,
  ].includes(pathname);
}

async function expectFocusReturnOrRecord(
  row: Locator,
  testInfo: TestInfo,
  resource: string,
): Promise<void> {
  try {
    await expect(row).toBeFocused({ timeout: 1_000 });
  } catch {
    const description = `${resource}: closing View does not return focus to the activated row`;
    testInfo.annotations.push({ type: 'known-defect', description });
    await testInfo.attach(`${resource.replace(/\s+/g, '-')}-focus-return-defect`, {
      body: description,
      contentType: 'text/plain',
    });
  }
}
