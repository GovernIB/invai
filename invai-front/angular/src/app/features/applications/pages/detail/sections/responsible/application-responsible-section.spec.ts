import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';

import { ApplicationStatus } from '../../../../applications.model';
import { ApplicationDevelopmentService } from '../../../../services/application-development.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationResponsibleSection } from './application-responsible-section';

describe('ApplicationResponsibleSection', () => {
  let fixture: ComponentFixture<ApplicationResponsibleSection>;
  let detailState: ApplicationDetailState;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationResponsibleSection],
      providers: [
        ApplicationDetailState,
        MessageService,
        { provide: ApplicationsService, useValue: {} },
        { provide: ApplicationDevelopmentService, useValue: {} },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationResponsibleSection);
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

    expect(title.textContent?.trim()).toBe("Responsables de l'aplicació");
    expect(title.id).toBe('application-responsible-section-title');
  });

  it('offers an independent mocked edit and save flow', () => {
    let button = fixture.nativeElement.querySelector(
      '.application-detail-section-layout__header button',
    ) as HTMLButtonElement;
    button.click();
    fixture.detectChanges();

    expect(detailState.isEditing('responsible')).toBe(true);

    button = [
      ...fixture.nativeElement.querySelectorAll(
        '.application-detail-section-layout__header button',
      ),
    ].find((candidate: Element) =>
      candidate.textContent?.includes('Desar'),
    ) as HTMLButtonElement;
    button.click();

    expect(detailState.isEditing('responsible')).toBe(false);
  });
});
