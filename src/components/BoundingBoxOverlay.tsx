import type { BoundingBoxSpec } from '../types/inspection.ts';

interface BoundingBoxOverlayProps {
  spec: BoundingBoxSpec;
  isLiveCamera?: boolean;
}

export function BoundingBoxOverlay({ spec, isLiveCamera = false }: BoundingBoxOverlayProps) {
  return (
    <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center p-4 sm:p-6">
      {/* Subtle outer optical mask */}
      <div className="relative w-full max-w-[82%] aspect-[4/3] border border-white/40 bg-transparent shadow-[0_0_0_9999px_rgba(12,12,11,0.34)]">
        {/* Corner calibration brackets */}
        <span className="absolute -top-px -left-px h-5 w-5 border-t-2 border-l-2 border-[#E3B873]" />
        <span className="absolute -top-px -right-px h-5 w-5 border-t-2 border-r-2 border-[#E3B873]" />
        <span className="absolute -bottom-px -left-px h-5 w-5 border-b-2 border-l-2 border-[#E3B873]" />
        <span className="absolute -bottom-px -right-px h-5 w-5 border-b-2 border-r-2 border-[#E3B873]" />

        {/* Feature-specific optical reticle guides */}
        {spec.reticleType === 'stitch-line' && (
          <div className="absolute inset-x-4 top-1/2 -translate-y-1/2 h-16 border-y border-dashed border-[#E3B873]/75 flex items-center justify-between px-4">
            <div className="h-6 w-px bg-[#E3B873]/60 rotate-[18deg]" />
            <div className="h-6 w-px bg-[#E3B873]/60 rotate-[18deg]" />
            <div className="h-6 w-px bg-[#E3B873]/60 rotate-[18deg]" />
            <div className="h-6 w-px bg-[#E3B873]/60 rotate-[18deg]" />
            <div className="h-6 w-px bg-[#E3B873]/60 rotate-[18deg]" />
            <div className="h-6 w-px bg-[#E3B873]/60 rotate-[18deg]" />
          </div>
        )}

        {spec.reticleType === 'typography-band' && (
          <div className="absolute inset-x-6 top-1/2 -translate-y-1/2 h-20 border border-dashed border-[#E3B873]/80 flex flex-col justify-center">
            <div className="w-full border-t border-[#E3B873]/40" />
          </div>
        )}

        {spec.reticleType === 'hardware-plate' && (
          <div className="absolute inset-6 border border-dashed border-[#E3B873]/75 flex items-center justify-center">
            <div className="h-8 w-8 rounded-full border border-[#E3B873]/70" />
          </div>
        )}

        {spec.reticleType === 'dial-circle' && (
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="h-[82%] aspect-square rounded-full border border-dashed border-[#E3B873]/85 flex items-center justify-center">
              <div className="h-3 w-3 border border-[#E3B873]/80 rounded-full" />
            </div>
          </div>
        )}

        {(spec.reticleType === 'serial-tag' || spec.reticleType === 'general-macro') && (
          <div className="absolute inset-5 border border-dashed border-[#E3B873]/75 flex items-center justify-center">
            <div className="h-4 w-px bg-[#E3B873]/70" />
            <div className="w-4 h-px bg-[#E3B873]/70 -ml-[8.5px]" />
          </div>
        )}

        {/* Top calibration specification line */}
        <div className="absolute -top-7 left-0 right-0 flex items-center justify-between text-[11px] font-mono tabular-nums text-white/90">
          <span>{spec.aspectRatioLabel}</span>
          <span>
            Target Fill {spec.targetFillMinPercent}–{spec.targetFillMaxPercent}%
          </span>
        </div>
      </div>

      {/* Bottom alignment cue */}
      <div className="mt-3 max-w-md text-center">
        <p className="text-xs text-white/90 bg-black/65 px-3 py-1.5 rounded-md backdrop-blur-xs">
          {isLiveCamera ? spec.alignmentRule : `Framing Check: ${spec.alignmentRule}`}
        </p>
      </div>
    </div>
  );
}
