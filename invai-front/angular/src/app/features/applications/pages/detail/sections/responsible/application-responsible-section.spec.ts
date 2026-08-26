import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { of, Subject, throwError } from 'rxjs';

import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';

import {
  ApplicationAuthorizedOutput,
  ApplicationAssignedResponsibleOutput,
  ApplicationResponsibleOutput,
  ApplicationStatus,
} from '../../../../applications.model';
import { ApplicationAuthorizedService } from '../../../../services/application-authorized.service';
import { ApplicationAuthorizedFormGroup } from '../../../../forms/application-authorized-form.factory';
import { ApplicationAssignmentDeactivateFormGroup } from '../../../../forms/application-assignment-deactivate-form.factory';
import { ApplicationResponsibleFormGroup } from '../../../../forms/application-responsible-form.factory';
import { ApplicationDevelopmentService } from '../../../../services/application-development.service';
import { ApplicationResponsiblesService } from '../../../../services/application-responsibles.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationAssignmentClipboardService } from './application-assignment-clipboard.service';
import {
  ApplicationAssignmentTableAction,
  ApplicationResponsibleTableRow,
} from './application-assignment-tables';
import { ApplicationResponsibleSection } from './application-responsible-section';
import { APPLICATION_RESPONSIBLE_RESOLVE_KEY } from './application-responsible-section.resolver';

const EXTERNAL_PERSON = {
  id: 11,
  company: { id: 3, name: 'Plexus', deletedAt: null },
  firstName: 'Maria',
  lastName: 'Tur Roig',
  email: 'maria@example.org',
  personalCaib: false,
  deletedAt: null,
};
const CAIB_PERSON = {
  ...EXTERNAL_PERSON,
  id: 12,
  company: null,
  firstName: 'Joan',
  lastName: 'Serra',
  email: 'joan@caib.es',
  personalCaib: true,
};
const RESPONSIBLE_TYPES = [
  {
    id: 1,
    name: 'Responsable de la informació',
    nameEs: 'Responsable de la información',
    requiresPersonalCaib: false,
  },
  {
    id: 2,
    name: 'Responsable del servei',
    nameEs: 'Responsable del servicio',
    requiresPersonalCaib: true,
  },
  {
    id: 3,
    name: 'Responsable de sistemes',
    nameEs: 'Responsable de sistemas',
    requiresPersonalCaib: false,
  },
];
const AUTHORIZATION_TYPES = [
  { id: 1, name: 'Signar peticions', nameEs: 'Firmar peticiones', deletedAt: null },
  { id: 2, name: 'Accés als logs', nameEs: 'Acceso a los logs', deletedAt: null },
];
const RESPONSIBLES: ApplicationAssignedResponsibleOutput[] = [
  {
    id: 21,
    appResponsibleAuthorizedId: 91,
    person: EXTERNAL_PERSON,
    responsibleType: RESPONSIBLE_TYPES[0],
    jobTitle: null,
    observation: null,
    deletedAt: null,
  },
  {
    id: 22,
    appResponsibleAuthorizedId: 91,
    person: CAIB_PERSON,
    responsibleType: RESPONSIBLE_TYPES[1],
    jobTitle: 'Cap de servei',
    observation: null,
    deletedAt: null,
  },
];
const AUTHORIZED: ApplicationAuthorizedOutput[] = [
  {
    id: 31,
    appResponsibleAuthorizedId: 91,
    person: EXTERNAL_PERSON,
    authorizationTypes: AUTHORIZATION_TYPES,
    observation: null,
    deletedAt: null,
  },
];

