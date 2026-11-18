import {
  AlertTriangle,
  ArrowRight,
  Check,
  Eye,
  Lightbulb,
  RotateCcw,
  ShieldAlert,
} from 'lucide-react';
import type { CapturedMarkerState, MarkerSpec } from '../types/inspection.ts';

interface MarkerInspectorPanelProps {
  marker: MarkerSpec;
  captureState: CapturedMarkerState;
  isAnalyzing: boolean;
  analysisError: string | null;
  hasNextStep: boolean;
  allStepsCaptured: boolean;
  onRetakeStep: () => void;
  onKeepLowConfidence: () => void;
  onNextStep: () => void;
  onFinalizeScorecard: () => void;
}

export function MarkerInspectorPanel({
  marker,
  captureState,
  isAnalyzing,
  analysisError,
  hasNextStep,
  allStepsCaptured,
  onRetakeStep,
  onKeepLowConfidence,
  onNextStep,
  onFinalizeScorecard,
}: MarkerInspectorPanelProps) {
  const assessment = captureState.assessment;

  return (
    <aside className="space-y-5">
      {/* Capture Specification Card */}
      <div className="bg-white border border-[#E2DDD2] rounded-2xl p-5 sm:p-6">
        <div className="flex items-baseline justify-between gap-4 pb-4 border-b border-[#E2DDD2]">
          <div>
            <p className="text-xs font-medium text-[#9A6B2F]">
              Protocol Step 0{marker.stepNumber} · Weight {Math.round(marker.weight * 100)}%
            </p>
            <h3 className="text-xl font-serif font-semibold text-[#181715] mt-0.5">
              Capture Specification
            </h3>
          </div>
          <span className="text-xs font-mono tabular-nums text-[#5C5850]">
            {marker.boundingBox.targetFillMinPercent}–{marker.boundingBox.targetFillMaxPercent}% Fill
          </span>
        </div>

        <dl className="mt-4 space-y-3.5 text-sm">
          <div>
            <dt className="text-xs font-medium text-[#5C5850]">Visual Target</dt>
            <dd className="text-[#181715] mt-0.5 leading-relaxed">{marker.focusFeature}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-[#5C5850] flex items-center gap-1.5">
              <Lightbulb className="w-3.5 h-3.5 text-[#9A6B2F]" />
              <span>Lighting Standard</span>
            </dt>
            <dd className="text-[#181715] mt-0.5 leading-relaxed">{marker.lightingRequirement}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-[#5C5850] flex items-center gap-1.5">
              <Eye className="w-3.5 h-3.5 text-[#9A6B2F]" />
              <span>Angle &amp; Bounding Box Fit</span>
            </dt>
            <dd className="text-[#181715] mt-0.5 leading-relaxed">{marker.angleAndDistance}</dd>
          </div>
        </dl>

        <div className="mt-5 pt-4 border-t border-[#E2DDD2] grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div>
            <p className="font-semibold text-[#1D6A4E] mb-1.5">Visual Confirmations</p>
            <ul className="space-y-1 text-[#5C5850]">
              {marker.targetConfirmations.map((item) => (
                <li key={item} className="leading-snug">
                  · {item}
                </li>
              ))}
            </ul>
          </div>
          <div>
            <p className="font-semibold text-[#B44332] mb-1.5">Anomaly Signals</p>
            <ul className="space-y-1 text-[#5C5850]">
              {marker.targetAnomalies.map((item) => (
                <li key={item} className="leading-snug">
                  · {item}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>

      {/* Analysis Error State */}
      {analysisError && (
        <div className="bg-[#FDF3F1] border border-[#F1CCC5] rounded-2xl p-4 text-sm text-[#8E2F20]">
          <p className="font-semibold">Macro Scoring Error</p>
          <p className="mt-1 text-xs leading-relaxed">{analysisError}</p>
        </div>
      )}

      {/* Loading State */}
      {isAnalyzing && (
        <div className="bg-white border border-[#E2DDD2] rounded-2xl p-6">
          <p className="text-sm font-medium text-[#181715]">
            Evaluating macro geometry, lighting, and craft markers...
          </p>
          <p className="text-xs text-[#5C5850] mt-1">
            Checking bounding-box proportion, micro-detail sharpness, and feature anomalies.
          </p>
          <div className="mt-4 h-1.5 w-full bg-[#EFEAE0] rounded-full overflow-hidden">
            <div className="h-full w-2/3 bg-[#9A6B2F] animate-pulse rounded-full" />
          </div>
        </div>
      )}

      {/* Marker Assessment Result */}
      {assessment && !isAnalyzing && (
        <div className="bg-white border border-[#E2DDD2] rounded-2xl p-5 sm:p-6 space-y-5">
          {/* Score Header */}
          <div className="grid grid-cols-2 gap-4 pb-4 border-b border-[#E2DDD2]">
            <div>
              <p className="text-xs text-[#5C5850]">Authenticity Signal</p>
              <div className="flex items-baseline gap-1.5 mt-1">
                <span className="text-4xl font-serif font-semibold tabular-nums text-[#181715]">
                  {assessment.authenticityScore}
                </span>
                <span className="text-xs font-mono text-[#5C5850]">/ 100</span>
              </div>
            </div>
            <div>
              <p className="text-xs text-[#5C5850]">Optical Confidence</p>
              <div className="flex items-baseline gap-1.5 mt-1">
                <span
                  className={`text-4xl font-serif font-semibold tabular-nums ${
                    assessment.confidenceScore >= 65
                      ? 'text-[#1D6A4E]'
                      : 'text-[#B44332]'
                  }`}
                >
                  {assessment.confidenceScore}
                </span>
                <span className="text-xs font-mono text-[#5C5850]">/ 100</span>
              </div>
            </div>
          </div>

          {/* Optical Sub-Metrics */}
          <div className="grid grid-cols-3 gap-3 text-xs">
            <div className="bg-[#FAF8F4] rounded-xl p-3 border border-[#EFEAE0]">
              <p className="text-[#5C5850]">Box Fit</p>
              <p className="font-mono font-semibold tabular-nums text-[#181715] mt-1">
                {assessment.framingScore}%
              </p>
            </div>
            <div className="bg-[#FAF8F4] rounded-xl p-3 border border-[#EFEAE0]">
              <p className="text-[#5C5850]">Lighting</p>
              <p className="font-mono font-semibold tabular-nums text-[#181715] mt-1">
                {assessment.lightingScore}%
              </p>
            </div>
            <div className="bg-[#FAF8F4] rounded-xl p-3 border border-[#EFEAE0]">
              <p className="text-[#5C5850]">Sharpness</p>
              <p className="font-mono font-semibold tabular-nums text-[#181715] mt-1">
                {assessment.sharpnessScore}%
              </p>
            </div>
          </div>

          {/* Quality Gate & Retake Warning */}
          {(!assessment.qualityPassed || assessment.retakeRecommended) && (
            <div className="bg-[#FDF6EB] border border-[#EBD2A9] rounded-xl p-4 space-y-3">
              <div className="flex items-start gap-2.5">
                <AlertTriangle className="w-4 h-4 text-[#9A6B2F] shrink-0 mt-0.5" />
                <div className="text-xs leading-relaxed">
                  <p className="font-semibold text-[#6F4B1B]">
                    Confidence Lowered — Retake Recommended
                  </p>
                  {assessment.qualityPenaltyReason && (
                    <p className="text-[#5C5850] mt-1">{assessment.qualityPenaltyReason}</p>
                  )}
                  {assessment.retakeAngleInstruction && (
                    <p className="text-[#181715] font-medium mt-1.5">
                      Correction: {assessment.retakeAngleInstruction}
                    </p>
                  )}
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-2 pt-1">
                <button
                  type="button"
                  onClick={onRetakeStep}
                  className="min-h-[44px] px-4 py-2 bg-[#181715] text-white text-xs font-medium rounded-lg hover:bg-[#2F2D29] transition-colors flex items-center gap-1.5 whitespace-nowrap cursor-pointer"
                >
                  <RotateCcw className="w-3.5 h-3.5" />
                  <span>Retake Angle</span>
                </button>
                {!captureState.keptLowConfidence && (
                  <button
                    type="button"
                    onClick={onKeepLowConfidence}
                    className="min-h-[44px] px-3 py-2 border border-[#D8C5A3] text-[#6F4B1B] text-xs font-medium rounded-lg hover:bg-white transition-colors whitespace-nowrap cursor-pointer"
                  >
                    Keep With Reduced Confidence
                  </button>
                )}
              </div>
            </div>
          )}

          {/* Summary & Marker Observations */}
          <p className="text-sm text-[#181715] leading-relaxed">{assessment.summary}</p>

          <div className="space-y-3 text-xs">
            {assessment.confirmations.length > 0 && (
              <div>
                <p className="font-semibold text-[#1D6A4E] flex items-center gap-1.5 mb-1.5">
                  <Check className="w-3.5 h-3.5" />
                  <span>Observed Confirmations</span>
                </p>
                <ul className="space-y-1 text-[#181715]">
                  {assessment.confirmations.map((c, idx) => (
                    <li key={idx} className="leading-relaxed">
                      · {c}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {assessment.anomalies.length > 0 && (
              <div>
                <p className="font-semibold text-[#B44332] flex items-center gap-1.5 mb-1.5">
                  <ShieldAlert className="w-3.5 h-3.5" />
                  <span>Observed Anomalies &amp; Caveats</span>
                </p>
                <ul className="space-y-1 text-[#181715]">
                  {assessment.anomalies.map((a, idx) => (
                    <li key={idx} className="leading-relaxed">
                      · {a}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          {/* Human Judgement Augmentation */}
          <div className="pt-3 border-t border-[#E2DDD2] text-xs">
            <p className="font-semibold text-[#5C5850]">Human Judgement Check</p>
            <p className="text-[#181715] mt-1 leading-relaxed">{assessment.humanJudgementNote}</p>
          </div>

          {/* Sequence Progression Action */}
          <div className="pt-2 flex flex-wrap items-center justify-end gap-3">
            {hasNextStep ? (
              <button
                type="button"
                onClick={onNextStep}
                className="w-full sm:w-auto min-h-[48px] px-5 py-2.5 bg-[#181715] text-white text-sm font-medium rounded-xl hover:bg-[#2F2D29] transition-colors flex items-center justify-center gap-2 whitespace-nowrap cursor-pointer"
              >
                <span>Proceed to Next Marker</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            ) : (
              <button
                type="button"
                disabled={!allStepsCaptured}
                onClick={onFinalizeScorecard}
                className="w-full sm:w-auto min-h-[48px] px-5 py-2.5 bg-[#1D6A4E] text-white text-sm font-medium rounded-xl hover:bg-[#16533C] disabled:opacity-50 transition-colors flex items-center justify-center gap-2 whitespace-nowrap cursor-pointer"
              >
                <span>Issue Final Scorecard</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      )}
    </aside>
  );
}
