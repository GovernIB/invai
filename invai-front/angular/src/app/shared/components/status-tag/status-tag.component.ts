import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Tag } from 'primeng/tag';

@Component({
  selector: 'app-status-tag',
  imports: [Tag],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (label()) {
      <p-tag
        [value]="label()"
        [severity]="active() === true ? 'success' : 'secondary'"
        [styleClass]="active() === true ? 'invai-status-tag invai-status-tag--active' : 'invai-status-tag'"
      />
    }
  `,
})
export class StatusTagComponent {
  label = input.required<string>();
  active = input.required<boolean | null>();
}
