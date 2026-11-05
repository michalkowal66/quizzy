using Quizzy.Application.Common;

namespace Quizzy.UnitTests.Common;

public class PageQueryTests
{
    [Theory]
    [InlineData(0, 1)]
    [InlineData(-5, 1)]
    [InlineData(1, 1)]
    [InlineData(7, 7)]
    public void NormalizedPage_ClampsBelowOneToOne(int page, int expected)
    {
        var query = new PageQuery(page, 20);

        Assert.Equal(expected, query.NormalizedPage);
    }

    [Theory]
    [InlineData(0, 20)]
    [InlineData(-1, 20)]
    [InlineData(50, 50)]
    [InlineData(100, 100)]
    [InlineData(1000, 100)]
    public void NormalizedPageSize_ClampsToAllowedRange(int pageSize, int expected)
    {
        var query = new PageQuery(1, pageSize);

        Assert.Equal(expected, query.NormalizedPageSize);
    }

    [Fact]
    public void TotalPages_IsCeilingOfCountOverPageSize()
    {
        var result = new PagedResult<int>(new[] { 1, 2, 3 }, 1, 2, 5);

        Assert.Equal(3, result.TotalPages);
    }
}
