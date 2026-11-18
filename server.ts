import 'dotenv/config';
import express from 'express';
import http from 'node:http';
import path from 'node:path';
import fs from 'node:fs';
import { fileURLToPath } from 'node:url';
import { GoogleGenAI, Type, ThinkingLevel } from '@google/genai';
import type {
  ClientOpticalMetrics,
  FinalScorecard,
  MarkerAssessment,
  MarkerSpec,
} from './src/types/inspection.ts';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const MODEL_NAME = 'gemini-4-hydrogen';

function getGeminiClient(): GoogleGenAI {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    throw new Error(
      'GEMINI_API_KEY is not configured. Please add your API key in the Settings > Secrets panel.'
    );
  }
  return new GoogleGenAI({
    apiKey,
    httpOptions: {
      headers: {
        'User-Agent': 'aistudio-build',
      },
    },
  });
}

function parseDataUrl(dataUrl: string): { mimeType: string; base64Data: string } {
  const match = /^data:([a-zA-Z0-9/+.-]+);base64,(.+)$/.exec(dataUrl.trim());
  if (match) {
    return { mimeType: match[1], base64Data: match[2] };
  }
  return { mimeType: 'image/jpeg', base64Data: dataUrl.trim() };
}

function clampScore(value: number): number {
  if (!Number.isFinite(value)) return 0;
  return Math.max(0, Math.min(100, Math.round(value)));
}

function formatUpstreamError(error: unknown): { status: number; message: string } {
  const rawMessage = error instanceof Error ? error.message : String(error);
  if (rawMessage.includes('API_KEY_INVALID') || rawMessage.includes('PERMISSION_DENIED')) {
    return {
      status: 403,
      message:
        'Gemini API authorization failed. Verify GEMINI_API_KEY in Settings > Secrets.',
    };
  }
  if (rawMessage.includes('RESOURCE_EXHAUSTED') || rawMessage.includes('429')) {
    return {
      status: 429,
      message:
        'Gemini API quota limit reached temporarily. Wait a moment and retry the macro scan.',
    };
  }
  if (rawMessage.includes('GEMINI_API_KEY is not configured')) {
    return { status: 500, message: rawMessage };
  }
  return {
    status: 502,
    message: `Macro inspection service error: ${rawMessage}`,
  };
}

const app = express();
app.use(express.json({ limit: '18mb' }));

app.get('/api/health', (_req, res) => {
  res.json({ ok: true, model: MODEL_NAME });
});

