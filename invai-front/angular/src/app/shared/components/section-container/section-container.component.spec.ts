import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { SectionContainerComponent } from './section-container.component';

@Component({
  imports: [SectionContainerComponent],
  template: `
    <app-section-container [header]="title">
      <button right-top type="button" (click)="actions = actions + 1">Action</button>
      <p>Page content</p>
    </app-section-container>
  `,
})
class TestPage {
  title = 'Applications';
  actions = 0;
}

describe('SectionContainerComponent', () => {
  it('renders one page heading and projects working actions into the header, before the body', () => {
    const fixture = TestBed.createComponent(TestPage);
    fixture.detectChanges();
    const root = fixture.nativeElement as HTMLElement;
    const header = root.querySelector('header')!;
    const body = root.querySelector('.section-container__body')!;

    expect(root.querySelectorAll('h1')).toHaveLength(1);
    expect(header.querySelector('h1')?.textContent).toBe('Applications');
    expect(body.querySelector('p')?.textContent).toBe('Page content');
    expect(body.querySelector('button')).toBeNull();
    expect(header.nextElementSibling).toBe(body);
    header.querySelector('button')!.click();
    expect(fixture.componentInstance.actions).toBe(1);

    fixture.componentInstance.title = 'Updated title';
    fixture.detectChanges();
    expect(header.querySelector('h1')?.textContent).toBe('Updated title');
  });

  it('does not invent a heading for a container without a title', () => {
    const fixture = TestBed.createComponent(SectionContainerComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('header')).toBeNull();
    expect(fixture.nativeElement.querySelector('.section-container__body')).not.toBeNull();
  });
});
