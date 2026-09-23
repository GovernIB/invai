import { ChangeDetectionStrategy, Component, computed, ElementRef, Injector, afterNextRender, inject, input, output, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { AdministrativeUnitSearchAction, AdministrativeUnitSearchState } from './administrative-unit-search.state';

@Component({
  selector: 'app-administrative-unit-field',
  imports: [ReactiveFormsModule, Select, InputText, Button],
  template: `
    <div class="flex flex-col gap-2 min-w-0">
      <label class="text-xs text-muted-color" [id]="inputId() + '-label'" [for]="inputId()">{{ label() }}</label>
      @if (readOnly()) {
        <p class="invai-form-static-value" [attr.aria-labelledby]="inputId() + '-label'">{{ state().selectedLabel() }}</p>
      } @else {
        <p-select #select styleClass="w-full" [inputId]="inputId()" [formControl]="control()"
          [ariaLabelledBy]="inputId() + '-label'" [ariaFilterLabel]="searchLabel"
          [options]="displayOptions()" optionLabel="label" optionValue="code" [filter]="true"
          [showClear]="allowClear()" [resetFilterOnHide]="true" [loading]="state().loading()"
          [pt]="{ label: { 'aria-invalid': invalid(), 'aria-describedby': inputId() + '-status' } }"
          (onShow)="open()" (onHide)="action.emit({kind: 'close'})">
          <ng-template #selectedItem>{{ state().selectedLabel() }}</ng-template>
          <ng-template #item let-unit><span class="whitespace-normal break-words">{{ unit.name }} ({{ unit.code }})</span></ng-template>
          <ng-template #filter>
            <input #searchInput pInputText type="search" role="combobox" aria-expanded="true" aria-autocomplete="list"
              [attr.aria-controls]="select.id + '_list'" [attr.aria-activedescendant]="select.focusedOptionId" class="w-full" [value]="state().query()"
              [attr.aria-label]="searchLabel" (input)="search($event)"
              (keydown)="select.onFilterKeyDown($event)" />
          </ng-template>
          <ng-template #footer>
            <div class="p-3 text-sm" aria-live="polite">
              @if (state().failed()) {
                <p class="m-0" role="alert">{{ errorLabel }}</p>
                <p-button type="button" [label]="retryLabel" [text]="true" (onClick)="action.emit({kind: 'retry'})" />
              } @else if (state().loading()) {
                <p class="m-0">{{ loadingLabel }}</p>
              } @else {
                <p class="m-0">{{ resultMessage(state().total()) }}</p>
              }
            </div>
          </ng-template>
        </p-select>
      }
      <div [id]="inputId() + '-status'" aria-live="polite">
        @if (invalid()) { <p class="text-sm text-red-600 m-0">{{ requiredError }}</p> }
        @if (state().loading()) { <p class="text-sm m-0">{{ loadingLabel }}</p> }
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdministrativeUnitField {
  control = input.required<FormControl<string | null>>();
  state = input.required<AdministrativeUnitSearchState>();
  inputId = input.required<string>();
  label = input.required<string>();
  readOnly = input(false);
  allowClear = input(false);
  action = output<AdministrativeUnitSearchAction>();
  protected readonly displayOptions = computed(() => this.state().options().map(unit => ({ ...unit, label: `${unit.name} (${unit.code})` })));
  private readonly injector = inject(Injector);
  private readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('searchInput');
  protected open(): void {
    this.action.emit({ kind: 'open' });
    afterNextRender(() => this.searchInput()?.nativeElement.focus(), { injector: this.injector });
  }
  protected readonly searchLabel = $localize`:@@dir3Search:Cerca unitats administratives per nom`;
  protected readonly requiredError = $localize`Aquest camp és obligatori.`;
  protected readonly loadingLabel = $localize`:@@dir3Loading:Carregant unitats administratives.`;
  protected readonly errorLabel = $localize`:@@dir3Error:No s'han pogut carregar les unitats administratives.`;
  protected readonly retryLabel = $localize`Torna-ho a provar`;
  protected invalid(): boolean { return this.control().invalid && this.control().touched; }
  protected search(event: Event): void { this.action.emit({ kind: 'search', query: (event.target as HTMLInputElement).value }); }
  protected resultMessage(total: number): string {
    return total > 20
      ? $localize`:@@dir3Refine:Hi ha ${total} resultats. Es mostren els primers 20; concreta la cerca.`
      : $localize`:@@dir3Total:Resultats: ${total}`;
  }
}
