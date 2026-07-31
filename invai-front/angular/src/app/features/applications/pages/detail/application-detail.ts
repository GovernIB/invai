import {
  Component,
  HostListener,
  OnDestroy,
  computed,
  effect,
  inject,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  ActivatedRoute,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { SectionContainerComponent } from '@components/section-container/section-container.component';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { map } from 'rxjs';

import {
  APPLICATIONS_ROUTES_LABELS,
  APPLICATIONS_ROUTES_LOC,
} from '../../applications.routes.i18n';
import {
  APPLICATION_DETAIL_SECTIONS_ARIA_LABEL,
  APPLICATION_DETAIL_TABS,
  APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_ARIA_LABEL,
  APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_LABEL,
  APPLICATION_DETAIL_UNSAVED_CHANGES_MESSAGE,
  APPLICATION_DETAIL_UNSAVED_CHANGES_TITLE,
} from './application-detail.i18n';
import {
  ApplicationDetailSection,
  ApplicationDetailState,
} from './application-detail-state';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
} from './application-detail.resolver';

@Component({
  standalone: true,
  selector: 'app-application-detail',
  imports: [
    Button,
    Dialog,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    SectionContainerComponent,
  ],
  providers: [ApplicationDetailState],
  templateUrl: './application-detail.html',
  styleUrl: './application-detail.scss',
})
export class ApplicationDetail implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly paramMap = toSignal(this.route.paramMap, {
    initialValue: this.route.snapshot.paramMap,
  });
  private readonly resolvedData = toSignal(
    this.route.data.pipe(
      map(
        (data) =>
          data[APPLICATION_DETAIL_RESOLVE_KEY] as ApplicationDetailResolvedData,
      ),
    ),
    {
      initialValue: this.route.snapshot.data[
        APPLICATION_DETAIL_RESOLVE_KEY
      ] as ApplicationDetailResolvedData,
    },
  );

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionsAriaLabel = APPLICATION_DETAIL_SECTIONS_ARIA_LABEL;
  protected readonly unsavedChangesTitle = APPLICATION_DETAIL_UNSAVED_CHANGES_TITLE;
  protected readonly unsavedChangesCloseLabel =
    APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_LABEL;
  protected readonly unsavedChangesCloseAriaLabel =
    APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_ARIA_LABEL;
  protected readonly tabs: {
    label: string;
    route: ApplicationDetailSection;
  }[] = [
    { label: APPLICATION_DETAIL_TABS.general, route: 'general' },
    { label: APPLICATION_DETAIL_TABS.responsible, route: 'responsible' },
    {
      label: APPLICATION_DETAIL_TABS.systemsDatabases,
      route: 'systems-databases',
    },
    { label: APPLICATION_DETAIL_TABS.development, route: 'development' },
  ];
  private readonly sectionLabels: Record<ApplicationDetailSection, string> = {
    general: APPLICATION_DETAIL_TABS.general,
    responsible: APPLICATION_DETAIL_TABS.responsible,
    'systems-databases': APPLICATION_DETAIL_TABS.systemsDatabases,
    development: APPLICATION_DETAIL_TABS.development,
  };
  protected readonly applicationId = computed(() => this.paramMap().get('id'));
  protected readonly applicationName = computed(() => {
    const id = this.applicationId();
    if (!id) return $localize`Detall de l'aplicació`;

    return this.detailState.application()?.name ?? $localize`Aplicació ${id}`;
  });
  protected readonly applicationBreadcrumbLabel = computed(() => {
    const application = this.detailState.application();
    return application?.code
      ? $localize`Codi: ${application.code}`
      : this.applicationName();
  });
  protected readonly pageHeader = computed(() => this.applicationName());
  protected readonly unsavedChangesMessage = () => {
    const labels = this.detailState
      .dirtySections()
      .map((section) => `• ${this.sectionLabels[section]}`)
      .join('\n');
    return APPLICATION_DETAIL_UNSAVED_CHANGES_MESSAGE(labels);
  };

  constructor() {
    effect(() => {
      this.detailState.initialize(this.resolvedData().application);
    });

    effect(() => {
      const id = this.applicationId();
      const label = this.applicationBreadcrumbLabel();

      this.breadcrumbService.setCustomBreadcrumbs([
        {
          label: APPLICATIONS_ROUTES_LABELS.BASE,
          routerLink: ['/', APPLICATIONS_ROUTES_LOC.BASE],
        },
        {
          label,
          ...(id && {
            routerLink: ['/', APPLICATIONS_ROUTES_LOC.BASE, id, 'general'],
          }),
        },
      ]);
    });
  }

  ngOnDestroy(): void {
    this.breadcrumbService.clear();
  }

  canDeactivate(): boolean {
    if (!this.detailState.hasDirtySections()) return true;

    this.detailState.showUnsavedChangesDialog();
    return false;
  }

  @HostListener('window:beforeunload', ['$event'])
  protected onBeforeUnload(event: BeforeUnloadEvent): void {
    if (!this.detailState.hasDirtySections()) return;

    event.preventDefault();
    event.returnValue = '';
  }

  protected closeUnsavedChangesDialog(): void {
    this.detailState.hideUnsavedChangesDialog();
  }
}
