import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  Injector,
  OnInit,
  afterNextRender,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Editor } from 'primeng/editor';
import { EditorInitEvent } from 'primeng/types/editor';
import { finalize } from 'rxjs';

import { ApplicationIntegrationConnectionsService } from '../../../../services/application-integration-connections.service';
import { ApplicationIntegrationService } from '../../../../services/application-integration.service';
import { ApplicationsService } from '../../../../services/applications.service';
import {
  APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_TITLE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
} from '../../application-detail.i18n';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionActions } from '../../components/application-detail-section-actions/application-detail-section-actions';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import { ApplicationIntegrationConnectionDialog } from './application-integration-connection-dialog';
import { ApplicationIntegrationConnectionsState } from './application-integration-connections-state';
import {
  APPLICATION_INTEGRATIONS_ADD_ARIA_LABEL,
  APPLICATION_INTEGRATIONS_COLUMNS,
  APPLICATION_INTEGRATIONS_CONNECTIONS_TITLE,
  APPLICATION_INTEGRATIONS_DELETE_DIALOG,
  APPLICATION_INTEGRATIONS_MESSAGES,
  APPLICATION_INTEGRATIONS_OBSERVATIONS_LABEL,
  APPLICATION_INTEGRATIONS_SECTION_TITLE,
} from './application-integrations-section.i18n';
import {
  APPLICATION_INTEGRATIONS_RESOLVE_KEY,
  ApplicationIntegrationsLoadResult,
  loadApplicationIntegrations,
} from './application-integrations-section.resolver';
import { ApplicationIntegrationsTable } from './application-integrations-table';

@Component({
  selector: 'app-application-integrations-section',
  standalone: true,
  imports: [
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    ApplicationIntegrationConnectionDialog,
    ApplicationIntegrationsTable,
    Button,
    ConfirmationDialogComponent,
    Editor,
    ReactiveFormsModule,
  ],
  providers: [ApplicationIntegrationConnectionsState],
  templateUrl: './application-integrations-section.html',
  styleUrl: './application-integrations-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationIntegrationsSection implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);
  private readonly messageService = inject(MessageService);
  private readonly applicationsService = inject(ApplicationsService);
  private readonly integrationService = inject(ApplicationIntegrationService);
  private readonly connectionsService = inject(ApplicationIntegrationConnectionsService);

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly connections = inject(ApplicationIntegrationConnectionsState);
  protected readonly sectionTitle = APPLICATION_INTEGRATIONS_SECTION_TITLE;
  protected readonly connectionsTitle = APPLICATION_INTEGRATIONS_CONNECTIONS_TITLE;
  protected readonly addAriaLabel = APPLICATION_INTEGRATIONS_ADD_ARIA_LABEL;
  protected readonly observationsLabel = APPLICATION_INTEGRATIONS_OBSERVATIONS_LABEL;
  protected readonly observationsLabelId = 'application-integrations-observations-label';
  protected readonly connectionsTitleId = 'application-integrations-connections-title';
  protected readonly columns = APPLICATION_INTEGRATIONS_COLUMNS;
  protected readonly messages = APPLICATION_INTEGRATIONS_MESSAGES;
  protected readonly deleteDialog = APPLICATION_INTEGRATIONS_DELETE_DIALOG;
  protected readonly icons = PrimeIcons;
  protected readonly isSaving = signal(false);
  protected readonly anchorLoading = signal(false);
  protected readonly showContent = computed(() =>
    ['loaded', 'absent', 'deleted'].includes(this.detailState.integrationStatus()),
  );
  protected readonly problem = computed(() => {
    const status = this.detailState.integrationStatus();
    if (status === 'loaded' || status === 'absent') return null;
    if (status === 'failed') {
      return this.detailState.integrationErrorMessage() ?? this.messages.failed;
    }
    return this.messages[status];
  });
  // Connections hang from the anchor, which is created by the first observations save.
  protected readonly anchorRequired = computed(
    () => this.connections.showActions() && this.detailState.integrationStatus() === 'absent',
  );

  ngOnInit(): void {
    const result: ApplicationIntegrationsLoadResult = this.route.snapshot.data[
      APPLICATION_INTEGRATIONS_RESOLVE_KEY
    ] ?? {
      applicationId: Number(this.detailState.application()?.id),
      appIntegrationId: null,
      record: null,
      status: 'failed',
      errorMessage: null,
      connectionsPage: null,
      connectionsLoadFailed: false,
      connectionsErrorMessage: null,
    };
    this.applyResult(result);
  }

  protected retry(): void {
    if (this.anchorLoading() || this.isSaving() || this.detailState.isEditing('integrations')) {
      return;
    }
    const retryHadFocus = this.host.nativeElement.contains(document.activeElement);
    this.anchorLoading.set(true);
    loadApplicationIntegrations(
      Number(this.detailState.application()?.id),
      {
        applications: this.applicationsService,
        integrations: this.integrationService,
        connections: this.connectionsService,
      },
      true,
    )
      .pipe(
        finalize(() => this.anchorLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        this.applyResult(result);
        // The retry button disappears on success; keep keyboard users inside the section.
        if (retryHadFocus && this.showContent()) this.focusConnectionsTitle();
      });
  }

  protected startEditing(): void {
    if (this.anchorLoading() || this.isSaving()) return;
    const problem = this.problem();
    if (problem) {
      this.messageService.add({ severity: 'info', summary: this.messages.infoTitle, detail: problem });
      return;
    }
    this.detailState.startEditing('integrations');
  }

  protected cancelEditing(): void {
    if (this.isSaving()) return;
    this.detailState.cancelEditing('integrations');
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save('integrations')
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
          // Structured validation errors are already shown by the global dialog.
          if (isStructuredBadRequest(error)) return;
          this.messageService.add({
            severity: 'error',
            summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
            detail:
              error instanceof HttpErrorResponse && error.status === 403
                ? this.messages.forbidden
                : APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
          });
        },
      });
  }

  protected richTextValue(value: string): string {
    return value.trim() || '-';
  }

  protected labelEditor(event: EditorInitEvent): void {
    event.editor.root.setAttribute('aria-labelledby', this.observationsLabelId);
  }

  private applyResult(result: ApplicationIntegrationsLoadResult): void {
    this.detailState.initializeIntegrations(result);
    this.connections.initialize(result);
  }

  private focusConnectionsTitle(): void {
    afterNextRender(
      () => this.host.nativeElement.querySelector<HTMLElement>(`#${this.connectionsTitleId}`)?.focus(),
      { injector: this.injector },
    );
  }
}