describe('ApplicationResponsibleSection', () => {
  let fixture: ComponentFixture<ApplicationResponsibleSection>;
  let detailState: ApplicationDetailState;
  let messageService: MessageService;
  const copy = vi.fn(() => Promise.resolve());
  const responsiblesService = {
    getPage: vi.fn(() => of(page(RESPONSIBLES))),
    create: vi.fn(() => of({ ...RESPONSIBLES[0], id: 99 })),
    update: vi.fn(() => of(RESPONSIBLES[0])),
    deactivate: vi.fn(() => of(RESPONSIBLES[0])),
  };
  const authorizedService = {
    getPage: vi.fn(() => of(page(AUTHORIZED))),
    create: vi.fn(() => of({ ...AUTHORIZED[0], id: 99 })),
    update: vi.fn(() => of(AUTHORIZED[0])),
    deactivate: vi.fn(() => of(undefined)),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [ApplicationResponsibleSection],
      providers: [
        ApplicationDetailState,
        MessageService,
        { provide: ApplicationsService, useValue: {} },
        { provide: ApplicationDevelopmentService, useValue: {} },
        { provide: ApplicationResponsiblesService, useValue: responsiblesService },
        { provide: ApplicationAuthorizedService, useValue: authorizedService },
        { provide: ApplicationAssignmentClipboardService, useValue: { copy } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                [APPLICATION_RESPONSIBLE_RESOLVE_KEY]: {
                  anchorId: 91,
                  available: true,
                  responsiblesPage: page(RESPONSIBLES),
                  responsiblesLoadFailed: false,
                  authorizedPage: page(AUTHORIZED),
                  authorizedLoadFailed: false,
                  responsibleTypes: RESPONSIBLE_TYPES,
                  responsibleTypesLoadFailed: false,
                  companyOptions: [{ id: 3, label: 'Plexus' }],
                  companyOptionsLoadFailed: false,
                  people: [EXTERNAL_PERSON],
                  peopleLoadFailed: false,
                  authorizationTypes: AUTHORIZATION_TYPES,
                  authorizationTypesLoadFailed: false,
                },
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationResponsibleSection);
    detailState = TestBed.inject(ApplicationDetailState);
    messageService = TestBed.inject(MessageService);
    detailState.application.set({ id: '7', status: ApplicationStatus.ACTIVE } as never);
    fixture.detectChanges();
  });

  it('renders every fixed responsibility and marks an unassigned one as incomplete', () => {
    const headings = Array.from(fixture.nativeElement.querySelectorAll('h4')).map((element) =>
      (element as HTMLElement).textContent?.trim(),
    );
    expect(headings).toEqual(['Responsables', 'Autoritzats']);
    expect(fixture.nativeElement.textContent).toContain('Cap de servei');
    expect(fixture.nativeElement.textContent).toContain('Responsable de sistemes');
    expect(fixture.nativeElement.textContent).toContain('Incomplet');
    expect(fixture.nativeElement.textContent).toContain('Signar peticions, Accés als logs');
    expect(fixture.nativeElement.textContent).not.toContain('Històric');
    const divider = fixture.nativeElement.querySelector(
      '.application-responsible__divider',
    ) as HTMLElement;
    expect(divider.getAttribute('aria-hidden')).toBe('true');
  });

  it('only renders add controls and action columns while editing the section', () => {
    expect(findButton('Afegeix un responsable')).toBeNull();
    expect(findButton('Afegeix una persona autoritzada')).toBeNull();
    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(0);

    startEditing();

    expect(findButton('Afegeix un responsable')).not.toBeNull();
    expect(findButton('Afegeix una persona autoritzada')).not.toBeNull();
    expect(
      fixture.nativeElement.querySelectorAll('.invai-table-actions-column').length,
    ).toBeGreaterThan(0);
  });

  it('uses the shared primary outlined style for table toolbar actions', () => {
    const copyResponsibles = button('Copia la taula de responsables');
    const copyAuthorized = button('Copia la taula de persones autoritzades');
    startEditing();

    const toolbarButtons = [
      copyResponsibles,
      button('Afegeix un responsable'),
      copyAuthorized,
      button('Afegeix una persona autoritzada'),
    ];

    for (const toolbarButton of toolbarButtons) {
      expect(toolbarButton.classList).toContain('invai-table-toolbar-button');
      expect(toolbarButton.classList).toContain('p-button-outlined');
      expect(toolbarButton.classList).not.toContain('p-button-secondary');
    }
  });

  it('opens the add dialog and creates a responsible with an exact backend payload', () => {
    const add = vi.spyOn(messageService, 'add');
    startEditing();
    button('Afegeix un responsable').click();
    fixture.detectChanges();
    expect(
      fixture.nativeElement.querySelector('app-application-responsible-dialog'),
    ).not.toBeNull();
    const observation = fixture.nativeElement.querySelector(
      '#application-responsible-dialog-observation',
    ) as HTMLTextAreaElement;
    expect(observation).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('label[for="application-responsible-dialog-observation"]')
        .textContent,
    ).toContain('Observacions (Opcional)');

    const component = harness(fixture);
    component.responsibleForm.patchValue({
      responsibleTypeId: 3,
      companyId: 3,
      personId: 11,
    });
    component.submitResponsible();

    expect(responsiblesService.create).toHaveBeenCalledWith({
      appResponsibleAuthorizedId: 91,
      personId: 11,
      responsibleTypeId: 3,
      jobTitle: null,
      observation: null,
      personalCaib: false,
    });
    expect(responsiblesService.getPage).toHaveBeenCalledWith({
      appResponsibleAuthorizedId: 91,
      page: 0,
      size: 1000,
      sort: 'id,asc',
      statusId: 1,
    });
    expect(authorizedService.getPage).not.toHaveBeenCalled();
    expect(add).not.toHaveBeenCalledWith(expect.objectContaining({ severity: 'warn' }));
  });

  it('opens the real authorized dialog and creates an assignment', () => {
    const add = vi.spyOn(messageService, 'add');
    startEditing();
    const addAuthorized = button('Afegeix una persona autoritzada');
    expect(addAuthorized.disabled).toBe(false);
    addAuthorized.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-application-authorized-dialog')).not.toBeNull();
    const component = harness(fixture);
    expect(component.authorizedForm.controls.personalCaib.disabled).toBe(true);
    const caibToggle = fixture.nativeElement.querySelector(
      '#application-authorized-dialog-caib',
    ) as HTMLInputElement;
    expect(caibToggle.disabled || caibToggle.getAttribute('aria-disabled') === 'true').toBe(true);
    expect(caibToggle.getAttribute('aria-describedby')).toBe(
      'application-authorized-dialog-caib-unavailable',
    );
    expect(
      fixture.nativeElement.querySelector('#application-authorized-dialog-observation'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('label[for="application-authorized-dialog-observation"]')
        .textContent,
    ).toContain('Observacions (Opcional)');

    component.authorizedForm.patchValue({
      companyId: 3,
      personId: 11,
      authorizationTypeIds: [1, 2],
    });
    component.submitAuthorized();

    expect(authorizedService.create).toHaveBeenCalledWith({
      appResponsibleAuthorizedId: 91,
      personId: 11,
      authorizationTypeIds: [1, 2],
      observation: null,
      personalCaib: false,
    });
    expect(authorizedService.getPage).toHaveBeenCalledWith({
      appResponsibleAuthorizedId: 91,
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: 1,
    });
    expect(responsiblesService.getPage).not.toHaveBeenCalled();
    expect(add).not.toHaveBeenCalledWith(expect.objectContaining({ severity: 'warn' }));
  });

  it('persists trimmed observations when creating assignments', () => {
    const add = vi.spyOn(messageService, 'add');
    startEditing();
    button('Afegeix un responsable').click();
    const component = harness(fixture);
    component.responsibleForm.patchValue({
      responsibleTypeId: 3,
      companyId: 3,
      personId: 11,
      observation: '  Context del canvi  ',
    });

    component.submitResponsible();

    expect(responsiblesService.create).toHaveBeenCalledWith({
      appResponsibleAuthorizedId: 91,
      personId: 11,
      responsibleTypeId: 3,
      jobTitle: null,
      observation: 'Context del canvi',
      personalCaib: false,
    });

    button('Afegeix una persona autoritzada').click();
    component.authorizedForm.patchValue({
      companyId: 3,
      personId: 11,
      observation: 'Context de l’autorització',
      authorizationTypeIds: [1],
    });

    component.submitAuthorized();

    expect(authorizedService.create).toHaveBeenCalledWith({
      appResponsibleAuthorizedId: 91,
      personId: 11,
      authorizationTypeIds: [1],
      observation: 'Context de l’autorització',
      personalCaib: false,
    });
    expect(add).not.toHaveBeenCalledWith(expect.objectContaining({ severity: 'warn' }));
  });

  it('treats a backend placeholder as a vacant responsibility', () => {
    const component = harness(fixture);
    const placeholder: ApplicationResponsibleOutput = {
      id: null,
      appResponsibleAuthorizedId: 91,
      person: null,
      responsibleType: RESPONSIBLE_TYPES[2],
      jobTitle: null,
      observation: null,
      deletedAt: null,
    };
    component.responsibleAssignments.set([...RESPONSIBLES, placeholder]);
    component.rebuildResponsibleRows();
    expect(
      component.responsibles().items.find(({ responsibleType }) => responsibleType.id === 3)
        ?.assignment,
    ).toBeNull();
  });

  it('keeps assignment identities immutable while updating supported fields', () => {
    const component = harness(fixture);
    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: row(RESPONSIBLES[0]),
    });
    expect(component.responsibleDialogVisible()).toBe(false);

    button("Editar Responsables de l'aplicació").click();
    fixture.detectChanges();
    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: row(RESPONSIBLES[0]),
    });
    component.responsibleForm.controls.personalCaib.setValue(true);
    component.responsibleForm.patchValue({
      personId: 12,
      responsibleTypeId: 3,
      cargo: 'Cap de projecte',
    });
    component.submitResponsible();
    expect(responsiblesService.update).toHaveBeenCalledWith(21, {
      appResponsibleAuthorizedId: 91,
      personId: 11,
      responsibleTypeId: 1,
      jobTitle: null,
      observation: null,
      personalCaib: false,
    });

    component.onAuthorizedTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: AUTHORIZED[0],
    });
    component.authorizedForm.patchValue({
      personId: 12,
      observation: 'Revisat',
      authorizationTypeIds: [2],
    });
    component.submitAuthorized();
    expect(authorizedService.update).toHaveBeenCalledWith(31, {
      appResponsibleAuthorizedId: 91,
      personId: 11,
      authorizationTypeIds: [2],
      observation: 'Revisat',
      personalCaib: false,
    });
  });

  it('allows editing the job title only for an assigned CAIB responsible', () => {
    startEditing();
    const component = harness(fixture);
    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: row(RESPONSIBLES[1]),
    });
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('#application-responsible-dialog-caib'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-responsible-dialog-person'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-responsible-dialog-cargo'),
    ).not.toBeNull();
    expect(component.responsibleForm.controls.personalCaib.disabled).toBe(true);
    expect(component.responsibleForm.controls.personId.disabled).toBe(true);
    expect(component.responsibleForm.controls.cargo.enabled).toBe(true);

    component.responsibleForm.controls.cargo.setValue('  Cap de projecte  ');
    component.submitResponsible();

    expect(responsiblesService.update).toHaveBeenCalledWith(22, {
      appResponsibleAuthorizedId: 91,
      personId: 12,
      responsibleTypeId: 2,
      jobTitle: 'Cap de projecte',
      observation: null,
      personalCaib: true,
    });
  });

  it('keeps an existing CAIB authorized person editable without exposing a job title', () => {
    const caibAuthorized: ApplicationAuthorizedOutput = {
      ...AUTHORIZED[0],
      id: 32,
      person: CAIB_PERSON,
    };
    startEditing();
    const component = harness(fixture);
    component.onAuthorizedTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: caibAuthorized,
    });
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('#application-authorized-dialog-caib'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-authorized-dialog-person'),
    ).not.toBeNull();
    expect(fixture.nativeElement.querySelector('#application-authorized-dialog-cargo')).toBeNull();
    expect(component.authorizedForm.controls.personalCaib.disabled).toBe(true);
    expect(component.authorizedForm.controls.personId.disabled).toBe(true);
    expect(component.authorizedSelectablePeople()).toContainEqual(CAIB_PERSON);

    component.authorizedForm.patchValue({
      personId: 11,
      observation: '  Revisat  ',
      authorizationTypeIds: [2],
    });
    component.submitAuthorized();

    expect(authorizedService.update).toHaveBeenCalledWith(32, {
      appResponsibleAuthorizedId: 91,
      personId: 12,
      authorizationTypeIds: [2],
      observation: 'Revisat',
      personalCaib: true,
    });
  });

  it('persists a trimmed responsible observation while editing', () => {
    startEditing();
    const component = harness(fixture);
    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: row(RESPONSIBLES[0]),
    });
    component.responsibleForm.controls.observation.setValue('  Revisió pendent  ');

    component.submitResponsible();

    expect(responsiblesService.update).toHaveBeenCalledWith(21, {
      appResponsibleAuthorizedId: 91,
      personId: 11,
      responsibleTypeId: 1,
      jobTitle: null,
      observation: 'Revisió pendent',
      personalCaib: false,
    });
  });

  it('renders immutable assignment fields as disabled controls while editing', () => {
    startEditing();
    const component = harness(fixture);

    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: row(RESPONSIBLES[0]),
    });
    fixture.detectChanges();
    expect(
      fixture.nativeElement.querySelector('#application-responsible-dialog-type'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-responsible-dialog-caib'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-responsible-dialog-company'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-responsible-dialog-person'),
    ).not.toBeNull();
    expect(fixture.nativeElement.querySelector('#application-responsible-dialog-cargo')).toBeNull();
    expect(component.responsibleForm.controls.personalCaib.disabled).toBe(true);
    expect(component.responsibleForm.controls.responsibleTypeId.disabled).toBe(true);
    expect(component.responsibleForm.controls.companyId.disabled).toBe(true);
    expect(component.responsibleForm.controls.personId.disabled).toBe(true);
    expect(component.responsibleForm.controls.email.disabled).toBe(true);
    expect(component.responsibleForm.controls.cargo.disabled).toBe(true);
    expect(component.responsibleForm.controls.observation.enabled).toBe(true);
    for (const id of [
      'application-responsible-dialog-caib',
      'application-responsible-dialog-type',
      'application-responsible-dialog-company',
      'application-responsible-dialog-person',
    ]) {
      const control = fixture.nativeElement.querySelector(`#${id}`) as HTMLElement;
      expect(control.matches(':disabled') || control.getAttribute('aria-disabled') === 'true').toBe(
        true,
      );
    }
    const responsibleEmail = fixture.nativeElement.querySelector(
      '#application-responsible-dialog-email',
    ) as HTMLInputElement;
    expect(responsibleEmail.disabled).toBe(true);
    expect(responsibleEmail.readOnly).toBe(false);
    expect(
      fixture.nativeElement.querySelector('#application-responsible-dialog-observation'),
    ).not.toBeNull();
    component.closeResponsibleDialog();

    component.onAuthorizedTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: AUTHORIZED[0],
    });
    fixture.detectChanges();
    expect(
      fixture.nativeElement.querySelector('#application-authorized-dialog-caib'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-authorized-dialog-person'),
    ).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-authorized-dialog-company'),
    ).not.toBeNull();
    expect(component.authorizedForm.controls.personalCaib.disabled).toBe(true);
    expect(component.authorizedForm.controls.companyId.disabled).toBe(true);
    expect(component.authorizedForm.controls.personId.disabled).toBe(true);
    expect(component.authorizedForm.controls.email.disabled).toBe(true);
    expect(component.authorizedForm.controls.observation.enabled).toBe(true);
    expect(component.authorizedForm.controls.authorizationTypeIds.enabled).toBe(true);
    for (const id of [
      'application-authorized-dialog-caib',
      'application-authorized-dialog-company',
      'application-authorized-dialog-person',
    ]) {
      const control = fixture.nativeElement.querySelector(`#${id}`) as HTMLElement;
      expect(control.matches(':disabled') || control.getAttribute('aria-disabled') === 'true').toBe(
        true,
      );
    }
    const authorizedEmail = fixture.nativeElement.querySelector(
      '#application-authorized-dialog-email',
    ) as HTMLInputElement;
    expect(authorizedEmail.disabled).toBe(true);
    expect(authorizedEmail.readOnly).toBe(false);
    expect(fixture.nativeElement.querySelector('#application-authorized-dialog-cargo')).toBeNull();
    expect(
      fixture.nativeElement.querySelector('#application-authorized-dialog-observation'),
    ).not.toBeNull();
  });

  it('restores create availability after closing an edit dialog', () => {
    startEditing();
    const component = harness(fixture);
    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: row(RESPONSIBLES[0]),
    });
    component.closeResponsibleDialog();

    button('Afegeix un responsable').click();

    expect(component.responsibleForm.controls.personalCaib.disabled).toBe(true);
    expect(component.responsibleForm.controls.responsibleTypeId.enabled).toBe(true);
    expect(component.responsibleForm.controls.companyId.enabled).toBe(true);
    expect(component.responsibleForm.controls.personId.enabled).toBe(true);
    expect(component.responsibleForm.controls.email.disabled).toBe(true);
    expect(component.responsibleForm.controls.cargo.disabled).toBe(true);
    expect(component.responsibleForm.controls.observation.enabled).toBe(true);
  });

  it('deactivates an assigned responsible with a trimmed observation and refreshes only its table', () => {
    const add = vi.spyOn(messageService, 'add');
    responsiblesService.getPage.mockReturnValueOnce(of(page([RESPONSIBLES[1]])));
    button("Editar Responsables de l'aplicació").click();
    fixture.detectChanges();
    const component = harness(fixture);

    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Deactivate,
      params: row(RESPONSIBLES[0]),
    });
    fixture.detectChanges();

    expect(component.deactivateDialogVisible()).toBe(true);
    expect(component.deactivateMessage()).toContain('Maria Tur Roig');
    expect(component.deactivateMessage()).toContain('Responsable de la informació');

    component.deactivateForm.controls.observation.setValue('  Canvi de responsable  ');
    component.confirmDeactivate();

    expect(responsiblesService.deactivate).toHaveBeenCalledWith(21, {
      observation: 'Canvi de responsable',
    });
    expect(responsiblesService.getPage).toHaveBeenCalledOnce();
    expect(authorizedService.getPage).not.toHaveBeenCalled();
    expect(component.deactivateDialogVisible()).toBe(false);
    expect(
      component.responsibles().items.find(({ responsibleType }) => responsibleType.id === 1)
        ?.assignment,
    ).toBeNull();
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'success',
        detail: "El responsable s'ha donat de baixa correctament.",
      }),
    );
  });

  it('deactivates an authorized person with an observation and refreshes only its table', () => {
    authorizedService.getPage.mockReturnValueOnce(of(page([])));
    button("Editar Responsables de l'aplicació").click();
    fixture.detectChanges();
    const component = harness(fixture);

    component.onAuthorizedTableAction({
      action: ApplicationAssignmentTableAction.Deactivate,
      params: AUTHORIZED[0],
    });
    expect(component.deactivateMessage()).toContain('Signar peticions, Accés als logs');
    component.deactivateForm.controls.observation.setValue('Canvi de permisos');

    component.confirmDeactivate();

    expect(authorizedService.deactivate).toHaveBeenCalledWith(31, {
      observation: 'Canvi de permisos',
    });
    expect(authorizedService.getPage).toHaveBeenCalledOnce();
    expect(responsiblesService.getPage).not.toHaveBeenCalled();
    expect(component.authorized().items).toEqual([]);
  });

  it('keeps the wider primary dialog open and exposes an accessible error for blank observations', () => {
    button("Editar Responsables de l'aplicació").click();
    fixture.detectChanges();
    const component = harness(fixture);
    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Deactivate,
      params: row(RESPONSIBLES[0]),
    });
    fixture.detectChanges();

    const dialog = fixture.debugElement.query(By.directive(ConfirmationDialogComponent))
      .componentInstance as ConfirmationDialogComponent;
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    let textarea = fixture.nativeElement.querySelector(
      '#application-assignment-deactivate-observation',
    ) as HTMLTextAreaElement;

    expect(dialog.width()).toBe('32rem');
    expect(dialog.severity()).toBe('primary');
    expect(dialog.confirmOutlined()).toBe(false);
    expect(form.classList).toContain('mt-6');
    expect(form.classList).toContain('mb-2');
    expect(form.classList).toContain('gap-2');
    expect(textarea.rows).toBe(4);

    component.confirmDeactivate();
    component.deactivateForm.controls.observation.setValue('   ');
    component.confirmDeactivate();
    fixture.detectChanges();

    textarea = fixture.nativeElement.querySelector(
      '#application-assignment-deactivate-observation',
    ) as HTMLTextAreaElement;
    expect(responsiblesService.deactivate).not.toHaveBeenCalled();
    expect(component.deactivateDialogVisible()).toBe(true);
    expect(component.deactivateForm.controls.observation.touched).toBe(true);
    expect(textarea.getAttribute('aria-invalid')).toBe('true');
    expect(textarea.getAttribute('aria-describedby')).toBe(
      'application-assignment-deactivate-observation-error',
    );
    expect(
      fixture.nativeElement.querySelector('#application-assignment-deactivate-observation-error')
        .textContent,
    ).toContain('Les observacions són obligatòries.');
  });

  it('keeps the deactivation dialog and observation available after a request error', () => {
    const add = vi.spyOn(messageService, 'add');
    responsiblesService.deactivate.mockReturnValueOnce(
      throwError(() => new Error('network failure')),
    );
    button("Editar Responsables de l'aplicació").click();
    fixture.detectChanges();
    const component = harness(fixture);
    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Deactivate,
      params: row(RESPONSIBLES[0]),
    });
    component.deactivateForm.controls.observation.setValue('Conservar aquest text');

    component.confirmDeactivate();

    expect(component.deactivateDialogVisible()).toBe(true);
    expect(component.deactivateForm.controls.observation.value).toBe('Conservar aquest text');
    expect(responsiblesService.getPage).not.toHaveBeenCalled();
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'error',
        detail: "No s'ha pogut donar de baixa el responsable.",
      }),
    );
  });

  it('ignores repeated deactivation and close actions while the request is pending', () => {
    const request = new Subject<ApplicationAssignedResponsibleOutput>();
    responsiblesService.deactivate.mockReturnValueOnce(request.asObservable());
    button("Editar Responsables de l'aplicació").click();
    fixture.detectChanges();
    const component = harness(fixture);
    component.onResponsibleTableAction({
      action: ApplicationAssignmentTableAction.Deactivate,
      params: row(RESPONSIBLES[0]),
    });
    component.deactivateForm.controls.observation.setValue('Canvi de responsable');

    component.confirmDeactivate();
    component.confirmDeactivate();
    component.closeDeactivateDialog();

    expect(responsiblesService.deactivate).toHaveBeenCalledOnce();
    expect(component.deactivatePending()).toBe(true);
    expect(component.deactivateDialogVisible()).toBe(true);

    request.next(RESPONSIBLES[0]);
    request.complete();

    expect(component.deactivatePending()).toBe(false);
    expect(component.deactivateDialogVisible()).toBe(false);
  });

  it('keeps the dialog open and localizes forbidden deactivation errors', () => {
    const add = vi.spyOn(messageService, 'add');
    authorizedService.deactivate.mockReturnValueOnce(
      throwError(() => new HttpErrorResponse({ status: 403 })),
    );
    button("Editar Responsables de l'aplicació").click();
    fixture.detectChanges();
    const component = harness(fixture);
    component.onAuthorizedTableAction({
      action: ApplicationAssignmentTableAction.Deactivate,
      params: AUTHORIZED[0],
    });
    component.deactivateForm.controls.observation.setValue('Canvi de permisos');

    component.confirmDeactivate();

    expect(component.deactivateDialogVisible()).toBe(true);
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'error',
        detail: 'No tens permisos per fer aquesta operació.',
      }),
    );
  });

  it('blocks new CAIB assignments in the control and in stale programmatic submissions', () => {
    startEditing();
    button('Afegeix un responsable').click();
    const component = harness(fixture);
    component.responsibleForm.controls.companyId.setValue(3);
    expect(component.responsibleSelectablePeople()).toEqual([EXTERNAL_PERSON]);
    expect(component.responsibleForm.controls.personalCaib.disabled).toBe(true);
    fixture.detectChanges();
    const toggle = fixture.nativeElement.querySelector(
      '#application-responsible-dialog-caib',
    ) as HTMLInputElement;
    expect(toggle.disabled || toggle.getAttribute('aria-disabled') === 'true').toBe(true);
    expect(toggle.getAttribute('aria-describedby')).toBe(
      'application-responsible-dialog-caib-unavailable',
    );

    component.responsibleForm.patchValue({
      personalCaib: true,
      responsibleTypeId: 3,
      personId: 12,
    });
    component.submitResponsible();
    expect(responsiblesService.create).not.toHaveBeenCalled();

    component.closeResponsibleDialog();
    button('Afegeix una persona autoritzada').click();
    component.authorizedForm.patchValue({
      personalCaib: true,
      personId: 12,
      authorizationTypeIds: [1],
    });
    component.submitAuthorized();
    expect(authorizedService.create).not.toHaveBeenCalled();
  });

  it('disables CAIB-only responsibility options and rejects stale type selections', () => {
    startEditing();
    const component = harness(fixture);
    component.responsibleAssignments.set([RESPONSIBLES[0]]);
    component.rebuildResponsibleRows();
    fixture.detectChanges();

    button('Afegeix un responsable').click();

    expect(component.responsibleTypeOptions()).toContainEqual({
      id: 2,
      label: 'Responsable del servei',
      disabled: true,
    });
    expect(component.responsibleTypeOptions()).toContainEqual({
      id: 3,
      label: 'Responsable de sistemes',
      disabled: false,
    });

    component.responsibleForm.patchValue({
      responsibleTypeId: 2,
      companyId: 3,
      personId: 11,
    });
    component.submitResponsible();
    expect(responsiblesService.create).not.toHaveBeenCalled();
  });

  it('keeps currently associated inactive people and authorizations available in edit', () => {
    const inactivePerson = {
      ...EXTERNAL_PERSON,
      id: 77,
      deletedAt: '2026-01-01T00:00:00',
    };
    const inactiveAuthorization = {
      id: 88,
      name: 'Autorització antiga',
      nameEs: 'Autorización antigua',
      deletedAt: '2026-01-01T00:00:00',
    };
    const existing = {
      ...AUTHORIZED[0],
      person: inactivePerson,
      authorizationTypes: [inactiveAuthorization],
    };
    startEditing();
    const component = harness(fixture);

    component.onAuthorizedTableAction({
      action: ApplicationAssignmentTableAction.Edit,
      params: existing,
    });

    expect(component.authorizedSelectablePeople()).toContainEqual(inactivePerson);
    expect(component.authorizationTypeOptions()).toContainEqual({
      id: 88,
      label: 'Autorització antiga',
    });
  });

  it('disables the add control when the responsibility catalog is complete', () => {
    startEditing();
    const component = harness(fixture);
    component.responsibleAssignments.set([
      ...RESPONSIBLES,
      { ...RESPONSIBLES[0], id: 23, responsibleType: RESPONSIBLE_TYPES[2] },
    ]);
    component.rebuildResponsibleRows();
    fixture.detectChanges();

    const addResponsible = button('Afegeix un responsable');
    expect(addResponsible.disabled).toBe(true);
    expect(addResponsible.getAttribute('aria-disabled')).toBe('true');
  });

  it('explains and disables add when only CAIB responsibilities remain vacant', () => {
    startEditing();
    const component = harness(fixture);
    component.responsibleAssignments.set([
      RESPONSIBLES[0],
      { ...RESPONSIBLES[0], id: 23, responsibleType: RESPONSIBLE_TYPES[2] },
    ]);
    component.rebuildResponsibleRows();
    fixture.detectChanges();

    const addResponsible = button('Afegeix un responsable');
    expect(addResponsible.disabled).toBe(true);
    expect(addResponsible.getAttribute('aria-describedby')).toBe(
      'application-responsible-caib-unavailable',
    );
    expect(
      fixture.nativeElement.querySelector('#application-responsible-caib-unavailable').textContent,
    ).toContain('requereixen personal CAIB');
  });

  it('copies visible rows and keeps authorizations comma-separated in one CSV column', async () => {
    button('Copia la taula de responsables').click();
    await fixture.whenStable();
    expect(copy).toHaveBeenCalledWith(
      'Responsabilitat,Persona,Càrrec / Empresa\r\n' +
        'Responsable de la informació,Maria Tur Roig,Plexus\r\n' +
        'Responsable del servei,Joan Serra,Cap de servei\r\n' +
        'Responsable de sistemes,Incomplet,—',
    );

    button('Copia la taula de persones autoritzades').click();
    await fixture.whenStable();
    expect(copy).toHaveBeenCalledWith(
      'Persona,Autoritzat\r\nMaria Tur Roig,"Signar peticions, Accés als logs"',
    );
  });

  it('keeps active status and server sorting while paging authorized independently', () => {
    harness(fixture).onAuthorizedPage({
      first: 20,
      rows: 10,
      sortField: 'person.firstName',
      sortOrder: -1,
    });
    expect(authorizedService.getPage).toHaveBeenCalledWith({
      appResponsibleAuthorizedId: 91,
      page: 2,
      size: 10,
      sort: 'person.firstName,desc',
      statusId: 1,
    });
  });

  function button(ariaLabel: string): HTMLButtonElement {
    const result = findButton(ariaLabel);
    expect(result).not.toBeNull();
    return result!;
  }

  function findButton(ariaLabel: string): HTMLButtonElement | null {
    return fixture.nativeElement.querySelector(
      `button[aria-label="${ariaLabel}"]`,
    ) as HTMLButtonElement | null;
  }

  function startEditing(): void {
    button("Editar Responsables de l'aplicació").click();
    fixture.detectChanges();
  }
});

