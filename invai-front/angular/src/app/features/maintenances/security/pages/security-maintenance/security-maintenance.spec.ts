import { Component, input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AccordionPanel } from 'primeng/accordion';
import { BehaviorSubject } from 'rxjs';

import { SECURITY_MAINTENANCE_PANELS } from '../../security.constants';
import { SecurityResourceKey } from '../../security.model';
import { SecurityResourceList } from '../security-resource-list/security-resource-list';
import { SecurityMaintenance } from './security-maintenance';

@Component({ selector: 'app-security-resource-list', standalone: true, template: '' })
class SecurityResourceListStub {
  resource = input.required<SecurityResourceKey>();
  initialPage = input<unknown>(null);
  initialLoadFailed = input(false);
}

describe('SecurityMaintenance', () => {
  let fixture: ComponentFixture<SecurityMaintenance>;
  let fragment: BehaviorSubject<string | null>;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    fragment = new BehaviorSubject<string | null>(null);
    navigate = vi.fn(() => Promise.resolve(true));
    await TestBed.configureTestingModule({
      imports: [SecurityMaintenance],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: {}, fragment: null }, fragment },
        },
        { provide: Router, useValue: { navigate } },
      ],
    })
      .overrideComponent(SecurityMaintenance, {
        remove: { imports: [SecurityResourceList] },
        add: { imports: [SecurityResourceListStub] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(SecurityMaintenance);
    fixture.detectChanges();
  });

  it('renders the five security panels in the audited order', () => {
    const headers = fixture.nativeElement.querySelectorAll(
      '.maintenance-panel-header',
    ) as NodeListOf<HTMLElement>;

    expect(headers).toHaveLength(5);
    expect(
      Array.from(headers).map((header) => ({
        title: header.querySelector('.maintenance-panel-title')?.textContent,
        description: header.querySelector('.maintenance-panel-description')?.textContent,
      })),
    ).toEqual(SECURITY_MAINTENANCE_PANELS.map(({ title, description }) => ({ title, description })));
  });

  it('creates five CRUD resources without the security-role panel or notice', () => {
    expect(fixture.debugElement.queryAll(By.directive(SecurityResourceListStub))).toHaveLength(5);
    expect(fixture.nativeElement.textContent).not.toContain('Rols de seguretat');
    expect(fixture.nativeElement.textContent).not.toContain(
      "Aquest manteniment està pendent que el backend exposi l'estat d'activació dels rols.",
    );
  });

  it('closes the active panel when navigating to the removed security-roles fragment', () => {
    fragment.next('web-contexts');
    fixture.detectChanges();

    expect(
      fixture.debugElement.queryAll(By.directive(AccordionPanel))
        .some(({ componentInstance }) => componentInstance.active()),
    ).toBe(true);

    fragment.next('security-roles');
    fixture.detectChanges();

    expect(
      fixture.debugElement.queryAll(By.directive(AccordionPanel))
        .some(({ componentInstance }) => componentInstance.active()),
    ).toBe(false);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('keeps one active panel and synchronizes it with the URL fragment', () => {
    (
      fixture.componentInstance as unknown as {
        onPanelChange(value: string): void;
      }
    ).onPanelChange('web-contexts');
    fixture.detectChanges();

    expect(
      fixture.debugElement
        .queryAll(By.directive(AccordionPanel))
        .filter(({ componentInstance }) => componentInstance.active())
        .map(({ componentInstance }) => componentInstance.value()),
    ).toEqual(['web-contexts']);
    expect(navigate).toHaveBeenLastCalledWith([], {
      relativeTo: TestBed.inject(ActivatedRoute),
      fragment: 'web-contexts',
      queryParamsHandling: 'preserve',
      replaceUrl: true,
    });
  });
});
