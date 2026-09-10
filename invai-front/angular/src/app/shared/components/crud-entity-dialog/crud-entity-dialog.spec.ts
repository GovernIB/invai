import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';

import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from './crud-entity-dialog';

const ARIA_LABELS: CrudEntityDialogAriaLabels = {
  accept: 'Accept entity view',
  add: 'Add entity',
  cancel: 'Cancel entity changes',
  close: 'Close entity form',
  deactivate: 'Deactivate entity',
  edit: 'Edit entity',
  restore: 'Restore entity',
  save: 'Save entity',
};

describe('CrudEntityDialog', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [CrudEntityDialog] }).compileComponents();
  });

  it('distinguishes save from add without changing their submission output', () => {
    const fixture = createFixture('create');
    const submit = vi.fn();
    fixture.componentInstance.submitForm.subscribe(submit);
    expect(buttonByLabel(fixture, 'Afegir').icon).toBe('pi pi-plus');
    buttonByLabel(fixture, 'Afegir').onClick.emit(new MouseEvent('click'));
    fixture.componentRef.setInput('mode', 'edit');
    fixture.detectChanges();
    expect(buttonByLabel(fixture, 'Desar').icon).toBe('pi pi-save');
    buttonByLabel(fixture, 'Desar').onClick.emit(new MouseEvent('click'));
    expect(submit).toHaveBeenCalledTimes(2);
  });

  it.each([
    { mode: 'view' as const, canRestore: false, labels: ['Editar', 'Acceptar'] },
    { mode: 'view' as const, canRestore: true, labels: ['Restaurar', 'Acceptar'] },
    {
      mode: 'edit' as const,
      canRestore: false,
      labels: ['Donar de baixa', 'Cancel·lar', 'Desar'],
    },
    { mode: 'create' as const, canRestore: false, labels: ['Cancel·lar', 'Afegir'] },
  ])('renders the standard $mode action matrix', ({ mode, canRestore, labels }) => {
    const fixture = createFixture(mode, canRestore);

    expect(buttonLabels(fixture)).toEqual(labels);

    if (mode === 'view') {
      expect(buttonByLabel(fixture, 'Acceptar').icon).toBe('pi pi-check');
    }
  });

  it('emits edit, cancel, save, deactivate and restore intentions', () => {
    const fixture = createFixture('view');
    const component = fixture.componentInstance;
    const edit = vi.fn();
    const restore = vi.fn();
    const cancelEdit = vi.fn();
    const save = vi.fn();
    const deactivate = vi.fn();
    component.edit.subscribe(edit);
    component.restore.subscribe(restore);
    component.cancelEdit.subscribe(cancelEdit);
    component.submitForm.subscribe(save);
    component.deactivate.subscribe(deactivate);

    buttonByLabel(fixture, 'Editar').onClick.emit(new MouseEvent('click'));
    expect(edit).toHaveBeenCalledOnce();

    fixture.componentRef.setInput('canRestore', true);
    fixture.detectChanges();
    buttonByLabel(fixture, 'Restaurar').onClick.emit(new MouseEvent('click'));
    expect(restore).toHaveBeenCalledOnce();

    fixture.componentRef.setInput('mode', 'edit');
    fixture.componentRef.setInput('canRestore', false);
    fixture.detectChanges();
    buttonByLabel(fixture, 'Cancel·lar').onClick.emit(new MouseEvent('click'));
    buttonByLabel(fixture, 'Desar').onClick.emit(new MouseEvent('click'));
    buttonByLabel(fixture, 'Donar de baixa').onClick.emit(new MouseEvent('click'));

    expect(cancelEdit).toHaveBeenCalledOnce();
    expect(save).toHaveBeenCalledOnce();
    expect(deactivate).toHaveBeenCalledOnce();
  });

  it('keeps view available while hiding edit when the parent context is read-only', () => {
    const fixture = createFixture('view');
    const edit = vi.fn();
    fixture.componentInstance.edit.subscribe(edit);
    fixture.componentRef.setInput('canEdit', false);
    fixture.detectChanges();

    expect(buttonLabels(fixture)).toEqual(['Acceptar']);
    expect(edit).not.toHaveBeenCalled();
    expect(buttonByLabel(fixture, 'Acceptar').disabled).toBe(false);
  });

  it('uses edit as the only view action when configured as primary', () => {
    const fixture = createFixture('view');
    const edit = vi.fn();
    fixture.componentInstance.edit.subscribe(edit);
    fixture.componentRef.setInput('viewPrimaryAction', 'edit');
    fixture.detectChanges();

    expect(buttonLabels(fixture)).toEqual(['Editar']);

    const editButton = buttonByLabel(fixture, 'Editar');
    expect(editButton.icon).toBe('pi pi-pencil');
    editButton.onClick.emit(new MouseEvent('click'));
    expect(edit).toHaveBeenCalledOnce();
  });

  it('falls back to accept when primary edit is not available', () => {
    const fixture = createFixture('view');
    fixture.componentRef.setInput('viewPrimaryAction', 'edit');
    fixture.componentRef.setInput('canEdit', false);
    fixture.detectChanges();

    expect(buttonLabels(fixture)).toEqual(['Acceptar']);
  });

  it('keeps save available while preventing deactivation outside its allowed context', () => {
    const fixture = createFixture('edit');
    const deactivate = vi.fn();
    fixture.componentInstance.deactivate.subscribe(deactivate);
    fixture.componentRef.setInput('canDeactivate', false);
    fixture.detectChanges();

    const deactivateButton = buttonByLabel(fixture, 'Donar de baixa');
    expect(deactivateButton.disabled).toBe(true);
    expect(buttonByLabel(fixture, 'Desar').disabled).toBe(false);

    deactivateButton.onClick.emit(new MouseEvent('click'));
    expect(deactivate).not.toHaveBeenCalled();
  });

  it('can hide the deactivation action for edit-only resources', () => {
    const fixture = createFixture('edit');
    fixture.componentRef.setInput('showDeactivate', false);
    fixture.detectChanges();

    expect(buttonLabels(fixture)).toEqual(['Cancel·lar', 'Desar']);
  });

  it('guards X, Escape and mask requests while edits are dirty', () => {
    const fixture = createFixture('edit', false, () => true);
    const component = fixture.componentInstance;
    const closed = vi.fn();
    const saved = vi.fn();
    component.closed.subscribe(closed);
    component.submitForm.subscribe(saved);

    harness(component).requestClose();
    expect(harness(component).isUnsavedChangesDialogVisible()).toBe(true);
    expect(closed).not.toHaveBeenCalled();

    harness(component).closeUnsavedChangesDialog();
    harness(component).dialogPassThrough.root?.['onkeydown']?.(
      new KeyboardEvent('keydown', { key: 'Escape' }),
    );
    expect(harness(component).isUnsavedChangesDialogVisible()).toBe(true);

    harness(component).closeUnsavedChangesDialog();
    const mask = document.createElement('div');
    harness(component).dialogPassThrough.mask?.['onclick']?.({
      target: mask,
      currentTarget: mask,
    } as unknown as MouseEvent);
    expect(harness(component).isUnsavedChangesDialogVisible()).toBe(true);

    harness(component).saveAndClose();
    expect(saved).toHaveBeenCalledOnce();
    expect(closed).not.toHaveBeenCalled();

    harness(component).requestClose();
    harness(component).discardAndClose();
    expect(closed).toHaveBeenCalledOnce();
  });

  it('closes directly when the current edit has no changes', () => {
    const fixture = createFixture('edit', false, () => false);
    const closed = vi.fn();
    fixture.componentInstance.closed.subscribe(closed);

    harness(fixture.componentInstance).requestClose();

    expect(closed).toHaveBeenCalledOnce();
    expect(harness(fixture.componentInstance).isUnsavedChangesDialogVisible()).toBe(false);
    const dialog = fixture.debugElement.query(By.directive(Dialog)).componentInstance as Dialog;
    expect(dialog.closable).toBe(false);
    expect(dialog.closeOnEscape).toBe(false);
    expect(dialog.dismissableMask).toBe(false);
    expect(dialog.focusOnShow).toBe(true);
  });

  it('restores focus to the connected element that opened the dialog', async () => {
    const trigger = document.createElement('button');
    document.body.appendChild(trigger);
    trigger.focus();
    const fixture = createFixture('view');

    fixture.destroy();
    await Promise.resolve();

    expect(document.activeElement).toBe(trigger);
    trigger.remove();
  });

  it('does not focus an opener that is no longer connected', async () => {
    const trigger = document.createElement('button');
    document.body.appendChild(trigger);
    trigger.focus();
    const fixture = createFixture('view');
    trigger.remove();

    fixture.destroy();
    await Promise.resolve();

    expect(document.activeElement).not.toBe(trigger);
  });
});

