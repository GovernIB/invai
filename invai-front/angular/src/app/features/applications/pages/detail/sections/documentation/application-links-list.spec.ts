import { HttpErrorResponse } from '@angular/common/http';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentCategoryOption } from '@features/document-categories/document-categories.model';
import { DocumentCategoryCatalogService } from '@features/document-categories/services/document-category-catalog.service';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { ApplicationLink } from '../../../../application-documentation.model';
import { DOCUMENT_CATEGORIES, documentationPage } from '../../../../application-documentation.testing';
import { ApplicationLinkFormGroup } from '../../../../forms/application-documentation-form.factory';
import { ApplicationLinksService } from '../../../../services/application-links.service';
import { ApplicationDocumentationAction } from './application-documentation-table-action';
import { ApplicationLinksList } from './application-links-list';

const LINKS: ApplicationLink[] = [
  {
    id: 1,
    title: 'Postman',
    description: "Proves i col·laboració d'API",
    category: DOCUMENT_CATEGORIES[0],
    url: 'https://www.postman.com',
    deletedAt: null,
  },
  {
    id: 2,
    title: 'Grafana',
    description: null,
    category: DOCUMENT_CATEGORIES[1],
    url: 'https://grafana.com',
    deletedAt: null,
  },
];

// Active categories, as the catalog gives them: sorted by their label.
const CATEGORY_OPTIONS: DocumentCategoryOption[] = [
  { id: 4, label: 'Base de dades' },
  { id: 1, label: 'General' },
  { id: 3, label: 'Sistemes' },
];

interface ListInternals {
  entityForm: ApplicationLinkFormGroup;
  filtersForm: { patchValue(value: object): void };
  dialogMode: () => string;
  isDialogVisible: () => boolean;
  categories: () => DocumentCategoryOption[];
  dialogCategoryOptions: () => DocumentCategoryOption[];
  selectedCategory: () => ApplicationLink['category'] | null;
  openCreateDialog(): void;
  onTableAction(event: { action: number; params: ApplicationLink }): void;
  onFilterSearch(): void;
  onQuickSearchChange(value: string): void;
  startEdit(): void;
  cancelEdit(): void;
  submit(): void;
  confirmDelete(): void;
}

