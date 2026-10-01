import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import { ApplicationIntegrationConnectionOutput } from '../../../../applications.model';
import {
  ApplicationIntegrationConnectionFormGroup,
  createApplicationIntegrationConnectionForm,
} from '../../../../forms/application-integration-connection-form.factory';
import { ApplicationIntegrationConnectionDialog } from './application-integration-connection-dialog';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const ROW: ApplicationIntegrationConnectionOutput = {
  id: 1,
  appIntegrationId: 13,
  application: null,
  externalSystem: { id: 2, name: 'Soffid', company: null, deletedAt: null },
  technology: { id: 4, name: 'Spring Boot', layer: { id: 1, name: 'Backend', deletedAt: null }, deletedAt: null },
  username: 'u00004',
  requiredRoles: [{ id: 26, name: 'INV_ADMIN', description: null, system: null }],
  grantedRoles: null,
  rolesMismatch: null,
  deletedAt: null,
};
const EMPTY_SEARCH = { options: [], loading: false, searched: false, error: false, total: 0 };

describe('ApplicationIntegrationConnectionDialog', () => {
  let fixture: ComponentFixture<ApplicationIntegrationConnectionDialog>;
  let form: ApplicationIntegrationConnectionFormGroup;

  const body = () => document.body;
  const byId = <T extends HTMLElement>(id: string) => document.getElementById(id) as T | null;

  async function create(mode: 'create' | 'view' | 'edit', selected: ApplicationIntegrationConnectionOutput | null = null) {
    await TestBed.configureTestingModule({
      imports: [ApplicationIntegrationConnectionDialog],
    }).compileComponents();

    form = createApplicationIntegrationConnectionForm(new FormBuilder());
    if (selected) {
      form.reset({
        systemKind: 'external',
        applicationId: null,
        externalSystemId: 2,
        technologyId: 4,
        user: null,
        requiredRoles: [{ roleId: 26, label: 'INV_ADMIN' }],
      });
    }
    fixture = TestBed.createComponent(ApplicationIntegrationConnectionDialog);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('mode', mode);
    fixture.componentRef.setInput('selected', selected);
    fixture.componentRef.setInput('canEdit', true);
    fixture.componentRef.setInput('technologyOptions', [{ id: 4, label: 'Spring Boot' }]);
    fixture.componentRef.setInput('externalSystemOptions', [{ id: 2, label: 'Soffid' }]);
    fixture.componentRef.setInput('applicationSearch', EMPTY_SEARCH);
    fixture.componentRef.setInput('userSearch', EMPTY_SEARCH);
    fixture.componentRef.setInput('roleSearch', EMPTY_SEARCH);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  afterEach(() => fixture?.destroy());

  it('renders the persisted connection as readonly floating fields in view mode', async () => {
    await create('view', ROW);

    const field = (name: string) => {
      const input = byId<HTMLInputElement>(`application-integration-dialog-${name}-view`);
      const label = body().querySelector(`label[for="application-integration-dialog-${name}-view"]`);
      expect(input?.readOnly).toBe(true);
      expect(input?.classList).toContain('invai-form-readonly-control');
      expect(label?.closest('p-floatlabel')).toBe(input?.closest('p-floatlabel') ?? null);
      return { label: label?.textContent?.trim(), value: input?.value };
    };
    expect(field('system')).toEqual({ label: 'Tipus de sistema', value: 'Sistema extern' });
    expect(field('system-name')).toEqual({ label: 'Sistema extern', value: 'Soffid' });
    expect(field('technology')).toEqual({ label: 'Tecnologia', value: 'Spring Boot' });
    expect(field('user')).toEqual({ label: "Usuari d'integració", value: 'u00004' });
    expect(field('roles-match')).toEqual({
      label: 'Coincidència de rols',
      value: "No s'ha pogut comprovar",
    });
    expect(body().querySelector('app-application-integration-connection-dialog p-select')).toBeNull();
    expect(body().querySelector('p-radiobutton')).toBeNull();
  });

  it('renders the wrapping role lists as labelled static values in view mode', async () => {
    await create('view', ROW);

    const value = (name: string) => {
      const labelId = `application-integration-dialog-${name}-view-label`;
      const staticValue = body().querySelector(`[aria-labelledby="${labelId}"]`);
      expect(staticValue?.closest('.invai-dialog-static-field')?.querySelector(`#${labelId}`)).not.toBeNull();
      return staticValue?.textContent?.trim();
    };
    expect(value('required-roles')).toBe('INV_ADMIN');
    expect(value('granted-roles')).toBe('No disponible');
  });

  it('groups the system kind radios under a legend and swaps the matching selector', async () => {
    await create('create');

    const legend = byId('application-integration-dialog-system-kind-legend');
    expect(legend?.tagName).toBe('LEGEND');
    expect(body().querySelector('label[for="application-integration-dialog-kind-application"]')?.textContent?.trim()).toBe(
      "Aplicació de l'inventari",
    );
    expect(byId('application-integration-dialog-application')).not.toBeNull();
    expect(byId('application-integration-dialog-external-system')).toBeNull();

    form.controls.systemKind.setValue('external');
    fixture.detectChanges();

    expect(byId('application-integration-dialog-application')).toBeNull();
    expect(byId('application-integration-dialog-external-system')?.getAttribute('role')).toBe('combobox');
  });

  it('connects visible errors to the focusable controls after a failed submission', async () => {
    await create('create');
    form.markAllAsTouched();
    fixture.detectChanges();

    const application = byId('application-integration-dialog-application')!;
    expect(application.getAttribute('aria-invalid')).toBe('true');
    expect(application.getAttribute('aria-describedby')?.split(' ')).toContain(
      'application-integration-dialog-application-error',
    );
    expect(byId('application-integration-dialog-application-error')?.textContent?.trim()).toBe('Camp obligatori');
    expect(byId('application-integration-dialog-technology')?.getAttribute('aria-describedby')).toBe(
      'application-integration-dialog-technology-error',
    );

    const roles = byId<HTMLInputElement>('application-integration-dialog-roles')!;
    expect(roles.getAttribute('role')).toBe('combobox');
    expect(roles.getAttribute('aria-describedby')?.split(' ')).toContain('application-integration-dialog-roles-error');
    expect(byId('application-integration-dialog-roles-error')?.textContent?.trim()).toBe(
      'Selecciona almenys un rol requerit.',
    );
    expect(body().querySelector('label[for="application-integration-dialog-soffid-person"]')?.textContent?.trim()).toBe(
      "Usuari d'integració",
    );
  });

  it('requests the first remote page when a search panel opens and debounces the filter', async () => {
    vi.useFakeTimers();
    try {
      await create('create');
      const applicationQueries: string[] = [];
      const roleQueries: string[] = [];
      fixture.componentInstance.applicationSearchRequested.subscribe((query) => applicationQueries.push(query));
      fixture.componentInstance.roleSearchRequested.subscribe((query) => roleQueries.push(query));
      const dialog = fixture.componentInstance as unknown as {
        onApplicationPanelOpen: () => void;
        onApplicationFilter: (event: { filter: string }) => void;
        onRolePanelOpen: () => void;
        onRoleFilter: (event: { filter: string }) => void;
      };

      dialog.onApplicationPanelOpen();
      dialog.onApplicationFilter({ filter: ' po' });
      dialog.onApplicationFilter({ filter: ' por ' });
      dialog.onRolePanelOpen();
      dialog.onRoleFilter({ filter: 'INV' });
      expect(applicationQueries).toEqual(['']);
      expect(roleQueries).toEqual(['']);

      vi.advanceTimersByTime(300);
      expect(applicationQueries).toEqual(['', 'por']);
      expect(roleQueries).toEqual(['', 'INV']);
    } finally {
      vi.useRealTimers();
    }
  });

  it('guards only dirty drafts and never submits from view mode', async () => {
    await create('view', ROW);
    const submitted = vi.fn();
    fixture.componentInstance.submitForm.subscribe(submitted);
    const dialog = fixture.componentInstance as unknown as {
      hasUnsavedChanges: () => boolean;
      onSubmit: () => void;
    };

    dialog.onSubmit();
    expect(submitted).not.toHaveBeenCalled();
    form.markAsDirty();
    expect(dialog.hasUnsavedChanges()).toBe(false);

    fixture.componentRef.setInput('mode', 'edit');
    fixture.detectChanges();
    expect(dialog.hasUnsavedChanges()).toBe(true);
    dialog.onSubmit();
    expect(submitted).toHaveBeenCalledOnce();
  });
});
