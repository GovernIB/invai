import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ServerErrorDialogService } from '@core/services/server-error-dialog.service';
import { Button } from 'primeng/button';

import { ServerErrorDialog } from './server-error-dialog';

describe('ServerErrorDialog', () => {
  let fixture: ComponentFixture<ServerErrorDialog>;
  let dialogService: ServerErrorDialogService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ServerErrorDialog] }).compileComponents();

    fixture = TestBed.createComponent(ServerErrorDialog);
    dialogService = TestBed.inject(ServerErrorDialogService);
  });

  it('renders the server title and message and closes from its action', () => {
    dialogService.open({
      error: 'Error de validació',
      message: 'El prefix ja està assignat a una altra aplicació.',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Error de validació');
    expect(fixture.nativeElement.textContent).toContain(
      'El prefix ja està assignat a una altra aplicació.',
    );
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();

    const closeButton = fixture.debugElement
      .queryAll(By.directive(Button))
      .find((button) => button.componentInstance.label === 'Tanca');
    closeButton?.componentInstance.onClick.emit();

    expect(dialogService.visible()).toBe(false);
  });
});
