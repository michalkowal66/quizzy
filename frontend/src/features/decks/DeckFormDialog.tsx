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

export type DeckFormValues = z.infer<typeof schema>;

interface DeckFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  submitLabel: string;
  defaultValues: DeckFormValues;
  pending: boolean;
  errorMessage: string | null;
  onSubmit: (values: DeckFormValues) => void;
}

export function DeckFormDialog({
  open,
  onOpenChange,
  title,
  submitLabel,
  defaultValues,
  pending,
  errorMessage,
  onSubmit,
}: DeckFormDialogProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<DeckFormValues>({ resolver: zodResolver(schema), defaultValues });

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
          <Label htmlFor="deck-title">Title</Label>
          <Input id="deck-title" {...register("title")} />
          {errors.title && (
            <p className="text-sm text-destructive">{errors.title.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-2">
          <Label htmlFor="deck-description">Description</Label>
          <Textarea
            id="deck-description"
            rows={3}
            {...register("description")}
          />
          {errors.description && (
            <p className="text-sm text-destructive">
              {errors.description.message}
            </p>
          )}
        </div>
        <div className="flex flex-col gap-2">
          <Label htmlFor="deck-folder">Folder</Label>
          <Select id="deck-folder" {...register("folderId")}>
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
