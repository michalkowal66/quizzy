import { Link } from "react-router-dom";
import {
  BookOpen,
  Flame,
  Layers,
  ListChecks,
  Target,
  type LucideIcon,
} from "lucide-react";

import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useAuth } from "@/features/auth/use-auth";
import { getErrorMessage } from "@/lib/problem";
import { cn } from "@/lib/utils";
import { useDashboard } from "./hooks";
import type { DashboardStats } from "./types";

function formatDate(value: string): string {
  return new Date(`${value}T00:00:00Z`).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
    timeZone: "UTC",
  });
}

function streakCaption(stats: DashboardStats): string {
  if (stats.currentStreak === 0) {
    return "Study a deck today to start your streak.";
  }
  if (stats.studiedToday) {
    return "You have studied today. Keep it going tomorrow.";
  }
  return "Study today to keep your streak alive.";
}

export function DashboardPage() {
  const { user } = useAuth();
  const dashboardQuery = useDashboard();

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">
          Welcome back, {user?.displayName}
        </h1>
        <p className="text-muted-foreground">
          Here is how your learning is going.
        </p>
      </div>

      {dashboardQuery.isPending ? (
        <p className="text-muted-foreground">Loading your overview…</p>
      ) : dashboardQuery.isError ? (
        <p className="text-destructive">
          {getErrorMessage(dashboardQuery.error)}
        </p>
      ) : (
        <Overview stats={dashboardQuery.data} />
      )}
    </div>
  );
}

function Overview({ stats }: { stats: DashboardStats }) {
  const active = stats.currentStreak > 0;

  return (
    <div className="flex flex-col gap-6">
      <Card
        className={cn(
          "border-primary/20 bg-primary/5",
          !active && "border-border bg-card",
        )}
      >
        <CardContent className="flex flex-col gap-6 p-6 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-5">
            <Flame
              className={cn(
                "size-12 shrink-0",
                active ? "text-primary" : "text-muted-foreground",
              )}
              {...(active ? { fill: "currentColor" } : {})}
            />
            <div>
              <div className="flex items-baseline gap-2">
                <span className="text-5xl font-semibold tabular-nums leading-none">
                  {stats.currentStreak}
                </span>
                <span className="text-lg text-muted-foreground">
                  {stats.currentStreak === 1 ? "day" : "days"}
                </span>
              </div>
              <p className="mt-2 text-sm text-muted-foreground">
                {streakCaption(stats)}
              </p>
            </div>
          </div>

          <dl className="flex gap-8 sm:flex-col sm:gap-2 sm:text-right">
            <div>
              <dt className="text-xs text-muted-foreground">Longest streak</dt>
              <dd className="font-medium tabular-nums">
                {stats.longestStreak}{" "}
                {stats.longestStreak === 1 ? "day" : "days"}
              </dd>
            </div>
            <div>
              <dt className="text-xs text-muted-foreground">Last activity</dt>
              <dd className="font-medium">
                {stats.lastActivity
                  ? formatDate(stats.lastActivity)
                  : "None yet"}
              </dd>
            </div>
          </dl>
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatTile icon={Layers} label="Decks" value={stats.deckCount} />
        <StatTile
          icon={BookOpen}
          label="Flashcards"
          value={stats.flashcardCount}
        />
        <StatTile icon={ListChecks} label="Quizzes" value={stats.quizCount} />
        <StatTile icon={Target} label="Attempts" value={stats.attemptCount} />
      </div>

      <div className="flex flex-wrap gap-3">
        <Link to="/decks" className={cn(buttonVariants())}>
          Study a deck
        </Link>
        <Link
          to="/quizzes"
          className={cn(buttonVariants({ variant: "outline" }))}
        >
          Take a quiz
        </Link>
      </div>
    </div>
  );
}

interface StatTileProps {
  icon: LucideIcon;
  label: string;
  value: number;
}

function StatTile({ icon: Icon, label, value }: StatTileProps) {
  return (
    <Card>
      <CardContent className="flex items-center gap-4 p-5">
        <span className="flex size-10 shrink-0 items-center justify-center rounded-md bg-secondary text-secondary-foreground">
          <Icon className="size-5" />
        </span>
        <div>
          <div className="text-2xl font-semibold tabular-nums leading-none">
            {value}
          </div>
          <div className="mt-1 text-sm text-muted-foreground">{label}</div>
        </div>
      </CardContent>
    </Card>
  );
}
