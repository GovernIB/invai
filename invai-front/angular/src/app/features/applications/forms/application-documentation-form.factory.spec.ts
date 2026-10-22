import { FormBuilder } from '@angular/forms';

import {
  applicationAttachmentFileRejection,
  createApplicationAttachmentFiltersForm,
  createApplicationAttachmentForm,
  createApplicationLinkForm,
  fileExtensionOf,
} from './application-documentation-form.factory';

describe('application documentation forms', () => {
  const fb = new FormBuilder();

  it('requires everything the back-end needs to create a document but the subcategory', () => {
    const form = createApplicationAttachmentForm(fb);

    expect(form.getRawValue()).toEqual({
      title: '',
      sectionId: null,
      file: null,
      categoryId: null,
      subcategoryId: null,
      versionLabel: '',
    });
    expect(
      Object.entries(form.controls)
        .filter(([, control]) => control.invalid)
        .map(([key]) => key),
    ).toEqual(['title', 'sectionId', 'file', 'categoryId', 'versionLabel']);

    form.patchValue({ title: ' ', versionLabel: 'x'.repeat(21) });
    expect([form.controls.title.hasError('pattern'), form.controls.versionLabel.hasError('maxlength')]).toEqual([
      true,
      true,
    ]);

    form.patchValue({
      title: 'Pla de proves',
      sectionId: 2,
      file: new File(['%PDF'], 'pla.pdf'),
      categoryId: 1,
      versionLabel: '1.0',
    });
    expect(form.valid).toBe(true);
  });

  it('rejects files larger than 5 MB and, once known, formats that are not accepted', () => {
    let extensions: string[] = [];
    const file = createApplicationAttachmentForm(fb, () => extensions).controls.file;

    file.setValue(new File(['x'], 'programa.exe'));
    expect(file.valid).toBe(true);

    extensions = ['pdf', 'docx'];
    file.updateValueAndValidity();
    expect(file.hasError('fileType')).toBe(true);

    file.setValue(new File(['x'], 'Manual.PDF'));
    expect(file.valid).toBe(true);

    file.setValue(new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'gran.pdf'));
    expect(file.hasError('fileTooLarge')).toBe(true);
  });

  it('tells why the back-end would refuse a file', () => {
    const extensions = ['pdf'];

    expect(applicationAttachmentFileRejection(new File(['x'], 'manual.pdf'), extensions)).toBeNull();
    expect(applicationAttachmentFileRejection(new File(['x'], 'programa.exe'), extensions)).toBe('fileType');
    expect(applicationAttachmentFileRejection(new File(['x'], 'programa.exe'), [])).toBeNull();
    expect(
      applicationAttachmentFileRejection(new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'gran.exe'), extensions),
    ).toBe('fileTooLarge');
  });

  it('reads the extension of a file name', () => {
    expect([fileExtensionOf('a.b.PDF'), fileExtensionOf('README'), fileExtensionOf('.env')]).toEqual([
      'pdf',
      '',
      '',
    ]);
  });

  it('accepts only http and https URLs for a link, with the scheme in lower case as the back-end does', () => {
    const url = createApplicationLinkForm(fb).controls.url;

    for (const [value, valid] of [
      ['https://example.com/a', true],
      ['http://example.com', true],
      ['HTTP://example.com', false],
      ['example.com', false],
      ['ftp://example.com', false],
      ['https://exa mple.com', false],
    ] as const) {
      url.setValue(value);
      expect(url.valid).toBe(valid);
    }
  });

  it('limits the fields of a link as the back-end does', () => {
    const { title, description, url } = createApplicationLinkForm(fb).controls;
    const longUrl = (length: number) => 'https://' + 'a'.repeat(length - 'https://'.length);

    title.setValue('t'.repeat(250));
    description.setValue('d'.repeat(1000));
    url.setValue(longUrl(500));
    expect([title.valid, description.valid, url.valid]).toEqual([true, true, true]);

    title.setValue('t'.repeat(251));
    description.setValue('d'.repeat(1001));
    url.setValue(longUrl(501));
    expect([title, description, url].map((control) => control.hasError('maxlength'))).toEqual([true, true, true]);
  });

  it('starts the attachment filters empty, with the subcategory waiting for a category', () => {
    const filters = createApplicationAttachmentFiltersForm(fb);

    expect(filters.getRawValue()).toEqual({
      sectionId: null,
      mimeTypeId: null,
      categoryId: null,
      subcategoryId: null,
      statusId: null,
    });
    expect(filters.controls.subcategoryId.disabled).toBe(true);
  });
});
