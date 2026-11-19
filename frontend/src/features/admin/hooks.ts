import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { changeUserRole, deleteUser, getUser, listUsers } from "./api";
import type { UserRole } from "./types";

export const userKeys = {
  all: ["admin", "users"] as const,
  detail: (id: string) => ["admin", "users", id] as const,
};

export function useUsers() {
  return useQuery({
    queryKey: userKeys.all,
    queryFn: listUsers,
  });
}

export function useUser(id: string) {
  return useQuery({
    queryKey: userKeys.detail(id),
    queryFn: () => getUser(id),
  });
}

export function useChangeUserRole() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, role }: { id: string; role: UserRole }) =>
      changeUserRole(id, role),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: userKeys.all }),
  });
}

export function useDeleteUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteUser(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: userKeys.all }),
  });
}
