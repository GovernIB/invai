import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { HelpModalService } from './help-modal.service';
import { HELP_TOPICS, helpTopic } from './help-topics';

@Component({ template: '' })
class Screen {}

describe('HelpModalService', () => {
  let service: HelpModalService;
  let requested: string[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          {
            path: 'applications',
            data: helpTopic(HELP_TOPICS.applicationsList),
            children: [
              { path: '', component: Screen },
              { path: 'new', component: Screen, data: helpTopic(HELP_TOPICS.applicationCreate) },
              { path: ':id', component: Screen },
            ],
          },
          { path: 'maintenances', component: Screen },
        ]),
      ],
    });
    service = TestBed.inject(HelpModalService);
    requested = [];
    service.topicRequests.subscribe((document) => requested.push(document));
  });

  const navigate = (url: string) => TestBed.inject(Router).navigateByUrl(url);

  function reopen() {
    service.close();
    service.open();
  }

  it('opens, closes and toggles the dialog', () => {
    service.open();
    expect(service.visible()).toBe(true);
    service.toggle();
    expect(service.visible()).toBe(false);
    service.toggle();
    service.close();
    expect(service.visible()).toBe(false);
  });

  describe('document on opening', () => {
    it('asks for the document of the active screen', async () => {
      await navigate('/applications/new');

      service.open();

      expect(requested).toEqual([HELP_TOPICS.applicationCreate]);
    });

    it('uses the document of the containing screen when the active one has none', async () => {
      await navigate('/applications/42');

      service.open();

      expect(requested).toEqual([HELP_TOPICS.applicationsList]);
    });

    it('asks for nothing when neither the screen nor a dialog has a document', async () => {
      await navigate('/maintenances');

      service.open();

      expect(requested).toEqual([]);
      expect(service.visible()).toBe(true);
    });

    it('asks only when the dialog opens, not while it is already visible', async () => {
      await navigate('/applications');

      service.open();
      service.open();

      expect(requested).toEqual([HELP_TOPICS.applicationsList]);
    });

    it('prefers the last open dialog and goes back to the screen as dialogs close', async () => {
      await navigate('/applications');
      const releaseFirst = service.registerDialogTopic(HELP_TOPICS.maintenanceCategories);
      const releaseSecond = service.registerDialogTopic(HELP_TOPICS.maintenanceFields);

      service.open();
      releaseFirst();
      reopen();
      releaseSecond();
      reopen();

      expect(requested).toEqual([
        HELP_TOPICS.maintenanceFields,
        HELP_TOPICS.maintenanceFields,
        HELP_TOPICS.applicationsList,
      ]);
    });

    it('withdraws only its own entry when two open dialogs share a document', async () => {
      await navigate('/maintenances');
      const releaseOuter = service.registerDialogTopic(HELP_TOPICS.maintenancePeople);
      service.registerDialogTopic(HELP_TOPICS.maintenancePeople);

      releaseOuter();
      releaseOuter();
      service.open();

      expect(requested).toEqual([HELP_TOPICS.maintenancePeople]);
    });
  });

  describe('focus return', () => {
    let trigger: HTMLButtonElement;
    let elsewhere: HTMLButtonElement;

    beforeEach(() => {
      trigger = document.body.appendChild(document.createElement('button'));
      elsewhere = document.body.appendChild(document.createElement('button'));
    });

    afterEach(() => {
      trigger.remove();
      elsewhere.remove();
    });

    it('returns focus to the element focused when the dialog opened, once', () => {
      trigger.focus();
      service.toggle();
      elsewhere.focus();

      service.restoreFocus();
      expect(document.activeElement).toBe(trigger);

      elsewhere.focus();
      service.restoreFocus();
      expect(document.activeElement).toBe(elsewhere);
    });

    it('keeps the first element when the dialog is opened again while visible', () => {
      trigger.focus();
      service.open();
      elsewhere.focus();
      service.open();

      service.restoreFocus();
      expect(document.activeElement).toBe(trigger);
    });

    it('leaves focus alone when that element is no longer on the page', () => {
      trigger.focus();
      service.open();
      elsewhere.focus();
      trigger.remove();

      service.restoreFocus();
      expect(document.activeElement).toBe(elsewhere);
    });
  });
});
