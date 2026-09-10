import { signal } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Select } from 'primeng/select';
import { of } from 'rxjs';

import { createApplicationSecurityForm } from '../../../../forms/application-security-form.factory';
import {
  ApplicationSecurityMeasuresService,
  ApplicationSecurityRisksService,
  ApplicationSecurityRolesService,
  ApplicationWebContextsService,
} from '../../../../services/application-security.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationSecuritySection } from './application-security-section';
import {
  APPLICATION_SECURITY_RESOLVE_KEY,
  ApplicationSecurityResolvedData,
} from './application-security-section.resolver';

const ENS_SELECT_IDS = [
  'application-security-overall-grade',
  'application-security-identity-provider',
  'application-security-ens-subject',
  'application-security-personal-data',
  'application-security-confidentiality',
  'application-security-integrity',
  'application-security-traceability',
  'application-security-availability',
  'application-security-authenticity',
] as const;

describe('ApplicationSecuritySection', () => {
  let fixture: ComponentFixture<ApplicationSecuritySection>;
  let state: ReturnType<typeof createState>;

  beforeEach(async () => {
    state = createState();

    await TestBed.configureTestingModule({
      imports: [ApplicationSecuritySection],
      providers: [
        MessageService,
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: resolvedData() } },
        },
        { provide: ApplicationDetailState, useValue: state },
        { provide: ApplicationSecurityRolesService, useValue: resourceServiceStub() },
        { provide: ApplicationWebContextsService, useValue: resourceServiceStub() },
        { provide: ApplicationSecurityRisksService, useValue: resourceServiceStub() },
        { provide: ApplicationSecurityMeasuresService, useValue: resourceServiceStub() },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationSecuritySection);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it(
    'keeps the ENS grade editable, omits small-catalog filters and preserves dialog filters',
    () => {
      state.editing.set(true);
      fixture.detectChanges();

      const overallGrade = selectById('application-security-overall-grade');
      const overallGradeTrigger = fixture.nativeElement.querySelector(
        '#application-security-overall-grade',
      ) as HTMLElement;
      expect(state.securityForm.controls.overallGradeId.enabled).toBe(true);
      expect(overallGradeTrigger.getAttribute('aria-disabled')).not.toBe('true');
      expect(overallGrade.ariaLabelledBy).toBe('application-security-overall-grade-label');

      for (const inputId of ENS_SELECT_IDS) {
        const select = selectById(inputId);
        expect(select.filter).toBeFalsy();
        expect(select.ariaFilterLabel).toBeFalsy();
      }

      harness().openCreateDialog('measure');
      fixture.detectChanges();

      for (const inputId of [
        'application-security-dialog-measure-type',
        'application-security-dialog-ens-requirement',
      ]) {
        const select = selectById(inputId);
        expect(select.filter).toBe(true);
        expect(select.ariaFilterLabel).toBeTruthy();
      }
    },
    15_000,
  );

  function selectById(inputId: string): Select {
    const select = fixture.debugElement
      .queryAll(By.directive(Select))
      .map(({ componentInstance }) => componentInstance as Select)
      .find((candidate) => candidate.inputId === inputId);
    expect(select).toBeTruthy();
    return select!;
  }

  function harness(): ApplicationSecuritySectionHarness {
    return fixture.componentInstance as unknown as ApplicationSecuritySectionHarness;
  }
});

interface ApplicationSecuritySectionHarness {
  openCreateDialog(kind: 'web-context' | 'risk' | 'measure'): void;
}

function createState() {
  const securityForm = createApplicationSecurityForm(new FormBuilder());
  const editing = signal(false);

  return {
    appSecurityId: signal<number | null>(9),
    canEdit: signal(true),
    editing,
    ensClassification: signal(null),
    ensClassificationLoadState: signal('ready'),
    securityForm,
    isEditing: vi.fn(() => editing()),
    startEditing: vi.fn(() => editing.set(true)),
    cancelEditing: vi.fn(() => editing.set(false)),
    save: vi.fn(() => of({ status: 'saved', section: 'security' as const })),
    initializeSecurity: vi.fn(),
  };
}

function resolvedData(): Record<string, ApplicationSecurityResolvedData> {
  return {
    [APPLICATION_SECURITY_RESOLVE_KEY]: {
      applicationId: 7,
      appSecurityId: 9,
      security: null,
      rolesPage: null,
      webContextsPage: null,
      classifications: [],
      risksPage: null,
      measuresPage: null,
      options: {
        securityLevels: [],
        identityProviders: [],
        ensSubjects: [],
        personalDataProcessing: [],
        webContexts: [],
        fields: [],
        measureTypes: [],
        ensRequirements: [],
      },
      failures: {
        security: false,
        roles: false,
        webContexts: false,
        classifications: false,
        risks: false,
        measures: false,
        securityLevels: false,
        identityProviders: false,
        ensSubjects: false,
        personalDataProcessing: false,
        webContextOptions: false,
        fields: false,
        measureTypes: false,
        ensRequirements: false,
      },
    },
  };
}

function resourceServiceStub() {
  return {
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    getPage: vi.fn(),
  };
}
