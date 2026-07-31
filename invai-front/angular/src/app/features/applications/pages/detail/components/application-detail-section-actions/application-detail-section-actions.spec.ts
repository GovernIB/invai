import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationDetailSectionActions } from './application-detail-section-actions';

describe('ApplicationDetailSectionActions', () => {
  let fixture: ComponentFixture<ApplicationDetailSectionActions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationDetailSectionActions],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDetailSectionActions);
    fixture.componentRef.setInput('sectionLabel', 'General');
    fixture.detectChanges();
  });

  it('emits Edit with a section-specific accessible label', () => {
    const editSpy = vi.fn();
    fixture.componentInstance.edit.subscribe(editSpy);
    const button = fixture.nativeElement.querySelector('button') as HTMLButtonElement;

    expect(button.getAttribute('aria-label')).toBe('Editar General');
    button.click();
    expect(editSpy).toHaveBeenCalledOnce();
  });

  it('renders independent Cancel and Save actions while editing', () => {
    fixture.componentRef.setInput('isEditing', true);
    fixture.detectChanges();

    const buttons = [
      ...fixture.nativeElement.querySelectorAll('button'),
    ] as HTMLButtonElement[];
    expect(buttons.map((button) => button.textContent?.trim())).toEqual([
      'Cancel·lar',
      'Desar',
    ]);
    expect(buttons[0].getAttribute('aria-label')).toContain('General');
    expect(buttons[1].getAttribute('aria-label')).toContain('General');
  });
});
