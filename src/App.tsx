import { useEffect, useMemo, useState } from 'react';
import {
  ArrowRight,
  CheckCircle2,
  ClipboardCheck,
  Compass,
  AlertTriangle,
  Sparkles,
} from 'lucide-react';
import { ArchiveDrawer } from './components/ArchiveDrawer.tsx';
import { FinalScorecardView } from './components/FinalScorecardView.tsx';
import { MacroViewfinder } from './components/MacroViewfinder.tsx';
import { MarkerInspectorPanel } from './components/MarkerInspectorPanel.tsx';
import { OfflineIndicator, PWAInstallButton } from './components/PWAInstallButton.tsx';
import { CATEGORY_PRESETS } from './data/protocols.ts';
import type {
  CapturedMarkerState,
  CategoryId,
  ClientOpticalMetrics,
  FinalScorecard,
  MarkerAssessment,
  MarkerSpec,
  SavedInspectionRecord,
} from './types/inspection.ts';

type WorkspaceView = 'setup' | 'inspect' | 'scorecard';

const ARCHIVE_STORAGE_KEY = 'loupe_atelier_archive_v1';

function buildInitialCaptures(markers: MarkerSpec[]): Record<string, CapturedMarkerState> {
  const map: Record<string, CapturedMarkerState> = {};
  for (const m of markers) {
    map[m.id] = {
      markerId: m.id,
      imageDataUrl: null,
      clientMetrics: null,
      assessment: null,
      retakeCount: 0,
      keptLowConfidence: false,
    };
  }
  return map;
}

