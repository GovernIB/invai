const NBSP_ENTITY = '&nbsp;';
const NBSP_RUN = /(?:&nbsp;)+/g;

export function normalizeQuillHtml(value: string | null | undefined): string {
  if (!value) return '';

  const visibleText = value
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;|&#160;|&#xa0;/gi, ' ')
    .trim();

  return visibleText ? compactQuillHtml(value) : '';
}

/**
 * Quill's semantic HTML encodes every space as `&nbsp;`, which multiplies the stored size of the
 * text. Replaces each `&nbsp;` with a regular space wherever the browser would still render it,
 * keeping the entity next to tags and between consecutive spaces so the content looks the same.
 */
export function compactQuillHtml(value: string): string {
  return value.replace(NBSP_RUN, (run: string, offset: number, html: string) => {
    const count = run.length / NBSP_ENTITY.length;
    const previous = html.charAt(offset - 1);
    const next = html.charAt(offset + run.length);
    const tokens: string[] = [];

    for (let index = 0; index < count; index++) {
      const followsText =
        index === 0 ? isTextCharacter(previous) : tokens[index - 1] === NBSP_ENTITY;
      const precedesText = index < count - 1 || isTextCharacter(next);
      tokens.push(followsText && precedesText ? ' ' : NBSP_ENTITY);
    }

    return tokens.join('');
  });
}

function isTextCharacter(character: string): boolean {
  return character !== '' && character !== '<' && character !== '>' && !/\s/.test(character);
}
