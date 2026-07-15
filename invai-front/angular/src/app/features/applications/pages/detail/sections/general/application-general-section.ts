import { Component, inject } from '@angular/core';

import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationGeneralForm } from './application-general-form';

@Component({
  standalone: true,
  selector: 'app-application-general-section',
  imports: [ApplicationGeneralForm],
  templateUrl: './application-general-section.html',
})
export class ApplicationGeneralSection {
  protected readonly detailState = inject(ApplicationDetailState);
}
