import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationDataEndpoint } from '../../../../applications.model';
import { ApplicationDataEndpointsTable } from './application-data-endpoints-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const ENDPOINTS: ApplicationDataEndpoint[] = [
  {
    path: '/api/aplicacions',
    method: 'GET',
    operation: {
      operationId: 'list',
      summary: 'Llista aplicacions',
      description: 'Retorna les aplicacions publicades.',
      parameters: [
        {
          name: 'page',
          in: 'query',
          required: false,
          description: null,
          type: 'integer',
          format: 'int32',
          defaultValue: 0,
          enumValues: null,
        },
        {
          name: 'estat',
          in: 'query',
          required: true,
          description: 'Estat de l’aplicació',
          type: null,
          format: null,
          defaultValue: null,
          enumValues: ['ACTIVE', 'INACTIVE'],
        },
      ],
    },
  },
  {
    path: '/api/aplicacions/{id}',
    method: 'GET',
    operation: { operationId: null, summary: null, description: null, parameters: [] },
  },
];

describe('ApplicationDataEndpointsTable', () => {
  let fixture: ComponentFixture<ApplicationDataEndpointsTable>;

  const root = () => fixture.nativeElement as HTMLElement;
  const bodyRows = () => [
    ...root().querySelector('table')!.querySelectorAll<HTMLTableRowElement>(':scope > tbody > tr'),
  ];
  const toggle = () => root().querySelector<HTMLButtonElement>('.application-data-endpoints__toggle')!;

  async function create(endpoints: ApplicationDataEndpoint[] = ENDPOINTS): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ApplicationDataEndpointsTable],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDataEndpointsTable);
    fixture.componentRef.setInput('endpoints', endpoints);
    fixture.componentRef.setInput('idPrefix', 'application-data-open-data');
    fixture.componentRef.setInput('label', 'Open Data: Endpoints GET publicats');
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('renders a named, striped read-only table without the PrimeNG loading mask', async () => {
    await create();

    const table = root().querySelector('table');
    expect(table?.getAttribute('aria-label')).toBe('Open Data: Endpoints GET publicats');
    expect(root().querySelector('.p-datatable-striped')).not.toBeNull();
    expect(root().querySelector('.p-datatable-mask')).toBeNull();
    expect(root().querySelector('.invai-table-loading-container')?.getAttribute('aria-busy')).toBe(
      'false',
    );
    expect([...root().querySelectorAll('thead th')].map((th) => th.textContent?.trim())).toEqual([
      "Paràmetres de l'endpoint",
      'Ruta',
      'Resum',
      'Descripció',
      'Paràmetres',
    ]);
  });

  it('shows missing operation texts as empty values and endpoints without parameters without a toggle', async () => {
    await create();

    const cells = [...bodyRows()[1].querySelectorAll('td')].map((td) => td.textContent?.trim());
    expect(cells).toEqual(['', '/api/aplicacions/{id}', '-', '-', 'Sense paràmetres']);
    expect(root().querySelectorAll('.application-data-endpoints__toggle')).toHaveLength(1);
  });

  it('expands and collapses the parameters with a native, stateful toggle', async () => {
    await create();

    expect(toggle().tagName).toBe('BUTTON');
    expect(toggle().getAttribute('aria-label')).toBe('Paràmetres de /api/aplicacions');
    expect(toggle().getAttribute('aria-expanded')).toBe('false');
    expect(toggle().hasAttribute('aria-controls')).toBe(false);

    toggle().click();
    fixture.detectChanges();

    const controls = toggle().getAttribute('aria-controls');
    expect(toggle().getAttribute('aria-expanded')).toBe('true');
    expect(controls).toBe('application-data-open-data-endpoint-0-parameters');
    const expansion = root().querySelector(`#${controls}`)!;
    const nested = expansion.querySelector('table')!;
    expect(nested.getAttribute('aria-label')).toBe('Paràmetres de /api/aplicacions');
    expect(expansion.querySelector('.p-datatable-striped')).not.toBeNull();
    const parameterRows = [...nested.querySelectorAll(':scope > tbody > tr')].map((row) =>
      [...row.querySelectorAll('td')].map((td) => td.textContent?.trim()),
    );
    expect(parameterRows).toEqual([
      ['page', 'query', 'No', 'integer', 'int32', '0', '-', '-'],
      ['estat', 'query', 'Sí', '-', '-', '-', 'ACTIVE, INACTIVE', 'Estat de l’aplicació'],
    ]);

    toggle().click();
    fixture.detectChanges();

    expect(toggle().getAttribute('aria-expanded')).toBe('false');
    expect(root().querySelector(`#${controls}`)).toBeNull();
  });

  it('shows the empty message across every column', async () => {
    await create([]);

    const cell = root().querySelector('tbody td');
    expect(cell?.textContent?.trim()).toBe("L'aplicació no publica cap endpoint GET.");
    expect(cell?.getAttribute('colspan')).toBe('5');
  });

  it('keeps the rows and marks the table busy while refreshing', async () => {
    await create();
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();

    expect(root().querySelector('.invai-table-loading-container')?.getAttribute('aria-busy')).toBe(
      'true',
    );
    expect(root().querySelector('.invai-table-loading-shield')?.getAttribute('aria-hidden')).toBe(
      'true',
    );
    expect(root().querySelector('.invai-table-refresh-indicator')).not.toBeNull();
    expect(bodyRows()).toHaveLength(2);
  });
});
