import { PageParams } from '@models/page.model';

export interface Category {
  id: number;
  name: string | null;
}

export interface CategoryInput {
  name: string;
}

export type CategoryPageParams = PageParams;
