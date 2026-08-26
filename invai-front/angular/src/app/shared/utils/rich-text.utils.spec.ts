import { normalizeQuillHtml } from './rich-text.utils';

describe('rich text utils', () => {
  it.each([
    null,
    undefined,
    '',
    '   ',
    '<p><br></p>',
    '<p><br></p><p>&nbsp;</p>',
    '<p>&#160;</p>',
    '<p>&#xA0;</p>',
  ])(
    'normalizes visually empty Quill content %s to an empty string',
    (value) => {
      expect(normalizeQuillHtml(value)).toBe('');
    },
  );

  it('preserves Quill HTML containing visible text', () => {
    const value = '<p>Description with <strong>format</strong></p>';

    expect(normalizeQuillHtml(value)).toBe(value);
  });
});
