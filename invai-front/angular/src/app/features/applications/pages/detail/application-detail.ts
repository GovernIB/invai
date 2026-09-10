import {
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  computed,
  effect,
  inject,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { SectionContainerComponent } from '@components/section-container/section-container.component';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { map } from 'rxjs';

import {
  APPLICATIONS_ROUTES_LABELS,
  APPLICATIONS_ROUTES_LOC,
} from '../../applications.routes.i18n';
import {
  APPLICATION_DETAIL_SECTIONS_ARIA_LABEL,
  APPLICATION_DETAIL_COMPLETENESS_REFRESH_ERROR,
  APPLICATION_DETAIL_COMPLETENESS_RETRY,
  APPLICATION_DETAIL_AUTHORIZED_INCOMPLETE,
  APPLICATION_DETAIL_ACCESSIBILITY_INCOMPLETE,
  APPLICATION_DETAIL_DATABASES_INCOMPLETE,
  APPLICATION_DETAIL_DEVELOPMENT_INCOMPLETE,
  APPLICATION_DETAIL_RESPONSIBLE_AND_AUTHORIZED_INCOMPLETE,
  APPLICATION_DETAIL_RESPONSIBLE_TYPES_INCOMPLETE,
  APPLICATION_DETAIL_SECURITY_INCOMPLETE,
  APPLICATION_DETAIL_SYSTEMS_AND_DATABASES_INCOMPLETE,
  APPLICATION_DETAIL_SYSTEMS_INCOMPLETE,
  APPLICATION_DETAIL_TABS,
  APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_ARIA_LABEL,
  APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_LABEL,
  APPLICATION_DETAIL_UNSAVED_CHANGES_MESSAGE,
  APPLICATION_DETAIL_UNSAVED_CHANGES_TITLE,
} from './application-detail.i18n';
import { ApplicationDetailSection, ApplicationDetailState } from './application-detail-state';
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
    TooltipModule,
  ],
  providers: [ApplicationDetailState],
  templateUrl: './application-detail.html',
  styleUrl: './application-detail.scss',
})
export class ApplicationDetail implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly sectionTabs = viewChild<ElementRef<HTMLElement>>('sectionTabs');
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly paramMap = toSignal(this.route.paramMap, {
    initialValue: this.route.snapshot.paramMap,
  });
  private readonly resolvedData = toSignal(
    this.route.data.pipe(
      map((data) => data[APPLICATION_DETAIL_RESOLVE_KEY] as ApplicationDetailResolvedData),
    ),
    {
      initialValue: this.route.snapshot.data[
        APPLICATION_DETAIL_RESOLVE_KEY
      ] as ApplicationDetailResolvedData,
    },
  );

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionsAriaLabel = APPLICATION_DETAIL_SECTIONS_ARIA_LABEL;
  protected readonly completenessRefreshError = APPLICATION_DETAIL_COMPLETENESS_REFRESH_ERROR;
  protected readonly completenessRetry = APPLICATION_DETAIL_COMPLETENESS_RETRY;
  protected readonly unsavedChangesTitle = APPLICATION_DETAIL_UNSAVED_CHANGES_TITLE;
  protected readonly unsavedChangesCloseLabel = APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_LABEL;
  protected readonly unsavedChangesCloseAriaLabel =
    APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_ARIA_LABEL;
  protected readonly tabs = computed(() => [
    {
      label: APPLICATION_DETAIL_TABS.general,
      route: 'general' as const,
      incompleteMessage: '',
      warning: false,
    },
    {
      label: APPLICATION_DETAIL_TABS.responsible,
      route: 'responsible' as const,
      incompleteMessage: this.responsibleIncompleteMessage(),
      warning: false,
    },
    {
      label: APPLICATION_DETAIL_TABS.systemsDatabases,
      route: 'systems-databases' as const,
      incompleteMessage: this.systemsDatabasesIncompleteMessage(),
      warning: false,
    },
    {
      label: APPLICATION_DETAIL_TABS.development,
      route: 'development' as const,
      incompleteMessage: this.detailState.application()?.missingDevelopmentFields
        ? APPLICATION_DETAIL_DEVELOPMENT_INCOMPLETE
        : '',
      warning: false,
    },
    {
      label: APPLICATION_DETAIL_TABS.accessibility,
      route: 'accessibility' as const,
      incompleteMessage: this.detailState.application()?.missingAccessibilityFields
        ? APPLICATION_DETAIL_ACCESSIBILITY_INCOMPLETE
        : '',
      warning: false,
    },
    {
      label: APPLICATION_DETAIL_TABS.security,
      route: 'security' as const,
      incompleteMessage: this.detailState.application()?.missingSecurityData
        ? APPLICATION_DETAIL_SECURITY_INCOMPLETE
        : '',
      warning: false,
    },
  ]);
  private readonly sectionLabels: Record<ApplicationDetailSection, string> = {
    general: APPLICATION_DETAIL_TABS.general,
    responsible: APPLICATION_DETAIL_TABS.responsible,
    'systems-databases': APPLICATION_DETAIL_TABS.systemsDatabases,
    development: APPLICATION_DETAIL_TABS.development,
    accessibility: APPLICATION_DETAIL_TABS.accessibility,
    security: APPLICATION_DETAIL_TABS.security,
  };
  protected readonly applicationId = computed(() => this.paramMap().get('id'));
  protected readonly applicationName = computed(() => {
    const id = this.applicationId();
    if (!id) return $localize`Detall de l'aplicació`;

    return this.detailState.application()?.name ?? $localize`Aplicació ${id}`;
  });
  protected readonly applicationBreadcrumbLabel = computed(() => {
    const application = this.detailState.application();
    return application?.code ? $localize`Codi: ${application.code}` : this.applicationName();
  });
  protected readonly pageHeader = computed(() => this.applicationName());
  protected readonly unsavedChangesMessage = () => {
    const labels = this.detailState
      .dirtySections()
      .map((section) => `• ${this.sectionLabels[section]}`)
      .join('\n');
    return APPLICATION_DETAIL_UNSAVED_CHANGES_MESSAGE(labels);
  };

  protected retryCompleteness(): void {
    const tabs = this.sectionTabs()?.nativeElement;
    const activeTab = tabs?.querySelector<HTMLAnchorElement>('[aria-current="page"]')
      ?? tabs?.querySelector<HTMLAnchorElement>('a');
    // Keep focus on stable navigation when a successful retry removes the notice.
    activeTab?.focus();
    this.detailState.refreshCompletenessAfterMutation();
  }

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

  private responsibleIncompleteMessage(): string {
    const application = this.detailState.application();
    if (application?.missingResponsibleTypes && application.missingAuthorized) {
      return APPLICATION_DETAIL_RESPONSIBLE_AND_AUTHORIZED_INCOMPLETE;
    }
    if (application?.missingResponsibleTypes) {
      return APPLICATION_DETAIL_RESPONSIBLE_TYPES_INCOMPLETE;
    }
    if (application?.missingAuthorized) return APPLICATION_DETAIL_AUTHORIZED_INCOMPLETE;
    return '';
  }

  private systemsDatabasesIncompleteMessage(): string {
    const application = this.detailState.application();
    if (application?.missingSystems && application.missingDatabases) {
      return APPLICATION_DETAIL_SYSTEMS_AND_DATABASES_INCOMPLETE;
    }
    if (application?.missingSystems) return APPLICATION_DETAIL_SYSTEMS_INCOMPLETE;
    if (application?.missingDatabases) return APPLICATION_DETAIL_DATABASES_INCOMPLETE;
    return '';
  }
}
