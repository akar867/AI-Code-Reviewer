export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';
export type FindingSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface ReviewSummary {
  id: number;
  githubPrId: number;
  repository: string;
  prNumber: number;
  title: string;
  author: string;
  riskScore: number;
  riskLevel: RiskLevel;
  summary: string;
  findings: number;
  createdAt: string;
  updatedAt: string;
}

export interface Finding {
  id: number;
  category: string;
  severity: FindingSeverity;
  filePath: string;
  lineNumber: number | null;
  message: string;
  suggestion: string;
}

export interface TestSuggestion {
  id: number;
  filePath: string;
  framework: string;
  description: string;
  example: string;
}

export interface ReviewDetail {
  summary: ReviewSummary;
  findings: Finding[];
  testSuggestions: TestSuggestion[];
}
