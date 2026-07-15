import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  HostListener,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MarkdownModule } from 'ngx-markdown';
import { MenuItem } from 'primeng/api';
import { AutoComplete } from 'primeng/autocomplete';
import { Card } from 'primeng/card';
import { Dialog } from 'primeng/dialog';
import { PanelMenu } from 'primeng/panelmenu';
import { filter, switchMap } from 'rxjs';
import {
  HELP_MODAL_EMPTY_MESSAGE,
  HELP_MODAL_EMPTY_SELECTION,
  HELP_MODAL_SEARCH_ARIA_LABEL,
  HELP_MODAL_SEARCH_PLACEHOLDER,
  HELP_MODAL_TITLE,
} from './help-modal.i18n';
import { HelpModalService, SearchResult } from './help-modal.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AutoComplete, Card, Dialog, MarkdownModule, PanelMenu],
  selector: 'app-help-modal',
  templateUrl: './help-modal.html',
})
export default class HelpModal {
  private readonly _helpModalService = inject(HelpModalService);
  private readonly _destroyRef = inject(DestroyRef);

  @HostListener('document:keydown', ['$event'])
  handleKeydown(event: KeyboardEvent) {
    const modifierPressed = event.metaKey || event.ctrlKey;

    if (modifierPressed && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      this._helpModalService.toggle();
    }
  }

  protected readonly visible = this._helpModalService.visible;
  protected readonly title = HELP_MODAL_TITLE;
  protected readonly searchAriaLabel = HELP_MODAL_SEARCH_ARIA_LABEL;
  protected readonly searchPlaceholder = HELP_MODAL_SEARCH_PLACEHOLDER;
  protected readonly emptyMessage = HELP_MODAL_EMPTY_MESSAGE;
  protected readonly emptySelection = HELP_MODAL_EMPTY_SELECTION;
  protected readonly content = signal<string>('');
  protected readonly searchResults = signal<SearchResult[]>([]);
  protected readonly isSearching = signal<boolean>(false);
  private readonly _selectedValue = signal<string | null>(null);

  protected readonly groups = toSignal(
    toObservable(this.visible).pipe(
      filter((visible) => visible),
      switchMap(() => this._helpModalService.getGroups()),
    ),
    {
      initialValue: [] as MenuItem[],
    },
  );

  protected readonly selectedDoc = computed(() => {
    const selectedValue = this._selectedValue();
    if (!selectedValue) return null;

    const findInItems = (items: MenuItem[] | undefined): MenuItem | null => {
      if (!items) return null;
      for (const item of items) {
        if (item['value'] === selectedValue) return item;
        const fromChildren = findInItems(item.items);
        if (fromChildren) return fromChildren;
      }
      return null;
    };

    return findInItems(this.groups());
  });

  protected readonly helpMenu = computed(() => {
    const mapItems = (items: MenuItem[] | undefined): MenuItem[] | undefined =>
      items?.map((item) => ({
        ...item,
        items: mapItems(item.items),
        command: item['value'] ? () => this._onSelect(item['value'] as string) : item.command,
      }));

    return mapItems(this.groups()) ?? [];
  });

  protected onSelectResult(result: SearchResult) {
    this._onSelect(result.value);
  }

  protected onSearchComplete(event: { query: string }) {
    const term = event.query.trim();

    if (!term) {
      this.searchResults.set([]);
      this.isSearching.set(false);
      return;
    }

    this.isSearching.set(true);

    this._helpModalService
      .searchInAllDocs(term)
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe((results) => {
        this.searchResults.set(results);
        this.isSearching.set(false);
      });
  }

  private _onSelect(value: string | null) {
    this._selectedValue.set(value);
    this.content.set('');

    if (!value) return;

    this._helpModalService
      .loadContent(value)
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe((text) => {
        this.content.set(text);
      });
  }
}
