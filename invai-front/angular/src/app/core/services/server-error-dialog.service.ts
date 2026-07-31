import { Injectable, signal } from '@angular/core';
import { ApiErrorResponse } from '@core/models/api-error.model';

@Injectable({ providedIn: 'root' })
export class ServerErrorDialogService {
  readonly visible = signal(false);
  readonly error = signal<ApiErrorResponse | null>(null);

  open(error: ApiErrorResponse): void {
    this.error.set({ ...error });
    this.visible.set(true);
  }

  close(): void {
    this.visible.set(false);
  }
}
