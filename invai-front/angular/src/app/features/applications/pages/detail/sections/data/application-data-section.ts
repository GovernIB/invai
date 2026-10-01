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
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Editor } from 'primeng/editor';
import { Tab, TabList, TabPanel, TabPanels, Tabs } from 'primeng/tabs';
import { EditorInitEvent } from 'primeng/types/editor';
import { TabListPassThrough } from 'primeng/types/tabs';
import { finalize } from 'rxjs';

import { ApplicationDataSource } from '../../../../forms/application-data-form.factory';
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
  APPLICATION_DATA_SECTION_TITLE,
} from './application-data-section.i18n';
import {
  APPLICATION_DATA_RESOLVE_KEY,
  ApplicationDataLoadResult,
  loadApplicationData,
} from './application-data-section.resolver';
import { ApplicationDataSourcePanel } from './application-data-source';

@Component({
  selector: 'app-application-data-section',
  standalone: true,
  imports: [
    ApplicationDataSourcePanel,
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    Button,
    Editor,
    ReactiveFormsModule,
    Tab,
    TabList,
    TabPanel,
    TabPanels,
    Tabs,
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
  protected readonly icons = PrimeIcons;
  protected readonly isSaving = signal(false);
  protected readonly dataLoading = signal(false);
  protected readonly observationsLabelId = 'application-data-observations-label';
  protected readonly tabListPassThrough: TabListPassThrough = {
    tabList: { 'aria-label': APPLICATION_DATA_LABELS.sources },
  };
  protected readonly showContent = computed(() =>
    ['loaded', 'absent', 'deleted'].includes(this.detailState.dataStatus()),
  );
  protected readonly problem = computed(() => {
    const status = this.detailState.dataStatus();
    if (status === 'loaded' || status === 'absent') return null;
    if (status === 'failed') return this.detailState.dataErrorMessage() ?? this.messages.failed;
    return this.messages[status];
  });

  // A loaded record carries null endpoints only for a source whose live fetch failed.
  protected sourceUnavailable(source: ApplicationDataSource): boolean {
    return this.detailState.dataStatus() === 'loaded' && this.detailState.data()?.[source] === null;
  }

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

  protected onSourceChange(value: string | number | undefined): void {
    if (value === 'openData' || value === 'reuse') {
      this.detailState.dataActiveSource.set(value satisfies ApplicationDataSource);
    }
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
        if (retryHadFocus && this.showContent() && !this.problem()) this.focusActiveSource();
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
    return value.trim() || this.labels.noValue;
  }

  protected labelEditor(event: EditorInitEvent): void {
    event.editor.root.setAttribute('aria-labelledby', this.observationsLabelId);
  }

  private focusActiveSource(): void {
    afterNextRender(
      () =>
        this.host.nativeElement
          .querySelector<HTMLElement>('[role="tab"][aria-selected="true"]')
          ?.focus(),
      { injector: this.injector },
    );
  }
}
