import { WritableSignal, signal } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Editor } from 'primeng/editor';
import { Select } from 'primeng/select';
import { of, throwError } from 'rxjs';

import {
  ApplicationDevelopmentOutput,
  ApplicationProviderOutput,
  ApplicationTechnologyOutput,
  DevelopmentModality,
  DevelopmentStandardAdaption,
} from '../../../../applications.model';
import {
  ApplicationProviderTableAction,
  ApplicationTechnologyDialog,
  ApplicationTechnologyTableAction,
} from '../../../../components';
import {
  ApplicationProviderFormGroup,
  ApplicationTechnologyFormGroup,
  createApplicationDevelopmentForm,
} from '../../../../forms/application-development-form.factory';
import { ApplicationProvidersService } from '../../../../services/application-providers.service';
import { ApplicationTechnologiesService } from '../../../../services/application-technologies.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDevelopmentSection } from './application-development-section';
import {
  APPLICATION_DEVELOPMENT_RESOLVE_KEY,
  ApplicationDevelopmentResolvedData,
} from './application-development-section.resolver';

const DEVELOPMENT: ApplicationDevelopmentOutput = {
  id: 9,
  application: { id: 7 } as ApplicationDevelopmentOutput['application'],
  environment: {
    id: 3,
    code: 'PRO',
    name: 'Producció',
    nameEs: 'Producción',
  },
  modality: {
    id: DevelopmentModality.INTERNAL,
    name: 'Intern',
    nameEs: 'Interno',
  },
  code: 'https://git.caib.es/invai',
  standardAdaption: {
    id: DevelopmentStandardAdaption.CONFORMING,
    name: 'Conforme',
    nameEs: 'Conforme',
  },
  revisionDate: '2026-05-02T00:00:00',
  observation: '<p>Observació</p>',
  deletedAt: null,
};
const PROVIDER: ApplicationProviderOutput = {
  id: 4,
  companyName: 'Plexus SL',
  role: {
    id: 3,
    name: 'Desenvolupadora',
    nameEs: 'Desarrolladora',
    deletedAt: null,
  },
  startDate: '2026-05-02T00:00:00',
  expireDate: null,
  deletedAt: null,
};
const TECHNOLOGY: ApplicationTechnologyOutput = {
  id: 5,
  layer: { id: 1, name: 'Frontend', deletedAt: null },
  technology: {
    id: 2,
    name: 'Angular',
    layer: { id: 1, name: 'Frontend', deletedAt: null },
    deletedAt: null,
  },
  version: '21',
  architecture: 'Monolítica',
  deletedAt: null,
};

