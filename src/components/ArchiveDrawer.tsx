import { Trash2, X } from 'lucide-react';
import type { SavedInspectionRecord } from '../types/inspection.ts';

interface ArchiveDrawerProps {
  isOpen: boolean;
  records: SavedInspectionRecord[];
  onClose: () => void;
  onSelectRecord: (record: SavedInspectionRecord) => void;
  onDeleteRecord: (id: string) => void;
}

export function ArchiveDrawer({
  isOpen,
  records,
  onClose,
  onSelectRecord,
  onDeleteRecord,
}: ArchiveDrawerProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/40 backdrop-blur-xs">
      <div className="w-full max-w-md bg-[#F7F5F0] h-full border-l border-[#E2DDD2] flex flex-col">
        <div className="px-5 py-4 bg-white border-b border-[#E2DDD2] flex items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-serif font-semibold text-[#181715]">
              Inspection Archive
            </h2>
            <p className="text-xs text-[#5C5850]">Saved local pre-screening scorecards</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close archive"
            className="min-h-[44px] min-w-[44px] flex items-center justify-center rounded-lg text-[#5C5850] hover:text-[#181715] hover:bg-[#FAF8F4] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-5 space-y-3">
          {records.length === 0 ? (
            <div className="bg-white border border-[#E2DDD2] rounded-2xl p-6 text-center">
              <p className="text-sm font-medium text-[#181715]">No saved inspections yet</p>
              <p className="text-xs text-[#5C5850] mt-1 leading-relaxed">
                Complete a sequential macro inspection and save the scorecard to revisit it here.
              </p>
            </div>
          ) : (
            records.map((rec) => (
              <div
                key={rec.id}
                className="bg-white border border-[#E2DDD2] rounded-2xl p-4 flex items-center justify-between gap-3"
              >
                <button
                  type="button"
                  onClick={() => onSelectRecord(rec)}
                  className="text-left min-w-0 flex-1 cursor-pointer"
                >
                  <p className="text-sm font-semibold text-[#181715] truncate">{rec.itemName}</p>
                  <div className="flex flex-wrap items-center gap-2 text-xs text-[#5C5850] mt-1 font-mono tabular-nums">
                    <span>Prob {rec.scorecard.overallProbabilityScore}/100</span>
                    <span aria-hidden="true">·</span>
                    <span>Conf {rec.scorecard.overallConfidenceScore}/100</span>
                    <span aria-hidden="true">·</span>
                    <span>{new Date(rec.savedAt).toLocaleDateString()}</span>
                  </div>
                  <p className="text-xs text-[#9A6B2F] mt-1 truncate">
                    {rec.scorecard.verdictBand}
                  </p>
                </button>
                <button
                  type="button"
                  onClick={() => onDeleteRecord(rec.id)}
                  aria-label={`Delete ${rec.itemName}`}
                  className="min-h-[44px] min-w-[44px] flex items-center justify-center text-[#5C5850] hover:text-[#B44332] rounded-lg transition-colors shrink-0 cursor-pointer"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
