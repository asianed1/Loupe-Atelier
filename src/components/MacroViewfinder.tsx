import { useCallback, useEffect, useRef, useState } from 'react';
import {
  Camera,
  CheckCircle2,
  Flashlight,
  ImagePlus,
  RefreshCw,
  ScanSearch,
  Sparkles,
  Sun,
  VideoOff,
} from 'lucide-react';
import type {
  CapturedMarkerState,
  ClientOpticalMetrics,
  MarkerSpec,
} from '../types/inspection.ts';
import {
  analyzeVideoLiveMetrics,
  captureVideoFrame,
  loadFileAsMacro,
  loadSampleImageAsMacro,
} from '../utils/imageAnalysis.ts';
import { BoundingBoxOverlay } from './BoundingBoxOverlay.tsx';

interface MacroViewfinderProps {
  marker: MarkerSpec;
  captureState: CapturedMarkerState;
  isAnalyzing: boolean;
  onCaptureReady: (imageDataUrl: string, metrics: ClientOpticalMetrics) => void;
  onRunAssessment: () => void;
  onClearCapture: () => void;
}

const ZOOM_OPTIONS = [1, 1.5, 2, 3];

export function MacroViewfinder({
  marker,
  captureState,
  isAnalyzing,
  onCaptureReady,
  onRunAssessment,
  onClearCapture,
}: MacroViewfinderProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [cameraMode, setCameraMode] = useState<'camera' | 'upload'>('upload');
  const [cameraError, setCameraError] = useState<string | null>(null);
  const [cameraReady, setCameraReady] = useState(false);
  const [zoomLevel, setZoomLevel] = useState<number>(1);
  const [torchSupported, setTorchSupported] = useState(false);
  const [torchOn, setTorchOn] = useState(false);
  const [liveMetrics, setLiveMetrics] = useState<ClientOpticalMetrics | null>(null);
  const [showReticleOnPreview, setShowReticleOnPreview] = useState(true);
  const [localError, setLocalError] = useState<string | null>(null);

  const stopCamera = useCallback(() => {
    if (streamRef.current) {
      for (const track of streamRef.current.getTracks()) {
        track.stop();
      }
      streamRef.current = null;
    }
    setCameraReady(false);
    setTorchSupported(false);
    setTorchOn(false);
  }, []);

  const startCamera = useCallback(async () => {
    setCameraError(null);
    setLocalError(null);

    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      setCameraError('Live camera access is unavailable in this browser context. Upload a macro photo below.');
      setCameraMode('upload');
      return;
    }

    try {
      stopCamera();
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: { ideal: 'environment' },
          width: { ideal: 1920 },
          height: { ideal: 1440 },
        },
        audio: false,
      });

      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        await videoRef.current.play();
      }

      const [videoTrack] = stream.getVideoTracks();
      if (videoTrack && typeof videoTrack.getCapabilities === 'function') {
        const caps = videoTrack.getCapabilities() as MediaTrackCapabilities & { torch?: boolean };
        setTorchSupported(Boolean(caps.torch));
      }

      setCameraReady(true);
    } catch (err) {
      const msg =
        err instanceof Error && err.name === 'NotAllowedError'
          ? 'Camera permission was declined. Enable camera access or upload a macro photo.'
          : 'No rear macro camera detected on this device. Upload a photo or test with a studio macro.';
      setCameraError(msg);
      setCameraMode('upload');
    }
  }, [stopCamera]);

  useEffect(() => {
    if (cameraMode === 'camera' && !captureState.imageDataUrl) {
      void startCamera();
    } else {
      stopCamera();
    }
    return () => {
      stopCamera();
    };
  }, [cameraMode, captureState.imageDataUrl, startCamera, stopCamera]);

  // Live optical pre-check loop when camera is streaming
  useEffect(() => {
    if (!cameraReady || cameraMode !== 'camera' || captureState.imageDataUrl) {
      return;
    }
    const interval = window.setInterval(() => {
      if (videoRef.current && videoRef.current.readyState >= 2) {
        const metrics = analyzeVideoLiveMetrics(videoRef.current, zoomLevel);
        setLiveMetrics(metrics);
      }
    }, 550);
    return () => window.clearInterval(interval);
  }, [cameraReady, cameraMode, captureState.imageDataUrl, zoomLevel]);

  const handleToggleTorch = async () => {
    const track = streamRef.current?.getVideoTracks()[0];
    if (!track) return;
    const next = !torchOn;
    try {
      await track.applyConstraints({
        advanced: [{ torch: next } as MediaTrackConstraintSet],
      });
      setTorchOn(next);
    } catch {
      setLocalError('Device torch could not be toggled.');
    }
  };

  const handleCaptureShutter = () => {
    if (!videoRef.current || !cameraReady) return;
    setLocalError(null);
    try {
      const { dataUrl, metrics } = captureVideoFrame(
        videoRef.current,
        zoomLevel,
        marker.boundingBox.aspectRatioValue
      );
      onCaptureReady(dataUrl, metrics);
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : 'Capture failed.');
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    setLocalError(null);
    try {
      const { dataUrl, metrics } = await loadFileAsMacro(file);
      onCaptureReady(dataUrl, metrics);
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : 'Unable to read uploaded photo.');
    }
  };

  const handleLoadSample = async () => {
    if (!marker.sampleImageUrl) return;
    setLocalError(null);
    try {
      const { dataUrl, metrics } = await loadSampleImageAsMacro(marker.sampleImageUrl);
      onCaptureReady(dataUrl, metrics);
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : 'Unable to load reference macro.');
    }
  };

  const activeMetrics = captureState.imageDataUrl ? captureState.clientMetrics : liveMetrics;

  const brightnessStatus =
    !activeMetrics || activeMetrics.averageBrightness === 0
      ? 'Awaiting frame'
      : activeMetrics.averageBrightness < 62
        ? 'Low Light — Add Lamp'
        : activeMetrics.averageBrightness > 225
          ? 'Overexposed — Reduce Glare'
          : 'Balanced Exposure';

  const sharpnessStatus =
    !activeMetrics || activeMetrics.sharpnessIndex === 0
      ? 'Awaiting frame'
      : activeMetrics.sharpnessIndex < 22
        ? 'Soft Focus — Hold Steady'
        : 'Crisp Macro Detail';

  return (
    <section className="bg-white border border-[#E2DDD2] rounded-2xl overflow-hidden shadow-xs">
      {/* Top Viewfinder Mode Header */}
      <div className="flex flex-wrap items-center justify-between gap-3 px-4 sm:px-5 py-3.5 border-b border-[#E2DDD2] bg-[#FAF8F4]">
        <div className="min-w-0">
          <div className="flex items-center gap-2 text-xs text-[#5C5850] font-mono tabular-nums">
            <span>Step 0{marker.stepNumber}</span>
            <span aria-hidden="true">/</span>
            <span>{marker.boundingBox.aspectRatioLabel}</span>
          </div>
          <h2 className="text-lg sm:text-xl font-semibold text-[#181715] truncate">
            {marker.title}
          </h2>
        </div>

        {/* Interactive Mode Switcher (allowed segmented button control) */}
        <div className="flex items-center gap-1 p-1 bg-[#EFEAE0] rounded-lg shrink-0">
          <button
            type="button"
            onClick={() => {
              onClearCapture();
              setCameraMode('camera');
            }}
            className={`min-h-[36px] px-3 py-1.5 text-xs font-medium rounded-md transition-colors flex items-center gap-1.5 whitespace-nowrap shrink-0 cursor-pointer ${
              cameraMode === 'camera' && !captureState.imageDataUrl
                ? 'bg-white text-[#181715] shadow-xs'
                : 'text-[#5C5850] hover:text-[#181715]'
            }`}
          >
            <Camera className="w-3.5 h-3.5" />
            <span>Live Lens</span>
          </button>
          <button
            type="button"
            onClick={() => setCameraMode('upload')}
            className={`min-h-[36px] px-3 py-1.5 text-xs font-medium rounded-md transition-colors flex items-center gap-1.5 whitespace-nowrap shrink-0 cursor-pointer ${
              cameraMode === 'upload' || Boolean(captureState.imageDataUrl)
                ? 'bg-white text-[#181715] shadow-xs'
                : 'text-[#5C5850] hover:text-[#181715]'
            }`}
          >
            <ImagePlus className="w-3.5 h-3.5" />
            <span>Photo Input</span>
          </button>
        </div>
      </div>

      {/* Optical Loupe Viewport */}
      <div className="relative bg-[#111110] aspect-[4/3] w-full overflow-hidden flex items-center justify-center">
        {captureState.imageDataUrl ? (
          <>
            <img
              src={captureState.imageDataUrl}
              alt={`Captured macro for ${marker.title}`}
              referrerPolicy="no-referrer"
              className="w-full h-full object-contain bg-[#111110]"
            />
            {showReticleOnPreview && <BoundingBoxOverlay spec={marker.boundingBox} />}
          </>
        ) : cameraMode === 'camera' ? (
          <>
            <video
              ref={videoRef}
              playsInline
              muted
              style={{ transform: `scale(${zoomLevel})` }}
              className="w-full h-full object-cover transition-transform duration-150"
            />
            {cameraReady && <BoundingBoxOverlay spec={marker.boundingBox} isLiveCamera />}
            {!cameraReady && (
              <div className="absolute inset-0 flex flex-col items-center justify-center p-6 text-center text-white/80">
                <VideoOff className="w-8 h-8 mb-3 text-[#E3B873]" />
                <p className="text-sm font-medium">Initializing optical viewfinder...</p>
                {cameraError && <p className="text-xs text-white/65 mt-1 max-w-sm">{cameraError}</p>}
              </div>
            )}
          </>
        ) : (
          <div className="w-full h-full flex flex-col items-center justify-center p-6 sm:p-10 text-center bg-[#181715] text-white">
            <div className="w-full max-w-md border border-dashed border-white/25 rounded-xl p-6 sm:p-8 flex flex-col items-center">
              <ScanSearch className="w-9 h-9 text-[#E3B873] mb-3" />
              <p className="text-base font-medium text-white">
                Capture or Upload Macro Frame
              </p>
              <p className="text-xs text-white/70 mt-1.5 leading-relaxed">
                Ensure the target fits {marker.boundingBox.targetFillMinPercent}–
                {marker.boundingBox.targetFillMaxPercent}% of the {marker.boundingBox.aspectRatioLabel}{' '}
                frame under diffused lighting.
              </p>

              <div className="mt-5 flex flex-wrap items-center justify-center gap-3">
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="min-h-[44px] px-4 py-2.5 bg-white text-[#181715] text-xs font-semibold rounded-lg hover:bg-[#EFEAE0] transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
                >
                  <ImagePlus className="w-4 h-4" />
                  <span>Select Macro Photo</span>
                </button>
                <button
                  type="button"
                  onClick={() => setCameraMode('camera')}
                  className="min-h-[44px] px-4 py-2.5 bg-white/10 text-white border border-white/20 text-xs font-medium rounded-lg hover:bg-white/20 transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
                >
                  <Camera className="w-4 h-4" />
                  <span>Open Camera</span>
                </button>
              </div>

              {marker.sampleImageUrl && (
                <button
                  type="button"
                  onClick={() => void handleLoadSample()}
                  className="mt-4 min-h-[40px] px-3 py-1.5 text-xs text-[#E3B873] hover:text-white underline underline-offset-4 transition-colors flex items-center gap-1.5 whitespace-nowrap cursor-pointer"
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>Load {marker.sampleLabel || 'Studio Reference Macro'}</span>
                </button>
              )}
            </div>
          </div>
        )}

        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          capture="environment"
          onChange={handleFileChange}
          className="hidden"
        />
      </div>

      {/* Optical Sensor Bar (Brightness & Sharpness Readout) */}
      <div className="px-4 sm:px-5 py-3 border-t border-[#E2DDD2] bg-[#FAF8F4] flex flex-wrap items-center justify-between gap-3 text-xs">
        <div className="flex flex-wrap items-center gap-x-5 gap-y-2 text-[#5C5850]">
          <div className="flex items-center gap-1.5">
            <Sun className="w-3.5 h-3.5 text-[#9A6B2F]" />
            <span className="font-medium text-[#181715]">{brightnessStatus}</span>
            {activeMetrics && activeMetrics.averageBrightness > 0 && (
              <span className="font-mono tabular-nums text-[#5C5850]">
                ({activeMetrics.averageBrightness}/255)
              </span>
            )}
          </div>
          <div className="flex items-center gap-1.5">
            <CheckCircle2 className="w-3.5 h-3.5 text-[#1D6A4E]" />
            <span className="font-medium text-[#181715]">{sharpnessStatus}</span>
            {activeMetrics && activeMetrics.sharpnessIndex > 0 && (
              <span className="font-mono tabular-nums text-[#5C5850]">
                ({activeMetrics.sharpnessIndex}%)
              </span>
            )}
          </div>
        </div>

        {captureState.imageDataUrl && (
          <button
            type="button"
            onClick={() => setShowReticleOnPreview((v) => !v)}
            className="text-xs font-medium text-[#5C5850] hover:text-[#181715] underline underline-offset-4 whitespace-nowrap cursor-pointer"
          >
            {showReticleOnPreview ? 'Hide Bounding Guide' : 'Show Bounding Guide'}
          </button>
        )}
      </div>

      {/* Error Banner */}
      {(localError || cameraError) && (
        <div className="px-4 sm:px-5 py-2.5 bg-[#FDF3F1] border-t border-[#F1CCC5] text-xs text-[#8E2F20]">
          {localError || cameraError}
        </div>
      )}

      {/* Viewfinder Action Controls */}
      <div className="p-4 sm:p-5 border-t border-[#E2DDD2] bg-white flex flex-wrap items-center justify-between gap-3">
        {!captureState.imageDataUrl && cameraMode === 'camera' ? (
          <>
            <div className="flex items-center gap-1.5">
              {ZOOM_OPTIONS.map((z) => (
                <button
                  key={z}
                  type="button"
                  onClick={() => setZoomLevel(z)}
                  className={`min-h-[40px] px-2.5 text-xs font-mono tabular-nums rounded-lg border transition-colors whitespace-nowrap shrink-0 cursor-pointer ${
                    zoomLevel === z
                      ? 'bg-[#181715] text-white border-[#181715]'
                      : 'bg-white text-[#5C5850] border-[#E2DDD2] hover:border-[#181715]'
                  }`}
                >
                  {z.toFixed(1)}×
                </button>
              ))}
              {torchSupported && (
                <button
                  type="button"
                  onClick={() => void handleToggleTorch()}
                  aria-label="Toggle camera flash light"
                  className={`min-h-[40px] min-w-[40px] px-3 rounded-lg border flex items-center justify-center transition-colors cursor-pointer ${
                    torchOn
                      ? 'bg-[#9A6B2F] text-white border-[#9A6B2F]'
                      : 'bg-white text-[#5C5850] border-[#E2DDD2] hover:text-[#181715]'
                  }`}
                >
                  <Flashlight className="w-4 h-4" />
                </button>
              )}
            </div>

            <button
              type="button"
              disabled={!cameraReady}
              onClick={handleCaptureShutter}
              className="min-h-[48px] px-6 py-2.5 bg-[#181715] text-white text-sm font-medium rounded-xl hover:bg-[#2F2D29] disabled:opacity-45 transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
            >
              <Camera className="w-4 h-4" />
              <span>Capture Macro</span>
            </button>
          </>
        ) : captureState.imageDataUrl ? (
          <>
            <div className="flex flex-wrap items-center gap-2">
              <button
                type="button"
                disabled={isAnalyzing}
                onClick={onClearCapture}
                className="min-h-[44px] px-4 py-2 border border-[#E2DDD2] text-[#181715] text-xs font-medium rounded-xl hover:bg-[#FAF8F4] disabled:opacity-50 transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Retake Shot</span>
              </button>
              <button
                type="button"
                disabled={isAnalyzing}
                onClick={() => fileInputRef.current?.click()}
                className="min-h-[44px] px-4 py-2 border border-[#E2DDD2] text-[#5C5850] text-xs font-medium rounded-xl hover:text-[#181715] hover:bg-[#FAF8F4] disabled:opacity-50 transition-colors whitespace-nowrap shrink-0 cursor-pointer"
              >
                Replace Photo
              </button>
            </div>

            <button
              type="button"
              disabled={isAnalyzing}
              onClick={onRunAssessment}
              className="min-h-[48px] px-6 py-2.5 bg-[#9A6B2F] text-white text-sm font-medium rounded-xl hover:bg-[#825823] disabled:opacity-60 transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
            >
              <ScanSearch className="w-4 h-4" />
              <span>
                {isAnalyzing
                  ? 'Inspecting Macro...'
                  : captureState.assessment
                    ? 'Re-Score This Frame'
                    : 'Score Macro Marker'}
              </span>
            </button>
          </>
        ) : (
          <div className="w-full flex flex-wrap items-center justify-between gap-2 text-xs text-[#5C5850]">
            <span>
              Fit target within {marker.boundingBox.targetFillMinPercent}–
              {marker.boundingBox.targetFillMaxPercent}% of box
            </span>
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="min-h-[44px] px-4 py-2 bg-[#181715] text-white text-xs font-medium rounded-xl hover:bg-[#2F2D29] transition-colors whitespace-nowrap shrink-0 cursor-pointer"
            >
              Upload Macro Image
            </button>
          </div>
        )}
      </div>
    </section>
  );
}
