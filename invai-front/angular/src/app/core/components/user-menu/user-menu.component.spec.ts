import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { OAuthService } from '@core/services/auth.service';
import { LanguagePreferenceService } from '@core/services/language-preference.service';

import { HelpModalService } from './components/help-modal/help-modal.service';
import { UserMenuComponent } from './user-menu.component';

describe('UserMenuComponent', () => {
  it('should expose the popover state on the native trigger button', async () => {
    await TestBed.configureTestingModule({
      imports: [UserMenuComponent],
      providers: [
        {
          provide: OAuthService,
          useValue: {
            currentUser: signal({ username: 'jdoe', fullName: 'Jane Doe' }),
            logout: vi.fn(),
          },
        },
        { provide: HelpModalService, useValue: { open: vi.fn() } },
        { provide: LanguagePreferenceService, useValue: { select: vi.fn() } },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(UserMenuComponent);
    fixture.detectChanges();
    const host = fixture.nativeElement.querySelector(
      '[data-testid="button-user-menu-testid"]',
    ) as HTMLElement;
    const button = host.querySelector('button') as HTMLButtonElement;

    expect(host.hasAttribute('aria-expanded')).toBe(false);
    expect(button.getAttribute('aria-expanded')).toBe('false');

    button.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('true');

    button.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
  });
});