describe('ApplicationDevelopmentSection', () => {
  let fixture: ComponentFixture<ApplicationDevelopmentSection>;
  let state: ReturnType<typeof createState>;
  let createProvider: ReturnType<typeof vi.fn>;
  let updateProvider: ReturnType<typeof vi.fn>;
  let deleteProvider: ReturnType<typeof vi.fn>;
  let createTechnology: ReturnType<typeof vi.fn>;
  let updateTechnology: ReturnType<typeof vi.fn>;
  let deleteTechnology: ReturnType<typeof vi.fn>;
  let getProvidersPage: ReturnType<typeof vi.fn>;
  let getTechnologiesPage: ReturnType<typeof vi.fn>;
  let routeData: Record<string, ApplicationDevelopmentResolvedData>;

  beforeEach(async () => {
    state = createState();
    createProvider = vi.fn(() => of(PROVIDER));
    updateProvider = vi.fn(() => of(PROVIDER));
    deleteProvider = vi.fn(() => of(void 0));
    createTechnology = vi.fn(() => of(TECHNOLOGY));
    updateTechnology = vi.fn(() => of(TECHNOLOGY));
    deleteTechnology = vi.fn(() => of(void 0));
    getProvidersPage = vi.fn(() => of(page([PROVIDER])));
    getTechnologiesPage = vi.fn(() => of(page([TECHNOLOGY])));
    routeData = resolvedData();

    await TestBed.configureTestingModule({
      imports: [ApplicationDevelopmentSection],
      providers: [
        MessageService,
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: routeData } },
        },
        { provide: ApplicationDetailState, useValue: state },
        {
          provide: ApplicationProvidersService,
          useValue: {
            create: createProvider,
            update: updateProvider,
            delete: deleteProvider,
            getPage: getProvidersPage,
          },
        },
        {
          provide: ApplicationTechnologiesService,
          useValue: {
            create: createTechnology,
            update: updateTechnology,
            delete: deleteTechnology,
            getPage: getTechnologiesPage,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDevelopmentSection);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('initializes Development and its read-only resource pages', () => {
    expect(state.initializeDevelopment).toHaveBeenCalledWith(7, DEVELOPMENT);
    expect(state.initializeDevelopmentResources).toHaveBeenCalledWith(
      { items: [PROVIDER], total: 1 },
      { items: [TECHNOLOGY], total: 1 },
    );
  });

  it('reports HTTP load failures but accepts a missing development aggregate', () => {
    const messageService = TestBed.inject(MessageService);
    const addSpy = vi.spyOn(messageService, 'add');

    recreateWithResolvedData({
      ...routeData[APPLICATION_DEVELOPMENT_RESOLVE_KEY],
      development: null,
      developmentLoadFailed: false,
    });
    expect(addSpy).not.toHaveBeenCalled();

    recreateWithResolvedData({
      ...routeData[APPLICATION_DEVELOPMENT_RESOLVE_KEY],
      development: null,
      developmentLoadFailed: true,
    });
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'han pogut carregar les dades de desenvolupament.",
    });
  });

  it('renders section actions beside the standardized title', () => {
    const header = fixture.nativeElement.querySelector(
      '.application-detail-section-layout__header',
    ) as HTMLElement;
    const title = header.querySelector(
      '.application-detail-section-layout__title',
    ) as HTMLHeadingElement;

    expect(title.textContent?.trim()).toBe('Configuració del desenvolupament');
    expect(title.id).toBe('application-development-section-title');
    expect(header.textContent).toContain('Editar');
  });

  function recreateWithResolvedData(data: ApplicationDevelopmentResolvedData): void {
    fixture.destroy();
    routeData[APPLICATION_DEVELOPMENT_RESOLVE_KEY] = data;
    fixture = TestBed.createComponent(ApplicationDevelopmentSection);
    fixture.detectChanges();
  }

  it('filters maintenance catalogs but not local development options', () => {
    const environment = selectById('application-development-environment');
    const modality = selectById('application-development-modality');
    const standard = selectById('application-development-standard');

    expect(environment.filter).toBe(true);
    expect(environment.ariaFilterLabel).toBeTruthy();
    expect(modality.filter).toBeFalsy();
    expect(modality.ariaFilterLabel).toBeFalsy();
    expect(standard.filter).toBeFalsy();
    expect(standard.ariaFilterLabel).toBeFalsy();

    harness().providerDialogVisible.set(true);
    fixture.detectChanges();
    const role = selectById('application-provider-dialog-role');
    expect(role.filter).toBe(true);
    expect(role.ariaFilterLabel).toBeTruthy();

    harness().providerDialogVisible.set(false);
    harness().technologyDialogVisible.set(true);
    fixture.detectChanges();
    const technology = selectById('application-technology-dialog-technology');
    expect(technology.filter).toBe(true);
    expect(technology.ariaFilterLabel).toBeTruthy();
  });

  it('delegates editing and the single save to the page state', () => {
    const buttons = [
      ...fixture.nativeElement.querySelectorAll(
        '.application-detail-section-layout__header button',
      ),
    ] as HTMLButtonElement[];
    buttons.find((button) => button.textContent?.includes('Editar'))?.click();
    expect(state.startEditing).toHaveBeenCalledWith('development');

    state.editing.set(true);
    fixture.detectChanges();
    const saveButton = [
      ...fixture.nativeElement.querySelectorAll(
        '.application-detail-section-layout__header button',
      ),
    ].find((button: Element) => button.textContent?.includes('Desar')) as
      HTMLButtonElement | undefined;
    saveButton?.click();

    expect(state.save).toHaveBeenCalledWith('development');
  });

  it('shows a localized fallback when development save returns an unstructured error', () => {
    const messageService = TestBed.inject(MessageService);
    const addSpy = vi.spyOn(messageService, 'add');
    state.save.mockReturnValueOnce(throwError(() => new Error('NoSuchMessageException')));

    harness().save();

    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'han pogut desar els canvis.",
    });
  });

  it('only renders resource actions while editing and disables them when unavailable', () => {
    expect(fixture.nativeElement.textContent).toContain('Plexus SL');
    expect(fixture.nativeElement.textContent).toContain('Angular');
    expect(findAddButton('Afegeix un proveïdor')).toBeNull();
    expect(findAddButton('Afegeix una tecnologia')).toBeNull();
    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(0);

    state.editing.set(true);
    fixture.detectChanges();

    const providerButton = addButton('Afegeix un proveïdor');
    const technologyButton = addButton('Afegeix una tecnologia');
    expect(providerButton.disabled).toBe(false);
    expect(technologyButton.disabled).toBe(false);
    expect(providerButton.classList).not.toContain('p-button-secondary');
    expect(technologyButton.classList).not.toContain('p-button-secondary');
    expect(providerButton.classList).toContain('invai-table-toolbar-button');
    expect(technologyButton.classList).toContain('invai-table-toolbar-button');
    expect(
      fixture.nativeElement.querySelectorAll('.invai-table-actions-column').length,
    ).toBeGreaterThan(0);

    state.development.set(null);
    fixture.detectChanges();

    expect(addButton('Afegeix un proveïdor').disabled).toBe(true);
    expect(addButton('Afegeix una tecnologia').disabled).toBe(true);
    const rowActionButtons = fixture.nativeElement.querySelectorAll(
      '.invai-table-actions-column button',
    ) as NodeListOf<HTMLButtonElement>;
    expect(rowActionButtons.length).toBeGreaterThan(0);
    expect([...rowActionButtons].every((button) => button.disabled)).toBe(true);
  });

  it('offers provider editing from consultation and promotes Development edit mode', () => {
    const row = fixture.nativeElement.querySelector(
      'app-application-providers-table .invai-table-consultable-row',
    ) as HTMLTableRowElement;

    row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    fixture.detectChanges();

    const component = harness();
    expect(component.providerDialogVisible()).toBe(true);
    expect(component.providerDialogMode()).toBe('view');
    expect(component.providerDialogCanEdit()).toBe(true);
    expect(component.providerForm.disabled).toBe(true);
    expect(component.providerForm.getRawValue()).toEqual({
      companyName: 'Plexus SL',
      roleId: 3,
      startDate: new Date(2026, 4, 2),
      expireDate: null,
    });

    component.startProviderEdit();

    expect(state.startEditing).toHaveBeenCalledWith('development');
    expect(state.editing()).toBe(true);
    expect(component.providerDialogMode()).toBe('edit');
    expect(component.providerForm.enabled).toBe(true);
  });

  it('opens providers directly in edit while Development is editing', () => {
    state.editing.set(true);
    fixture.detectChanges();
    const component = harness();

    component.onProviderTableAction({
      action: ApplicationProviderTableAction.View,
      params: PROVIDER,
    });
    expect(component.providerDialogMode()).toBe('edit');
    expect(component.providerForm.enabled).toBe(true);

    component.providerForm.controls.companyName.setValue('Changed');
    component.cancelProviderEdit();
    expect(component.providerDialogMode()).toBe('view');
    expect(component.providerForm.disabled).toBe(true);
    expect(component.providerForm.controls.companyName.value).toBe('Plexus SL');

    component.startProviderEdit();
    component.providerForm.patchValue({ companyName: '  Plexus Nova  ' });
    component.submitProvider();

    expect(updateProvider).toHaveBeenCalledWith(4, {
      appDevelopmentId: 9,
      companyName: 'Plexus Nova',
      roleId: 3,
      startDate: '2026-05-02T00:00:00',
      expireDate: null,
    });
    expect(component.providerDialogVisible()).toBe(false);
    expect(getProvidersPage).toHaveBeenCalledWith({
      appDevelopmentId: 9,
      page: 0,
      size: 10,
      sort: 'id,asc',
    });
  });

  it('offers technology editing from consultation and promotes Development edit mode', () => {
    const component = harness();

    component.onTechnologyTableAction({
      action: ApplicationTechnologyTableAction.View,
      params: TECHNOLOGY,
    });
    expect(component.technologyDialogMode()).toBe('view');
    expect(component.technologyDialogCanEdit()).toBe(true);

    component.startTechnologyEdit();

    expect(state.startEditing).toHaveBeenCalledWith('development');
    expect(state.editing()).toBe(true);
    expect(component.technologyDialogMode()).toBe('edit');
  });

  it('opens technologies directly in edit and restores their snapshot on cancel', () => {
    state.editing.set(true);
    fixture.detectChanges();
    const component = harness();

    component.onTechnologyTableAction({
      action: ApplicationTechnologyTableAction.View,
      params: TECHNOLOGY,
    });
    expect(component.technologyDialogMode()).toBe('edit');
    expect(component.technologyForm.enabled).toBe(true);
    expect(component.technologyForm.controls.technologyId.enabled).toBe(true);
    expect(component.technologyForm.value).toEqual({
      layerId: 1,
      technologyId: 2,
      version: '21',
      architecture: 'Monolítica',
    });

    component.technologyForm.patchValue({
      technologyId: null,
      version: 'Changed',
      architecture: 'Changed',
    });

    component.cancelTechnologyEdit();
    expect(component.technologyDialogMode()).toBe('view');
    expect(component.technologyForm.enabled).toBe(true);
    expect(component.technologyForm.controls.technologyId.enabled).toBe(true);
    expect(component.technologyForm.value).toEqual({
      layerId: 1,
      technologyId: 2,
      version: '21',
      architecture: 'Monolítica',
    });
  });

  it('deactivates a technology after confirmation and refreshes only its table', () => {
    state.editing.set(true);
    fixture.detectChanges();
    const component = harness();

    component.onTechnologyTableAction({
      action: ApplicationTechnologyTableAction.Delete,
      params: TECHNOLOGY,
    });
    expect(component.technologyDeleteDialogVisible()).toBe(true);

    component.confirmTechnologyDelete();

    expect(deleteTechnology).toHaveBeenCalledWith(5);
    expect(component.technologyDeleteDialogVisible()).toBe(false);
    expect(getTechnologiesPage).toHaveBeenCalledWith({
      appDevelopmentId: 9,
      page: 0,
      size: 10,
      sort: 'id,asc',
    });
    expect(getProvidersPage).not.toHaveBeenCalled();
  });

  it('creates a provider with its required maintenance role and refreshes its table', () => {
    state.editing.set(true);
    fixture.detectChanges();
    addButton('Afegeix un proveïdor').click();

    const component = harness();
    component.providerForm.setValue({
      companyName: '  Plexus SL  ',
      roleId: 3,
      startDate: new Date(2026, 4, 2),
      expireDate: null,
    });
    component.submitProvider();

    expect(createProvider).toHaveBeenCalledWith({
      appDevelopmentId: 9,
      companyName: 'Plexus SL',
      roleId: 3,
      startDate: '2026-05-02T00:00:00',
      expireDate: null,
    });
    expect(getProvidersPage).toHaveBeenCalledWith({
      appDevelopmentId: 9,
      page: 0,
      size: 10,
      sort: 'id,asc',
    });
    expect(component.providerDialogVisible()).toBe(false);
  });

  it('derives the layer from the selected maintenance technology before creating', () => {
    state.editing.set(true);
    fixture.detectChanges();
    addButton('Afegeix una tecnologia').click();
    fixture.detectChanges();

    const component = harness();
    component.technologyForm.patchValue({
      version: ' 21 ',
      architecture: ' Monolítica ',
    });
    const dialog = fixture.debugElement.query(By.directive(ApplicationTechnologyDialog))
      .componentInstance as unknown as {
      onTechnologyChange(technologyId: number | null): void;
    };
    component.technologyForm.controls.technologyId.setValue(2);
    dialog.onTechnologyChange(2);
    fixture.detectChanges();

    expect(component.technologyForm.controls.layerId.value).toBe(1);
    expect(
      (
        fixture.nativeElement.querySelector(
          '#application-technology-dialog-layer',
        ) as HTMLInputElement
      ).value,
    ).toBe('Frontend');
    component.submitTechnology();

    expect(createTechnology).toHaveBeenCalledWith({
      appDevelopmentId: 9,
      layerId: 1,
      technologyId: 2,
      version: '21',
      architecture: 'Monolítica',
    });
    expect(getTechnologiesPage).toHaveBeenCalledWith({
      appDevelopmentId: 9,
      page: 0,
      size: 10,
      sort: 'id,asc',
    });
    expect(component.technologyDialogVisible()).toBe(false);
  });

  it('keeps an invalid or failed provider dialog open', () => {
    state.editing.set(true);
    fixture.detectChanges();
    addButton('Afegeix un proveïdor').click();
    fixture.detectChanges();
    const component = harness();

    component.submitProvider();
    expect(createProvider).not.toHaveBeenCalled();
    expect(component.providerForm.controls.companyName.touched).toBe(true);
    expect(component.providerForm.controls.roleId.touched).toBe(true);
    expect(component.providerDialogVisible()).toBe(true);
    fixture.detectChanges();

    const role = fixture.nativeElement.querySelector(
      '#application-provider-dialog-role',
    ) as HTMLElement;
    expect(role.getAttribute('aria-invalid')).toBe('true');
    expect(role.getAttribute('aria-describedby')).toBe('application-provider-dialog-role-error');
    expect(fixture.nativeElement.querySelector('.p-select.p-invalid')).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-provider-dialog-role-error'),
    ).not.toBeNull();

    component.providerForm.setValue({
      companyName: 'Plexus SL',
      roleId: null,
      startDate: new Date(2026, 4, 3),
      expireDate: new Date(2026, 4, 2),
    });
    component.providerForm.markAllAsTouched();
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('#application-provider-dialog-date-range-error'),
    ).not.toBeNull();
    expect(
      (
        fixture.nativeElement.querySelector(
          '#application-provider-dialog-start-date',
        ) as HTMLInputElement
      ).getAttribute('aria-describedby'),
    ).toBe('application-provider-dialog-date-range-error');

    createProvider.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    component.providerForm.patchValue({
      roleId: 3,
      startDate: null,
      expireDate: null,
    });
    component.submitProvider();

    expect(component.providerDialogVisible()).toBe(true);
  });

  it('renders a safe source link and accessible validation errors', () => {
    const sourceLink = fixture.nativeElement.querySelector(
      '.application-development__external-link',
    ) as HTMLAnchorElement;
    const sourceInput = fixture.nativeElement.querySelector(
      '#application-development-code',
    ) as HTMLInputElement;
    expect(sourceInput.classList).toContain('p-inputtext');
    expect(sourceLink.getAttribute('href')).toBe('https://git.caib.es/invai');
    expect(sourceLink.getAttribute('target')).toBe('_blank');
    expect(sourceLink.getAttribute('rel')).toBe('noopener noreferrer');

    state.developmentForm.enable({ emitEvent: false });
    state.developmentForm.controls.code.setValue('invalid-url');
    state.developmentForm.controls.code.markAsTouched();
    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector(
      '#application-development-code',
    ) as HTMLInputElement;
    expect(input.getAttribute('aria-invalid')).toBe('true');
    expect(input.getAttribute('aria-describedby')).toBe('application-development-code-error');
  });

  it('identifies Development observations as optional without required-error semantics', () => {
    state.developmentForm.enable({ emitEvent: false });
    state.developmentForm.controls.observation.setValue('<p><br></p><p>&nbsp;</p>');
    state.developmentForm.controls.observation.markAsTouched();
    fixture.detectChanges();

    const label = fixture.nativeElement.querySelector(
      'label[for="application-development-observation"]',
    ) as HTMLLabelElement;
    const editor = fixture.nativeElement.querySelector(
      '#application-development-observation',
    ) as HTMLElement;

    expect(label.textContent).toContain('Observacions (Opcional)');
    expect(editor.getAttribute('aria-invalid')).toBeNull();
    expect(editor.getAttribute('aria-describedby')).toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-development-observation-error'),
    ).toBeNull();
  });

  it('keeps Development observations read-only outside edit mode', async () => {
    const editor = fixture.debugElement.query(By.directive(Editor));
    const content = () =>
      fixture.nativeElement.querySelector(
        '.application-development__observations .ql-editor',
      ) as HTMLElement | null;

    await vi.waitFor(() => expect(content()).not.toBeNull());

    expect(editor.componentInstance.readonly).toBe(true);
    expect(content()?.getAttribute('contenteditable')).toBe('false');

    state.editing.set(true);
    fixture.detectChanges();

    expect(editor.componentInstance.readonly).toBe(false);
    expect(content()?.getAttribute('contenteditable')).toBe('true');

    state.editing.set(false);
    fixture.detectChanges();

    expect(editor.componentInstance.readonly).toBe(true);
    expect(content()?.getAttribute('contenteditable')).toBe('false');
  });

  it('disables source navigation when the URL is not configured', () => {
    state.developmentForm.enable({ emitEvent: false });
    state.developmentForm.controls.code.setValue('');
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('a.application-development__external-link'),
    ).toBeNull();

    const disabledSourceLink = fixture.nativeElement.querySelector(
      '.application-development__external-link--disabled',
    ) as HTMLSpanElement;
    expect(disabledSourceLink.tagName).toBe('SPAN');
    expect(disabledSourceLink.getAttribute('aria-disabled')).toBe('true');
  });

  function addButton(ariaLabel: string): HTMLButtonElement {
    const result = findAddButton(ariaLabel);
    expect(result).not.toBeNull();
    return result!;
  }

  function findAddButton(ariaLabel: string): HTMLButtonElement | null {
    return fixture.nativeElement.querySelector(
      `button[aria-label="${ariaLabel}"]`,
    ) as HTMLButtonElement | null;
  }

  function selectById(inputId: string): Select {
    return fixture.debugElement
      .queryAll(By.directive(Select))
      .map(({ componentInstance }) => componentInstance as Select)
      .find((select) => select.inputId === inputId)!;
  }

  function harness(): ApplicationDevelopmentSectionHarness {
    return fixture.componentInstance as unknown as ApplicationDevelopmentSectionHarness;
  }
});

