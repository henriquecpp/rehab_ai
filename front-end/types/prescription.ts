import type { ExerciseDto } from './exercise'

export interface PlanDraftResponse {
  prescriptionId: string
  userId: string
  title: string
  description: string
  diagnosis: string
  exercises: ExerciseDto[]
  goals: string[]
  duration: number
  frequency: string
  startDate: string
  endDate: string
  confidenceScore: number
  modelUsed: string
  guardrailStatus: string
}
