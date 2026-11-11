import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { DocumentCategoryPermissionsService } from './document-category-permissions.service';

const PERMISSIONS_URL = '/invaiback/documentation/role-permission';

const RESPONSE = [
  {
    roleId: 31,
    name: 'INV_ADMIN',
    description: "Administració de l'aplicació INVAI",
    canRead: true,
    canUpload: true,
    canEdit: true,
    canDelete: true,
  },
  {
    roleId: 34,
    name: 'INV_LECTOR',
    description: null,
    canRead: true,
    canUpload: false,
    canEdit: false,
    canDelete: false,
  },
];

const PERMISSIONS = [
  { roleId: 31, roleName: 'INV_ADMIN', read: true, upload: true, modify: true, delete: true },
  { roleId: 34, roleName: 'INV_LECTOR', read: true, upload: false, modify: false, delete: false },
];

describe('DocumentCategoryPermissionsService', () => {
  let service: DocumentCategoryPermissionsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DocumentCategoryPermissionsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('reads the permissions of every role in a category', async () => {
    const permissions = firstValueFrom(service.getByCategory(7));

    httpTesting.expectOne({ method: 'GET', url: `${PERMISSIONS_URL}/7` }).flush(RESPONSE);

    await expect(permissions).resolves.toEqual(PERMISSIONS);
  });

  it('reads them again every time, as each save changes them', () => {
    service.getByCategory(7).subscribe();
    service.getByCategory(7).subscribe();

    expect(httpTesting.match(`${PERMISSIONS_URL}/7`)).toHaveLength(2);
  });

  it('saves the permissions of the roles of a category with the names of the back-end', async () => {
    const saved = firstValueFrom(
      service.save(7, [{ ...PERMISSIONS[1], upload: true }]),
    );

    const request = httpTesting.expectOne({ method: 'PUT', url: `${PERMISSIONS_URL}/7` });
    expect(request.request.body).toEqual({
      permissions: [{ roleId: 34, canRead: true, canUpload: true, canEdit: false, canDelete: false }],
    });
    request.flush(RESPONSE);

    await expect(saved).resolves.toEqual(PERMISSIONS);
  });
});
