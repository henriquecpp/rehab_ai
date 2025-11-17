// types/plan.ts
import type { ExerciseDto } from "./exercise";

export type PlanStatus = "DRAFT" | "APPROVED" | "ARCHIVED";

export interface PlanDataStructure {
  title: string;
  description: string;
  diagnosis: string;
  exercises: ExerciseDto[];
  goals: string[];
  notes?: string;
}

export interface PlanResponse {
  id: string
  userId: string
  prescriptionId: string
  version: number
  planData: string
  status: PlanStatus
  createdAt: string
  updatedAt: string
}

export interface CreatePlanRequest {
  userId: string;
  prescriptionId?: string | null;
  planData: string;
  origin: "AI_GENERATED" | "CLINICIAN_CREATED" | "AI_REVIEWED_BY_THERAPIST";
  confidenceScore?: number;
  priority?: "LOW" | "MEDIUM" | "HIGH";
  painLevelStart?: number;
  painLevelExpectedEnd?: number;
  startDate?: string;
  endDate?: string;
  tags?: string[];
  active?: boolean;
}
