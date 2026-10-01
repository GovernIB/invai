import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { ActionParams } from '@models/table.model';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { ExternalSystemTableAction } from '../../components/external-systems-table/external-systems-table';
import { ExternalSystem } from '../../external-systems.model';
import { ExternalSystemsService } from '../../services/external-systems.service';
import { ExternalSystemsList } from './external-systems-list';
import {
  EXTERNAL_SYSTEMS_LIST_RESOLVE_KEY,
  ExternalSystemsListResolvedData,
} from './external-systems-list.resolver';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

interface ListHarness {
  entityForm: FormGroup;
  isDialogVisible: () => boolean;
  isDeleteDialogVisible: () => boolean;
  dialogMode: () => string;
  dialogCompanyOptions: () => { id: number; label: string }[];
  openCreateDialog(): void;
  submitEntity(): void;
  confirmDelete(): void;
  onTableAction(event: ActionParams<ExternalSystem>): void;
}

const SOFFID: ExternalSystem = {
  id: 1,
  name: 'Soffid',
  company: { id: 3, name: 'Plexus Tech', nif: 'B00000000', deletedAt: null },
  deletedAt: null,
};
const RETIRED: ExternalSystem = {
  id: 2,
  name: 'Portal antic',
  company: { id: 9, name: 'Empresa antiga', nif: 'B99999999', deletedAt: '2026-01-01T00:00:00' },
  deletedAt: '2026-02-01T00:00:00',
};

function page(content: ExternalSystem[]): SpringPage<ExternalSystem> {
  return { content, totalElements: content.length, number: 0 } as SpringPage<ExternalSystem>;
}

describe('ExternalSystemsList', () => {
  let fixture: ComponentFixture<ExternalSystemsList>;
  let list: ListHarness;
  let messageService: MessageService;
  let resolved: ExternalSystemsListResolvedData;
  const service = {
    getAll: vi.fn(),
    getById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    reactivate: vi.fn(),
  };

  const root = () => fixture.nativeElement as HTMLElement;

  async function create(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ExternalSystemsList],
      providers: [
        MessageService,
        { provide: ExternalSystemsService, useValue: service },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: { [EXTERNAL_SYSTEMS_LIST_RESOLVE_KEY]: resolved } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ExternalSystemsList);
    fixture.componentRef.setInput('companyOptions', [{ id: 3, label: 'Plexus Tech' }]);
    fixture.componentRef.setInput('filterCompanyOptions', [
      { id: 3, label: 'Plexus Tech' },
      { id: 9, label: 'Empresa antiga' },
    ]);
    list = fixture.componentInstance as unknown as ListHarness;
    messageService = TestBed.inject(MessageService);
    vi.spyOn(messageService, 'add');
    fixture.detectChanges();
    await fixture.whenStable();
  }

  beforeEach(() => {
    for (const mock of Object.values(service)) mock.mockReset();
    service.getAll.mockReturnValue(of(page([SOFFID])));
    service.create.mockImplementation((input) => of({ ...SOFFID, id: 5, name: input.name }));
    service.update.mockImplementation((id, input) => of({ ...SOFFID, id, name: input.name }));
    service.delete.mockReturnValue(of(undefined));
    service.reactivate.mockReturnValue(of(RETIRED));
    resolved = { page: page([SOFFID]), pageLoadFailed: false };
  });

  it('renders the resolved page without repeating the initial request', async () => {
    await create();

    expect(service.getAll).not.toHaveBeenCalled();
    const cells = [...root().querySelectorAll('tbody tr td')].map((cell) => cell.textContent?.trim());
    expect(cells.slice(0, 2)).toEqual(['Soffid', 'Plexus Tech']);
    expect(
      [...root().querySelectorAll('thead th')].map((header) => header.textContent?.trim()).slice(0, 2),
    ).toEqual(['Nom', 'Empresa']);
  });

  it('reports a failed initial load', async () => {
    resolved = { page: null, pageLoadFailed: true };
    await create();

    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'error', detail: "No s'han pogut carregar els sistemes externs." }),
    );
  });

  it('validates and creates a system with its trimmed name and company identifier', async () => {
    await create();
    list.openCreateDialog();
    list.submitEntity();
    expect(list.entityForm.controls['name'].touched).toBe(true);
    expect(service.create).not.toHaveBeenCalled();

    list.entityForm.setValue({ name: '  DIR3CAIB  ', companyId: 3 });
    list.submitEntity();

    expect(service.create).toHaveBeenCalledWith({ name: 'DIR3CAIB', companyId: 3 });
    expect(list.isDialogVisible()).toBe(false);
    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success', summary: 'Sistema extern afegit' }),
    );
  });

  it('rejects names longer than 150 characters', async () => {
    await create();
    list.openCreateDialog();
    list.entityForm.setValue({ name: 'x'.repeat(151), companyId: 3 });
    list.submitEntity();

    expect(list.entityForm.controls['name'].hasError('maxlength')).toBe(true);
    expect(service.create).not.toHaveBeenCalled();
  });

  it('keeps an inactive current company selectable while editing', async () => {
    service.getById.mockReturnValue(of(RETIRED));
    await create();

    list.onTableAction({ action: ExternalSystemTableAction.Edit, params: RETIRED });

    expect(service.getById).toHaveBeenCalledWith(2);
    expect(list.dialogMode()).toBe('edit');
    expect(list.dialogCompanyOptions()).toEqual([
      { id: 3, label: 'Plexus Tech' },
      { id: 9, label: 'Empresa antiga' },
    ]);
    expect(list.entityForm.getRawValue()).toEqual({ name: 'Portal antic', companyId: 9 });
  });

  it('leaves structured save errors to the global dialog', async () => {
    await create();
    service.create.mockReturnValueOnce(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { error: 'Error', message: 'Ja existeix un sistema extern registrat amb aquest mateix nom.' },
          }),
      ),
    );
    list.openCreateDialog();
    list.entityForm.setValue({ name: 'Soffid', companyId: 3 });
    list.submitEntity();

    expect(list.isDialogVisible()).toBe(true);
    expect(messageService.add).not.toHaveBeenCalled();
  });

  it('confirms before deactivating and restores inactive systems', async () => {
    await create();

    list.onTableAction({ action: ExternalSystemTableAction.Delete, params: SOFFID });
    expect(list.isDeleteDialogVisible()).toBe(true);
    list.confirmDelete();
    expect(service.delete).toHaveBeenCalledWith(1);

    list.onTableAction({ action: ExternalSystemTableAction.Restore, params: RETIRED });
    expect(service.reactivate).toHaveBeenCalledWith(2);
    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success', summary: 'Sistema extern restaurat' }),
    );
  });
});
