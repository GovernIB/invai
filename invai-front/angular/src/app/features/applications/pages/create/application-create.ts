import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SectionContainerComponent } from '@components/section-container/section-container.component';

import {
  APPLICATION_OPTIONS_RESOLVE_KEY,
  ApplicationOptionsResolvedData,
} from '../../resolvers/application-options.resolver';
import { ApplicationCreateForm } from './application-create-form';

@Component({
  standalone: true,
  selector: 'app-application-create',
  imports: [ApplicationCreateForm, SectionContainerComponent],
  templateUrl: './application-create.html',
  styleUrl: './application-create.scss',
})
export class ApplicationCreate {
  private readonly route = inject(ActivatedRoute);

  protected readonly header = $localize`Afegir aplicació`;
  protected readonly options = (
    this.route.snapshot.data[APPLICATION_OPTIONS_RESOLVE_KEY] as ApplicationOptionsResolvedData
  ).options;
}
