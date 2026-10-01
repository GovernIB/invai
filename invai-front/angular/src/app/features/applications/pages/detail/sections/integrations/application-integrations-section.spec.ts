import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { ExternalSystemCatalogService } from '@features/external-systems/services/external-system-catalog.service';
import { TechnologyCatalogService } from '@features/technologies/services/technology-catalog.service';
import { SpringPage } from '@models/page.model';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import {
  ApplicationIntegrationConnectionOutput,
  ApplicationStatus,
} from '../../../../applications.model';
import { ApplicationIntegrationConnectionsService } from '../../../../services/application-integration-connections.service';
import { ApplicationIntegrationService } from '../../../../services/application-integration.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationIntegrationsSection } from './application-integrations-section';
import {
  APPLICATION_INTEGRATIONS_RESOLVE_KEY,
  ApplicationIntegrationsLoadResult,
} from './application-integrations-section.resolver';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const ROW: ApplicationIntegrationConnectionOutput = {
  id: 1,
  appIntegrationId: 13,
  application: null,
  externalSystem: { id: 2, name: 'Soffid', company: null, deletedAt: null },
  technology: null,
  username: 'u00004',
  requiredRoles: [{ id: 26, name: 'INV_ADMIN', description: null, system: null }],
  grantedRoles: [],
  rolesMismatch: true,
  deletedAt: null,
};

function page(content: ApplicationIntegrationConnectionOutput[]): SpringPage<ApplicationIntegrationConnectionOutput> {
  return { content, totalElements: content.length, number: 0 } as SpringPage<ApplicationIntegrationConnectionOutput>;
}