interface ApplicationDevelopmentSectionHarness {
  providerForm: ApplicationProviderFormGroup;
  technologyForm: ApplicationTechnologyFormGroup;
  providerDialogVisible: WritableSignal<boolean>;
  technologyDialogVisible: WritableSignal<boolean>;
  providerDialogMode: WritableSignal<'create' | 'view' | 'edit'>;
  technologyDialogMode: WritableSignal<'create' | 'view' | 'edit'>;
  providerDialogCanEdit: () => boolean;
  technologyDialogCanEdit: () => boolean;
  technologyDeleteDialogVisible: WritableSignal<boolean>;
  save(): void;
  onProviderTableAction(event: {
    action: ApplicationProviderTableAction;
    params: ApplicationProviderOutput;
  }): void;
  onTechnologyTableAction(event: {
    action: ApplicationTechnologyTableAction;
    params: ApplicationTechnologyOutput;
  }): void;
  startProviderEdit(): void;
  cancelProviderEdit(): void;
  startTechnologyEdit(): void;
  cancelTechnologyEdit(): void;
  confirmTechnologyDelete(): void;
  submitProvider(): void;
  submitTechnology(): void;
}

function createState() {
  const form = createApplicationDevelopmentForm(new FormBuilder());
  form.setValue({
    environment: 3,
    modality: DevelopmentModality.INTERNAL,
    code: 'https://git.caib.es/invai',
    standardAdaption: DevelopmentStandardAdaption.CONFORMING,
    revisionDate: new Date(2026, 4, 2),
    observation: '<p>Observació</p>',
  });
  form.disable({ emitEvent: false });
  const development = signal<ApplicationDevelopmentOutput | null>(DEVELOPMENT);
  const providers = signal({ items: [PROVIDER], total: 1 });
  const technologies = signal({ items: [TECHNOLOGY], total: 1 });
  const editing = signal(false);

  return {
    application: signal({ id: '7' }),
    canEdit: signal(true),
    development,
    developmentForm: form,
    providers,
    technologies,
    editing,
    isEditing: vi.fn(() => editing()),
    startEditing: vi.fn(() => editing.set(true)),
    cancelEditing: vi.fn(() => editing.set(false)),
    save: vi.fn(() => of({ status: 'saved', section: 'development' as const })),
    initializeDevelopment: vi.fn(
      (_applicationId: number, value: ApplicationDevelopmentOutput | null) =>
        development.set(value),
    ),
    initializeDevelopmentResources: vi.fn(
      (
        providerPage: { items: ApplicationProviderOutput[]; total: number },
        technologyPage: { items: ApplicationTechnologyOutput[]; total: number },
      ) => {
        providers.set(providerPage);
        technologies.set(technologyPage);
      },
    ),
    setProviderPage: vi.fn((providerPage: { items: ApplicationProviderOutput[]; total: number }) =>
      providers.set(providerPage),
    ),
    setTechnologyPage: vi.fn(
      (technologyPage: { items: ApplicationTechnologyOutput[]; total: number }) =>
        technologies.set(technologyPage),
    ),
  };
}

