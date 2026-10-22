import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  LOCALE_ID,
  ViewEncapsulation,
} from '@angular/core';
import { Tag } from 'primeng/tag';

import {
  APPLICATION_DOCUMENT_STATUS_CODES,
  ApplicationDocumentStatus,
} from '../../../../application-documentation.model';
import { documentationLabel } from './application-documentation.utils';

const STATE_MODIFIERS: Record<string, string> = {
  [APPLICATION_DOCUMENT_STATUS_CODES.active]: 'invai-status-tag--active',
  [APPLICATION_DOCUMENT_STATUS_CODES.inReview]: 'application-attachment-state-tag--review',
  [APPLICATION_DOCUMENT_STATUS_CODES.obsolete]: 'application-attachment-state-tag--obsolete',
};

// Lifecycle of an attached document, on the shared status tag: green while active, blue under
// review and orange when obsolete. The dot only repeats the colour; the label carries the state.
@Component({
  selector: 'app-application-attachment-state-tag',
  standalone: true,
  imports: [Tag],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  template: `
    <p-tag [styleClass]="styleClass()" [value]="label()">
      <span class="application-attachment-state-tag__dot" aria-hidden="true"></span>
    </p-tag>
  `,
  styles: `
    .p-tag.application-attachment-state-tag {
      gap: 0.375rem;
    }

    .p-tag.invai-status-tag.application-attachment-state-tag--review {
      color: var(--p-blue-800);
      background: var(--p-blue-100);
    }

    .p-tag.invai-status-tag.application-attachment-state-tag--obsolete {
      color: var(--p-orange-800);
      background: var(--p-orange-100);
    }

    .application-attachment-state-tag__dot {
      width: 0.5rem;
      height: 0.5rem;
      flex: none;
      background: currentcolor;
      border-radius: 50%;
    }
  `,
})
export class ApplicationAttachmentStateTag {
  status = input.required<ApplicationDocumentStatus>();

  private readonly locale = inject(LOCALE_ID);
  protected readonly label = computed(() => documentationLabel(this.status(), this.locale));
  protected readonly styleClass = computed(() => {
    const modifier = STATE_MODIFIERS[this.status().code] ?? '';
    return `invai-status-tag application-attachment-state-tag ${modifier}`.trim();
  });
}
