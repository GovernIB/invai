import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';

import { ApplicationStatus } from '../../../../applications.model';
import { APPLICATIONS_ROUTES_LOC } from '../../../../applications.routes.i18n';
import {
  APPLICATION_OPTIONS_RESOLVE_KEY,
  ApplicationOptionsResolvedData,
} from '../../../../resolvers/application-options.resolver';
import {
  APPLICATION_DETAIL_ACTIVATION_ARIA_LABEL,
  APPLICATION_DETAIL_ACTIVATION_DIALOG_CANCEL_ARIA_LABEL,
  APPLICATION_DETAIL_ACTIVATION_DIALOG_CANCEL_LABEL,
  APPLICATION_DETAIL_ACTIVATION_DIALOG_CONFIRM_ARIA_LABEL,
  APPLICATION_DETAIL_ACTIVATION_DIALOG_CONFIRM_LABEL,
  APPLICATION_DETAIL_ACTIVATION_DIALOG_MESSAGE,
  APPLICATION_DETAIL_ACTIVATION_DIALOG_TITLE,
  APPLICATION_DETAIL_ACTIVATION_ERROR_MESSAGE,
  APPLICATION_DETAIL_ACTIVATION_ERROR_TITLE,
  APPLICATION_DETAIL_ACTIVATION_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_ACTIVATION_SUCCESS_TITLE,
  APPLICATION_DETAIL_INACTIVE_NOTE,
  APPLICATION_DETAIL_RESTORE_LABEL,
  APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_TITLE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
  APPLICATION_DETAIL_WITHDRAWAL_ARIA_LABEL,
  APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CANCEL_ARIA_LABEL,
  APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CANCEL_LABEL,
  APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CONFIRM_ARIA_LABEL,
  APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CONFIRM_LABEL,
  APPLICATION_DETAIL_WITHDRAWAL_DIALOG_MESSAGE,
  APPLICATION_DETAIL_WITHDRAWAL_DIALOG_TITLE,
  APPLICATION_DETAIL_WITHDRAWAL_ERROR_MESSAGE,
  APPLICATION_DETAIL_WITHDRAWAL_ERROR_TITLE,
  APPLICATION_DETAIL_WITHDRAWAL_LABEL,
  APPLICATION_DETAIL_WITHDRAWAL_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_WITHDRAWAL_SUCCESS_TITLE,
} from '../../application-detail.i18n';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionActions } from '../../components/application-detail-section-actions/application-detail-section-actions';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import { ApplicationGeneralForm } from './application-general-form';
import { APPLICATION_GENERAL_SECTION_TITLE } from './application-general.i18n';

@Component({
  standalone: true,
  selector: 'app-application-general-section',
  imports: [
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    ApplicationGeneralForm,
    Button,
    ConfirmationDialogComponent,
  ],
  templateUrl: './application-general-section.html',
  styleUrl: './application-general-section.scss',
})
export class ApplicationGeneralSection {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionTitle = APPLICATION_GENERAL_SECTION_TITLE;
  protected readonly options = (
    this.route.snapshot.data[APPLICATION_OPTIONS_RESOLVE_KEY] as ApplicationOptionsResolvedData
  ).options;
  protected readonly isSaving = signal(false);
  protected readonly isActivating = signal(false);
  protected readonly isWithdrawing = signal(false);
  protected readonly isActivationDialogVisible = signal(false);
  protected readonly isWithdrawalDialogVisible = signal(false);
  protected readonly isInactive = computed(
    () => this.detailState.application()?.status === ApplicationStatus.INACTIVE,
  );
  protected readonly withdrawalDialogMessage = computed(() =>
    APPLICATION_DETAIL_WITHDRAWAL_DIALOG_MESSAGE(
      this.detailState.application()?.name ?? '',
    ),
  );
  protected readonly activationDialogMessage = computed(() =>
    APPLICATION_DETAIL_ACTIVATION_DIALOG_MESSAGE(
      this.detailState.application()?.name ?? '',
    ),
  );
  protected readonly inactiveNote = APPLICATION_DETAIL_INACTIVE_NOTE;
  protected readonly restoreLabel = APPLICATION_DETAIL_RESTORE_LABEL;
  protected readonly activationAriaLabel = APPLICATION_DETAIL_ACTIVATION_ARIA_LABEL;
  protected readonly withdrawalLabel = APPLICATION_DETAIL_WITHDRAWAL_LABEL;
  protected readonly withdrawalAriaLabel = APPLICATION_DETAIL_WITHDRAWAL_ARIA_LABEL;
  protected readonly activationDialogTitle = APPLICATION_DETAIL_ACTIVATION_DIALOG_TITLE;
  protected readonly activationDialogCancelLabel =
    APPLICATION_DETAIL_ACTIVATION_DIALOG_CANCEL_LABEL;
  protected readonly activationDialogConfirmLabel =
    APPLICATION_DETAIL_ACTIVATION_DIALOG_CONFIRM_LABEL;
  protected readonly activationDialogCancelAriaLabel =
    APPLICATION_DETAIL_ACTIVATION_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly activationDialogConfirmAriaLabel =
    APPLICATION_DETAIL_ACTIVATION_DIALOG_CONFIRM_ARIA_LABEL;
  protected readonly withdrawalDialogTitle = APPLICATION_DETAIL_WITHDRAWAL_DIALOG_TITLE;
  protected readonly withdrawalDialogCancelLabel =
    APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CANCEL_LABEL;
  protected readonly withdrawalDialogConfirmLabel =
    APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CONFIRM_LABEL;
  protected readonly withdrawalDialogCancelAriaLabel =
    APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly withdrawalDialogConfirmAriaLabel =
    APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CONFIRM_ARIA_LABEL;
  protected readonly icons = PrimeIcons;

