import { computed, Component, DestroyRef, effect, inject, OnDestroy, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  ActivatedRoute,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { SectionContainerComponent } from '@components/section-container/section-container.component';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import {
  APPLICATIONS_ROUTES_LABELS,
  APPLICATIONS_ROUTES_LOC,
} from '../../applications.routes.i18n';
import {
  APPLICATION_DETAIL_CANCEL_ARIA_LABEL,
  APPLICATION_DETAIL_CANCEL_LABEL,
  APPLICATION_DETAIL_EDIT_ARIA_LABEL,
  APPLICATION_DETAIL_EDIT_LABEL,
  APPLICATION_DETAIL_INFO_TITLE,
  APPLICATION_DETAIL_SAVE_ARIA_LABEL,
  APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_TITLE,
  APPLICATION_DETAIL_SAVE_LABEL,
  APPLICATION_DETAIL_SAVE_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_SAVE_SUCCESS_TITLE,
  APPLICATION_DETAIL_SECTIONS_ARIA_LABEL,
  APPLICATION_DETAIL_SYSTEMS_DATABASES_CANCEL_ARIA_LABEL,
  APPLICATION_DETAIL_SYSTEMS_DATABASES_CANCEL_PENDING_MESSAGE,
  APPLICATION_DETAIL_SYSTEMS_DATABASES_SAVE_ARIA_LABEL,
  APPLICATION_DETAIL_SYSTEMS_DATABASES_SAVE_PENDING_MESSAGE,
  APPLICATION_DETAIL_TABS,
} from './application-detail.i18n';
import { ApplicationDetailState } from './application-detail-state';

@Component({
  standalone: true,
  selector: 'app-application-detail',
  imports: [Button, RouterLink, RouterLinkActive, RouterOutlet, SectionContainerComponent],
  providers: [ApplicationDetailState],
  templateUrl: './application-detail.html',
  styleUrl: './application-detail.scss',
})
export class ApplicationDetail implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly paramMap = toSignal(this.route.paramMap, {
    initialValue: this.route.snapshot.paramMap,
  });
  private readonly routerEvent = toSignal(this.router.events, { initialValue: null });

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly editLabel = APPLICATION_DETAIL_EDIT_LABEL;
  protected readonly editAriaLabel = APPLICATION_DETAIL_EDIT_ARIA_LABEL;
  protected readonly cancelLabel = APPLICATION_DETAIL_CANCEL_LABEL;
  protected readonly cancelAriaLabel = APPLICATION_DETAIL_CANCEL_ARIA_LABEL;
  protected readonly saveLabel = APPLICATION_DETAIL_SAVE_LABEL;
  protected readonly saveAriaLabel = APPLICATION_DETAIL_SAVE_ARIA_LABEL;
  protected readonly systemsDatabasesCancelAriaLabel =
    APPLICATION_DETAIL_SYSTEMS_DATABASES_CANCEL_ARIA_LABEL;
  protected readonly systemsDatabasesSaveAriaLabel =
    APPLICATION_DETAIL_SYSTEMS_DATABASES_SAVE_ARIA_LABEL;
  protected readonly sectionsAriaLabel = APPLICATION_DETAIL_SECTIONS_ARIA_LABEL;
  protected readonly editIcon = PrimeIcons.PENCIL;
  protected readonly cancelIcon = PrimeIcons.TIMES;
  protected readonly saveIcon = PrimeIcons.CHECK;
  protected readonly isSaving = signal(false);
  protected readonly tabs = [
    { label: APPLICATION_DETAIL_TABS.general, route: 'general' },
    { label: APPLICATION_DETAIL_TABS.responsible, route: 'responsible' },
    { label: APPLICATION_DETAIL_TABS.systemsDatabases, route: 'systems-databases' },
  ];
  protected readonly applicationId = computed(() => this.paramMap().get('id'));
  protected readonly applicationName = computed(() => {
    const id = this.applicationId();

    if (!id) {
      return $localize`Detall de l'aplicació`;
    }

    return this.detailState.application()?.name ?? $localize`Aplicació ${id}`;
  });
  protected readonly applicationBreadcrumbLabel = computed(() => {
    const application = this.detailState.application();

    return application?.code ? $localize`Codi: ${application.code}` : this.applicationName();
  });
  protected readonly header = computed(() => this.applicationName());
  protected readonly isGeneralActive = computed(() => {
    this.routerEvent();
    const childPath = this.route.firstChild?.snapshot.routeConfig?.path;
    return !childPath || childPath === 'general';
  });
  protected readonly isSystemsDatabasesActive = computed(() => {
    this.routerEvent();
    return this.route.firstChild?.snapshot.routeConfig?.path === 'systems-databases';
  });

  constructor() {
    effect(() => {
      this.detailState.load(this.applicationId());
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
          ...(id && { routerLink: ['/', APPLICATIONS_ROUTES_LOC.BASE, id, 'general'] }),
        },
      ]);
    });
  }

  ngOnDestroy(): void {
    this.breadcrumbService.clear();
  }

  protected startEditing(): void {
    this.detailState.startEditing();
  }

  protected cancelEditing(): void {
    this.detailState.cancelEditing();
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (saved) => {
          this.isSaving.set(false);
          if (!saved) return;

          this.messageService.add({
            severity: 'success',
            summary: APPLICATION_DETAIL_SAVE_SUCCESS_TITLE,
            detail: APPLICATION_DETAIL_SAVE_SUCCESS_MESSAGE,
          });
        },
        error: () => {
          this.isSaving.set(false);
          this.messageService.add({
            severity: 'error',
            summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
            detail: APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
          });
        },
      });
  }

  protected cancelSystemsDatabasesChanges(): void {
    this.showPendingMessage(APPLICATION_DETAIL_SYSTEMS_DATABASES_CANCEL_PENDING_MESSAGE);
  }

  protected saveSystemsDatabasesChanges(): void {
    this.showPendingMessage(APPLICATION_DETAIL_SYSTEMS_DATABASES_SAVE_PENDING_MESSAGE);
  }

  protected onTabNavigation(route: string): void {
    if (route !== 'general' && this.detailState.isEditing()) {
      this.detailState.cancelEditing();
    }
  }

  private showPendingMessage(detail: string): void {
    this.messageService.add({
      severity: 'info',
      summary: APPLICATION_DETAIL_INFO_TITLE,
      detail,
    });
  }
}
