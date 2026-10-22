import { Component, signal, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams } from '@models/table.model';
import { MenuItem } from 'primeng/api';

import { ApplicationAttachment, ApplicationLink } from '../../../../application-documentation.model';
import { DOCUMENT_SECTIONS, DOCUMENT_STATUSES, documentRow } from '../../../../application-documentation.testing';
import { ApplicationAttachmentsTable } from './application-attachments-table';
import {
  APPLICATION_ATTACHMENTS_COLUMNS,
  APPLICATION_LINKS_COLUMNS,
} from './application-documentation.constants';
import { ApplicationDocumentationAction } from './application-documentation-table-action';
import { ApplicationLinksTable } from './application-links-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const attachment = (id: number, title: string, statusIndex: number): ApplicationAttachment =>
  documentRow({
    id,
    title,
    section: DOCUMENT_SECTIONS[1],
    subcategory: null,
    versionLabel: '1.0',
    status: DOCUMENT_STATUSES[statusIndex],
  });

const LINK: ApplicationLink = {
  id: 9,
  title: 'Swagger',
  description: null,
  section: { id: 9, code: 'INTEGRATIONS', name: 'Integracions', nameEs: 'Integraciones' },
  url: 'https://swagger.io',
  deletedAt: null,
};

@Component({
  imports: [ApplicationAttachmentsTable, ApplicationLinksTable],
  template: `
    <app-application-attachments-table
      [canManage]="canManage()"
      [columns]="attachmentColumns"
      [itemsList]="{ items: attachments, total: attachments.length }"
      (onSelectAction)="actions.push($event)"
    />
    <app-application-links-table
      [canManage]="canManage()"
      [columns]="linkColumns"
      [itemsList]="{ items: [link], total: 1 }"
      (onSelectAction)="actions.push($event)"
    />
  `,
})
class HostComponent {
  readonly attachmentColumns = APPLICATION_ATTACHMENTS_COLUMNS;
  readonly linkColumns = APPLICATION_LINKS_COLUMNS;
  readonly attachments = [
    attachment(1, 'Manual', 0),
    attachment(2, 'Guia', 1),
    { ...attachment(3, 'Protocol', 2), mimeType: null, currentAttachmentId: null },
  ];
  readonly link = LINK;
  readonly canManage = signal(true);
  readonly actions: ActionParams<ApplicationAttachment | ApplicationLink>[] = [];
  readonly attachmentsTable = viewChild.required(ApplicationAttachmentsTable);
  readonly linksTable = viewChild.required(ApplicationLinksTable);
}

describe('Application documentation tables', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  const root = () => fixture.nativeElement as HTMLElement;
  function menu(button: string, table: 'attachmentsTable' | 'linksTable'): MenuItem[] {
    root().querySelector<HTMLButtonElement>(`button[aria-label="${button}"]`)!.click();
    fixture.detectChanges();
    return (host[table]() as unknown as { rowActions: () => MenuItem[] }).rowActions();
  }
  async function render(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [HostComponent] }).compileComponents();
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    await render();
  });

  it('shows each attachment with the labels of its catalogs and a coloured state tag', () => {
    const rows = [...root().querySelectorAll("table[aria-label=\"Adjunts de l'aplicació\"] tbody tr")];
    expect([...rows[0].querySelectorAll('td')].map((cell) => cell.textContent?.trim())).toEqual([
      'Manual',
      'Sistemes',
      'PDF',
      'Tècnica',
      '—',
      '1.0',
      'Vigent',
      '',
    ]);
    expect(rows[2].querySelectorAll('td')[2].textContent?.trim()).toBe('—');
    expect(rows.map((row) => row.querySelector('.p-tag')?.className)).toEqual([
      expect.stringContaining('invai-status-tag--active'),
      expect.stringContaining('application-attachment-state-tag--review'),
      expect.stringContaining('application-attachment-state-tag--obsolete'),
    ]);
    for (const table of root().querySelectorAll('p-table')) {
      expect(table.classList).toContain('p-datatable-striped');
    }
  });

  it('opens each URL in a new tab and says so', () => {
    const link = root().querySelector<HTMLAnchorElement>('a[href="https://swagger.io"]')!;

    expect([link.target, link.rel, link.textContent?.trim()]).toEqual([
      '_blank',
      'noopener noreferrer',
      "https://swagger.io (s'obre en una pestanya nova)",
    ]);
  });

  it('names the section of a link as the back-end does', () => {
    const row = root().querySelector('table[aria-label="Enllaços de l\'aplicació"] tbody tr')!;

    expect(row.textContent).toContain('Integracions');
  });

  it('offers deleting only with permission to manage the application', async () => {
    expect(menu("Accions de l'adjunt «Guia»", 'attachmentsTable').map(({ label }) => label)).toEqual([
      'Consulta',
      'Descarrega',
      'Elimina',
    ]);
    expect(menu("Accions de l'enllaç «Swagger»", 'linksTable').map(({ label }) => label)).toEqual([
      'Consulta',
      'Elimina',
    ]);

    host.canManage.set(false);
    await render();

    expect(menu("Accions de l'adjunt «Guia»", 'attachmentsTable').map(({ label }) => label)).toEqual([
      'Consulta',
      'Descarrega',
    ]);
    expect(menu("Accions de l'enllaç «Swagger»", 'linksTable').map(({ label }) => label)).toEqual([
      'Consulta',
    ]);
  });

  it('offers downloading only a document with a current version', () => {
    expect(menu("Accions de l'adjunt «Protocol»", 'attachmentsTable').map(({ label }) => label)).toEqual([
      'Consulta',
      'Elimina',
    ]);
  });

  it('emits the chosen action and consults a row on double click', () => {
    menu("Accions de l'adjunt «Guia»", 'attachmentsTable')[1].command?.({} as never);
    root()
      .querySelector("table[aria-label=\"Enllaços de l'aplicació\"] tbody tr")!
      .dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));

    expect(host.actions).toEqual([
      { action: ApplicationDocumentationAction.Download, params: host.attachments[1] },
      { action: ApplicationDocumentationAction.View, params: LINK },
    ]);
  });
});
