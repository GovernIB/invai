export function normalizeQuillHtml(value: string | null | undefined): string {
  if (!value) return '';

  const visibleText = value
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;|&#160;|&#xa0;/gi, ' ')
    .trim();

  return visibleText ? value : '';
}
