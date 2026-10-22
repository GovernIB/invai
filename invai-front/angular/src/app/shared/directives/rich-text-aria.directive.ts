import { Directive, effect, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Editor } from 'primeng/editor';

/**
 * Exposes a PrimeNG editor's semantics on Quill's editable root, which is the element assistive
 * technologies interact with: `aria-invalid` follows the editor's `[invalid]` input and
 * `aria-describedby` points to the visible error. When `appRichTextLabelledBy` is given, the root
 * is also exposed as a labelled multi-line textbox. The root only exists once Quill has loaded, so
 * the attributes are applied after `onInit` and kept in sync afterwards.
 */
@Directive({
  selector: 'p-editor[appRichTextAria]',
  standalone: true,
})
export class RichTextAria {
  /** ID of the visible error message, or `null` while there is none. */
  readonly describedBy = input<string | null>(null, { alias: 'appRichTextAria' });
  /** ID of the visible label, for editors whose root is not labelled elsewhere. */
  readonly labelledBy = input<string | null>(null, { alias: 'appRichTextLabelledBy' });

  private readonly editor = inject(Editor);
  private readonly root = signal<HTMLElement | null>(null);

  constructor() {
    this.editor.onEditorInit
      .pipe(takeUntilDestroyed())
      .subscribe(({ editor }) => this.root.set((editor?.root as HTMLElement | undefined) ?? null));

    effect(() => {
      const root = this.root();
      if (!root) return;

      const labelledBy = this.labelledBy();
      if (labelledBy) {
        root.setAttribute('role', 'textbox');
        root.setAttribute('aria-multiline', 'true');
        root.setAttribute('aria-labelledby', labelledBy);
      }
      setAttribute(root, 'aria-invalid', this.editor.invalid() ? 'true' : null);
      setAttribute(root, 'aria-describedby', this.describedBy());
    });
  }
}

function setAttribute(element: HTMLElement, name: string, value: string | null): void {
  if (value === null) {
    element.removeAttribute(name);
  } else {
    element.setAttribute(name, value);
  }
}
