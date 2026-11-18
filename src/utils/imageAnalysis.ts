import type { ClientOpticalMetrics } from '../types/inspection.ts';

function computeCanvasOpticalMetrics(
  canvas: HTMLCanvasElement,
  sourceWidth: number,
  sourceHeight: number,
  captureSource: ClientOpticalMetrics['captureSource'],
  zoomLevel: number
): ClientOpticalMetrics {
  const ctx = canvas.getContext('2d', { willReadFrequently: true });
  if (!ctx) {
    return {
      averageBrightness: 128,
      sharpnessIndex: 50,
      width: sourceWidth,
      height: sourceHeight,
      captureSource,
      zoomLevel,
    };
  }

  const w = canvas.width;
  const h = canvas.height;
  const { data } = ctx.getImageData(0, 0, w, h);

  // Compute grayscale luminance array & average brightness
  const gray = new Float32Array(w * h);
  let sumLum = 0;
  for (let i = 0; i < w * h; i++) {
    const idx = i * 4;
    const lum = 0.299 * data[idx] + 0.587 * data[idx + 1] + 0.114 * data[idx + 2];
    gray[i] = lum;
    sumLum += lum;
  }
  const averageBrightness = Math.round(sumLum / (w * h));

  // Focus sharpness estimate via central-region Laplacian variance
  const xStart = Math.floor(w * 0.15);
  const xEnd = Math.floor(w * 0.85);
  const yStart = Math.floor(h * 0.15);
  const yEnd = Math.floor(h * 0.85);

  let lapSum = 0;
  let lapSqSum = 0;
  let count = 0;

  for (let y = Math.max(1, yStart); y < Math.min(h - 1, yEnd); y++) {
    for (let x = Math.max(1, xStart); x < Math.min(w - 1, xEnd); x++) {
      const center = gray[y * w + x];
      const lap =
        gray[(y - 1) * w + x] +
        gray[(y + 1) * w + x] +
        gray[y * w + (x - 1)] +
        gray[y * w + (x + 1)] -
        4 * center;
      lapSum += lap;
      lapSqSum += lap * lap;
      count++;
    }
  }

  const mean = count > 0 ? lapSum / count : 0;
  const variance = count > 0 ? lapSqSum / count - mean * mean : 0;
  // Normalize variance into 0-100 practical macro focus scale
  const sharpnessIndex = Math.max(0, Math.min(100, Math.round((Math.sqrt(Math.max(0, variance)) / 32) * 100)));

  return {
    averageBrightness,
    sharpnessIndex,
    width: sourceWidth,
    height: sourceHeight,
    captureSource,
    zoomLevel,
  };
}

export function analyzeVideoLiveMetrics(video: HTMLVideoElement, zoomLevel: number): ClientOpticalMetrics {
  const analysisCanvas = document.createElement('canvas');
  analysisCanvas.width = 160;
  analysisCanvas.height = 120;
  const ctx = analysisCanvas.getContext('2d');
  if (!ctx || video.videoWidth === 0 || video.videoHeight === 0) {
    return {
      averageBrightness: 0,
      sharpnessIndex: 0,
      width: 0,
      height: 0,
      captureSource: 'camera',
      zoomLevel,
    };
  }

  const vw = video.videoWidth;
  const vh = video.videoHeight;
  const cropW = vw / zoomLevel;
  const cropH = vh / zoomLevel;
  const sx = (vw - cropW) / 2;
  const sy = (vh - cropH) / 2;

  ctx.drawImage(video, sx, sy, cropW, cropH, 0, 0, 160, 120);
  return computeCanvasOpticalMetrics(analysisCanvas, Math.round(cropW), Math.round(cropH), 'camera', zoomLevel);
}

