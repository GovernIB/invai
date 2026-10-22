import { inject, Injectable, LOCALE_ID } from '@angular/core';
import { DocumentCategoryCatalogService } from '@features/document-categories/services/document-category-catalog.service';
import { forkJoin, map, Observable } from 'rxjs';

import {
  ApplicationDocument,
  ApplicationDocumentCategoryOption,
  ApplicationDocumentDetailData,
} from '../../application-documentation.model';
import { ApplicationStatusCode } from '../../applications.model';
import { ApplicationDocumentStatusCatalogService } from '../../services/application-document-status-catalog.service';
import { ApplicationDocumentationService } from '../../services/application-documentation.service';
import { ApplicationsService } from '../../services/applications.service';
import { documentationLabel } from '../detail/sections/documentation/application-documentation.utils';

// Everything the document page needs: the document with its versions, its application, and the
// categories and states it can be changed to. Only an active application lets a document that is
// not deleted be changed; a document of another application is not found.
@Injectable({ providedIn: 'root' })
export class ApplicationDocumentDetailLoader {
  private readonly locale = inject(LOCALE_ID);
  private readonly documents = inject(ApplicationDocumentationService);
  private readonly applications = inject(ApplicationsService);
  private readonly categories = inject(DocumentCategoryCatalogService);
  private readonly statuses = inject(ApplicationDocumentStatusCatalogService);

  load(applicationId: number, documentId: number): Observable<ApplicationDocumentDetailData> {
    return forkJoin({
      document: this.documents.getDocument(documentId),
      application: this.applications.getById(applicationId),
      categories: this.categories.getActiveCategoryTree(),
      statuses: this.statuses.getAll(),
    }).pipe(
      map(({ document, application, categories, statuses }) => {
        if (document.applicationId !== applicationId) {
          throw new Error(`Document ${documentId} does not belong to application ${applicationId}`);
        }
        return {
          application: {
            id: applicationId,
            code: application.code || String(applicationId),
            name: application.name ?? '',
            canManage: application.status !== ApplicationStatusCode.INACTIVE && !document.deletedAt,
          },
          document,
          categories: this.withDocumentCategory(categories, document),
          statuses,
        };
      }),
    );
  }

  // Keeps the category and subcategory of the document selectable even after their deactivation.
  private withDocumentCategory(
    categories: ApplicationDocumentCategoryOption[],
    document: ApplicationDocument,
  ): ApplicationDocumentCategoryOption[] {
    const options = categories.map((category) => ({ ...category, subcategories: [...category.subcategories] }));
    let category = options.find(({ id }) => id === document.category.id);
    if (!category) {
      category = { id: document.category.id, label: documentationLabel(document.category, this.locale), subcategories: [] };
      options.push(category);
    }
    const subcategory = document.subcategory;
    if (subcategory && !category.subcategories.some(({ id }) => id === subcategory.id)) {
      category.subcategories.push({ id: subcategory.id, label: documentationLabel(subcategory, this.locale) });
    }
    return options;
  }
}
