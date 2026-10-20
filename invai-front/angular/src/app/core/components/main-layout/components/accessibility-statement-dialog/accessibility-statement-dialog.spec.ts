import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Dialog } from 'primeng/dialog';

import { AccessibilityStatementDialog } from './accessibility-statement-dialog';

describe('AccessibilityStatementDialog', () => {
  let fixture: ComponentFixture<AccessibilityStatementDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccessibilityStatementDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AccessibilityStatementDialog);
    fixture.componentRef.setInput('visible', true);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  function dialogElement(): HTMLElement {
    return fixture.nativeElement.querySelector('[role="dialog"]') as HTMLElement;
  }

  it('renders a modal dialog named by its level-two title', () => {
    const dialog = dialogElement();
    const titleId = dialog.getAttribute('aria-labelledby');
    const title = titleId ? fixture.nativeElement.querySelector(`[id="${titleId}"]`) : null;

    expect(dialog.getAttribute('aria-modal')).toBe('true');
    expect(title?.tagName).toBe('H2');
    expect(title?.textContent?.trim()).toBe("Declaració d'accessibilitat");
  });

  it('labels the icon-only close button in the interface language', () => {
    const closeButton = dialogElement().querySelector('.p-dialog-close-button');

    expect(closeButton?.getAttribute('aria-label')).toBe("Tanca la declaració d'accessibilitat");
  });

  it('informs that the first accessibility audit is still pending', () => {
    const notice = dialogElement().querySelector('.accessibility-statement__start');

    expect(notice?.textContent).toContain(
      "La primera auditoria d'accessibilitat d'aquesta aplicació encara està pendent de realitzar.",
    );
  });

  it('starts focus on the notice when the dialog is shown', () => {
    fixture.debugElement.query(By.directive(Dialog)).componentInstance.onShow.emit({});

    expect(document.activeElement?.classList).toContain('accessibility-statement__start');
    expect(document.activeElement?.getAttribute('tabindex')).toBe('-1');
  });

  it('notifies the owner once the dialog has been hidden', () => {
    const hidden = vi.fn();
    fixture.componentInstance.hidden.subscribe(hidden);

    fixture.debugElement.query(By.directive(Dialog)).componentInstance.onHide.emit({});

    expect(hidden).toHaveBeenCalledOnce();
  });
});
