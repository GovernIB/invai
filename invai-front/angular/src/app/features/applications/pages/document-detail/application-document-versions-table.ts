import { ChangeDetectionStrategy, Component, computed, inject, input, LOCALE_ID, signal, ViewEncapsulation } from '@angular/core';
import { formatDate, formatNumber } from '@angular/common';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { KeyLabel } from '@models/table.model';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { TableModule } from 'primeng/table';
import { ApplicationDocumentVersion } from '../../application-documentation.model';
import { APPLICATION_ATTACHMENT_DIALOG_TEXTS, APPLICATION_DOCUMENTATION_TEXTS } from '../detail/sections/documentation/application-documentation.i18n';
import { DOCUMENT_DETAIL_TEXTS } from './application-document-detail.i18n';

export enum DocumentVersionAction { View = 1, Download = 2 }
export const DOCUMENT_VERSION_COLUMNS: KeyLabel[] = [
  { key: 'versionLabel', label: APPLICATION_ATTACHMENT_DIALOG_TEXTS.labels.version, sortBy: 'versionLabel', minWidth: '7rem' },
  { key: 'uploadedAt', label: DOCUMENT_DETAIL_TEXTS.uploadedAt, sortBy: 'uploadedAt', minWidth: '10rem' },
  { key: 'uploadedBy', label: DOCUMENT_DETAIL_TEXTS.uploadedBy, sortBy: 'uploadedBy', minWidth: '14rem' },
  { key: 'fileSize', label: DOCUMENT_DETAIL_TEXTS.size, sortBy: 'fileSize', minWidth: '7rem' },
];

@Component({
  selector: 'app-application-document-versions-table',
  imports: [Button, Menu, TableModule],
  templateUrl: './application-document-versions-table.html',
  styleUrls: ['../../../../shared/styles/development-maintenance-table.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDocumentVersionsTable extends TableComponentBase<ApplicationDocumentVersion> {
  first = input(0);
  protected readonly locale = inject(LOCALE_ID);
  protected readonly texts = DOCUMENT_DETAIL_TEXTS;
  protected readonly actionsHeader = APPLICATION_DOCUMENTATION_TEXTS.actionsHeader;
  protected readonly icons = PrimeIcons;
  protected readonly Action = DocumentVersionAction;
  protected readonly selectedRow = signal<ApplicationDocumentVersion | null>(null);
  protected readonly menuVisible = signal(false);
  protected readonly rowActions = computed<MenuItem[]>(() => [
    { label: this.texts.downloadVersion, icon: PrimeIcons.DOWNLOAD, command: () => this.select(DocumentVersionAction.Download) },
    { label: this.texts.viewVersion, icon: PrimeIcons.EYE, command: () => this.select(DocumentVersionAction.View) },
  ]);

  cellText(row: ApplicationDocumentVersion, key: string): string {
    if (key === 'uploadedAt') return formatDate(row.uploadedAt, 'dd/MM/yy', this.locale);
    if (key === 'fileSize') return formatNumber(row.fileSize / 1000000, this.locale, '1.1-1') + ' MB';
    return String(row[key as keyof ApplicationDocumentVersion]);
  }

  protected openMenu(event: Event, row: ApplicationDocumentVersion, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private select(action: DocumentVersionAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
