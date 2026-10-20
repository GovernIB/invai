import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { version } from '@pkg';

import { AccessibilityStatementDialog } from '../accessibility-statement-dialog/accessibility-statement-dialog';
import { Footer } from './footer';

describe('Footer', () => {
  let fixture: ComponentFixture<Footer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Footer] }).compileComponents();

    fixture = TestBed.createComponent(Footer);
    fixture.detectChanges();
  });

  function trigger(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('footer button') as HTMLButtonElement;
  }

  function statementDialog(): AccessibilityStatementDialog {
    return fixture.debugElement.query(By.directive(AccessibilityStatementDialog))
      .componentInstance as AccessibilityStatementDialog;
  }

  it('shows the copyright, the application version and the statement action', () => {
    const footer = fixture.nativeElement.querySelector('footer') as HTMLElement;

    expect(footer.textContent).toContain('© Govern Illes Balears');
    expect(footer.textContent).toContain(`Versió ${version}`);
    expect(trigger().textContent?.trim()).toBe("Declaració d'accessibilitat");
  });

  it('uses a native button that announces it opens a dialog', () => {
    expect(trigger().getAttribute('type')).toBe('button');
    expect(trigger().getAttribute('aria-haspopup')).toBe('dialog');
  });

  it('opens the accessibility statement from the footer action', () => {
    expect(statementDialog().visible()).toBe(false);

    trigger().click();
    fixture.detectChanges();

    expect(statementDialog().visible()).toBe(true);
  });

  it('returns focus to the footer action when the statement is hidden', () => {
    trigger().click();
    fixture.detectChanges();

    statementDialog().hidden.emit();

    expect(document.activeElement).toBe(trigger());
  });
});
