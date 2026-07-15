import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { ApplicationCreateFormGroup } from '../../forms/application-form.factory';
import { ApplicationOptionsService } from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationCreateForm } from './application-create-form';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

describe('ApplicationCreateForm', () => {
  let fixture: ComponentFixture<ApplicationCreateForm>;
  let formComponent: {
    form: ApplicationCreateFormGroup;
    submit: () => void;
  };
  let messageService: MessageService;
  let create: ReturnType<typeof vi.fn>;
  const route = {};
  const router = { navigate: vi.fn() };

  beforeEach(async () => {
    router.navigate.mockReset();
    create = vi.fn(() => of({ id: 1 }));

    await TestBed.configureTestingModule({
      imports: [ApplicationCreateForm],
      providers: [
        MessageService,
        { provide: ActivatedRoute, useValue: route },
        {
          provide: ApplicationOptionsService,
          useValue: {
            getOptions: () =>
              of({
                categories: [{ label: 'DRASSANA', value: 1 }],
                informationSystems: [{ label: 'Instrumental', value: 2 }],
                scopes: [{ label: 'Departamental', value: 3 }],
                commissions: [{ label: 'A04026930', value: 4 }],
                administrativeUnits: [{ label: 'DGEDOT', value: 5 }],
              }),
          },
        },
        { provide: ApplicationsService, useValue: { create } },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationCreateForm);
    formComponent = fixture.componentInstance as unknown as typeof formComponent;
    messageService = TestBed.inject(MessageService);
    fixture.detectChanges();
  });

  it('marks invalid controls and exposes accessible errors without navigating', () => {
    const addSpy = vi.spyOn(messageService, 'add');

    formComponent.submit();
    fixture.detectChanges();

    const applicationInput = fixture.nativeElement.querySelector(
      '#create-application-application',
    ) as HTMLInputElement;

    expect(formComponent.form.controls.application.touched).toBe(true);
    expect(applicationInput.getAttribute('aria-invalid')).toBe('true');
    expect(applicationInput.getAttribute('aria-describedby')).toBe(
      'create-application-application-error',
    );
    expect(create).not.toHaveBeenCalled();
    expect(addSpy).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('submits a valid form to the server and navigates back to the list', () => {
    const addSpy = vi.spyOn(messageService, 'add');

    formComponent.form.setValue({
      application: 'Invai',
      category: 1,
      informationSystem: 2,
      scope: 3,
      commission: 4,
      prefix: 'CVF',
      code: '0001',
      administrativeUnit: 5,
      description: '',
    });

    formComponent.submit();

    expect(create).toHaveBeenCalledWith({
      name: 'Invai',
      prefix: 'CVF',
      code: '0001',
      categoryId: 1,
      systemTypeId: 2,
      fieldId: 3,
      admUnitId: 5,
      commissionId: 4,
      description: null,
      statusId: 1,
    });
    expect(addSpy).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'success' }),
    );
    expect(router.navigate).toHaveBeenCalledWith(['..'], { relativeTo: route });
  });

  it('does not submit an application code shorter than four characters', () => {
    formComponent.form.setValue({
      application: 'Invai',
      category: 1,
      informationSystem: 2,
      scope: 3,
      commission: 4,
      prefix: 'CVF',
      code: '123',
      administrativeUnit: 5,
      description: '',
    });

    formComponent.submit();
    fixture.detectChanges();

    const codeInput = fixture.nativeElement.querySelector(
      '#create-application-code',
    ) as HTMLInputElement;
    const codeError = fixture.nativeElement.querySelector('#create-application-code-error');

    expect(formComponent.form.controls.code.touched).toBe(true);
    expect(codeInput.getAttribute('aria-invalid')).toBe('true');
    expect(codeInput.getAttribute('aria-describedby')).toBe('create-application-code-error');
    expect(codeError?.textContent.trim()).toBe('El codi ha de tenir com a mínim 4 caràcters');
    expect(create).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('shows an error and stays on the form when creating fails', () => {
    const addSpy = vi.spyOn(messageService, 'add');
    create.mockReturnValueOnce(throwError(() => new Error('Request failed')));

    formComponent.form.setValue({
      application: 'Invai',
      category: 1,
      informationSystem: 2,
      scope: 3,
      commission: 4,
      prefix: 'CVF',
      code: '0001',
      administrativeUnit: 5,
      description: 'Desc',
    });

    formComponent.submit();

    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'ha pogut afegir l'aplicació.",
    });
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
