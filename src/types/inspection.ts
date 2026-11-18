export type CategoryId = 'handbag' | 'watch' | 'footwear' | 'jewelry' | 'custom';

export type ReticleType =
  | 'stitch-line'
  | 'typography-band'
  | 'hardware-plate'
  | 'serial-tag'
  | 'dial-circle'
  | 'general-macro';

export interface BoundingBoxSpec {
  aspectRatioLabel: string;
  aspectRatioValue: number;
  targetFillMinPercent: number;
  targetFillMaxPercent: number;
  alignmentRule: string;
  reticleType: ReticleType;
}

export interface MarkerSpec {
  id: string;
  stepNumber: number;
  title: string;
  focusFeature: string;
  weight: number;
  lightingRequirement: string;
  angleAndDistance: string;
  targetConfirmations: string[];
  targetAnomalies: string[];
  boundingBox: BoundingBoxSpec;
  sampleImageUrl?: string;
  sampleLabel?: string;
}

export interface ClientOpticalMetrics {
  averageBrightness: number;
  sharpnessIndex: number;
  width: number;
  height: number;
  captureSource: 'camera' | 'upload' | 'sample';
  zoomLevel: number;
}

export interface MarkerAssessment {
  markerId: string;
  authenticityScore: number;
  confidenceScore: number;
  framingScore: number;
  lightingScore: number;
  sharpnessScore: number;
  qualityPassed: boolean;
  retakeRecommended: boolean;
  retakeAngleInstruction: string | null;
  qualityPenaltyReason: string | null;
  confirmations: string[];
  anomalies: string[];
  humanJudgementNote: string;
  summary: string;
  assessedAt: string;
}

export interface CapturedMarkerState {
  markerId: string;
  imageDataUrl: string | null;
  clientMetrics: ClientOpticalMetrics | null;
  assessment: MarkerAssessment | null;
  retakeCount: number;
  keptLowConfidence: boolean;
}

export interface FinalScorecard {
  overallProbabilityScore: number;
  overallConfidenceScore: number;
  verdictBand:
    | 'High Visual Consistency'
    | 'Moderate Visual Consistency'
    | 'Inconclusive — Retake or Human Audit Needed'
    | 'Elevated Anomaly Signal';
  executiveSummary: string;
  lowConfidenceMarkers: string[];
  prioritizedHumanChecks: string[];
  disclaimer: string;
  generatedAt: string;
}

export interface SavedInspectionRecord {
  id: string;
  category: CategoryId;
  itemName: string;
  markers: MarkerSpec[];
  captures: Record<string, CapturedMarkerState>;
  scorecard: FinalScorecard;
  savedAt: string;
}
