import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";

import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

const schema = z.object({
  front: z
    .string()
    .trim()
    .min(1, "Front is required.")
    .max(1000, "Front is too long."),
  back: z
    .string()
    .trim()
    .min(1, "Back is required.")
    .max(1000, "Back is too long."),
});

export type FlashcardFormValues = z.infer<typeof schema>;

interface FlashcardFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  submitLabel: string;
  defaultValues: FlashcardFormValues;
  pending: boolean;
  errorMessage: string | null;
  onSubmit: (values: FlashcardFormValues) => void;
}

export function FlashcardFormDialog({
  open,
  onOpenChange,
  title,
  submitLabel,
  defaultValues,
  pending,
  errorMessage,
  onSubmit,
}: FlashcardFormDialogProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FlashcardFormValues>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange} title={title}>
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="flex flex-col gap-4"
        noValidate
      >
        <div className="flex flex-col gap-2">
          <Label htmlFor="flashcard-front">Front</Label>
          <Textarea id="flashcard-front" rows={3} {...register("front")} />
          {errors.front && (
            <p className="text-sm text-destructive">{errors.front.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-2">
          <Label htmlFor="flashcard-back">Back</Label>
          <Textarea id="flashcard-back" rows={3} {...register("back")} />
          {errors.back && (
            <p className="text-sm text-destructive">{errors.back.message}</p>
          )}
        </div>
        {errorMessage && (
          <p className="text-sm text-destructive">{errorMessage}</p>
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
