package com.loupeatelier.app.engine

import com.loupeatelier.app.model.BoundingBoxSpec
import com.loupeatelier.app.model.MarkerSpec

data class CategoryProtocol(
    val id: String,
    val name: String,
    val defaultItemName: String,
    val markers: List<MarkerSpec>,
    val humanChecks: List<String>,
    val isAdaptiveCustom: Boolean = false
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
            keywords = listOf("hermes", "hermès", "birkin", "kelly", "constance", "evelyne", "picotin", "lindy", "carré", "carre", "h belt"),
            signatureMacroCues = "Hand saddle-stitched 18°–20° waxed lin-thread slant; hammered touret pearling rivets; crisp sans-serif heat stamp without foil halo; hand-rolled inward roulotté edges on silk.",
            eraAndSerialTip = "Blindstamp year letter shifted inside left gusset/upper interior panel post-2015 (T–W/K/B); silk care tags use fine French serif typography."
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
            signatureMacroCues = "Coated canvas matte grain with crisp olive/gold ochre LV motifs; mustard waxed thread with clean tear-drop heat-stamp O's.",
            eraAndSerialTip = "Pre-March 2021 uses 2-letter factory code + 4-digit week/year date code; March 2021+ embedded NFC microchip with no printed date code."
        ),
        HouseReferenceNote(
            houseName = "Christian Dior",
            keywords = listOf("dior", "lady dior", "saddle", "book tote", "cannage"),
            signatureMacroCues = "Tight cannage quilt geometry; D.I.O.R. charm eyelets with smooth chamfered collars; crisp serif MADE IN ITALY embossing.",
            eraAndSerialTip = "Leather tag heat-stamped date code format: 2 digits – 2 letters – 4 digits (e.g., 01-MA-0219)."
        ),
        HouseReferenceNote(
            houseName = "Gucci",
            keywords = listOf("gucci", "marmont", "jackie", "dionysus", "horsebit", "ophidia", "supreme"),
            signatureMacroCues = "Tight double-G web/canvas symmetry; crisp Italian leather serif '® GUCCI made in italy' heat-stamp with distinct curled serif R.",
            eraAndSerialTip = "Underside of leather tag bears dual-row 6-digit + 4/6-digit model/supplier serial in tall serif numerals; modern bags include NFC tag."
        ),
        HouseReferenceNote(
            houseName = "Prada",
            keywords = listOf("prada", "saffiano", "galleria", "re-nylon", "cleoh"),
            signatureMacroCues = "Enamel triangle plaque with crisp 'DAL 1913' spacing and distinct curved 'R' notch; corner rivets flush with enamel lip.",
            eraAndSerialTip = "Interior white rectangular factory tag carries 2–3 digit black serif number; check Lampo/Ipi/Riri underside zipper markings."
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
            houseName = "Burberry",
            keywords = listOf("burberry", "trench", "heritage", "check", "kensington", "chelsea"),
            signatureMacroCues = "Symmetric Nova/Vintage check alignment across collar, pocket flaps, and placket seams; crisp horn/corozo buttons with clean laser rim text.",
            eraAndSerialTip = "Main neck label matches era font weight; check interior multi-page fabric composition & care tag stitch line."
        ),
        HouseReferenceNote(
            houseName = "Moncler",
            keywords = listOf("moncler", "maya", "puffer", "down", "grenoble"),
            signatureMacroCues = "Dense felt sleeve crest with tight zig-zag border stitching (never overlapping letters); crisp comic strip wash label ink without bleed.",
            eraAndSerialTip = "Certilogo QR/12-digit code tag + RFID tag inside interior left seam."
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
            houseName = "Audemars & Patek",
            keywords = listOf("patek", "philippe", "nautilus", "aquanaut", "audemars", "royal oak", "ap"),
            signatureMacroCues = "Razor-sharp hexagonal bezel screws aligned cleanly in slots (Royal Oak); uniform tapisserie / embossed wave dial relief; hand-beveled clasp bridges.",
            eraAndSerialTip = "Inspect precious metal hallmarks (750 / St. Bernard dog) and crisp movement bridge anglage under loupe."
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
        ),
        HouseReferenceNote(
            houseName = "Montblanc",
            keywords = listOf("montblanc", "meisterstück", "meisterstuck", "149", "146", "starwalker", "fountain"),
            signatureMacroCues = "Deep crisp two-tone gold nib engraving ('4810 / M / 18K'); symmetric slit cut & rounded iridium tipping; clean resin seam polish.",
            eraAndSerialTip = "Clip ring carries crisp 'GERMANY' + individual laser serial; verify precious-resin warm cherry glow under strong flashlight."
        )
    )

    fun findHouseReference(itemName: String): HouseReferenceNote? {
        val q = itemName.lowercase().trim()
        if (q.isBlank()) return null
        return houseReferences.firstOrNull { ref ->
            ref.keywords.any { kw -> q.contains(kw) }
        }
    }

    /**
     * Synthesizes a tailored offline inspection protocol for ANY unlisted item purely on-device
     * using material & craftsmanship keyword routing—requiring zero network calls.
     */
    fun buildAdaptiveOfflineProtocol(itemName: String): CategoryProtocol {
        val cleanName = itemName.trim().ifBlank { "Custom Luxury Item" }
        val q = cleanName.lowercase()
        val house = findHouseReference(cleanName)

        // Direct best-match routing across our 12 specialized templates when obvious
        val matchedTemplate = when {
            listOf("belt", "buckle").any { q.contains(it) } -> categories.firstOrNull { it.id == "belts" }
            listOf("scarf", "silk", "shawl", "cashmere", "carré", "carre").any { q.contains(it) } -> categories.firstOrNull { it.id == "textiles" }
            listOf("jacket", "coat", "trench", "parka", "puffer", "hoodie", "blazer", "moncler", "burberry").any { q.contains(it) } -> categories.firstOrNull { it.id == "outerwear" }
            listOf("pen", "fountain", "nib", "montblanc", "lighter", "dupont").any { q.contains(it) } -> categories.firstOrNull { it.id == "writing" }
            listOf("sunglass", "eyewear", "glasses", "frames", "acetate").any { q.contains(it) } -> categories.firstOrNull { it.id == "eyewear" }
            listOf("luggage", "suitcase", "trunk", "rimowa", "cabin").any { q.contains(it) } -> categories.firstOrNull { it.id == "luggage" }
            listOf("chrono", "patek", "nautilus", "audemars", "dress watch").any { q.contains(it) } -> categories.firstOrNull { it.id == "watch-chrono" }
            listOf("watch", "rolex", "omega", "submariner", "datejust", "gmt", "seamaster", "speedmaster").any { q.contains(it) } -> categories.firstOrNull { it.id == "watch-dive" }
            listOf("sneaker", "shoe", "boot", "loafer", "trainer", "heel").any { q.contains(it) } -> categories.firstOrNull { it.id == "footwear" }
            listOf("ring", "bracelet", "necklace", "earring", "cartier", "alhambra", "gold", "jewelry").any { q.contains(it) } -> categories.firstOrNull { it.id == "jewelry" }
            listOf("chanel", "louis vuitton", "lv", "dior", "gucci", "monogram", "quilted").any { q.contains(it) } -> categories.firstOrNull { it.id == "handbag-quilted" }
            listOf("bag", "tote", "clutch", "wallet", "birkin", "kelly", "hermes", "hermès", "goyard", "bottega", "prada").any { q.contains(it) } -> categories.firstOrNull { it.id == "handbag-heritage" }
            else -> null
        }

        if (matchedTemplate != null) {
            return matchedTemplate.copy(
                id = "adaptive-${matchedTemplate.id}",
                name = "Tailored Offline · $cleanName",
                defaultItemName = cleanName,
                humanChecks = buildList {
                    if (house != null) {
                        add("${house.houseName} Signature Check: ${house.signatureMacroCues}")
                        add("${house.houseName} Serial/Era Check: ${house.eraAndSerialTip}")
                    }
                    addAll(matchedTemplate.humanChecks)
                },
                isAdaptiveCustom = true
            )
        }

        // Universal 4-pillar craftsmanship protocol for any completely novel luxury object
        return CategoryProtocol(
            id = "adaptive-universal",
            name = "Universal Craftsmanship · $cleanName",
            defaultItemName = cleanName,
            markers = listOf(
                MarkerSpec(
                    id = "univ-typography",
                    stepNumber = 1,
                    title = "Primary Brand Stamp & Serial Typography",
                    focusFeature = "Letterform serifs, kerning rhythm & ink/foil containment",
                    weight = 0.30,
                    lightingRequirement = "Soft raking side light (30° angle) without glare blowout.",
                    angleAndDistance = "Dead-level straight-on macro at 5–8 cm centered on maker mark.",
                    targetConfirmations = listOf(
                        "Crisp edge definition without ink bleed or haloing",
                        "Consistent kerning and straight horizontal baseline"
                    ),
                    targetAnomalies = listOf(
                        "Thick blobbed strokes or uneven character spacing",
                        "Shallow surface paint rather than crisp impression"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center brand mark inside the horizontal typography guide.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "univ-stitch",
                    stepNumber = 2,
                    title = "Seam Stitching / Structural Join Regularity",
                    focusFeature = "Stitch pitch consistency or precision joint seam cleanliness",
                    weight = 0.25,
                    lightingRequirement = "Diffused indirect daylight revealing surface grain cleanly.",
                    angleAndDistance = "Perpendicular 90° macro covering 8–12 stitches or primary seam.",
                    targetConfirmations = listOf(
                        "Uniform stitch pitch or tight zero-gap material join",
                        "Clean finished edges without frayed fiber or glue bleed"
                    ),
                    targetAnomalies = listOf(
                        "Irregular hole spacing, loose threads, or adhesive squeeze-out",
                        "Rough unskived edges or uneven tension"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align primary seam horizontally through the central guide.",
                        reticleType = "stitch-line"
                    )
                ),
                MarkerSpec(
                    id = "univ-hardware",
                    stepNumber = 3,
                    title = "Metal Hardware, Fasteners & Plating Grain",
                    focusFeature = "Engraving floor clarity, screw chamfers & surface polish",
                    weight = 0.25,
                    lightingRequirement = "Bounced overcast light avoiding mirror hotspots on metal.",
                    angleAndDistance = "Straight-on macro framing hardware plate, clasp, or fasteners.",
                    targetConfirmations = listOf(
                        "Clean CNC-milled or die-struck markings with sharp bevels",
                        "Uniform brushed/polished grain without pitting"
                    ),
                    targetAnomalies = listOf(
                        "Orange-peel casting porosity under thin plating",
                        "Stripped/burred fastener slots or flaking coating"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Fit primary metallic hardware inside inner reticle brackets.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "univ-serial",
                    stepNumber = 4,
                    title = "Care / Serial Label & Edge Finishing",
                    focusFeature = "Serial numeral typeface, tag attachment & border finishing",
                    weight = 0.20,
                    lightingRequirement = "Even diffused interior illumination without deep shadows.",
                    angleAndDistance = "Perpendicular close-up framing serial/care tag and adjacent edge.",
                    targetConfirmations = listOf(
                        "Crisp consistent numeral printing and neat corner reinforcement",
                        "Smooth multi-coat edge paint or clean hem finish"
                    ),
                    targetAnomalies = listOf(
                        "Smudged inkjet numerals or crooked tag stitching",
                        "Thick uneven edge paint spilling onto surface"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Capture full serial/care tag and adjacent edge inside frame.",
                        reticleType = "serial-tag"
                    )
                )
            ),
            humanChecks = buildList {
                if (house != null) {
                    add("${house.houseName} Signature Check: ${house.signatureMacroCues}")
                    add("${house.houseName} Serial/Era Check: ${house.eraAndSerialTip}")
                }
                add("Verify total item weight, natural material scent/hand-feel, and hardware solidity.")
                add("Test mechanical closures, zippers, or hinges for smooth, play-free precision.")
                add("Cross-check packaging, serial/date code era, and boutique documentation.")
            },
            isAdaptiveCustom = true
        )
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
            id = "belts",
            name = "Designer Leather Belts & Buckles",
            defaultItemName = "Reversible Calfskin Belt with Metallic Buckle",
            markers = listOf(
                MarkerSpec(
                    id = "belts-typography",
                    stepNumber = 1,
                    title = "Inner Leather Foil Stamp & Sizing Code",
                    focusFeature = "Hot-foil serif brand mark, sizing digits & date code kerning",
                    weight = 0.35,
                    lightingRequirement = "Soft raking side light (30°) avoiding metallic foil glare.",
                    angleAndDistance = "Dead-level macro at 5–8 cm aligned with inner strap text.",
                    targetConfirmations = listOf(
                        "Crisp debossed serif letters with zero foil halo bleed",
                        "Consistent baseline spacing across brand and size digits"
                    ),
                    targetAnomalies = listOf(
                        "Blurry surface foil ink without crisp debossed walls",
                        "Generic uneven fonts in sizing/serial digits"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center inner belt stamp horizontally inside typography band.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "belts-hardware",
                    stepNumber = 2,
                    title = "Buckle Casting, Engraving & Post Stud",
                    focusFeature = "Buckle chamfer sharpness, underside hallmark & plating smoothness",
                    weight = 0.35,
                    lightingRequirement = "Diffused bounce light eliminating specular blowout on buckle.",
                    angleAndDistance = "Straight-on macro framing buckle face or underside stamp.",
                    targetConfirmations = listOf(
                        "Clean CNC/forged bevel edges and crisp underside brand stamp",
                        "Smooth brushed or mirror plating without orange-peel pitting"
                    ),
                    targetAnomalies = listOf(
                        "Rough cast seam lines or porous pitting along inner buckle wall",
                        "Flaking thin chrome or uneven brass plating"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Frame primary buckle plate and hallmark inside inner corners.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "belts-stitch",
                    stepNumber = 3,
                    title = "Strap Perimeter Stitch & Edge Glazing",
                    focusFeature = "Border stitch pitch consistency, punched hole edges & glazing",
                    weight = 0.30,
                    lightingRequirement = "Even indirect daylight highlighting border seam and leather pores.",
                    angleAndDistance = "Perpendicular macro capturing strap edge seam and punched hole.",
                    targetConfirmations = listOf(
                        "Uniform stitch pitch following strap edge at fixed margin",
                        "Clean resin edge paint without cracking or spilling onto pores"
                    ),
                    targetAnomalies = listOf(
                        "Wavy stitch path drifting toward strap edge or thick rubbery paint",
                        "Torn frayed leather inside punched buckle holes"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align belt border stitch line horizontally through central guide.",
                        reticleType = "stitch-line"
                    )
                )
            ),
            humanChecks = listOf(
                "Weigh detachable metal buckle for solid brass/zamac heft vs lightweight hollow alloy.",
                "Verify full-grain calfskin aroma and suppleness vs bonded/plasticized split leather.",
                "Check buckle screw/post threading smoothness and keeper loop back-stitch symmetry."
            )
        ),
        CategoryProtocol(
            id = "textiles",
            name = "Luxury Silk Scarves & Cashmere Textiles",
            defaultItemName = "Hand-Rolled 90cm Twill Silk Scarf / Cashmere Wrap",
            markers = listOf(
                MarkerSpec(
                    id = "textiles-edge",
                    stepNumber = 1,
                    title = "Hand-Rolled Roulotté Edge Stitching",
                    focusFeature = "Plump rolled hem stitch pitch & silk thread tension",
                    weight = 0.40,
                    lightingRequirement = "Diffused neutral light highlighting rolled border fibers clearly.",
                    angleAndDistance = "Perpendicular 90° macro at 5–8 cm along 6 cm of hem edge.",
                    targetConfirmations = listOf(
                        "Plump rounded hand-rolled hem with slightly irregular hand stitches",
                        "Color-matched silk thread cleanly anchored into twill weave"
                    ),
                    targetAnomalies = listOf(
                        "Flat machine overlock/zigzag seam or stiff melted synthetic edge",
                        "Coarse polyester thread or uneven puckering"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align rolled scarf hem horizontally through the stitch guide.",
                        reticleType = "stitch-line"
                    )
                ),
                MarkerSpec(
                    id = "textiles-caretag",
                    stepNumber = 2,
                    title = "Care Tag Typography & Corner Tack Stitches",
                    focusFeature = "Fine woven/printed care tag serif lettering & attachment stitches",
                    weight = 0.35,
                    lightingRequirement = "Even diffused light revealing woven backing and fine print.",
                    angleAndDistance = "Dead-level macro framing entire care label and tack threads.",
                    targetConfirmations = listOf(
                        "Razor-sharp micro-serif '100% SOIE / MADE IN FRANCE/ITALY' text",
                        "Clean two-corner or edge-matched hand tack stitches"
                    ),
                    targetAnomalies = listOf(
                        "Blurred inkjet care tag, misspelled words, or thick bold font",
                        "Cheap synthetic satin label sewn into machine hem"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center full care tag horizontally inside typography box.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "textiles-print",
                    stepNumber = 3,
                    title = "Screen-Print Color Registration & Artist Mark",
                    focusFeature = "Multi-screen pigment crispness & copyright © signature sharpness",
                    weight = 0.25,
                    lightingRequirement = "Soft daylight without specular glare across twill ribs.",
                    angleAndDistance = "Macro at 6–8 cm centered on intricate line art or title signature.",
                    targetConfirmations = listOf(
                        "Crisp hairline ink registration with zero color bleed across borders",
                        "Diagonal silk twill rib weave clearly visible beneath pigment"
                    ),
                    targetAnomalies = listOf(
                        "Pixelated digital inkjet dots or muddy overlapping color borders",
                        "Flat plain-weave polyester sheen lacking twill diagonal ribs"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center copyright signature or fine motif lines inside frame.",
                        reticleType = "general-macro"
                    )
                )
            ),
            humanChecks = listOf(
                "Verify which side the roulotté hem rolls toward (Hermès hand-rolls toward the front printed side).",
                "Check silk twill crisp drape and weight (~65g for a classic 90cm carré) vs slippery lightweight polyester.",
                "Inspect color bleed-through on reverse side (authentic silk screen prints show rich pigment penetration)."
            )
        ),
        CategoryProtocol(
            id = "outerwear",
            name = "Designer Outerwear & Leather Jackets",
            defaultItemName = "Tailored Wool Trench / Down Puffer / Calfskin Jacket",
            markers = listOf(
                MarkerSpec(
                    id = "outerwear-labels",
                    stepNumber = 1,
                    title = "Neck Label, Size Tab & Care Book Typography",
                    focusFeature = "Woven crest thread density, comic/care wash ink & font kerning",
                    weight = 0.35,
                    lightingRequirement = "Even diffused daylight without flash reflection on satin labels.",
                    angleAndDistance = "Dead-level macro framing neck label or interior care/QR label.",
                    targetConfirmations = listOf(
                        "High-density crisp jacquard weave without stray bridge threads",
                        "Sharp ink lines and balanced spacing on interior composition tags"
                    ),
                    targetAnomalies = listOf(
                        "Connected threads linking adjacent embroidered letters",
                        "Blurry or washed-out ink on care labels or misspelled origin text"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Center primary neck or care label inside typography box.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "outerwear-hardware",
                    stepNumber = 2,
                    title = "Zipper Pull, Snap Buttons & Crest Hardware",
                    focusFeature = "Die-cast zipper slider engraving, snap domes & crest teeth",
                    weight = 0.35,
                    lightingRequirement = "Soft bounced light showing metallic engraving without hotspot.",
                    angleAndDistance = "Straight-on macro framing main zipper pull or branded snap button.",
                    targetConfirmations = listOf(
                        "Crisp recessed brand engraving on zipper pull & underside maker mark",
                        "Smooth gunmetal/horn button bevels with uniform finish"
                    ),
                    targetAnomalies = listOf(
                        "Shallow stamped zipper pull with pitted surface or rough edges",
                        "Generic unbranded zipper teeth on luxury outerwear"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Center zipper slider or snap button inside hardware plate box.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "outerwear-stitch",
                    stepNumber = 3,
                    title = "Sleeve Crest / Placket & Quilting Seam Stitch",
                    focusFeature = "Felt badge border stitch density or check-pattern seam alignment",
                    weight = 0.30,
                    lightingRequirement = "Indirect daylight revealing fabric weave and stitch tension.",
                    angleAndDistance = "Perpendicular macro at 6–9 cm framing sleeve patch or main seam.",
                    targetConfirmations = listOf(
                        "Tight uniform border stitching and clean pattern alignment across seams",
                        "Crisp woven/felt crest lettering without puckered backing"
                    ),
                    targetAnomalies = listOf(
                        "Loose looping embroidery threads or misaligned plaid/check seams",
                        "Uneven stitch spacing along cuff or pocket welt"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Align placket seam or patch edge horizontally in stitch guide.",
                        reticleType = "stitch-line"
                    )
                )
            ),
            humanChecks = listOf(
                "Test two-way zipper glide resistance and verify underside slider stamping (Lampo / Riri / Cobrax).",
                "Assess down loft/rebound or heavyweight virgin wool drape and breathable cupro/silk lining.",
                "Verify Certilogo/QR serial tags strictly on authentic manufacturer domain if present."
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
        ),
        CategoryProtocol(
            id = "writing",
            name = "Fine Writing Instruments & Desktop Objects",
            defaultItemName = "Precious-Resin Fountain Pen / Luxury Pocket Lighter",
            markers = listOf(
                MarkerSpec(
                    id = "writing-nib",
                    stepNumber = 1,
                    title = "Nib Engraving, Slit & Clip Band Tolerances",
                    focusFeature = "Two-tone nib scroll engraving crispness & centered breather hole",
                    weight = 0.45,
                    lightingRequirement = "Tented diffused light avoiding mirror glare off 14k/18k nib.",
                    angleAndDistance = "Macro at 4–6 cm perpendicular to top nib face or lighter lid hinge.",
                    targetConfirmations = listOf(
                        "Crisp clean-walled '4810 / 18K' nib scrollwork with centered tine slit",
                        "Sharp rhodium/gold masking boundary line without plating bleed"
                    ),
                    targetAnomalies = listOf(
                        "Shallow blurred nib stamping or off-center slit cut",
                        "Flaking two-tone plating or rough iridium tipping"
                    ),
                    boundingBox = BoundingBoxSpec(
                        alignmentRule = "Center nib face or metallic cap band inside hardware frame.",
                        reticleType = "hardware-plate"
                    )
                ),
                MarkerSpec(
                    id = "writing-serial",
                    stepNumber = 2,
                    title = "Cap Ring Engraving & Clip Serial Kerning",
                    focusFeature = "Laser-engraved clip ring serial & cap-band brand typography",
                    weight = 0.35,
                    lightingRequirement = "Soft side lighting along curved cap ring without hotspot streak.",
                    angleAndDistance = "Close-up macro centered on clip collar serial or cap-band lettering.",
                    targetConfirmations = listOf(
                        "Razor-sharp laser-etched serial numerals and balanced cap-band serifs",
                        "Flush snowcap emblem inlay with crisp star-point symmetry"
                    ),
                    targetAnomalies = listOf(
                        "Shallow stamped digits, uneven spacing, or wavy cap-band borders",
                        "Crooked or recessed snowcap/emblem border gap"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Align cap band or clip serial horizontally in typography box.",
                        reticleType = "typography-band"
                    )
                ),
                MarkerSpec(
                    id = "writing-barrel",
                    stepNumber = 3,
                    title = "Barrel Threads, Piston Seam & Resin Finish",
                    focusFeature = "Seamless barrel parting finish & clean brass piston threads",
                    weight = 0.20,
                    lightingRequirement = "Neutral bounce light inspecting barrel threads and ink window.",
                    angleAndDistance = "Macro at 5–7 cm capturing barrel-to-cone seam or section threads.",
                    targetConfirmations = listOf(
                        "Invisible piston knob seam when closed and clean thread crests",
                        "Uniform deep gloss polish without mold flash lines"
                    ),
                    targetAnomalies = listOf(
                        "Visible vertical mold seam line along grip section",
                        "Plastic click threads where precision brass mechanism belongs"
                    ),
                    boundingBox = BoundingBoxSpec(
                        targetFillMinPercent = 60,
                        alignmentRule = "Frame barrel seam or grip threads cleanly in macro box.",
                        reticleType = "general-macro"
                    )
                )
            ),
            humanChecks = listOf(
                "Test clip spring tension, cap thread engagement smoothness, and non-magnetic 14k/18k gold nib.",
                "Shine a strong phone LED inside black precious resin barrel for authentic deep crimson/cherry translucence (vs opaque black plastic).",
                "Check underside of pocket clip for 'PIX' or era-appropriate engraving where specified."
            )
        )
    )
}
