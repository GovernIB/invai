import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Button } from 'primeng/button';

const FONT_CONFIG = {
  defaultFontSize: 0.9,
  fontSizeIncrement: 0.1,
  minFontSize: 0.7,
  maxFontSize: 1.5,
  fontSizeLocalStorageKey: 'fontSize',
  legacyDefaultFontSize: 1,
};

const FONT_SIZE_SWITCHER_LABELS = {
  TITLE: $localize`Font`,
  DECREASE: $localize`Redueix la mida de la font`,
  RESET: $localize`Restableix la mida de la font`,
  INCREASE: $localize`Augmenta la mida de la font`,
};

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Button],
  selector: 'app-font-size-switcher',
  template: `
    <h3 class="text-primary font-semibold mb-4">{{ labels.TITLE }}</h3>

    <section>
      <p-button
        styleClass="text-sm"
        [outlined]="true"
        [rounded]="true"
        [text]="true"
        [ariaLabel]="labels.DECREASE"
        (onClick)="decreaseFont()"
        icon="pi pi-minus"
        iconPos="right"
        label="A"
        severity="secondary"
        size="small"
      />

      <p-button
        [outlined]="true"
        [rounded]="true"
        [text]="true"
        [ariaLabel]="labels.RESET"
        (onClick)="resetFont()"
        icon="pi pi-refresh"
        iconPos="right"
        label="A"
        severity="secondary"
        size="small"
      />

      <p-button
        styleClass="text-lg"
        [outlined]="true"
        [rounded]="true"
        [text]="true"
        [ariaLabel]="labels.INCREASE"
        (onClick)="increaseFont()"
        icon="pi pi-plus"
        iconPos="right"
        label="A"
        severity="secondary"
        size="small"
      />
    </section>
  `,
})
export class FontSizeSwitcher {
  private readonly _appConfig = FONT_CONFIG;

  protected readonly labels = FONT_SIZE_SWITCHER_LABELS;
  protected fontSize = this._appConfig.defaultFontSize;

  constructor() {
    this._initializeFontSize();
  }

  protected increaseFont() {
    this._updateFont(this.fontSize + this._appConfig.fontSizeIncrement);
  }

  protected decreaseFont() {
    this._updateFont(this.fontSize - this._appConfig.fontSizeIncrement);
  }

  protected resetFont() {
    this._updateFont(this._appConfig.defaultFontSize);
  }

  private _initializeFontSize() {
    const storedFontSize = localStorage.getItem(this._appConfig.fontSizeLocalStorageKey);
    if (storedFontSize) {
      const parsedFontSize = parseFloat(storedFontSize);
      this.fontSize =
        Number.isFinite(parsedFontSize) && parsedFontSize !== this._appConfig.legacyDefaultFontSize
          ? parsedFontSize
          : this._appConfig.defaultFontSize;
    }
    this._updateFont(this.fontSize);
  }

  private _updateFont(newSize: number) {
    const clampedSize = Math.min(
      Math.max(newSize, this._appConfig.minFontSize),
      this._appConfig.maxFontSize,
    );
    this.fontSize = clampedSize;
    document.documentElement.style.setProperty('--font-size', `${clampedSize}rem`);
    localStorage.setItem(this._appConfig.fontSizeLocalStorageKey, clampedSize.toString());
  }
}
