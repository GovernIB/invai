import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

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

const OPEN_DATA_URL = 'https://intranet.caib.es/appapi/externa';
const REUSE_URL = 'https://intranet.caib.es/appapi/interna';

const RECORD: ApplicationDataOutput = {
  id: 5,
  application: { id: 7 },
  observation: '<p>Notes de dades</p>',
  openDataUrl: OPEN_DATA_URL,
  reuseUrl: REUSE_URL,
  deletedAt: null,
};

describe('ApplicationDataSection', () => {
  let fixture: ComponentFixture<ApplicationDataSection>;
  let detailState: ApplicationDetailState;
  let messageService: MessageService;
  let resolved: ApplicationDataLoadResult;
  const dataService = {
    getById: vi.fn(),
    refreshById: vi.fn(),
    update: vi.fn(),
    create: vi.fn(),
    updateUrl: vi.fn(),
    checkUrl: vi.fn(),
  };
  const applications = { getById: vi.fn(), refreshById: vi.fn(), clearCache: vi.fn() };

  const root = () => fixture.nativeElement as HTMLElement;
  const section = () =>
    fixture.componentInstance as unknown as {
      retry: () => void;
      startEditing: () => void;
      save: () => void;
    };
  const titles = () =>
    [...root().querySelectorAll<HTMLElement>('.application-data-url__title')].map((title) =>
      title.textContent?.trim(),
    );
  const panel = (prefix: string) =>
    root().querySelector<HTMLElement>(`section[aria-labelledby="${prefix}-title"]`)!;

  async function create(status = ApplicationStatus.ACTIVE): Promise<void> {
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
    detailState.application.set({ id: '7', status } as never);
    await render();
  }

  async function render(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(() => {
    for (const mock of [...Object.values(dataService), ...Object.values(applications)]) mock.mockReset();
    // The open data URL answers; the reuse one does not.
    dataService.checkUrl.mockImplementation((url: string) => of({ url, validUrl: url === OPEN_DATA_URL }));
    resolved = { applicationId: 7, appDataId: 5, record: RECORD, status: 'loaded', errorMessage: null };
  });

  it('shows both URLs as links opening in a new tab without repeating the initial requests', async () => {
    await create();

    expect(dataService.getById).not.toHaveBeenCalled();
    expect(applications.getById).not.toHaveBeenCalled();
    expect(dataService.checkUrl.mock.calls).toEqual([[OPEN_DATA_URL], [REUSE_URL]]);
    expect(root().querySelector('[role="tablist"]')).toBeNull();
    expect(titles()).toEqual(['Dades obertes', 'Reutilització']);

    const link = panel('application-data-open-data').querySelector<HTMLAnchorElement>('a')!;
    expect(link.getAttribute('href')).toBe(OPEN_DATA_URL);
    expect(link.target).toBe('_blank');
    expect(link.rel).toBe('noopener noreferrer');
    expect(link.textContent?.replace(/\s+/g, ' ').trim()).toBe(
      `${OPEN_DATA_URL} (s'obre en una pestanya nova)`,
    );
    expect(panel('application-data-reuse').querySelector('a')?.getAttribute('href')).toBe(REUSE_URL);

    expect(panel('application-data-open-data').querySelector('.application-data-url__warning')).toBeNull();
    expect(
      panel('application-data-reuse').querySelector('.application-data-url__warning')?.textContent?.trim(),
    ).toBe('Aquesta URL no respon. Edita les dades per indicar-ne una manualment.');
    expect(root().querySelector('p-checkbox')).toBeNull();
    expect(root().querySelector('p-editor')).toBeNull();
    expect(
      root().querySelector('.application-data__observations .invai-form-static-value')?.innerHTML,
    ).toContain('Notes de dades');
  });

  it('does not suggest editing when the application cannot be edited', async () => {
    await create(ApplicationStatus.INACTIVE);

    expect(
      panel('application-data-reuse').querySelector('.application-data-url__warning')?.textContent?.trim(),
    ).toBe('Aquesta URL no respon.');
  });

  it('shows a load failure inline, blocks editing and recovers on retry', async () => {
    const message = 'No se ha encontrado ningún anclaje de datos con el identificador especificado.';
    resolved = { applicationId: 7, appDataId: 5, record: null, status: 'failed', errorMessage: message };
    await create();

    expect(root().querySelector('[role="alert"]')?.textContent?.trim()).toBe(message);
    expect(root().querySelector('.application-data')).toBeNull();

    section().startEditing();
    expect(detailState.isEditing('data')).toBe(false);
    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'info', detail: message }),
    );

    applications.refreshById.mockReturnValue(of({ id: 7, appDataId: 5 }));
    dataService.refreshById.mockReturnValue(of(RECORD));
    root().querySelector<HTMLButtonElement>('.application-data__problem button')!.focus();
    section().retry();
    await render();

    expect(applications.refreshById).toHaveBeenCalledWith(7);
    expect(dataService.refreshById).toHaveBeenCalledWith(5);
    expect(root().querySelector('[role="alert"]')).toBeNull();
    expect(titles()).toEqual(['Dades obertes', 'Reutilització']);
    expect(document.activeElement?.id).toBe('application-data-open-data-title');
  });

  it('uses the generic message when the backend gives no detail', async () => {
    resolved = { applicationId: 7, appDataId: 5, record: null, status: 'failed', errorMessage: null };
    await create();

    expect(root().querySelector('[role="alert"]')?.textContent?.trim()).toBe(
      "No s'han pogut carregar les dades de l'aplicació. Torna-ho a provar.",
    );
  });

  it('offers a URL typed in by hand for each source while editing', async () => {
    await create();

    section().startEditing();
    await render();

    const checkbox = root().querySelector<HTMLInputElement>('#application-data-reuse-manual')!;
    expect(root().querySelectorAll('p-checkbox')).toHaveLength(2);
    expect(checkbox.checked).toBe(false);
    expect(root().querySelector('label[for="application-data-reuse-manual"]')?.textContent?.trim()).toBe(
      'Indicar la URL manualment',
    );
    expect(checkbox.getAttribute('aria-describedby')).toBe('application-data-reuse-warning');
    expect(
      panel('application-data-reuse').querySelector('.application-data-url__warning')?.textContent?.trim(),
    ).toBe('Aquesta URL no respon. Marca «Indicar la URL manualment» per indicar-ne una altra.');
    expect(root().querySelector('#application-data-reuse-url')).toBeNull();
    expect(root().querySelector('p-editor')).not.toBeNull();

    checkbox.click();
    await render();

    const input = root().querySelector<HTMLInputElement>('#application-data-reuse-url')!;
    expect(root().querySelector('label[for="application-data-reuse-url"]')?.textContent?.trim()).toBe(
      'Nova URL',
    );
    expect(input.getAttribute('aria-describedby')).toBe('application-data-reuse-url-help');
    expect(root().querySelector('#application-data-open-data-url')).toBeNull();
  });

  it('saves a URL typed in by hand and warns when it does not answer', async () => {
    dataService.updateUrl.mockReturnValue(of({ url: 'https://reuse.example/api', validUrl: false }));
    await create();

    section().startEditing();
    detailState.dataForm.patchValue({ manualReuseUrl: true, reuseUrl: 'https://reuse.example/api' });
    detailState.dataForm.markAsDirty();
    section().save();
    await render();

    expect(dataService.update).not.toHaveBeenCalled();
    expect(dataService.updateUrl).toHaveBeenCalledWith(5, 'reuse', 'https://reuse.example/api');
    expect(detailState.isEditing('data')).toBe(false);
    expect(messageService.add).toHaveBeenCalledWith({
      severity: 'warn',
      summary: 'Canvis desats',
      detail:
        "S'han desat les dades, però la URL de Reutilització no respon. Comprova-la o indica'n una altra.",
    });
    expect(panel('application-data-reuse').querySelector('a')?.getAttribute('href')).toBe(
      'https://reuse.example/api',
    );
  });

  it('confirms a save whose URLs answer', async () => {
    dataService.update.mockImplementation((id, input) =>
      of({ ...RECORD, id, observation: input.observation, openDataUrl: null, reuseUrl: null }),
    );
    await create();

    section().startEditing();
    detailState.dataForm.controls.observations.setValue('<p>Noves</p>');
    detailState.dataForm.markAsDirty();
    section().save();
    await render();

    expect(dataService.update).toHaveBeenCalledWith(5, { applicationId: 7, observation: '<p>Noves</p>' });
    expect(messageService.add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }));
    expect(panel('application-data-open-data').querySelector('a')?.getAttribute('href')).toBe(
      OPEN_DATA_URL,
    );
  });

  it('explains that the URLs will be known once an application without anchor is saved', async () => {
    resolved = { applicationId: 7, appDataId: null, record: null, status: 'absent', errorMessage: null };
    await create();

    expect(root().querySelector('[role="alert"]')).toBeNull();
    expect(root().querySelector('.application-data-url a')).toBeNull();
    expect(
      [...root().querySelectorAll('.application-data-url__current dd')].map((value) =>
        value.textContent?.trim(),
      ),
    ).toEqual([
      'Es coneixerà quan es desin les dades de la pestanya.',
      'Es coneixerà quan es desin les dades de la pestanya.',
    ]);
    expect(dataService.checkUrl).not.toHaveBeenCalled();
  });

  it('keeps one source usable when the URL of the other one cannot be checked', async () => {
    dataService.checkUrl.mockImplementation((url: string) =>
      url === OPEN_DATA_URL ? of({ url, validUrl: true }) : throwError(() => new Error('offline')),
    );
    await create();

    expect(panel('application-data-open-data').querySelector('.application-data-url__warning')).toBeNull();
    expect(panel('application-data-reuse').querySelector('a')?.getAttribute('href')).toBe(REUSE_URL);
    expect(
      panel('application-data-reuse').querySelector('.application-data-url__warning')?.textContent?.trim(),
    ).toBe("No s'ha pogut comprovar la URL d'aquesta font. Edita les dades per indicar-ne una manualment.");
  });
});
