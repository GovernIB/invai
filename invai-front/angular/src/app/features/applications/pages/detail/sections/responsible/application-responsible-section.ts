import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MessageService } from 'primeng/api';

import {
  APPLICATION_DETAIL_INFO_TITLE,
  APPLICATION_DETAIL_SECTION_SAVE_MOCKED_MESSAGE,
} from '../../application-detail.i18n';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionActions } from '../../components/application-detail-section-actions/application-detail-section-actions';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import { ApplicationResponsibleForm } from './application-responsible-form';
import { APPLICATION_RESPONSIBLE_SECTION_TITLE } from './application-responsible-section.i18n';

@Component({
  standalone: true,
  selector: 'app-application-responsible-section',
  imports: [
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    ApplicationResponsibleForm,
  ],
  templateUrl: './application-responsible-section.html',
})
export class ApplicationResponsibleSection {
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionTitle = APPLICATION_RESPONSIBLE_SECTION_TITLE;
  protected readonly isSaving = signal(false);

  protected startEditing(): void {
    this.detailState.startEditing('responsible');
  }

  protected cancelEditing(): void {
    this.detailState.cancelEditing('responsible');
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save('responsible')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.isSaving.set(false);
        this.messageService.add({
          severity: 'info',
          summary: APPLICATION_DETAIL_INFO_TITLE,
          detail: APPLICATION_DETAIL_SECTION_SAVE_MOCKED_MESSAGE(this.sectionTitle),
        });
      });
  }
}
