import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import { ApplicationDataUrlState } from '../../../../applications.model';
import { createApplicationDataForm } from '../../../../forms/application-data-form.factory';
import { ApplicationDataUrlPanel } from './application-data-url-panel';

@Component({
  imports: [ApplicationDataUrlPanel],
  template: `
    <app-application-data-url-panel
      idPrefix="test-source"
      title="Dades obertes"
      pendingMessage="Pendent"
      [canEdit]="canEdit()"
      [isEditing]="isEditing()"
      [manualControl]="form.controls.manualOpenDataUrl"
      [state]="state()"
      [urlControl]="form.controls.openDataUrl"
    />
  `,
})
class HostComponent {
  readonly form = createApplicationDataForm(new FormBuilder());
  readonly state = signal<ApplicationDataUrlState | null>({
    url: 'https://intranet.caib.es/appapi/externa',
    check: 'valid',
  });
  readonly canEdit = signal(true);
  readonly isEditing = signal(false);
}

describe('ApplicationDataUrlPanel', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  const root = () => fixture.nativeElement as HTMLElement;
  const warning = () => root().querySelector<HTMLElement>('#test-source-warning');
  const value = () => root().querySelector<HTMLElement>('dd')!;
  const status = () => root().querySelector<HTMLElement>('[role="status"]');

  async function render(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [HostComponent] }).compileComponents();
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    await render();
  });

  it('names its section and labels the URL in use', () => {
    const section = root().querySelector('section')!;
    const title = root().querySelector('h3')!;

    expect(section.getAttribute('aria-labelledby')).toBe(title.id);
    expect(title.id).toBe('test-source-title');
    expect(title.textContent?.trim()).toBe('Dades obertes');
    expect(root().querySelector('dt')?.textContent?.trim()).toBe('URL');
    expect(value().querySelector('a')?.getAttribute('href')).toBe(
      'https://intranet.caib.es/appapi/externa',
    );
    expect(value().querySelector('i')?.getAttribute('aria-hidden')).toBe('true');
    expect(warning()).toBeNull();
    expect(status()?.textContent?.trim()).toBe('Aquesta URL respon.');
  });

  it('shows the URL at once and announces whether it answers when the check ends', async () => {
    host.state.set({ url: 'https://intranet.caib.es/appapi/externa', check: 'checking' });
    await render();

    expect(value().querySelector('a')).not.toBeNull();
    expect(status()?.getAttribute('aria-busy')).toBe('true');
    expect(status()?.textContent?.trim()).toBe('Comprovant si aquesta URL respon…');
    expect(status()?.querySelector('i')?.getAttribute('aria-hidden')).toBe('true');

    host.state.set({ url: 'https://intranet.caib.es/appapi/externa', check: 'invalid' });
    await render();

    // The same live region carries the outcome, so it is announced once.
    expect(status()?.getAttribute('aria-busy')).toBeNull();
    expect(status()?.contains(warning())).toBe(true);
  });

  it('warns about a URL that does not answer and suggests editing', async () => {
    host.state.set({ url: 'https://intranet.caib.es/appapi/externa', check: 'invalid' });
    await render();

    expect(value().querySelector('a')).not.toBeNull();
    expect(warning()?.textContent?.trim()).toBe(
      'Aquesta URL no respon. Edita les dades per indicar-ne una manualment.',
    );
    expect(warning()?.querySelector('i')?.getAttribute('aria-hidden')).toBe('true');
  });

  it('reports a URL that could not be checked, keeping it visible', async () => {
    host.state.set({ url: 'https://intranet.caib.es/appapi/externa', check: 'failed' });
    host.canEdit.set(false);
    await render();

    expect(value().querySelector('a')).not.toBeNull();
    expect(warning()?.textContent?.trim()).toBe("No s'ha pogut comprovar la URL d'aquesta font.");
  });

  it('reports a URL the back-end could not resolve', async () => {
    host.state.set({ url: null, check: 'failed' });
    host.canEdit.set(false);
    await render();

    expect(value().querySelector('a')).toBeNull();
    expect(value().textContent?.trim()).toBe('No disponible');
    expect(warning()?.textContent?.trim()).toBe("No s'ha pogut comprovar la URL d'aquesta font.");
  });

  it('shows the pending message while the URL is not known yet', async () => {
    host.state.set(null);
    await render();

    expect(value().textContent?.trim()).toBe('Pendent');
    expect(warning()).toBeNull();
    expect(status()).toBeNull();
  });

  it('keeps the checkbox out of the view mode', () => {
    expect(root().querySelector('p-checkbox')).toBeNull();
    expect(root().querySelector('input')).toBeNull();
  });

  it('asks for the URL only while the checkbox is checked and connects its errors', async () => {
    host.state.set({ url: 'https://intranet.caib.es/appapi/externa', check: 'invalid' });
    host.isEditing.set(true);
    await render();

    const checkbox = root().querySelector<HTMLInputElement>('#test-source-manual')!;
    expect(checkbox.type).toBe('checkbox');
    expect(root().querySelector('label[for="test-source-manual"]')?.textContent?.trim()).toBe(
      'Indicar la URL manualment',
    );
    expect(checkbox.getAttribute('aria-describedby')).toBe('test-source-warning');
    expect(warning()?.textContent?.trim()).toBe(
      'Aquesta URL no respon. Marca «Indicar la URL manualment» per indicar-ne una altra.',
    );
    expect(root().querySelector('#test-source-url')).toBeNull();

    checkbox.click();
    await render();

    const input = root().querySelector<HTMLInputElement>('#test-source-url')!;
    expect(host.form.controls.manualOpenDataUrl.value).toBe(true);
    expect(input.type).toBe('url');
    expect(input.getAttribute('maxlength')).toBe('500');
    expect(input.getAttribute('aria-invalid')).toBeNull();
    expect(root().querySelector('#test-source-url-help')?.textContent?.trim()).toBe(
      'Ha de començar per http:// o https://. En desar es comprovarà si respon.',
    );

    host.form.controls.openDataUrl.markAsTouched();
    await render();

    expect(input.getAttribute('aria-invalid')).toBe('true');
    expect(input.getAttribute('aria-describedby')).toBe('test-source-url-error test-source-url-help');
    expect(root().querySelector('#test-source-url-error')?.textContent?.trim()).toBe(
      'Indica la nova URL o desmarca «Indicar la URL manualment».',
    );

    input.value = 'HTTPS://reuse.example/api';
    input.dispatchEvent(new Event('input'));
    await render();

    expect(root().querySelector('#test-source-url-error')?.textContent?.trim()).toBe(
      'Introdueix una URL HTTP o HTTPS vàlida.',
    );
  });

  it('does not describe the checkbox when the URL answers', async () => {
    host.isEditing.set(true);
    await render();

    expect(
      root().querySelector('#test-source-manual')?.getAttribute('aria-describedby'),
    ).toBeNull();
  });
});
