import { Component, input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { EnvironmentsList } from '@features/environments/pages/list/environments-list';
import { AccordionPanel } from 'primeng/accordion';
import { BehaviorSubject } from 'rxjs';

import { SYSTEMS_PANELS } from '../../systems.constants';
import { DatabasesList } from '../databases-list/databases-list';
import { DatabaseVendorsList } from '../database-vendors-list/database-vendors-list';
import { PhysicalServersList } from '../physical-servers-list/physical-servers-list';
import { ServersList } from '../servers-list/servers-list';
import { SystemsShell } from './systems-shell';

@Component({ selector: 'app-databases-list', standalone: true, template: '' })
class DatabasesListStub {}

@Component({ selector: 'app-servers-list', standalone: true, template: '' })
class ServersListStub {}

@Component({ selector: 'app-environments-list', standalone: true, template: '' })
class EnvironmentsListStub {}

@Component({ selector: 'app-database-vendors-list', standalone: true, template: '' })
class DatabaseVendorsListStub {}

@Component({ selector: 'app-physical-servers-list', standalone: true, template: '' })
class PhysicalServersListStub {
  serverTypeCode = input.required<string>();
}

describe('SystemsShell', () => {
  let fixture: ComponentFixture<SystemsShell>;
  let fragment: BehaviorSubject<string | null>;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    fragment = new BehaviorSubject<string | null>(null);
    navigate = vi.fn(() => Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [SystemsShell],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { fragment: null },
            fragment,
          },
        },
        { provide: Router, useValue: { navigate } },
      ],
    })
      .overrideComponent(SystemsShell, {
        remove: {
          imports: [
            DatabasesList,
            DatabaseVendorsList,
            EnvironmentsList,
            PhysicalServersList,
            ServersList,
          ],
        },
        add: {
          imports: [
            DatabasesListStub,
            DatabaseVendorsListStub,
            EnvironmentsListStub,
            PhysicalServersListStub,
            ServersListStub,
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(SystemsShell);
    fixture.detectChanges();
  });

  it('renders the six system panels in order with their descriptions', () => {
    const headers = fixture.nativeElement.querySelectorAll(
      '.maintenance-panel-header',
    ) as NodeListOf<HTMLElement>;

    expect(
      Array.from(headers).map((header) => ({
        title: header.querySelector('.maintenance-panel-title')?.textContent,
        description: header.querySelector('.maintenance-panel-description')?.textContent,
      })),
    ).toEqual(SYSTEMS_PANELS.map(({ title, description }) => ({ title, description })));
    expect(SYSTEMS_PANELS.map(({ id }) => id)).toEqual([
      'servers',
      'databases',
      'physical-servers',
      'database-servers',
      'database-vendors',
      'environments',
    ]);
  });

  it('starts with every panel collapsed', () => {
    expect(activePanels()).toEqual([]);
  });

  it('renders inside the maintenance page without a nested section container', () => {
    expect(fixture.nativeElement.querySelector('app-section-container')).toBeNull();
  });

  it('keeps one active panel and synchronizes it with the URL fragment', () => {
    changePanel('databases');
    expect(activePanels()).toEqual(['databases']);

    changePanel('environments');
    expect(activePanels()).toEqual(['environments']);
    expect(navigate).toHaveBeenLastCalledWith([], {
      relativeTo: TestBed.inject(ActivatedRoute),
      fragment: 'environments',
      queryParamsHandling: 'preserve',
      replaceUrl: true,
    });
  });

  it('opens a valid panel received from a compatible URL', () => {
    fragment.next('servers');
    fixture.detectChanges();

    expect(activePanels()).toEqual(['servers']);
  });

  function changePanel(value: string | null): void {
    (
      fixture.componentInstance as unknown as {
        onPanelChange(value: string | null): void;
      }
    ).onPanelChange(value);
    fixture.detectChanges();
  }

  function activePanels(): string[] {
    return fixture.debugElement
      .queryAll(By.directive(AccordionPanel))
      .filter(({ componentInstance }) => componentInstance.active())
      .map(({ componentInstance }) => componentInstance.value());
  }
});
