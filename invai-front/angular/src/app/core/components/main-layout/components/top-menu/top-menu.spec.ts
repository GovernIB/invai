import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MenuService } from '@core/services/menu.service';
import { MenuItem, MessageService } from 'primeng/api';

import { SidebarService } from '../sidebar/sidebar.service';
import { TopMenu } from './top-menu';

describe('TopMenu', () => {
  const setup = async () => {
    const sidebarItems = signal<MenuItem[]>([]);
    await TestBed.configureTestingModule({
      imports: [TopMenu],
      providers: [
        MessageService,
        {
          provide: MenuService,
          useValue: {
            menuItems: signal([
              { id: 'APPLICATIONS', label: 'Aplicacions', items: [{ label: 'Llistat' }] },
            ]),
            menuOptionsResource: {
              isLoading: () => false,
              error: () => undefined,
              reload: vi.fn(),
            },
          },
        },
        {
          provide: SidebarService,
          useValue: {
            menuItems: sidebarItems,
            revealMenu: vi.fn(),
            setMenuItems: (items: MenuItem[]) => sidebarItems.set(items),
            setVisibility: vi.fn(),
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(TopMenu);
    fixture.detectChanges();
    return fixture;
  };

  it('should name its navigation landmark so it is distinguishable from the other navs', async () => {
    const fixture = await setup();
    const nav = fixture.nativeElement.querySelector('nav') as HTMLElement;

    expect(nav.getAttribute('aria-label')).toBe('Navegació principal');
  });

  it('should expose the sidebar state on the native menu button', async () => {
    const fixture = await setup();
    const host = fixture.nativeElement.querySelector(
      '[data-testid="button-applications-testid"]',
    ) as HTMLElement;
    const button = host.querySelector('button') as HTMLButtonElement;

    expect(host.hasAttribute('aria-expanded')).toBe(false);
    expect(button.getAttribute('aria-expanded')).toBe('false');

    button.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('true');

    button.click();
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
  });
});
