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
import { Observable, catchError, combineLatest, map, of } from 'rxjs';
import { SelectOption } from '../applications.model';

export interface ApplicationSelectOptions {
  categories: SelectOption<number>[];
  informationSystems: SelectOption<number>[];
  scopes: SelectOption<number>[];
  commissions: ApplicationCommissionOption[];
  administrativeUnits: SelectOption<number>[];
}

export interface ApplicationCommissionOption extends SelectOption<number> {
  expedientNumber: string;
  approvalDate: string;
  commissionType: CommissionType | null;
}

@Injectable({ providedIn: 'root' })
export class ApplicationOptionsService {
  private readonly locale = inject(LOCALE_ID);
  private readonly categoriesService = inject(CategoriesService);
  private readonly systemTypesService = inject(SystemTypesService);
  private readonly fieldsService = inject(FieldsService);
  private readonly commissionsService = inject(CommissionsService);
  private readonly administrativeUnitsService = inject(AdministrativeUnitsService);

  getOptions(): Observable<ApplicationSelectOptions> {
    return combineLatest({
      categories: this.pageContent(this.categoriesService.getAll()),
      informationSystems: this.pageContent(this.systemTypesService.getAll()),
      scopes: this.pageContent(this.fieldsService.getAll()),
      commissions: this.pageContent(this.commissionsService.getAll()),
      administrativeUnits: this.pageContent(this.administrativeUnitsService.getAll()),
    }).pipe(
      map(({ categories, informationSystems, scopes, commissions, administrativeUnits }) => ({
        categories: categories.map((category) => this.nameOption(category)),
        informationSystems: informationSystems.map((systemType) => this.nameOption(systemType)),
        scopes: scopes.map((field) => this.nameOption(field)),
        commissions: commissions.map((commission) => this.commissionOption(commission)),
        administrativeUnits: administrativeUnits.map((unit) => this.administrativeUnitOption(unit)),
      })),
    );
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

  private administrativeUnitOption(item: AdministrativeUnit): SelectOption<number> {
    return {
      label: localizedName(item, this.locale, item.code),
      value: item.id,
    };
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
}