function createFixture(
  mode: CrudEntityDialogMode,
  canRestore = false,
  hasUnsavedChanges: () => boolean = () => false,
): ComponentFixture<CrudEntityDialog> {
  const fixture = TestBed.createComponent(CrudEntityDialog);
  fixture.componentRef.setInput('visible', true);
  fixture.componentRef.setInput('title', 'Entity');
  fixture.componentRef.setInput('mode', mode);
  fixture.componentRef.setInput('ariaLabels', ARIA_LABELS);
  fixture.componentRef.setInput('canRestore', canRestore);
  fixture.componentRef.setInput('hasUnsavedChanges', hasUnsavedChanges);
  fixture.detectChanges();
  return fixture;
}

function buttonLabels(fixture: ComponentFixture<CrudEntityDialog>): string[] {
  return fixture.debugElement
    .queryAll(By.directive(Button))
    .map(({ componentInstance }) => (componentInstance as Button).label)
    .filter((label): label is string => Boolean(label));
}

function buttonByLabel(fixture: ComponentFixture<CrudEntityDialog>, label: string): Button {
  return fixture.debugElement
    .queryAll(By.directive(Button))
    .map(({ componentInstance }) => componentInstance as Button)
    .find((button) => button.label === label)!;
}

interface CrudEntityDialogHarness {
  requestClose(): void;
  closeUnsavedChangesDialog(): void;
  discardAndClose(): void;
  saveAndClose(): void;
  isUnsavedChangesDialogVisible(): boolean;
  dialogPassThrough: {
    root?: Record<string, (event: KeyboardEvent) => void>;
    mask?: Record<string, (event: MouseEvent) => void>;
  };
}

function harness(component: CrudEntityDialog): CrudEntityDialogHarness {
  return component as unknown as CrudEntityDialogHarness;
}
