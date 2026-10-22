import { CanDeactivateFn } from '@angular/router';
import { ApplicationDocumentDetail } from './application-document-detail';

export const applicationDocumentDetailCanDeactivate: CanDeactivateFn<ApplicationDocumentDetail> =
  (component) => component.canDeactivate();
