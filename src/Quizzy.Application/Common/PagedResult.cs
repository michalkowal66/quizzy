namespace Quizzy.Application.Common;

/// <summary>
/// A single page of results together with paging metadata.
/// </summary>
public record PagedResult<T>(
    IReadOnlyList<T> Items,
    int Page,
    int PageSize,
    int TotalCount)
{
    public int TotalPages => PageSize == 0 ? 0 : (int)Math.Ceiling((double)TotalCount / PageSize);
}

/// <summary>
/// Common paging and search parameters for list endpoints.
/// </summary>
public record PageQuery(int Page = 1, int PageSize = 20, string? Search = null)
{
    private const int MaxPageSize = 100;

    public int NormalizedPage => Page < 1 ? 1 : Page;

    public int NormalizedPageSize => PageSize switch
    {
        < 1 => 20,
        > MaxPageSize => MaxPageSize,
        _ => PageSize,
    };
}
