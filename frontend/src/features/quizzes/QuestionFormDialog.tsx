import { useState, type FormEvent } from "react";
import { Plus, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";
import {
  questionTypeLabels,
  type OptionInput,
  type QuestionInput,
  type QuestionType,
} from "./types";

const MIN_OPTIONS = 2;

const typeOrder: QuestionType[] = [
  "SingleChoice",
  "MultipleChoice",
  "TrueFalse",
];

function trueFalseOptions(correct: "True" | "False"): OptionInput[] {
  return [
    { text: "True", isCorrect: correct === "True" },
    { text: "False", isCorrect: correct === "False" },
  ];
}

function validate(
  text: string,
  type: QuestionType,
  options: OptionInput[],
): string | null {
  if (!text.trim()) {
    return "Question text is required.";
  }
  if (type !== "TrueFalse") {
    if (options.length < MIN_OPTIONS) {
      return "Add at least two options.";
    }
    if (options.some((option) => !option.text.trim())) {
      return "Every option needs text.";
    }
  }
  const correctCount = options.filter((option) => option.isCorrect).length;
  if (type === "MultipleChoice") {
    if (correctCount < 1) {
      return "Mark at least one correct option.";
    }
  } else if (correctCount !== 1) {
    return "Mark exactly one correct option.";
  }
  return null;
}

interface QuestionFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  submitLabel: string;
  defaultValues: QuestionInput;
  pending: boolean;
  errorMessage: string | null;
  onSubmit: (values: QuestionInput) => void;
}

export function QuestionFormDialog({
  open,
  onOpenChange,
  title,
  submitLabel,
  defaultValues,
  pending,
  errorMessage,
  onSubmit,
}: QuestionFormDialogProps) {
  const [text, setText] = useState(defaultValues.text);
  const [type, setType] = useState<QuestionType>(defaultValues.type);
  const [options, setOptions] = useState<OptionInput[]>(defaultValues.options);
  const [validationError, setValidationError] = useState<string | null>(null);

  const changeType = (next: QuestionType) => {
    if (next === type) {
      return;
    }
    setType(next);
    if (next === "TrueFalse") {
      setOptions(trueFalseOptions("True"));
    } else if (type === "TrueFalse") {
      setOptions([
        { text: "", isCorrect: false },
        { text: "", isCorrect: false },
      ]);
    } else if (next === "SingleChoice") {
      // Keep at most one correct option when narrowing to a single answer.
      setOptions((current) => {
        const firstCorrect = current.findIndex((option) => option.isCorrect);
        return current.map((option, i) => ({
          ...option,
          isCorrect: i === firstCorrect,
        }));
      });
    }
  };

  const setOptionText = (index: number, value: string) => {
    setOptions((current) =>
      current.map((option, i) =>
        i === index ? { ...option, text: value } : option,
      ),
    );
  };

  const toggleCorrect = (index: number) => {
    setOptions((current) =>
      current.map((option, i) => {
        if (type === "MultipleChoice") {
          return i === index ? { ...option, isCorrect: !option.isCorrect } : option;
        }
        // Single choice and true/false allow only one correct option.
        return { ...option, isCorrect: i === index };
      }),
    );
  };

  const addOption = () => {
    setOptions((current) => [...current, { text: "", isCorrect: false }]);
  };

  const removeOption = (index: number) => {
    setOptions((current) => current.filter((_, i) => i !== index));
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    const error = validate(text, type, options);
    if (error) {
      setValidationError(error);
      return;
    }
    setValidationError(null);
    onSubmit({ text: text.trim(), type, options });
  };

  const isTrueFalse = type === "TrueFalse";

  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
      title={title}
      className="max-w-lg"
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
        <div className="flex flex-col gap-2">
          <Label htmlFor="question-text">Question</Label>
          <Textarea
            id="question-text"
            rows={2}
            value={text}
            onChange={(event) => setText(event.target.value)}
          />
        </div>

        <div className="flex flex-col gap-2">
          <Label>Type</Label>
          <div className="flex gap-1 rounded-md border p-1">
            {typeOrder.map((option) => (
              <button
                key={option}
                type="button"
                onClick={() => changeType(option)}
                className={cn(
                  "flex-1 rounded px-2 py-1.5 text-xs font-medium transition-colors",
                  type === option
                    ? "bg-secondary text-secondary-foreground"
                    : "text-muted-foreground hover:text-foreground",
                )}
              >
                {questionTypeLabels[option]}
              </button>
            ))}
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <div className="flex items-center justify-between">
            <Label>Options</Label>
            <span className="text-xs text-muted-foreground">
              {type === "MultipleChoice"
                ? "Select all correct answers"
                : "Select the correct answer"}
            </span>
          </div>
          <div className="flex flex-col gap-2">
            {options.map((option, index) => (
              <div key={index} className="flex items-center gap-2">
                <input
                  type={type === "MultipleChoice" ? "checkbox" : "radio"}
                  name="correct-option"
                  checked={option.isCorrect}
                  onChange={() => toggleCorrect(index)}
                  className="size-4 shrink-0 accent-primary"
                  aria-label="Correct answer"
                />
                {isTrueFalse ? (
                  <span className="flex-1 text-sm">{option.text}</span>
                ) : (
                  <Input
                    value={option.text}
                    placeholder={`Option ${index + 1}`}
                    onChange={(event) => setOptionText(index, event.target.value)}
                  />
                )}
                {!isTrueFalse && options.length > MIN_OPTIONS && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => removeOption(index)}
                    aria-label="Remove option"
                  >
                    <Trash2 />
                  </Button>
                )}
              </div>
            ))}
          </div>
          {!isTrueFalse && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              className="self-start"
              onClick={addOption}
            >
              <Plus /> Add option
            </Button>
          )}
        </div>

        {(validationError || errorMessage) && (
          <p className="text-sm text-destructive">
            {validationError ?? errorMessage}
          </p>
        )}

        <div className="flex justify-end gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={pending}>
            {pending ? "Saving…" : submitLabel}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
