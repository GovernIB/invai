import { By } from '@angular/platform-browser';
import { Select } from 'primeng/select';
import { HttpErrorResponse } from '@angular/common/http';
import { ApplicationAccessibilityService } from '../../../../services/application-accessibility.service';
import { ApplicationAccessibilityOutput } from '../../../../applications.model';
import { ApplicationAccessibilityResolvedData } from './application-accessibility-section.resolver';
import { ActivatedRoute } from '@angular/router';
import { LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { of, throwError } from 'rxjs';
import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
} from '@features/maintenances/accessibility/services/accessibility-resource.services';
import {
  APPLICATION_ACCESSIBILITY_MESSAGES,
  APPLICATION_ACCESSIBILITY_CATALOG_FORBIDDEN,
} from './application-accessibility-section.i18n';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';

import { Application, ApplicationStatus } from '../../../../applications.model';
import { ApplicationDevelopmentService } from '../../../../services/application-development.service';
import { ApplicationSystemDatabaseService } from '../../../../services/application-system-database.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationAccessibilitySection } from './application-accessibility-section';

describe('ApplicationAccessibilitySection', () => {
  let fixture: ComponentFixture<ApplicationAccessibilitySection>;
  let state: ApplicationDetailState;
  const classification = { getAll: vi.fn() };
  const compliance = { getAll: vi.fn() };
  const add = vi.fn();
  let resolved: ApplicationAccessibilityResolvedData;
  const record: ApplicationAccessibilityOutput = {
    id: 99,
    application: { id: 7 },
    classificationSegment: null,
    compliance: null,
    publicUrl: 'https://git.caib.es/invai',
    mobileApplication: true,
    mobileApplicationName: 'Aplicación móvil de pruebas',
    expireDate: '2026-02-12T00:00:00',
    nonAccessibleContent: null,
    observations: null,
    deletedAt: null,
  };
  const accessibility = { getById: vi.fn(), refreshById: vi.fn(), create: vi.fn(), update: vi.fn() };

  beforeEach(async () => {
    registerLocaleData(localeEs);
    add.mockClear();
    accessibility.update
      .mockReset()
      .mockImplementation((_id, input) => of({ ...record, observations: input.observations }));
    accessibility.create.mockReset();
    accessibility.getById.mockReset();
    accessibility.refreshById.mockReset();
    classification.getAll.mockReset().mockReturnValue(of({ content: [], totalPages: 1 }));
    compliance.getAll.mockReset().mockReturnValue(of({ content: [], totalPages: 1 }));
    resolved = {
      accessibility: { applicationId: 7, appAccessibilityId: 99, record, status: 'loaded' },
      classification: {
        items: [{ id: 17, name: 'Segment I', nameEs: 'Segmento I', deletedAt: null }],
        failed: false,
        forbidden: false,
      },
      compliance: {
        items: [{ id: 29, name: 'Totalment', nameEs: 'Totalmente', deletedAt: null }],
        failed: false,
        forbidden: false,
      },
    };
    await TestBed.configureTestingModule({
      imports: [ApplicationAccessibilitySection],
      providers: [
        ApplicationDetailState,
        { provide: ApplicationAccessibilityService, useValue: accessibility },
        { provide: MessageService, useValue: { add } },
        { provide: LOCALE_ID, useValue: 'es' },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: { applicationAccessibility: resolved } } },
        },
        { provide: ClassificationSegmentsService, useValue: classification },
        { provide: ComplianceSituationsService, useValue: compliance },
        {
          provide: ApplicationsService,
          useValue: {
            update: vi.fn(),
            reactivate: vi.fn(),
            delete: vi.fn(),
            toApplication: vi.fn(),
            refreshById: vi.fn(() => of({ id: 7, appAccessibilityId: 99 })),
          },
        },
        {
          provide: ApplicationDevelopmentService,
          useValue: { create: vi.fn(), update: vi.fn() },
        },
        {
          provide: ApplicationSystemDatabaseService,
          useValue: { create: vi.fn(), update: vi.fn() },
        },
      ],
    }).compileComponents();

    state = TestBed.inject(ApplicationDetailState);
    state.application.set({ id: '7', status: ApplicationStatus.ACTIVE } as Application);
    fixture = TestBed.createComponent(ApplicationAccessibilitySection);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('renders labelled values without inventing catalog selections', () => {
    const root = fixture.nativeElement as HTMLElement;
    const title = root.querySelector(
      '.application-detail-section-layout__title',
    ) as HTMLHeadingElement;
    const publicUrl = root.querySelector(
      '#application-accessibility-public-url',
    ) as HTMLInputElement;
    const link = root.querySelector(
      '.application-accessibility__external-link',
    ) as HTMLAnchorElement;

    expect(title.textContent?.trim()).toBe('Accessibilitat');
    expect(root.querySelectorAll('p-select')).toHaveLength(0);
    expect(root.querySelectorAll('p-editor')).toHaveLength(0);
    expect(root.textContent).toContain('No informat');
    expect(state.accessibilityForm.controls.classificationSegment.value).toBeNull();
    expect(state.accessibilityForm.controls.complianceStatus.value).toBeNull();
    expect(root.textContent).toContain('Aplicación móvil de pruebas');
    expect(root.textContent).toContain('12/02/2026');
    expect(publicUrl.readOnly).toBe(true);
    expect(publicUrl.disabled).toBe(false);
    expect(link.href).toContain('https://git.caib.es/invai');
    expect(link.target).toBe('_blank');
  });

  it('renders the controls in DOM and keyboard order while editing', () => {
    editButton().click();
    fixture.detectChanges();

    const root = fixture.nativeElement as HTMLElement;
    const ids = [...root.querySelectorAll('input[id]')].map((input) => input.id);

    expect(state.isEditing('accessibility')).toBe(true);
    expect(root.querySelectorAll('p-select')).toHaveLength(3);
    expect(root.querySelectorAll('p-datepicker')).toHaveLength(1);
    expect(root.querySelectorAll('p-editor')).toHaveLength(2);
    expect(ids).toContain('application-accessibility-public-url');
    expect(
      root
        .querySelector('#application-accessibility-classification')
        ?.getAttribute('aria-labelledby'),
    ).toBe('application-accessibility-classification-label');
    expect(
      (root.querySelector('#application-accessibility-public-url') as HTMLInputElement).readOnly,
    ).toBe(false);
  });

  it('connects an invalid public URL with its visible error', () => {
    state.startEditing('accessibility');
    state.accessibilityForm.controls.publicUrl.setValue('git.caib.es/invai');
    state.accessibilityForm.controls.publicUrl.markAsDirty();
    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector(
      '#application-accessibility-public-url',
    ) as HTMLInputElement;
    const error = fixture.nativeElement.querySelector(
      '#application-accessibility-public-url-error',
    ) as HTMLElement;

    expect(input.getAttribute('aria-invalid')).toBe('true');
    expect(input.getAttribute('aria-describedby')).toBe(error.id);
    expect(error.getAttribute('role')).toBe('alert');
  });

  it('makes the mobile application name genuinely unavailable when it does not apply', () => {
    state.startEditing('accessibility');
    state.accessibilityForm.controls.mobileApplication.setValue(false);
    fixture.detectChanges();

    const combobox = fixture.nativeElement.querySelector(
      '#application-accessibility-mobile-name',
    ) as HTMLElement;

    expect(state.accessibilityForm.controls.mobileApplicationName.disabled).toBe(true);
    expect(state.accessibilityForm.controls.mobileApplicationName.value).toBeNull();
    expect((combobox as HTMLInputElement).disabled).toBe(true);
    expect(combobox.getAttribute('aria-labelledby')).toBe(
      'application-accessibility-mobile-name-label',
    );
    expect(combobox.getAttribute('aria-describedby')).toBe(
      'application-accessibility-mobile-name-help',
    );
    expect(fixture.nativeElement.textContent).toContain(
      "Disponible quan l'aplicació mòbil està marcada com a «Sí».",
    );
  });

  it('labels the native Quill content element for each rich text field', () => {
    const component = fixture.componentInstance as unknown as {
      labelEditor: (event: { editor: { root: HTMLElement } }, labelId: string) => void;
    };
    const content = document.createElement('div');

    component.labelEditor(
      { editor: { root: content } },
      'application-accessibility-observations-label',
    );

    expect(content.getAttribute('aria-labelledby')).toBe(
      'application-accessibility-observations-label',
    );
  });

  it('persists a dirty draft and announces success only after the response', () => {
    const messageService = TestBed.inject(MessageService);
    const addSpy = vi.spyOn(messageService, 'add');
    state.startEditing('accessibility');
    state.accessibilityForm.controls.observations.setValue('<p>Revisat</p>');
    state.accessibilityForm.controls.observations.markAsDirty();
    fixture.detectChanges();

    const saveButton = [...fixture.nativeElement.querySelectorAll('button')].find(
      (button: HTMLButtonElement) => button.textContent?.includes('Desar'),
    ) as HTMLButtonElement;
    saveButton.click();

    expect(state.isEditing('accessibility')).toBe(false);
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'success',
      summary: expect.any(String),
      detail: expect.any(String),
    });
  });

  it('reports that documentation is still mocked', () => {
    const messageService = TestBed.inject(MessageService);
    const addSpy = vi.spyOn(messageService, 'add');
    const button = [...fixture.nativeElement.querySelectorAll('button')].find(
      (candidate: HTMLButtonElement) => candidate.textContent?.includes('Documentació'),
    ) as HTMLButtonElement;

    button.click();

    expect(addSpy).toHaveBeenCalledWith({
      severity: 'info',
      summary: 'Informació',
      detail: "La documentació d'accessibilitat encara no està implementada.",
    });
  });

  it('consumes resolved data without duplicate HTTP or provisional toasts', () => {
    fixture.detectChanges();
    expect(add).not.toHaveBeenCalled();
    expect(accessibility.getById).not.toHaveBeenCalled();
    expect(classification.getAll).not.toHaveBeenCalled();
    expect(compliance.getAll).not.toHaveBeenCalled();
    const component = fixture.componentInstance as unknown as {
      classificationOptions(): { value: number; label: string }[];
    };
    expect(component.classificationOptions()).toEqual([
      { value: 17, label: 'Segmento I', disabled: false },
    ]);
  });

  it('groups denied catalogs into a single permission toast and retries them', () => {
    fixture.destroy();
    resolved.classification = { items: [], failed: true, forbidden: true };
    resolved.compliance = { items: [], failed: true, forbidden: true };
    add.mockClear();
    fixture = TestBed.createComponent(ApplicationAccessibilitySection);
    fixture.detectChanges();
    expect(
      add.mock.calls.filter(
        ([message]) => message.detail === APPLICATION_ACCESSIBILITY_CATALOG_FORBIDDEN,
      ),
    ).toHaveLength(1);
    (fixture.componentInstance as unknown as { retryCatalogs(): void }).retryCatalogs();
    expect(classification.getAll).toHaveBeenCalledOnce();
    expect(compliance.getAll).toHaveBeenCalledOnce();
  });

  it('keeps a saved inactive selection without offering it as a new option', () => {
    fixture.destroy();
    resolved.accessibility = {
      ...resolved.accessibility,
      record: {
        ...record,
        classificationSegment: {
          id: 999,
          name: 'Antic',
          nameEs: 'Antiguo',
          deletedAt: '2026-01-01',
        },
      },
    };
    fixture = TestBed.createComponent(ApplicationAccessibilitySection);
    fixture.detectChanges();
    const component = fixture.componentInstance as unknown as {
      classificationOptions(): { value: number; label: string; disabled: boolean }[];
    };
    expect(component.classificationOptions()).toContainEqual({
      value: 999,
      label: 'Antiguo',
      disabled: true,
    });
    expect(state.accessibilityForm.controls.classificationSegment.value).toBe(999);
  });

  it('does not announce success for invalid input', () => {
    add.mockClear();
    state.startEditing('accessibility');
    state.accessibilityForm.controls.publicUrl.setValue('invalid');
    state.accessibilityForm.markAsDirty();
    (fixture.componentInstance as unknown as { save(): void }).save();
    expect(add).not.toHaveBeenCalled();
    expect(state.isEditing('accessibility')).toBe(true);
    expect(state.accessibilityForm.controls.publicUrl.touched).toBe(true);
  });

  it('restores a saved selection and announces the clearing limitation', () => {
    state.initializeAccessibility({
      ...resolved.accessibility,
      record: {
        ...record,
        compliance: { id: 29, name: 'Totalment', nameEs: 'Totalmente', deletedAt: null },
      },
    });
    state.startEditing('accessibility');
    state.accessibilityForm.controls.complianceStatus.setValue(null);
    expect(state.accessibilityForm.controls.complianceStatus.value).toBe(29);
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ detail: APPLICATION_ACCESSIBILITY_MESSAGES.clearBlocked }),
    );
  });

  it('restores the visible PrimeNG selection after its clear event completes', () => {
    state.initializeAccessibility({ ...resolved.accessibility, record: { ...record,
      compliance: { id: 29, name: 'Totalment', nameEs: 'Totalmente', deletedAt: null } } });
    state.startEditing('accessibility');
    fixture.detectChanges();
    const select = fixture.debugElement.queryAll(By.directive(Select))
      .find((element) => element.attributes['formControlName'] === 'complianceStatus')!.componentInstance as Select;
    select.clear(new Event('click'));
    fixture.detectChanges();
    expect(state.accessibilityForm.controls.complianceStatus.value).toBe(29);
    expect(fixture.nativeElement.querySelector('#application-accessibility-compliance').textContent).toContain('Totalmente');
  });

  it('keeps edits after forbidden saves and announces the specific limitation', () => {
    accessibility.update.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    state.startEditing('accessibility');
    state.accessibilityForm.controls.observations.setValue('Draft');
    state.accessibilityForm.markAsDirty();
    (fixture.componentInstance as unknown as { save(): void }).save();
    expect(state.isEditing('accessibility')).toBe(true);
    expect(state.accessibilityForm.controls.observations.value).toBe('Draft');
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ detail: APPLICATION_ACCESSIBILITY_MESSAGES.forbidden }),
    );
  });

  it('groups forbidden record and catalogs and allows a successful record retry', () => {
    fixture.destroy();
    resolved.accessibility = { ...resolved.accessibility, record: null, status: 'forbidden' };
    resolved.classification = { items: [], failed: true, forbidden: true };
    resolved.compliance = { items: [], failed: true, forbidden: true };
    add.mockClear();
    fixture = TestBed.createComponent(ApplicationAccessibilitySection);
    fixture.detectChanges();
    expect(add).toHaveBeenCalledTimes(1);
    editButton().click();
    expect(state.isEditing('accessibility')).toBe(false);
    accessibility.refreshById.mockReturnValue(of(record));
    (fixture.componentInstance as unknown as { retryAccessibility(): void }).retryAccessibility();
    expect(state.accessibilityStatus()).toBe('loaded');
    expect(accessibility.refreshById).toHaveBeenCalledWith(99);
    expect(accessibility.getById).not.toHaveBeenCalled();
  });

  function editButton(): HTMLButtonElement {
    return [...fixture.nativeElement.querySelectorAll('button')].find((button: HTMLButtonElement) =>
      button.textContent?.includes('Editar'),
    ) as HTMLButtonElement;
  }
});
