import { LOCALE_ID, WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { RoleTransferFormGroup } from '../../forms/responsible-forms.factory';
import {
  ResponsiblePerson,
  RoleAssignmentOutput,
  RoleAssignmentType,
  RoleTransferInput,
  RoleTransferSourceRequest,
} from '../../responsibles.model';
import { ResponsiblePeopleService } from '../../services/responsible-people.service';
import { RoleTransferService } from '../../services/role-transfer.service';
import { RoleTransfer } from './role-transfer';

const SOURCE = person(1, 'Maria Tur Roig');
const DESTINATION = person(2, 'Joan Serra');
const OTHER = person(3, 'Aina Vidal');

const ASSIGNMENTS: RoleAssignmentOutput[] = [
  {
    id: 10,
    type: RoleAssignmentType.RESPONSIBLE,
    applicationId: 100,
    applicationName: 'Aplicació A',
    responsibleType: {
      id: 1,
      name: 'Responsable de servei',
      nameEs: 'Responsable de servicio',
      requiresPersonalCaib: false,
    },
    authorizationTypes: null,
  },
  {
    id: 20,
    type: RoleAssignmentType.AUTHORIZED,
    applicationId: 100,
    applicationName: 'Aplicació A',
    responsibleType: null,
    authorizationTypes: [
      {
        id: 10,
        name: 'Signar peticions',
        nameEs: 'Firmar peticiones',
        deletedAt: null,
      },
    ],
  },
  {
    id: 21,
    type: RoleAssignmentType.AUTHORIZED,
    applicationId: 200,
    applicationName: 'Aplicació B',
    responsibleType: null,
    authorizationTypes: [
      {
        id: 11,
        name: 'Accés als logs',
        nameEs: 'Acceso a los logs',
        deletedAt: '2026-08-01T10:00:00',
      },
    ],
  },
];

interface AssignmentSubgroupTest {
  type: RoleAssignmentType;
  items: RoleAssignmentOutput[];
}

interface AssignmentGroupTest {
  applicationId: number;
  subgroups: AssignmentSubgroupTest[];
  items: RoleAssignmentOutput[];
}

interface RoleTransferTestApi {
  form: RoleTransferFormGroup;
  sourceAssignments: WritableSignal<RoleAssignmentOutput[]>;
  destinationOptions: WritableSignal<ResponsiblePerson[]>;
  preparedAssignments: WritableSignal<RoleAssignmentOutput[]>;
  sourceGroups: () => AssignmentGroupTest[];
  confirmationVisible: WritableSignal<boolean>;
  moveAll(fromSource: boolean): void;
  moveSubgroup(subgroup: AssignmentSubgroupTest, fromSource: boolean): void;
  openApplyConfirmation(): void;
  confirmAction(): void;
}

describe('RoleTransfer', () => {
  let fixture: ComponentFixture<RoleTransfer>;
  let component: RoleTransferTestApi;
  let peopleGetPage: ReturnType<typeof vi.fn>;
  let getAssignments: ReturnType<typeof vi.fn>;
  let apply: ReturnType<typeof vi.fn>;
  let addMessage: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    peopleGetPage = vi.fn(() => of(page([SOURCE, DESTINATION, OTHER])));
    getAssignments = vi.fn((personId: number) => of(personId === SOURCE.id ? ASSIGNMENTS : []));
    apply = vi.fn(() => of(undefined));
    addMessage = vi.fn();
    await configureComponent('ca');
  });

  it('starts without a source and loads assignments grouped by application and type', () => {
    expect(component.form.controls.sourcePersonId.value).toBeNull();

    selectSource();

    expect(getAssignments).toHaveBeenCalledWith(SOURCE.id);
    expect(peopleGetPage).toHaveBeenCalledWith(
      expect.objectContaining({ excludeId: SOURCE.id, size: 1000 }),
    );
    expect(component.destinationOptions().map(({ id }) => id)).toEqual([DESTINATION.id, OTHER.id]);
    expect(component.sourceGroups().map(({ applicationId }) => applicationId)).toEqual([100, 200]);
    expect(component.sourceGroups()[0].subgroups.map(({ type }) => type)).toEqual([
      RoleAssignmentType.RESPONSIBLE,
      RoleAssignmentType.AUTHORIZED,
    ]);
    fixture.detectChanges();
    expect(
      fixture.nativeElement.querySelectorAll('.role-transfer-options input[type="checkbox"]'),
    ).toHaveLength(3);
    expect(fixture.nativeElement.textContent).toContain('Aplicació A');
    expect(fixture.nativeElement.textContent).toContain('Responsable de servei');
    expect(fixture.nativeElement.textContent).toContain('Signar peticions');
    expect(fixture.nativeElement.textContent).toContain('Accés als logs');
  });

  it('renders and sorts the nested names using the Spanish locale', async () => {
    const localizedAssignments: RoleAssignmentOutput[] = [
      {
        ...ASSIGNMENTS[0],
        id: 11,
        responsibleType: {
          id: 1,
          name: 'Zulu',
          nameEs: 'Alfa',
          requiresPersonalCaib: false,
        },
      },
      {
        ...ASSIGNMENTS[0],
        id: 12,
        responsibleType: {
          id: 2,
          name: 'Alfa',
          nameEs: 'Zulu',
          requiresPersonalCaib: false,
        },
      },
      ASSIGNMENTS[1],
    ];
    getAssignments.mockImplementation((personId: number) =>
      of(personId === SOURCE.id ? localizedAssignments : []),
    );
    TestBed.resetTestingModule();
    await configureComponent('es');

    selectSource();

    const responsibilities = component.sourceGroups()[0].subgroups[0].items;
    expect(responsibilities.map(({ id }) => id)).toEqual([11, 12]);
    const labels = fixture.nativeElement.querySelectorAll(
      '.role-transfer-option',
    ) as NodeListOf<HTMLLabelElement>;
    expect([...labels].some((label) => label.textContent?.includes('Alfa'))).toBe(true);
    expect([...labels].some((label) => label.textContent?.includes('Zulu'))).toBe(true);
    expect([...labels].some((label) => label.textContent?.includes('Firmar peticiones'))).toBe(
      true,
    );
  });

  it('keeps the existing fallbacks for missing responsibility and authorization types', () => {
    getAssignments.mockImplementation((personId: number) =>
      of(
        personId === SOURCE.id
          ? [
              { ...ASSIGNMENTS[0], responsibleType: null },
              { ...ASSIGNMENTS[1], authorizationTypes: null },
              { ...ASSIGNMENTS[2], authorizationTypes: [] },
            ]
          : [],
      ),
    );

    selectSource();

    expect(fixture.nativeElement.textContent).toContain('Responsabilitat sense tipus');
    expect(
      fixture.nativeElement.textContent.match(/Sense autoritzacions detallades/g),
    ).toHaveLength(2);
  });

  it('moves a complete subgroup while preserving the other roles of the application', () => {
    selectSource();
    const authorizationGroup = component
      .sourceGroups()[0]
      .subgroups.find(({ type }) => type === RoleAssignmentType.AUTHORIZED)!;

    component.moveSubgroup(authorizationGroup, true);

    expect(component.preparedAssignments()).toEqual([ASSIGNMENTS[1]]);
    expect(component.sourceAssignments()).toEqual([ASSIGNMENTS[0], ASSIGNMENTS[2]]);
  });

  it('silently returns authorizations that conflict with the destination', () => {
    getAssignments.mockImplementation((personId: number) =>
      of(
        personId === SOURCE.id
          ? ASSIGNMENTS
          : [
              {
                ...ASSIGNMENTS[1],
                id: 99,
              },
            ],
      ),
    );
    selectSource();
    const authorizationGroup = component
      .sourceGroups()[0]
      .subgroups.find(({ type }) => type === RoleAssignmentType.AUTHORIZED)!;
    component.moveSubgroup(authorizationGroup, true);

    component.form.controls.destinationPersonId.setValue(DESTINATION.id);

    expect(component.preparedAssignments()).toEqual([]);
    expect(component.sourceAssignments()).toContainEqual(ASSIGNMENTS[1]);
  });

  it('confirms a transfer with the effective flat batch and resets the complete form', () => {
    selectSource();
    component.moveSubgroup(component.sourceGroups()[0].subgroups[0], true);
    component.form.controls.destinationPersonId.setValue(DESTINATION.id);

    component.openApplyConfirmation();
    expect(component.confirmationVisible()).toBe(true);
    component.confirmAction();

    expect(apply).toHaveBeenCalledWith({
      items: [{ id: ASSIGNMENTS[0].id, type: RoleAssignmentType.RESPONSIBLE }],
      toPersonId: DESTINATION.id,
      revoke: false,
    } satisfies RoleTransferInput);
    expect(component.form.getRawValue()).toEqual({
      sourcePersonId: null,
      destinationPersonId: null,
      revoke: false,
    });
    expect(component.preparedAssignments()).toEqual([]);
    expect(addMessage).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }));
  });

  it('revokes with an unset destination and confirms before discarding a staged source', () => {
    selectSource();
    component.moveAll(true);
    component.form.controls.revoke.setValue(true);
    component.openApplyConfirmation();
    component.confirmAction();

    expect(apply).toHaveBeenCalledWith(expect.objectContaining({ toPersonId: null, revoke: true }));

    selectSource();
    component.moveAll(true);
    component.form.controls.sourcePersonId.setValue(OTHER.id);
    expect(component.form.controls.sourcePersonId.value).toBe(SOURCE.id);
    expect(component.confirmationVisible()).toBe(true);
    component.confirmAction();
    expect(component.form.controls.sourcePersonId.value).toBe(OTHER.id);
    expect(getAssignments).toHaveBeenCalledWith(OTHER.id);
  });

  it('discards a prepared batch, selects an external source and moves focus to it', async () => {
    selectSource();
    component.moveAll(true);
    component.form.controls.destinationPersonId.setValue(DESTINATION.id);
    component.confirmationVisible.set(true);

    fixture.componentRef.setInput('sourceRequest', {
      requestId: 1,
      person: OTHER,
    } satisfies RoleTransferSourceRequest);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.form.controls.sourcePersonId.value).toBe(OTHER.id);
    expect(component.form.controls.destinationPersonId.value).toBeNull();
    expect(component.form.controls.revoke.value).toBe(false);
    expect(component.preparedAssignments()).toEqual([]);
    expect(component.confirmationVisible()).toBe(false);
    expect(getAssignments).toHaveBeenCalledWith(OTHER.id);
    expect(document.activeElement).toBe(
      fixture.nativeElement.querySelector('#role-transfer-source'),
    );
  });

  async function configureComponent(locale: string): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [RoleTransfer],
      providers: [
        { provide: LOCALE_ID, useValue: locale },
        { provide: ResponsiblePeopleService, useValue: { getPage: peopleGetPage } },
        { provide: RoleTransferService, useValue: { getAssignments, apply } },
        { provide: MessageService, useValue: { add: addMessage } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RoleTransfer);
    fixture.componentRef.setInput('initialPeople', [SOURCE, DESTINATION, OTHER]);
    fixture.detectChanges();
    component = fixture.componentInstance as unknown as RoleTransferTestApi;
  }

  function selectSource(): void {
    component.form.controls.sourcePersonId.setValue(SOURCE.id);
    fixture.detectChanges();
  }
});

function person(id: number, name: string): ResponsiblePerson {
  const [firstName, ...lastNameParts] = name.split(' ');
  return {
    id,
    firstName,
    lastName: lastNameParts.join(' '),
    email: `${id}@example.org`,
    personalCaib: false,
    company: null,
    deletedAt: null,
  };
}

function page<T>(content: T[]) {
  return { content, totalElements: content.length };
}
