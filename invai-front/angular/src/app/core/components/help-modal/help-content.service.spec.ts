import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { helpLocale, HelpContentService } from './help-content.service';
import { HelpTopic } from './help-topic.model';

const TOPICS: HelpTopic[] = [
  {
    label: 'Primers passos',
    items: [{ label: "Com fer servir l'ajuda", document: 'getting-started/using-help.md' }],
  },
  { label: 'Aplicacions', document: 'applications/list.md' },
];

describe('helpLocale', () => {
  it('uses the Spanish folder for Spanish builds and Catalan for everything else', () => {
    expect(helpLocale('es')).toBe('es');
    expect(helpLocale('es-ES')).toBe('es');
    expect(helpLocale('ca')).toBe('ca');
    expect(helpLocale('en-US')).toBe('ca');
  });
});

describe('HelpContentService', () => {
  let service: HelpContentService;
  let httpTesting: HttpTestingController;

  function setup(locale = 'ca') {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: locale },
      ],
    });
    service = TestBed.inject(HelpContentService);
    httpTesting = TestBed.inject(HttpTestingController);
  }

  afterEach(() => httpTesting.verify());

  it('reads the index of the running language', () => {
    setup('es');

    service.getTopics().subscribe();

    httpTesting.expectOne('help/es/index.json').flush(TOPICS);
  });

  it('shares one index request and reuses it afterwards', async () => {
    setup();
    const first = firstValueFrom(service.getTopics());
    const second = firstValueFrom(service.getTopics());

    httpTesting.expectOne('help/ca/index.json').flush(TOPICS);

    expect(await first).toEqual(TOPICS);
    expect(await second).toEqual(TOPICS);
    expect(await firstValueFrom(service.getTopics())).toEqual(TOPICS);
  });

  it('retries the index after a failed load', async () => {
    setup();
    const failed = firstValueFrom(service.getTopics());
    httpTesting.expectOne('help/ca/index.json').flush('', { status: 404, statusText: 'Not Found' });
    await expect(failed).rejects.toBeTruthy();

    const retried = firstValueFrom(service.getTopics());
    httpTesting.expectOne('help/ca/index.json').flush(TOPICS);

    expect(await retried).toEqual(TOPICS);
  });

  it('caches each document and resolves its images relative to the document', async () => {
    setup();
    const markdown = [
      '### Llista',
      '![Llista d’aplicacions](imgs/list.webp "Llista")',
      '![Logo](../shared/logo.webp)',
      '![Extern](https://example.org/a.png)',
    ].join('\n');

    const first = firstValueFrom(service.loadDocument('applications/list.md'));
    httpTesting.expectOne('help/ca/applications/list.md').flush(markdown);
    const content = await first;

    const base = document.baseURI;
    expect(content).toContain(
      `![Llista d’aplicacions](${new URL('help/ca/applications/imgs/list.webp', base).href} "Llista")`,
    );
    expect(content).toContain(`![Logo](${new URL('help/ca/shared/logo.webp', base).href})`);
    expect(content).toContain('![Extern](https://example.org/a.png)');
    expect(await firstValueFrom(service.loadDocument('applications/list.md'))).toBe(content);
  });

  it('resolves links to other help documents and headings, and nothing else', () => {
    setup();
    const from = 'getting-started/using-help.md';

    expect(service.resolveLink('../applications/list.md', from)).toEqual({
      document: 'applications/list.md',
      anchor: '',
    });
    expect(service.resolveLink('../applications/list.md#filtres', from)).toEqual({
      document: 'applications/list.md',
      anchor: 'filtres',
    });
    expect(service.resolveLink('#com-shi-accedeix', from)).toEqual({
      document: from,
      anchor: 'com-shi-accedeix',
    });
    expect(service.resolveLink('https://www.caib.es/ajuda.md', from)).toBeNull();
    expect(service.resolveLink('/aplicacions', from)).toBeNull();
    expect(service.resolveLink('imgs/list.webp', from)).toBeNull();
    expect(service.resolveLink('../../es/applications/list.md', from)).toBeNull();
    expect(service.resolveLink('http://[', from)).toBeNull();
  });

  it('finds the term in the visible text of every document, nested ones included', async () => {
    setup();
    const results = firstValueFrom(service.searchInAllDocs('CERCADOR'));

    httpTesting.expectOne('help/ca/index.json').flush(TOPICS);
    httpTesting
      .expectOne('help/ca/getting-started/using-help.md')
      .flush("### Com fer servir l'ajuda\n\nEl **cercador** busca en tots els temes.");
    httpTesting
      .expectOne('help/ca/applications/list.md')
      .flush('### Aplicacions\n\n![Llista](imgs/cercador.webp)');

    expect(await results).toEqual([
      {
        label: "Com fer servir l'ajuda",
        document: 'getting-started/using-help.md',
        trail: ['Primers passos'],
        name: "Com fer servir l'ajuda, Primers passos",
        excerpt: {
          before: "Com fer servir l'ajuda. El ",
          match: 'cercador',
          after: ' busca en tots els temes.',
        },
      },
    ]);
  });

  it('searches a document that does not load by its title, and the others as usual', async () => {
    setup();
    const results = firstValueFrom(service.searchInAllDocs('aplicacions'));

    httpTesting.expectOne('help/ca/index.json').flush(TOPICS);
    httpTesting
      .expectOne('help/ca/getting-started/using-help.md')
      .flush("### Com fer servir l'ajuda\n\nExplica les pantalles de les aplicacions.");
    httpTesting
      .expectOne('help/ca/applications/list.md')
      .flush('', { status: 500, statusText: 'Server Error' });

    expect((await results).map(({ label }) => label)).toEqual([
      'Aplicacions',
      "Com fer servir l'ajuda",
    ]);
  });
});
