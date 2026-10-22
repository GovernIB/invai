import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs';

import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import { ApplicationAttachmentsList } from './application-attachments-list';
import { APPLICATION_DOCUMENTATION_TEXTS } from './application-documentation.i18n';
import {
  APPLICATION_DOCUMENTATION_RESOLVE_KEY,
  ApplicationDocumentationResolvedData,
} from './application-documentation-section.resolver';
import { ApplicationLinksList } from './application-links-list';

// Documentation tab of the application detail: the attachments and the links of the
// application. Each list saves its own changes, so the tab has no edit mode of its own.
@Component({
  selector: 'app-application-documentation-section',
  standalone: true,
  imports: [ApplicationAttachmentsList, ApplicationDetailSectionLayout, ApplicationLinksList],
  templateUrl: './application-documentation-section.html',
  styles: `
    .application-documentation {
      display: flex;
      flex-direction: column;
      gap: 2.5rem;
      min-width: 0;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDocumentationSection {
  private readonly route = inject(ActivatedRoute);
  private readonly resolved = toSignal(
    this.route.data.pipe(
      map(
        (data) => data[APPLICATION_DOCUMENTATION_RESOLVE_KEY] as ApplicationDocumentationResolvedData,
      ),
    ),
    {
      initialValue: this.route.snapshot.data[
        APPLICATION_DOCUMENTATION_RESOLVE_KEY
      ] as ApplicationDocumentationResolvedData,
    },
  );

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly title = APPLICATION_DOCUMENTATION_TEXTS.title;
  protected readonly applicationId = computed(() => this.resolved()?.applicationId ?? null);
  protected readonly attachments = computed(() => this.resolved()?.attachments ?? null);
  protected readonly links = computed(() => this.resolved()?.links ?? null);
}
