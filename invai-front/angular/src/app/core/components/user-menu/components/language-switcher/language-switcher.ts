import { ChangeDetectionStrategy, Component, inject, LOCALE_ID } from '@angular/core';
import { Language } from '@core/enums/language.enum';
import { Button } from 'primeng/button';

const LANGUAGE_SWITCHER_LABELS = {
  TITLE: $localize`Llenguatge`,
};

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Button],
  selector: 'app-language-switcher',
  template: `
    <h3 class="text-primary font-semibold mb-3">{{ labels.TITLE }}</h3>

    <section>
      @for (language of availableLanguages; track language) {
        <p-button
          [outlined]="true"
          [rounded]="true"
          [text]="true"
          severity="secondary"
          size="small"
          [attr.aria-pressed]="selectedLanguage === language"
          [styleClass]="selectedLanguage === language ? 'bg-surface-100' : ''"
          (click)="changeLanguage(language)"
          [label]="language.toUpperCase()"
        />
      }
    </section>
  `,
})
export class LanguageSwitcher {
  protected readonly selectedLanguage = inject(LOCALE_ID);
  protected readonly labels = LANGUAGE_SWITCHER_LABELS;

  protected readonly availableLanguages: Language[] = Object.values(Language);

  protected changeLanguage(lang: Language) {
    globalThis.location.href = `/invaifront/${lang}`;
    sessionStorage.clear();
  }
}
