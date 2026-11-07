/** Mirrors the API's PagedResult<T> envelope returned by list endpoints. */
export interface PagedResult<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalCount: number;
  totalPages: number;
}
