import {
  AlertTriangle,
  BookmarkCheck,
  CheckCircle2,
  Download,
  Printer,
  RotateCcw,
  ShieldAlert,
  UserCheck,
} from 'lucide-react';
import type {
  CapturedMarkerState,
  FinalScorecard,
  MarkerSpec,
} from '../types/inspection.ts';

interface FinalScorecardViewProps {
  itemName: string;
  categoryName: string;
  markers: MarkerSpec[];
  captures: Record<string, CapturedMarkerState>;
  scorecard: FinalScorecard;
  isSaved: boolean;
  onRetakeMarker: (stepIndex: number) => void;
  onSaveArchive: () => void;
  onNewInspection: () => void;
}

export function FinalScorecardView({
  itemName,
  categoryName,
  markers,
  captures,
  scorecard,
  isSaved,
  onRetakeMarker,
  onSaveArchive,
  onNewInspection,
}: FinalScorecardViewProps) {
  const handleExportJson = () => {
    const payload = {
      itemName,
      categoryName,
      scorecard,
      markers: markers.map((m) => ({
        stepNumber: m.stepNumber,
        title: m.title,
        weight: m.weight,
        assessment: captures[m.id]?.assessment ?? null,
      })),
    };
    const blob = new Blob([JSON.stringify(payload, null, 2)], {
      type: 'application/json',
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `loupe-atelier-scorecard-${itemName
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .slice(0, 32)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const verdictTone =
    scorecard.verdictBand === 'High Visual Consistency'
      ? 'text-[#1D6A4E]'
      : scorecard.verdictBand === 'Moderate Visual Consistency'
        ? 'text-[#9A6B2F]'
        : 'text-[#B44332]';

  return (
    <div className="space-y-8">
      {/* Executive Scorecard Hero */}
      <section className="bg-white border border-[#E2DDD2] rounded-2xl p-6 sm:p-8">
        <div className="flex flex-wrap items-start justify-between gap-6 pb-6 border-b border-[#E2DDD2]">
          <div className="max-w-2xl">
            <div className="flex flex-wrap items-center gap-2 text-xs text-[#5C5850]">
              <span>{categoryName}</span>
              <span aria-hidden="true">·</span>
              <span>{markers.length} Macro Markers Assessed</span>
              <span aria-hidden="true">·</span>
              <span className="font-mono tabular-nums">
                {new Date(scorecard.generatedAt).toLocaleString()}
              </span>
            </div>
            <h1 className="text-3xl sm:text-4xl font-serif font-semibold text-[#181715] mt-2 text-balance">
              {itemName}
            </h1>
            <p className={`mt-2 text-sm font-semibold flex items-center gap-2 ${verdictTone}`}>
              {scorecard.verdictBand === 'High Visual Consistency' ? (
                <CheckCircle2 className="w-4 h-4 shrink-0" />
              ) : scorecard.verdictBand === 'Elevated Anomaly Signal' ? (
                <ShieldAlert className="w-4 h-4 shrink-0" />
              ) : (
                <AlertTriangle className="w-4 h-4 shrink-0" />
              )}
              <span>{scorecard.verdictBand}</span>
            </p>
          </div>

          {/* Primary Numeric Readout */}
          <div className="flex items-stretch gap-6 sm:gap-8">
            <div>
              <p className="text-xs text-[#5C5850]">Authenticity Probability</p>
              <div className="flex items-baseline gap-1.5 mt-1">
                <span className="text-5xl font-serif font-semibold tabular-nums text-[#181715]">
                  {scorecard.overallProbabilityScore}
                </span>
                <span className="text-xs font-mono text-[#5C5850]">/ 100</span>
              </div>
            </div>
            <div className="w-px bg-[#E2DDD2]" aria-hidden="true" />
            <div>
              <p className="text-xs text-[#5C5850]">Visual Confidence</p>
              <div className="flex items-baseline gap-1.5 mt-1">
                <span
                  className={`text-5xl font-serif font-semibold tabular-nums ${
                    scorecard.overallConfidenceScore >= 65
                      ? 'text-[#1D6A4E]'
                      : 'text-[#B44332]'
                  }`}
                >
                  {scorecard.overallConfidenceScore}
                </span>
                <span className="text-xs font-mono text-[#5C5850]">/ 100</span>
              </div>
            </div>
          </div>
        </div>

        <div className="pt-6 space-y-4">
          <p className="text-sm sm:text-base text-[#181715] leading-relaxed max-w-4xl">
            {scorecard.executiveSummary}
          </p>

          {scorecard.lowConfidenceMarkers.length > 0 && (
            <div className="bg-[#FDF6EB] border border-[#EBD2A9] rounded-xl p-4 text-xs text-[#6F4B1B] flex items-start gap-2.5">
              <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5 text-[#9A6B2F]" />
              <div>
                <p className="font-semibold">
                  Low-Confidence Optical Penalties Applied ({scorecard.lowConfidenceMarkers.length})
                </p>
                <p className="mt-1 text-[#5C5850]">
                  The following areas had insufficient lighting, focus sharpness, or bounding-box
                  fill and lowered overall confidence:{' '}
                  <span className="font-medium text-[#181715]">
                    {scorecard.lowConfidenceMarkers.join(', ')}
                  </span>
                  . Retake those angles below to refine the probability estimate.
                </p>
              </div>
            </div>
          )}

          {/* Action Toolbar */}
          <div className="no-print pt-2 flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap items-center gap-2.5">
              <button
                type="button"
                onClick={onSaveArchive}
                disabled={isSaved}
                className="min-h-[44px] px-4 py-2 bg-[#181715] text-white text-xs font-medium rounded-xl hover:bg-[#2F2D29] disabled:bg-[#1D6A4E] transition-colors flex items-center gap-2 whitespace-nowrap cursor-pointer"
              >
                <BookmarkCheck className="w-4 h-4" />
                <span>{isSaved ? 'Saved to Archive' : 'Save to Archive'}</span>
              </button>
              <button
                type="button"
                onClick={handleExportJson}
                className="min-h-[44px] px-4 py-2 border border-[#E2DDD2] text-[#181715] text-xs font-medium rounded-xl hover:bg-[#FAF8F4] transition-colors flex items-center gap-2 whitespace-nowrap cursor-pointer"
              >
                <Download className="w-4 h-4" />
                <span>Export JSON</span>
              </button>
              <button
                type="button"
                onClick={() => window.print()}
                className="min-h-[44px] px-4 py-2 border border-[#E2DDD2] text-[#181715] text-xs font-medium rounded-xl hover:bg-[#FAF8F4] transition-colors flex items-center gap-2 whitespace-nowrap cursor-pointer"
              >
                <Printer className="w-4 h-4" />
                <span>Print Report</span>
              </button>
            </div>

            <button
              type="button"
              onClick={onNewInspection}
              className="min-h-[44px] px-4 py-2 border border-[#E2DDD2] text-[#5C5850] text-xs font-medium rounded-xl hover:text-[#181715] hover:bg-[#FAF8F4] transition-colors whitespace-nowrap cursor-pointer"
            >
              Start New Inspection
            </button>
          </div>
        </div>
      </section>

      {/* Feature-by-Feature Breakdown */}
      <section className="space-y-4">
        <div className="flex items-baseline justify-between gap-4">
          <h2 className="text-2xl font-serif font-semibold text-[#181715]">
            Macro Feature Breakdown
          </h2>
          <span className="text-xs text-[#5C5850] font-mono tabular-nums">
            Weighted Visual Signal Breakdown
          </span>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          {markers.map((marker, idx) => {
            const capture = captures[marker.id];
            const assessment = capture?.assessment;

            return (
              <article
                key={marker.id}
                className="bg-white border border-[#E2DDD2] rounded-2xl overflow-hidden flex flex-col"
              >
                <div className="grid grid-cols-1 sm:grid-cols-12 border-b border-[#E2DDD2]">
                  {/* Macro Thumbnail */}
                  <div className="sm:col-span-5 bg-[#111110] aspect-[4/3] sm:aspect-auto sm:h-full overflow-hidden">
                    {capture?.imageDataUrl ? (
                      <img
                        src={capture.imageDataUrl}
                        alt={marker.title}
                        referrerPolicy="no-referrer"
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center p-4 text-xs text-white/60">
                        No macro image captured
                      </div>
                    )}
                  </div>

                  {/* Marker Scores */}
                  <div className="sm:col-span-7 p-4 sm:p-5 flex flex-col justify-between">
                    <div>
                      <div className="flex items-center justify-between gap-2 text-xs text-[#5C5850] font-mono tabular-nums">
                        <span>
                          0{marker.stepNumber} · Weight {Math.round(marker.weight * 100)}%
                        </span>
                        <span>
                          {assessment?.qualityPassed
                            ? 'Verified Frame'
                            : 'Confidence Penalized'}
                        </span>
                      </div>
                      <h3 className="text-lg font-semibold text-[#181715] mt-1">
                        {marker.title}
                      </h3>
                    </div>

                    {assessment ? (
                      <div className="mt-4 pt-3 border-t border-[#EFEAE0] grid grid-cols-2 gap-4">
                        <div>
                          <p className="text-[11px] text-[#5C5850]">Signal Score</p>
                          <p className="text-2xl font-serif font-semibold tabular-nums text-[#181715]">
                            {assessment.authenticityScore}
                            <span className="text-xs font-mono text-[#5C5850] ml-1">/100</span>
                          </p>
                        </div>
                        <div>
                          <p className="text-[11px] text-[#5C5850]">Confidence</p>
                          <p
                            className={`text-2xl font-serif font-semibold tabular-nums ${
                              assessment.confidenceScore >= 65
                                ? 'text-[#1D6A4E]'
                                : 'text-[#B44332]'
                            }`}
                          >
                            {assessment.confidenceScore}
                            <span className="text-xs font-mono text-[#5C5850] ml-1">/100</span>
                          </p>
                        </div>
                      </div>
                    ) : (
                      <p className="text-xs text-[#5C5850] mt-4">Not assessed</p>
                    )}
                  </div>
                </div>

                {/* Observations & Retake Trigger */}
                {assessment && (
                  <div className="p-4 sm:p-5 space-y-3.5 text-xs flex-1 flex flex-col justify-between">
                    <div className="space-y-3">
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 font-mono tabular-nums text-[#5C5850]">
                        <span>Box Fit {assessment.framingScore}%</span>
                        <span aria-hidden="true">·</span>
                        <span>Lighting {assessment.lightingScore}%</span>
                        <span aria-hidden="true">·</span>
                        <span>Sharpness {assessment.sharpnessScore}%</span>
                      </div>

                      {!assessment.qualityPassed && assessment.retakeAngleInstruction && (
                        <p className="text-[#8E2F20] bg-[#FDF3F1] border border-[#F1CCC5] rounded-lg px-3 py-2">
                          Retake Guidance: {assessment.retakeAngleInstruction}
                        </p>
                      )}

                      {assessment.confirmations.length > 0 && (
                        <div>
                          <p className="font-semibold text-[#1D6A4E] mb-1">Confirmations</p>
                          <ul className="space-y-0.5 text-[#181715]">
                            {assessment.confirmations.map((c, i) => (
                              <li key={i}>· {c}</li>
                            ))}
                          </ul>
                        </div>
                      )}

                      {assessment.anomalies.length > 0 && (
                        <div>
                          <p className="font-semibold text-[#B44332] mb-1">Anomalies &amp; Caveats</p>
                          <ul className="space-y-0.5 text-[#181715]">
                            {assessment.anomalies.map((a, i) => (
                              <li key={i}>· {a}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </div>

                    <div className="no-print pt-3 border-t border-[#EFEAE0] flex items-center justify-between gap-3">
                      <span className="text-[#5C5850] truncate">
                        Human Note: {assessment.humanJudgementNote}
                      </span>
                      <button
                        type="button"
                        onClick={() => onRetakeMarker(idx)}
                        className="min-h-[36px] px-3 py-1.5 border border-[#E2DDD2] text-[#181715] font-medium rounded-lg hover:bg-[#FAF8F4] transition-colors flex items-center gap-1.5 whitespace-nowrap shrink-0 cursor-pointer"
                      >
                        <RotateCcw className="w-3.5 h-3.5" />
                        <span>Retake</span>
                      </button>
                    </div>
                  </div>
                )}
              </article>
            );
          })}
        </div>
      </section>

      {/* Human Augmentation & Notice */}
      <section className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        <div className="lg:col-span-7 bg-white border border-[#E2DDD2] rounded-2xl p-5 sm:p-6">
          <div className="flex items-center gap-2 mb-3">
            <UserCheck className="w-4 h-4 text-[#1D6A4E]" />
            <h2 className="text-xl font-serif font-semibold text-[#181715]">
              Human Judgement Augmentation Checklist
            </h2>
          </div>
          <p className="text-xs text-[#5C5850] mb-4">
            Complete these physical checks in person before making a final authentication decision:
          </p>
          <ol className="space-y-2.5 text-sm text-[#181715]">
            {scorecard.prioritizedHumanChecks.map((check, i) => (
              <li key={i} className="flex items-start gap-2.5 leading-relaxed">
                <span className="font-mono text-xs text-[#9A6B2F] mt-1 tabular-nums">
                  0{i + 1}.
                </span>
                <span>{check}</span>
              </li>
            ))}
          </ol>
        </div>

        <div className="lg:col-span-5 bg-[#FAF8F4] border border-[#E2DDD2] rounded-2xl p-5 sm:p-6 flex flex-col justify-between">
          <div>
            <h2 className="text-xl font-serif font-semibold text-[#181715]">
              Pre-Screening Scope &amp; Limitations
            </h2>
            <p className="text-xs text-[#5C5850] mt-2 leading-relaxed">
              {scorecard.disclaimer}
            </p>
          </div>
          <p className="text-xs text-[#5C5850] mt-6 pt-4 border-t border-[#E2DDD2]">
            Scorecard scores are probabilistic estimates derived strictly from submitted macro imagery.
          </p>
        </div>
      </section>
    </div>
  );
}
