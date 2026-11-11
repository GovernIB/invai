import { HelpDocument, SearchExcerpt, SearchResult } from './help-topic.model';

const CONTEXT_BEFORE = 40;
const CONTEXT_AFTER = 80;
const ELLIPSIS = '…';
// Any topic with every word in its title goes before any topic without, however many times the
// words repeat in the text of the second one.
const TITLE_SCORE = 1_000_000;

/** Text a reader sees in a Markdown document, on one line and without its syntax. */
export function markdownToPlainText(markdown: string): string {
  return (
    markdown
      .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
      .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
      .replace(/<[^>]+>/g, ' ')
      // Table alignment rows; cells are separated by `|` so the pattern cannot backtrack.
      .replace(/^\s*\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)*\|?\s*$/gm, ' ')
      .replace(/^\s*([-*_]\s*){3,}$/gm, ' ')
      // A heading ends with a full stop, so it does not run into the paragraph that follows it
      // («Què hi trobareu. A dalt…», not «Què hi trobareu A dalt…»).
      .replace(/^[ \t]*#{1,6}[ \t]+(.*?)[ \t]*#*[ \t]*$/gm, (_line, title: string) =>
        /[.!?:…]$/.test(title) ? title : `${title}.`,
      )
      .replace(/^\s*(>|[-*+]|\d+[.)])\s+/gm, '')
      .replace(/\*\*|~~|[*`]/g, '')
      .replace(/\|/g, ' ')
      .replace(/\s+/g, ' ')
      .trim()
  );
}

/**
 * Anchor of a heading as GitHub writes it: lower case, without diacritics or punctuation, and
 * with hyphens for spaces («Com s'hi accedeix» → `com-shi-accedeix`).
 */
export function headingAnchor(text: string): string {
  return foldForSearch(text)
    .replace(/[^\p{L}\p{N}\s-]/gu, '')
    .trim()
    .replace(/\s+/g, '-');
}

/** Lower case without diacritics, so «aplicacio» finds «Aplicació». */
export function foldForSearch(text: string): string {
  return text.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase();
}

/**
 * Finds the first occurrence of `term` in `text`, ignoring case and diacritics, and
 * returns the original text around it cut at word boundaries.
 */
export function findExcerpt(text: string, term: string): SearchExcerpt | null {
  const needle = foldForSearch(term.trim().replace(/\s+/g, ' '));
  if (!needle) return null;

  // Folding can change the length of a character, so keep where each folded unit came from.
  let folded = '';
  const origin: number[] = [];
  for (let index = 0; index < text.length; index++) {
    const unit = foldForSearch(text[index]);
    folded += unit;
    for (let i = 0; i < unit.length; i++) origin.push(index);
  }

  const position = folded.indexOf(needle);
  if (position === -1) return null;

  const start = origin[position];
  const end = origin[position + needle.length - 1] + 1;

  let from = Math.max(0, start - CONTEXT_BEFORE);
  if (from > 0) {
    const space = text.indexOf(' ', from);
    from = space !== -1 && space < start ? space + 1 : start;
  }

  let to = Math.min(text.length, end + CONTEXT_AFTER);
  if (to < text.length) {
    const space = text.lastIndexOf(' ', to);
    to = space > end ? space : end;
  }

  return {
    before: (from > 0 ? ELLIPSIS : '') + text.slice(from, start),
    match: text.slice(start, end),
    after: text.slice(end, to) + (to < text.length ? ELLIPSIS : ''),
  };
}

/**
 * Words of a query to look for, folded and without repeats. One-letter words («i», «a», «k»)
 * appear in almost every topic, so they narrow nothing; a query made only of them is looked for
 * as typed.
 */
export function searchWords(query: string): string[] {
  const folded = foldForSearch(query.trim().replace(/\s+/g, ' '));
  const words = [...new Set(folded.split(' ').filter((word) => word.length > 1))];
  return words.length ? words : folded ? [folded] : [];
}

/**
 * Documents with every word of the query, together or not, in their title or their text.
 * First those with every word in the title, then those that repeat the words most; ties keep
 * the order of the index.
 */
export function searchDocuments(
  documents: readonly (HelpDocument & { text: string })[],
  query: string,
): SearchResult[] {
  const words = searchWords(query);
  if (!words.length) return [];

  return documents
    .flatMap(({ text, ...document }, order) => {
      const title = foldForSearch(document.label);
      const content = foldForSearch(text);
      if (!words.every((word) => title.includes(word) || content.includes(word))) return [];

      const score =
        (words.every((word) => title.includes(word)) ? TITLE_SCORE : 0) +
        words.reduce((sum, word) => sum + occurrences(content, word), 0);
      const result: SearchResult = {
        ...document,
        name: [document.label, ...document.trail].join(', '),
        excerpt: excerptFor(text, content, query, words),
      };
      return [{ result, score, order }];
    })
    .sort((a, b) => b.score - a.score || a.order - b.order)
    .map(({ result }) => result);
}

function occurrences(text: string, word: string): number {
  return text.split(word).length - 1;
}

/**
 * The query as typed when the text has it («Ctrl + K»); otherwise the word that appears first.
 * When the words are only in the title, the beginning of the text with nothing marked.
 */
function excerptFor(text: string, content: string, query: string, words: string[]): SearchExcerpt {
  const phrase = findExcerpt(text, query);
  if (phrase) return phrase;

  const first = words
    .map((word) => ({ word, at: content.indexOf(word) }))
    .filter(({ at }) => at !== -1)
    .sort((a, b) => a.at - b.at)[0];
  return (first && findExcerpt(text, first.word)) ?? leadingExcerpt(text);
}

function leadingExcerpt(text: string): SearchExcerpt {
  if (text.length <= CONTEXT_AFTER) return { before: '', match: '', after: text };
  const space = text.lastIndexOf(' ', CONTEXT_AFTER);
  const to = space > 0 ? space : CONTEXT_AFTER;
  return { before: '', match: '', after: text.slice(0, to) + ELLIPSIS };
}
