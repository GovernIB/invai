import { TestBed } from '@angular/core/testing';

import { RESPONSIBLES_MAINTENANCE_EMPTY_MESSAGE } from '../../maintenances.i18n';
import { MAINTENANCES_ROUTES_LABELS } from '../../maintenances.routes.i18n';
import { ResponsiblesMaintenance } from './responsibles-maintenance';

describe('ResponsiblesMaintenance', () => {
  it('renders an accessible empty state', async () => {
    await TestBed.configureTestingModule({
      imports: [ResponsiblesMaintenance],
    }).compileComponents();

    const fixture = TestBed.createComponent(ResponsiblesMaintenance);
    fixture.detectChanges();
    const section = fixture.nativeElement.querySelector('section');

    expect(section.getAttribute('aria-label')).toBe(MAINTENANCES_ROUTES_LABELS.RESPONSIBLES);
    expect(section.textContent).toContain(RESPONSIBLES_MAINTENANCE_EMPTY_MESSAGE);
  });
});
