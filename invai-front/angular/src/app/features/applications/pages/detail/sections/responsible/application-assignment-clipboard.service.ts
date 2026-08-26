import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ApplicationAssignmentClipboardService {
  copy(value: string): Promise<void> {
    if (!navigator.clipboard?.writeText) {
      return Promise.reject(new Error('Clipboard API unavailable'));
    }

    return navigator.clipboard.writeText(value);
  }
}
