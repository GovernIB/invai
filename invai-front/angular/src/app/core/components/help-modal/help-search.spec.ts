import { HelpDocument } from './help-topic.model';
import {
  findExcerpt,
  foldForSearch,
  headingAnchor,
  markdownToPlainText,
  searchDocuments,
  searchWords,
} from './help-search';

describe('markdownToPlainText', () => {
  it('keeps the visible text and drops the Markdown syntax', () => {
    const markdown = [
      "### Com fer servir l'ajuda",
      '',
      '> Nota: **important** i *cursiva*, amb `codi` i ~~ratllat~~.',
      '',
      '- Premeu [Ctrl + K](getting-started/using-help.md).',
      '1. Primer pas',
      '',
      '![Captura de la llista](imgs/list.webp "Llista")',
      '',
      '---',
      '',
      '| Camp | Descripció |',
      '| :--- | ---------: |',
      '| Nom | Obligatori |',
      '',
      'Text amb <br> salt.',
    ].join('\n');

    expect(markdownToPlainText(markdown)).toBe(
      "Com fer servir l'ajuda. Nota: important i cursiva, amb codi i ratllat. " +
        'Premeu Ctrl + K. Primer pas Camp Descripció Nom Obligatori Text amb salt.',
    );
  });

  it('leaves out the review notes for the team', () => {
    const markdown = 'Escriviu el codi.\n   <!-- REVISAR: en quins camps busca -->\nFet.';

    expect(markdownToPlainText(markdown)).toBe('Escriviu el codi. Fet.');
  });

  it('does not backtrack on long lines of dashes', () => {
    const line = `${'-'.repeat(5000)}x`;
    const started = performance.now();

    expect(markdownToPlainText(line)).toBe(line);
    expect(performance.now() - started).toBeLessThan(200);
  });
});

describe('foldForSearch', () => {
  it('ignores case and diacritics', () => {
    expect(foldForSearch('Aplicació Çà ÜÏ Ñ')).toBe('aplicacio ca ui n');
  });
});

describe('findExcerpt', () => {
  it('returns the original text of the match, whatever the case and accents typed', () => {
    expect(findExcerpt('Gestió de les aplicacions', 'GESTIO')).toEqual({
      before: '',
      match: 'Gestió',
      after: ' de les aplicacions',
    });
  });

  it('collapses the spaces typed in the term', () => {
    expect(findExcerpt('Premeu Ctrl + K', '  ctrl   +  k ')?.match).toBe('Ctrl + K');
  });

  it('cuts long texts at word boundaries and marks the cuts', () => {
    const words = Array.from({ length: 60 }, (_, index) => `mot${index}`);
    const excerpt = findExcerpt(words.join(' '), 'mot30');

    expect(excerpt?.match).toBe('mot30');
    expect(excerpt?.before).toMatch(/^…mot\d+ .* $/);
    expect(excerpt?.after).toMatch(/^ .* mot\d+…$/);
    expect(excerpt!.before.length).toBeLessThanOrEqual(42);
    expect(excerpt!.after.length).toBeLessThanOrEqual(82);
  });

  it('returns null when the term is missing or blank', () => {
    expect(findExcerpt('Aplicacions', 'sistemes')).toBeNull();
    expect(findExcerpt('Aplicacions', '   ')).toBeNull();
  });
});

describe('searchWords', () => {
  it('folds the words of the query, without repeats or one-letter words', () => {
    expect(searchWords('  Llista  d’APLICACIONS i aplicacions ')).toEqual([
      'llista',
      'd’aplicacions',
      'aplicacions',
    ]);
  });

  it('looks for a query of one-letter words as typed', () => {
    expect(searchWords(' +  K ')).toEqual(['+ k']);
    expect(searchWords('   ')).toEqual([]);
  });
});

describe('searchDocuments', () => {
  const doc = (label: string, text: string, trail: string[] = []) => ({
    label,
    document: `${label}.md`,
    trail,
    text,
  });
  const labels = (results: HelpDocument[]) => results.map(({ label }) => label);

  it('finds the documents with every word, together or not, in the title or the text', () => {
    const documents = [
      doc('Alta', 'Ompliu el formulari i deseu.'),
      doc('Llista', 'Cerqueu una aplicació al formulari de filtres.'),
      doc('Filtres', 'Per cercar una aplicació, ompliu el formulari.'),
    ];

    expect(labels(searchDocuments(documents, 'formulari APLICACIO'))).toEqual([
      'Llista',
      'Filtres',
    ]);
  });

  it('puts first the documents with every word in the title, then the ones that repeat them most', () => {
    const documents = [
      doc('Ajuda', 'Les aplicacions. Més aplicacions.'),
      doc('Fitxa', 'Una aplicació, una altra aplicació i una tercera aplicació.'),
      doc('Llista d’aplicacions', 'La taula.'),
      doc('Seguretat', 'Les dades de cada aplicació.'),
    ];

    expect(labels(searchDocuments(documents, 'aplicacio'))).toEqual([
      'Llista d’aplicacions',
      'Fitxa',
      'Ajuda',
      'Seguretat',
    ]);
  });

  it('keeps the order of the index between documents that match as well', () => {
    const documents = [doc('Primer', 'Un filtre.'), doc('Segon', 'Un filtre.')];

    expect(labels(searchDocuments(documents, 'filtre'))).toEqual(['Primer', 'Segon']);
  });

  it('names each result by its title and its groups', () => {
    const [result] = searchDocuments(
      [doc('General', 'Les dades generals.', ['Aplicacions', 'Fitxa'])],
      'dades',
    );

    expect(result).toMatchObject({
      label: 'General',
      document: 'General.md',
      trail: ['Aplicacions', 'Fitxa'],
      name: 'General, Aplicacions, Fitxa',
    });
  });

  it('marks the query as typed when the text has it, or else the word that comes first', () => {
    const text = 'Premeu Ctrl + K per obrir l’ajuda des de qualsevol pàgina.';

    expect(searchDocuments([doc('Ajuda', text)], 'ctrl + k')[0].excerpt.match).toBe('Ctrl + K');
    expect(searchDocuments([doc('Ajuda', text)], 'pagina premeu')[0].excerpt.match).toBe('Premeu');
  });

  it('shows the beginning of the text, with nothing marked, when the words are only in the title', () => {
    const [result] = searchDocuments(
      [doc('Declaració d’accessibilitat', 'Text breu.')],
      'declaracio',
    );

    expect(result.excerpt).toEqual({ before: '', match: '', after: 'Text breu.' });
  });

  it('finds nothing for a blank query', () => {
    expect(searchDocuments([doc('Ajuda', 'Text.')], '  ')).toEqual([]);
  });
});

describe('headingAnchor', () => {
  it('writes the anchor of a heading as GitHub does', () => {
    expect(headingAnchor("Com s'hi accedeix")).toBe('com-shi-accedeix');
    expect(headingAnchor('  Què hi trobareu?  ')).toBe('que-hi-trobareu');
    expect(headingAnchor('Cómo se accede')).toBe('como-se-accede');
  });
});
