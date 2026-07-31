import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { SpringPage } from '@models/page.model';
import { localizedName } from '@shared/utils/localized-name.utils';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';

import {
  InfrastructureServer,
  ServerCatalogOption,
  ServerTypeCode,
} from '../systems.model';
import { ServersService } from './servers.service';

const CATALOG_PAGE_SIZE = 100;

@Injectable({ providedIn: 'root' })
export class ServerCatalogService {
  private readonly locale = inject(LOCALE_ID);
  private readonly serversService = inject(ServersService);

  getActiveOptions(serverTypeCode: ServerTypeCode): Observable<ServerCatalogOption[]> {
    return this.getAllPages(0, serverTypeCode).pipe(
      map((servers) =>
        servers
          .map((server) => ({
            id: server.id,
            name: server.name,
            environment: server.environment,
            label: `${server.name} · ${localizedName(
              server.environment,
              this.locale,
              server.environment.code || `#${server.environment.id}`,
            )}`,
          }))
          .sort((left, right) =>
            left.label.localeCompare(right.label, this.locale, {
              sensitivity: 'base',
            }),
          ),
      ),
    );
  }

  private getAllPages(
    page: number,
    serverTypeCode: ServerTypeCode,
  ): Observable<InfrastructureServer[]> {
    return this.loadPage(page, serverTypeCode).pipe(
      expand((result) =>
        result.last
          ? EMPTY
          : this.loadPage(result.number + 1, serverTypeCode),
      ),
      reduce(
        (items, result) => [...items, ...result.content],
        [] as InfrastructureServer[],
      ),
    );
  }

  private loadPage(
    page: number,
    serverTypeCode: ServerTypeCode,
  ): Observable<SpringPage<InfrastructureServer>> {
    return this.serversService.getAll({
      page,
      size: CATALOG_PAGE_SIZE,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
      serverTypeCode,
    });
  }
}
