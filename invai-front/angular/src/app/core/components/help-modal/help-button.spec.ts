import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { HelpButton } from './help-button';
import { HelpModalService } from './help-modal.service';
import { HELP_TOPICS, helpTopic } from './help-topics';

@Component({ template: '' })
class Screen {}

describe('HelpButton', () => {
  it('follows navigation, opens the screen topic and restores its trigger focus', async () => {
    TestBed.configureTestingModule({ providers: [provideRouter([
      { path: 'list', component: Screen, data: helpTopic(HELP_TOPICS.applicationsList) },
      { path: 'create', component: Screen, data: helpTopic(HELP_TOPICS.applicationCreate) },
      { path: 'other', component: Screen },
    ])] });
    const router = TestBed.inject(Router);
    const fixture = TestBed.createComponent(HelpButton);
    const service = TestBed.inject(HelpModalService);
    const requested: string[] = [];
    service.topicRequests.subscribe(value => requested.push(value));
    await router.navigateByUrl('/list');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button')).not.toBeNull();
    await router.navigateByUrl('/create');
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(button.getAttribute('aria-keyshortcuts')).toBe('Control+K');
    button.click();
    expect(requested).toEqual([HELP_TOPICS.applicationCreate]);
    service.close();
    service.restoreFocus();
    expect(document.activeElement).toBe(button);
    await router.navigateByUrl('/other');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button')).toBeNull();
  });

  it('uses an explicit document even without screen metadata', () => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(HelpButton);
    fixture.componentRef.setInput('topic', HELP_TOPICS.maintenanceCategories);
    fixture.detectChanges();
    const requested: string[] = [];
    TestBed.inject(HelpModalService).topicRequests.subscribe(value => requested.push(value));
    fixture.nativeElement.querySelector('button').click();
    expect(requested).toEqual([HELP_TOPICS.maintenanceCategories]);
  });
});
