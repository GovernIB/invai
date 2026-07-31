import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Commission, CommissionType } from '../../commissions.model';
import { CommissionsService } from '../../services/commissions.service';
import { CommissionsList } from './commissions-list';
import {
  COMMISSIONS_LIST_RESOLVE_KEY,
  CommissionsListResolvedData,
} from './commissions-list.resolver';

const COMMISSION: Commission = {
  id: 1,
  name: 'Comissió tècnica',
  nameEs: 'Comisión técnica',
  expedientNumber: 'EXP-1',
  approvalDate: '2026-07-17',
  commissionType: CommissionType.TECNICA,
  deletedAt: null,
};

describe('CommissionsList', () => {
  let fixture: ComponentFixture<CommissionsList>;
  let getAll: ReturnType<typeof vi.fn>;
  let routeData: CommissionsListResolvedData;

  beforeEach(async () => {
    getAll = vi.fn(() => of(page([COMMISSION])));
    routeData = { page: page([COMMISSION]), pageLoadFailed: false };

    await TestBed.configureTestingModule({
      imports: [CommissionsList],
      providers: [
        MessageService,
        { provide: CommissionsService, useValue: { getAll } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: { [COMMISSIONS_LIST_RESOLVE_KEY]: routeData },
            },
          },
        },
      ],
    })
      .overrideComponent(CommissionsList, { set: { template: '' } })
      .compileComponents();
  });

  it('should consume the resolved commission page without requesting it again', () => {
    fixture = TestBed.createComponent(CommissionsList);
    fixture.detectChanges();

    expect(getAll).not.toHaveBeenCalled();
    expect(fixture.componentInstance.itemsList()).toEqual({
      items: [COMMISSION],
      total: 1,
    });
    expect(fixture.componentInstance.selectedFilters()).toBe(1);
  });

  it('should keep later searches in the component', () => {
    fixture = TestBed.createComponent(CommissionsList);
    fixture.detectChanges();

    (fixture.componentInstance as unknown as { onFilterSearch: () => void }).onFilterSearch();

    expect(getAll).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      sort: undefined,
      name: undefined,
      nameEs: undefined,
      expedientNumber: undefined,
      approvalDateFrom: undefined,
      approvalDateTo: undefined,
      commissionType: undefined,
      statusId: SoftDeleteStatus.ACTIVE,
      quickSearch: undefined,
    });
  });

  it('should omit statusId when all statuses are applied', () => {
    fixture = TestBed.createComponent(CommissionsList);
    fixture.detectChanges();
    const list = fixture.componentInstance as unknown as {
      filtersForm: FormGroup;
      onFilterSearch(): void;
    };

    list.filtersForm.controls['status'].setValue(null);
    list.onFilterSearch();

    expect(getAll).toHaveBeenCalledWith(
      expect.objectContaining({ statusId: undefined }),
    );
    expect(fixture.componentInstance.selectedFilters()).toBe(0);
  });
});

function page<TItem>(content: TItem[]): SpringPage<TItem> {
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
