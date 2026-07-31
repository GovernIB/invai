import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { InitialNavigationLoadingService } from '@core/services/initial-navigation-loading.service';

import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        {
          provide: InitialNavigationLoadingService,
          useValue: { isLoading: signal(false).asReadonly() },
        },
      ],
    })
      .overrideComponent(App, { set: { template: '' } })
      .compileComponents();
  });

  it('should create the app component', () => {
    const component = TestBed.createComponent(App).componentInstance;

    expect(component).toBeTruthy();
  });
});
