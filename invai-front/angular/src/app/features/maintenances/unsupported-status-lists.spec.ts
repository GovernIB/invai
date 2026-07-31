import { Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { FieldsList } from '@features/fields/pages/list/fields-list';
import {
  FIELDS_LIST_RESOLVE_KEY,
  FieldsListResolvedData,
} from '@features/fields/pages/list/fields-list.resolver';
import { FieldsService } from '@features/fields/services/fields.service';
import { SystemTypesList } from '@features/system-types/pages/list/system-types-list';
import {
  SYSTEM_TYPES_LIST_RESOLVE_KEY,
  SystemTypesListResolvedData,
} from '@features/system-types/pages/list/system-types-list.resolver';
import { SystemTypesService } from '@features/system-types/services/system-types.service';

interface UnsupportedStatusList {
  filtersForm: FormGroup;
}

describe('maintenance lists with unsupported status endpoints', () => {
  let fieldsGetAll: ReturnType<typeof vi.fn>;
  let systemTypesGetAll: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    const resolvedPage = page([]);
    fieldsGetAll = vi.fn(() => of(resolvedPage));
    systemTypesGetAll = vi.fn(() => of(resolvedPage));

    const fieldsData: FieldsListResolvedData = {
      page: resolvedPage,
      pageLoadFailed: false,
    };
    const systemTypesData: SystemTypesListResolvedData = {
      page: resolvedPage,
      pageLoadFailed: false,
    };

    await TestBed.configureTestingModule({
      imports: [FieldsList, SystemTypesList],
      providers: [
        MessageService,
        { provide: FieldsService, useValue: { getAll: fieldsGetAll } },
        { provide: SystemTypesService, useValue: { getAll: systemTypesGetAll } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                [FIELDS_LIST_RESOLVE_KEY]: fieldsData,
                [SYSTEM_TYPES_LIST_RESOLVE_KEY]: systemTypesData,
              },
            },
          },
        },
      ],
    })
      .overrideComponent(FieldsList, { set: { template: '' } })
      .overrideComponent(SystemTypesList, { set: { template: '' } })
      .compileComponents();
  });

  it.each([
    ['àmbits', FieldsList, () => fieldsGetAll],
    ['tipus de sistema', SystemTypesList, () => systemTypesGetAll],
  ])(
    'restores active without requesting %s for inactive and all statuses',
    (_name, componentType, getAll) => {
      const fixture = TestBed.createComponent(componentType as Type<unknown>);
      const add = vi.spyOn(TestBed.inject(MessageService), 'add');
      fixture.detectChanges();
      const list = fixture.componentInstance as UnsupportedStatusList;

      for (const status of [SoftDeleteStatus.INACTIVE, null]) {
        list.filtersForm.controls['status'].setValue(status);
        expect(list.filtersForm.controls['status'].value).toBe(SoftDeleteStatus.ACTIVE);
      }

      expect(getAll()).not.toHaveBeenCalled();
      expect(add).toHaveBeenCalledTimes(2);
      fixture.destroy();
    },
  );
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
