import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Sidebar, SidebarVariant } from './sidebar';
import { SidebarService } from './sidebar.service';

describe('Sidebar', () => {
  const isVisible = signal(true);

  async function render(variant: SidebarVariant): Promise<ComponentFixture<Sidebar>> {
    await TestBed.configureTestingModule({
      imports: [Sidebar],
      providers: [
        provideRouter([]),
        {
          provide: SidebarService,
          useValue: {
            isCollapsed: signal(false).asReadonly(),
            isDrawerVisible: signal(false).asReadonly(),
            isTablet: signal(false).asReadonly(),
            isVisible: isVisible.asReadonly(),
            closeDrawer: vi.fn(),
            toggleCollapsed: vi.fn(),
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(Sidebar);
    fixture.componentRef.setInput('variant', variant);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => {
    isVisible.set(true);
    // PrimeNG TieredMenu listens to a breakpoint media query that jsdom does not implement.
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({ matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() })),
    );
  });

  afterEach(() => vi.unstubAllGlobals());

  it('should expose the inline sidebar as a named navigation landmark', async () => {
    const fixture = await render('inline');
    const landmark = fixture.nativeElement.querySelector('#sidebar-navigation-inline') as HTMLElement;

    expect(landmark.tagName).toBe('NAV');
    expect(landmark.getAttribute('aria-label')).toBe('Navegació lateral');
    expect(landmark.hasAttribute('aria-hidden')).toBe(false);
    expect(fixture.nativeElement.querySelector('aside')).toBeNull();
  });

  it('should name the drawer copy differently because both copies coexist on tablet', async () => {
    const fixture = await render('drawer');
    const landmark = fixture.nativeElement.querySelector(
      '#sidebar-navigation-drawer-content',
    ) as HTMLElement;

    expect(landmark.tagName).toBe('NAV');
    expect(landmark.getAttribute('aria-label')).toBe('Navegació lateral desplegable');
  });

  it('should hide the empty landmark while the sidebar is not visible', async () => {
    isVisible.set(false);
    const fixture = await render('inline');
    const landmark = fixture.nativeElement.querySelector('nav') as HTMLElement;

    expect(landmark.getAttribute('aria-hidden')).toBe('true');
    expect(landmark.querySelector('a, button')).toBeNull();
  });
});
