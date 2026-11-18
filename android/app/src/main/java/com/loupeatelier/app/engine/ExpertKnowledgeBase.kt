package com.loupeatelier.app.engine

import com.loupeatelier.app.model.BoundingBoxSpec
import com.loupeatelier.app.model.MarkerSpec

data class CategoryProtocol(
    val id: String,
    val name: String,
    val defaultItemName: String,
    val markers: List<MarkerSpec>,
    val humanChecks: List<String>,
    val isCustomCached: Boolean = false
)

data class HouseReferenceNote(
    val houseName: String,
    val keywords: List<String>,
    val signatureMacroCues: String,
    val eraAndSerialTip: String
)

object ExpertKnowledgeBase {

    val houseReferences: List<HouseReferenceNote> = listOf(
        HouseReferenceNote(
            houseName = "Hermès",
            keywords = listOf("hermes", "hermès", "birkin", "kelly", "constance", "evelyne", "picotin", "lindy"),
            signatureMacroCues = "Hand saddle-stitched 18°–20° waxed lin-thread slant; hammered touret pearling rivets; crisp sans-serif heat stamp without foil halo.",
            eraAndSerialTip = "Blindstamp year letter placement shifted inside left gusset/interior upper panel post-2015 (T–W/K/B/etc.); verify artisan mark crispness."
        ),
        HouseReferenceNote(
            houseName = "Chanel",
            keywords = listOf("chanel", "classic flap", "boy bag", "2.55", "reissue", "woc", "lambskin", "caviar"),
            signatureMacroCues = "High stitch count (10–11 stitches per diamond edge on Classic Flap); centered CC lock overlap (right C over left at top); flat-head screws.",
            eraAndSerialTip = "Pre-2021 items use 7–8 digit serial stickers (series 0–31 with cross-cut/hologram); 2021+ use engraved metal microchip plate."
        ),
        HouseReferenceNote(
            houseName = "Louis Vuitton",
            keywords = listOf("louis vuitton", "lv", "neverfull", "speedy", "keepall", "pochette", "alma", "monogram", "damier"),
            signatureMacroCues = "Coated canvas matte texture with crisp olive/gold ochre LV motifs; mustard waxed cotton thread with clean tear-drop heat-stamp O's.",
            eraAndSerialTip = "Pre-March 2021 uses 2-letter factory code + 4-digit week/year date code; March 2021+ embedded NFC microchip with no printed date code."
        ),
        HouseReferenceNote(
            houseName = "Christian Dior",
            keywords = listOf("dior", "lady dior", "saddle", "book tote", "cannage"),
            signatureMacroCues = "Tight cannage quilt geometry; D.I.O.R. charm eyelets with smooth chamfered collars; crisp serif MADE IN ITALY embossing.",
            eraAndSerialTip = "Leather tag heat-stamped date code format: 2 digits – 2 letters – 4 digits (e.g., 01-MA-0219)."
        ),
        HouseReferenceNote(
            houseName = "Goyard",
            keywords = listOf("goyard", "saint louis", "st louis", "anjou", "artois", "goyardine"),
            signatureMacroCues = "Raised resin-dot chevron hand-screen feel (not flat digital inkjet blur); Y-dot clusters showing organic paint relief; crisp 'GOYARD PARIS' chevron text.",
            eraAndSerialTip = "Leather strap/pocket tab hot-stamped 3-letter workshop + 6-digit production code; check double-stitched handle roots."
        ),
        HouseReferenceNote(
            houseName = "Bottega Veneta",
            keywords = listOf("bottega", "veneta", "intrecciato", "jodie", "cassette", "pouch"),
            signatureMacroCues = "Clean cut fettucce weave strips without fraying filler; unlined or suede interior bonding; crisp triangular hardware geometry.",
            eraAndSerialTip = "White/cream fabric certilogo/serial tag sewn inside pocket with dual-line 16-char alphanumeric code."
        ),
        HouseReferenceNote(
            houseName = "Rolex",
            keywords = listOf("rolex", "submariner", "datejust", "daytona", "gmt", "explorer", "oyster"),
            signatureMacroCues = "Rehaut R-O-L-E-X letters centered on minute hash marks (post-2007); laser-etched crown crystal at 6h; crisp 2.5x cyclops AR coat.",
            eraAndSerialTip = "Modern 6 o'clock rehaut scrambled 8-char alphanumeric serial; verify smooth 28,800 vph sweep and crisp platinum/ceramic bezel fill."
        ),
        HouseReferenceNote(
            houseName = "Omega",
            keywords = listOf("omega", "speedmaster", "seamaster", "aqua terra", "constellation"),
            signatureMacroCues = "Micro-engraved Ω symbol or serial on 7 o'clock/rear lug underside; twisted lyre lug chamfer crispness; ceramic dial laser wave crispness.",
            eraAndSerialTip = "8-digit serial number on lug & movement must match Master Chronometer pictograms."
        ),
        HouseReferenceNote(
            houseName = "Cartier & Van Cleef",
            keywords = listOf("cartier", "love", "juste un clou", "tank", "santos", "van cleef", "vca", "alhambra"),
            signatureMacroCues = "Crisp deep laser/die Au750 + French eagle/dog assay hallmarks; Cartier screw-slot sharp edges without rounding; VCA milgrain pearl uniformity.",
            eraAndSerialTip = "Check exact gram weight against millimeter size/link count; authentic 18k gold is strictly non-magnetic."
        ),
        HouseReferenceNote(
            houseName = "Rimowa",
            keywords = listOf("rimowa", "classic", "original", "essential", "topas", "cabin", "trunk"),
            signatureMacroCues = "Flush domed anodized rivets; crisp multi-wheel Rimowa hub embossing; clean TSA006 lock housing alignment without burrs.",
            eraAndSerialTip = "17-digit serial engraved on outer hinge rail (pre-LVMH) or riveted aluminum plate inside shell."
        )
    )

