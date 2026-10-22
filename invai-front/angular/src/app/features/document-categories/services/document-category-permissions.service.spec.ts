import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { DocumentCategoryPermissionsService } from './document-category-permissions.service';

describe('DocumentCategoryPermissionsService', () => {
  let service: DocumentCategoryPermissionsService;

  beforeEach(() => {
    service = TestBed.inject(DocumentCategoryPermissionsService);
  });

  it('starts every category with the default permissions of each role', async () => {
    const permissions = await firstValueFrom(service.getByCategory(1));

    expect(permissions.map(({ roleName, read, upload, modify, delete: remove }) => [
      roleName,
      read,
      upload,
      modify,
      remove,
    ])).toEqual([
      ['Administració funcional', true, true, true, true],
      ["Responsable d'aplicació", true, true, true, false],
      ['Responsable de seguretat', true, false, false, false],
      ['Consulta', true, false, false, false],
    ]);
  });

  it('keeps the permissions saved for a category apart from the others', async () => {
    const permissions = await firstValueFrom(service.getByCategory(1));
    permissions[3].upload = true;

    await firstValueFrom(service.save(1, permissions));
    // Changing what was saved does not change the stored permissions.
    permissions[3].delete = true;

    expect((await firstValueFrom(service.getByCategory(1)))[3]).toMatchObject({
      upload: true,
      delete: false,
    });
    expect((await firstValueFrom(service.getByCategory(2)))[3].upload).toBe(false);
  });
});
