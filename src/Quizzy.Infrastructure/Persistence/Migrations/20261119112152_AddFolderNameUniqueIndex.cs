using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Quizzy.Infrastructure.Persistence.Migrations
{
    /// <inheritdoc />
    public partial class AddFolderNameUniqueIndex : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_folders_OwnerId_Name",
                table: "folders");

            migrationBuilder.CreateIndex(
                name: "IX_folders_OwnerId_Name",
                table: "folders",
                columns: new[] { "OwnerId", "Name" },
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_folders_OwnerId_Name",
                table: "folders");

            migrationBuilder.CreateIndex(
                name: "IX_folders_OwnerId_Name",
                table: "folders",
                columns: new[] { "OwnerId", "Name" });
        }
    }
}
