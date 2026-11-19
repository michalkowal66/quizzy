import { Link, useParams } from "react-router-dom";

import { Card, CardContent } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/problem";
import { useUser } from "./hooks";

export function AdminUserDetailPage() {
  const { userId } = useParams();
  const userQuery = useUser(userId!);

  if (userQuery.isPending) {
    return <p className="text-muted-foreground">Loading user…</p>;
  }

  if (userQuery.isError) {
    return <p className="text-destructive">{getErrorMessage(userQuery.error)}</p>;
  }

  const user = userQuery.data;
  const stats = [
    { label: "Current streak", value: `${user.currentStreak} days` },
    { label: "Longest streak", value: `${user.longestStreak} days` },
    { label: "Decks", value: user.deckCount },
    { label: "Flashcards", value: user.flashcardCount },
    { label: "Quizzes", value: user.quizCount },
    { label: "Attempts", value: user.attemptCount },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to="/admin/users"
          className="text-sm text-muted-foreground hover:underline"
        >
          ← Back to users
        </Link>
        <h1 className="mt-2 text-2xl font-semibold">{user.displayName}</h1>
        <p className="text-muted-foreground">
          {user.email} · {user.role} · joined{" "}
          {new Date(user.createdAt).toLocaleDateString()}
        </p>
        {user.lastActivity && (
          <p className="text-sm text-muted-foreground">
            Last activity {new Date(user.lastActivity).toLocaleDateString()}
          </p>
        )}
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {stats.map((stat) => (
          <Card key={stat.label}>
            <CardContent className="p-5">
              <p className="text-sm text-muted-foreground">{stat.label}</p>
              <p className="mt-1 text-2xl font-semibold">{stat.value}</p>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
