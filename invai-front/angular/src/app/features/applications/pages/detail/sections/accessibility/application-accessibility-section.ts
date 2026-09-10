import { HttpErrorResponse } from '@angular/common/http';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { ApplicationAccessibilityService } from '../../../../services/application-accessibility.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { AccessibilityResource } from '@features/maintenances/accessibility/accessibility.model';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  LOCALE_ID,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of, finalize } from 'rxjs';
import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
} from '@features/maintenances/accessibility/services/accessibility-resource.services';
import {
  APPLICATION_ACCESSIBILITY_RESOLVE_KEY,
  ApplicationAccessibilityResolvedData,
  AccessibilityCatalogResult,
  loadAccessibilityCatalog,
  loadApplicationAccessibility,
  ApplicationAccessibilityLoadResult,
} from './application-accessibility-section.resolver';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { formatDate } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { Editor } from 'primeng/editor';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { EditorInitEvent } from 'primeng/types/editor';

import {
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_TITLE,
} from '../../application-detail.i18n';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionActions } from '../../components/application-detail-section-actions/application-detail-section-actions';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import {
  APPLICATION_ACCESSIBILITY_MESSAGES,
  APPLICATION_ACCESSIBILITY_CATALOG_FAILED,
  APPLICATION_ACCESSIBILITY_CATALOG_FORBIDDEN,
  APPLICATION_ACCESSIBILITY_CATALOG_EMPTY,
  APPLICATION_ACCESSIBILITY_CATALOG_RETRY,
  APPLICATION_ACCESSIBILITY_CATALOG_FILTER,
  APPLICATION_ACCESSIBILITY_CATALOG_UNAVAILABLE,
  APPLICATION_ACCESSIBILITY_DATE_FORMAT,
  APPLICATION_ACCESSIBILITY_DATE_PLACEHOLDER,
  APPLICATION_ACCESSIBILITY_DOCUMENTATION_ARIA_LABEL,
  APPLICATION_ACCESSIBILITY_DOCUMENTATION_LABEL,
  APPLICATION_ACCESSIBILITY_DOCUMENTATION_PENDING,
  APPLICATION_ACCESSIBILITY_INFO_TITLE,
  APPLICATION_ACCESSIBILITY_LABELS,
  APPLICATION_ACCESSIBILITY_MOBILE_NAME_UNAVAILABLE,
  APPLICATION_ACCESSIBILITY_MOBILE_OPTIONS,
  APPLICATION_ACCESSIBILITY_NO_VALUE,
  APPLICATION_ACCESSIBILITY_PUBLIC_URL_ARIA_LABEL,
  APPLICATION_ACCESSIBILITY_SECTION_TITLE,
  APPLICATION_ACCESSIBILITY_URL_ERROR,
} from './application-accessibility-section.i18n';

interface CatalogOption {
  value: number;
  label: string;
  disabled: boolean;
}

