import type { CategoryId, MarkerSpec } from '../types/inspection.ts';

export interface CategoryPreset {
  id: CategoryId;
  name: string;
  shortLabel: string;
  defaultItemName: string;
  description: string;
  markers: MarkerSpec[];
}

export const CATEGORY_PRESETS: CategoryPreset[] = [
  {
    id: 'handbag',
    name: 'Leather Handbag & SLG',
    shortLabel: 'Handbag',
    defaultItemName: 'Structured Calfskin Flap Bag',
    description:
      'Sequential assessment of saddle stitching pitch, heat-stamped serif kerning, hardware engraving, and interior serial tab glazing.',
    markers: [
      {
        id: 'handbag-stitching',
        stepNumber: 1,
        title: 'Saddle Stitching Density & Angle',
        focusFeature: 'Stitch pitch, waxed thread twist & slant angle',
        weight: 0.3,
        lightingRequirement:
          'Diffused indirect daylight or 5000K overhead lamp. Eliminate direct flash glare on leather grain.',
        angleAndDistance:
          'Hold lens perpendicular (90°) 6–10 cm from the seam, capturing 8–12 consecutive stitches across the frame.',
        targetConfirmations: [
          'Consistent 15°–20° saddle stitch slant angle',
          'Uniform stitch pitch and recessed tension holes',
          'Twisted waxed linen thread fiber clarity',
        ],
        targetAnomalies: [
          'Flat machine lock-stitching with zero slant',
          'Irregular hole spacing or torn leather perforations',
          'Synthetic monofilament sheen or frayed loose ends',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Horizontal',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 65,
          targetFillMaxPercent: 85,
          alignmentRule: 'Align stitch seam diagonally or horizontally through the central guide band.',
          reticleType: 'stitch-line',
        },
        sampleImageUrl: '/src/assets/images/macro_leather_stitching_1794973407490.jpg',
        sampleLabel: 'Studio Saddle Stitch Reference',
      },
      {
        id: 'handbag-typography',
        stepNumber: 2,
        title: 'Heat-Stamped Brand Font Kerning',
        focusFeature: 'Letterform serifs, kerning rhythm & foil bleed',
        weight: 0.25,
        lightingRequirement:
          'Soft raking side light (approx. 30° angle) so debossed letter walls and metallic foil edges read cleanly without specular blowout.',
        angleAndDistance:
          'Dead-level straight-on macro at 5–8 cm; keep brand baseline parallel to the horizontal reticle line.',
        targetConfirmations: [
          'Crisp serif terminals and balanced optical kerning',
          'Clean foil containment strictly inside debossed trenches',
          'Consistent baseline alignment across multi-line stamps',
        ],
        targetAnomalies: [
          'Foil bleeding or haloing onto surrounding leather pores',
          'Uneven letter spacing, thick/blobbed strokes, or tilted baseline',
          'Shallow surface paint instead of heat-debossed impression',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Centered Band',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 60,
          targetFillMaxPercent: 85,
          alignmentRule: 'Center the entire heat stamp inside the horizontal typography box within ±3° tilt.',
          reticleType: 'typography-band',
        },
        sampleImageUrl: '/src/assets/images/macro_brand_stamp_1794973408592.jpg',
        sampleLabel: 'Studio Foil Heat-Stamp Reference',
      },
      {
        id: 'handbag-hardware',
        stepNumber: 3,
        title: 'Hardware Engraving & Plating Finish',
        focusFeature: 'CNC engraving depth, screw chamfers & plating grain',
        weight: 0.25,
        lightingRequirement:
          'Bounce light through white paper or shoot under overcast shade to avoid mirror glare on metallic surfaces.',
        angleAndDistance:
          'Straight-on macro (max 5° tilt) filling the box with the lock plate, engraved mark, and screw heads.',
        targetConfirmations: [
          'Clean CNC-milled or laser-crisp flat-bottom engraving trenches',
          'Flush chamfered screw slots without burrs or stripped edges',
          'Fine brushed or uniform electroplated metallic grain',
        ],
        targetAnomalies: [
          'Pitted orange-peel casting texture under plating',
          'Wavy or shallow stamped lettering with rounded walls',
          'Uneven brass/gold tone or flaking corner plating',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Plate Frame',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 65,
          targetFillMaxPercent: 85,
          alignmentRule: 'Fit the full clasp plate and flanking screws within the inner reticle corners.',
          reticleType: 'hardware-plate',
        },
        sampleImageUrl: '/src/assets/images/macro_hardware_clasp_1794973408158.jpg',
        sampleLabel: 'Studio Brushed Hardware Reference',
      },
      {
        id: 'handbag-serial',
        stepNumber: 4,
        title: 'Interior Serial Tab & Edge Glazing',
        focusFeature: 'Date/serial font geometry, tag stitching & edge paint',
        weight: 0.2,
        lightingRequirement:
          'Even interior illumination; diffuse a phone flashlight through tissue paper if shooting inside a deep pocket.',
        angleAndDistance:
          'Close-up perpendicular macro framing the entire serial/date tab and adjacent glazed leather edge.',
        targetConfirmations: [
          'Consistent numeral typeface matching house era conventions',
          'Smooth, multi-coat matte or satin edge paint without cracking',
          'Clean corner reinforcement stitching on tab attachment',
        ],
        targetAnomalies: [
          'Thick, rubbery edge paint spilling onto leather face',
          'Generic sans-serif numerals or smudged ink printing',
          'Crooked tab cut or asymmetric corner stitches',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Interior Tag',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 60,
          targetFillMaxPercent: 85,
          alignmentRule: 'Capture the full serial tab and at least 1 cm of adjacent edge paint inside the box.',
          reticleType: 'serial-tag',
        },
      },
    ],
  },
  {
    id: 'watch',
    name: 'Luxury Mechanical Watch',
    shortLabel: 'Timepiece',
    defaultItemName: 'Swiss Mechanical Steel Chronometer',
    description:
      'Macro pre-screening of dial serif printing, applied index beveling, clasp engraving tolerances, and crown alignment.',
    markers: [
      {
        id: 'watch-dial',
        stepNumber: 1,
        title: 'Dial Printing, Kerning & Indices',
        focusFeature: 'Micro-typography crispness, minute track & marker symmetry',
        weight: 0.35,
        lightingRequirement:
          'Soft diffused side lighting angled to eliminate sapphire crystal reflection across the dial face.',
        angleAndDistance:
          'Perpendicular macro filling the circular dial reticle so the 12, 6, and brand signature are razor sharp.',
        targetConfirmations: [
          'Razor-sharp pad-printed serifs without ink pooling',
          'Centered applied indices aligned cleanly with minute ticks',
          'Uniform sunburst or guilloché dial surface grain',
        ],
        targetAnomalies: [
          'Fuzzy letter edges, uneven kerning, or crooked script',
          'Misaligned hour markers relative to chapter ring ticks',
          'Rough or off-tone lume fill spilling over marker frames',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Dial Loupe',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 70,
          targetFillMaxPercent: 90,
          alignmentRule: 'Center the dial perimeter inside the circular reticle ring; keep 12 o’clock vertical.',
          reticleType: 'dial-circle',
        },
        sampleImageUrl: '/src/assets/images/macro_watch_dial_1794973409291.jpg',
        sampleLabel: 'Studio Swiss Dial Reference',
      },
      {
        id: 'watch-hands',
        stepNumber: 2,
        title: 'Hand Beveling & Center Pinion',
        focusFeature: 'Hand edge finishing, center stack & date window frame',
        weight: 0.25,
        lightingRequirement:
          'Controlled directional light highlighting metallic hand facets without harsh crystal glare.',
        angleAndDistance:
          'Macro shot angled 10°–15° off-axis at 5–7 cm to inspect hand bevels, pinion cap, and dial text.',
        targetConfirmations: [
          'Diamond-cut mirror bevels with clean edge transitions',
          'Neatly finished center pinion cap without tool marks',
          'Crisp date wheel typography centered in aperture',
        ],
        targetAnomalies: [
          'Stamped flat metal hands with rough underside burrs',
          'Hollow or unfinished center pinion tube',
          'Off-center date numerals or uneven cyclops magnification',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Center Macro',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 65,
          targetFillMaxPercent: 85,
          alignmentRule: 'Position center pinion and hour/minute hand tips inside the central target frame.',
          reticleType: 'dial-circle',
        },
        sampleImageUrl: '/src/assets/images/macro_watch_dial_1794973409291.jpg',
        sampleLabel: 'Studio Hand & Pinion Reference',
      },
      {
        id: 'watch-clasp',
        stepNumber: 3,
        title: 'Clasp Engraving & Chamfer Finishing',
        focusFeature: 'Deployment clasp stamps, brushed grain & lug chamfers',
        weight: 0.25,
        lightingRequirement:
          'Softbox or overcast outdoor bounce light to show brushed steel grain and recessed engravings clearly.',
        angleAndDistance:
          'Straight-on macro of open/closed clasp engraving or caseback hallmarks filling 65–85% of frame.',
        targetConfirmations: [
          'Deep, crisp engravings with clean bottom floors',
          'Directional satin brushing parallel to link geometry',
          'Sharp transition between brushed tops and polished chamfers',
        ],
        targetAnomalies: [
          'Shallow acid-etched or blurred laser markings',
          'Rounded, over-polished edges blurring lug transitions',
          'Coarse brushing lines or mismatched metal sheen',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Clasp Frame',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 65,
          targetFillMaxPercent: 85,
          alignmentRule: 'Align clasp engraving horizontally inside the central hardware box.',
          reticleType: 'hardware-plate',
        },
        sampleImageUrl: '/src/assets/images/macro_hardware_clasp_1794973408158.jpg',
        sampleLabel: 'Studio Engraved Clasp Reference',
      },
      {
        id: 'watch-rehaut',
        stepNumber: 4,
        title: 'Rehaut Engraving & Crown Profile',
        focusFeature: 'Inner bezel serial alignment & winding crown fluting',
        weight: 0.15,
        lightingRequirement:
          'Angled soft light illuminating inner rehaut wall and fluted crown teeth without shadowed crevices.',
        angleAndDistance:
          '35° angled macro capturing the 6 o’clock rehaut wall or side profile of the winding crown.',
        targetConfirmations: [
          'Rehaut letters aligned precisely with minute markers',
          'Crisp symmetrical crown logo and uniform knurling depth',
          'Tight bracelet end-link fit against case lugs',
        ],
        targetAnomalies: [
          'Misaligned rehaut text drifting from minute indices',
          'Mushy crown logo relief or wide end-link gaps',
          'Uneven bezel numeral fill or misaligned 12 pip',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Angled Macro',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 60,
          targetFillMaxPercent: 85,
          alignmentRule: 'Frame the crown profile or 6 o’clock rehaut inside the inner calibration bounds.',
          reticleType: 'general-macro',
        },
      },
    ],
  },
  {
    id: 'footwear',
    name: 'Designer Sneakers & Footwear',
    shortLabel: 'Footwear',
    defaultItemName: 'Calfskin Low-Top Court Sneaker',
    description:
      'Macro assessment of upper/midsole stitch pitch, tongue label typography, eyelet hardware plating, and heel symmetry.',
    markers: [
      {
        id: 'footwear-stitching',
        stepNumber: 1,
        title: 'Upper & Midsole Stitch Pitch',
        focusFeature: 'Quarter panel double-stitching & midsole seam tension',
        weight: 0.3,
        lightingRequirement: 'Even daylight illumination highlighting leather pores and recessed stitch channels.',
        angleAndDistance: 'Perpendicular macro at 7–10 cm capturing at least 10 consecutive stitches.',
        targetConfirmations: [
          'Consistent stitch density per inch with parallel tracks',
          'Clean skived leather edges without loose fiber fuzz',
          'Zero adhesive squeeze-out along midsole welt',
        ],
        targetAnomalies: [
          'Wandered stitch tracks or uneven spacing',
          'Visible glue residue or heat-melted synthetic trim',
          'Plastic-like coated leather lacking natural pore variation',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Seam Frame',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 65,
          targetFillMaxPercent: 85,
          alignmentRule: 'Align upper-to-midsole seam horizontally through the stitch guide.',
          reticleType: 'stitch-line',
        },
        sampleImageUrl: '/src/assets/images/macro_leather_stitching_1794973407490.jpg',
        sampleLabel: 'Studio Leather Seam Reference',
      },
      {
        id: 'footwear-typography',
        stepNumber: 2,
        title: 'Tongue & Insole Label Kerning',
        focusFeature: 'Woven tongue tag or gold-stamped insole typography',
        weight: 0.3,
        lightingRequirement: 'Raking soft light across tongue label or insole stamp with zero flash washout.',
        angleAndDistance: 'Dead-level straight-on macro framing the full brand wordmark and size/origin text.',
        targetConfirmations: [
          'Sharp letterform edges and consistent baseline kerning',
          'Tight, high-thread-count woven label backing',
          'Accurate trademark ® or accent glyph proportions',
        ],
        targetAnomalies: [
          'Connected stray threads between adjacent letters',
          'Thick or compressed kerning in origin text',
          'Off-center label placement relative to tongue seam',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Label Frame',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 60,
          targetFillMaxPercent: 85,
          alignmentRule: 'Center the wordmark horizontally within the typography band.',
          reticleType: 'typography-band',
        },
        sampleImageUrl: '/src/assets/images/macro_brand_stamp_1794973408592.jpg',
        sampleLabel: 'Studio Gold Insole Stamp Reference',
      },
      {
        id: 'footwear-hardware',
        stepNumber: 3,
        title: 'Eyelets, Lace Locks & Hardware',
        focusFeature: 'Metallic eyelet finish, engraving & aglet fit',
        weight: 0.2,
        lightingRequirement: 'Diffused soft light avoiding harsh metallic hotspots.',
        angleAndDistance: 'Close-up macro centered on engraved lace lock or top metallic eyelets.',
        targetConfirmations: [
          'Crisp recessed lettering on metal lace dubrae or eyelets',
          'Smooth rolled eyelet rims without sharp burrs',
          'Uniform plating color matching brand specification',
        ],
        targetAnomalies: [
          'Rough cast metal seams or flaking lacquer',
          'Blurred stamped characters',
          'Loose or crooked eyelet crimping',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Hardware Frame',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 60,
          targetFillMaxPercent: 85,
          alignmentRule: 'Center the primary hardware piece inside the inner plate reticle.',
          reticleType: 'hardware-plate',
        },
        sampleImageUrl: '/src/assets/images/macro_hardware_clasp_1794973408158.jpg',
        sampleLabel: 'Studio Hardware Engraving Reference',
      },
      {
        id: 'footwear-heel',
        stepNumber: 4,
        title: 'Heel Counter & Size Tag Geometry',
        focusFeature: 'Heel tab symmetry, rear seam centering & interior size tag',
        weight: 0.2,
        lightingRequirement: 'Balanced neutral room light showing both sides of the rear heel tab evenly.',
        angleAndDistance: 'Straight-on rear macro at 12–15 cm capturing full heel tab symmetry.',
        targetConfirmations: [
          'Symmetrical heel tab curve centered over rear vertical seam',
          'Even box-stitch reinforcement and clean cut leather edge',
          'Crisp interior size tag font and barcode spacing',
        ],
        targetAnomalies: [
          'Asymmetric hourglass heel silhouette or crooked tab',
          'Off-center embossed heel logo',
          'Mismatched stitch count between left and right heel edges',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Symmetry Frame',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 60,
          targetFillMaxPercent: 85,
          alignmentRule: 'Align the rear vertical seam with the center crosshair.',
          reticleType: 'serial-tag',
        },
      },
    ],
  },
  {
    id: 'jewelry',
    name: 'Fine Jewelry & Hardware',
    shortLabel: 'Jewelry',
    defaultItemName: '18k Gold Engraved Bracelet',
    description:
      'Macro screening of purity hallmarks, maker stamp kerning, hinge/clasp machining tolerances, and facet polish.',
    markers: [
      {
        id: 'jewelry-hallmark',
        stepNumber: 1,
        title: 'Hallmark & Maker Stamp Kerning',
        focusFeature: '750/Au750 purity mark, maker signature & serial engraving',
        weight: 0.4,
        lightingRequirement: 'Tented soft light (white paper cone around piece) to reveal dark engraved interiors cleanly.',
        angleAndDistance: 'Macro at 4–6 cm perpendicular to the interior hallmark band.',
        targetConfirmations: [
          'Crisp laser or deep die-struck hallmark walls',
          'Consistent font thickness and serial digit alignment',
          'Clean recessed shield/cartouche borders',
        ],
        targetAnomalies: [
          'Thick, mushy stamped digits or double-struck ghosting',
          'Shallow acid-etched lettering fading at corners',
          'Incorrect spacing between purity mark and house signature',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Hallmark Band',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 60,
          targetFillMaxPercent: 85,
          alignmentRule: 'Align hallmark line horizontally inside the central band.',
          reticleType: 'typography-band',
        },
        sampleImageUrl: '/src/assets/images/macro_hardware_clasp_1794973408158.jpg',
        sampleLabel: 'Studio Engraved Metal Reference',
      },
      {
        id: 'jewelry-clasp',
        stepNumber: 2,
        title: 'Clasp Hinge & Screw Tolerance',
        focusFeature: 'Seam gap tightness, screw slot chamfer & plating integrity',
        weight: 0.35,
        lightingRequirement: 'Diffused neutral white lighting with zero blown specular glare.',
        angleAndDistance: 'Close-up macro centered on closed clasp seam and screw heads.',
        targetConfirmations: [
          'Hairline-tight hinge seam when closed',
          'Precision slotted screw heads sitting flush with surface',
          'Solid precious-metal luster without base-metal bleed',
        ],
        targetAnomalies: [
          'Wide uneven gap or stepped misalignment at clasp joint',
          'Stripped, off-center, or rounded screw slots',
          'Bubbling plating or discoloration inside crevices',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Hardware Plate',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 65,
          targetFillMaxPercent: 85,
          alignmentRule: 'Center the clasp mechanism and screw heads inside the inner brackets.',
          reticleType: 'hardware-plate',
        },
        sampleImageUrl: '/src/assets/images/macro_hardware_clasp_1794973408158.jpg',
        sampleLabel: 'Studio Clasp & Screw Reference',
      },
      {
        id: 'jewelry-facets',
        stepNumber: 3,
        title: 'Bevel Crispness & Stone Setting',
        focusFeature: 'Prong symmetry, pavé bead work & mirror-polished bevels',
        weight: 0.25,
        lightingRequirement: 'Soft diffused overhead light so facet edges reflect cleanly without lens flare.',
        angleAndDistance: '15° angled macro capturing prong tips or chamfered motif edges.',
        targetConfirmations: [
          'Sharp, distortion-free mirror reflections on flat facets',
          'Uniform rounded micro-beads holding any pavé stones',
          'Clean interior gallery finishing without casting porosity',
        ],
        targetAnomalies: [
          'Wavy "funhouse" reflections indicating poor hand-polishing',
          'Pinholes or grainy casting pitting in recessed areas',
          'Uneven prong spacing or visible adhesive around stones',
        ],
        boundingBox: {
          aspectRatioLabel: '4:3 Detail Frame',
          aspectRatioValue: 4 / 3,
          targetFillMinPercent: 60,
          targetFillMaxPercent: 85,
          alignmentRule: 'Fill the central box with the primary motif edge or setting.',
          reticleType: 'general-macro',
        },
      },
    ],
  },
];
