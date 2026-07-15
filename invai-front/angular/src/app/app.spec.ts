import { TestBed } from '@angular/core/testing';

import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    })
      .overrideComponent(App, { set: { template: '' } })
      .compileComponents();
  });

  it('should create the app component', () => {
    const component = TestBed.createComponent(App).componentInstance;

    expect(component).toBeTruthy();
  });
});