app.post('/api/protocol/plan', async (req, res) => {
  try {
    const { itemDescription } = req.body as { itemDescription?: string };
    const cleanItem = (itemDescription ?? '').trim();
    if (!cleanItem || cleanItem.length < 3) {
      res.status(400).json({ error: 'Please describe the luxury item to build a macro protocol.' });
      return;
    }

    const ai = getGeminiClient();
    const response = await ai.models.generateContent({
      model: MODEL_NAME,
      contents: `Design a sequential 4-step visual macro photography pre-screening protocol for: "${cleanItem}".
Each step must ask the user to photograph a specific high-signal macro confirmation or anomaly marker (e.g., stitching density, font kerning, hardware plating/engraving, serial tag or dial/edge finishing) in a well-lit area fitting a 4:3 macro bounding box.
Weights across the 4 steps must sum to approximately 1.0.`,
      config: {
        systemInstruction:
          'You are a senior luxury goods pre-screening specialist designing macro photography checklists for visual probability scoring (never claiming 100% authentication). Keep instructions concrete, measurable, and physically actionable with a smartphone macro camera.',
        thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
        responseMimeType: 'application/json',
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            itemTitle: { type: Type.STRING },
            markers: {
              type: Type.ARRAY,
              items: {
                type: Type.OBJECT,
                properties: {
                  id: { type: Type.STRING },
                  stepNumber: { type: Type.INTEGER },
                  title: { type: Type.STRING },
                  focusFeature: { type: Type.STRING },
                  weight: { type: Type.NUMBER },
                  lightingRequirement: { type: Type.STRING },
                  angleAndDistance: { type: Type.STRING },
                  targetConfirmations: {
                    type: Type.ARRAY,
                    items: { type: Type.STRING },
                  },
                  targetAnomalies: {
                    type: Type.ARRAY,
                    items: { type: Type.STRING },
                  },
                  alignmentRule: { type: Type.STRING },
                  reticleType: {
                    type: Type.STRING,
                    description:
                      'One of: stitch-line, typography-band, hardware-plate, serial-tag, dial-circle, general-macro',
                  },
                },
                required: [
                  'id',
                  'stepNumber',
                  'title',
                  'focusFeature',
                  'weight',
                  'lightingRequirement',
                  'angleAndDistance',
                  'targetConfirmations',
                  'targetAnomalies',
                  'alignmentRule',
                  'reticleType',
                ],
              },
            },
          },
          required: ['itemTitle', 'markers'],
        },
      },
    });

    const parsed = JSON.parse((response.text ?? '{}').trim()) as {
      itemTitle?: string;
      markers?: Array<{
        id: string;
        stepNumber: number;
        title: string;
        focusFeature: string;
        weight: number;
        lightingRequirement: string;
        angleAndDistance: string;
        targetConfirmations: string[];
        targetAnomalies: string[];
        alignmentRule: string;
        reticleType: string;
      }>;
    };

    const allowedReticles = new Set([
      'stitch-line',
      'typography-band',
      'hardware-plate',
      'serial-tag',
      'dial-circle',
      'general-macro',
    ]);

    const rawMarkers = parsed.markers ?? [];
    if (rawMarkers.length === 0) {
      res.status(502).json({ error: 'Unable to generate protocol steps for this item.' });
      return;
    }

    const totalWeight = rawMarkers.reduce((acc, m) => acc + (m.weight > 0 ? m.weight : 0.25), 0);
    const normalizedMarkers: MarkerSpec[] = rawMarkers.slice(0, 5).map((m, idx) => ({
      id: `custom-${idx + 1}-${m.id.replace(/[^a-zA-Z0-9-]/g, '').slice(0, 18) || 'marker'}`,
      stepNumber: idx + 1,
      title: m.title,
      focusFeature: m.focusFeature,
      weight: Number(((m.weight > 0 ? m.weight : 0.25) / totalWeight).toFixed(2)),
      lightingRequirement: m.lightingRequirement,
      angleAndDistance: m.angleAndDistance,
      targetConfirmations: m.targetConfirmations.slice(0, 3),
      targetAnomalies: m.targetAnomalies.slice(0, 3),
      boundingBox: {
        aspectRatioLabel: '4:3 Calibrated Macro',
        aspectRatioValue: 4 / 3,
        targetFillMinPercent: 60,
        targetFillMaxPercent: 85,
        alignmentRule: m.alignmentRule,
        reticleType: (allowedReticles.has(m.reticleType)
          ? m.reticleType
          : 'general-macro') as MarkerSpec['boundingBox']['reticleType'],
      },
    }));

    res.json({
      itemTitle: parsed.itemTitle || cleanItem,
      markers: normalizedMarkers,
    });
  } catch (err) {
    const formatted = formatUpstreamError(err);
    res.status(formatted.status).json({ error: formatted.message });
  }
});

