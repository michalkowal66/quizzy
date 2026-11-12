import { api } from "@/lib/api";
import type { Folder, FolderInput } from "./types";

export async function listFolders(): Promise<Folder[]> {
  const { data } = await api.get<Folder[]>("/folders");
  return data;
}

export async function createFolder(input: FolderInput): Promise<Folder> {
  const { data } = await api.post<Folder>("/folders", input);
  return data;
}

export async function renameFolder(
  id: string,
  input: FolderInput,
): Promise<Folder> {
  const { data } = await api.put<Folder>(`/folders/${id}`, input);
  return data;
}

export async function deleteFolder(id: string): Promise<void> {
  await api.delete(`/folders/${id}`);
}
