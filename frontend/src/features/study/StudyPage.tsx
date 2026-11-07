import { useEffect, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useDeck, useFlashcards } from "@/features/decks/hooks";
import { getErrorMessage } from "@/lib/problem";
import { logStudy } from "./api";

export function StudyPage() {
  const { deckId = "" } = useParams();
  const deckQuery = useDeck(deckId);
  const flashcardsQuery = useFlashcards(deckId);
  const logStudyMutation = useMutation({ mutationFn: logStudy });

  const [index, setIndex] = useState(0);
  const [showBack, setShowBack] = useState(false);
  const [finished, setFinished] = useState(false);

  // Record study activity once, when the learner reaches the end of the deck.
  const { mutate: recordStudy } = logStudyMutation;
  useEffect(() => {
    if (finished) {
      recordStudy();
    }
  }, [finished, recordStudy]);

  if (flashcardsQuery.isPending || deckQuery.isPending) {
    return <p className="text-muted-foreground">Loading…</p>;
  }

  if (flashcardsQuery.isError) {
    return (
      <p className="text-destructive">
        {getErrorMessage(flashcardsQuery.error)}
      </p>
    );
  }

  const flashcards = flashcardsQuery.data ?? [];
  const deckTitle = deckQuery.data?.title ?? "Deck";

  if (flashcards.length === 0) {
    return (
      <div className="flex flex-col gap-4">
        <p className="text-muted-foreground">
          This deck has no flashcards to study.
        </p>
        <Link
          to={`/decks/${deckId}`}
          className="text-sm underline underline-offset-4"
        >
          Back to deck
        </Link>
      </div>
    );
  }

  const restart = () => {
    setIndex(0);
    setShowBack(false);
    setFinished(false);
  };

  const advance = () => {
    if (index + 1 >= flashcards.length) {
      setFinished(true);
    } else {
      setIndex((current) => current + 1);
      setShowBack(false);
    }
  };

  if (finished) {
    return (
      <div className="mx-auto flex max-w-md flex-col items-center gap-4 py-12 text-center">
        <h1 className="text-2xl font-semibold">Session complete</h1>
        <p className="text-muted-foreground">
          You reviewed {flashcards.length}{" "}
          {flashcards.length === 1 ? "card" : "cards"} from {deckTitle}.
        </p>
        <div className="flex gap-2">
          <Button onClick={restart}>Study again</Button>
          <Link
            to={`/decks/${deckId}`}
            className={buttonVariants({ variant: "outline" })}
          >
            Back to deck
          </Link>
        </div>
      </div>
    );
  }

  const card = flashcards[index];

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <div className="flex items-center justify-between">
        <Link
          to={`/decks/${deckId}`}
          className="text-sm text-muted-foreground underline underline-offset-4"
        >
          ← {deckTitle}
        </Link>
        <span className="text-sm text-muted-foreground">
          {index + 1} / {flashcards.length}
        </span>
      </div>

      <Card className="min-h-60">
        <CardContent className="flex min-h-60 flex-col items-center justify-center gap-3 p-8 text-center">
          <span className="text-xs uppercase tracking-wide text-muted-foreground">
            {showBack ? "Answer" : "Question"}
          </span>
          <p className="text-lg">{showBack ? card.back : card.front}</p>
        </CardContent>
      </Card>

      <div className="flex justify-center gap-2">
        {!showBack ? (
          <Button onClick={() => setShowBack(true)}>Show answer</Button>
        ) : (
          <Button onClick={advance}>
            {index + 1 >= flashcards.length ? "Finish" : "Next card"}
          </Button>
        )}
      </div>
    </div>
  );
}
