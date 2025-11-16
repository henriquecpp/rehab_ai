import type { PlanDraftResponse } from './prescription'

export type PlanStatus = 'DRAFT' | 'APPROVED' | 'ARCHIVED'

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
  userId: string
  prescriptionId: string
  planData: string
}

export type EditablePlan = PlanDraftResponse