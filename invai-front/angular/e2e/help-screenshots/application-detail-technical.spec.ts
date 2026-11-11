import AxeBuilder from '@axe-core/playwright';
import type { Page } from '@playwright/test';

import { APPLICATION_DEVELOPMENT_API } from '../support/application-development';
import { SECURITY_API } from '../support/application-security';
import { expect, test } from '../support/auth.fixture';
import { CleanupRegistry } from '../support/cleanup-registry';
import { uniqueToken } from '../support/data';
import { apiUrl, expectApiJsonResponse } from '../support/http';
import { helpDialog, openApp, saveHelpImage, uiText } from './capture';
import { APPLICATION_API_PATH, createExampleApplication, findCatalogs } from './example-applications';

interface CatalogItem {
  id: number;
  name: string;
  deletedAt?: string | null;
  layer?: { id: number };
}

interface TechnicalAnchors {
  appDevelopmentId: number;
  appAccessibilityId: number;
  appSecurityId: number;
}

// Read current reference data instead of fixing database identifiers in screenshots.
async function catalog(page: Page, resource: string, name?: string): Promise<CatalogItem> {
  const pathname = `/invaiback/${resource}`;
  const body = await expectApiJsonResponse<CatalogItem[] | { content: CatalogItem[] }>(
    await page.request.get(apiUrl(page, pathname), {
      params: { page: 0, size: 100, statusId: 1 },
    }),
    `GET ${pathname}`,
  );
  const items = Array.isArray(body) ? body : body.content;
  const item = items.find(
    (candidate) =>
      !candidate.deletedAt &&
      (name ? candidate.name === name : Boolean(candidate.name) && !candidate.name.startsWith('E2E')),
  );
  if (!item) {
    throw new Error(`${pathname} has no active reference record${name ? ` named ${name}` : ''}`);
  }
  return item;
}

async function update(page: Page, pathname: string, id: number, data: object): Promise<void> {
  expect(Number.isInteger(id) && id > 0, `${pathname} aggregate ID`).toBeTruthy();
  await expectApiJsonResponse(
    await page.request.put(apiUrl(page, `${pathname}/${id}`), { data }),
    `PUT ${pathname}/${id}`,
  );
}

// All created child records are deactivated before the example application, including on failure.
async function createResource(
  page: Page,
  cleanup: CleanupRegistry,
  pathname: string,
  data: object,
): Promise<void> {
  const record = await expectApiJsonResponse<{ id: number }>(
    await page.request.post(apiUrl(page, pathname), { data }),
    `POST ${pathname}`,
  );
  cleanup.register(`help example ${pathname}/${record.id}`, async () => {
    const response = await page.request.delete(apiUrl(page, `${pathname}/${record.id}`));
    expect(response.ok(), `DELETE ${pathname}/${record.id}`).toBeTruthy();
  });
}

