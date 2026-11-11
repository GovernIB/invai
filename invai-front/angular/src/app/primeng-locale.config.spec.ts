import { TestBed } from '@angular/core/testing';
import { PrimeNG } from 'primeng/config';

import { primengLocale } from './primeng-locale.config';

// Texts that hold only the number PrimeNG puts in, so they read the same in every language.
const LANGUAGE_NEUTRAL = new Set(['slideNumber']);

describe('primengLocale', () => {
  // PrimeNG's own texts, before any app configuration: the English defaults.
  const defaults = () => TestBed.inject(PrimeNG).translation;

  it('gives every accessible name PrimeNG defines, since PrimeNG replaces the whole aria object', () => {
    const english = defaults().aria as Record<string, string>;

    expect(Object.keys(primengLocale.aria).sort()).toEqual(Object.keys(english).sort());
    for (const [key, text] of Object.entries(primengLocale.aria)) {
      if (LANGUAGE_NEUTRAL.has(key)) continue;
      expect(text, key).not.toBe(english[key]);
      expect(text.trim(), key).not.toBe('');
    }
  });

  it('translates the hidden messages of selects, autocompletes and date pickers', () => {
    const english = defaults() as unknown as Record<string, string>;
    const translated = primengLocale as unknown as Record<string, string>;

    for (const key of [
      'emptyMessage',
      'emptySearchMessage',
      'emptyFilterMessage',
      'searchMessage',
      'selectionMessage',
      'emptySelectionMessage',
      'chooseYear',
      'chooseMonth',
      'chooseDate',
      'prevMonth',
      'nextMonth',
      'weekHeader',
    ]) {
      expect(translated[key], key).toBeTruthy();
      expect(translated[key], key).not.toBe(english[key]);
    }
  });

  it('keeps the placeholders PrimeNG replaces', () => {
    expect(primengLocale.searchMessage).toContain('{0}');
    expect(primengLocale.selectionMessage).toContain('{0}');
    expect(primengLocale.aria.stars).toContain('{star}');
    expect(primengLocale.aria.pageLabel).toContain('{page}');
  });
});
