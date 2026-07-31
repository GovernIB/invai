export const APPLICATION_SYSTEMS_DATABASES_SECTION_TITLE =
  $localize`:@@applicationSystemsDatabasesSectionTitle:Infraestructura de sistemes i bases de dades`;

export const APPLICATION_SYSTEMS_DATABASES_SERVERS_TITLE = $localize`Servidors`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_NAME = $localize`servidor`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASES_TITLE = $localize`Bases de dades`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_NAME = $localize`base de dades`;
export const APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_LABEL = $localize`Documentació`;
export const APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_ARIA_LABEL =
  $localize`Obrir la documentació de sistemes i bases de dades`;
export const APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_PENDING_MESSAGE =
  $localize`La documentació de sistemes i bases de dades encara no està implementada.`;
export const APPLICATION_SYSTEMS_DATABASES_INFO_TITLE = $localize`Informació`;
export const APPLICATION_SYSTEMS_DATABASES_ERROR_TITLE = $localize`Error`;
export const APPLICATION_SYSTEMS_DATABASES_SERVERS_LOAD_ERROR =
  $localize`No s'han pogut carregar els servidors de l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASES_LOAD_ERROR =
  $localize`No s'han pogut carregar les bases de dades de l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_OBSERVATIONS_LABEL = $localize`Observacions`;
export const APPLICATION_SYSTEMS_DATABASES_SERVERS_FILTERS_ARIA_LABEL =
  $localize`Mostra o oculta els filtres de servidors`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASES_FILTERS_ARIA_LABEL =
  $localize`Mostra o oculta els filtres de bases de dades`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_FILTER_LABELS = {
  environment: $localize`Entorn`,
  server: $localize`Servidor`,
  instance: $localize`Instància`,
  port: $localize`Port`,
  version: $localize`Versió`,
  status: $localize`Estat`,
  observations: $localize`Observacions`,
};
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_FILTER_LABELS = {
  environment: $localize`Entorn`,
  server: $localize`Servidor`,
  version: $localize`Versió`,
  database: $localize`Base de dades`,
  service: $localize`Servei`,
  port: $localize`Port`,
  type: $localize`Tipus`,
  status: $localize`Estat`,
  observations: $localize`Observacions`,
};
export const APPLICATION_SYSTEMS_DATABASES_WARNING_TITLE = $localize`Atenció`;
export const APPLICATION_SYSTEMS_DATABASES_UNSUPPORTED_FILTER = (label: string) =>
  $localize`El filtre «${label}» encara no està suportat pel servidor i no s'aplicarà.`;
export const APPLICATION_SYSTEMS_DATABASES_UNSUPPORTED_FILTERS_ON_SEARCH =
  $localize`Alguns filtres encara no estan suportats pel servidor i no s'aplicaran.`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_OPTIONS_LOAD_ERROR =
  $localize`No s'han pogut carregar les opcions de servidors.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_OPTIONS_LOAD_ERROR =
  $localize`No s'han pogut carregar les opcions de bases de dades.`;
export const APPLICATION_SYSTEMS_DATABASES_ENVIRONMENT_OPTIONS_LOAD_ERROR =
  $localize`No s'han pogut carregar les opcions d'entorns.`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_CATALOG_LOAD_ERROR =
  $localize`No s'han pogut carregar els servidors actius de manteniments.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_CATALOG_LOAD_ERROR =
  $localize`No s'han pogut carregar les bases de dades actives de manteniments.`;
export const APPLICATION_SYSTEMS_DATABASES_SYSTEM_DATABASE_LOAD_ERROR =
  $localize`No s'han pogut carregar les observacions de sistemes i bases de dades.`;
export const APPLICATION_SYSTEMS_DATABASES_SYSTEM_DATABASE_REQUIRED =
  $localize`L'aplicació no té informat l'identificador de sistemes i bases de dades.`;
export const APPLICATION_SYSTEMS_DATABASES_RELATION_ERROR = $localize`Error`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_CREATE_SUCCESS =
  $localize`El servidor s'ha afegit correctament a l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_UPDATE_SUCCESS =
  $localize`El servidor de l'aplicació s'ha actualitzat correctament.`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_SUCCESS =
  $localize`El servidor s'ha donat de baixa de l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_SAVE_ERROR =
  $localize`No s'ha pogut desar el servidor de l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_ERROR =
  $localize`No s'ha pogut donar de baixa el servidor de l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_CREATE_SUCCESS =
  $localize`La base de dades s'ha afegit correctament a l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_UPDATE_SUCCESS =
  $localize`La base de dades de l'aplicació s'ha actualitzat correctament.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_SUCCESS =
  $localize`La base de dades s'ha donat de baixa de l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_SAVE_ERROR =
  $localize`No s'ha pogut desar la base de dades de l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_ERROR =
  $localize`No s'ha pogut donar de baixa la base de dades de l'aplicació.`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_TITLE =
  $localize`Donar de baixa el servidor?`;
export const APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_MESSAGE = (id: number) =>
  $localize`La relació amb el servidor #${id}:id: quedarà inactiva.`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_TITLE =
  $localize`Donar de baixa la base de dades?`;
export const APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_MESSAGE = (id: number) =>
  $localize`La relació amb la base de dades #${id}:id: quedarà inactiva.`;
export const APPLICATION_SYSTEMS_DATABASES_RELATION_DELETE_CANCEL =
  $localize`Cancel·la`;
export const APPLICATION_SYSTEMS_DATABASES_RELATION_DELETE_CONFIRM =
  $localize`Dona de baixa`;
