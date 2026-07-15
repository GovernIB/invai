import { PageParams } from '@models/page.model';

export interface SystemType {
  id: number;
  name: string | null;
}

export interface SystemTypeInput {
  name: string;
}

export type SystemTypePageParams = PageParams;
