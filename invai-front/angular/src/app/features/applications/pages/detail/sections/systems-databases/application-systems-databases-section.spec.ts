import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Editor } from 'primeng/editor';

import { ApplicationInfrastructureList } from '../../../../components';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationSystemsDatabasesSection } from './application-systems-databases-section';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

describe('ApplicationSystemsDatabasesSection', () => {
  let fixture: ComponentFixture<ApplicationSystemsDatabasesSection>;
  let messageService: MessageService;
  let detailState: ApplicationDetailState;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationSystemsDatabasesSection],
      providers: [ApplicationDetailState, MessageService],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationSystemsDatabasesSection);
    messageService = TestBed.inject(MessageService);
    detailState = TestBed.inject(ApplicationDetailState);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should render independent server and database lists', () => {
    const lists = fixture.debugElement.queryAll(By.directive(ApplicationInfrastructureList));

    expect(lists).toHaveLength(2);
    expect(lists[0].componentInstance.title()).toBe('Servidors');
    expect(lists[1].componentInstance.title()).toBe('Bases de dades');
    expect(lists[0].componentInstance.sourceItems()[0].id).toBe('server-1');
    expect(lists[1].componentInstance.sourceItems()[0].id).toBe('database-1');
  });

  it('should render the documentation action and observations editor', () => {
    const documentationButton = fixture.debugElement
      .queryAll(By.directive(Button))
      .find((button) => button.componentInstance.label === 'Documentació');
    const editor = fixture.debugElement.query(By.directive(Editor));
    const observationsGroup = fixture.debugElement.query(
      By.css('.application-systems-databases__observations'),
    );
    const labelId = 'application-systems-databases-observations-label';

    expect(documentationButton?.componentInstance.ariaLabel).toBe(
      'Obrir la documentació de sistemes i bases de dades',
    );
    expect(editor).toBeTruthy();
    expect(editor.nativeElement.id).toBe('application-systems-databases-observations');
    expect(observationsGroup.attributes['aria-labelledby']).toBe(labelId);
    expect(fixture.debugElement.query(By.css(`#${labelId}`)).nativeElement.textContent.trim()).toBe(
      'Observacions',
    );
  });

  it('should bind observations to the page-scoped detail form', () => {
    expect(detailState.systemsDatabasesForm.controls.observations.value).toBe('');

    detailState.systemsDatabasesForm.controls.observations.setValue('<p>Text enriquit</p>');
    fixture.detectChanges();

    const editor = fixture.debugElement.query(By.directive(Editor));
    expect(editor.componentInstance.value).toBe('<p>Text enriquit</p>');
  });

  it('should show a pending message when opening documentation', () => {
    const addSpy = vi.spyOn(messageService, 'add');
    const section = fixture.componentInstance as unknown as { onDocumentation: () => void };

    section.onDocumentation();

    expect(addSpy).toHaveBeenCalledWith({
      severity: 'info',
      summary: 'Informació',
      detail: 'La documentació de sistemes i bases de dades encara no està implementada.',
    });
  });
});
