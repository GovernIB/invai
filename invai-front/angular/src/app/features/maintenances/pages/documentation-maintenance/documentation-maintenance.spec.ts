import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentCategoriesList } from '@features/document-categories/pages/list/document-categories-list';
import { DocumentCategoriesService } from '@features/document-categories/services/document-categories.service';
import { DocumentCategoryCatalogService } from '@features/document-categories/services/document-category-catalog.service';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

import { DocumentationMaintenance } from './documentation-maintenance';

describe('DocumentationMaintenance', () => {
  let fixture: ComponentFixture<DocumentationMaintenance>;
  const navigate = vi.fn();

  async function create(fragment: string | null): Promise<void> {
    navigate.mockReset();
    await TestBed.configureTestingModule({
      imports: [DocumentationMaintenance],
      providers: [
        MessageService,
        { provide: DocumentCategoriesService, useValue: {} },
        { provide: DocumentCategoryCatalogService, useValue: {} },
        { provide: Router, useValue: { navigate } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { fragment, data: {} }, fragment: of(fragment) },
        },
      ],
    })
      .overrideComponent(DocumentCategoriesList, { set: { template: '' } })
      .compileComponents();
    fixture = TestBed.createComponent(DocumentationMaintenance);
    fixture.detectChanges();
  }

  const activePanel = () =>
    (fixture.componentInstance as unknown as { activePanel: () => string | null }).activePanel();

  it('lists the document categories panel with its description and opens it from the fragment', async () => {
    await create('document-categories');
    const root = fixture.nativeElement as HTMLElement;

    expect(root.querySelector('.maintenance-panel-title')?.textContent?.trim()).toBe(
      'Categories documentals',
    );
    expect(root.querySelector('.maintenance-panel-description')?.textContent).toContain(
      'documentació de les aplicacions',
    );
    expect(activePanel()).toBe('document-categories');
  });

  it('ignores an unknown fragment and records the opened panel in the URL', async () => {
    await create('unknown');
    expect(activePanel()).toBeNull();

    (fixture.componentInstance as unknown as { onPanelChange: (value: string) => void }).onPanelChange(
      'document-categories',
    );

    expect(activePanel()).toBe('document-categories');
    expect(navigate).toHaveBeenCalledWith([], expect.objectContaining({ fragment: 'document-categories' }));
  });
});
