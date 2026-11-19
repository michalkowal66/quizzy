import { api } from "@/lib/api";
import type { UserDetail, UserRole, UserSummary } from "./types";

export async function listUsers(): Promise<UserSummary[]> {
  const { data } = await api.get<UserSummary[]>("/admin/users");
  return data;
}

export async function getUser(id: string): Promise<UserDetail> {
  const { data } = await api.get<UserDetail>(`/admin/users/${id}`);
  return data;
}

export async function changeUserRole(
  id: string,
  role: UserRole,
): Promise<UserSummary> {
  const { data } = await api.put<UserSummary>(`/admin/users/${id}/role`, {
    role,
  });
  return data;
}

export async function deleteUser(id: string): Promise<void> {
  await api.delete(`/admin/users/${id}`);
}
