import { CrudService } from './crud.service';

export abstract class CrudFilterService<T> extends CrudService<T> {
  // getFiltered(
  //   filters: Partial<T>,
  //   event?: TableLazyLoadEvent,
  // ): Observable<PaginatedList<T>> {
  //   this.setLoadingState('get', true);
  //   const pageSortParams = fnParseTableEventToParams(event);
  //   const fullFilters = {
  //     ...filters,
  //     ...pageSortParams,
  //   };
  //   const params = fnBuildHttpParams(fullFilters);
  //   return this.http
  //     .get<PaginatedList<T>>(`${this.BASE_URI}`, { params })
  //     .pipe(finalize(() => this.setLoadingState('get', false)));
  // }
  //
  // exportToExcel(filters: Partial<T>, fallbackName = 'exports.xlsx'): Observable<FileWithBlob> {
  //   const params = fnBuildHttpParams(filters);
  //   const headers = new HttpHeaders({
  //     Accept: MIME.XLSX,
  //   });
  //   return this.http
  //     .get(`${this.BASE_URI}/export-to-excel`, {
  //       observe: 'response',
  //       responseType: 'blob',
  //       params,
  //       headers,
  //     })
  //     .pipe(
  //       take(1),
  //       map((res: HttpResponse<Blob>) => {
  //         const cd = res.headers.get('Content-Disposition') ?? '';
  //         const fileName = decodeURIComponent(
  //           cd.match(/filename\*=UTF-8''([^;]+)/i)?.[1] ??
  //             cd.match(/filename="?([^"]+)"?/i)?.[1] ??
  //             fallbackName,
  //         );
  //         return { blob: res.body!, mime: MIME.XLSX, fileName: fileName };
  //       }),
  //     );
  // }
}
