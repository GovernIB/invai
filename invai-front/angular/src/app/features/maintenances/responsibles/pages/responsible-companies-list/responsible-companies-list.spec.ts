import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';
import { ActionParams, PaginatedList } from '@models/table.model';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { ResponsibleMaintenanceTableAction } from '../../components/responsible-maintenance-tables/responsible-maintenance-tables';
import { ResponsibleCompanyFormGroup } from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_COMPANY_COPY } from '../../responsibles.i18n';
import { ResponsibleCompany } from '../../responsibles.model';
import { ResponsibleCompaniesService } from '../../services/responsible-companies.service';
import { ResponsibleCompaniesList } from './responsible-companies-list';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}
globalThis.ResizeObserver ??= ResizeObserverMock;

interface CompaniesListHarness {
  entityForm: ResponsibleCompanyFormGroup;
  itemsList(): PaginatedList<ResponsibleCompany>;
  dialogMode(): string;
  isDialogVisible(): boolean;
  openCreateDialog(): void;
  submitEntity(): void;
  onTableAction(event: ActionParams<ResponsibleCompany>): void;
  startEntityEdit(): void;
  cancelEntityEdit(): void;
  confirmDelete(): void;
}

describe('ResponsibleCompaniesList', () => {
  let fixture: ComponentFixture<ResponsibleCompaniesList>;
  let component: CompaniesListHarness;
  let companies: ResponsibleCompany[];
  let addMessage: ReturnType<typeof vi.fn>;
  let deactivate: ReturnType<typeof vi.fn>;
  let getPage: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    companies = [{ id: 1, name: 'Plexus', nif: 'B12345678', deletedAt: null }];
    addMessage = vi.fn();
    deactivate = vi.fn(() => of(undefined));
    getPage = vi.fn(() => of(page(companies)));
    const companiesService = {
      getPage,
      getById: vi.fn((id: number) => of(companies.find((item) => item.id === id)!)),
      create: vi.fn((input: { name: string; nif: string | null }) => {
        const company = { id: 2, name: input.name, nif: input.nif, deletedAt: null };
        companies = [...companies, company];
        return of(company);
      }),
      update: vi.fn(),
      deactivate,
      reactivate: vi.fn(),
    };
    await TestBed.configureTestingModule({
      imports: [ResponsibleCompaniesList],
      providers: [
        { provide: MessageService, useValue: { add: addMessage } },
        { provide: ResponsibleCompaniesService, useValue: companiesService },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(ResponsibleCompaniesList);
    component = fixture.componentInstance as unknown as CompaniesListHarness;
    fixture.detectChanges();
  });

  it('creates a company and refreshes only its current list', () => {
    component.openCreateDialog();
    component.entityForm.setValue({ name: 'Nova empresa', nif: '' });
    component.submitEntity();
    expect(component.itemsList().total).toBe(2);
    expect(component.itemsList().items.some(({ name }) => name === 'Nova empresa')).toBe(true);
    expect(component.isDialogVisible()).toBe(false);
    expect(addMessage).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success', detail: RESPONSIBLE_COMPANY_COPY.created }),
    );
  });

  it('restores the selected snapshot when edit is cancelled', () => {
    const company = component.itemsList().items[0];
    component.onTableAction({ action: ResponsibleMaintenanceTableAction.View, params: company });
    component.startEntityEdit();
    component.entityForm.controls.name.setValue('Canvi temporal');
    component.entityForm.controls.nif.setValue('CHANGED');
    component.cancelEntityEdit();
    expect(component.dialogMode()).toBe('view');
    expect(component.entityForm.controls.name.value).toBe('Plexus');
    expect(component.entityForm.controls.nif.value).toBe('B12345678');
  });

  it('preserves NIF when saving another field and sends null when explicitly cleared', () => {
    const service = TestBed.inject(ResponsibleCompaniesService);
    vi.mocked(service.update).mockImplementation((id, input) => of({ id, ...input, deletedAt: null }));
    for (const nif of ['B12345678', '']) {
      component.onTableAction({ action: ResponsibleMaintenanceTableAction.Edit, params: companies[0] });
      component.entityForm.controls.name.setValue('Updated');
      component.entityForm.controls.nif.setValue(nif);
      component.submitEntity();
      expect(service.update).toHaveBeenLastCalledWith(1, { name: 'Updated', nif: nif || null });
    }
  });

  it('delegates company deactivation to the backend and refreshes the current list', () => {
    const company = component.itemsList().items[0];
    component.onTableAction({ action: ResponsibleMaintenanceTableAction.Edit, params: company });
    component.onTableAction({
      action: ResponsibleMaintenanceTableAction.Deactivate,
      params: company,
    });
    component.confirmDelete();
    expect(deactivate).toHaveBeenCalledWith(company.id);
    expect(getPage).toHaveBeenCalledTimes(2);
    expect(component.isDialogVisible()).toBe(false);
    expect(addMessage).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'success',
        detail: RESPONSIBLE_COMPANY_COPY.deactivated,
      }),
    );
  });
});

function page<T>(content: T[]): SpringPage<T> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: 1,
  };
}
