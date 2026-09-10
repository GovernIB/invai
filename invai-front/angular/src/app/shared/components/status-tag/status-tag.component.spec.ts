import { TestBed } from '@angular/core/testing';
import { StatusTagComponent } from './status-tag.component';

describe('StatusTagComponent', () => {
  it.each([
    [true, 'Actiu', true],
    [false, 'Inactiu', false],
    [null, 'Desconegut', false],
  ] as const)('renders %s as a non-interactive, labelled status', (active, label, green) => {
    const fixture = TestBed.createComponent(StatusTagComponent);
    fixture.componentRef.setInput('active', active);
    fixture.componentRef.setInput('label', label);
    fixture.detectChanges();

    const tag: HTMLElement = fixture.nativeElement.querySelector('.invai-status-tag');
    expect(tag.textContent?.trim()).toBe(label);
    expect(tag.classList.contains('invai-status-tag--active')).toBe(green);
    expect(fixture.nativeElement.querySelector('button, a, [tabindex], .p-tag-icon')).toBeNull();
  });

  it('does not invent a label for an unmapped status', () => {
    const fixture = TestBed.createComponent(StatusTagComponent);
    fixture.componentRef.setInput('active', null);
    fixture.componentRef.setInput('label', '');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('p-tag')).toBeNull();
  });
});
