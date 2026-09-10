import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { AdministrativeUnit } from '@features/administrative-units/administrative-units.model';
import { AdministrativeUnitsService } from '@features/administrative-units/services/administrative-units.service';
import { Category } from '@features/categories/categories.model';
import { CategoriesService } from '@features/categories/services/categories.service';
import { Commission, CommissionType } from '@features/commissions/commissions.model';
import { CommissionsService } from '@features/commissions/services/commissions.service';
import { Field } from '@features/fields/fields.model';
import { FieldsService } from '@features/fields/services/fields.service';
import { SystemType } from '@features/system-types/system-types.model';
import { SystemTypesService } from '@features/system-types/services/system-types.service';
import { SpringPage } from '@models/page.model';
import { localizedName } from '@shared/utils/localized-name.utils';
import { Observable, catchError, combineLatest, forkJoin, map, of, switchMap } from 'rxjs';
import { SelectOption } from '../applications.model';

export interface ApplicationSelectOptions {
  categories: SelectOption<number>[];
  informationSystems: SelectOption<number>[];
  scopes: SelectOption<number>[];
  commissions: ApplicationCommissionOption[];
  departments: SelectOption<string>[];
  administrativeUnits: SelectOption<string>[];
}

export interface ApplicationCommissionOption extends SelectOption<number> {
  expedientNumber: string;
  approvalDate: string;
  commissionType: CommissionType | null;
}

export type ApplicationStaticSelectOptions = Omit<
  ApplicationSelectOptions,
  'departments' | 'administrativeUnits'
>;

const DIR3_PAGE_SIZE = 100;

@Injectable({ providedIn: 'root' })
export class ApplicationOptionsService {
  private readonly locale = inject(LOCALE_ID);
  private readonly categoriesService = inject(CategoriesService);
  private readonly systemTypesService = inject(SystemTypesService);
  private readonly fieldsService = inject(FieldsService);
  private readonly commissionsService = inject(CommissionsService);
  private readonly administrativeUnitsService = inject(AdministrativeUnitsService);

  getStaticOptions(): Observable<ApplicationStaticSelectOptions> {
    return combineLatest({
      categories: this.pageContent(this.categoriesService.getAll()),
      informationSystems: this.pageContent(this.systemTypesService.getAll()),
      scopes: this.pageContent(this.fieldsService.getAll()),
      commissions: this.pageContent(this.commissionsService.getAll()),
    }).pipe(
      map(({ categories, informationSystems, scopes, commissions }) => ({
        categories: categories.map((category) => this.nameOption(category)),
        informationSystems: informationSystems.map((systemType) => this.nameOption(systemType)),
        scopes: scopes.map((field) => this.nameOption(field)),
        commissions: commissions.map((commission) => this.commissionOption(commission)),
      })),
    );
  }

  getDepartmentOptions(): Observable<SelectOption<string>[]> {
    return this.loadAllPages((page) =>
      this.administrativeUnitsService.getDepartments({ page, size: DIR3_PAGE_SIZE }),
    ).pipe(map((items) => this.dir3Options(items)));
  }

  getAdministrativeUnitOptions(departmentCode: string): Observable<SelectOption<string>[]> {
    return this.loadAllPages((page) =>
      this.administrativeUnitsService.getAdmUnitsByDepartment(departmentCode, {
        page,
        size: DIR3_PAGE_SIZE,
      }),
    ).pipe(map((items) => this.dir3Options(items)));
  }

  private pageContent<TItem>(source$: Observable<SpringPage<TItem>>): Observable<TItem[]> {
    return source$.pipe(
      map((page) => page.content),
      catchError(() => of([])),
    );
  }

  private nameOption(item: Category | Field | SystemType): SelectOption<number> {
    return { label: localizedName(item, this.locale), value: item.id };
  }

  private dir3Options(items: AdministrativeUnit[]): SelectOption<string>[] {
    return items
      .map((item) => ({ label: item.name || item.code, value: item.code }))
      .sort((first, second) => first.label.localeCompare(second.label, this.locale));
  }

  private commissionOption(item: Commission): ApplicationCommissionOption {
    return {
      label: localizedName(item, this.locale),
      value: item.id,
      expedientNumber: item.expedientNumber ?? '',
      approvalDate: item.approvalDate ?? '',
      commissionType: item.commissionType,
    };
  }

  private loadAllPages<TItem>(
    getPage: (page: number) => Observable<SpringPage<TItem>>,
  ): Observable<TItem[]> {
    return getPage(0).pipe(
      switchMap((firstPage) => {
        if (firstPage.totalPages <= 1) return of(firstPage.content);

        const remaining = Array.from({ length: firstPage.totalPages - 1 }, (_, index) =>
          getPage(index + 1),
        );
        return forkJoin(remaining).pipe(
          map((pages) => [firstPage, ...pages].flatMap((page) => page.content)),
        );
      }),
    );
  }
}
