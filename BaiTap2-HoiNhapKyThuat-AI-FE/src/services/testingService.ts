import { apiRequest } from '../api/client'
import type { CreateTestRequest, FinishTestRequest, TestSession } from '../types/testing'

export function createTest(request: CreateTestRequest, token: string): Promise<TestSession> {
  return apiRequest<TestSession>('/api/v1/tests', {
    method: 'POST',
    body: request,
    token,
  })
}

export function getTest(testId: number, token: string): Promise<TestSession> {
  return apiRequest<TestSession>(`/api/v1/tests/${testId}`, {
    token,
  })
}

export function finishTest(testId: number, request: FinishTestRequest, token: string): Promise<TestSession> {
  return apiRequest<TestSession>(`/api/v1/tests/${testId}/finish`, {
    method: 'POST',
    body: request,
    token,
  })
}

export function getTestResult(testId: number, token: string): Promise<TestSession> {
  return apiRequest<TestSession>(`/api/v1/tests/${testId}/result`, {
    token,
  })
}
