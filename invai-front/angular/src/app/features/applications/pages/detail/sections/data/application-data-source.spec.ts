import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import {
  ApplicationDataFormGroup,
  createApplicationDataForm,
} from '../../../../forms/application-data-form.factory';
import { ApplicationDataSourcePanel } from './application-data-source';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const DETECTED_URL = 'https://intranet.caib.es/0001api/externa/swagger.json';

describe('ApplicationDataSourcePanel', () => {
  let fixture: ComponentFixture<ApplicationDataSourcePanel>;
  let form: ApplicationDataFormGroup;

  const root = () => fixture.nativeElement as HTMLElement;
  const urlInput = () => root().querySelector<HTMLInputElement>('#application-data-open-data-url')!;

  async function create(isEditing = false): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ApplicationDataSourcePanel],
    }).compileComponents();

    form = createApplicationDataForm(new FormBuilder());
    form.controls.openDataUrl.setValue(DETECTED_URL);
    fixture = TestBed.createComponent(ApplicationDataSourcePanel);
    fixture.componentRef.setInput('useUrlControl', form.controls.useOpenDataUrl);
    fixture.componentRef.setInput('urlControl', form.controls.openDataUrl);
    fixture.componentRef.setInput('endpoints', []);
    fixture.componentRef.setInput('idPrefix', 'application-data-open-data');
    fixture.componentRef.setInput('title', 'Open Data');
    fixture.componentRef.setInput('isEditing', isEditing);
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('exposes every value in view mode through labelled static or readonly content', async () => {
    await create();

    const flagValue = root().querySelector('p.invai-form-static-value')!;
    expect(root().querySelector('p-toggleswitch')).toBeNull();
    expect(flagValue.textContent?.trim()).toBe('No');
    expect(root().querySelector(`#${flagValue.getAttribute('aria-labelledby')}`)?.textContent?.trim()).toBe(
      'Utilitza una URL pròpia',
    );
    expect(root().querySelector('label[for="application-data-open-data-url"]')?.textContent?.trim()).toBe(
      'URL de consulta',
    );
    expect(urlInput().readOnly).toBe(true);
    expect(urlInput().disabled).toBe(false);
    expect(urlInput().value).toBe(DETECTED_URL);
    expect(root().querySelector('h4')?.textContent?.trim()).toBe('Endpoints GET publicats');
  });

  it('keeps the detected URL readonly in edit mode until the custom URL is enabled', async () => {
    await create(true);

    const toggle = root().querySelector<HTMLInputElement>('#application-data-open-data-use-url')!;
    expect(toggle.getAttribute('aria-labelledby')).toBe('application-data-open-data-use-url-label');
    expect(urlInput().readOnly).toBe(true);
    expect(urlInput().getAttribute('aria-describedby')).toBe('application-data-open-data-url-help');
    expect(root().querySelector('#application-data-open-data-url-help')).not.toBeNull();

    form.controls.useOpenDataUrl.setValue(true);
    fixture.detectChanges();

    expect(urlInput().readOnly).toBe(false);
    expect(urlInput().hasAttribute('aria-describedby')).toBe(false);
  });

  it('shows an associated error once the owner marks an invalid URL as touched', async () => {
    await create(true);
    form.controls.useOpenDataUrl.setValue(true);
    form.controls.openDataUrl.setValue('');
    fixture.detectChanges();
    expect(root().querySelector('[role="alert"]')).toBeNull();

    form.markAllAsTouched();
    fixture.detectChanges();

    const error = root().querySelector('#application-data-open-data-url-error');
    expect(error?.getAttribute('role')).toBe('alert');
    expect(error?.textContent?.trim()).toBe(
      'Indica la URL de consulta quan utilitzes una URL pròpia.',
    );
    expect(urlInput().getAttribute('aria-invalid')).toBe('true');
    expect(urlInput().getAttribute('aria-describedby')).toBe('application-data-open-data-url-error');
  });

  it('explains that endpoints are resolved after the first save when the anchor is missing', async () => {
    await create();
    fixture.componentRef.setInput('showEndpoints', false);
    fixture.detectChanges();

    expect(root().querySelector('app-application-data-endpoints-table')).toBeNull();
    expect(root().textContent).toContain(
      'Els endpoints publicats es consultaran quan es desin les dades de la pestanya.',
    );
  });

  it('distinguishes an unavailable source from a document without endpoints', async () => {
    await create();
    expect(root().querySelector('app-application-data-endpoints-table')).not.toBeNull();
    expect(root().querySelector('#application-data-open-data-unavailable')).toBeNull();

    fixture.componentRef.setInput('endpoints', null);
    fixture.componentRef.setInput('unavailable', true);
    fixture.detectChanges();

    const notice = root().querySelector('#application-data-open-data-unavailable');
    expect(root().querySelector('app-application-data-endpoints-table')).toBeNull();
    expect(notice?.getAttribute('role')).toBe('status');
    expect(notice?.textContent?.trim()).toBe(
      "No s'ha pogut consultar el catàleg d'aquesta font en aquest moment. Torna-ho a provar.",
    );
    expect(root().textContent).not.toContain("L'aplicació no publica cap endpoint GET.");
  });

  it('asks the owner to retry an unavailable source only outside edit mode', async () => {
    await create();
    const retry = vi.fn();
    fixture.componentInstance.retry.subscribe(retry);
    fixture.componentRef.setInput('unavailable', true);
    fixture.detectChanges();

    const button = () =>
      root().querySelector<HTMLButtonElement>('.application-data-source__unavailable button')!;
    expect(button().textContent?.trim()).toBe('Torna a carregar les dades');
    button().click();
    expect(retry).toHaveBeenCalledOnce();

    fixture.componentRef.setInput('isEditing', true);
    fixture.detectChanges();
    expect(button().disabled).toBe(true);
  });
});
