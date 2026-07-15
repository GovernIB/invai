import { PageParams } from '@models/page.model';

export interface Field {
  id: number;
  name: string | null;
}

export interface FieldInput {
  name: string;
}

export type FieldPageParams = PageParams;
