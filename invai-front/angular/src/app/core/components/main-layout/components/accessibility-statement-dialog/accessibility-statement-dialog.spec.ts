import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { accessibilityEvaluation } from '@pkg';
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

  it('names the application and its address from the evaluation data of package.json', () => {
    const text = dialogElement().textContent ?? '';
    const appLink = dialogElement().querySelector(`a[href="${accessibilityEvaluation.urlApp}"]`);

    expect(text).toContain(`l'aplicació ${accessibilityEvaluation.app}`);
    expect(appLink?.textContent?.trim()).toBe(accessibilityEvaluation.urlApp);
  });

  it('shows the evaluation date, score, level and compliance status in a labelled table', () => {
    const table = dialogElement().querySelector('table') as HTMLTableElement;
    const title = dialogElement().querySelector(`[id="${table.getAttribute('aria-labelledby')}"]`);
    const rows = Array.from(table.querySelectorAll('tbody tr')).map((row) => [
      row.querySelector('th')?.textContent?.trim(),
      row.querySelector('td')?.textContent?.trim(),
    ]);

    expect(title?.tagName).toBe('H3');
    expect(rows).toEqual([
      ['Puntuació mitjana del lloc web', accessibilityEvaluation.score],
      ["Nivell d'adequació estimat", accessibilityEvaluation.level],
      ["Situació d'acompliment estimada", accessibilityEvaluation.complianceStatus],
    ]);
    expect(dialogElement().textContent).toContain(accessibilityEvaluation.dateText);
  });

  it('structures the statement with level-three headings under the dialog title', () => {
    const headings = Array.from(dialogElement().querySelectorAll('h3')).map((heading) =>
      heading.textContent?.trim(),
    );

    expect(headings).toEqual([
      'Situació de compliment',
      'Contingut no accessible',
      "Preparació de la present declaració d'accessibilitat",
      'Observacions i dades de contacte',
      "Procediment d'aplicació",
      'Informació addicional',
    ]);
  });

  it('opens every external link in a new tab without leaking the opener', () => {
    const links = Array.from(dialogElement().querySelectorAll('a'));

    expect(links.length).toBeGreaterThan(0);
    for (const link of links) {
      expect(link.getAttribute('target')).toBe('_blank');
      expect(link.getAttribute('rel')).toBe('noopener noreferrer');
    }
  });

  it('starts focus on the first paragraph when the dialog is shown', () => {
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
