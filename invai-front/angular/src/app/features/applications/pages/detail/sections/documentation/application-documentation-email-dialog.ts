import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  input,
  model,
  OnInit,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { isStructuredBadRequest, readApiErrorMessage } from '@core/models/api-error.model';
import { PaginatedList } from '@models/table.model';
import { EMPTY_LIST, PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { MessageService, PrimeIcons } from 'primeng/api';
import { AutoComplete } from 'primeng/autocomplete';
import { InputText } from 'primeng/inputtext';
import { TableLazyLoadEvent } from 'primeng/table';
import { Textarea } from 'primeng/textarea';
import { AutoCompletePassThrough } from 'primeng/types/autocomplete';
import {
  catchError,
  debounceTime,
  finalize,
  map,
  of,
  Subject,
  switchMap,
  tap,
} from 'rxjs';

import {
  ApplicationAttachment,
  ApplicationAttachmentPageParams,
  ApplicationDocumentationEmailInput,
} from '../../../../application-documentation.model';
import {
  createApplicationDocumentationEmailForm,
  normalizeEmailRecipients,
} from '../../../../forms/application-documentation-email-form.factory';
import { ApplicationDocumentationService } from '../../../../services/application-documentation.service';
import { ApplicationDocumentationEmailTable } from './application-documentation-email-table';
import {
  ApplicationDocumentationEmailSelection,
  ApplicationDocumentationEmailVersion,
  ApplicationDocumentationEmailVersionChange,
} from './application-documentation-email.types';
import {
  APPLICATION_EMAIL_MAX_ATTACHMENTS_BYTES,
  APPLICATION_EMAIL_MESSAGE_MAX_LENGTH,
  APPLICATION_EMAIL_SUBJECT_MAX_LENGTH,
} from './application-documentation.constants';
import {
  APPLICATION_ATTACHMENTS_TEXTS,
  APPLICATION_DOCUMENTATION_EMAIL_TEXTS,
  APPLICATION_DOCUMENTATION_TEXTS,
} from './application-documentation.i18n';

const QUICK_SEARCH_DEBOUNCE_MS = 400;
const ID = 'application-documentation-email';
const RECIPIENT_CHIP_SELECTOR = '.p-autocomplete-chip-item';
// A space, a comma or a semicolon end an address, as Return and leaving the field do.
const RECIPIENT_SEPARATOR = /[,;\s]+/;

// Sends versions of the attachments of an application by e-mail. It owns the whole sending: the
// form with the recipients, the subject and the message, the table to choose the documents and
// their versions, and the request. The selection survives paging and searching, and it closes
// once the mail server has accepted the message.
@Component({
  selector: 'app-application-documentation-email-dialog',
  standalone: true,
  imports: [
    ApplicationDocumentationEmailTable,
    AutoComplete,
    CrudEntityDialog,
    InputText,
    ReactiveFormsModule,
    SectionActionsComponent,
    Textarea,
  ],
  templateUrl: './application-documentation-email-dialog.html',
  styleUrl: './application-documentation-email-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDocumentationEmailDialog implements OnInit {
  applicationId = input.required<number>();
  // Names the application in the subject proposed to the user.
  applicationCode = input<string | null>(null);
  visible = model(false);
  closed = output<void>();

  private readonly service = inject(ApplicationDocumentationService);
  private readonly messageService = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly searchRequests = new Subject<ApplicationAttachmentPageParams>();
  private readonly quickSearchChanges = new Subject<string>();
  // Versions already read, so choosing a document again doesn't read them again.
  private readonly versionsCache = new Map<number, ApplicationDocumentationEmailVersion[]>();
  private readonly recipientsField = viewChild.required(AutoComplete);
  private readonly recipientEditor = viewChild<ElementRef<HTMLInputElement>>('recipientEditor');
  private lastParams: ApplicationAttachmentPageParams = {
    page: 0,
    size: PAGINATOR_ROWS,
    sort: 'title,asc',
    deleted: false,
  };

  protected readonly ids = {
    instructions: `${ID}-instructions`,
    recipients: `${ID}-recipients`,
    subject: `${ID}-subject`,
    message: `${ID}-message`,
    selectionTitle: `${ID}-selection-title`,
    selectionError: `${ID}-selection-error`,
  };
  protected readonly texts = APPLICATION_DOCUMENTATION_EMAIL_TEXTS;
  protected readonly common = APPLICATION_DOCUMENTATION_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly subjectMaxLength = APPLICATION_EMAIL_SUBJECT_MAX_LENGTH;
  protected readonly messageMaxLength = APPLICATION_EMAIL_MESSAGE_MAX_LENGTH;
  protected readonly recipientSeparator = RECIPIENT_SEPARATOR;
  // The address whose chip is a text field, and what it holds while it is being changed.
  protected readonly editedRecipient = signal<string | null>(null);
  protected readonly recipientDraft = signal('');
  // The shell only shows Close, Cancel and the submit action in create mode.
  protected readonly ariaLabels: CrudEntityDialogAriaLabels = {
    accept: this.texts.closeAriaLabel,
    add: this.texts.actions.send,
    cancel: this.texts.cancelAriaLabel,
    close: this.texts.closeAriaLabel,
    deactivate: this.texts.closeAriaLabel,
    edit: this.texts.closeAriaLabel,
    restore: this.texts.closeAriaLabel,
    save: this.texts.actions.send,
  };

  protected readonly form = createApplicationDocumentationEmailForm(inject(FormBuilder));
  protected readonly page = signal<PaginatedList<ApplicationAttachment>>(EMPTY_LIST);
  protected readonly first = signal(0);
  protected readonly isLoading = signal(false);
  protected readonly isSearchPending = signal(false);
  protected readonly quickSearch = signal('');
  protected readonly selection = signal<ReadonlyMap<number, ApplicationDocumentationEmailSelection>>(
    new Map(),
  );
  protected readonly isSending = signal(false);
  private readonly submitted = signal(false);
  // With an address, a text or a document chosen, a click outside the dialog does not close it.
  protected readonly hasUnsavedChanges = () => this.form.dirty || this.selectedCount() > 0;

  protected readonly selectedCount = computed(() => this.selection().size);
  // Size of the chosen versions whose size is known; the back-end checks the rest.
  private readonly knownBytes = computed(() =>
    [...this.selection().values()].reduce(
      (total, { versionId, versions }) =>
        total + (versions?.find(({ id }) => id === versionId)?.fileSize ?? 0),
      0,
    ),
  );
  protected readonly selectionError = computed(() => {
    if (this.knownBytes() > APPLICATION_EMAIL_MAX_ATTACHMENTS_BYTES) {
      return this.texts.errors.attachmentsTooLarge;
    }
    return this.submitted() && this.selectedCount() === 0
      ? this.texts.errors.attachmentsRequired
      : null;
  });
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isSearchPending(),
  );

  constructor() {
    // The editor exists once its chip has been drawn again: the address is ready to be retyped.
    effect(() => {
      const editor = this.recipientEditor()?.nativeElement;
      editor?.focus();
      editor?.select();
    });
  }

  ngOnInit(): void {
    const code = this.applicationCode()?.trim();
    this.form.reset({
      recipients: [],
      subject: code ? this.texts.defaultSubject(code) : '',
      message: '',
    });
    this.observeSearchRequests();
    this.observeQuickSearch();
    this.searchRequests.next(this.lastParams);
  }

  protected onQuickSearchChange(value: string): void {
    this.quickSearch.set(value);
    this.isSearchPending.set(true);
    this.quickSearchChanges.next(value);
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    const rows = event.rows ?? PAGINATOR_ROWS;
    const first = event.first ?? 0;
    this.first.set(first);
    this.requestPage({ ...this.lastParams, page: Math.floor(first / rows), size: rows });
  }

  protected toggle(attachment: ApplicationAttachment): void {
    const versionId = attachment.currentAttachmentId;
    if (this.isSending() || versionId == null) return;

    const next = new Map(this.selection());
    if (next.delete(attachment.id)) {
      this.selection.set(next);
      return;
    }
    const versions = this.versionsCache.get(attachment.id) ?? null;
    next.set(attachment.id, {
      documentId: attachment.id,
      title: attachment.title,
      versionId,
      versions,
    });
    this.selection.set(next);
    if (!versions) this.loadVersions(attachment, versionId);
  }

  protected changeVersion({ documentId, versionId }: ApplicationDocumentationEmailVersionChange): void {
    if (this.isSending()) return;
    this.updateSelection(documentId, (selected) => ({ ...selected, versionId }));
  }

  // The address typed is added as written; the list keeps them as the back-end will.
  protected onRecipientAdded(): void {
    const control = this.form.controls.recipients;
    control.setValue(normalizeEmailRecipients(control.value));
    control.markAsDirty();
  }

  protected onRecipientsDoubleClick(event: MouseEvent): void {
    const chip = (event.target as Element | null)?.closest(RECIPIENT_CHIP_SELECTOR);
    const chips = chip?.parentElement?.querySelectorAll(`:scope > ${RECIPIENT_CHIP_SELECTOR}`);
    // A double click inside the address being edited selects a word of it.
    if (!chip || !chips || chip.contains(this.recipientEditor()?.nativeElement ?? null)) return;
    event.preventDefault();
    this.editRecipient([...chips].indexOf(chip));
  }

  protected onRecipientsKeydown(event: KeyboardEvent): void {
    if (event.target === this.recipientsInput()) {
      // A space, a comma or a semicolon add the address typed so far, and none holds one.
      if (event.key.length === 1 && RECIPIENT_SEPARATOR.test(event.key)) event.preventDefault();
      return;
    }
    // Return edits the address reached with the arrows.
    if (event.key !== 'Enter') return;
    const index = this.recipientsField().focusedMultipleOptionIndex();
    if (index < 0) return;
    event.preventDefault();
    this.editRecipient(index);
  }

  protected onRecipientEditorInput(event: Event): void {
    this.recipientDraft.set((event.target as HTMLInputElement).value);
  }

  // Return, a space, a comma or a semicolon keep the change, as leaving the address does, and
  // Escape gives it up. The keys stay in the editor: the list of PrimeNG would take the arrows
  // and Backspace as its own, and the dialog would close with Escape.
  protected onRecipientEditorKeydown(event: KeyboardEvent): void {
    event.stopPropagation();
    if (event.key === 'Escape') {
      event.preventDefault();
      this.editedRecipient.set(null);
      this.recipientsInput()?.focus();
    } else if (event.key === 'Enter' || RECIPIENT_SEPARATOR.test(event.key)) {
      event.preventDefault();
      this.commitRecipientEdit();
      this.recipientsInput()?.focus();
    }
  }

  // Replaces the address edited where it is. Emptied, it goes away; with several addresses
  // pasted in, they all take its place.
  protected commitRecipientEdit(): void {
    const edited = this.editedRecipient();
    if (edited === null) return;
    this.editedRecipient.set(null);

    const control = this.form.controls.recipients;
    const index = control.value.indexOf(edited);
    if (index < 0) return;
    const values = [...control.value];
    values.splice(index, 1, ...this.recipientDraft().split(RECIPIENT_SEPARATOR));
    const next = normalizeEmailRecipients(values);
    if (next.length === control.value.length && next.every((value, i) => value === control.value[i])) {
      return;
    }
    control.setValue(next);
    control.markAsDirty();
  }

  // Turns the chip of the address into a text field, in its place in the list.
  private editRecipient(index: number): void {
    const control = this.form.controls.recipients;
    const value = control.value[index];
    if (value === undefined || this.isSending() || control.disabled) return;

    this.commitRecipientEdit();
    if (!control.value.includes(value)) return;
    this.recipientsField().focusedMultipleOptionIndex.set(-1);
    this.recipientDraft.set(value);
    this.editedRecipient.set(value);
  }

  private recipientsInput(): HTMLInputElement | null {
    return (this.recipientsField().multiInputEl?.nativeElement as HTMLInputElement | undefined) ?? null;
  }

  protected send(): void {
    if (this.isSending()) return;
    this.submitted.set(true);
    if (this.form.invalid || this.selectedCount() === 0 || this.selectionError()) {
      this.form.markAllAsTouched();
      return;
    }

    const { recipients, subject, message } = this.form.getRawValue();
    const input: ApplicationDocumentationEmailInput = {
      applicationId: this.applicationId(),
      recipients: normalizeEmailRecipients(recipients),
      subject: subject.trim(),
      message: message.trim() || null,
      attachments: [...this.selection().values()].map(({ documentId, versionId }) => ({
        documentId,
        versionId,
      })),
    };
    this.isSending.set(true);
    this.service
      .sendByEmail(input)
      .pipe(
        finalize(() => this.isSending.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: this.texts.sentTitle,
            detail: this.texts.sentDetail(input.recipients.length),
          });
          this.closed.emit();
        },
        error: (error: unknown) => this.showSendError(error),
      });
  }

  protected close(): void {
    if (!this.isSending()) this.closed.emit();
  }

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected describedBy(control: AbstractControl, id: string, hintId?: string): string | null {
    const ids = [hintId, this.isInvalid(control) ? `${id}-error` : null].filter(Boolean);
    return ids.length ? ids.join(' ') : null;
  }

  // In multiple mode the text input is bound to the `input` section, which PrimeNG 21 leaves out
  // of the type of its pass-through options.
  protected recipientsPassThrough(): AutoCompletePassThrough {
    const control = this.form.controls.recipients;
    return {
      input: {
        'aria-describedby':
          this.describedBy(control, this.ids.recipients, `${this.ids.recipients}-hint`) ??
          undefined,
        'aria-invalid': this.isInvalid(control),
        'aria-required': true,
      },
    } as AutoCompletePassThrough;
  }

  protected recipientsError(): string {
    const errors = this.form.controls.recipients.errors;
    if (errors?.['invalidRecipients']) {
      const values = errors['invalidRecipients'].values as string[];
      return this.texts.errors.recipientsInvalid(values.join(', '));
    }
    if (errors?.['maxRecipients']) {
      return this.texts.errors.recipientsTooMany(errors['maxRecipients'].max);
    }
    return this.texts.errors.recipientsRequired;
  }

  protected subjectError(): string {
    const control = this.form.controls.subject;
    if (control.hasError('maxlength')) return this.common.maxLength(this.subjectMaxLength);
    if (control.hasError('lineBreak')) return this.texts.errors.subjectLineBreak;
    return this.common.required;
  }

  protected messageError(): string {
    return this.common.maxLength(this.messageMaxLength);
  }

  private loadVersions(attachment: ApplicationAttachment, currentVersionId: number): void {
    this.service
      .getDocument(attachment.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (document) => {
          const versions = document.attachments.map(({ id, versionLabel, fileSize }) => ({
            id,
            label: versionLabel,
            fileSize,
          }));
          this.versionsCache.set(attachment.id, versions);
          this.updateSelection(attachment.id, (selected) => ({ ...selected, versions }));
        },
        error: () => {
          // Choosing the document again reads them again.
          const versions = [
            { id: currentVersionId, label: attachment.versionLabel ?? '—', fileSize: null },
          ];
          this.updateSelection(attachment.id, (selected) => ({ ...selected, versions }));
          this.messageService.add({
            severity: 'warn',
            summary: this.common.infoSummary,
            detail: this.texts.versionsLoadError(attachment.title),
          });
        },
      });
  }

  private updateSelection(
    documentId: number,
    change: (selected: ApplicationDocumentationEmailSelection) => ApplicationDocumentationEmailSelection,
  ): void {
    const selected = this.selection().get(documentId);
    if (!selected) return;
    this.selection.set(new Map(this.selection()).set(documentId, change(selected)));
  }

  private requestPage(params: ApplicationAttachmentPageParams): void {
    this.lastParams = params;
    this.searchRequests.next(params);
  }

  private observeSearchRequests(): void {
    this.searchRequests
      .pipe(
        tap(() => this.isLoading.set(true)),
        switchMap((params) =>
          this.service.getAttachments(this.applicationId(), params).pipe(
            map((page) => ({ items: page.content, total: page.totalElements })),
            catchError(() => {
              this.showError(APPLICATION_ATTACHMENTS_TEXTS.loadError);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((list) => {
        this.isLoading.set(false);
        this.isSearchPending.set(false);
        if (list) this.page.set(list);
      });
  }

  private observeQuickSearch(): void {
    this.quickSearchChanges
      .pipe(
        debounceTime(QUICK_SEARCH_DEBOUNCE_MS),
        map((value) => value.trim()),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((search) => {
        this.first.set(0);
        this.requestPage({ ...this.lastParams, page: 0, search: search || undefined });
      });
  }

  // A structured 400 is shown by the global error dialog; the rest of the failures here.
  private showSendError(error: unknown): void {
    if (isStructuredBadRequest(error)) return;
    const detail =
      error instanceof HttpErrorResponse && error.status === 403
        ? this.texts.forbidden
        : (readApiErrorMessage(error) ?? this.texts.sendError);
    this.showError(detail);
  }

  private showError(detail: string): void {
    this.messageService.add({ severity: 'error', summary: this.common.errorSummary, detail });
  }
}
