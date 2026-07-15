import { Component } from '@angular/core';

import { ApplicationResponsibleForm } from './application-responsible-form';

@Component({
  standalone: true,
  selector: 'app-application-responsible-section',
  imports: [ApplicationResponsibleForm],
  templateUrl: './application-responsible-section.html',
})
export class ApplicationResponsibleSection {
  protected readonly header = $localize`Responsable`;
}
