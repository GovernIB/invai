import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { MessageService } from 'primeng/api';

import { ApplicationStatus } from '../../../../applications.model';
import { ApplicationDevelopmentService } from '../../../../services/application-development.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { APPLICATION_OPTIONS_RESOLVE_KEY } from '../../../../resolvers/application-options.resolver';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationGeneralSection } from './application-general-section';

describe('ApplicationGeneralSection', () => {
  let fixture: ComponentFixture<ApplicationGeneralSection>;
  let detailState: ApplicationDetailState;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationGeneralSection],
      providers: [
        ApplicationDetailState,
        MessageService,
        provideRouter([]),
        { provide: ApplicationsService, useValue: {} },
        { provide: ApplicationDevelopmentService, useValue: {} },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                [APPLICATION_OPTIONS_RESOLVE_KEY]: {
                  options: {
                    categories: [],
                    informationSystems: [],
                    scopes: [],
                    commissions: [],
                    administrativeUnits: [],
                  },
                  loadFailed: false,
                },
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationGeneralSection);
    detailState = TestBed.inject(ApplicationDetailState);
    detailState.application.set({
      id: '7',
      status: ApplicationStatus.ACTIVE,
    } as never);
    fixture.detectChanges();
  });

  it('renders the standardized descriptive section title', () => {
    const title = fixture.nativeElement.querySelector(
      '.application-detail-section-layout__title',
    ) as HTMLHeadingElement;

    expect(title.textContent?.trim()).toBe("Dades generals de l'aplicació");
    expect(title.id).toBe('application-general-section-title');
  });

  it('shows the withdrawal action only while General is being edited', () => {
    const findWithdrawalButton = (): HTMLButtonElement | undefined =>
      Array.from(
        fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>,
      ).find((button) => button.textContent?.includes('Donar de baixa'));

    expect(findWithdrawalButton()).toBeUndefined();

    detailState.startEditing('general');
    fixture.detectChanges();

    expect(findWithdrawalButton()).toBeDefined();

    detailState.cancelEditing('general');
    fixture.detectChanges();

    expect(findWithdrawalButton()).toBeUndefined();
  });

  it('renders section editing actions and lifecycle controls after the form while editing', () => {
    detailState.startEditing('general');
    fixture.detectChanges();

    const header = fixture.nativeElement.querySelector(
      '.application-detail-section-layout__header',
    ) as HTMLElement;
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    const lifecycle = fixture.nativeElement.querySelector(
      '.application-general-section__lifecycle',
    ) as HTMLElement;

    expect(header.textContent).toContain('Cancel·lar');
    expect(lifecycle.textContent).toContain('Donar de baixa');
    expect(
      form.compareDocumentPosition(lifecycle) & Node.DOCUMENT_POSITION_FOLLOWING,
    ).toBeTruthy();
  });

  it('starts only General editing from its header action', () => {
    const editButton = fixture.nativeElement.querySelector(
      '.application-detail-section-layout__header button',
    ) as HTMLButtonElement;

    editButton.click();

    expect(detailState.isEditing('general')).toBe(true);
    expect(detailState.isEditing('systems-databases')).toBe(false);
  });

  it('blocks withdrawal and requests the dirty-tabs dialog', () => {
    detailState.form.markAsDirty();
    const access = fixture.componentInstance as unknown as {
      openWithdrawalDialog: () => void;
    };

    access.openWithdrawalDialog();

    expect(detailState.isUnsavedChangesDialogVisible()).toBe(true);
  });
});
