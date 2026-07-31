import { Component, input, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResolverNavigationLoadingService } from '@core/services/resolver-navigation-loading.service';

import { Sidebar } from './components/sidebar/sidebar';
import { SidebarService } from './components/sidebar/sidebar.service';
import { TopMenu } from './components/top-menu/top-menu';
import { MainLayout } from './main-layout';

@Component({
  selector: 'app-sidebar',
  template: '',
})
class SidebarStub {
  readonly variant = input('inline');
}

@Component({
  selector: 'app-top-menu',
  template: '',
})
class TopMenuStub {}

describe('MainLayout', () => {
  let fixture: ComponentFixture<MainLayout>;
  const isResolverLoading = signal(false);
  const isDrawerVisible = signal(false);
  const isMobile = signal(false);
  const isSidebarVisible = signal(true);

  beforeEach(async () => {
    isResolverLoading.set(false);

    await TestBed.configureTestingModule({
      imports: [MainLayout],
      providers: [
        {
          provide: ResolverNavigationLoadingService,
          useValue: { isLoading: isResolverLoading.asReadonly() },
        },
        {
          provide: SidebarService,
          useValue: {
            isDrawerVisible: isDrawerVisible.asReadonly(),
            isMobile: isMobile.asReadonly(),
            isVisible: isSidebarVisible.asReadonly(),
            openDrawer: vi.fn(),
            setDrawerVisibility: vi.fn(),
          },
        },
      ],
    })
      .overrideComponent(MainLayout, {
        remove: { imports: [Sidebar, TopMenu] },
        add: { imports: [SidebarStub, TopMenuStub] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(MainLayout);
    fixture.detectChanges();
  });

  it('should not render resolver progress while no resolver is pending', () => {
    expect(fixture.nativeElement.querySelector('.invai-resolver-progress')).toBeNull();
  });

  it('should render an accessible, indeterminate progress bar while resolving', () => {
    isResolverLoading.set(true);
    fixture.detectChanges();

    const progress = fixture.nativeElement.querySelector(
      '.invai-resolver-progress',
    ) as HTMLElement;

    expect(progress).toBeTruthy();
    expect(progress.getAttribute('role')).toBe('progressbar');
    expect(progress.getAttribute('aria-label')).toBe('Carregant la pàgina');
    expect(progress.hasAttribute('aria-valuenow')).toBe(false);
  });
});
