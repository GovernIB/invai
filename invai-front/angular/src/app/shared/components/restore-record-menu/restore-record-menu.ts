import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';

@Component({
  selector: 'app-restore-record-menu',
  standalone: true,
  imports: [Button, Menu],
  templateUrl: './restore-record-menu.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RestoreRecordMenu {
  readonly restore = output<void>();

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly actionsAriaLabel = $localize`Obrir les accions del registre inactiu`;
  protected readonly actions: MenuItem[] = [
    {
      label: $localize`Restaura`,
      icon: PrimeIcons.REFRESH,
      command: () => this.restore.emit(),
    },
  ];
}