export default function App() {
  const [view, setView] = useState<WorkspaceView>('setup');
  const [selectedCategory, setSelectedCategory] = useState<CategoryId>('handbag');
  const [itemName, setItemName] = useState<string>(CATEGORY_PRESETS[0].defaultItemName);
  const [customItemPrompt, setCustomItemPrompt] = useState<string>('');
  const [isPlanningProtocol, setIsPlanningProtocol] = useState<boolean>(false);
  const [planError, setPlanError] = useState<string | null>(null);

  const [markers, setMarkers] = useState<MarkerSpec[]>(CATEGORY_PRESETS[0].markers);
  const [activeStepIndex, setActiveStepIndex] = useState<number>(0);
  const [captures, setCaptures] = useState<Record<string, CapturedMarkerState>>(() =>
    buildInitialCaptures(CATEGORY_PRESETS[0].markers)
  );

  const [isAnalyzingMarker, setIsAnalyzingMarker] = useState<boolean>(false);
  const [markerError, setMarkerError] = useState<string | null>(null);

  const [isFinalizing, setIsFinalizing] = useState<boolean>(false);
  const [finalizeError, setFinalizeError] = useState<string | null>(null);
  const [scorecard, setScorecard] = useState<FinalScorecard | null>(null);
  const [isSavedToArchive, setIsSavedToArchive] = useState<boolean>(false);

  const [archiveOpen, setArchiveOpen] = useState<boolean>(false);
  const [archiveRecords, setArchiveRecords] = useState<SavedInspectionRecord[]>(() => {
    try {
      const raw = localStorage.getItem(ARCHIVE_STORAGE_KEY);
      return raw ? (JSON.parse(raw) as SavedInspectionRecord[]) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(ARCHIVE_STORAGE_KEY, JSON.stringify(archiveRecords));
    } catch {
      // Ignore storage quota errors
    }
  }, [archiveRecords]);

  const activeCategoryPreset = useMemo(
    () => CATEGORY_PRESETS.find((c) => c.id === selectedCategory),
    [selectedCategory]
  );

  const activeCategoryName =
    selectedCategory === 'custom'
      ? 'Custom Macro Sequence'
      : (activeCategoryPreset?.name ?? 'Luxury Goods');

  const currentMarker = markers[activeStepIndex] ?? markers[0];
  const currentCapture = captures[currentMarker.id] ?? {
    markerId: currentMarker.id,
    imageDataUrl: null,
    clientMetrics: null,
    assessment: null,
    retakeCount: 0,
    keptLowConfidence: false,
  };

  const completedCount = useMemo(
    () => markers.filter((m) => Boolean(captures[m.id]?.assessment)).length,
    [markers, captures]
  );
  const allStepsAssessed = completedCount === markers.length && markers.length > 0;

  const handleSelectCategory = (categoryId: CategoryId) => {
    setSelectedCategory(categoryId);
    setPlanError(null);
    setScorecard(null);
    setIsSavedToArchive(false);

    if (categoryId === 'custom') {
      return;
    }

    const preset = CATEGORY_PRESETS.find((c) => c.id === categoryId);
    if (preset) {
      setItemName(preset.defaultItemName);
      setMarkers(preset.markers);
      setCaptures(buildInitialCaptures(preset.markers));
      setActiveStepIndex(0);
    }
  };

  const handleGenerateCustomProtocol = async () => {
    const query = customItemPrompt.trim();
    if (query.length < 3) {
      setPlanError('Enter the brand, model, or material details to generate a custom macro protocol.');
      return;
    }

    setIsPlanningProtocol(true);
    setPlanError(null);

    try {
      const res = await fetch('/api/protocol/plan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ itemDescription: query }),
      });
      const data = (await res.json()) as {
        itemTitle?: string;
        markers?: MarkerSpec[];
        error?: string;
      };

      if (!res.ok || !data.markers?.length) {
        throw new Error(data.error || 'Could not generate a macro protocol.');
      }

      setSelectedCategory('custom');
      setItemName(data.itemTitle || query);
      setMarkers(data.markers);
      setCaptures(buildInitialCaptures(data.markers));
      setActiveStepIndex(0);
      setScorecard(null);
      setIsSavedToArchive(false);
    } catch (err) {
      setPlanError(err instanceof Error ? err.message : 'Protocol generation failed.');
    } finally {
      setIsPlanningProtocol(false);
    }
  };

  const handleStartSequence = () => {
    setMarkerError(null);
    setFinalizeError(null);
    setActiveStepIndex(0);
    setView('inspect');
  };

  const handleResetInspection = () => {
    setScorecard(null);
    setIsSavedToArchive(false);
    setMarkerError(null);
    setFinalizeError(null);
    setCaptures(buildInitialCaptures(markers));
    setActiveStepIndex(0);
    setView('setup');
  };

  const handleCaptureReady = (imageDataUrl: string, metrics: ClientOpticalMetrics) => {
    setMarkerError(null);
    setScorecard(null);
    setIsSavedToArchive(false);

    setCaptures((prev) => {
      const existing = prev[currentMarker.id];
      return {
        ...prev,
        [currentMarker.id]: {
          markerId: currentMarker.id,
          imageDataUrl,
          clientMetrics: metrics,
          assessment: null,
          retakeCount: existing?.assessment ? (existing.retakeCount ?? 0) + 1 : existing?.retakeCount ?? 0,
          keptLowConfidence: false,
        },
      };
    });
  };

  const handleClearCurrentCapture = () => {
    setMarkerError(null);
    setScorecard(null);
    setIsSavedToArchive(false);

    setCaptures((prev) => {
      const existing = prev[currentMarker.id];
      return {
        ...prev,
        [currentMarker.id]: {
          markerId: currentMarker.id,
          imageDataUrl: null,
          clientMetrics: null,
          assessment: null,
          retakeCount: existing?.imageDataUrl ? (existing.retakeCount ?? 0) + 1 : existing?.retakeCount ?? 0,
          keptLowConfidence: false,
        },
      };
    });
  };

  const handleRunMarkerAssessment = async () => {
    if (!currentCapture.imageDataUrl) return;
    setIsAnalyzingMarker(true);
    setMarkerError(null);

    try {
      const res = await fetch('/api/inspect/marker', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          imageDataUrl: currentCapture.imageDataUrl,
          categoryName: activeCategoryName,
          itemName,
          markerSpec: currentMarker,
          clientMetrics: currentCapture.clientMetrics,
        }),
      });

      const data = (await res.json()) as MarkerAssessment & { error?: string };
      if (!res.ok) {
        throw new Error(data.error || 'Macro marker scoring failed.');
      }

      setCaptures((prev) => ({
        ...prev,
        [currentMarker.id]: {
          ...prev[currentMarker.id],
          assessment: data,
          keptLowConfidence: false,
        },
      }));
    } catch (err) {
      setMarkerError(err instanceof Error ? err.message : 'Unable to assess macro frame.');
    } finally {
      setIsAnalyzingMarker(false);
    }
  };

  const handleKeepLowConfidence = () => {
    setCaptures((prev) => ({
      ...prev,
      [currentMarker.id]: {
        ...prev[currentMarker.id],
        keptLowConfidence: true,
      },
    }));
  };

  const handleFinalizeScorecard = async () => {
    const assessedList = markers
      .map((m) => captures[m.id]?.assessment)
      .filter((a): a is MarkerAssessment => Boolean(a));

    if (assessedList.length === 0) {
      setFinalizeError('Score at least one macro marker before issuing a scorecard.');
      return;
    }

    setIsFinalizing(true);
    setFinalizeError(null);

    try {
      const res = await fetch('/api/inspect/finalize', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          itemName,
          categoryName: activeCategoryName,
          markers,
          assessments: assessedList,
        }),
      });
      const data = (await res.json()) as FinalScorecard & { error?: string };
      if (!res.ok) {
        throw new Error(data.error || 'Could not generate final scorecard.');
      }

      setScorecard(data);
      setIsSavedToArchive(false);
      setView('scorecard');
    } catch (err) {
      setFinalizeError(err instanceof Error ? err.message : 'Scorecard synthesis failed.');
    } finally {
      setIsFinalizing(false);
    }
  };

  const handleSaveArchive = () => {
    if (!scorecard || isSavedToArchive) return;
    const record: SavedInspectionRecord = {
      id: `insp-${Date.now()}`,
      category: selectedCategory,
      itemName,
      markers,
      captures,
      scorecard,
      savedAt: new Date().toISOString(),
    };
    setArchiveRecords((prev) => [record, ...prev.slice(0, 14)]);
    setIsSavedToArchive(true);
  };

  const handleLoadArchiveRecord = (record: SavedInspectionRecord) => {
    setSelectedCategory(record.category);
    setItemName(record.itemName);
    setMarkers(record.markers);
    setCaptures(record.captures);
    setScorecard(record.scorecard);
    setIsSavedToArchive(true);
    setActiveStepIndex(0);
    setArchiveOpen(false);
    setView('scorecard');
  };

  const handleDeleteArchiveRecord = (id: string) => {
    setArchiveRecords((prev) => prev.filter((r) => r.id !== id));
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#F7F5F0] text-[#181715]">
      {/* Top Navigation Bar — 3-Zone Contract */}
      <header className="no-print sticky top-0 z-30 bg-[#F7F5F0]/92 backdrop-blur-md border-b border-[#E2DDD2]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between gap-8">
          <button
            type="button"
            onClick={() => setView('setup')}
            className="text-xl sm:text-2xl font-serif font-semibold tracking-tight text-[#181715] whitespace-nowrap shrink-0 cursor-pointer"
          >
            Loupe Atelier
          </button>

          <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-[#5C5850]">
            <button
              type="button"
              onClick={() => setView('setup')}
              className={`hover:text-[#181715] transition-colors whitespace-nowrap shrink-0 cursor-pointer ${
                view === 'setup' ? 'text-[#181715] underline underline-offset-8' : ''
              }`}
            >
              Protocol
            </button>
            <button
              type="button"
              onClick={() => setView('inspect')}
              className={`hover:text-[#181715] transition-colors whitespace-nowrap shrink-0 cursor-pointer ${
                view === 'inspect' ? 'text-[#181715] underline underline-offset-8' : ''
              }`}
            >
              Viewfinder
            </button>
            <button
              type="button"
              disabled={!scorecard}
              onClick={() => scorecard && setView('scorecard')}
              className={`hover:text-[#181715] disabled:opacity-40 transition-colors whitespace-nowrap shrink-0 cursor-pointer ${
                view === 'scorecard' ? 'text-[#181715] underline underline-offset-8' : ''
              }`}
            >
              Scorecard
            </button>
            <button
              type="button"
              onClick={() => setArchiveOpen(true)}
              className="hover:text-[#181715] transition-colors whitespace-nowrap shrink-0 cursor-pointer"
            >
              Archive ({archiveRecords.length})
            </button>
          </nav>

          <div className="flex items-center gap-3 shrink-0">
            <button
              type="button"
              onClick={handleResetInspection}
              className="min-h-[40px] px-4 py-2 bg-[#181715] text-white text-xs font-medium rounded-lg hover:bg-[#2F2D29] transition-colors whitespace-nowrap shrink-0 cursor-pointer"
            >
              New Inspection
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 py-6 sm:py-10 pb-24 lg:pb-12">
        {view === 'setup' && (
          <div className="space-y-8">
            {/* Editorial Intro & Scope */}
            <section className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
              <div className="lg:col-span-7 flex flex-col justify-between bg-white border border-[#E2DDD2] rounded-2xl p-6 sm:p-8">
                <div>
                  <div className="flex flex-wrap items-center gap-2 text-xs text-[#5C5850]">
                    <span>Macro Pre-Screening Studio</span>
                    <span aria-hidden="true">·</span>
                    <span>0–100 Authenticity Probability</span>
                    <span aria-hidden="true">·</span>
                    <span>Human-Augmented Decision Support</span>
                  </div>
                  <h1 className="text-3xl sm:text-5xl font-serif font-semibold text-[#181715] mt-3 leading-[1.1] text-balance">
                    Sequential macro inspection for luxury goods pre-screening.
                  </h1>
                  <p className="mt-4 text-sm sm:text-base text-[#5C5850] leading-relaxed max-w-2xl">
                    Capture calibrated macro photographs in sequence—stitching density, brand font
                    kerning, hardware plating, and serial marks. Each frame is checked for lighting,
                    sharpness, and bounding-box proportion before scoring. Results express visual
                    probability to assist human judgement, never a 100% guarantee.
                  </p>
                  <div className="mt-5 flex flex-wrap items-center gap-3">
                    <PWAInstallButton />
                  </div>
                </div>

                {/* Custom AI Protocol Builder */}
                <div className="mt-8 pt-6 border-t border-[#E2DDD2]">
                  <label
                    htmlFor="custom-item-input"
                    className="block text-xs font-semibold text-[#181715]"
                  >
                    Inspect a Specific Reference or Custom Piece
                  </label>
                  <div className="mt-2 flex flex-col sm:flex-row gap-2.5">
                    <input
                      id="custom-item-input"
                      type="text"
                      value={customItemPrompt}
                      onChange={(e) => setCustomItemPrompt(e.target.value)}
                      placeholder="e.g., Hermès Kelly 28 Togo Palladium, Rolex Submariner 126610LN"
                      className="flex-1 min-h-[44px] px-3.5 py-2 bg-[#FAF8F4] border border-[#E2DDD2] rounded-xl text-sm text-[#181715] placeholder:text-[#8E897E] focus:outline-none focus:border-[#181715]"
                    />
                    <button
                      type="button"
                      disabled={isPlanningProtocol}
                      onClick={() => void handleGenerateCustomProtocol()}
                      className="min-h-[44px] px-4 py-2 bg-[#9A6B2F] text-white text-xs font-semibold rounded-xl hover:bg-[#825823] disabled:opacity-60 transition-colors flex items-center justify-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
                    >
                      <Sparkles className="w-4 h-4" />
                      <span>
                        {isPlanningProtocol ? 'Planning Sequence...' : 'Generate Macro Protocol'}
                      </span>
                    </button>
                  </div>
                  {planError && (
                    <p className="mt-2 text-xs text-[#B44332]">{planError}</p>
                  )}
                </div>
              </div>

              {/* Category Preset Selector */}
              <div className="lg:col-span-5 bg-white border border-[#E2DDD2] rounded-2xl p-6 sm:p-8 flex flex-col justify-between">
                <div>
                  <h2 className="text-xl font-serif font-semibold text-[#181715]">
                    Select Category Protocol
                  </h2>
                  <p className="text-xs text-[#5C5850] mt-1">
                    Choose a calibrated house sequence or generate a custom sequence on the left.
                  </p>

                  <div className="mt-4 space-y-2.5">
                    {CATEGORY_PRESETS.map((preset) => {
                      const active = selectedCategory === preset.id;
                      return (
                        <button
                          key={preset.id}
                          type="button"
                          onClick={() => handleSelectCategory(preset.id)}
                          className={`w-full text-left p-3.5 rounded-xl border transition-colors cursor-pointer ${
                            active
                              ? 'bg-[#FAF8F4] border-[#181715]'
                              : 'bg-white border-[#E2DDD2] hover:border-[#9A6B2F]'
                          }`}
                        >
                          <div className="flex items-center justify-between gap-2">
                            <span className="text-sm font-semibold text-[#181715]">
                              {preset.name}
                            </span>
                            <span className="text-xs font-mono tabular-nums text-[#5C5850]">
                              {preset.markers.length} Steps
                            </span>
                          </div>
                          <p className="text-xs text-[#5C5850] mt-1 line-clamp-2">
                            {preset.description}
                          </p>
                        </button>
                      );
                    })}
                  </div>
                </div>

                <div className="mt-5 pt-4 border-t border-[#E2DDD2]">
                  <label
                    htmlFor="item-label-input"
                    className="block text-xs font-medium text-[#5C5850]"
                  >
                    Inspection Subject Label
                  </label>
                  <input
                    id="item-label-input"
                    type="text"
                    value={itemName}
                    onChange={(e) => setItemName(e.target.value)}
                    className="mt-1.5 w-full min-h-[42px] px-3 py-2 bg-[#FAF8F4] border border-[#E2DDD2] rounded-xl text-sm text-[#181715] focus:outline-none focus:border-[#181715]"
                  />
                </div>
              </div>
            </section>

            {/* Planned Sequential Checklist Preview */}
            <section className="bg-white border border-[#E2DDD2] rounded-2xl p-6 sm:p-8">
              <div className="flex flex-wrap items-center justify-between gap-4 pb-5 border-b border-[#E2DDD2]">
                <div>
                  <p className="text-xs font-medium text-[#9A6B2F]">{activeCategoryName}</p>
                  <h2 className="text-2xl font-serif font-semibold text-[#181715] mt-0.5">
                    Sequential Macro Capture Plan · {itemName}
                  </h2>
                </div>
                <button
                  type="button"
                  onClick={handleStartSequence}
                  className="min-h-[48px] px-6 py-2.5 bg-[#181715] text-white text-sm font-medium rounded-xl hover:bg-[#2F2D29] transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
                >
                  <span>Begin Macro Sequence</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>

              <div className="mt-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
                {markers.map((m) => (
                  <div
                    key={m.id}
                    className="bg-[#FAF8F4] border border-[#E2DDD2] rounded-xl p-4 flex flex-col justify-between"
                  >
                    <div>
                      <div className="flex items-center justify-between text-xs font-mono tabular-nums text-[#5C5850]">
                        <span>Step 0{m.stepNumber}</span>
                        <span>Weight {Math.round(m.weight * 100)}%</span>
                      </div>
                      <h3 className="text-base font-semibold text-[#181715] mt-1.5">
                        {m.title}
                      </h3>
                      <p className="text-xs text-[#5C5850] mt-1.5 leading-relaxed">
                        {m.focusFeature}
                      </p>
                    </div>
                    <p className="mt-4 pt-3 border-t border-[#E2DDD2] text-[11px] font-mono tabular-nums text-[#5C5850]">
                      Box Fit {m.boundingBox.targetFillMinPercent}–
                      {m.boundingBox.targetFillMaxPercent}% · {m.boundingBox.aspectRatioLabel}
                    </p>
                  </div>
                ))}
              </div>
            </section>
          </div>
        )}

        {view === 'inspect' && (
          <div className="space-y-6">
            {/* Sequence Step Strip */}
            <section className="bg-white border border-[#E2DDD2] rounded-2xl p-4 sm:p-5">
              <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
                <div className="min-w-0">
                  <p className="text-xs text-[#5C5850]">
                    {activeCategoryName} ·{' '}
                    <span className="font-mono tabular-nums">
                      {completedCount} of {markers.length} scored
                    </span>
                  </p>
                  <h1 className="text-xl sm:text-2xl font-serif font-semibold text-[#181715] truncate">
                    {itemName}
                  </h1>
                </div>

                <button
                  type="button"
                  disabled={completedCount === 0 || isFinalizing}
                  onClick={() => void handleFinalizeScorecard()}
                  className="min-h-[44px] px-4 py-2 bg-[#1D6A4E] text-white text-xs font-semibold rounded-xl hover:bg-[#16533C] disabled:opacity-45 transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
                >
                  <ClipboardCheck className="w-4 h-4" />
                  <span>
                    {isFinalizing
                      ? 'Generating Scorecard...'
                      : allStepsAssessed
                        ? 'Issue Final Scorecard'
                        : `Issue Partial Scorecard (${completedCount}/${markers.length})`}
                  </span>
                </button>
              </div>

              {finalizeError && (
                <div className="mb-4 px-4 py-2.5 bg-[#FDF3F1] border border-[#F1CCC5] rounded-xl text-xs text-[#8E2F20]">
                  {finalizeError}
                </div>
              )}

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                {markers.map((m, idx) => {
                  const cap = captures[m.id];
                  const assess = cap?.assessment;
                  const isActive = idx === activeStepIndex;

                  return (
                    <button
                      key={m.id}
                      type="button"
                      onClick={() => {
                        setMarkerError(null);
                        setActiveStepIndex(idx);
                      }}
                      className={`min-h-[48px] text-left px-3.5 py-2.5 rounded-xl border transition-colors cursor-pointer ${
                        isActive
                          ? 'bg-[#181715] text-white border-[#181715]'
                          : 'bg-[#FAF8F4] text-[#181715] border-[#E2DDD2] hover:border-[#9A6B2F]'
                      }`}
                    >
                      <div className="flex items-center justify-between gap-2 text-[11px] font-mono tabular-nums">
                        <span className={isActive ? 'text-white/75' : 'text-[#5C5850]'}>
                          0{m.stepNumber}
                        </span>
                        {assess ? (
                          <span className="flex items-center gap-1">
                            {assess.qualityPassed ? (
                              <CheckCircle2
                                className={`w-3 h-3 ${
                                  isActive ? 'text-[#87D6B6]' : 'text-[#1D6A4E]'
                                }`}
                              />
                            ) : (
                              <AlertTriangle
                                className={`w-3 h-3 ${
                                  isActive ? 'text-[#E3B873]' : 'text-[#B44332]'
                                }`}
                              />
                            )}
                            <span>{assess.authenticityScore}/100</span>
                          </span>
                        ) : cap?.imageDataUrl ? (
                          <span className={isActive ? 'text-[#E3B873]' : 'text-[#9A6B2F]'}>
                            Ready
                          </span>
                        ) : (
                          <span className={isActive ? 'text-white/60' : 'text-[#8E897E]'}>
                            Pending
                          </span>
                        )}
                      </div>
                      <p className="text-xs font-medium truncate mt-0.5">{m.title}</p>
                    </button>
                  );
                })}
              </div>
            </section>

            {/* 2-Column Optical Studio Workspace */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
              <div className="lg:col-span-7">
                <MacroViewfinder
                  marker={currentMarker}
                  captureState={currentCapture}
                  isAnalyzing={isAnalyzingMarker}
                  onCaptureReady={handleCaptureReady}
                  onRunAssessment={() => void handleRunMarkerAssessment()}
                  onClearCapture={handleClearCurrentCapture}
                />
              </div>

              <div className="lg:col-span-5">
                <MarkerInspectorPanel
                  marker={currentMarker}
                  captureState={currentCapture}
                  isAnalyzing={isAnalyzingMarker}
                  analysisError={markerError}
                  hasNextStep={activeStepIndex < markers.length - 1}
                  allStepsCaptured={allStepsAssessed}
                  onRetakeStep={handleClearCurrentCapture}
                  onKeepLowConfidence={handleKeepLowConfidence}
                  onNextStep={() => setActiveStepIndex((i) => Math.min(markers.length - 1, i + 1))}
                  onFinalizeScorecard={() => void handleFinalizeScorecard()}
                />
              </div>
            </div>
          </div>
        )}

        {view === 'scorecard' && scorecard && (
          <FinalScorecardView
            itemName={itemName}
            categoryName={activeCategoryName}
            markers={markers}
            captures={captures}
            scorecard={scorecard}
            isSaved={isSavedToArchive}
            onRetakeMarker={(idx) => {
              setActiveStepIndex(idx);
              setView('inspect');
            }}
            onSaveArchive={handleSaveArchive}
            onNewInspection={handleResetInspection}
          />
        )}
      </main>

      {/* Mobile Bottom Navigation Bar (< lg, <= 15% viewport height) */}
      <nav className="no-print lg:hidden fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-[#E2DDD2] h-16 grid grid-cols-4 items-center px-2">
        <button
          type="button"
          onClick={() => setView('setup')}
          className={`min-h-[44px] flex flex-col items-center justify-center text-[11px] font-medium cursor-pointer ${
            view === 'setup' ? 'text-[#9A6B2F]' : 'text-[#5C5850]'
          }`}
        >
          <Compass className="w-5 h-5" />
          <span className="mt-0.5">Protocol</span>
        </button>
        <button
          type="button"
          onClick={() => setView('inspect')}
          className={`min-h-[44px] flex flex-col items-center justify-center text-[11px] font-medium cursor-pointer ${
            view === 'inspect' ? 'text-[#9A6B2F]' : 'text-[#5C5850]'
          }`}
        >
          <Sparkles className="w-5 h-5" />
          <span className="mt-0.5">Inspect</span>
        </button>
        <button
          type="button"
          disabled={!scorecard}
          onClick={() => scorecard && setView('scorecard')}
          className={`min-h-[44px] flex flex-col items-center justify-center text-[11px] font-medium disabled:opacity-35 cursor-pointer ${
            view === 'scorecard' ? 'text-[#9A6B2F]' : 'text-[#5C5850]'
          }`}
        >
          <ClipboardCheck className="w-5 h-5" />
          <span className="mt-0.5">Scorecard</span>
        </button>
        <button
          type="button"
          onClick={() => setArchiveOpen(true)}
          className="min-h-[44px] flex flex-col items-center justify-center text-[11px] font-medium text-[#5C5850] cursor-pointer"
        >
          <span className="font-mono text-sm font-semibold tabular-nums">
            {archiveRecords.length}
          </span>
          <span>Archive</span>
        </button>
      </nav>

      <ArchiveDrawer
        isOpen={archiveOpen}
        records={archiveRecords}
        onClose={() => setArchiveOpen(false)}
        onSelectRecord={handleLoadArchiveRecord}
        onDeleteRecord={handleDeleteArchiveRecord}
      />

      <OfflineIndicator />
    </div>
  );
}
