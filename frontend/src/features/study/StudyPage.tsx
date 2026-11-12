import { useEffect, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useDeck, useFlashcards } from "@/features/decks/hooks";
import { getErrorMessage } from "@/lib/problem";
import { cn } from "@/lib/utils";
import { logStudy } from "./api";

export function StudyPage() {
  const { deckId = "" } = useParams();
  const deckQuery = useDeck(deckId);
  const flashcardsQuery = useFlashcards(deckId);
  const logStudyMutation = useMutation({ mutationFn: logStudy });

  const [index, setIndex] = useState(0);
  const [flipped, setFlipped] = useState(false);
  // The furthest card whose answer has been revealed; gates moving forward.
  const [maxRevealed, setMaxRevealed] = useState(-1);
  const [finished, setFinished] = useState(false);

  // Record study activity once, when the learner finishes the deck.
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
    setFlipped(false);
    setMaxRevealed(-1);
    setFinished(false);
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
  const isLast = index === flashcards.length - 1;
  const currentRevealed = index <= maxRevealed;
  const canGoBack = index > 0;
  const canGoForward = currentRevealed;

  const flip = () => {
    setFlipped((previous) => {
      const next = !previous;
      if (next) {
        setMaxRevealed((furthest) => Math.max(furthest, index));
      }
      return next;
    });
  };

  const goTo = (nextIndex: number) => {
    setIndex(nextIndex);
    setFlipped(false);
  };

  const goNext = () => {
    if (!canGoForward) {
      return;
    }
    if (isLast) {
      setFinished(true);
    } else {
      goTo(index + 1);
    }
  };

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

      <button
        type="button"
        onClick={flip}
        aria-label={flipped ? "Show the question" : "Show the answer"}
        className="w-full rounded-xl text-left [perspective:1000px] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
      >
        <div
          className={cn(
            "relative min-h-60 transition-transform duration-500 transform-3d motion-reduce:transition-none",
            flipped && "rotate-y-180",
          )}
        >
          <Card className="absolute inset-0 backface-hidden">
            <CardContent className="flex h-full flex-col items-center justify-center gap-3 p-8 text-center">
              <span className="text-xs uppercase tracking-wide text-muted-foreground">
                Question
              </span>
              <p className="text-lg">{card.front}</p>
            </CardContent>
          </Card>
          <Card className="absolute inset-0 rotate-y-180 backface-hidden">
            <CardContent className="flex h-full flex-col items-center justify-center gap-3 p-8 text-center">
              <span className="text-xs uppercase tracking-wide text-muted-foreground">
                Answer
              </span>
              <p className="text-lg">{card.back}</p>
            </CardContent>
          </Card>
        </div>
      </button>

      <div className="flex items-center justify-between gap-2">
        <Button
          variant="outline"
          onClick={() => goTo(index - 1)}
          disabled={!canGoBack}
        >
          Previous
        </Button>
        <Button variant={currentRevealed ? "outline" : "default"} onClick={flip}>
          {flipped ? "Show question" : "Show answer"}
        </Button>
        <Button onClick={goNext} disabled={!canGoForward}>
          {isLast ? "Finish" : "Next"}
        </Button>
      </div>
    </div>
  );
}
