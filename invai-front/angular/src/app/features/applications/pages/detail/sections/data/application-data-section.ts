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
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { TEXT_FIELD_MAX_BYTES_ERROR } from '@shared/constants/text-field.i18n';
import { RichTextAria } from '@shared/directives/rich-text-aria.directive';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Editor } from 'primeng/editor';
import { EditorInitEvent } from 'primeng/types/editor';
import { finalize } from 'rxjs';

import { ApplicationDataSource } from '../../../../applications.model';
import { ApplicationDataService } from '../../../../services/application-data.service';
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
import {
  APPLICATION_DATA_LABELS,
  APPLICATION_DATA_MESSAGES,
  APPLICATION_DATA_SAVED_INVALID_URLS_MESSAGE,
  APPLICATION_DATA_SECTION_TITLE,
  APPLICATION_DATA_URL_MESSAGES,
} from './application-data-section.i18n';
import {
  APPLICATION_DATA_RESOLVE_KEY,
  ApplicationDataLoadResult,
  loadApplicationData,
} from './application-data-section.resolver';
import { ApplicationDataUrlPanel } from './application-data-url-panel';

@Component({
  selector: 'app-application-data-section',
  standalone: true,
  imports: [
    ApplicationDataUrlPanel,
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    Button,
    Editor,
    ReactiveFormsModule,
    RichTextAria,
  ],
  templateUrl: './application-data-section.html',
  styleUrl: './application-data-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDataSection implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);
  private readonly messageService = inject(MessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly applicationsService = inject(ApplicationsService);
  private readonly dataService = inject(ApplicationDataService);

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionTitle = APPLICATION_DATA_SECTION_TITLE;
  protected readonly labels = APPLICATION_DATA_LABELS;
  protected readonly messages = APPLICATION_DATA_MESSAGES;
  protected readonly urlMessages = APPLICATION_DATA_URL_MESSAGES;
  protected readonly icons = PrimeIcons;
  protected readonly isSaving = signal(false);
  protected readonly dataLoading = signal(false);
  protected readonly observationsLabelId = 'application-data-observations-label';
  protected readonly observationsErrorId = 'application-data-observations-error';
  protected readonly textMaxBytesError = TEXT_FIELD_MAX_BYTES_ERROR;
  protected readonly showContent = computed(() =>
    ['loaded', 'absent', 'deleted'].includes(this.detailState.dataStatus()),
  );
  protected readonly problem = computed(() => {
    const status = this.detailState.dataStatus();
    if (status === 'loaded' || status === 'absent') return null;
    if (status === 'failed') return this.detailState.dataErrorMessage() ?? this.messages.failed;
    return this.messages[status];
  });

  ngOnInit(): void {
    const result: ApplicationDataLoadResult = this.route.snapshot.data[
      APPLICATION_DATA_RESOLVE_KEY
    ] ?? {
      applicationId: Number(this.detailState.application()?.id),
      appDataId: null,
      record: null,
      status: 'failed',
      errorMessage: null,
    };
    this.detailState.initializeData(result);
  }

  protected retry(): void {
    if (this.dataLoading() || this.isSaving() || this.detailState.isEditing('data')) return;
    const retryHadFocus = this.host.nativeElement.contains(document.activeElement);
    this.dataLoading.set(true);
    loadApplicationData(
      Number(this.detailState.application()?.id),
      this.applicationsService,
      this.dataService,
      true,
    )
      .pipe(
        finalize(() => this.dataLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        this.detailState.initializeData(result);
        // The retry button disappears on success; keep keyboard users inside the tab.
        if (retryHadFocus && this.showContent() && !this.problem()) this.focusFirstSource();
      });
  }

  protected startEditing(): void {
    if (this.dataLoading() || this.isSaving()) return;
    const problem = this.problem();
    if (problem) {
      this.messageService.add({
        severity: 'info',
        summary: this.messages.infoTitle,
        detail: problem,
      });
      return;
    }
    this.detailState.startEditing('data');
  }

  protected cancelEditing(): void {
    if (this.isSaving()) return;
    this.detailState.cancelEditing('data');
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save('data')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.isSaving.set(false);
          if (result.status !== 'saved') return;
          const invalidUrls = result.invalidDataUrls ?? [];
          this.messageService.add({
            severity: invalidUrls.length ? 'warn' : 'success',
            summary: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
            detail: invalidUrls.length
              ? APPLICATION_DATA_SAVED_INVALID_URLS_MESSAGE(invalidUrls.map((source) => this.sourceLabel(source)))
              : APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE(this.sectionTitle),
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
    return value.trim() || this.labels.noValue;
  }

  protected observationsInvalid(): boolean {
    const control = this.detailState.dataForm.controls.observations;
    return control.invalid && (control.dirty || control.touched);
  }

  protected labelEditor(event: EditorInitEvent): void {
    event.editor.root.setAttribute('aria-labelledby', this.observationsLabelId);
  }

  private sourceLabel(source: ApplicationDataSource): string {
    return source === 'openData' ? this.labels.openData : this.labels.reuse;
  }

  private focusFirstSource(): void {
    afterNextRender(
      () => this.host.nativeElement.querySelector<HTMLElement>('.application-data-url__title')?.focus(),
      { injector: this.injector },
    );
  }
}
