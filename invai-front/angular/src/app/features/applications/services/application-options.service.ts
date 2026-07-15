import { Injectable, inject } from '@angular/core';
import { AdministrativeUnit } from '@features/administrative-units/administrative-units.model';
import { AdministrativeUnitsService } from '@features/administrative-units/services/administrative-units.service';
import { Category } from '@features/categories/categories.model';
import { CategoriesService } from '@features/categories/services/categories.service';
import { Commission } from '@features/commissions/commissions.model';
import { CommissionsService } from '@features/commissions/services/commissions.service';
import { Field } from '@features/fields/fields.model';
import { FieldsService } from '@features/fields/services/fields.service';
import { SystemType } from '@features/system-types/system-types.model';
import { SystemTypesService } from '@features/system-types/services/system-types.service';
import { SpringPage } from '@models/page.model';
import { Observable, catchError, combineLatest, map, of } from 'rxjs';
import { SelectOption } from '../applications.model';

export interface ApplicationSelectOptions {
  categories: SelectOption<number>[];
  informationSystems: SelectOption<number>[];
  scopes: SelectOption<number>[];
  commissions: SelectOption<number>[];
  administrativeUnits: SelectOption<number>[];
}

@Injectable({ providedIn: 'root' })
export class ApplicationOptionsService {
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
        commissions: commissions.map((commission) => this.nameOption(commission)),
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

  private nameOption(item: Category | Commission | Field | SystemType): SelectOption<number> {
    const label = item.name ?? String(item.id);
    return { label, value: item.id };
  }

  private administrativeUnitOption(item: AdministrativeUnit): SelectOption<number> {
    const label = item.name ?? item.code ?? String(item.id);
    return { label, value: item.id };
  }
}
