import { Link } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/use-auth";
import { getErrorMessage } from "@/lib/problem";
import { cn } from "@/lib/utils";
import { useChangeUserRole, useDeleteUser, useUsers } from "./hooks";
import type { UserSummary } from "./types";

function RoleBadge({ role }: { role: UserSummary["role"] }) {
  return (
    <span
      className={cn(
        "inline-flex rounded-full px-2 py-0.5 text-xs font-medium",
        role === "Admin"
          ? "bg-primary/10 text-primary"
          : "bg-secondary text-secondary-foreground",
      )}
    >
      {role}
    </span>
  );
}

export function AdminUsersPage() {
  const { user } = useAuth();
  const usersQuery = useUsers();
  const changeRole = useChangeUserRole();
  const deleteUser = useDeleteUser();

  const handleToggleRole = (target: UserSummary) => {
    changeRole.mutate({
      id: target.id,
      role: target.role === "Admin" ? "User" : "Admin",
    });
  };

  const handleDelete = (target: UserSummary) => {
    if (
      !window.confirm(
        `Delete ${target.email}? This permanently removes the account along with its decks, quizzes and progress.`,
      )
    ) {
      return;
    }
    deleteUser.mutate(target.id);
  };

  const users = usersQuery.data ?? [];
  const pending = changeRole.isPending || deleteUser.isPending;
  const actionError = changeRole.isError
    ? getErrorMessage(changeRole.error)
    : deleteUser.isError
      ? getErrorMessage(deleteUser.error)
      : null;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Users</h1>
        <p className="text-muted-foreground">
          Manage accounts and their access level.
        </p>
      </div>

      {actionError && <p className="text-destructive">{actionError}</p>}

      {usersQuery.isPending ? (
        <p className="text-muted-foreground">Loading users…</p>
      ) : usersQuery.isError ? (
        <p className="text-destructive">{getErrorMessage(usersQuery.error)}</p>
      ) : (
        <div className="overflow-x-auto rounded-xl border">
          <table className="w-full text-sm">
            <thead className="border-b bg-muted/50 text-left text-muted-foreground">
              <tr>
                <th className="px-4 py-3 font-medium">User</th>
                <th className="px-4 py-3 font-medium">Role</th>
                <th className="px-4 py-3 font-medium">Joined</th>
                <th className="px-4 py-3 text-right font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((row) => {
                const isSelf = row.id === user?.userId;
                return (
                  <tr key={row.id} className="border-b last:border-0">
                    <td className="px-4 py-3">
                      <Link
                        to={`/admin/users/${row.id}`}
                        className="font-medium hover:underline"
                      >
                        {row.displayName}
                      </Link>
                      <div className="text-xs text-muted-foreground">
                        {row.email}
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <RoleBadge role={row.role} />
                    </td>
                    <td className="px-4 py-3 text-muted-foreground">
                      {new Date(row.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleToggleRole(row)}
                          disabled={isSelf || pending}
                        >
                          {row.role === "Admin" ? "Make user" : "Make admin"}
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(row)}
                          disabled={isSelf || pending}
                        >
                          Delete
                        </Button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
