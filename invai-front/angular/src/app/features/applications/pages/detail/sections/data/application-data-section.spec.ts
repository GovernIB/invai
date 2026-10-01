import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { ApplicationDataOutput, ApplicationStatus } from '../../../../applications.model';
import { ApplicationDataService } from '../../../../services/application-data.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDataSection } from './application-data-section';
import {
  APPLICATION_DATA_RESOLVE_KEY,
  ApplicationDataLoadResult,
} from './application-data-section.resolver';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const RECORD: ApplicationDataOutput = {
  id: 5,
  application: { id: 7 },
  observation: '<p>Notes de dades</p>',
  openDataUrl: 'https://intranet.caib.es/appapi/externa/swagger.json',
  useOpenDataUrl: false,
  openData: [
    {
      path: '/api/open',
      method: 'GET',
      operation: { operationId: 'open', summary: 'Obertes', description: null, parameters: [] },
    },
  ],
  reuseUrl: 'https://reuse.example/openapi.json',
  useReuseUrl: true,
  reuse: [
    {
      path: '/api/reuse',
      method: 'GET',
      operation: { operationId: 'reuse', summary: 'Reutilitzables', description: null, parameters: [] },
    },
  ],
  deletedAt: null,
};

describe('ApplicationDataSection', () => {
  let fixture: ComponentFixture<ApplicationDataSection>;
  let detailState: ApplicationDetailState;
  let messageService: MessageService;
  let resolved: ApplicationDataLoadResult;
  const dataService = { getById: vi.fn(), refreshById: vi.fn(), update: vi.fn(), create: vi.fn() };
  const applications = { getById: vi.fn(), refreshById: vi.fn(), clearCache: vi.fn() };

  const root = () => fixture.nativeElement as HTMLElement;
  const section = () =>
    fixture.componentInstance as unknown as {
      retry: () => void;
      startEditing: () => void;
      save: () => void;
    };
  const tabs = () => [...root().querySelectorAll<HTMLElement>('[role="tab"]')];

  async function create(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ApplicationDataSection],
      providers: [
        ApplicationDetailState,
        MessageService,
        { provide: ApplicationDataService, useValue: dataService },
        { provide: ApplicationsService, useValue: applications },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: { [APPLICATION_DATA_RESOLVE_KEY]: resolved } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDataSection);
    detailState = TestBed.inject(ApplicationDetailState);
    messageService = TestBed.inject(MessageService);
    vi.spyOn(messageService, 'add');
    detailState.application.set({ id: '7', status: ApplicationStatus.ACTIVE } as never);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(() => {
    for (const mock of [...Object.values(dataService), ...Object.values(applications)]) mock.mockReset();
    resolved = { applicationId: 7, appDataId: 5, record: RECORD, status: 'loaded', errorMessage: null };
  });

  it('renders the resolved data without repeating the initial requests', async () => {
    await create();

    expect(dataService.getById).not.toHaveBeenCalled();
    expect(applications.getById).not.toHaveBeenCalled();
    expect(root().querySelector('[role="tablist"]')?.getAttribute('aria-label')).toBe(
      'Fonts de dades',
    );
    expect(tabs().map((tab) => tab.textContent?.trim())).toEqual(['Open Data', 'Reutilització']);
    expect(tabs()[0].getAttribute('aria-selected')).toBe('true');
    expect(root().textContent).toContain('/api/open');
    expect(root().querySelector('p-editor')).toBeNull();
    expect(
      root().querySelector('.application-data__observations .invai-form-static-value')?.innerHTML,
    ).toContain('Notes de dades');
  });

  it('switches between the Open Data and Reutilització sources', async () => {
    await create();

    tabs()[1].click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(detailState.dataActiveSource()).toBe('reuse');
    expect(tabs()[1].getAttribute('aria-selected')).toBe('true');
    const panel = root().querySelector(`#${tabs()[1].getAttribute('aria-controls')}`);
    expect(panel?.textContent).toContain('/api/reuse');
  });

  it('shows an unavailable document inline, blocks editing and recovers on retry', async () => {
    const message = "No s'ha pogut consultar el catàleg d'API REST externa.";
    resolved = { applicationId: 7, appDataId: 5, record: null, status: 'failed', errorMessage: message };
    await create();

    expect(root().querySelector('[role="alert"]')?.textContent?.trim()).toBe(message);
    expect(root().querySelector('[role="tablist"]')).toBeNull();

    section().startEditing();
    expect(detailState.isEditing('data')).toBe(false);
    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'info', detail: message }),
    );

    applications.refreshById.mockReturnValue(of({ id: 7, appDataId: 5 }));
    dataService.refreshById.mockReturnValue(of(RECORD));
    const retry = root().querySelector<HTMLButtonElement>('.application-data__problem button')!;
    retry.focus();
    section().retry();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(applications.refreshById).toHaveBeenCalledWith(7);
    expect(dataService.refreshById).toHaveBeenCalledWith(5);
    expect(root().querySelector('[role="alert"]')).toBeNull();
    expect(root().querySelector('[role="tablist"]')).not.toBeNull();
    expect(document.activeElement?.getAttribute('role')).toBe('tab');
  });

  it('keeps the available source and reports only the one the backend could not fetch', async () => {
    resolved = { ...resolved, record: { ...RECORD, reuse: null } };
    await create();

    expect(root().querySelector('[role="alert"]')).toBeNull();
    expect(root().textContent).toContain('/api/open');
    expect(root().querySelector('#application-data-open-data-unavailable')).toBeNull();

    applications.refreshById.mockReturnValue(of({ id: 7, appDataId: 5 }));
    dataService.refreshById.mockReturnValue(of(RECORD));
    tabs()[1].click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(root().querySelector('#application-data-reuse-unavailable')?.textContent?.trim()).toBe(
      "No s'ha pogut consultar el catàleg d'aquesta font en aquest moment. Torna-ho a provar.",
    );
    root()
      .querySelector<HTMLButtonElement>('#application-data-reuse-unavailable + p-button button')!
      .click();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(dataService.refreshById).toHaveBeenCalledWith(5);
    expect(root().querySelector('#application-data-reuse-unavailable')).toBeNull();
    expect(root().textContent).toContain('/api/reuse');
  });

  it('uses the generic message when the backend gives no detail', async () => {
    resolved = { applicationId: 7, appDataId: 5, record: null, status: 'failed', errorMessage: null };
    await create();

    expect(root().querySelector('[role="alert"]')?.textContent?.trim()).toBe(
      "No s'han pogut carregar les dades de l'aplicació. Torna-ho a provar.",
    );
  });

  it('saves edited data and confirms it once the endpoints are reloaded', async () => {
    dataService.update.mockImplementation((id, input) =>
      of({ ...RECORD, ...input, id, observation: input.observation, openData: null, reuse: null }),
    );
    dataService.refreshById.mockReturnValue(of(RECORD));
    await create();

    section().startEditing();
    fixture.detectChanges();
    expect(root().querySelector('p-editor')).not.toBeNull();
    expect(root().querySelectorAll('p-toggleswitch')).toHaveLength(2);

    detailState.dataForm.controls.reuseUrl.setValue('https://reuse.example/v2/openapi.json');
    detailState.dataForm.markAsDirty();
    section().save();
    fixture.detectChanges();

    expect(dataService.update).toHaveBeenCalledWith(
      5,
      expect.objectContaining({ reuseUrl: 'https://reuse.example/v2/openapi.json', useReuseUrl: true }),
    );
    expect(dataService.refreshById).toHaveBeenCalledWith(5);
    expect(detailState.isEditing('data')).toBe(false);
    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success' }),
    );
  });

  it('does not show endpoint tables before the anchor exists', async () => {
    resolved = { applicationId: 7, appDataId: null, record: null, status: 'absent', errorMessage: null };
    await create();

    expect(root().querySelector('[role="alert"]')).toBeNull();
    expect(root().querySelector('app-application-data-endpoints-table')).toBeNull();
    expect(root().textContent).toContain(
      'Els endpoints publicats es consultaran quan es desin les dades de la pestanya.',
    );
  });
});
