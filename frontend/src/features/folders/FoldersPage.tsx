import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { getErrorMessage } from "@/lib/problem";
import { FolderFormDialog, type FolderFormValues } from "./FolderFormDialog";
import { useDeleteFolder, useFolders, useSaveFolder } from "./hooks";
import type { Folder } from "./types";

export function FoldersPage() {
  const foldersQuery = useFolders();
  const saveFolder = useSaveFolder();
  const deleteFolder = useDeleteFolder();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<Folder | null>(null);
  const [pendingDelete, setPendingDelete] = useState<Folder | null>(null);

  const openCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const openEdit = (folder: Folder) => {
    setEditing(folder);
    setDialogOpen(true);
  };

  const handleSave = (values: FolderFormValues) => {
    saveFolder.mutate(
      { id: editing?.id, input: values },
      { onSuccess: () => setDialogOpen(false) },
    );
  };

  const openDelete = (folder: Folder) => {
    deleteFolder.reset();
    setPendingDelete(folder);
  };

  const confirmDelete = () => {
    if (!pendingDelete) {
      return;
    }
    deleteFolder.mutate(pendingDelete.id, {
      onSuccess: () => setPendingDelete(null),
    });
  };

  const folders = foldersQuery.data ?? [];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">Folders</h1>
          <p className="text-muted-foreground">
            Group your decks and quizzes into folders.
          </p>
        </div>
        <Button onClick={openCreate}>New folder</Button>
      </div>

      {foldersQuery.isPending ? (
        <p className="text-muted-foreground">Loading folders…</p>
      ) : foldersQuery.isError ? (
        <p className="text-destructive">{getErrorMessage(foldersQuery.error)}</p>
      ) : folders.length === 0 ? (
        <p className="text-muted-foreground">
          No folders yet. Create one to organise your material.
        </p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {folders.map((folder) => (
            <Card key={folder.id}>
              <CardContent className="flex items-start justify-between gap-3 p-5">
                <div>
                  <p className="font-medium">{folder.name}</p>
                  <span className="text-xs text-muted-foreground">
                    {folder.deckCount}{" "}
                    {folder.deckCount === 1 ? "deck" : "decks"} ·{" "}
                    {folder.quizCount}{" "}
                    {folder.quizCount === 1 ? "quiz" : "quizzes"}
                  </span>
                </div>
                <div className="flex gap-1">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => openEdit(folder)}
                  >
                    Rename
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => openDelete(folder)}
                    disabled={deleteFolder.isPending}
                  >
                    Delete
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {dialogOpen && (
        <FolderFormDialog
          open={dialogOpen}
          onOpenChange={setDialogOpen}
          title={editing ? "Rename folder" : "New folder"}
          submitLabel={editing ? "Save" : "Create"}
          defaultValues={{ name: editing?.name ?? "" }}
          pending={saveFolder.isPending}
          errorMessage={saveFolder.isError ? getErrorMessage(saveFolder.error) : null}
          onSubmit={handleSave}
        />
      )}

      <ConfirmDialog
        open={pendingDelete !== null}
        onOpenChange={(open) => {
          if (!open) {
            setPendingDelete(null);
          }
        }}
        title="Delete folder"
        description="Delete this folder? Its decks and quizzes are kept but moved out of it."
        confirmLabel="Delete"
        destructive
        pending={deleteFolder.isPending}
        errorMessage={
          deleteFolder.isError ? getErrorMessage(deleteFolder.error) : null
        }
        onConfirm={confirmDelete}
      />
    </div>
  );
}
