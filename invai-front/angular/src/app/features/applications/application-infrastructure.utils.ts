import { localizedName } from '@shared/utils/localized-name.utils';
import {
  DatabaseRecord,
  InfrastructureSystem,
} from '@features/systems/systems.model';

import {
  ApplicationDatabaseCatalogRow,
  ApplicationSystemCatalogRow,
} from './applications.model';

export function toApplicationSystemCatalogRow(
  system: InfrastructureSystem,
  locale: string,
): ApplicationSystemCatalogRow {
  return {
    id: system.id,
    server: system.server?.name ?? '',
    environment: system.server?.environment
      ? localizedName(
          system.server.environment,
          locale,
          system.server.environment.code || `#${system.server.environment.id}`,
        )
      : '',
    instance: system.instance ?? '',
    port: system.port,
    version: system.version ?? '',
    description: system.description ?? '',
    source: system,
  };
}

export function toApplicationDatabaseCatalogRow(
  database: DatabaseRecord,
  locale: string,
): ApplicationDatabaseCatalogRow {
  return {
    id: database.id,
    server: database.server?.name ?? '',
    environment: database.server?.environment
      ? localizedName(
          database.server.environment,
          locale,
          database.server.environment.code || `#${database.server.environment.id}`,
        )
      : '',
    service: database.service ?? '',
    port: database.port,
    databaseType: database.databaseType?.name ?? '',
    description: database.description ?? '',
    source: database,
  };
}