app.post('/api/inspect/marker', async (req, res) => {
  try {
    const {
      imageDataUrl,
      categoryName,
      itemName,
      markerSpec,
      clientMetrics,
    } = req.body as {
      imageDataUrl?: string;
      categoryName?: string;
      itemName?: string;
      markerSpec?: MarkerSpec;
      clientMetrics?: ClientOpticalMetrics;
    };

    if (!imageDataUrl || !markerSpec) {
      res.status(400).json({ error: 'Image capture and marker specification are required.' });
      return;
    }

    const { mimeType, base64Data } = parseDataUrl(imageDataUrl);
    if (!base64Data || base64Data.length < 100) {
      res.status(400).json({ error: 'Captured image payload is empty or invalid.' });
      return;
    }

    const ai = getGeminiClient();

    const prompt = `Inspect this macro photograph for luxury pre-screening step ${markerSpec.stepNumber}: "${markerSpec.title}" on item "${itemName || categoryName || 'Luxury item'}".

REQUIRED CAPTURE STANDARD:
- Target feature: ${markerSpec.focusFeature}
- Lighting specification: ${markerSpec.lightingRequirement}
- Angle & distance specification: ${markerSpec.angleAndDistance}
- Bounding box proportion rule: Target must fill ${markerSpec.boundingBox.targetFillMinPercent}%–${markerSpec.boundingBox.targetFillMaxPercent}% of the ${markerSpec.boundingBox.aspectRatioLabel} frame. ${markerSpec.boundingBox.alignmentRule}
- Client optical sensor reading: Brightness ${clientMetrics?.averageBrightness ?? 'N/A'}/255, Sharpness index ${clientMetrics?.sharpnessIndex ?? 'N/A'}, Resolution ${clientMetrics?.width ?? 'N/A'}x${clientMetrics?.height ?? 'N/A'}.

CONFIRMATIONS TO CHECK:
${markerSpec.targetConfirmations.map((c) => `- ${c}`).join('\n')}

ANOMALIES TO CHECK:
${markerSpec.targetAnomalies.map((a) => `- ${a}`).join('\n')}

STRICT EVALUATION RULES:
1. First evaluate image usability for macro pre-screening:
   - framingScore (0-100): Does the target feature appear inside the frame in the right macro proportion (60-85% fill) and level angle? If the photo is zoomed too far out, cut off, off-subject, or does not show the requested feature, score framingScore below 50.
   - lightingScore (0-100): Is the macro zone evenly lit without dark shadows obscuring detail or harsh blown-out glare?
   - sharpnessScore (0-100): Are fine textures (thread fibers, serif edges, metal grain) crisp enough to inspect?
2. Set qualityPassed = true ONLY if framingScore >= 55 AND lightingScore >= 50 AND sharpnessScore >= 55 AND the requested feature is actually visible in the image.
3. If qualityPassed is false:
   - Set retakeRecommended = true.
   - Lower confidenceScore substantially (must be <= 52, proportional to how unusable the image is).
   - Provide specific retakeAngleInstruction telling the user exact angle, distance, lighting, or bounding-box framing correction needed.
   - Provide concise qualityPenaltyReason explaining why confidence was lowered.
4. Score authenticityScore (0-100) strictly as a visual macro probability signal based on observed craftsmanship markers (do not claim 100% certainty; authentic-consistent studio macros typically score 78-94, ambiguous/mixed score 50-75, clear anomalies score 10-45). If the image is completely off-subject or blurry, keep authenticityScore near 50 (inconclusive) and confidenceScore very low (<= 30).
5. Always list concrete visual confirmations (or note none visible), concrete anomalies or caveats, and one actionable physical humanJudgementNote (what a human appraiser should physically verify alongside this visual check).`;

    const response = await ai.models.generateContent({
      model: MODEL_NAME,
      contents: {
        parts: [
          {
            inlineData: {
              mimeType,
              data: base64Data,
            },
          },
          { text: prompt },
        ],
      },
      config: {
        systemInstruction:
          'You are an AI luxury pre-screening computer vision inspector. Your role is strictly probabilistic visual macro pre-screening (0-100) to augment human judgement—never absolute authentication. Be rigorous about lighting, sharpness, and bounding-box proportion: if image quality is too low to inspect micro-markers reliably, lower the confidence score and specify the exact retake angle.',
        thinkingConfig: { thinkingLevel: ThinkingLevel.MEDIUM },
        responseMimeType: 'application/json',
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            authenticityScore: { type: Type.INTEGER },
            confidenceScore: { type: Type.INTEGER },
            framingScore: { type: Type.INTEGER },
            lightingScore: { type: Type.INTEGER },
            sharpnessScore: { type: Type.INTEGER },
            qualityPassed: { type: Type.BOOLEAN },
            retakeRecommended: { type: Type.BOOLEAN },
            retakeAngleInstruction: { type: Type.STRING, nullable: true },
            qualityPenaltyReason: { type: Type.STRING, nullable: true },
            confirmations: {
              type: Type.ARRAY,
              items: { type: Type.STRING },
            },
            anomalies: {
              type: Type.ARRAY,
              items: { type: Type.STRING },
            },
            humanJudgementNote: { type: Type.STRING },
            summary: { type: Type.STRING },
          },
          required: [
            'authenticityScore',
            'confidenceScore',
            'framingScore',
            'lightingScore',
            'sharpnessScore',
            'qualityPassed',
            'retakeRecommended',
            'confirmations',
            'anomalies',
            'humanJudgementNote',
            'summary',
          ],
        },
      },
    });

    const raw = JSON.parse((response.text ?? '{}').trim()) as Omit<
      MarkerAssessment,
      'markerId' | 'assessedAt'
    >;

    const framingScore = clampScore(raw.framingScore);
    const lightingScore = clampScore(raw.lightingScore);
    const sharpnessScore = clampScore(raw.sharpnessScore);
    const qualityPassed =
      Boolean(raw.qualityPassed) &&
      framingScore >= 55 &&
      lightingScore >= 50 &&
      sharpnessScore >= 55;

    let confidenceScore = clampScore(raw.confidenceScore);
    if (!qualityPassed && confidenceScore > 52) {
      confidenceScore = Math.min(confidenceScore, Math.round((framingScore + lightingScore + sharpnessScore) / 3));
    }

    const assessment: MarkerAssessment = {
      markerId: markerSpec.id,
      authenticityScore: clampScore(raw.authenticityScore),
      confidenceScore,
      framingScore,
      lightingScore,
      sharpnessScore,
      qualityPassed,
      retakeRecommended: !qualityPassed || Boolean(raw.retakeRecommended),
      retakeAngleInstruction:
        raw.retakeAngleInstruction ||
        (!qualityPassed
          ? `Retake perpendicular to ${markerSpec.focusFeature} in diffused light, filling ${markerSpec.boundingBox.targetFillMinPercent}%–${markerSpec.boundingBox.targetFillMaxPercent}% of the bounding box.`
          : null),
      qualityPenaltyReason:
        raw.qualityPenaltyReason ||
        (!qualityPassed
          ? 'Optical sharpness, lighting balance, or bounding-box proportion fell below macro inspection threshold.'
          : null),
      confirmations: (raw.confirmations ?? []).slice(0, 4),
      anomalies: (raw.anomalies ?? []).slice(0, 4),
      humanJudgementNote:
        raw.humanJudgementNote ||
        'Verify physical material weight, tactile finish, and provenance documentation alongside visual inspection.',
      summary: raw.summary || 'Macro marker inspection completed.',
      assessedAt: new Date().toISOString(),
    };

    res.json(assessment);
  } catch (err) {
    const formatted = formatUpstreamError(err);
    res.status(formatted.status).json({ error: formatted.message });
  }
});