function resolvedData(): Record<string, ApplicationDevelopmentResolvedData> {
  return {
    [APPLICATION_DEVELOPMENT_RESOLVE_KEY]: {
      applicationId: 7,
      appDevelopmentId: 9,
      development: DEVELOPMENT,
      developmentLoadFailed: false,
      providersPage: page([PROVIDER]),
      providersLoadFailed: false,
      technologiesPage: page([TECHNOLOGY]),
      technologiesLoadFailed: false,
      environmentOptions: [{ id: 3, code: 'PRO', label: 'Producció' }],
      environmentOptionsLoadFailed: false,
      roleOptions: [{ id: 3, label: 'Desenvolupament' }],
      roleOptionsLoadFailed: false,
      technologyOptions: [
        {
          id: 2,
          label: 'Angular',
          layerId: 1,
          layerLabel: 'Frontend',
        },
      ],
      technologyOptionsLoadFailed: false,
      modalityOptions: [{ id: DevelopmentModality.INTERNAL, name: 'Intern', nameEs: 'Interno' }],
      modalityOptionsLoadFailed: false,
      standardAdaptionOptions: [
        { id: DevelopmentStandardAdaption.CONFORMING, name: 'Conforme', nameEs: 'Conforme' },
      ],
      standardAdaptionOptionsLoadFailed: false,
    },
  };
}

function page<TItem>(content: TItem[]) {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: 1,
  };
}
