import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationDetailSectionLayout } from './application-detail-section-layout';

@Component({
  standalone: true,
  imports: [ApplicationDetailSectionLayout],
  template: `
    <app-application-detail-section-layout title="Títol de secció" titleId="section-title">
      <button section-actions class="projected-action">Acció</button>
      <p class="projected-content">Contingut</p>
    </app-application-detail-section-layout>
  `,
})
class TestHost {}

describe('ApplicationDetailSectionLayout', () => {
  let fixture: ComponentFixture<TestHost>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHost],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHost);
    fixture.detectChanges();
  });

  it('renders an accessible title and projects the section content', () => {
    const section = fixture.nativeElement.querySelector(
      '.application-detail-section-layout',
    ) as HTMLElement;
    const title = fixture.nativeElement.querySelector(
      '.application-detail-section-layout__title',
    ) as HTMLHeadingElement;
    const content = fixture.nativeElement.querySelector(
      '.projected-content',
    ) as HTMLParagraphElement;
    const action = fixture.nativeElement.querySelector(
      '.projected-action',
    ) as HTMLButtonElement;

    expect(section.getAttribute('aria-labelledby')).toBe('section-title');
    expect(title.id).toBe('section-title');
    expect(title.textContent?.trim()).toBe('Títol de secció');
    expect(action.closest('.application-detail-section-layout__header')).not.toBeNull();
    expect(content.textContent?.trim()).toBe('Contingut');
  });
});
