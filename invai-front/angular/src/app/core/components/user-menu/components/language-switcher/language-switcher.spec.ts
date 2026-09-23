import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Language } from '@core/enums/language.enum';
import { LanguagePreferenceService } from '@core/services/language-preference.service';

import { LanguageSwitcher } from './language-switcher';

describe('LanguageSwitcher', () => {
  it('exposes the selected language on native buttons and saves a new selection', async () => {
    const select = vi.fn();
    await TestBed.configureTestingModule({
      imports: [LanguageSwitcher],
      providers: [
        { provide: LOCALE_ID, useValue: Language.CA },
        { provide: LanguagePreferenceService, useValue: { select } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(LanguageSwitcher);
    fixture.detectChanges();
    const buttons: HTMLButtonElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    );

    expect(buttons.map((button) => button.textContent?.trim())).toEqual(['CA', 'ES']);
    expect(buttons[0].getAttribute('aria-pressed')).toBe('true');
    expect(buttons[1].getAttribute('aria-pressed')).toBe('false');
    buttons[1].click();
    expect(select).toHaveBeenCalledWith(Language.ES);
  });
});
