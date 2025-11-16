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
