import { CanDeactivateFn } from '@angular/router';

import { ApplicationDetail } from './application-detail';

export const applicationDetailCanDeactivate: CanDeactivateFn<ApplicationDetail> = (
  component,
) => component.canDeactivate();
