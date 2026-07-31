import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-application-detail-section-layout',
  standalone: true,
  templateUrl: './application-detail-section-layout.html',
  styleUrl: './application-detail-section-layout.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDetailSectionLayout {
  title = input.required<string>();
  titleId = input.required<string>();
}
