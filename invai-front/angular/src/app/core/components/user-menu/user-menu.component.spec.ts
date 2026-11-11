import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { OAuthService } from '@core/services/auth.service';
import { LanguagePreferenceService } from '@core/services/language-preference.service';

import { HelpModalService } from '@core/components/help-modal/help-modal.service';
import { UserMenuComponent } from './user-menu.component';

describe('UserMenuComponent', () => {
  async function renderMenu(user: { username: string; fullName?: string }) {
    await TestBed.configureTestingModule({
      imports: [UserMenuComponent],
      providers: [
        {
          provide: OAuthService,
          useValue: { currentUser: signal(user), logout: vi.fn() },
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
    return { fixture, host };
  }

  it('should expose the popover state on the native trigger button', async () => {
    const { fixture, host } = await renderMenu({ username: 'jdoe', fullName: 'Jane Doe' });
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

  it('should fall back to the username when there is no full name', async () => {
    const { host } = await renderMenu({ username: 'mgarcia' });

    expect(host.textContent).toContain('Mgarcia');
    expect(host.querySelector('[data-testid="user-menu-full-name"]')).toBeNull();
    expect(host.querySelector('[data-testid="user-menu-username"]')).toBeNull();
    expect(host.querySelector('p-avatar')?.textContent?.trim()).toBe('M');
  });
});
