import { PageParams } from '@models/page.model';

export interface Commission {
  id: number;
  name: string | null;
}

export interface CommissionInput {
  name: string;
}

export type CommissionPageParams = PageParams;
