import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";

import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const schema = z.object({
  name: z.string().trim().min(1, "Name is required.").max(100, "Name is too long."),
});

export type FolderFormValues = z.infer<typeof schema>;

interface FolderFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  submitLabel: string;
  defaultValues: FolderFormValues;
  pending: boolean;
  errorMessage: string | null;
  onSubmit: (values: FolderFormValues) => void;
}

export function FolderFormDialog({
  open,
  onOpenChange,
  title,
  submitLabel,
  defaultValues,
  pending,
  errorMessage,
  onSubmit,
}: FolderFormDialogProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FolderFormValues>({
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
          <Label htmlFor="folder-name">Name</Label>
          <Input id="folder-name" {...register("name")} />
          {errors.name && (
            <p className="text-sm text-destructive">{errors.name.message}</p>
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
