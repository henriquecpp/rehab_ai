// types/prescription.ts
import type { ExerciseDto } from './exercise';

export interface PlanDraftResponse {
  prescriptionId: string;
  userId: string;
  title: string;
  description: string;
  diagnosis: string;
  exercises: ExerciseDto[];
  goals: string[];
  duration: number;
  frequency: string;
  startDate?: string;
  endDate?: string;
  confidenceScore: number;
  modelUsed: string;
  guardrailStatus: string;
}

export interface FileMetadata {
  fileId: string;
  fileName: string;
  fileType: string;
  viewUrl?: string;
  downloadUrl?: string;
}

export interface PrescriptionResponse {
  id: string;
  fileId: string;
  userId: string;
  normalizationId: string;
  originalText: string;
  cleanText: string | null;
  jsonData: string;
  status: string | null;
  failureReason: string | null;
  createdAt: string;
  updatedAt: string | null;
  fileMetadata?: FileMetadata; // Optional file metadata
}

export interface PrescriptionListItem {
  id: string;
  prescriptionText: string;
  modelUsed: string;
  guardrailStatus: "OK" | "BLOCKED";
  createdAt: string;
  userId: string;
}