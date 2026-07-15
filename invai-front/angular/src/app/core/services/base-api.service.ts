import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '@environments/environment';

export type UrlSeg = string | number;

@Injectable({ providedIn: 'root' })
export abstract class BaseApiService {
  protected readonly http = inject(HttpClient);
  protected readonly apiUri = environment.apiBasePath;

  protected abstract readonly ENTITY_URI: string;

  protected get BASE_URI(): string {
    return `${this.apiUri}/${this.ENTITY_URI}`;
  }

  /**
   * Helper que construye URLs de forma consistente:
   * - url() -> BASE_URI --> invaiapi/interna/services
   * - url(users) -> BASE_URI/users --> invaiapi/interna/services/users
   * - url('users', 10) --> invaiapi/interna/services/users/10
   * - url('/users', '10', 'expedients') -> invaiapi/interna/services/users/10/expedients
   */
  protected url(...segments: UrlSeg[]): string {
    const cleaned = segments
      .filter((s) => s !== undefined && s !== null)
      .map(String)
      .map((s) => s.replace(/^\/+|\/+$/g, ''));

    return cleaned.length ? `${this.BASE_URI}/${cleaned.join('/')}` : this.BASE_URI;
  }
}
