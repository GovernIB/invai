import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideMarkdown } from 'ngx-markdown';
import { AutoComplete } from 'primeng/autocomplete';
import { Dialog } from 'primeng/dialog';
import { of, Subject } from 'rxjs';

import { HelpModal } from './help-modal';
import { HelpContentService } from './help-content.service';
import { HelpModalService } from './help-modal.service';
import { HelpTopic, SearchResult } from './help-topic.model';
import { HELP_TOPICS } from './help-topics';

function result(label: string, match: string, trail: string[] = []): SearchResult {
  return {
    label,
    document: `${label}.md`,
    trail,
    name: [label, ...trail].join(', '),
    excerpt: { before: '…el ', match, after: ' busca en tots els temes.' },
  };
}

const TOPICS: HelpTopic[] = [
  { label: "Com fer servir l'ajuda", document: 'getting-started/using-help.md' },
];

// Dialogs can only declare documents of HELP_TOPICS.
const APPLICATIONS_LIST: HelpTopic[] = [
  { label: "Llista d'aplicacions", document: HELP_TOPICS.applicationsList },
];

describe('HelpModal', () => {
  let fixture: ComponentFixture<HelpModal>;
  let service: HelpModalService;
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    // The overlay of PrimeNG AutoComplete listens to a breakpoint media query that jsdom does not implement.
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({ matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() })),
    );
    await TestBed.configureTestingModule({
      imports: [HelpModal],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideMarkdown(),
        { provide: LOCALE_ID, useValue: 'ca' },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HelpModal);
    service = TestBed.inject(HelpModalService);
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTesting.verify();
    // An open overlay reads the media query again while it is destroyed.
    fixture.destroy();
    vi.unstubAllGlobals();
  });

  function press(key: string, init: KeyboardEventInit = {}) {
    const event = new KeyboardEvent('keydown', { key, cancelable: true, ...init });
    document.dispatchEvent(event);
    return event;
  }

  function open(topics: HelpTopic[] = TOPICS) {
    service.open();
    fixture.detectChanges();
    httpTesting.expectOne('help/ca/index.json').flush(topics);
    fixture.detectChanges();
  }

  /** Button of a group or a topic in the index, by its visible label. */
  function topicHeader(label: string): HTMLButtonElement {
    const button = Array.from(
      topicsNav().querySelectorAll('button') as NodeListOf<HTMLButtonElement>,
    ).find((element) => element.textContent?.trim() === label);
    if (!button) throw new Error(`Topic "${label}" is not in the index`);
    return button;
  }

  it('toggles with Ctrl + K and Cmd + K, and ignores a plain K', () => {
    const ctrl = press('k', { ctrlKey: true });
    expect(ctrl.defaultPrevented).toBe(true);
    expect(service.visible()).toBe(true);

    press('K', { metaKey: true });
    expect(service.visible()).toBe(false);

    const plain = press('k');
    expect(plain.defaultPrevented).toBe(false);
    expect(service.visible()).toBe(false);
  });

  it('lists the topics of the index and does not request it again when reopened', () => {
    open();
    expect(topicHeader("Com fer servir l'ajuda")).toBeTruthy();

    service.close();
    fixture.detectChanges();
    service.open();
    fixture.detectChanges();

    httpTesting.expectNone('help/ca/index.json');
    expect(topicHeader("Com fer servir l'ajuda")).toBeTruthy();
  });

  describe('index', () => {
    const GROUPED: HelpTopic[] = [
      {
        label: 'Primers passos',
        icon: 'pi pi-compass',
        items: [
          {
            label: "Com fer servir l'ajuda",
            icon: 'pi pi-question-circle',
            document: 'getting-started/using-help.md',
          },
        ],
      },
      {
        label: 'Aplicacions',
        items: [{ label: "Llista d'aplicacions", document: 'applications/list.md' }],
      },
    ];

    function groupList(group: HTMLButtonElement): HTMLElement {
      return document.getElementById(group.getAttribute('aria-controls')!) as HTMLElement;
    }

    it('starts collapsed without an active topic and lets each button show and hide its topics', () => {
      open(GROUPED);
      const group = topicHeader('Primers passos');

      expect(group.getAttribute('aria-expanded')).toBe('false');
      expect(groupList(group).hidden).toBe(true);
      expect(groupList(group).contains(topicHeader("Com fer servir l'ajuda"))).toBe(true);
      expect(groupList(group).parentElement).toBe(group.parentElement);
      expect(group.querySelectorAll('.pi:not([aria-hidden="true"])').length).toBe(0);

      group.click();
      fixture.detectChanges();
      expect(group.getAttribute('aria-expanded')).toBe('true');
      expect(groupList(group).hidden).toBe(false);

      group.click();
      fixture.detectChanges();
      expect(group.getAttribute('aria-expanded')).toBe('false');
    });

    it('marks only the open topic as the current one', async () => {
      open(GROUPED);
      topicHeader('Aplicacions').click();
      fixture.detectChanges();

      topicHeader("Llista d'aplicacions").click();
      fixture.detectChanges();
      httpTesting.expectOne('help/ca/applications/list.md').flush("### Llista d'aplicacions");
      await rendered();

      expect(topicHeader("Llista d'aplicacions").getAttribute('aria-current')).toBe('true');
      expect(topicHeader("Com fer servir l'ajuda").hasAttribute('aria-current')).toBe(false);
      expect(topicsNav().querySelectorAll('[aria-current]').length).toBe(1);
    });

    it('opens the collapsed group of a topic opened from elsewhere', async () => {
      open(GROUPED);
      topicHeader('Primers passos').click();
      fixture.detectChanges();
      service.close();
      fixture.detectChanges();

      const release = service.registerDialogTopic(HELP_TOPICS.applicationsList);
      service.open();
      fixture.detectChanges();
      httpTesting.expectOne('help/ca/applications/list.md').flush("### Llista d'aplicacions");
      await rendered();

      expect(topicHeader('Aplicacions').getAttribute('aria-expanded')).toBe('true');
      expect(topicHeader('Primers passos').getAttribute('aria-expanded')).toBe('false');
      expect(topicHeader("Llista d'aplicacions").getAttribute('aria-current')).toBe('true');
      release();
    });

    it('expands only the active ancestors after the index loads and resets them on reopening', async () => {
      const release = service.registerDialogTopic(HELP_TOPICS.applicationsList);
      open([
        GROUPED[0],
        { label: 'Gestió', items: [GROUPED[1], { label: 'Altres', items: TOPICS }] },
      ]);
      httpTesting.expectOne('help/ca/applications/list.md').flush("### Llista d'aplicacions");
      await rendered();

      expect(topicHeader('Gestió').getAttribute('aria-expanded')).toBe('true');
      expect(topicHeader('Aplicacions').getAttribute('aria-expanded')).toBe('true');
      expect(topicHeader('Primers passos').getAttribute('aria-expanded')).toBe('false');
      expect(topicHeader('Altres').getAttribute('aria-expanded')).toBe('false');

      topicHeader('Primers passos').click();
      topicHeader('Aplicacions').click();
      fixture.detectChanges();
      expect(topicHeader('Primers passos').getAttribute('aria-expanded')).toBe('true');
      expect(topicHeader('Aplicacions').getAttribute('aria-expanded')).toBe('false');
      service.close();
      fixture.detectChanges();
      service.open();
      await rendered();

      httpTesting.expectNone('help/ca/applications/list.md');
      expect(topicHeader('Gestió').getAttribute('aria-expanded')).toBe('true');
      expect(topicHeader('Aplicacions').getAttribute('aria-expanded')).toBe('true');
      expect(topicHeader('Primers passos').getAttribute('aria-expanded')).toBe('false');
      expect(topicHeader('Altres').getAttribute('aria-expanded')).toBe('false');
      release();
    });
  });

  async function rendered() {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  function documentCard(): HTMLElement {
    return fixture.nativeElement.querySelector('p-card') as HTMLElement;
  }

  function topicsNav(): HTMLElement {
    return fixture.nativeElement.querySelector('nav') as HTMLElement;
  }

  function retryButton(container: HTMLElement): HTMLButtonElement {
    const button = Array.from(container.querySelectorAll('button')).find((element) =>
      element.textContent?.includes('Reintentar'),
    );
    if (!button) throw new Error('There is no retry button');
    return button;
  }

  it('names the dialog by its title and labels its close button', () => {
    open();

    const dialog = fixture.nativeElement.querySelector('[role="dialog"]') as HTMLElement;
    const titleId = dialog.getAttribute('aria-labelledby');
    expect(titleId).toBeTruthy();
    expect(document.getElementById(titleId!)?.textContent?.trim()).toBe("Centre d'ajuda");
    expect(document.getElementById(titleId!)?.tagName).toBe('H2');
    expect(dialog.querySelector('button[aria-label="Tanca el centre d\'ajuda"]')).not.toBeNull();
  });

  it('shows the document of the selected topic and moves focus to its heading', async () => {
    open();

    topicHeader("Com fer servir l'ajuda").click();
    fixture.detectChanges();
    expect(documentCard().getAttribute('aria-busy')).toBe('true');
    expect(documentCard().textContent).toContain("Carregant el tema d'ajuda");
    httpTesting
      .expectOne('help/ca/getting-started/using-help.md')
      .flush("### Com fer servir l'ajuda\n\nPremeu **Ctrl + K**.");
    await rendered();

    const heading = fixture.nativeElement.querySelector('markdown h3') as HTMLElement | null;
    expect(heading?.textContent).toBe("Com fer servir l'ajuda");
    expect(fixture.nativeElement.querySelector('markdown strong')?.textContent).toBe('Ctrl + K');
    expect(documentCard().getAttribute('aria-busy')).toBe('false');
    expect(document.activeElement).toBe(heading);
    expect(heading?.tabIndex).toBe(-1);
  });

  describe('links', () => {
    async function showTopic(markdown: string) {
      open();
      topicHeader("Com fer servir l'ajuda").click();
      fixture.detectChanges();
      httpTesting.expectOne('help/ca/getting-started/using-help.md').flush(markdown);
      await rendered();
    }

    function link(text: string): HTMLAnchorElement {
      const found = Array.from(
        fixture.nativeElement.querySelectorAll('markdown a') as NodeListOf<HTMLAnchorElement>,
      ).find((element) => element.textContent?.startsWith(text));
      if (!found) throw new Error(`There is no link "${text}"`);
      return found;
    }

    /** Clicks a link and says whether the help handled it; the browser never follows it. */
    function click(element: HTMLElement): boolean {
      let handled = false;
      const stop = (event: Event) => {
        handled = event.defaultPrevented;
        event.preventDefault();
      };
      document.addEventListener('click', stop);
      element.click();
      document.removeEventListener('click', stop);
      return handled;
    }

    it('opens a linked topic inside the help and moves focus to its heading', async () => {
      await showTopic(
        "### Com fer servir l'ajuda\n\nVegeu la [Llista d'aplicacions](../applications/list.md).",
      );
      const listLink = link("Llista d'aplicacions");
      expect(listLink.href).toBe(new URL('help/ca/applications/list.md', document.baseURI).href);

      expect(click(listLink)).toBe(true);
      fixture.detectChanges();
      httpTesting.expectOne('help/ca/applications/list.md').flush("### Llista d'aplicacions");
      await rendered();

      const heading = fixture.nativeElement.querySelector('markdown h3') as HTMLElement;
      expect(heading.textContent).toBe("Llista d'aplicacions");
      expect(document.activeElement).toBe(heading);
    });

    it('moves to the heading an anchor names, in the same topic or in another one', async () => {
      await showTopic(
        [
          "### Com fer servir l'ajuda",
          '',
          "Vegeu [com s'hi accedeix](#com-shi-accedeix) i [els filtres](../applications/list.md#filtres).",
          '',
          "#### Com s'hi accedeix",
        ].join('\n'),
      );

      expect(click(link("com s'hi accedeix"))).toBe(true);
      expect(document.activeElement?.textContent).toBe("Com s'hi accedeix");

      expect(click(link('els filtres'))).toBe(true);
      fixture.detectChanges();
      httpTesting
        .expectOne('help/ca/applications/list.md')
        .flush("### Llista d'aplicacions\n\n#### Columnes\n\n#### Filtres");
      await rendered();
      expect(document.activeElement?.textContent).toBe('Filtres');
    });

    it('opens links to other sites in a new tab and says so', async () => {
      await showTopic(
        "### Com fer servir l'ajuda\n\nMés a [la web del Govern](https://www.caib.es).",
      );
      const external = link('la web del Govern');

      expect(external.target).toBe('_blank');
      expect(external.rel).toBe('noopener noreferrer');
      expect(external.querySelector('i.pi-external-link')?.getAttribute('aria-hidden')).toBe(
        'true',
      );
      expect(external.querySelector('.sr-only')?.textContent).toBe(
        " (s'obre en una pestanya nova)",
      );
      expect(click(external)).toBe(false);
    });
  });

  it('does not show the review notes left for the team', async () => {
    open();
    topicHeader("Com fer servir l'ajuda").click();
    fixture.detectChanges();
    httpTesting
      .expectOne('help/ca/getting-started/using-help.md')
      .flush("### Com fer servir l'ajuda\n\n1. Pas\n   <!-- REVISAR: nota interna -->\n2. Pas");
    await rendered();

    const content = fixture.nativeElement.querySelector('markdown') as HTMLElement;
    expect(content.textContent).not.toContain('REVISAR');
    expect(content.querySelectorAll('li').length).toBe(2);
  });

  it('does not move focus to the document when the dialog is reopened', async () => {
    open();
    topicHeader("Com fer servir l'ajuda").click();
    fixture.detectChanges();
    httpTesting
      .expectOne('help/ca/getting-started/using-help.md')
      .flush("### Com fer servir l'ajuda");
    await rendered();
    const outside = document.body.appendChild(document.createElement('button'));
    outside.focus();

    service.close();
    await rendered();
    service.open();
    await rendered();

    expect(document.activeElement?.closest('markdown')).toBeNull();
    outside.remove();
  });

  it('opens on the document of the open dialog, keeps focus out of it and names it in the description', async () => {
    const release = service.registerDialogTopic(HELP_TOPICS.applicationsList);
    service.open();
    fixture.detectChanges();
    httpTesting.expectOne('help/ca/applications/list.md').flush("### Llista d'aplicacions");
    httpTesting.expectOne('help/ca/index.json').flush(APPLICATIONS_LIST);
    await rendered();

    expect(fixture.nativeElement.querySelector('markdown h3')?.textContent).toBe(
      "Llista d'aplicacions",
    );
    expect(document.activeElement?.closest('markdown')).toBeNull();
    const dialog = fixture.nativeElement.querySelector('[role="dialog"]') as HTMLElement;
    const description = document.getElementById(dialog.getAttribute('aria-describedby')!);
    expect(description?.hidden).toBe(true);
    expect(description?.textContent?.trim()).toBe("Tema obert: Llista d'aplicacions");
    release();
  });

  it('does not load again the document on show when the help opens on it', async () => {
    const release = service.registerDialogTopic(HELP_TOPICS.applicationsList);
    service.open();
    fixture.detectChanges();
    httpTesting.expectOne('help/ca/applications/list.md').flush("### Llista d'aplicacions");
    httpTesting.expectOne('help/ca/index.json').flush(APPLICATIONS_LIST);
    await rendered();
    const load = vi.spyOn(TestBed.inject(HelpContentService), 'loadDocument');

    service.close();
    await rendered();
    service.open();
    await rendered();

    expect(load).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('markdown h3')?.textContent).toBe(
      "Llista d'aplicacions",
    );
    release();
  });

  it('announces a document that does not load and loads it again on retry', async () => {
    open();
    topicHeader("Com fer servir l'ajuda").click();
    fixture.detectChanges();
    httpTesting
      .expectOne('help/ca/getting-started/using-help.md')
      .flush('', { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();

    expect(documentCard().querySelector('[role="alert"]')?.textContent).toBe(
      "No s'ha pogut carregar el tema d'ajuda.",
    );

    retryButton(documentCard()).click();
    fixture.detectChanges();
    httpTesting
      .expectOne('help/ca/getting-started/using-help.md')
      .flush("### Com fer servir l'ajuda");
    await rendered();

    expect(documentCard().querySelector('[role="alert"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('markdown h3')?.textContent).toBe(
      "Com fer servir l'ajuda",
    );
  });

  it('shows only the results of the latest search', () => {
    open();
    const answers = new Map([
      ['ajuda', new Subject<SearchResult[]>()],
      ['cercador', new Subject<SearchResult[]>()],
    ]);
    vi.spyOn(TestBed.inject(HelpContentService), 'searchInAllDocs').mockImplementation((term) =>
      answers.get(term)!,
    );
    const autoComplete = fixture.debugElement.query(By.directive(AutoComplete))
      .componentInstance as AutoComplete;

    autoComplete.completeMethod.emit({ originalEvent: new Event('input'), query: 'ajuda' });
    autoComplete.completeMethod.emit({ originalEvent: new Event('input'), query: ' cercador ' });
    answers.get('ajuda')!.next([result('Antic', 'ajuda')]);
    answers.get('cercador')!.next([result('Actual', 'cercador')]);
    fixture.detectChanges();

    expect(autoComplete.suggestions).toEqual([result('Actual', 'cercador')]);
  });

  it('announces how many topics a search finds, and nothing about a selection while typing', () => {
    open();
    const answer = new Subject<SearchResult[]>();
    vi.spyOn(TestBed.inject(HelpContentService), 'searchInAllDocs').mockReturnValue(answer);
    const autoComplete = fixture.debugElement.query(By.directive(AutoComplete))
      .componentInstance as AutoComplete;
    const status = () =>
      Array.from(
        fixture.nativeElement.querySelectorAll('[role="status"]') as NodeListOf<HTMLElement>,
      ).map((element) => element.textContent?.trim());

    autoComplete.completeMethod.emit({ originalEvent: new Event('input'), query: 'temes' });
    answer.next([result('Primer', 'temes'), result('Segon', 'temes')]);
    fixture.detectChanges();
    expect(status()).toContain('2 temes trobats');
    expect(status()).not.toContain('Tema seleccionat');

    autoComplete.completeMethod.emit({ originalEvent: new Event('input'), query: 'cap' });
    answer.next([]);
    fixture.detectChanges();
    expect(status()).toContain("No s'han trobat resultats");

    autoComplete.onClear.emit();
    fixture.detectChanges();
    expect(status().filter(Boolean)).toEqual([]);
  });

  it('renders each result as its topic and a plain-text excerpt with the match marked', async () => {
    open();
    const answer = new Subject<SearchResult[]>();
    vi.spyOn(TestBed.inject(HelpContentService), 'searchInAllDocs').mockReturnValue(answer);
    const autoComplete = fixture.debugElement.query(By.directive(AutoComplete))
      .componentInstance as AutoComplete;

    autoComplete.search(new Event('input'), 'cercador', 'input');
    answer.next([result("Com fer servir l'ajuda", 'cercador', ['Primers passos'])]);
    fixture.detectChanges();
    await fixture.whenStable();
    // AutoComplete aligns its overlay in a timeout; let it run while matchMedia is stubbed.
    await new Promise((resolve) => setTimeout(resolve, 10));
    fixture.detectChanges();

    const option = document.body.querySelector('[role="option"]') as HTMLElement;
    const item = option.querySelector('[data-testid="help-search-result"]') as HTMLElement;
    expect(option.getAttribute('aria-label')).toBe("Com fer servir l'ajuda, Primers passos");
    expect(item.querySelector('mark')?.textContent).toBe('cercador');
    const [title, trail, excerpt] = Array.from(item.children).map((child) => child.textContent);
    expect(title).toBe("Com fer servir l'ajuda");
    expect(trail).toBe('Primers passos');
    expect(excerpt).toBe('…el cercador busca en tots els temes.');
    expect(item.querySelector('markdown, h1, h2, h3, h4, ul, ol')).toBeNull();
  });

  it('focuses the first result as soon as the suggestions show, so Enter opens it', async () => {
    open();
    const answer = new Subject<SearchResult[]>();
    vi.spyOn(TestBed.inject(HelpContentService), 'searchInAllDocs').mockReturnValue(answer);
    const autoComplete = fixture.debugElement.query(By.directive(AutoComplete))
      .componentInstance as AutoComplete;

    const input = fixture.nativeElement.querySelector('#help-modal-search') as HTMLInputElement;
    input.focus();
    autoComplete.search(new Event('input'), 'temes', 'input');
    answer.next([result('Primer', 'temes'), result('Segon', 'temes')]);
    fixture.detectChanges();
    await fixture.whenStable();
    await new Promise((resolve) => setTimeout(resolve, 10));
    fixture.detectChanges();

    const active = document.getElementById(input.getAttribute('aria-activedescendant')!);
    expect(active?.getAttribute('aria-label')).toBe('Primer');
  });

  it('moves focus to a topic chosen in the search once the suggestions have closed', async () => {
    open();
    const chosen = result("Com fer servir l'ajuda", 'cercador');
    chosen.document = 'getting-started/using-help.md';
    vi.spyOn(TestBed.inject(HelpContentService), 'searchInAllDocs').mockReturnValue(of([chosen]));
    const autoComplete = fixture.debugElement.query(By.directive(AutoComplete))
      .componentInstance as AutoComplete;
    autoComplete.search(new Event('input'), 'cercador', 'input');
    await rendered();
    await new Promise((resolve) => setTimeout(resolve, 10));

    const option = document.body.querySelector('[role="option"]') as HTMLElement;
    option.click();
    httpTesting
      .expectOne('help/ca/getting-started/using-help.md')
      .flush("### Com fer servir l'ajuda");
    await rendered();
    // AutoComplete focuses its input again in a timeout before it reports that it closed.
    await new Promise((resolve) => setTimeout(resolve, 10));

    const heading = fixture.nativeElement.querySelector('markdown h3') as HTMLElement;
    expect(document.activeElement).toBe(heading);
    expect(autoComplete.inputEL?.nativeElement.value).toBe('');
  });

  it('marks the topics as busy until the index arrives', () => {
    service.open();
    fixture.detectChanges();

    expect(topicsNav().getAttribute('aria-busy')).toBe('true');
    expect(topicsNav().textContent).toContain("Carregant els temes d'ajuda");

    httpTesting.expectOne('help/ca/index.json').flush(TOPICS);
    fixture.detectChanges();

    expect(topicsNav().getAttribute('aria-busy')).toBe('false');
    expect(topicHeader("Com fer servir l'ajuda")).toBeTruthy();
  });

  it('announces an index that does not load and loads it again on retry', () => {
    service.open();
    fixture.detectChanges();
    httpTesting.expectOne('help/ca/index.json').flush('', { status: 404, statusText: 'Not Found' });

    expect(() => fixture.detectChanges()).not.toThrow();
    expect(topicsNav().querySelector('[role="alert"]')?.textContent).toBe(
      "No s'han pogut carregar els temes d'ajuda.",
    );
    expect(topicsNav().querySelector('.help-topics')).toBeNull();

    retryButton(topicsNav()).click();
    fixture.detectChanges();
    httpTesting.expectOne('help/ca/index.json').flush(TOPICS);
    fixture.detectChanges();

    expect(topicsNav().querySelector('[role="alert"]')).toBeNull();
    expect(topicHeader("Com fer servir l'ajuda")).toBeTruthy();
  });

  it('says so when the index has no topics', () => {
    service.open();
    fixture.detectChanges();
    httpTesting.expectOne('help/ca/index.json').flush([]);
    fixture.detectChanges();

    expect(topicsNav().textContent?.trim()).toBe("Encara no hi ha temes d'ajuda.");
  });

  it('asks the service to return focus when the dialog hides', () => {
    open();
    const restoreFocus = vi.spyOn(service, 'restoreFocus');

    fixture.debugElement.query(By.directive(Dialog)).componentInstance.onHide.emit({});

    expect(restoreFocus).toHaveBeenCalledOnce();
  });
});
