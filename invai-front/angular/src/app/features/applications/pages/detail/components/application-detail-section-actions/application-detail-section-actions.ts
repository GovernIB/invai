import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';

@Component({
  selector: 'app-application-detail-section-actions',
  standalone: true,
  imports: [Button],
  templateUrl: './application-detail-section-actions.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDetailSectionActions {
  sectionLabel = input.required<string>();
  isEditing = input(false);
  isSaving = input(false);
  disabled = input(false);

  edit = output<void>();
  cancel = output<void>();
  save = output<void>();

  protected readonly editLabel = $localize`Editar`;
  protected readonly cancelLabel = $localize`Cancel·lar`;
  protected readonly saveLabel = $localize`Desar`;
  protected readonly editAriaLabel = computed(
    () => $localize`Editar ${this.sectionLabel()}:sectionLabel:`,
  );
  protected readonly cancelAriaLabel = computed(
    () => $localize`Cancel·lar els canvis de ${this.sectionLabel()}:sectionLabel:`,
  );
  protected readonly saveAriaLabel = computed(
    () => $localize`Desar els canvis de ${this.sectionLabel()}:sectionLabel:`,
  );
  protected readonly icons = PrimeIcons;
}
