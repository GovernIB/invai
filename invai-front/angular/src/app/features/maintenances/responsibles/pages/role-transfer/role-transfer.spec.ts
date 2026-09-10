import { LOCALE_ID, WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { RoleTransferFormGroup } from '../../forms/responsible-forms.factory';
import {
  ResponsiblePerson,
  ResponsiblePersonCombinedSearchOutput,
  RoleAssignmentOutput,
  RoleAssignmentType,
  RoleTransferDestinationOption,
  RoleTransferInput,
  RoleTransferPersonOption,
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
  sourceOptions: WritableSignal<RoleTransferPersonOption[]>;
  destinationOptions: WritableSignal<RoleTransferDestinationOption[]>;
  preparedAssignments: WritableSignal<RoleAssignmentOutput[]>;
  sourceGroups: () => AssignmentGroupTest[];
  confirmationVisible: WritableSignal<boolean>;
  moveAll(fromSource: boolean): void;
  moveSubgroup(subgroup: AssignmentSubgroupTest, fromSource: boolean): void;
  openApplyConfirmation(): void;
  confirmAction(): void;
  searchSources(query: string): void;
  searchDestinations(query: string): void;
}

describe('RoleTransfer', () => {
  let fixture: ComponentFixture<RoleTransfer>;
  let component: RoleTransferTestApi;
  let peopleSearchCombined: ReturnType<typeof vi.fn>;
  let getAssignments: ReturnType<typeof vi.fn>;
  let apply: ReturnType<typeof vi.fn>;
  let addMessage: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    peopleSearchCombined = vi.fn(() =>
      of({ database: page([DESTINATION, OTHER]), soffid: page([]) }),
    );
    getAssignments = vi.fn((personId: number) => of(personId === SOURCE.id ? ASSIGNMENTS : []));
    apply = vi.fn(() => of(undefined));
    addMessage = vi.fn();
    await configureComponent('ca', combinedPeople([SOURCE, DESTINATION, OTHER]));
  });

  it('starts without a source and loads assignments grouped by application and type', () => {
    expect(component.form.controls.sourcePerson.value).toBeNull();
    expect(component.sourceOptions().map(({ id }) => id)).toEqual([
      OTHER.id,
      DESTINATION.id,
      SOURCE.id,
    ]);

    component.searchSources('');

    selectSource();
    component.searchDestinations('');

    expect(getAssignments).toHaveBeenCalledWith(SOURCE.id);
    expect(peopleSearchCombined).not.toHaveBeenCalled();
    expect(component.destinationOptions().map(({ id }) => id)).toEqual([OTHER.id, DESTINATION.id]);
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

  it('lists only registered database sources, including local CAIB people', () => {
    const localCaib = {
      ...OTHER,
      email: 'aina.vidal@caib.es',
      personalCaib: true,
    };
    const duplicateSoffid = {
      id: null,
      company: null,
      firstName: 'Aina',
      lastName: 'Soffid',
      email: ' AINA.VIDAL@CAIB.ES ',
      personalCaib: true as const,
      deletedAt: null,
    };
    const soffidOnly = {
      ...duplicateSoffid,
      firstName: 'Pere',
      lastName: 'Ferrer',
      email: 'pere.ferrer@caib.es',
    };
    peopleSearchCombined.mockReturnValue(
      of({ database: page([SOURCE, localCaib]), soffid: page([duplicateSoffid, soffidOnly]) }),
    );

    component.searchSources('Aina');

    expect(peopleSearchCombined).toHaveBeenCalledWith(
      expect.objectContaining({ page: 0, size: 20, search: 'Aina' }),
    );
    expect(component.sourceOptions()).toEqual([
      sourceOption(localCaib),
      sourceOption(SOURCE),
    ]);
    expect(component.sourceOptions().some(({ email }) => email === soffidOnly.email)).toBe(false);
  });

  it('preserves the selected source while remote filter results refresh', () => {
    selectSource();
    peopleSearchCombined.mockReturnValue(
      of({ database: page([OTHER]), soffid: page([]) }),
    );

    component.searchSources('Aina');

    expect(component.sourceOptions()).toEqual([sourceOption(OTHER), sourceOption(SOURCE)]);
    expect(component.form.controls.sourcePerson.value).toEqual(sourceOption(SOURCE));
  });

  it('restores the initial page once after reopening a filtered source catalog', () => {
    component.searchSources('Aina');
    component.searchSources('');
    component.searchSources('');

    expect(peopleSearchCombined).toHaveBeenCalledTimes(2);
    expect(peopleSearchCombined).toHaveBeenNthCalledWith(
      1,
      expect.objectContaining({ search: 'Aina' }),
    );
    expect(peopleSearchCombined).toHaveBeenNthCalledWith(
      2,
      expect.objectContaining({ search: undefined }),
    );
  });

  it('retries an initial resolver failure when the source catalog opens', async () => {
    TestBed.resetTestingModule();
    await configureComponent('ca', null, true);

    component.searchSources('');

    expect(peopleSearchCombined).toHaveBeenCalledWith(
      expect.objectContaining({ search: undefined }),
    );
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

    component.form.controls.destinationPerson.setValue(destinationOption(DESTINATION));

    expect(component.preparedAssignments()).toEqual([]);
    expect(component.sourceAssignments()).toContainEqual(ASSIGNMENTS[1]);
  });

  it('confirms a transfer with the effective flat batch and resets the complete form', () => {
    selectSource();
    component.moveSubgroup(component.sourceGroups()[0].subgroups[0], true);
    component.form.controls.destinationPerson.setValue(destinationOption(DESTINATION));

    component.openApplyConfirmation();
    expect(component.confirmationVisible()).toBe(true);
    component.confirmAction();

    expect(apply).toHaveBeenCalledWith({
      items: [{ id: ASSIGNMENTS[0].id, type: RoleAssignmentType.RESPONSIBLE }],
      toPersonEmailAddress: DESTINATION.email,
      revoke: false,
    } satisfies RoleTransferInput);
    expect(component.form.getRawValue()).toEqual({
      sourcePerson: null,
      destinationPerson: null,
      revoke: false,
    });
    expect(component.preparedAssignments()).toEqual([]);
    expect(addMessage).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }));
  });

  it('transfers to a Soffid candidate by email without requesting assignments for a null id', () => {
    const soffidCandidate = {
      id: null,
      company: null,
      firstName: 'Pere',
      lastName: 'Ferrer',
      email: 'pere.ferrer@caib.es',
      personalCaib: true as const,
      deletedAt: null,
    };
    peopleSearchCombined.mockReturnValue(
      of({ database: page([]), soffid: page([soffidCandidate]) }),
    );
    selectSource();
    component.moveSubgroup(component.sourceGroups()[0].subgroups[0], true);
    component.searchDestinations('Pere');
    const destination = component.destinationOptions()[0];

    component.form.controls.destinationPerson.setValue(destination);
    component.openApplyConfirmation();
    component.confirmAction();

    expect(destination.id).toBeNull();
    expect(getAssignments).not.toHaveBeenCalledWith(null);
    expect(apply).toHaveBeenCalledWith({
      items: [{ id: ASSIGNMENTS[0].id, type: RoleAssignmentType.RESPONSIBLE }],
      toPersonEmailAddress: soffidCandidate.email,
      revoke: false,
    } satisfies RoleTransferInput);
    expect(peopleSearchCombined).toHaveBeenCalledWith(
      expect.objectContaining({ page: 0, size: 20, search: 'Pere' }),
    );
  });

  it('deduplicates combined results by normalized email and prefers the database person', () => {
    const duplicateSoffid = {
      id: null,
      company: null,
      firstName: 'Joan',
      lastName: 'Soffid',
      email: ` ${DESTINATION.email.toUpperCase()} `,
      personalCaib: true as const,
      deletedAt: null,
    };
    peopleSearchCombined.mockReturnValue(
      of({ database: page([SOURCE, DESTINATION]), soffid: page([duplicateSoffid]) }),
    );
    selectSource();

    component.searchDestinations('Joan');

    expect(component.destinationOptions()).toEqual([destinationOption(DESTINATION)]);
  });

  it('revokes with an unset destination and confirms before discarding a staged source', () => {
    selectSource();
    component.moveAll(true);
    component.form.controls.revoke.setValue(true);
    component.openApplyConfirmation();
    component.confirmAction();

    expect(apply).toHaveBeenCalledWith(
      expect.objectContaining({ toPersonEmailAddress: null, revoke: true }),
    );

    selectSource();
    component.moveAll(true);
    component.form.controls.sourcePerson.setValue(sourceOption(OTHER));
    expect(component.form.controls.sourcePerson.value).toEqual(sourceOption(SOURCE));
    expect(component.confirmationVisible()).toBe(true);
    component.confirmAction();
    expect(component.form.controls.sourcePerson.value).toEqual(sourceOption(OTHER));
    expect(getAssignments).toHaveBeenCalledWith(OTHER.id);
  });

  it('discards a prepared batch, selects an external source and moves focus to it', async () => {
    selectSource();
    component.moveAll(true);
    component.form.controls.destinationPerson.setValue(destinationOption(DESTINATION));
    component.confirmationVisible.set(true);

    fixture.componentRef.setInput('sourceRequest', {
      requestId: 1,
      person: OTHER,
    } satisfies RoleTransferSourceRequest);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.form.controls.sourcePerson.value).toEqual(sourceOption(OTHER));
    expect(component.form.controls.destinationPerson.value).toBeNull();
    expect(component.form.controls.revoke.value).toBe(false);
    expect(component.preparedAssignments()).toEqual([]);
    expect(component.confirmationVisible()).toBe(false);
    expect(getAssignments).toHaveBeenCalledWith(OTHER.id);
    expect(document.activeElement).toBe(
      fixture.nativeElement.querySelector('#role-transfer-source'),
    );
  });

  async function configureComponent(
    locale: string,
    initialPeopleSearch: ResponsiblePersonCombinedSearchOutput | null = null,
    initialPeopleSearchFailed = false,
  ): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [RoleTransfer],
      providers: [
        { provide: LOCALE_ID, useValue: locale },
        {
          provide: ResponsiblePeopleService,
          useValue: { searchCombined: peopleSearchCombined },
        },
        { provide: RoleTransferService, useValue: { getAssignments, apply } },
        { provide: MessageService, useValue: { add: addMessage } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RoleTransfer);
    fixture.componentRef.setInput('initialPeopleSearch', initialPeopleSearch);
    fixture.componentRef.setInput('initialPeopleSearchFailed', initialPeopleSearchFailed);
    fixture.detectChanges();
    component = fixture.componentInstance as unknown as RoleTransferTestApi;
  }

  function selectSource(): void {
    component.form.controls.sourcePerson.setValue(sourceOption(SOURCE));
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
  return { content, totalElements: content.length } as never;
}

function combinedPeople(
  database: ResponsiblePerson[],
): ResponsiblePersonCombinedSearchOutput {
  return { database: page(database), soffid: page([]) };
}

function destinationOption(
  value: ResponsiblePerson,
  source: RoleTransferDestinationOption['source'] = 'database',
): RoleTransferDestinationOption {
  return {
    id: value.id,
    firstName: value.firstName,
    lastName: value.lastName,
    email: value.email,
    label: `${value.firstName} ${value.lastName}`,
    source,
    disabled: false,
  };
}

function sourceOption(value: ResponsiblePerson): RoleTransferPersonOption {
  return destinationOption(value);
}
