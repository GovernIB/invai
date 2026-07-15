import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Editor } from 'primeng/editor';

import { ApplicationInfrastructureList } from '../../../../components';
import {
  APPLICATION_DATABASES_SEED_DATA,
  APPLICATION_DATABASES_TABLE_COLUMNS,
  APPLICATION_SERVERS_SEED_DATA,
  APPLICATION_SERVERS_TABLE_COLUMNS,
} from '../../../../applications.constants';
import {
  APPLICATION_SYSTEMS_DATABASES_DATABASE_NAME,
  APPLICATION_SYSTEMS_DATABASES_DATABASES_TITLE,
  APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_ARIA_LABEL,
  APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_LABEL,
  APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_PENDING_MESSAGE,
  APPLICATION_SYSTEMS_DATABASES_INFO_TITLE,
  APPLICATION_SYSTEMS_DATABASES_OBSERVATIONS_LABEL,
  APPLICATION_SYSTEMS_DATABASES_SERVER_NAME,
  APPLICATION_SYSTEMS_DATABASES_SERVERS_TITLE,
} from './application-systems-databases-section.i18n';
import { ApplicationDetailState } from '../../application-detail-state';

@Component({
  selector: 'app-application-systems-databases-section',
  standalone: true,
  imports: [ApplicationInfrastructureList, Button, Editor, ReactiveFormsModule],
  templateUrl: './application-systems-databases-section.html',
  styleUrl: './application-systems-databases-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSystemsDatabasesSection {
  private readonly messageService = inject(MessageService);

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly serversTitle = APPLICATION_SYSTEMS_DATABASES_SERVERS_TITLE;
  protected readonly serverName = APPLICATION_SYSTEMS_DATABASES_SERVER_NAME;
  protected readonly databasesTitle = APPLICATION_SYSTEMS_DATABASES_DATABASES_TITLE;
  protected readonly databaseName = APPLICATION_SYSTEMS_DATABASES_DATABASE_NAME;
  protected readonly documentationLabel = APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_LABEL;
  protected readonly documentationAriaLabel =
    APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_ARIA_LABEL;
  protected readonly observationsLabel = APPLICATION_SYSTEMS_DATABASES_OBSERVATIONS_LABEL;
  protected readonly documentationIcon = PrimeIcons.EXTERNAL_LINK;
  protected readonly observationsId = 'application-systems-databases-observations';
  protected readonly observationsLabelId = `${this.observationsId}-label`;
  protected readonly servers = APPLICATION_SERVERS_SEED_DATA;
  protected readonly serverColumns = APPLICATION_SERVERS_TABLE_COLUMNS;
  protected readonly databases = APPLICATION_DATABASES_SEED_DATA;
  protected readonly databaseColumns = APPLICATION_DATABASES_TABLE_COLUMNS;

  protected onDocumentation(): void {
    this.messageService.add({
      severity: 'info',
      summary: APPLICATION_SYSTEMS_DATABASES_INFO_TITLE,
      detail: APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_PENDING_MESSAGE,
    });
  }
}
