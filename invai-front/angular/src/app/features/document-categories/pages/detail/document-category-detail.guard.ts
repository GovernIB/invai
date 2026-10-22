import { CanDeactivateFn } from '@angular/router';

import { DocumentCategoryDetail } from './document-category-detail';

export const documentCategoryDetailCanDeactivate: CanDeactivateFn<DocumentCategoryDetail> = (
  component,
) => component.canDeactivate();
