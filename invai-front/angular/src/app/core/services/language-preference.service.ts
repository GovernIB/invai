import { DOCUMENT, Injectable, LOCALE_ID, inject } from '@angular/core';
import { Language } from '@core/enums/language.enum';

export const LANGUAGE_STORAGE_KEY = 'invai.language';

@Injectable({ providedIn: 'root' })
export class LanguagePreferenceService {
  private readonly document = inject(DOCUMENT);
  private readonly locale = inject(LOCALE_ID);

  select(language: Language): void {
    try {
      localStorage.setItem(LANGUAGE_STORAGE_KEY, language);
    } catch {
      // Language switching remains available when browser storage is blocked.
    }

    if (language !== this.locale) {
      this.document.location.assign(this.languageUrl(language));
    }
  }

  restore(): boolean {
    let stored: string | null;
    try {
      stored = localStorage.getItem(LANGUAGE_STORAGE_KEY);
    } catch {
      return false;
    }

    if (stored !== Language.CA && stored !== Language.ES) {
      return false;
    }

    const url = this.languageUrl(stored);
    if (stored === this.locale || url === this.document.location.href) {
      return false;
    }

    this.document.location.replace(url);
    return true;
  }

  private languageUrl(language: Language): string {
    const url = new URL(this.document.location.href);
    const localePrefix = /^\/invaifront\/(ca|es)(?=\/|$)/;
    url.pathname = localePrefix.test(url.pathname)
      ? url.pathname.replace(localePrefix, `/invaifront/${language}`)
      : `/invaifront/${language}/`;
    return url.href;
  }
}
