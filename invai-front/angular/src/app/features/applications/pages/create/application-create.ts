import { Component } from '@angular/core';
import { SectionContainerComponent } from '@components/section-container/section-container.component';

import { ApplicationCreateForm } from './application-create-form';

@Component({
  standalone: true,
  selector: 'app-application-create',
  imports: [ApplicationCreateForm, SectionContainerComponent],
  templateUrl: './application-create.html',
  styleUrl: './application-create.scss',
})
export class ApplicationCreate {
  protected readonly header = $localize`Afegir aplicació`;
}
