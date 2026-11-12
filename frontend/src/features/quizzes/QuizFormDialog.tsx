import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";

import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useFolders } from "@/features/folders/hooks";

const schema = z.object({
  title: z
    .string()
    .trim()
    .min(1, "Title is required.")
    .max(150, "Title is too long."),
  description: z
    .string()
    .trim()
    .max(1000, "Description is too long.")
    .optional(),
  folderId: z.string().optional(),
});

export type QuizFormValues = z.infer<typeof schema>;

interface QuizFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  submitLabel: string;
  defaultValues: QuizFormValues;
  pending: boolean;
  errorMessage: string | null;
  onSubmit: (values: QuizFormValues) => void;
}

export function QuizFormDialog({
  open,
  onOpenChange,
  title,
  submitLabel,
  defaultValues,
  pending,
  errorMessage,
  onSubmit,
}: QuizFormDialogProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<QuizFormValues>({ resolver: zodResolver(schema), defaultValues });

  const foldersQuery = useFolders();
  const folders = foldersQuery.data ?? [];

  return (
    <Dialog open={open} onOpenChange={onOpenChange} title={title}>
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="flex flex-col gap-4"
        noValidate
      >
        <div className="flex flex-col gap-2">
          <Label htmlFor="quiz-title">Title</Label>
          <Input id="quiz-title" {...register("title")} />
          {errors.title && (
            <p className="text-sm text-destructive">{errors.title.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-2">
          <Label htmlFor="quiz-description">Description</Label>
          <Textarea id="quiz-description" rows={3} {...register("description")} />
          {errors.description && (
            <p className="text-sm text-destructive">
              {errors.description.message}
            </p>
          )}
        </div>
        <div className="flex flex-col gap-2">
          <Label htmlFor="quiz-folder">Folder</Label>
          <Select id="quiz-folder" {...register("folderId")}>
            <option value="">No folder</option>
            {folders.map((folder) => (
              <option key={folder.id} value={folder.id}>
                {folder.name}
              </option>
            ))}
          </Select>
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
