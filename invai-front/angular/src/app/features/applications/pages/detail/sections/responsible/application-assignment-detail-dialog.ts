import { ChangeDetectionStrategy, Component, LOCALE_ID, computed, inject, input, output } from '@angular/core';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';
import { localizedName } from '@shared/utils/localized-name.utils';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { ApplicationAssignedResponsibleOutput, ApplicationAuthorizedOutput } from '../../../../applications.model';
import { APPLICATION_ASSIGNMENT_DETAIL_COPY } from './application-assignment-detail-dialog.i18n';
import { APPLICATION_RESPONSIBLE_COPY } from './application-responsible-section.i18n';

export type ApplicationAssignmentDetail =
  | { kind: 'responsible'; assignment: ApplicationAssignedResponsibleOutput }
  | { kind: 'authorized'; assignment: ApplicationAuthorizedOutput };

@Component({
  selector: 'app-application-assignment-detail-dialog',
  imports: [CrudEntityDialog, InputText, Textarea],
  templateUrl: './application-assignment-detail-dialog.html',
  styleUrl: './application-assignment-detail-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationAssignmentDetailDialog {
  detail = input.required<ApplicationAssignmentDetail>();
  closed = output<void>();

  private readonly locale = inject(LOCALE_ID);
  protected readonly copy = APPLICATION_RESPONSIBLE_COPY;
  protected readonly detailCopy = APPLICATION_ASSIGNMENT_DETAIL_COPY;
  protected readonly person = computed(() => this.detail().assignment.person);
  protected readonly title = computed(() => this.detail().kind === 'responsible'
    ? this.detailCopy.responsibleTitle : this.detailCopy.authorizedTitle);
  protected readonly ariaLabels = computed(() => ({
    ...(this.detail().kind === 'responsible' ? this.copy.actions : this.copy.authorizedActions),
    close: this.detailCopy.close,
  }));
  protected readonly responsibility = computed(() => {
    const detail = this.detail();
    return detail.kind === 'responsible'
      ? localizedName(detail.assignment.responsibleType, this.locale) : '';
  });
  protected readonly jobTitle = computed(() => {
    const detail = this.detail();
    return detail.kind === 'responsible' ? detail.assignment.jobTitle : null;
  });
  protected readonly authorizations = computed(() => {
    const detail = this.detail();
    return detail.kind === 'authorized'
      ? detail.assignment.authorizationTypes.map((type) => localizedName(type, this.locale)).join(', ')
      : '';
  });

  protected fieldId(field: string): string {
    return `assignment-detail-${this.detail().kind}-${this.detail().assignment.id}-${field}`;
  }
}
