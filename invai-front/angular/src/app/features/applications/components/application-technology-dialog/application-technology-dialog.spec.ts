import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';
import { Select } from 'primeng/select';

import {
  ApplicationTechnologyFormGroup,
  createApplicationTechnologyForm,
} from '../../forms/application-development-form.factory';
import { ApplicationTechnologyDialog } from './application-technology-dialog';

describe('ApplicationTechnologyDialog', () => {
  let fixture: ComponentFixture<ApplicationTechnologyDialog>;
  let form: ApplicationTechnologyFormGroup;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationTechnologyDialog],
    }).compileComponents();

    form = createApplicationTechnologyForm(new FormBuilder());
    form.setValue({
      layerId: 1,
      technologyId: 2,
      version: '21',
      architecture: 'Monolítica',
    });
    fixture = TestBed.createComponent(ApplicationTechnologyDialog);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('mode', 'view');
    fixture.componentRef.setInput('technologyOptions', [
      {
        id: 2,
        label: 'Angular',
        layerId: 1,
        layerLabel: 'Frontend',
      },
    ]);
    fixture.detectChanges();
  });

  it('shows every value as readonly with the disabled background in view mode', () => {
    const technology = fixture.debugElement.query(By.directive(Select));
    const technologyValue = inputById(
      'application-technology-dialog-technology',
    );
    const layer = inputById('application-technology-dialog-layer');
    const version = inputById('application-technology-dialog-version');
    const architecture = inputById(
      'application-technology-dialog-architecture',
    );

    expect(technology).toBeNull();
    expect(form.controls.technologyId.enabled).toBe(true);
    expect(technologyValue.value).toBe('Angular');
    expect(layer.value).toBe('Frontend');
    expect(
      [technologyValue, layer, version, architecture].every(
        (input) => input.readOnly,
      ),
    ).toBe(true);
    expect(
      [technologyValue, layer, version, architecture].every((input) =>
        input.classList.contains(
          'application-technology-dialog__readonly-control',
        ),
      ),
    ).toBe(true);
    expect(
      [technologyValue, layer, version, architecture].every(
        (input) => !input.disabled,
      ),
    ).toBe(true);
  });

  it('uses edit as its primary view action', () => {
    const dialog = fixture.debugElement.query(By.directive(CrudEntityDialog))
      .componentInstance as CrudEntityDialog;

    expect(dialog.viewPrimaryAction()).toBe('edit');
  });

  it.each(['create', 'edit'] as const)(
    'makes only technology, version and architecture editable in %s mode',
    (mode) => {
      fixture.componentRef.setInput('mode', mode);
      fixture.detectChanges();

      expect(fixture.debugElement.query(By.directive(Select))).not.toBeNull();
      expect(form.controls.technologyId.enabled).toBe(true);
      expect(inputById('application-technology-dialog-version').readOnly).toBe(
        false,
      );
      expect(
        inputById('application-technology-dialog-architecture').readOnly,
      ).toBe(false);
      const layer = inputById('application-technology-dialog-layer');
      expect(layer.readOnly).toBe(true);
      expect(layer.classList).toContain(
        'application-technology-dialog__readonly-control',
      );
    },
  );

  function inputById(id: string): HTMLInputElement {
    return fixture.nativeElement.querySelector(`#${id}`) as HTMLInputElement;
  }
});
