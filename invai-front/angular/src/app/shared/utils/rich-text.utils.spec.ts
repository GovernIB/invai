import { compactQuillHtml, normalizeQuillHtml } from './rich-text.utils';

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

  it('compacts the non-breaking spaces Quill emits between words', () => {
    expect(normalizeQuillHtml('<p>Una&nbsp;descripció&nbsp;llarga</p>')).toBe(
      '<p>Una descripció llarga</p>',
    );
  });

  describe('compactQuillHtml', () => {
    it.each([
      ['single spaces between words', '<p>a&nbsp;b&nbsp;c</p>', '<p>a b c</p>'],
      ['a run of spaces between words', '<p>a&nbsp;&nbsp;&nbsp;b</p>', '<p>a &nbsp; b</p>'],
      ['leading spaces of a block', '<p>&nbsp;&nbsp;a</p>', '<p>&nbsp; a</p>'],
      ['trailing spaces of a block', '<p>a&nbsp;&nbsp;</p>', '<p>a &nbsp;</p>'],
      ['a block that only holds a space', '<p>&nbsp;</p>', '<p>&nbsp;</p>'],
      [
        'spaces next to inline tags',
        '<p>a&nbsp;<strong>b</strong>&nbsp;c</p>',
        '<p>a&nbsp;<strong>b</strong>&nbsp;c</p>',
      ],
      ['spaces after escaped characters', '<p>a&nbsp;&amp;&nbsp;b</p>', '<p>a &amp; b</p>'],
      ['literal "&nbsp;" text typed by the user', '<p>&amp;nbsp;</p>', '<p>&amp;nbsp;</p>'],
    ])('keeps the rendered spacing of %s', (_case, input, expected) => {
      expect(compactQuillHtml(input)).toBe(expected);
    });

    it('never places a collapsible space next to a tag or another regular space', () => {
      const compacted = compactQuillHtml(
        '<p>&nbsp;Un&nbsp;&nbsp;text&nbsp;<em>amb</em>&nbsp;&nbsp;&nbsp;espais&nbsp;</p>',
      );

      expect(compacted).toBe(
        '<p>&nbsp;Un &nbsp;text&nbsp;<em>amb</em>&nbsp; &nbsp;espais&nbsp;</p>',
      );
      expect(compacted).not.toMatch(/> | <|  /);
    });
  });
});
