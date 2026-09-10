import { Component } from '@angular/core';
import { SectionContainerComponent } from '@components/section-container/section-container.component';

@Component({
  selector: 'app-documentation',
  imports: [SectionContainerComponent],
  templateUrl: './documentation.html',
  styleUrl: './documentation.scss',
})
export class Documentation {
  protected readonly header = $localize`Documentació`;
}
