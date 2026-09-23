import { DOCUMENT, LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Language } from '@core/enums/language.enum';

import { LANGUAGE_STORAGE_KEY, LanguagePreferenceService } from './language-preference.service';

describe('LanguagePreferenceService', () => {
  let service: LanguagePreferenceService;
  let location: { href: string; assign: ReturnType<typeof vi.fn>; replace: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    localStorage.removeItem(LANGUAGE_STORAGE_KEY);
    location = {
      href: 'https://example.test/invaifront/ca/applications/42?tab=details#title',
      assign: vi.fn(),
      replace: vi.fn(),
    };
    TestBed.configureTestingModule({
      providers: [
        { provide: LOCALE_ID, useValue: Language.CA },
        { provide: DOCUMENT, useValue: { location } },
      ],
    });
    service = TestBed.inject(LanguagePreferenceService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    localStorage.removeItem(LANGUAGE_STORAGE_KEY);
  });

  it('saves the selection before navigating and preserves the current route', () => {
    location.assign.mockImplementation(() => {
      expect(localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe(Language.ES);
    });
    service.select(Language.ES);

    expect(location.assign).toHaveBeenCalledWith(
      'https://example.test/invaifront/es/applications/42?tab=details#title',
    );
  });

  it('saves the current language without reloading', () => {
    service.select(Language.CA);

    expect(localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe(Language.CA);
    expect(location.assign).not.toHaveBeenCalled();
  });

  it('restores the preference when returning from login in another language', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, Language.ES);

    expect(service.restore()).toBe(true);
    expect(location.replace).toHaveBeenCalledWith(
      'https://example.test/invaifront/es/applications/42?tab=details#title',
    );
    expect(localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe(Language.ES);
  });

  it.each([null, '', 'en', 'invalid', Language.CA])(
    'keeps the current language when the preference is %s',
    (stored) => {
      if (stored !== null) localStorage.setItem(LANGUAGE_STORAGE_KEY, stored);

      expect(service.restore()).toBe(false);
      expect(location.replace).not.toHaveBeenCalled();
    },
  );

  it('does not loop if the server serves the wrong bundle for the preferred URL', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, Language.ES);
    location.href = 'https://example.test/invaifront/es/';

    expect(service.restore()).toBe(false);
    expect(location.replace).not.toHaveBeenCalled();
  });

  it('restores the preference from the application root', () => {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, Language.ES);
    location.href = 'https://example.test/invaifront/';

    expect(service.restore()).toBe(true);
    expect(location.replace).toHaveBeenCalledWith('https://example.test/invaifront/es/');
  });

  it('continues initialization when browser storage cannot be read', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new DOMException('Storage blocked', 'SecurityError');
    });

    expect(service.restore()).toBe(false);
    expect(location.replace).not.toHaveBeenCalled();
  });

  it('still switches language when browser storage cannot be written', () => {
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new DOMException('Storage blocked', 'SecurityError');
    });

    service.select(Language.ES);

    expect(location.assign).toHaveBeenCalledOnce();
  });
});
