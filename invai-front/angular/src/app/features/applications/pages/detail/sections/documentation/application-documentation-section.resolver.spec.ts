import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, convertToParamMap, RouterStateSnapshot } from '@angular/router';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';

import { ApplicationDocumentationService } from '../../../../services/application-documentation.service';
import { ApplicationLinksService } from '../../../../services/application-links.service';
import {
  APPLICATION_ATTACHMENTS_INITIAL_PARAMS,
  APPLICATION_LINKS_INITIAL_PARAMS,
  ApplicationDocumentationResolvedData,
  applicationDocumentationResolver,
} from './application-documentation-section.resolver';

describe('applicationDocumentationResolver', () => {
  const getAttachments = vi.fn();
  const getLinks = vi.fn();
  const attachments = { content: [], totalElements: 0 };
  const links = { content: [], totalElements: 0 };

  beforeEach(() => {
    getAttachments.mockReset().mockReturnValue(of(attachments));
    getLinks.mockReset().mockReturnValue(of(links));
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationDocumentationService, useValue: { getAttachments } },
        { provide: ApplicationLinksService, useValue: { getLinks } },
      ],
    });
  });

  it('reads the first page of attachments and links of the application of the route', async () => {
    await expect(resolve('7')).resolves.toEqual({ applicationId: 7, attachments, links });
    expect(getAttachments).toHaveBeenCalledWith(7, APPLICATION_ATTACHMENTS_INITIAL_PARAMS);
    expect(APPLICATION_ATTACHMENTS_INITIAL_PARAMS).toMatchObject({ page: 0, deleted: false });
    expect(getLinks).toHaveBeenCalledWith(7, APPLICATION_LINKS_INITIAL_PARAMS);
    expect(APPLICATION_LINKS_INITIAL_PARAMS).toMatchObject({ page: 0, deleted: false });
  });

  it('degrades each page independently and ignores an invalid ID', async () => {
    getLinks.mockReturnValueOnce(throwError(() => new Error('offline')));
    await expect(resolve('7')).resolves.toEqual({ applicationId: 7, attachments, links: null });

    await expect(resolve('new')).resolves.toEqual({ applicationId: null, attachments: null, links: null });
  });

  function resolve(id: string) {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          applicationDocumentationResolver(
            { parent: { paramMap: convertToParamMap({ id }) } } as unknown as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<ApplicationDocumentationResolvedData>,
      ),
    );
  }
});
