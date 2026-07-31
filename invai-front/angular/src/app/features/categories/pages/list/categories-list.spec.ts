import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Category } from '../../categories.model';
import { CategoriesService } from '../../services/categories.service';
import { CategoriesList } from './categories-list';
import {
  CATEGORIES_LIST_RESOLVE_KEY,
  CategoriesListResolvedData,
} from './categories-list.resolver';

const CATEGORY: Category = {
  id: 1,
  name: 'DRASSANA',
  nameEs: 'ASTILLERO',
  deletedAt: null,
};

describe('CategoriesList', () => {
  let fixture: ComponentFixture<CategoriesList>;
  let getAll: ReturnType<typeof vi.fn>;
  let routeData: CategoriesListResolvedData;

  beforeEach(async () => {
    getAll = vi.fn(() => of(page([CATEGORY])));
    routeData = { page: page([CATEGORY]), pageLoadFailed: false };

    await TestBed.configureTestingModule({
      imports: [CategoriesList],
      providers: [
        MessageService,
        { provide: CategoriesService, useValue: { getAll } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: { [CATEGORIES_LIST_RESOLVE_KEY]: routeData },
            },
          },
        },
      ],
    })
      .overrideComponent(CategoriesList, { set: { template: '' } })
      .compileComponents();
  });

  it('should consume the resolved category page without requesting it again', () => {
    fixture = TestBed.createComponent(CategoriesList);
    fixture.detectChanges();

    expect(getAll).not.toHaveBeenCalled();
    expect(fixture.componentInstance.itemsList()).toEqual({
      items: [CATEGORY],
      total: 1,
    });
    expect(fixture.componentInstance.selectedFilters()).toBe(1);
  });

  it.each([SoftDeleteStatus.INACTIVE, null])(
    'should warn, restore active and avoid requests for unsupported status %s',
    (unsupportedStatus) => {
    fixture = TestBed.createComponent(CategoriesList);
    const add = vi.spyOn(TestBed.inject(MessageService), 'add');
    fixture.detectChanges();
    const filtersForm = (
      fixture.componentInstance as unknown as { filtersForm: FormGroup }
    ).filtersForm;

      filtersForm.patchValue({ status: unsupportedStatus });

      expect(filtersForm.controls['status'].value).toBe(SoftDeleteStatus.ACTIVE);
      expect(getAll).not.toHaveBeenCalled();
      expect(add).toHaveBeenCalledWith(
        expect.objectContaining({ severity: 'info', detail: expect.stringContaining('inactius') }),
      );
    },
  );

  it('should keep later searches in the component', () => {
    fixture = TestBed.createComponent(CategoriesList);
    fixture.detectChanges();

    (fixture.componentInstance as unknown as { onFilterSearch: () => void }).onFilterSearch();

    expect(getAll).toHaveBeenCalledWith({ page: 0, size: 10 });
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
