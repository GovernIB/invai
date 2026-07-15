import { PageParams } from '@models/page.model';

export interface AdministrativeUnit {
  id: number;
  code: string | null;
  name: string | null;
}

export interface AdministrativeUnitInput {
  code: string;
  name: string;
}

export type AdministrativeUnitPageParams = PageParams;
