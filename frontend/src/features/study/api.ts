import { api } from "@/lib/api";

/** Records that the current user studied today (counts toward the daily streak). */
export async function logStudy(): Promise<void> {
  await api.post("/study/log");
}