test('the detail of an application: Desenvolupament, Accessibilitat and Seguretat', async ({
  page,
}, testInfo) => {
  test.setTimeout(180_000);
  const cleanup = new CleanupRegistry(testInfo);
  try {
    const locale = await openApp(page);
    const text = (ca: string, es: string) => (locale === 'ca' ? ca : es);
    const token = uniqueToken(testInfo, 5);
    const application = await createExampleApplication(page, cleanup, {
      name: text('Portal de tràmits (exemple)', 'Portal de trámites (ejemplo)'),
      code: `AJ${token}`,
      catalogs: await findCatalogs(page),
    });
    const anchors = await expectApiJsonResponse<TechnicalAnchors>(
      await page.request.get(apiUrl(page, `${APPLICATION_API_PATH}/${application.id}`)),
      `GET ${APPLICATION_API_PATH}/${application.id}`,
    );

    const [
      environment, modality, standard, providerRole, technology, webContext, field,
      segment, compliance, level, identityProvider, subject, personalData, measureType, requirement,
    ] = await Promise.all([
        catalog(page, 'environment'),
        catalog(page, 'modality', 'Desenvolupament Intern'),
        catalog(page, 'standard-adaption', 'Conforme'),
        catalog(page, 'role'),
        catalog(page, 'technology'),
        catalog(page, 'web-context'),
        catalog(page, 'field'),
        catalog(page, 'classification-segment', 'Segment I'),
        catalog(page, 'compliance-situation', 'Parcialment'),
        catalog(page, 'security-level', 'Mitjà'),
        catalog(page, 'identity-provider'),
        catalog(page, 'ens-subject', 'Sí'),
        catalog(page, 'personal-data-processing'),
        catalog(page, 'security-measure-type'),
        catalog(page, 'ens-requirement'),
    ]);

    await update(page, APPLICATION_DEVELOPMENT_API.aggregate, anchors.appDevelopmentId, {
      applicationId: application.id,
      environmentId: environment.id,
      modalityId: modality.id,
      standardAdaptionId: standard.id,
      code: 'https://example.test/tramits',
      revisionDate: '2026-10-15T00:00:00',
      observation: `<p>${text('Revisió dels estàndards completada.', 'Revisión de los estándares completada.')}</p>`,
    });
    const providerName = text("Empresa d'exemple", 'Empresa de ejemplo');
    await createResource(page, cleanup, APPLICATION_DEVELOPMENT_API.providers, {
      appDevelopmentId: anchors.appDevelopmentId,
      companyName: providerName,
      roleId: providerRole.id,
      startDate: '2026-01-12T00:00:00',
      expireDate: null,
    });
    expect(technology.layer?.id, 'The technology belongs to a layer').toBeTruthy();
    await createResource(page, cleanup, APPLICATION_DEVELOPMENT_API.technologies, {
      appDevelopmentId: anchors.appDevelopmentId,
      technologyId: technology.id,
      layerId: technology.layer!.id,
      version: '1.0',
      architecture: '64 bits',
    });
    await createResource(page, cleanup, '/invaiback/application/development/web-context', {
      appDevelopmentId: anchors.appDevelopmentId,
      webContextId: webContext.id,
      fieldId: field.id,
      url: 'https://example.test/tramits',
      observation: text('Portal de tràmits de prova', 'Portal de trámites de prueba'),
    });

    const inaccessibleContent = text('Vídeos sense subtítols.', 'Vídeos sin subtítulos.');
    await update(page, '/invaiback/application/accessibility', anchors.appAccessibilityId, {
      applicationId: application.id,
      complianceId: compliance.id,
      classificationSegmentId: segment.id,
      publicUrl: 'https://example.test/tramits',
      mobileApplication: false,
      mobileApplicationName: null,
      nonAccessibleContent: `<p>${inaccessibleContent}</p>`,
      observations: `<p>${text('Revisió anual prevista.', 'Revisión anual prevista.')}</p>`,
      expireDate: '2027-10-15T00:00:00',
    });

    await update(page, SECURITY_API, anchors.appSecurityId, {
      applicationId: application.id,
      observation: `<p>${text('Revisió de seguretat completada.', 'Revisión de seguridad completada.')}</p>`,
    });
    await createResource(page, cleanup, `${SECURITY_API}/ens-classification`, {
      appSecurityId: anchors.appSecurityId,
      overallGradeId: level.id,
      identityProviderId: identityProvider.id,
      ensSubjectId: subject.id,
      personalDataProcessingId: personalData.id,
      approvalDate: '2026-10-15T00:00:00',
      confidentialityId: level.id,
      integrityId: level.id,
      traceabilityId: level.id,
      availabilityId: level.id,
      authenticityId: level.id,
    });
    const riskDescription = text('Interrupció del servei', 'Interrupción del servicio');
    const measureDescription = text('Còpies de seguretat diàries', 'Copias de seguridad diarias');
    await createResource(page, cleanup, `${SECURITY_API}/risk`, {
      appSecurityId: anchors.appSecurityId,
      levelId: level.id,
      fieldId: field.id,
      description: riskDescription,
    });
    await createResource(page, cleanup, `${SECURITY_API}/measure`, {
      appSecurityId: anchors.appSecurityId,
      typeId: measureType.id,
      ensRequirementId: requirement.id,
      description: measureDescription,
    });

    const route = `/${uiText(locale, 'aplicacions')}/${application.id}`;
    await page.goto(`${route}/development`);
    const development = page.locator('app-application-development-section');
    await expect(development.getByRole('cell', { name: providerName, exact: true })).toBeVisible();
    await expect(development.getByRole('cell', { name: '64 bits', exact: true })).toBeVisible();
    await expect(
      development.getByRole('cell', { name: 'https://example.test/tramits', exact: true }),
    ).toBeVisible();
    await saveHelpImage(page, development, locale, 'application-detail/imgs/development.webp');

    await page.goto(`${route}/accessibility`);
    const accessibility = page.locator('app-application-accessibility-section');
    await expect(accessibility.getByText(inaccessibleContent, { exact: true })).toBeVisible();
    await saveHelpImage(page, accessibility, locale, 'application-detail/imgs/accessibility.webp');

    await page.goto(`${route}/security`);
    const security = page.locator('app-application-security-section');
    await expect(security.getByRole('cell', { name: riskDescription, exact: true })).toBeVisible();
    await expect(security.getByRole('cell', { name: measureDescription, exact: true })).toBeVisible();
    await saveHelpImage(page, security, locale, 'application-detail/imgs/security.webp');

    // Check each localized topic in the real help, including its image and keyboard focus.
    for (const [tab, title] of [
      ['development', uiText(locale, 'Desenvolupament')],
      ['accessibility', uiText(locale, 'Accessibilitat')],
      ['security', uiText(locale, 'Seguretat')],
    ]) {
      await page.goto(`${route}/${tab}`);
      await expect(page.locator(`app-application-${tab}-section`)).toBeVisible();
      await expect(page.locator('.app-initial-loader')).toBeHidden();
      await page.keyboard.press('Control+k');
      const help = helpDialog(page);
      await expect(help.getByRole('heading', { level: 3, name: title, exact: true })).toBeVisible();
      await expect(help.locator('#help-modal-search')).toBeFocused();
      await expect(help.locator('markdown img')).toHaveAttribute(
        'src',
        new RegExp(`/help/${locale}/application-detail/imgs/${tab}\\.webp$`),
      );
      await expect
        .poll(() =>
          help.locator('markdown img').evaluate((image) => (image as HTMLImageElement).naturalWidth),
        )
        .toBeGreaterThan(0);

      const desktop = page.viewportSize()!;
      await page.setViewportSize({ width: 320, height: 800 });
      await expect
        .poll(() =>
          help.locator('.p-dialog-content').evaluate((content) => content.scrollWidth - content.clientWidth),
        )
        .toBeLessThanOrEqual(1);
      const results = await new AxeBuilder({ page })
        .include('.p-dialog')
        .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
        .analyze();
      expect(results.violations, `${locale}/${tab} help at 320 px`).toEqual([]);
      await page.setViewportSize(desktop);

      const zoomStyle = await page.addStyleTag({ content: 'html { font-size: 200% !important; }' });
      await expect
        .poll(() =>
          help.locator('.p-dialog-content').evaluate((content) => content.scrollWidth - content.clientWidth),
        )
        .toBeLessThanOrEqual(1);
      await zoomStyle.evaluate((style) => {
        style.parentNode?.removeChild(style);
      });
      await page.keyboard.press('Escape');
      await expect(help).toBeHidden();
    }
  } finally {
    await cleanup.runAll();
  }
});