function row(assignment: ApplicationAssignedResponsibleOutput): ApplicationResponsibleTableRow {
  return {
    rowKey: `responsible-type-${assignment.responsibleType.id}`,
    responsibleType: assignment.responsibleType,
    assignment,
  };
}

interface SectionHarness {
  responsibleForm: ApplicationResponsibleFormGroup;
  authorizedForm: ApplicationAuthorizedFormGroup;
  deactivateForm: ApplicationAssignmentDeactivateFormGroup;
  responsibleSelectablePeople: () => (typeof EXTERNAL_PERSON)[];
  authorizedSelectablePeople: () => (typeof EXTERNAL_PERSON)[];
  responsibleTypeOptions: () => { id: number; label: string; disabled?: boolean }[];
  authorizationTypeOptions: () => { id: number; label: string }[];
  responsibleDialogVisible: () => boolean;
  deactivateDialogVisible: () => boolean;
  deactivatePending: () => boolean;
  deactivateMessage: () => string;
  responsibles: () => { items: ApplicationResponsibleTableRow[] };
  authorized: () => { items: ApplicationAuthorizedOutput[] };
  responsibleAssignments: { set(value: ApplicationResponsibleOutput[]): void };
  rebuildResponsibleRows(): void;
  closeResponsibleDialog(): void;
  submitResponsible(): void;
  submitAuthorized(): void;
  confirmDeactivate(): void;
  closeDeactivateDialog(): void;
  onResponsibleTableAction(event: {
    action: ApplicationAssignmentTableAction;
    params: ApplicationResponsibleTableRow;
  }): void;
  onAuthorizedTableAction(event: {
    action: ApplicationAssignmentTableAction;
    params: ApplicationAuthorizedOutput;
  }): void;
  onAuthorizedPage(event: {
    first: number;
    rows: number;
    sortField: string;
    sortOrder: number;
  }): void;
}

function harness(fixture: ComponentFixture<ApplicationResponsibleSection>): SectionHarness {
  return fixture.componentInstance as unknown as SectionHarness;
}

function page<T>(content: T[]) {
  return { content, totalElements: content.length } as never;
}
