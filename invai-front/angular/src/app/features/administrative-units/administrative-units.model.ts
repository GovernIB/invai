import { PageParams } from '@models/page.model';

export interface AdministrativeUnit {
  code: string;
  name: string;
  parentCode: string | null;
  level: number | null;
}

export type AdministrativeUnitPageParams = PageParams;
