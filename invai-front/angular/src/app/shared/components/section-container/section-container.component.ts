import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { HelpButton } from '@core/components/help-modal/help-button';

@Component({
  selector: 'app-section-container',
  standalone: true,
  imports: [HelpButton],
  templateUrl: './section-container.component.html',
  styleUrl: './section-container.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SectionContainerComponent {
  header = input<string>();
  bgStyle = input<string>('bg-white');
}
