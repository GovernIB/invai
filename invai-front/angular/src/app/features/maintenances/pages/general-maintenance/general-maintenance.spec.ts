import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoriesList } from '@features/categories/pages/list/categories-list';
import { CommissionsList } from '@features/commissions/pages/list/commissions-list';
import { FieldsList } from '@features/fields/pages/list/fields-list';
import { SystemTypesList } from '@features/system-types/pages/list/system-types-list';
import { AccordionPanel } from 'primeng/accordion';
import { BehaviorSubject } from 'rxjs';

import { GENERAL_MAINTENANCE_PANELS } from '../../maintenances.constants';
import { GeneralMaintenance } from './general-maintenance';

@Component({ selector: 'app-categories-list', standalone: true, template: '' })
class CategoriesListStub {}

@Component({ selector: 'app-system-types-list', standalone: true, template: '' })
class SystemTypesListStub {}

@Component({ selector: 'app-fields-list', standalone: true, template: '' })
class FieldsListStub {}

@Component({ selector: 'app-commissions-list', standalone: true, template: '' })
class CommissionsListStub {}

describe('GeneralMaintenance', () => {
  let fixture: ComponentFixture<GeneralMaintenance>;
  let fragment: BehaviorSubject<string | null>;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    fragment = new BehaviorSubject<string | null>(null);
    navigate = vi.fn(() => Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [GeneralMaintenance],
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
      .overrideComponent(GeneralMaintenance, {
        remove: { imports: [CategoriesList, CommissionsList, FieldsList, SystemTypesList] },
        add: {
          imports: [
            CategoriesListStub,
            CommissionsListStub,
            FieldsListStub,
            SystemTypesListStub,
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(GeneralMaintenance);
    fixture.detectChanges();
  });

  it('renders the four general maintenance panels with their descriptions', () => {
    const headers = fixture.nativeElement.querySelectorAll(
      '.maintenance-panel-header',
    ) as NodeListOf<HTMLElement>;

    expect(headers).toHaveLength(4);
    expect(
      Array.from(headers).map((header) => ({
        title: header.querySelector('.maintenance-panel-title')?.textContent,
        description: header.querySelector('.maintenance-panel-description')?.textContent,
      })),
    ).toEqual(
      GENERAL_MAINTENANCE_PANELS.map(({ title, description }) => ({ title, description })),
    );
  });

  it('starts with every panel collapsed', () => {
    expect(
      fixture.debugElement
        .queryAll(By.directive(AccordionPanel))
        .map(({ componentInstance }) => componentInstance.active()),
    ).toEqual([false, false, false, false]);
  });

  it('keeps one active panel and synchronizes it with the URL fragment', () => {
    changePanel('categories');
    expect(activePanels()).toEqual(['categories']);

    changePanel('system-types');
    expect(activePanels()).toEqual(['system-types']);
    expect(navigate).toHaveBeenLastCalledWith([], {
      relativeTo: TestBed.inject(ActivatedRoute),
      fragment: 'system-types',
      queryParamsHandling: 'preserve',
      replaceUrl: true,
    });
  });

  it('opens a valid panel received from a compatible URL', () => {
    fragment.next('fields');
    fixture.detectChanges();

    expect(activePanels()).toEqual(['fields']);
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