describe('ApplicationLinksList', () => {
  let fixture: ComponentFixture<ApplicationLinksList>;
  let list: ListInternals;
  let add: ReturnType<typeof vi.spyOn>;
  const getActiveCategoryOptions = vi.fn();
  const links = {
    getLinks: vi.fn(),
    createLink: vi.fn(),
    updateLink: vi.fn(),
    deleteLink: vi.fn(),
  };

  async function create(withInitialPage = true): Promise<void> {
    fixture = TestBed.createComponent(ApplicationLinksList);
    fixture.componentRef.setInput('applicationId', 3);
    fixture.componentRef.setInput('canManage', true);
    if (withInitialPage) fixture.componentRef.setInput('initialPage', documentationPage(LINKS));
    fixture.detectChanges();
    list = fixture.componentInstance as unknown as ListInternals;
  }

  const items = () => fixture.componentInstance.itemsList();
  const lastQuery = () => links.getLinks.mock.lastCall;
  const badRequest = () =>
    new HttpErrorResponse({ status: 400, error: { error: 'Bad Request', message: 'Enllaç repetit' } });

  beforeEach(async () => {
    getActiveCategoryOptions.mockReset().mockReturnValue(of(CATEGORY_OPTIONS));
    links.getLinks.mockReset().mockReturnValue(of(documentationPage(LINKS)));
    links.createLink.mockReset().mockReturnValue(of({ ...LINKS[0], id: 9 }));
    links.updateLink.mockReset().mockReturnValue(of(LINKS[0]));
    links.deleteLink.mockReset().mockReturnValue(of(undefined));
    await TestBed.configureTestingModule({
      imports: [ApplicationLinksList],
      providers: [
        MessageService,
        { provide: LOCALE_ID, useValue: 'ca' },
        { provide: ApplicationLinksService, useValue: links },
        { provide: DocumentCategoryCatalogService, useValue: { getActiveCategoryOptions } },
      ],
    })
      .overrideComponent(ApplicationLinksList, { set: { template: '' } })
      .compileComponents();
    add = vi.spyOn(TestBed.inject(MessageService), 'add');
  });

  it('shows the resolved page without reading it again', async () => {
    await create();

    expect(links.getLinks).not.toHaveBeenCalled();
    expect([items().items, items().total]).toEqual([LINKS, 2]);
  });

  it('reads the first page itself, leaving out the deleted links, when the resolver could not', async () => {
    await create(false);

    expect(lastQuery()).toEqual([3, expect.objectContaining({ page: 0, deleted: false })]);
    expect(items().total).toBe(2);
  });

  it('offers the active categories, for the filter and the new links', async () => {
    await create();

    expect(getActiveCategoryOptions).toHaveBeenCalledTimes(1);
    expect(list.categories()).toEqual(CATEGORY_OPTIONS);
    list.openCreateDialog();
    expect(list.dialogCategoryOptions()).toEqual(CATEGORY_OPTIONS);
  });

  it('reads the categories again when the dialog opens after they failed', async () => {
    getActiveCategoryOptions.mockReturnValueOnce(throwError(() => new Error('offline')));
    await create();
    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ detail: "No s'han pogut carregar les categories documentals." }),
    );
    expect(list.categories()).toEqual([]);

    list.openCreateDialog();

    expect(list.categories()).toHaveLength(3);
  });

  it('keeps the deactivated category of a link selectable while consulting or editing it', async () => {
    const archived = { id: 7, name: 'Arxiu', nameEs: 'Archivo', parentId: null };
    await create();

    list.onTableAction({ action: ApplicationDocumentationAction.View, params: { ...LINKS[0], category: archived } });
    expect(list.dialogCategoryOptions()).toEqual([...CATEGORY_OPTIONS, { id: 7, label: 'Arxiu' }]);
    expect(list.entityForm.controls.categoryId.value).toBe(7);

    list.openCreateDialog();
    expect(list.dialogCategoryOptions()).toEqual(CATEGORY_OPTIONS);
  });

  it('filters by category, searches and sorts by the keys of the back-end', async () => {
    vi.useFakeTimers();
    try {
      await create();

      list.filtersForm.patchValue({ categoryId: 3 });
      list.onFilterSearch();
      expect(lastQuery()?.[1]).toMatchObject({ categoryId: 3, deleted: false });
      expect(lastQuery()?.[1]).not.toHaveProperty('sectionId');
      expect(fixture.componentInstance.selectedFilters()).toBe(1);

      list.onQuickSearchChange('  grafana ');
      vi.advanceTimersByTime(400);
      expect(lastQuery()?.[1]).toMatchObject({ quickSearch: 'grafana', categoryId: 3, deleted: false });

      fixture.componentInstance.onSearch({ first: 10, rows: 10, sortField: 'url', sortOrder: -1 });
      expect(lastQuery()?.[1]).toMatchObject({ page: 1, sort: 'url,desc', deleted: false });
    } finally {
      vi.useRealTimers();
    }
  });

  it('adds a link with a valid URL and an optional description', async () => {
    await create();
    list.openCreateDialog();
    list.entityForm.setValue({ title: ' Portal ', description: '  ', categoryId: 1, url: 'portal.cat' });
    list.submit();
    expect(links.createLink).not.toHaveBeenCalled();
    expect(list.isDialogVisible()).toBe(true);

    list.entityForm.controls.url.setValue('https://portal.cat');
    list.submit();

    expect(links.createLink).toHaveBeenCalledWith(3, {
      title: 'Portal',
      description: null,
      categoryId: 1,
      url: 'https://portal.cat',
    });
    expect(list.isDialogVisible()).toBe(false);
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ summary: 'Enllaç afegit' }));
    expect(lastQuery()?.[1]).toMatchObject({ deleted: false });
  });

  it('refuses an upper-case scheme, as the back-end does', async () => {
    await create();
    list.openCreateDialog();
    list.entityForm.setValue({ title: 'Portal', description: '', categoryId: 1, url: 'HTTPS://portal.cat' });

    list.submit();

    expect(list.entityForm.controls.url.hasError('pattern')).toBe(true);
    expect(links.createLink).not.toHaveBeenCalled();
  });

  it('consults, edits, cancels and saves a link by its own id', async () => {
    await create();
    list.onTableAction({ action: ApplicationDocumentationAction.View, params: LINKS[0] });
    expect([list.dialogMode(), list.entityForm.controls.categoryId.value]).toEqual(['view', 1]);
    expect(list.selectedCategory()).toEqual(DOCUMENT_CATEGORIES[0]);

    list.startEdit();
    list.entityForm.controls.title.setValue('Una altra');
    list.cancelEdit();
    expect([list.dialogMode(), list.entityForm.controls.title.value]).toEqual(['view', 'Postman']);

    list.startEdit();
    list.entityForm.controls.categoryId.setValue(4);
    list.submit();

    expect(links.updateLink).toHaveBeenCalledWith(1, {
      title: 'Postman',
      description: "Proves i col·laboració d'API",
      categoryId: 4,
      url: 'https://www.postman.com',
    });
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ summary: 'Enllaç actualitzat' }));
  });

  it('leaves the back-end message to the error dialog and reports other failures itself', async () => {
    await create();
    list.openCreateDialog();
    list.entityForm.setValue({ title: 'Postman', description: '', categoryId: 1, url: 'https://www.postman.com' });
    links.createLink.mockReturnValueOnce(throwError(badRequest));
    list.submit();
    expect(add).not.toHaveBeenCalled();
    expect(list.isDialogVisible()).toBe(true);

    links.createLink.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })));
    list.submit();
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ detail: "No s'ha pogut desar l'enllaç." }));
  });

  it('reads the page again when an edited link is no longer active', async () => {
    await create();
    list.onTableAction({ action: ApplicationDocumentationAction.View, params: LINKS[1] });
    list.startEdit();
    links.updateLink.mockReturnValueOnce(throwError(badRequest));

    list.submit();

    expect(add).not.toHaveBeenCalled();
    expect(links.getLinks).toHaveBeenCalledTimes(1);
    expect(list.isDialogVisible()).toBe(true);
  });

  it('deletes a link after confirming it', async () => {
    await create();
    links.getLinks.mockReturnValue(of(documentationPage([LINKS[1]])));

    list.onTableAction({ action: ApplicationDocumentationAction.Delete, params: LINKS[0] });
    list.confirmDelete();

    expect(links.deleteLink).toHaveBeenCalledWith(1);
    expect(items().items.map(({ title }) => title)).toEqual(['Grafana']);
    expect(add).toHaveBeenCalledWith(expect.objectContaining({ summary: 'Enllaç eliminat' }));
  });

  it('reads the page again after a failed deletion', async () => {
    await create();
    links.deleteLink.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })));

    list.onTableAction({ action: ApplicationDocumentationAction.Delete, params: LINKS[0] });
    list.confirmDelete();

    expect(add).toHaveBeenCalledWith(expect.objectContaining({ detail: "No s'ha pogut eliminar l'enllaç." }));
    expect(links.getLinks).toHaveBeenCalledTimes(1);
  });
});
