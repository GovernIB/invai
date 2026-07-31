import { TestBed } from '@angular/core/testing';

import { ServerErrorDialogService } from './server-error-dialog.service';

describe('ServerErrorDialogService', () => {
  let service: ServerErrorDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ServerErrorDialogService);
  });

  it('opens and closes a server error', () => {
    service.open({ error: 'Error de validació', message: 'Request failed' });

    expect(service.error()).toEqual({
      error: 'Error de validació',
      message: 'Request failed',
    });
    expect(service.visible()).toBe(true);

    service.close();

    expect(service.visible()).toBe(false);
  });
});