@Component({
  selector: 'app-application-accessibility-section',
  standalone: true,
  imports: [
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    Button,
    DatePicker,
    Editor,
    InputText,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './application-accessibility-section.html',
  styleUrl: './application-accessibility-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationAccessibilitySection implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly locale = inject(LOCALE_ID);
  private readonly messageService = inject(MessageService);

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionTitle = APPLICATION_ACCESSIBILITY_SECTION_TITLE;
  protected readonly labels = APPLICATION_ACCESSIBILITY_LABELS;
  private readonly route = inject(ActivatedRoute);
  private readonly classificationService = inject(ClassificationSegmentsService);
  private readonly complianceService = inject(ComplianceSituationsService);
  private catalogData!: Pick<ApplicationAccessibilityResolvedData, 'classification' | 'compliance'>;
  private readonly accessibilityService = inject(ApplicationAccessibilityService);
  private readonly applicationsService = inject(ApplicationsService);
  protected readonly accessibilityLoading = signal(false);
  protected readonly messages = APPLICATION_ACCESSIBILITY_MESSAGES;
  protected readonly classificationOptions = signal<CatalogOption[]>([]);
  protected readonly complianceOptions = signal<CatalogOption[]>([]);
  protected readonly classificationFailed = signal(false);
  protected readonly complianceFailed = signal(false);
  protected readonly catalogsLoading = signal(false);
  protected readonly catalogEmpty = APPLICATION_ACCESSIBILITY_CATALOG_EMPTY;
  protected readonly catalogRetry = APPLICATION_ACCESSIBILITY_CATALOG_RETRY;
  protected readonly catalogFilter = APPLICATION_ACCESSIBILITY_CATALOG_FILTER;
  protected readonly catalogFailed = APPLICATION_ACCESSIBILITY_CATALOG_FAILED;
  protected readonly mobileOptions = APPLICATION_ACCESSIBILITY_MOBILE_OPTIONS;
  protected readonly dateFormat = APPLICATION_ACCESSIBILITY_DATE_FORMAT;
  protected readonly datePlaceholder = APPLICATION_ACCESSIBILITY_DATE_PLACEHOLDER;
  protected readonly urlError = APPLICATION_ACCESSIBILITY_URL_ERROR;
  protected readonly noValue = APPLICATION_ACCESSIBILITY_NO_VALUE;
  protected readonly mobileNameUnavailable = APPLICATION_ACCESSIBILITY_MOBILE_NAME_UNAVAILABLE;
  protected readonly publicUrlAriaLabel = APPLICATION_ACCESSIBILITY_PUBLIC_URL_ARIA_LABEL;
  protected readonly documentationLabel = APPLICATION_ACCESSIBILITY_DOCUMENTATION_LABEL;
  protected readonly documentationAriaLabel = APPLICATION_ACCESSIBILITY_DOCUMENTATION_ARIA_LABEL;
  protected readonly icons = PrimeIcons;
  protected readonly isSaving = signal(false);

  ngOnInit(): void {
    this.detailState.accessibilityClearBlocked
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() =>
        this.messageService.add({
          severity: 'info',
          summary: APPLICATION_ACCESSIBILITY_INFO_TITLE,
          detail: this.messages.clearBlocked,
        }),
      );
    const failed: AccessibilityCatalogResult = { items: [], failed: true, forbidden: false };
    const data: ApplicationAccessibilityResolvedData = this.route.snapshot.data[
      APPLICATION_ACCESSIBILITY_RESOLVE_KEY
    ] ?? {
      classification: failed,
      compliance: failed,
      accessibility: {
        applicationId: Number(this.detailState.application()?.id),
        appAccessibilityId: null,
        record: null,
        status: 'failed',
      },
    };
    this.applyAccessibility(data.accessibility);
    this.applyCatalogs(data, data.accessibility.status === 'forbidden');
  }

  protected accessibilityProblem(): string | null {
    const status = this.detailState.accessibilityStatus();
    return status === 'loaded' || status === 'absent' ? null : this.messages[status];
  }

  private applyAccessibility(result: ApplicationAccessibilityLoadResult): void {
    this.detailState.initializeAccessibility(result);
    const message = this.accessibilityProblem();
    if (message)
      this.messageService.add({
        severity: 'error',
        summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
        detail: message,
      });
  }

  protected retryAccessibility(): void {
    if (
      this.accessibilityLoading() ||
      this.isSaving() ||
      this.detailState.isEditing('accessibility')
    )
      return;
    this.accessibilityLoading.set(true);
    loadApplicationAccessibility(
      Number(this.detailState.application()?.id),
      this.applicationsService,
      this.accessibilityService,
      true,
    )
      .pipe(
        finalize(() => this.accessibilityLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        this.applyAccessibility(result);
        this.applyCatalogs(this.catalogData, true, false);
      });
  }

  protected retryCatalogs(): void {
    if (this.catalogsLoading()) return;
    this.catalogsLoading.set(true);
    forkJoin({
      classification: this.classificationFailed()
        ? loadAccessibilityCatalog(this.classificationService)
        : of(this.catalogData.classification),
      compliance: this.complianceFailed()
        ? loadAccessibilityCatalog(this.complianceService)
        : of(this.catalogData.compliance),
    })
      .pipe(
        finalize(() => this.catalogsLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((data) => this.applyCatalogs(data));
  }

  private applyCatalogs(
    data: Pick<ApplicationAccessibilityResolvedData, 'classification' | 'compliance'>,
    suppressForbidden = false,
    announce = true,
  ): void {
    this.catalogData = data;
    this.classificationFailed.set(data.classification.failed);
    this.complianceFailed.set(data.compliance.failed);
    const form = this.detailState.accessibilityForm;
    this.classificationOptions.set(
      this.catalogOptions(
        data.classification,
        form.controls.classificationSegment.value,
        this.detailState.accessibility()?.classificationSegment,
      ),
    );
    this.complianceOptions.set(
      this.catalogOptions(
        data.compliance,
        form.controls.complianceStatus.value,
        this.detailState.accessibility()?.compliance,
      ),
    );
    if (
      announce &&
      !suppressForbidden &&
      (data.classification.forbidden || data.compliance.forbidden)
    ) {
      this.messageService.add({
        severity: 'error',
        summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
        detail: APPLICATION_ACCESSIBILITY_CATALOG_FORBIDDEN,
      });
    }
    if (
      announce &&
      [data.classification, data.compliance].some((result) => result.failed && !result.forbidden)
    ) {
      this.messageService.add({
        severity: 'error',
        summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
        detail: APPLICATION_ACCESSIBILITY_CATALOG_FAILED,
      });
    }
  }

  private catalogOptions(
    result: AccessibilityCatalogResult,
    selected: number | null,
    saved?: AccessibilityResource | null,
  ): CatalogOption[] {
    const spanish = this.locale.startsWith('es');
    const options = result.items
      .filter((item) => !item.deletedAt)
      .map((item) => ({
        value: item.id,
        label:
          (spanish ? item.nameEs || item.name : item.name || item.nameEs) ||
          APPLICATION_ACCESSIBILITY_CATALOG_UNAVAILABLE(item.id),
        disabled: false,
      }));
    if (selected !== null && !options.some((option) => option.value === selected)) {
      options.push({
        value: selected,
        label:
          (saved?.id === selected
            ? spanish
              ? saved.nameEs || saved.name
              : saved.name || saved.nameEs
            : null) || APPLICATION_ACCESSIBILITY_CATALOG_UNAVAILABLE(selected),
        disabled: true,
      });
    }
    return options;
  }

  protected publicUrl(): string | null {
    const control = this.detailState.accessibilityForm.controls.publicUrl;
    const value = control.value.trim();
    return value && control.valid ? value : null;
  }

  protected startEditing(): void {
    if (this.accessibilityLoading() || this.isSaving()) return;
    const problem = this.accessibilityProblem();
    if (problem) {
      this.messageService.add({
        severity: 'info',
        summary: APPLICATION_ACCESSIBILITY_INFO_TITLE,
        detail: problem,
      });
      return;
    }
    this.detailState.startEditing('accessibility');
  }

  protected cancelEditing(): void {
    if (this.isSaving()) return;
    this.detailState.cancelEditing('accessibility');
  }

  protected syncCatalogSelection(key: 'classificationSegment' | 'complianceStatus'): void {
    // PrimeNG completes its clear operation after the form's valueChanges guard.
    // Write the guarded value back once the widget has finished clearing itself.
    const control = this.detailState.accessibilityForm.controls[key];
    control.setValue(control.value, { emitEvent: false });
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save('accessibility')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.isSaving.set(false);
          if (result.status !== 'saved') return;
          this.applyCatalogs(this.catalogData, true, false);

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
            detail:
              error instanceof HttpErrorResponse && error.status === 403
                ? this.messages.forbidden
                : APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
          });
        },
      });
  }

  protected onDocumentation(): void {
    this.messageService.add({
      severity: 'info',
      summary: APPLICATION_ACCESSIBILITY_INFO_TITLE,
      detail: APPLICATION_ACCESSIBILITY_DOCUMENTATION_PENDING,
    });
  }

  protected isInvalid(control: { invalid: boolean; dirty: boolean; touched: boolean }): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected optionLabel<T>(options: { value: T; label: string }[], value: T): string {
    return options.find((option) => option.value === value)?.label ?? this.noValue;
  }

  protected formattedDate(value: Date | null): string {
    return value ? formatDate(value, 'dd/MM/yyyy', this.locale) : this.noValue;
  }

  protected richTextValue(value: string): string {
    return value.trim() || this.noValue;
  }

  protected labelEditor(event: EditorInitEvent, labelId: string): void {
    event.editor.root.setAttribute('aria-labelledby', labelId);
  }
}
