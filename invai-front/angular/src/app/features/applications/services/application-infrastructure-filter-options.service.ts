import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { EnvironmentsService } from '@features/environments/services/environments.service';
import { DatabasesService } from '@features/systems/services/databases.service';
import { ServerCatalogService } from '@features/systems/services/server-catalog.service';
import { SystemsService } from '@features/systems/services/systems.service';
import {
  DatabaseRecord,
  InfrastructureSystem,
} from '@features/systems/systems.model';
import { SpringPage } from '@models/page.model';
import { localizedName } from '@shared/utils/localized-name.utils';
import { EMPTY, Observable, expand, forkJoin, map, reduce } from 'rxjs';

import { SelectOption } from '../applications.model';

const CATALOG_PAGE_SIZE = 100;

@Injectable({ providedIn: 'root' })
export class ApplicationInfrastructureFilterOptionsService {
  private readonly locale = inject(LOCALE_ID);
  private readonly systemsService = inject(SystemsService);
  private readonly serverCatalogService = inject(ServerCatalogService);
  private readonly databasesService = inject(DatabasesService);
  private readonly environmentsService = inject(EnvironmentsService);

  getServerOptions(): Observable<SelectOption<number>[]> {
    return this.getAllPages((page) =>
      this.systemsService.getAll({
        page,
        size: CATALOG_PAGE_SIZE,
        sort: 'server.name,asc',
      }),
    ).pipe(
      map((systems) =>
        this.sortOptions(this.toSystemOptions(systems)),
      ),
    );
  }

  getPhysicalServerOptions(): Observable<SelectOption<number>[]> {
    return forkJoin([
      this.serverCatalogService.getActiveOptions('APPLICATION'),
      this.serverCatalogService.getActiveOptions('DATABASE'),
    ]).pipe(
      map(([applicationServers, databaseServers]) => {
        const optionsById = new Map<number, SelectOption<number>>();
        [...applicationServers, ...databaseServers].forEach((server) => {
          optionsById.set(server.id, { label: server.label, value: server.id });
        });
        return this.sortOptions([...optionsById.values()]);
      }),
    );
  }

  getDatabaseOptions(): Observable<SelectOption<number>[]> {
    return this.getAllPages((page) =>
      this.databasesService.getAll({
        page,
        size: CATALOG_PAGE_SIZE,
        sort: 'service,asc',
      }),
    ).pipe(map((databases) => this.toDatabaseOptions(databases)));
  }

  getEnvironmentOptions(): Observable<SelectOption<number>[]> {
    return this.getAllPages((page) =>
      this.environmentsService.getAll({ page, size: CATALOG_PAGE_SIZE, sort: 'name,asc' }),
    ).pipe(
      map((environments) =>
        this.sortOptions(
          environments.map((environment) => ({
            label: localizedName(environment, this.locale, environment.code || `#${environment.id}`),
            value: environment.id,
          })),
        ),
      ),
    );
  }

  private getAllPages<TItem>(
    loadPage: (page: number) => Observable<SpringPage<TItem>>,
  ): Observable<TItem[]> {
    return loadPage(0).pipe(
      expand((page) => (page.last ? EMPTY : loadPage(page.number + 1))),
      reduce((items, page) => [...items, ...page.content], [] as TItem[]),
    );
  }

  private toSystemOptions(
    systems: InfrastructureSystem[],
  ): SelectOption<number>[] {
    const nameCounts = new Map<string, number>();
    systems.forEach((system) => {
      const name = system.server?.name?.trim() ?? '';
      const key = name.toLocaleLowerCase(this.locale);
      nameCounts.set(key, (nameCounts.get(key) ?? 0) + 1);
    });

    return systems.map((system) => {
      const name = system.server?.name?.trim() || `#${system.id}`;
      const key = name.toLocaleLowerCase(this.locale);
      const label =
        (nameCounts.get(key) ?? 0) > 1 && system.instance
          ? `${name} — ${system.instance}`
          : name;
      return { label, value: system.id };
    });
  }

  private toDatabaseOptions(databases: DatabaseRecord[]): SelectOption<number>[] {
    const nameCounts = new Map<string, number>();
    databases.forEach((database) => {
      const key = database.service.trim().toLocaleLowerCase(this.locale);
      nameCounts.set(key, (nameCounts.get(key) ?? 0) + 1);
    });

    return this.sortOptions(
      databases.map((database) => {
        const name = database.service || `#${database.id}`;
        const key = database.service.trim().toLocaleLowerCase(this.locale);
        const serverName = database.server?.name?.trim();
        const label = (nameCounts.get(key) ?? 0) > 1 && serverName
          ? `${name} — ${serverName}`
          : name;

        return { label, value: database.id };
      }),
    );
  }

  private sortOptions(options: SelectOption<number>[]): SelectOption<number>[] {
    return options.sort((left, right) =>
      left.label.localeCompare(right.label, this.locale, { sensitivity: 'base' }),
    );
  }
}