    fun findHouseReference(itemName: String): HouseReferenceNote? {
        val q = itemName.lowercase().trim()
        if (q.isBlank()) return null
        return houseReferences.firstOrNull { ref ->
            ref.keywords.any { kw -> q.contains(kw) }
        }
    }

    val categories: List<CategoryProtocol> = listOf(
        CategoryProtocol(
            id = "handbag-heritage",
            name = "Heritage Saddle-Stitch Handbags",
            defaultItemName = "Structured Calfskin Flap Bag (Hermès / Kelly / Birkin Style)",
            markers = listOf(
                MarkerSpec(
                    id = "heritage-stitching",
                    stepNumber = 1,
                    title = "Saddle Stitching Slant & Waxed Linen Pitch",
                    focusFeature = "18°–20° hand saddle stitch slant & thread twist",
                    weight = 0.30,
                    lightingRequirement = "Diffused indirect daylight or 5000K lamp. Avoid harsh flash glare on leather grain.",
                    angleAndDistance = "Perpendicular 90° top-down macro at 6–10 cm capturing 8–12 consecutive stitches.",
                    targetConfirmations = listOf(
                        "Consistent 15°–20° saddle stitch slant angle",
                        "Uniform stitch pitch and clean recessed awl holes",
                        "Multi-ply twisted waxed linen fiber definition"
                    ),
                    targetAnomalies = listOf(
                        "Flat straight machine lock-stitching with zero slant",
                        "Irregular hole spacing or torn leather perforations",
                        "Shiny synthetic monofilament thread"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align stitch seam horizontally inside the central guide band.",
                        reticleType = "stitch-line"
                    )
                ),
                MarkerSpec(
                    id = "heritage-typography",
                    stepNumber = 2,
                    title = "Heat-Stamped House Stamp & Serif Kerning",
                    focusFeature = "Foil containment inside debossed letter trenches",
                    weight = 0.25,
                    lightingRequirement = "Soft raking side light (30° angle) without metallic foil blowout.",
                    angleAndDistance = "Dead-level straight-on macro at 5–8 cm aligned with stamp baseline.",
                    targetConfirmations = listOf(
                        "Crisp letter terminals and balanced optical kerning",
                        "Clean foil containment strictly inside debossed walls"
                    ),
                    targetAnomalies = listOf(
                        "Foil bleeding or haloing onto surrounding leather pores",
                        "Thick blobbed strokes or tilted stamp baseline"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center the heat stamp inside the typography box within ±3°.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "heritage-hardware",
                    stepNumber = 3,
                    title = "Plaque Engraving & Pearling Rivets",
                    focusFeature = "CNC engraving floor, hammered pearling pins & plating grain",
                    weight = 0.25,
                    lightingRequirement = "Indirect bounced light through white paper to avoid mirror glare.",
                    angleAndDistance = "Straight-on macro (max 5° tilt) framing plaque and flanking rivets.",
                    targetConfirmations = listOf(
                        "Clean flat-bottom engraving trenches without burrs",
                        "Domed concentric hammered pearling pins sitting flush"
                    ),
                    targetAnomalies = listOf(
                        "Pitted orange-peel casting texture under plating",
                        "Wavy laser etching or protruding hollow screw heads"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Fit plaque and rivets symmetrically inside inner brackets.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "heritage-serial",
                    stepNumber = 4,
                    title = "Blindstamp Date Code & Edge Glazing",
                    focusFeature = "Debossed year/artisan mark crispness & edge resin layers",
                    weight = 0.20,
                    lightingRequirement = "Even diffused interior illumination without deep gusset shadow.",
                    angleAndDistance = "Perpendicular close-up capturing blindstamp and adjacent glazed edge.",
                    targetConfirmations = listOf(
                        "Uniform blindstamp impression without torn leather grain",
                        "Smooth matte/satin multi-coat edge resin without cracking"
                    ),
                    targetAnomalies = listOf(
                        "Thick rubbery edge paint spilling onto leather face",
                        "Soft shallow stamp with uneven pressure depth"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Capture full blindstamp tab and 1 cm of adjacent glazed edge.",
                        reticleType = "serial-tag"
                    )
                )
            ),
            humanChecks = listOf(
                "Verify natural vegetable/chrome-tanned leather aroma vs sharp polyurethane/adhesive odor.",
                "Test sangles/touret turnlock spring tension and weight of solid brass/palladium hardware (non-magnetic).",
                "Inspect interior zipper stop ('H' or house mark) and underside slider engraving.",
                "Cross-reference blindstamp year letter and leather texture (Togo/Epsom/Box/Chèvre) against archive specs."
            )
        ),
        CategoryProtocol(
            id = "handbag-quilted",
            name = "Quilted & Monogram Leather Goods",
            defaultItemName = "Quilted Lambskin / Monogram Flap Bag (Chanel / LV / Dior Style)",
            markers = listOf(
                MarkerSpec(
                    id = "quilted-stitching",
                    stepNumber = 1,
                    title = "Quilt SPI Density & Flap Alignment",
                    focusFeature = "High stitch-per-inch diamond alignment across flap seam",
                    weight = 0.30,
                    lightingRequirement = "Diffused neutral light revealing leather puff and thread channels.",
                    angleAndDistance = "Perpendicular 90° macro covering 2–3 quilt diamonds or seam intersection.",
                    targetConfirmations = listOf(
                        "High stitch density (~10–11 SPI on classic quilts) with tight thread",
                        "Seamless diamond pattern continuity across flap-to-body transition"
                    ),
                    targetAnomalies = listOf(
                        "Low stitch count (7–8 SPI) with visibly long loose stitches",
                        "Shifted quilt diamonds failing to align across flap edge"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align flap seam horizontally along the central guide line.",
                        reticleType = "stitch-line"
                    )
                ),
                MarkerSpec(
                    id = "quilted-typography",
                    stepNumber = 2,
                    title = "Interior Foil Stamp & Serif Proportions",
                    focusFeature = "Brand mark serifs, origin stamp kerning & foil thickness",
                    weight = 0.25,
                    lightingRequirement = "Soft raking side light (30°) avoiding silver/gold foil blowout.",
                    angleAndDistance = "Dead-level straight-on macro at 5–7 cm centered on interior stamp.",
                    targetConfirmations = listOf(
                        "Exact circular O proportions and crisp serif terminals",
                        "Clean debossed wall separation with zero foil bleed"
                    ),
                    targetAnomalies = listOf(
                        "Overly thick gold paint or fuzzy halo around letters",
                        "Compressed kerning in 'PARIS / MADE IN FRANCE / ITALY'"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center full two-line stamp inside typography band.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "quilted-hardware",
                    stepNumber = 3,
                    title = "Turnlock / Snap Engraving & Screw Heads",
                    focusFeature = "CC/clasp bevel sharpness, backplate flat screws & plating",
                    weight = 0.25,
                    lightingRequirement = "Diffused overhead bounce light without specular flash hotspot.",
                    angleAndDistance = "Straight-on macro framing turnlock or interior backplate screws.",
                    targetConfirmations = listOf(
                        "Correct C overlap geometry or crisp snap button engraving",
                        "Clean slotted/star screw heads without tool burrs"
                    ),
                    targetAnomalies = listOf(
                        "Wrong Phillips screw heads where flat/star are specified",
                        "Porous orange-peel gold plating or rounded clasp edges"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Frame lock plate and flanking screws inside inner brackets.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "quilted-serial",
                    stepNumber = 4,
                    title = "Serial Sticker / Date Tag / Microchip Plate",
                    focusFeature = "Hologram sticker serif numerals or engraved metal serial plate",
                    weight = 0.20,
                    lightingRequirement = "Soft diffused light inside corner pocket; avoid direct LED glare.",
                    angleAndDistance = "Close-up macro filling 60–85% with the serial tag or leather date tab.",
                    targetConfirmations = listOf(
                        "House-accurate numeral serifs (e.g. crossed zeros / serifed 1s)",
                        "Clean corner tab stitching and security cross-cuts where applicable"
                    ),
                    targetAnomalies = listOf(
                        "Generic sans-serif inkjet numerals or peeling clear tape",
                        "Misspelled/crooked factory code font"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center serial sticker, microchip plate, or date tab inside box.",
                        reticleType = "serial-tag"
                    )
                )
            ),
            humanChecks = listOf(
                "Check chain-strap leather-weave stitch tension and brass link weight/sound.",
                "Verify NFC/RFID microchip presence (post-2021 LV/Chanel) using a standard NFC reader app.",
                "Feel lambskin/caviar/canvas hand-feel for natural suppleness vs stiff plasticized coating.",
                "Cross-check interior lining color, pocket gusset fold, and zipper pull maker (EP / Lampo / riri)."
            )
        ),
        CategoryProtocol(
            id = "watch-dive",
            name = "Swiss Dive & GMT Timepieces",
            defaultItemName = "Swiss Mechanical Steel Chronometer (Rolex / Omega Style)",
            markers = listOf(
                MarkerSpec(
                    id = "watch-dial",
                    stepNumber = 1,
                    title = "Dial Pad-Printing, Lume & Minute Track",
                    focusFeature = "Micro-typography crispness, applied plot bevels & chapter ring",
                    weight = 0.35,
                    lightingRequirement = "Soft angled side light removing sapphire crystal glare.",
                    angleAndDistance = "Perpendicular macro centering dial inside the circular reticle.",
                    targetConfirmations = listOf(
                        "Razor-sharp pad-printed serifs without ink pooling",
                        "Centered applied hour plots aligned with minute track ticks"
                    ),
                    targetAnomalies = listOf(
                        "Fuzzy letter edges, uneven kerning, or bleeding lume paint",
                        "Crooked 12 o'clock triangle/coronet or off-center date frame"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 70,
                        targetFillMaxPercent = 90,
                        alignmentRule = "Center dial perimeter inside circular reticle; keep 12 o'clock vertical.",
                        reticleType = "dial-circle"
                    )
                ),
                MarkerSpec(
                    id = "watch-clasp",
                    stepNumber = 2,
                    title = "Clasp Coronet, Engraving & Chamfers",
                    focusFeature = "Deployment clasp stamps, 904L/316L brushed grain & sharp bevels",
                    weight = 0.35,
                    lightingRequirement = "Overcast bounce light showing directional satin brushing clearly.",
                    angleAndDistance = "Straight-on macro filling 65–85% of box with clasp blade/engraving.",
                    targetConfirmations = listOf(
                        "Deep clean bottom engraving floors with crisp corners",
                        "Directional satin brushing parallel to bracelet links"
                    ),
                    targetAnomalies = listOf(
                        "Shallow fuzzy laser etching or rounded over-polished chamfers",
                        "Rough gritty metal texture inside clasp blade hinge"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align clasp engraving horizontally inside hardware box.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "watch-rehaut",
                    stepNumber = 3,
                    title = "Rehaut Serial Alignment & Crown Profile",
                    focusFeature = "Inner bezel letter-to-minute alignment & crown fluting",
                    weight = 0.30,
                    lightingRequirement = "Angled soft light illuminating inner rehaut wall and fluted crown.",
                    angleAndDistance = "35° angled macro capturing 6 o'clock rehaut or winding crown.",
                    targetConfirmations = listOf(
                        "Rehaut letters aligned precisely with minute markers",
                        "Tight solid end-link fit flush against case lugs"
                    ),
                    targetAnomalies = listOf(
                        "Drifting rehaut text or mushy crown emblem relief",
                        "Wide end-link gap or uneven ceramic bezel engraving depth"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Frame rehaut wall or crown profile inside inner calibration bounds.",
                        reticleType = "general-macro"
                    )
                )
            ),
            humanChecks = listOf(
                "Weigh watch on a 0.1g precision scale and compare to exact reference/link count.",
                "Test crown winding smoothness, hacking seconds, and crisp midnight instantaneous date snap.",
                "Check 365nm UV lume brightness/decay consistency across hands, dial plots, and bezel pip.",
                "Have a watchmaker inspect balance bridge, free-sprung regulator (no regulator arm on modern calibres), and serial."
            )
        ),
        CategoryProtocol(
            id = "watch-chrono",
            name = "Haute Horlogerie & Dress Chronographs",
            defaultItemName = "Mechanical Chronograph / Dress Watch",
            markers = listOf(
                MarkerSpec(
                    id = "chrono-dial",
                    stepNumber = 1,
                    title = "Sub-Dial Guilloché & Register Typography",
                    focusFeature = "Concentric sub-dial grooves, tachymeter scale & hand pinion cap",
                    weight = 0.40,
                    lightingRequirement = "Diffused soft side light highlighting sub-dial texture without crystal glare.",
                    angleAndDistance = "Perpendicular macro centered on primary dial & sub-register intersection.",
                    targetConfirmations = listOf(
                        "Crisp concentric snailing inside recessed sub-dials",
                        "Cleanly finished center pinion cap and razor-sharp tachymeter print"
                    ),
                    targetAnomalies = listOf(
                        "Rough stamped sub-dial edges or hollow unfinished pinion tube",
                        "Off-center chronograph seconds hand reset relative to 12 index"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 70,
                        targetFillMaxPercent = 90,
                        alignmentRule = "Center main dial and registers inside circular dial reticle.",
                        reticleType = "dial-circle"
                    )
                ),
                MarkerSpec(
                    id = "chrono-case",
                    stepNumber = 2,
                    title = "Caseback Hallmarks & Pusher Chamfers",
                    focusFeature = "Exhibition caseback / gold hallmarks & pusher bezel tolerances",
                    weight = 0.35,
                    lightingRequirement = "Tented diffused light preventing mirror blowouts on polished lugs.",
                    angleAndDistance = "Straight-on macro framing caseback engraving or lug bevel transitions.",
                    targetConfirmations = listOf(
                        "Sharp Geneva seal/precious metal stamps and crisp lug bevels",
                        "Uniform brushed/polished flank transitions without waviness"
                    ),
                    targetAnomalies = listOf(
                        "Wavy distortion in mirror polish or shallow acid-etched text",
                        "Mismatched caseback tooth machining or loose pusher play"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Frame caseback hallmarks or lug/pusher profile in hardware box.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "chrono-strap",
                    stepNumber = 3,
                    title = "Deployant Buckle & Alligator Stitch Finish",
                    focusFeature = "Buckle hallmark crispness & strap keeper hand-stitching",
                    weight = 0.25,
                    lightingRequirement = "Neutral indirect light showing buckle stamp and leather pores.",
                    angleAndDistance = "Perpendicular close-up of deployant buckle underside or tang stamp.",
                    targetConfirmations = listOf(
                        "Deep maker stamp and crisp spring-bar hole chamfering",
                        "Clean edge paint and saddle-stitched strap keeper"
                    ),
                    targetAnomalies = listOf(
                        "Pitted underside casting or thick sloppy strap glue",
                        "Blurred maker signature or misaligned tongue slot"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center buckle inner plate horizontally inside guide.",
                        reticleType = "typography-band"
                    )
                )
            ),
            humanChecks = listOf(
                "Actuate chronograph start/stop/reset pushers for crisp column-wheel snap and exact 12-zero return.",
                "Inspect exhibition movement bridges for real anglage, perlage, and engraved (not glued) rotor markings.",
                "Verify total gram mass and precious metal hallmarks (750 / St. Bernard dog / balance scale)."
            )
        ),
        CategoryProtocol(
            id = "footwear",
            name = "Designer Sneakers & Court Footwear",
            defaultItemName = "Calfskin Low-Top Court Sneaker",
            markers = listOf(
                MarkerSpec(
                    id = "footwear-stitching",
                    stepNumber = 1,
                    title = "Upper & Midsole Stitch Pitch",
                    focusFeature = "Quarter panel stitch density & midsole welt cleanliness",
                    weight = 0.35,
                    lightingRequirement = "Even daylight illuminating leather pores and recessed stitch channels.",
                    angleAndDistance = "Perpendicular macro at 7–10 cm capturing 10 consecutive stitches.",
                    targetConfirmations = listOf(
                        "Consistent stitch density per inch with parallel tracks",
                        "Zero adhesive squeeze-out along midsole welt"
                    ),
                    targetAnomalies = listOf(
                        "Wandered stitch tracks or uneven spacing",
                        "Visible glue residue or heat-melted synthetic trim"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align upper-to-midsole seam horizontally through the stitch guide.",
                        reticleType = "stitch-line"
                    )
                ),
                MarkerSpec(
                    id = "footwear-typography",
                    stepNumber = 2,
                    title = "Tongue, Insole & Size Tag Kerning",
                    focusFeature = "Woven tongue label or stamped insole & size tag typography",
                    weight = 0.35,
                    lightingRequirement = "Raking soft light across label without flash washout.",
                    angleAndDistance = "Dead-level macro framing brand wordmark and origin text.",
                    targetConfirmations = listOf(
                        "Sharp letterform edges and consistent baseline kerning",
                        "Tight high-thread-count woven label backing"
                    ),
                    targetAnomalies = listOf(
                        "Connected stray threads between adjacent letters",
                        "Thick compressed kerning in origin text or off-font digits"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center the wordmark horizontally within the typography band.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "footwear-hardware",
                    stepNumber = 3,
                    title = "Eyelets, Lace Locks & Heel Symmetry",
                    focusFeature = "Metallic eyelet finish & rear heel tab geometry",
                    weight = 0.30,
                    lightingRequirement = "Balanced neutral light highlighting hardware rims and heel seam.",
                    angleAndDistance = "Close-up macro centered on metallic eyelets or heel tab.",
                    targetConfirmations = listOf(
                        "Smooth rolled eyelet rims without sharp burrs",
                        "Symmetrical heel tab centered over vertical seam"
                    ),
                    targetAnomalies = listOf(
                        "Rough cast metal seams or flaking lacquer",
                        "Crooked heel tab or asymmetric stitch count"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center primary hardware or heel seam inside calibration brackets.",
                        reticleType = "hardware-plate"
                    )
                )
            ),
            humanChecks = listOf(
                "Flex midsole and outsole to verify genuine rubber density vs hollow foam.",
                "Inspect insole underside stitching, strobel board pattern, and footbed glue lines.",
                "Check 365nm UV fluorescence on tongue tag, size label, and invisible midsole guide marks."
            )
        ),
        CategoryProtocol(
            id = "jewelry",
            name = "Fine Jewelry & Precious Hardware",
            defaultItemName = "18k Gold Engraved Bracelet (Cartier / VCA / Tiffany Style)",
            markers = listOf(
                MarkerSpec(
                    id = "jewelry-hallmark",
                    stepNumber = 1,
                    title = "Hallmark & Maker Stamp Kerning",
                    focusFeature = "750/Au750 purity mark & maker signature crispness",
                    weight = 0.40,
                    lightingRequirement = "Tented soft light (white paper diffuser) revealing dark engraved walls.",
                    angleAndDistance = "Macro at 4–6 cm perpendicular to interior hallmark band.",
                    targetConfirmations = listOf(
                        "Crisp laser or die-struck hallmark walls",
                        "Consistent digit alignment inside recessed cartouche"
                    ),
                    targetAnomalies = listOf(
                        "Thick mushy stamped digits or double-struck ghosting",
                        "Shallow acid-etched lettering fading at corners"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Align hallmark line horizontally inside the central band.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "jewelry-clasp",
                    stepNumber = 2,
                    title = "Clasp Hinge, Screws & Pavé Tolerances",
                    focusFeature = "Seam gap tightness, screw slot chamfer & mirror finish",
                    weight = 0.35,
                    lightingRequirement = "Diffused neutral white lighting with zero specular blowout.",
                    angleAndDistance = "Close-up macro centered on closed clasp seam and screw heads.",
                    targetConfirmations = listOf(
                        "Hairline-tight hinge seam when closed",
                        "Flush slotted screw heads with sharp crisp chamfers"
                    ),
                    targetAnomalies = listOf(
                        "Wide uneven gap or stepped misalignment at clasp joint",
                        "Pitted casting porosity or flaking plating"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Center clasp joint and screw heads inside the inner brackets.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "jewelry-bevels",
                    stepNumber = 3,
                    title = "Bevel Crispness & Stone Setting Gallery",
                    focusFeature = "Prong / milgrain bead uniformity & distortion-free facet polish",
                    weight = 0.25,
                    lightingRequirement = "Diffused overhead soft light without direct flash flare.",
                    angleAndDistance = "15° angled macro capturing motif edge or stone prongs.",
                    targetConfirmations = listOf(
                        "Uniform rounded milgrain beads / symmetric prongs",
                        "Sharp flat facet reflections without wavy polishing marks"
                    ),
                    targetAnomalies = listOf(
                        "Porous pinholes inside gallery or glue residue around stones",
                        "Rounded blunted edges on screw/motif contours"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Fill central macro frame with primary motif edge or setting.",
                        reticleType = "general-macro"
                    )
                )
            ),
            humanChecks = listOf(
                "Verify precious-metal density and gram weight to 0.01g against exact bracelet/ring size chart.",
                "Confirm strictly non-magnetic response using a neodymium magnet test.",
                "Perform non-destructive XRF alloy verification at a certified jeweler if any doubt remains."
            )
        ),
        CategoryProtocol(
            id = "eyewear",
            name = "Luxury Eyewear & Acetate Frames",
            defaultItemName = "Hand-Finished Acetate / Titanium Sunglasses",
            markers = listOf(
                MarkerSpec(
                    id = "eyewear-typography",
                    stepNumber = 1,
                    title = "Inner Temple Model, Size & CE Stamp",
                    focusFeature = "Micro-pad-printed or hot-foil temple typography & spacing",
                    weight = 0.40,
                    lightingRequirement = "Soft side lighting along inner temple arm without glare streak.",
                    angleAndDistance = "Dead-level macro at 5–7 cm aligned with inner temple code.",
                    targetConfirmations = listOf(
                        "Crisp spaced model number, bridge-temple dimensions & CE mark",
                        "Clean ink alignment parallel to acetate bevel"
                    ),
                    targetAnomalies = listOf(
                        "Smudged painted characters, wrong spacing in CE mark, or crooked text",
                        "Mismatched font weight between brand mark and origin stamp"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Align inner temple typography horizontally inside typography band.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "eyewear-hinge",
                    stepNumber = 2,
                    title = "Barrel Hinge Machining & Pin Rivets",
                    focusFeature = "Multi-barrel hinge tight tolerances, screw chamfer & front pins",
                    weight = 0.35,
                    lightingRequirement = "Diffused neutral light inspecting metal hinge barrels and temple pins.",
                    angleAndDistance = "Close-up macro centered on open/half-closed hinge barrel.",
                    targetConfirmations = listOf(
                        "Tight 5- or 7-barrel interlocking hinge seams with zero wobble",
                        "Flush pinned rivets passing cleanly through acetate front"
                    ),
                    targetAnomalies = listOf(
                        "Glued cosmetic pins not penetrating hinge plate inside",
                        "Rough stamped hinge burrs or loose gap tolerances"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Center hinge barrel and screw head inside hardware box.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "eyewear-bridge",
                    stepNumber = 3,
                    title = "Acetate Bevel Polish & Wirecore Detail",
                    focusFeature = "Hand-beveled acetate edges, nosepad solder & temple wirecore",
                    weight = 0.25,
                    lightingRequirement = "Indirect daylight revealing translucent acetate finish or titanium welds.",
                    angleAndDistance = "Macro at 6–8 cm capturing bridge/nosepad joint or engraved wirecore.",
                    targetConfirmations = listOf(
                        "Glass-smooth beveled acetate without mold seam flash lines",
                        "Clean engraved metal wirecore visible cleanly inside temple"
                    ),
                    targetAnomalies = listOf(
                        "Visible injection-molded plastic seam line on inner bridge",
                        "Blobby solder joints at nosepad arms"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Frame nosebridge joint or wirecore inside central macro guide.",
                        reticleType = "general-macro"
                    )
                )
            ),
            humanChecks = listOf(
                "Verify crisp temple folding resistance and smooth balance on a flat surface (no twisted frame).",
                "Check etched micro-brand mark on upper lens corner under raking light.",
                "Test polarized/UV400 lens optical clarity and confirm temple wirecore extends full length."
            )
        ),
        CategoryProtocol(
            id = "luggage",
            name = "Luxury Luggage & Aluminum / Canvas Trunks",
            defaultItemName = "Anodized Aluminum / Monogram Cabin Trunk (Rimowa / LV Style)",
            markers = listOf(
                MarkerSpec(
                    id = "luggage-hardware",
                    stepNumber = 1,
                    title = "TSA Lock Housing, Rivets & Corner Plates",
                    focusFeature = "Domed rivet seating, drawbolt clasp crispness & metal grain",
                    weight = 0.40,
                    lightingRequirement = "Diffused overcast light revealing anodized/brushed metal finish without glare.",
                    angleAndDistance = "Straight-on macro framing TSA lock latch or corner bracket rivets.",
                    targetConfirmations = listOf(
                        "Flush domed rivets with zero burred collar edges",
                        "Crisp stamped/cast lock numerals and tight housing seating"
                    ),
                    targetAnomalies = listOf(
                        "Crooked rivet caps, loose housing gap, or rough cast burrs",
                        "Thin flaking chrome paint over plastic lock bezel"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Center lock latch or corner bracket rivets inside hardware frame.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "luggage-serial",
                    stepNumber = 2,
                    title = "Serial Plate & Embossed Logo Kerning",
                    focusFeature = "Engraved/lasered 17-digit serial or leather badge typography",
                    weight = 0.35,
                    lightingRequirement = "Soft angled raking light illuminating engraved/stamped characters cleanly.",
                    angleAndDistance = "Perpendicular macro centered on hinge rail serial or badge stamp.",
                    targetConfirmations = listOf(
                        "Razor-sharp laser/die-engraved serial numerals with uniform spacing",
                        "Clean brand plaque inlay sitting flush inside shell recess"
                    ),
                    targetAnomalies = listOf(
                        "Blurred printed serial sticker or crooked glued plaque",
                        "Inconsistent font weight across serial digits"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Align serial number or badge text horizontally in typography band.",
                        reticleType = "serial-tag"
                    )
                ),
                MarkerSpec(
                    id = "luggage-trim",
                    stepNumber = 3,
                    title = "Leather Handle Stitching & Wheel Housing",
                    focusFeature = "Handle wrap saddle stitches or multi-wheel hub machining",
                    weight = 0.25,
                    lightingRequirement = "Even neutral light across handle seam or wheel hub emblem.",
                    angleAndDistance = "Macro at 8–10 cm filling 65–85% with handle seam or wheel hub.",
                    targetConfirmations = listOf(
                        "Tight heavy waxed thread stitching on leather grip trim",
                        "Clean molded wheel hub tolerances without plastic flash"
                    ),
                    targetAnomalies = listOf(
                        "Loose frayed synthetic thread or crooked handle wrap",
                        "Rough plastic parting seams around wheel axle housing"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align handle seam horizontally or center wheel hub inside frame.",
                        reticleType = "stitch-line"
                    )
                )
            ),
            humanChecks = listOf(
                "Test telescopic handle extension smoothness and multi-stage locking clicks without rattle.",
                "Verify silent 360° dual-bearing wheel spin balance across hard flooring.",
                "Inspect interior flex-divider zipper pulls, hinge stops, and shell alloy gauge/rigidity."
            )
        )
    )
}
