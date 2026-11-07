import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuth } from '@/features/auth/use-auth'

export function DashboardPage() {
  const { user } = useAuth()

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Welcome back, {user?.displayName}</h1>
        <p className="text-muted-foreground">Your learning overview will appear here.</p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Getting started</CardTitle>
          <CardDescription>
            Create flashcard decks and quizzes, then track your daily streak.
          </CardDescription>
        </CardHeader>
      </Card>
    </div>
  )
}
