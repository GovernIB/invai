import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Dialog } from 'primeng/dialog';

import { ConfirmationDialogComponent } from './confirmation-dialog.component';

describe('ConfirmationDialogComponent', () => {
  let fixture: ComponentFixture<ConfirmationDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfirmationDialogComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmationDialogComponent);
    fixture.componentRef.setInput('title', 'Confirmació');
    fixture.componentRef.setInput('message', 'Vols continuar?');
    fixture.componentRef.setInput('visible', true);
    fixture.detectChanges();
  });

  it('keeps focus management enabled on the native dialog', () => {
    const dialog = fixture.debugElement.query(By.directive(Dialog)).componentInstance as Dialog;

    expect(dialog.focusOnShow).toBe(true);
  });

  it('uses the backward-compatible width by default and accepts a wider override', () => {
    const dialog = fixture.debugElement.query(By.directive(Dialog)).componentInstance as Dialog;

    expect(dialog.style).toEqual({ width: '24rem', maxWidth: 'calc(100vw - 2rem)' });

    fixture.componentRef.setInput('width', '32rem');
    fixture.detectChanges();

    expect(dialog.style).toEqual({ width: '32rem', maxWidth: 'calc(100vw - 2rem)' });
  });

  it('can remain open after confirm for an owner-controlled asynchronous request', () => {
    const confirm = vi.fn();
    fixture.componentRef.setInput('closeOnConfirm', false);
    fixture.componentInstance.confirm.subscribe(confirm);

    harness().onConfirm();

    expect(confirm).toHaveBeenCalledOnce();
    expect(fixture.componentInstance.visible()).toBe(true);
  });

  it('blocks cancel and confirm while pending', () => {
    const confirm = vi.fn();
    const cancel = vi.fn();
    const auxiliary = vi.fn();
    fixture.componentRef.setInput('auxiliaryLabel', 'Transfereix');
    fixture.componentRef.setInput('pending', true);
    fixture.componentInstance.confirm.subscribe(confirm);
    fixture.componentInstance.cancel.subscribe(cancel);
    fixture.componentInstance.auxiliary.subscribe(auxiliary);

    harness().onConfirm();
    harness().onCancel();
    harness().onAuxiliary();

    expect(confirm).not.toHaveBeenCalled();
    expect(cancel).not.toHaveBeenCalled();
    expect(auxiliary).not.toHaveBeenCalled();
    expect(fixture.componentInstance.visible()).toBe(true);
  });

  it('exposes an optional auxiliary action without triggering cancellation', () => {
    const auxiliary = vi.fn();
    const cancel = vi.fn();
    fixture.componentRef.setInput('auxiliaryLabel', 'Transfereix');
    fixture.componentRef.setInput('auxiliaryAriaLabel', 'Transfereix els rols de la persona');
    fixture.componentInstance.auxiliary.subscribe(auxiliary);
    fixture.componentInstance.cancel.subscribe(cancel);
    fixture.detectChanges();

    harness().onAuxiliary();

    expect(auxiliary).toHaveBeenCalledOnce();
    expect(cancel).not.toHaveBeenCalled();
    expect(fixture.componentInstance.visible()).toBe(false);
  });

  function harness(): { onConfirm(): void; onCancel(): void; onAuxiliary(): void } {
    return fixture.componentInstance as unknown as {
      onConfirm(): void;
      onCancel(): void;
      onAuxiliary(): void;
    };
  }
});