app.post('/api/inspect/finalize', async (req, res) => {
  try {
    const { itemName, categoryName, markers, assessments } = req.body as {
      itemName?: string;
      categoryName?: string;
      markers?: MarkerSpec[];
      assessments?: MarkerAssessment[];
    };

    if (!markers?.length || !assessments?.length) {
      res.status(400).json({ error: 'Completed marker assessments are required to generate the scorecard.' });
      return;
    }

    // Deterministic weighted calculation anchored to marker weights and confidence gating
    let weightedProbSum = 0;
    let totalEffectiveWeight = 0;
    let weightedConfSum = 0;
    let totalBaseWeight = 0;
    const lowConfidenceMarkers: string[] = [];

    for (const marker of markers) {
      const result = assessments.find((a) => a.markerId === marker.id);
      if (!result) continue;

      const baseWeight = marker.weight > 0 ? marker.weight : 1 / markers.length;
      const confFactor = Math.max(0.2, result.confidenceScore / 100);
      weightedProbSum += result.authenticityScore * baseWeight * confFactor;
      totalEffectiveWeight += baseWeight * confFactor;
      weightedConfSum += result.confidenceScore * baseWeight;
      totalBaseWeight += baseWeight;

      if (!result.qualityPassed || result.confidenceScore < 60) {
        lowConfidenceMarkers.push(marker.title);
      }
    }

    const overallProbabilityScore =
      totalEffectiveWeight > 0 ? clampScore(weightedProbSum / totalEffectiveWeight) : 0;
    const overallConfidenceScore =
      totalBaseWeight > 0 ? clampScore(weightedConfSum / totalBaseWeight) : 0;

    let verdictBand: FinalScorecard['verdictBand'] = 'Moderate Visual Consistency';
    if (overallConfidenceScore < 55 || lowConfidenceMarkers.length >= Math.ceil(markers.length / 2)) {
      verdictBand = 'Inconclusive — Retake or Human Audit Needed';
    } else if (overallProbabilityScore >= 78) {
      verdictBand = 'High Visual Consistency';
    } else if (overallProbabilityScore >= 58) {
      verdictBand = 'Moderate Visual Consistency';
    } else {
      verdictBand = 'Elevated Anomaly Signal';
    }

    const ai = getGeminiClient();
    const synthesisPrompt = `Synthesize a concise, professional luxury pre-screening executive brief for "${itemName || categoryName || 'Luxury Item'}".
Calculated Weighted Visual Authenticity Probability: ${overallProbabilityScore}/100
Calculated Visual Confidence Index: ${overallConfidenceScore}/100
Verdict Band: ${verdictBand}
Low-Confidence Markers needing retake or physical check: ${lowConfidenceMarkers.length ? lowConfidenceMarkers.join(', ') : 'None'}

MARKER FINDINGS:
${markers
  .map((m) => {
    const a = assessments.find((x) => x.markerId === m.id);
    if (!a) return `- ${m.title}: Not assessed`;
    return `- ${m.title} (Weight ${Math.round(m.weight * 100)}%): Score ${a.authenticityScore}/100, Confidence ${a.confidenceScore}/100, QualityPassed=${a.qualityPassed}. Confirmations: ${a.confirmations.join('; ')}. Anomalies: ${a.anomalies.join('; ')}. Human Note: ${a.humanJudgementNote}`;
  })
  .join('\n')}

Write:
1. executiveSummary (2-3 crisp sentences summarizing visual consistency, any low-confidence image penalties, and clearly stating this is probabilistic visual pre-screening to support human judgement, not a 100% guarantee).
2. prioritizedHumanChecks (3-4 specific physical/tactile checks a human authenticator must perform next).`;

    const response = await ai.models.generateContent({
      model: MODEL_NAME,
      contents: synthesisPrompt,
      config: {
        systemInstruction:
          'You are a senior luxury authentication analyst summarizing macro vision pre-screening results for human appraisers. Be objective, calibrated, and transparent about uncertainty.',
        thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
        responseMimeType: 'application/json',
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            executiveSummary: { type: Type.STRING },
            prioritizedHumanChecks: {
              type: Type.ARRAY,
              items: { type: Type.STRING },
            },
          },
          required: ['executiveSummary', 'prioritizedHumanChecks'],
        },
      },
    });

    const parsed = JSON.parse((response.text ?? '{}').trim()) as {
      executiveSummary?: string;
      prioritizedHumanChecks?: string[];
    };

    const scorecard: FinalScorecard = {
      overallProbabilityScore,
      overallConfidenceScore,
      verdictBand,
      executiveSummary:
        parsed.executiveSummary ||
        `Visual macro screening produced a ${overallProbabilityScore}/100 authenticity probability index with ${overallConfidenceScore}/100 optical confidence.`,
      lowConfidenceMarkers,
      prioritizedHumanChecks: (parsed.prioritizedHumanChecks ?? []).slice(0, 5),
      disclaimer:
        'Visual Macro Pre-Screening Notice: This probability index evaluates photographic surface markers only. It does not inspect internal construction, material weight, olfactory cues, RFID/microchips, or metallurgical composition, and must be combined with qualified human judgement prior to purchase or certification.',
      generatedAt: new Date().toISOString(),
    };

    res.json(scorecard);
  } catch (err) {
    const formatted = formatUpstreamError(err);
    res.status(formatted.status).json({ error: formatted.message });
  }
});

async function startServer() {
  const port = Number(process.env.PORT) || 3000;
  const isProd = process.env.NODE_ENV === 'production';
  const httpServer = http.createServer(app);

  if (!isProd) {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: {
        middlewareMode: true,
        hmr: process.env.DISABLE_HMR === 'true' ? false : { server: httpServer },
        ws: process.env.DISABLE_HMR === 'true' ? false : { server: httpServer },
        watch: process.env.DISABLE_HMR === 'true' ? null : undefined,
      },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distDir = path.resolve(__dirname, 'dist');
    app.use(express.static(distDir));
    app.get('*', (_req, res) => {
      const indexFile = path.join(distDir, 'index.html');
      if (fs.existsSync(indexFile)) {
        res.sendFile(indexFile);
      } else {
        res.status(404).send('Build output not found.');
      }
    });
  }

  httpServer.listen(port, '0.0.0.0', () => {
    console.log(`Loupe Atelier server running on http://0.0.0.0:${port}`);
  });
}

startServer().catch((err) => {
  console.error('Failed to start server:', err);
  process.exit(1);
});
