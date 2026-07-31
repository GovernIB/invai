import { DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export function observeUnsupportedSoftDeleteStatus(
  control: FormControl<SoftDeleteStatus | null>,
  destroyRef: DestroyRef,
  onUnsupported: () => void,
): void {
  control.valueChanges.pipe(takeUntilDestroyed(destroyRef)).subscribe((status) => {
    if (status === SoftDeleteStatus.ACTIVE) return;

    onUnsupported();
    control.setValue(SoftDeleteStatus.ACTIVE, { emitEvent: false });
  });
}
