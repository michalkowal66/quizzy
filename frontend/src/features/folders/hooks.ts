import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { createFolder, deleteFolder, listFolders, renameFolder } from "./api";
import type { FolderInput } from "./types";

interface SaveFolderArgs {
  id?: string;
  input: FolderInput;
}

export const folderKeys = {
  all: ["folders"] as const,
};

export function useFolders() {
  return useQuery({
    queryKey: folderKeys.all,
    queryFn: listFolders,
  });
}

export function useSaveFolder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: SaveFolderArgs) =>
      id ? renameFolder(id, input) : createFolder(input),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: folderKeys.all }),
  });
}

export function useDeleteFolder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteFolder(id),
    onSuccess: () => {
      // Decks and quizzes keep existing but lose their folder, so refresh them too.
      queryClient.invalidateQueries({ queryKey: folderKeys.all });
      queryClient.invalidateQueries({ queryKey: ["decks"] });
      queryClient.invalidateQueries({ queryKey: ["quizzes"] });
    },
  });
}
