import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Editor } from 'primeng/editor';
import type { EditorInitEvent } from 'primeng/types/editor';
import { RichTextAria } from './rich-text-aria.directive';

@Component({
  standalone: true,
  imports: [Editor, ReactiveFormsModule, RichTextAria],
  template: `
    <p-editor
      [formControl]="control"
      [invalid]="invalid()"
      [appRichTextAria]="describedBy()"
      [appRichTextLabelledBy]="labelledBy()"
    />
  `,
})
class HostComponent {
  readonly control = new FormControl('');
  readonly invalid = signal(false);
  readonly describedBy = signal<string | null>(null);
  readonly labelledBy = signal<string | null>(null);
}

describe('RichTextAria', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  let root: HTMLElement;

  const initQuill = () => {
    const editor = fixture.debugElement.query(By.directive(Editor)).injector.get(Editor);
    editor.onEditorInit.emit({ editor: { root } } as unknown as EditorInitEvent);
    fixture.detectChanges();
  };

  beforeEach(() => {
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    root = document.createElement('div');
    fixture.detectChanges();
  });

  it('exposes the invalid state and the visible error on the editable root', () => {
    initQuill();
    expect(root.hasAttribute('aria-invalid')).toBe(false);
    expect(root.hasAttribute('aria-describedby')).toBe(false);

    host.invalid.set(true);
    host.describedBy.set('observations-error');
    fixture.detectChanges();

    expect(root.getAttribute('aria-invalid')).toBe('true');
    expect(root.getAttribute('aria-describedby')).toBe('observations-error');

    host.invalid.set(false);
    host.describedBy.set(null);
    fixture.detectChanges();

    expect(root.hasAttribute('aria-invalid')).toBe(false);
    expect(root.hasAttribute('aria-describedby')).toBe(false);
  });

  it('applies a state set before Quill finished loading', () => {
    host.invalid.set(true);
    host.describedBy.set('observations-error');
    fixture.detectChanges();

    initQuill();

    expect(root.getAttribute('aria-invalid')).toBe('true');
    expect(root.getAttribute('aria-describedby')).toBe('observations-error');
  });

  it('labels the root as a multi-line textbox only when asked to', () => {
    initQuill();
    expect(root.hasAttribute('role')).toBe(false);

    host.labelledBy.set('observations-label');
    fixture.detectChanges();

    expect(root.getAttribute('role')).toBe('textbox');
    expect(root.getAttribute('aria-multiline')).toBe('true');
    expect(root.getAttribute('aria-labelledby')).toBe('observations-label');
  });
});