export function captureVideoFrame(
  video: HTMLVideoElement,
  zoomLevel: number,
  targetAspectRatio: number
): { dataUrl: string; metrics: ClientOpticalMetrics } {
  const vw = video.videoWidth;
  const vh = video.videoHeight;

  // Apply zoom crop then fit exact target aspect ratio
  const zoomedW = vw / zoomLevel;
  const zoomedH = vh / zoomLevel;

  let cropW = zoomedW;
  let cropH = zoomedW / targetAspectRatio;
  if (cropH > zoomedH) {
    cropH = zoomedH;
    cropW = zoomedH * targetAspectRatio;
  }

  const sx = (vw - cropW) / 2;
  const sy = (vh - cropH) / 2;

  const maxOutWidth = 1280;
  const outW = Math.min(maxOutWidth, Math.round(cropW));
  const outH = Math.round(outW / targetAspectRatio);

  const canvas = document.createElement('canvas');
  canvas.width = outW;
  canvas.height = outH;
  const ctx = canvas.getContext('2d');
  if (!ctx) {
    throw new Error('Could not initialize capture canvas.');
  }

  ctx.drawImage(video, sx, sy, cropW, cropH, 0, 0, outW, outH);
  const dataUrl = canvas.toDataURL('image/jpeg', 0.92);

  const metricsCanvas = document.createElement('canvas');
  metricsCanvas.width = 200;
  metricsCanvas.height = Math.round(200 / targetAspectRatio);
  const mCtx = metricsCanvas.getContext('2d');
  if (mCtx) {
    mCtx.drawImage(canvas, 0, 0, metricsCanvas.width, metricsCanvas.height);
  }

  const metrics = computeCanvasOpticalMetrics(metricsCanvas, outW, outH, 'camera', zoomLevel);
  return { dataUrl, metrics };
}

async function processImageElement(
  img: HTMLImageElement,
  captureSource: ClientOpticalMetrics['captureSource']
): Promise<{ dataUrl: string; metrics: ClientOpticalMetrics }> {
  const maxDim = 1400;
  let outW = img.naturalWidth;
  let outH = img.naturalHeight;

  if (outW > maxDim || outH > maxDim) {
    const scale = maxDim / Math.max(outW, outH);
    outW = Math.round(outW * scale);
    outH = Math.round(outH * scale);
  }

  const canvas = document.createElement('canvas');
  canvas.width = outW;
  canvas.height = outH;
  const ctx = canvas.getContext('2d');
  if (!ctx) {
    throw new Error('Unable to create image processing canvas.');
  }
  ctx.drawImage(img, 0, 0, outW, outH);
  const dataUrl = canvas.toDataURL('image/jpeg', 0.92);

  const mCanvas = document.createElement('canvas');
  mCanvas.width = 200;
  mCanvas.height = Math.max(120, Math.round((200 * outH) / outW));
  const mCtx = mCanvas.getContext('2d');
  if (mCtx) {
    mCtx.drawImage(canvas, 0, 0, mCanvas.width, mCanvas.height);
  }
  const metrics = computeCanvasOpticalMetrics(mCanvas, outW, outH, captureSource, 1);

  return { dataUrl, metrics };
}

export async function loadFileAsMacro(
  file: File
): Promise<{ dataUrl: string; metrics: ClientOpticalMetrics }> {
  const objectUrl = URL.createObjectURL(file);
  try {
    const img = await new Promise<HTMLImageElement>((resolve, reject) => {
      const image = new Image();
      image.onload = () => resolve(image);
      image.onerror = () => reject(new Error('Could not decode selected image file.'));
      image.src = objectUrl;
    });
    return await processImageElement(img, 'upload');
  } finally {
    URL.revokeObjectURL(objectUrl);
  }
}

export async function loadSampleImageAsMacro(
  url: string
): Promise<{ dataUrl: string; metrics: ClientOpticalMetrics }> {
  const img = await new Promise<HTMLImageElement>((resolve, reject) => {
    const image = new Image();
    image.crossOrigin = 'anonymous';
    image.onload = () => resolve(image);
    image.onerror = () => reject(new Error('Could not load reference studio macro image.'));
    image.src = url;
  });
  return await processImageElement(img, 'sample');
}