  protected startEditing(): void {
    this.detailState.startEditing('general');
  }

  protected cancelEditing(): void {
    this.detailState.cancelEditing('general');
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save('general')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.isSaving.set(false);
          if (result.status !== 'saved') return;

          this.messageService.add({
            severity: 'success',
            summary: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
            detail: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE(this.sectionTitle),
          });
        },
        error: (error: unknown) => {
          this.isSaving.set(false);
          if (isStructuredBadRequest(error)) return;

          this.messageService.add({
            severity: 'error',
            summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
            detail: APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
          });
        },
      });
  }

  protected openActivationDialog(): void {
    if (!this.isActivating()) this.isActivationDialogVisible.set(true);
  }

  protected closeActivationDialog(): void {
    this.isActivationDialogVisible.set(false);
  }

  protected confirmActivation(): void {
    if (this.isActivating()) return;

    this.isActivationDialogVisible.set(false);
    this.isActivating.set(true);
    this.detailState
      .activate()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (activated) => {
          this.isActivating.set(false);
          if (!activated) return;

          this.messageService.add({
            severity: 'success',
            summary: APPLICATION_DETAIL_ACTIVATION_SUCCESS_TITLE,
            detail: APPLICATION_DETAIL_ACTIVATION_SUCCESS_MESSAGE,
          });
        },
        error: (error: unknown) => {
          this.isActivating.set(false);
          if (isStructuredBadRequest(error)) return;

          this.messageService.add({
            severity: 'error',
            summary: APPLICATION_DETAIL_ACTIVATION_ERROR_TITLE,
            detail: APPLICATION_DETAIL_ACTIVATION_ERROR_MESSAGE,
          });
        },
      });
  }

  protected openWithdrawalDialog(): void {
    if (this.isWithdrawing()) return;
    if (this.detailState.hasDirtySections()) {
      this.detailState.showUnsavedChangesDialog();
      return;
    }

    this.isWithdrawalDialogVisible.set(true);
  }

  protected closeWithdrawalDialog(): void {
    this.isWithdrawalDialogVisible.set(false);
  }

  protected confirmWithdrawal(): void {
    if (this.isWithdrawing()) return;

    this.isWithdrawalDialogVisible.set(false);
    this.isWithdrawing.set(true);
    this.detailState
      .withdraw()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (withdrawn) => {
          this.isWithdrawing.set(false);
          if (!withdrawn) return;

          this.messageService.add({
            severity: 'success',
            summary: APPLICATION_DETAIL_WITHDRAWAL_SUCCESS_TITLE,
            detail: APPLICATION_DETAIL_WITHDRAWAL_SUCCESS_MESSAGE,
          });
          void this.router.navigate(['/', APPLICATIONS_ROUTES_LOC.BASE]);
        },
        error: (error: unknown) => {
          this.isWithdrawing.set(false);
          if (isStructuredBadRequest(error)) return;

          this.messageService.add({
            severity: 'error',
            summary: APPLICATION_DETAIL_WITHDRAWAL_ERROR_TITLE,
            detail: APPLICATION_DETAIL_WITHDRAWAL_ERROR_MESSAGE,
          });
        },
      });
  }
}
