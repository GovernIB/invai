import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of } from 'rxjs';

import { Application, ApplicationInput, ApplicationOutput } from '../../applications.model';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationDetailState } from './application-detail-state';

const APPLICATION_OUTPUT: ApplicationOutput = {
  id: 1,
  code: '0001',
  prefix: 'CVF',
  name: 'Invai',
  category: { id: 1, name: 'DRASSANA' },
  systemType: { id: 2, name: 'Instrumental' },
  field: { id: 3, name: 'Departamental' },
  admUnit: { id: 5, code: 'DGEDOT', name: 'Direcció General' },
  csCommission: { id: 4, name: 'Equip directiu' },
  description: 'Aplicació interna',
  status: { id: 1, name: 'Activa' },
  expirationDate: null,
  createdAt: '2026-01-01T10:00:00',
  createdBy: null,
  updatedAt: null,
  updatedBy: null,
  loadUser: null,
  loadDate: null,
};

describe('ApplicationDetailState', () => {
  let state: ApplicationDetailState;
  let getById: ReturnType<typeof vi.fn>;
  let update: ReturnType<typeof vi.fn>;
  let toApplication: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getById = vi.fn(() => of(APPLICATION_OUTPUT));
    update = vi.fn((id: number, payload: ApplicationInput) =>
      of({
        ...APPLICATION_OUTPUT,
        name: payload.name,
        prefix: payload.prefix,
        description: payload.description,
      }),
    );
    toApplication = vi.fn((response: ApplicationOutput): Application => ({
      id: String(response.id),
      code: response.code ?? '',
      prefix: response.prefix ?? '',
      name: response.name ?? '',
      category: response.category?.name ?? '',
      informationSystem: response.systemType?.name ?? '',
      scope: response.field?.name ?? '',
      commission: response.csCommission?.name ?? '',
      administrativeUnit: response.admUnit?.name ?? '',
      status: response.status?.name ?? '',
      description: response.description ?? '',
      creationDate: response.createdAt ?? '',
      modificationDate: response.updatedAt ?? '',
      withdrawalDate: response.expirationDate ?? '',
      categoryId: response.category?.id,
      informationSystemId: response.systemType?.id,
      scopeId: response.field?.id,
      commissionId: response.csCommission?.id,
      administrativeUnitId: response.admUnit?.id,
      statusId: response.status?.id,
    }));

    TestBed.configureTestingModule({
      providers: [
        ApplicationDetailState,
        { provide: ApplicationsService, useValue: { getById, update, toApplication } },
      ],
    });
    state = TestBed.inject(ApplicationDetailState);
  });

  it('loads an application into a disabled form', () => {
    state.systemsDatabasesForm.controls.observations.setValue('<p>Temporal</p>');
    state.load('1');

    expect(getById).toHaveBeenCalledWith(1);
    expect(state.application()?.id).toBe('1');
    expect(state.form.getRawValue()).toEqual(
      expect.objectContaining({
        application: 'Invai',
        category: 1,
        informationSystem: 2,
        scope: 3,
        commission: 4,
        administrativeUnit: 5,
      }),
    );
    expect(state.form.disabled).toBe(true);
    expect(state.isEditing()).toBe(false);
    expect(state.systemsDatabasesForm.getRawValue()).toEqual({ observations: '' });
  });

  it('enables editable fields but keeps system dates disabled', () => {
    state.load('1');
    state.startEditing();

    expect(state.form.controls.application.enabled).toBe(true);
    expect(state.form.controls.creationDate.disabled).toBe(true);
    expect(state.form.controls.modificationDate.disabled).toBe(true);
    expect(state.form.controls.withdrawalDate.disabled).toBe(true);
    expect(state.isEditing()).toBe(true);
  });

  it('restores the saved snapshot when editing is cancelled', () => {
    state.load('1');
    state.startEditing();
    state.form.controls.application.setValue('Changed');

    state.cancelEditing();

    expect(state.form.getRawValue().application).toBe('Invai');
    expect(state.form.disabled).toBe(true);
    expect(state.isEditing()).toBe(false);
  });

  it('persists valid editable values through the applications service', async () => {
    state.load('1');
    state.startEditing();
    state.form.patchValue({
      application: 'Invai updated',
      prefix: 'INV',
      description: 'Updated description',
    });

    await expect(firstValueFrom(state.save())).resolves.toBe(true);

    expect(update).toHaveBeenCalledWith(1, {
      name: 'Invai updated',
      prefix: 'INV',
      code: '0001',
      categoryId: 1,
      systemTypeId: 2,
      fieldId: 3,
      admUnitId: 5,
      commissionId: 4,
      description: 'Updated description',
      statusId: 1,
    });
    expect(state.application()?.name).toBe('Invai updated');
    expect(state.form.disabled).toBe(true);
    expect(state.isEditing()).toBe(false);
  });

  it('persists the selected commission id when it changes', async () => {
    state.load('1');
    state.startEditing();
    state.form.controls.commission.setValue(9);

    await expect(firstValueFrom(state.save())).resolves.toBe(true);

    expect(update).toHaveBeenCalledWith(
      1,
      expect.objectContaining({
        commissionId: 9,
      }),
    );
  });

  it('does not call update when the form is invalid', async () => {
    state.load('1');
    state.startEditing();
    state.form.controls.application.setValue('');

    await expect(firstValueFrom(state.save())).resolves.toBe(false);

    expect(update).not.toHaveBeenCalled();
    expect(state.form.controls.application.touched).toBe(true);
  });
});
