import { FormControl } from '@angular/forms';
import {
  MAX_BYTES_ERROR,
  maxUtf8BytesValidator,
  richTextMaxBytesValidator,
  utf8ByteLength,
} from './text-length.utils';

describe('text length utils', () => {
  it.each([
    [null, 0],
    [undefined, 0],
    ['', 0],
    ['abc', 3],
    ['àç', 4],
    ['€', 3],
    ['😀', 4],
  ])('measures %s as %i UTF-8 bytes', (value, expected) => {
    expect(utf8ByteLength(value)).toBe(expected);
  });

  describe('maxUtf8BytesValidator', () => {
    const validate = (value: string | null) =>
      new FormControl(value, maxUtf8BytesValidator(4000)).errors;

    it('accepts empty values and values up to the limit', () => {
      expect(validate(null)).toBeNull();
      expect(validate('a'.repeat(4000))).toBeNull();
    });

    it('rejects values over the limit and reports the measured size', () => {
      expect(validate('a'.repeat(4001))).toEqual({ [MAX_BYTES_ERROR]: { max: 4000, actual: 4001 } });
    });

    it('counts accented characters as two bytes', () => {
      expect(validate('à'.repeat(2000))).toBeNull();
      expect(validate(`${'à'.repeat(2000)}a`)).toEqual({
        [MAX_BYTES_ERROR]: { max: 4000, actual: 4001 },
      });
    });

    it('ignores the surrounding whitespace that is trimmed before saving', () => {
      expect(validate(`  ${'a'.repeat(4000)}  `)).toBeNull();
    });
  });

  describe('richTextMaxBytesValidator', () => {
    const validate = (value: string | null) =>
      new FormControl(value, richTextMaxBytesValidator(4000)).errors;

    it('accepts visually empty editor content', () => {
      expect(validate('<p><br></p>')).toBeNull();
    });

    it('measures the compacted HTML that is persisted', () => {
      const words = Array.from({ length: 400 }, () => 'paraula').join('&nbsp;');
      const html = `<p>${words}</p>`;

      expect(html.length).toBeGreaterThan(4000);
      expect(validate(html)).toBeNull();
    });

    it('rejects HTML that is still over the limit once compacted', () => {
      expect(validate(`<p>${'a'.repeat(3994)}</p>`)).toEqual({
        [MAX_BYTES_ERROR]: { max: 4000, actual: 4001 },
      });
    });
  });
});