describe('ApplicationIntegrationsSection', () => {
  let fixture: ComponentFixture<ApplicationIntegrationsSection>;
  let detailState: ApplicationDetailState;
  let messageService: MessageService;
  let resolved: ApplicationIntegrationsLoadResult;
  const integrations = { getById: vi.fn(), refreshById: vi.fn(), create: vi.fn(), update: vi.fn() };
  const connections = { getPage: vi.fn(), clearCache: vi.fn(), create: vi.fn(), update: vi.fn(), delete: vi.fn() };
  const applications = { getById: vi.fn(), refreshById: vi.fn(), clearCache: vi.fn(), getPage: vi.fn() };

  const root = () => fixture.nativeElement as HTMLElement;
  const section = () =>
    fixture.componentInstance as unknown as {
      retry: () => void;
      startEditing: () => void;
      save: () => void;
    };

  async function create(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ApplicationIntegrationsSection],
      providers: [
        ApplicationDetailState,
        MessageService,
        { provide: ApplicationIntegrationService, useValue: integrations },
        { provide: ApplicationIntegrationConnectionsService, useValue: connections },
        { provide: ApplicationsService, useValue: applications },
        { provide: TechnologyCatalogService, useValue: { getActiveOptions: () => of([]) } },
        { provide: ExternalSystemCatalogService, useValue: { getActiveOptions: () => of([]) } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: { [APPLICATION_INTEGRATIONS_RESOLVE_KEY]: resolved } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationIntegrationsSection);
    detailState = TestBed.inject(ApplicationDetailState);
    messageService = TestBed.inject(MessageService);
    vi.spyOn(messageService, 'add');
    detailState.application.set({ id: '7', status: ApplicationStatus.ACTIVE } as never);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(() => {
    for (const mock of [
      ...Object.values(integrations),
      ...Object.values(connections),
      ...Object.values(applications),
    ]) {
      mock.mockReset();
    }
    connections.getPage.mockReturnValue(of(page([ROW])));
    integrations.update.mockImplementation((id, input) => of({ id, ...input, deletedAt: null }));
    resolved = {
      applicationId: 7,
      appIntegrationId: 13,
      record: { id: 13, applicationId: 7, observation: '<p>Notes</p>', deletedAt: null },
      status: 'loaded',
      errorMessage: null,
      connectionsPage: page([ROW]),
      connectionsLoadFailed: false,
      connectionsErrorMessage: null,
    };
  });

  it('renders the resolved connections and observations without repeating the requests', async () => {
    await create();

    expect(connections.getPage).not.toHaveBeenCalled();
    expect(integrations.getById).not.toHaveBeenCalled();
    expect(root().querySelectorAll('app-application-integrations-table tbody tr')).toHaveLength(1);
    expect(root().querySelector('app-application-integrations-table')?.textContent).toContain('Soffid');
    expect(root().querySelector('p-editor')).toBeNull();
    expect(
      root().querySelector('.application-integrations__observations .invai-form-static-value')?.innerHTML,
    ).toContain('Notes');
    expect(root().querySelector('.application-integrations__connections-header p-button')).toBeNull();
  });

  it('enables adding connections only while the section is edited', async () => {
    await create();

    section().startEditing();
    fixture.detectChanges();

    const add = root().querySelector<HTMLButtonElement>('.application-integrations__connections-header button')!;
    expect(add.getAttribute('aria-label')).toBe('Afegeix una connexió');
    expect(add.disabled).toBe(false);
    expect(root().querySelector('p-editor')).not.toBeNull();
  });

  it('reports a Soffid listing failure inline and recovers on reload', async () => {
    resolved = {
      ...resolved,
      connectionsPage: null,
      connectionsLoadFailed: true,
      connectionsErrorMessage: "No s'ha pogut consultar els rols atorgats.",
    };
    await create();

    const alert = root().querySelector('.application-integrations__connections [role="alert"]');
    expect(alert?.textContent?.trim()).toBe("No s'ha pogut consultar els rols atorgats.");
    expect(messageService.add).not.toHaveBeenCalled();

    root().querySelector<HTMLButtonElement>('.application-integrations__connections .application-integrations__problem button')!.click();
    fixture.detectChanges();

    expect(connections.getPage).toHaveBeenCalledWith(expect.objectContaining({ appIntegrationId: 13 }));
    expect(root().querySelector('.application-integrations__connections [role="alert"]')).toBeNull();
    expect(root().querySelectorAll('app-application-integrations-table tbody tr')).toHaveLength(1);
  });

  it('shows an anchor failure inline, blocks editing and recovers on retry', async () => {
    resolved = { ...resolved, record: null, status: 'failed', connectionsPage: null };
    await create();

    expect(root().querySelector('[role="alert"]')?.textContent?.trim()).toBe(
      "No s'han pogut carregar les dades d'integració de l'aplicació. Torna-ho a provar.",
    );
    expect(root().querySelector('app-application-integrations-table')).toBeNull();

    section().startEditing();
    expect(detailState.isEditing('integrations')).toBe(false);
    expect(messageService.add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'info' }));

    applications.refreshById.mockReturnValue(of({ id: 7, appIntegrationId: 13 }));
    integrations.refreshById.mockReturnValue(of(resolved.record ?? { id: 13, applicationId: 7, observation: null, deletedAt: null }));
    section().retry();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(applications.refreshById).toHaveBeenCalledWith(7);
    expect(connections.clearCache).toHaveBeenCalled();
    expect(root().querySelector('[role="alert"]')).toBeNull();
    expect(root().querySelector('app-application-integrations-table')).not.toBeNull();
  });

  it('explains that the anchor must exist before adding connections', async () => {
    resolved = { ...resolved, appIntegrationId: null, record: null, status: 'absent', connectionsPage: null };
    await create();
    section().startEditing();
    fixture.detectChanges();

    expect(root().textContent).toContain(
      "Desa les observacions de la pestanya per crear el registre d'integració abans d'afegir connexions.",
    );
    expect(root().querySelector<HTMLButtonElement>('.application-integrations__connections-header button')?.disabled).toBe(true);
  });

  it('saves the observations and confirms it once', async () => {
    await create();
    section().startEditing();
    detailState.integrationsForm.controls.observations.setValue('<p>Noves notes</p>');
    detailState.integrationsForm.markAsDirty();

    section().save();
    fixture.detectChanges();

    expect(integrations.update).toHaveBeenCalledWith(13, { applicationId: 7, observation: '<p>Noves notes</p>' });
    expect(detailState.isEditing('integrations')).toBe(false);
    expect(messageService.add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }));
  });

  it('leaves structured save errors to the global dialog and reports forbidden saves', async () => {
    await create();
    section().startEditing();
    detailState.integrationsForm.markAsDirty();

    integrations.update.mockReturnValueOnce(
      throwError(() => new HttpErrorResponse({ status: 400, error: { error: 'Error', message: 'Invàlid' } })),
    );
    section().save();
    expect(messageService.add).not.toHaveBeenCalled();

    integrations.update.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 403 })));
    section().save();
    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'error',
        detail: "El teu perfil no té permisos per fer aquesta operació d'integracions.",
      }),
    );
  });
});
