import { useState } from 'react';
import { Download, Smartphone, WifiOff, X } from 'lucide-react';
import { useOnlineStatus, usePWAInstall } from '../hooks/usePWAInstall.ts';

export function PWAInstallButton() {
  const { isInstallable, isInstalled, isIOS, isAndroid, install } = usePWAInstall();
  const [showGuide, setShowGuide] = useState(false);

  if (isInstalled) {
    return null;
  }

  if (isInstallable) {
    return (
      <button
        type="button"
        onClick={() => void install()}
        className="min-h-[44px] px-4 py-2 bg-[#181715] text-white text-xs font-medium rounded-xl hover:bg-[#2F2D29] transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
      >
        <Download className="w-4 h-4" />
        <span>Install on Android / Device</span>
      </button>
    );
  }

  return (
    <>
      <button
        type="button"
        onClick={() => setShowGuide(true)}
        className="min-h-[44px] px-4 py-2 border border-[#E2DDD2] text-[#181715] text-xs font-medium rounded-xl hover:bg-[#FAF8F4] transition-colors flex items-center gap-2 whitespace-nowrap shrink-0 cursor-pointer"
      >
        <Smartphone className="w-4 h-4 text-[#9A6B2F]" />
        <span>{isIOS ? 'Install on iOS' : isAndroid ? 'Install Android App' : 'Android Install & Source'}</span>
      </button>

      {showGuide && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white border border-[#E2DDD2] rounded-2xl p-6 space-y-4">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h3 className="text-xl font-serif font-semibold text-[#181715]">
                  Run on Android
                </h3>
                <p className="text-xs text-[#5C5850] mt-0.5">
                  Standalone Mobile App &amp; Native Kotlin Source
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowGuide(false)}
                aria-label="Close install guide"
                className="min-h-[40px] min-w-[40px] flex items-center justify-center rounded-lg text-[#5C5850] hover:text-[#181715] cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {isIOS ? (
              <div className="text-xs text-[#181715] space-y-2 leading-relaxed">
                <p>1. Tap the <strong>Share</strong> icon in Safari.</p>
                <p>2. Tap <strong>Add to Home Screen</strong> to launch Loupe Atelier full-screen.</p>
              </div>
            ) : (
              <div className="text-xs text-[#181715] space-y-2.5 leading-relaxed">
                <p>
                  <strong>Option 1 — Instant Android Install (Standalone PWA):</strong> Open the
                  shared app URL in Chrome on Android and tap{' '}
                  <strong>Menu (⋮) → Install app</strong> (or <strong>Add to Home screen</strong>).
                  It launches full-screen with rear macro camera and torch access.
                </p>
                <p>
                  <strong>Option 2 — Native Android Studio Project:</strong> Full Kotlin + Jetpack
                  Compose + CameraX source code is included in the workspace under{' '}
                  <code className="font-mono bg-[#FAF8F4] px-1.5 py-0.5 rounded border border-[#E2DDD2]">
                    /android
                  </code>{' '}
                  wired to this server’s inspection API.
                </p>
              </div>
            )}

            <button
              type="button"
              onClick={() => setShowGuide(false)}
              className="w-full min-h-[44px] bg-[#181715] text-white text-xs font-medium rounded-xl hover:bg-[#2F2D29] transition-colors cursor-pointer"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </>
  );
}

export function OfflineIndicator() {
  const isOnline = useOnlineStatus();
  if (isOnline) return null;

  return (
    <div className="fixed bottom-20 lg:bottom-4 left-4 z-50 flex items-center gap-2 rounded-xl bg-[#B44332] px-3.5 py-2 text-xs font-medium text-white shadow-lg">
      <WifiOff className="w-3.5 h-3.5 shrink-0" />
      <span>Offline — Cached protocol available; network required for AI scoring.</span>
    </div>
  );
}
