package aircraft.components.liftingSurface.creator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.auxiliary.airfoil.creator.AirfoilCreator.AirfoilBuilder;
import aircraft.components.liftingSurface.creator.AsymmetricFlapCreator.AsymmetricFlapBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator.LiftingSurfacePanelBuilder;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator.SymmetricFlapBuilder;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple5;
import standaloneutils.GeometryCalc;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class LiftingSurfaceCreator extends AbstractLiftingSurface {

	int _numberOfSpanwisePoints = 15;
	private LiftingSurfaceCreator _equivalentWing;
	private LiftingSurfaceCreator _wing2Panels;

	// equivalent wing fields
	private Boolean _equivalentWingFlag = Boolean.FALSE;
	private Amount<Area> _equivalentWingSurface;
	private Double _equivalentWingAspectRatio;
	private Double _nonDimensionalSpanStationKink;
	private Amount<Angle> _sweepQuarterChordEquivalentWing;
	private Amount<Angle> _angleOfIncidenceEquivalentWing;
	private Amount<Angle> _dihedralEquivalentWing;
	private Amount<Angle> _twistAtTipEquivalentWing;
	private Double _taperRatioEquivalentWing;
	private Amount<Length> _xOffsetEquivalentWingRootLE; // leading edge offset of the equivalent wing root chord ( >0 if inside original root chord)
	private Amount<Length> _xOffsetEquivalentWingRootTE; // trailing edge offset of the equivalent wing root chord ( >0 if inside original root chord)
	private AirfoilCreator airfoilRootEquivalentWing;
	private AirfoilCreator airfoilKinkEquivalentWing;
	private AirfoilCreator airfoilTipEquivalentWing;

	public LiftingSurfaceCreator(String id) {
		this.id = id;
		_panels = new ArrayList<LiftingSurfacePanelCreator>();
		_symmetricFlaps = new ArrayList<SymmetricFlapCreator>();
		_asymmetricFlaps = new ArrayList<AsymmetricFlapCreator>();
		_slats = new ArrayList<SlatCreator>();
		_spoilers = new ArrayList<SpoilerCreator>();
		resetData();
	}

	// use this to generate the equivalent wing or a simple wing
	public LiftingSurfaceCreator(
			String id,
			LiftingSurfacePanelCreator panel) {
		this.id = id;
		_panels = new ArrayList<LiftingSurfacePanelCreator>();
		resetData();

		_panels.add(panel);
		this.calculateGeometry(30);
	}

	//=====================================================================
	// Builder pattern via a nested public static class
	//=====================================================================
	public static class LiftingSurfaceBuilder {

		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...

		private List<LiftingSurfacePanelCreator> __panels = new ArrayList<LiftingSurfacePanelCreator>();
		private List<SymmetricFlapCreator> __symmetricFlaps = new ArrayList<SymmetricFlapCreator>();
		private List<AsymmetricFlapCreator> __asymmetricFlaps = new ArrayList<AsymmetricFlapCreator>();
		private List<SlatCreator> __slats = new ArrayList<SlatCreator>();
		private List<SpoilerCreator> __spoilers = new ArrayList<SpoilerCreator>();

		private MyArray __eta = new MyArray(Unit.ONE);

		private List<Amount<Length>> __yBreakPoints =  new ArrayList<Amount<Length>>();
		private List<Double> __etaBP = new ArrayList<>();
		private List<Amount<Length>> __xLEBreakPoints = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __zLEBreakPoints = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __chordsBreakPoints = new ArrayList<Amount<Length>>();
		private List<Amount<Angle>> __twistsBreakPoints = new ArrayList<Amount<Angle>>();

		private List<Amount<Length>> __yStationActual = new ArrayList<Amount<Length>>();
		private List<
		Tuple2<
		LiftingSurfacePanelCreator,
		Tuple5<
		List<Amount<Length>>, // Ys
		List<Amount<Length>>, // chords
		List<Amount<Length>>, // Xle
		List<Amount<Length>>, // Zle
		List<Amount<Angle>>   // twist
		> 
		>
		> __panelToSpanwiseDiscretizedVariables = new ArrayList<>();
		private List<
		Tuple5<
		Amount<Length>, // Ys
		Amount<Length>, // chords
		Amount<Length>, // Xle
		Amount<Length>, // Zle
		Amount<Angle>   // twist
		> 
		> __spanwiseDiscretizedVariables = new ArrayList<>();


		public LiftingSurfaceBuilder(String id) {
			this.__id = id;
			this.initializeDefaultVariables(AircraftEnum.ATR72);
		}

		public LiftingSurfaceBuilder(String id, AircraftEnum aircraftName) {
			this.__id = id;
			this.initializeDefaultVariables(aircraftName);
		}

		public LiftingSurfaceCreator build() {
			return new LiftingSurfaceCreator(this);
		}

		private void initializeDefaultVariables(AircraftEnum aircraftName) {
			// init variables - Reference aircraft:
			AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());

			// --- INPUT DATA ------------------------------------------

			switch(aircraftName) {
			case ATR72:

				AirfoilCreator airfoil1 = new AirfoilBuilder("ATR72 wing, root airfoil")
				.type(AirfoilTypeEnum.CONVENTIONAL)
				.thicknessToChordRatio(0.18)
				.camberRatio(0.09)
				.radiusLeadingEdgeNormalized(0.01)
				.alphaZeroLift(Amount.valueOf(-1.2, NonSI.DEGREE_ANGLE))
				.alphaEndLinearTrait(Amount.valueOf(9.5, NonSI.DEGREE_ANGLE))
				.alphaStall(Amount.valueOf(16.0, NonSI.DEGREE_ANGLE))
				.clAlphaLinearTrait(6.22)
				.cdMin(0.00675)
				.clAtCdMin(0.3)
				.clAtAlphaZero(0.13)
				.clEndLinearTrait(1.3)
				.clMax(1.7)
				.kFactorDragPolar(0.075)
				.mExponentDragPolar(2.0)
				.cmAlphaQuarterChord(0.043) // Clalpha * (c/4 - xac/c) 
				.xACNormalized(0.243)
				.cmAC(-0.083)
				.cmACAtStall(-0.09)
				.machCritical(0.656)
				.build();

				LiftingSurfacePanelCreator panel1 =
						new LiftingSurfacePanelBuilder(
								"ATR72 wing, inner panel",
								Amount.valueOf(2.5759,SI.METER), // chordRoot, 
								Amount.valueOf(2.5759,SI.METER), // chordTip,
								airfoil1, // airfoilRoot, 
								airfoil1, // airfoilTip,
								Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), // twistGeometricTip,
								Amount.valueOf(4.7,SI.METER), // semiSpan, 
								Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), // sweepLeadingEdge, 
								Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), // dihedral
								Amount.valueOf(2.0, NonSI.DEGREE_ANGLE)  // angle of incidence (iw)
								)
						.build();

				AirfoilCreator airfoil2 = new AirfoilBuilder("ATR72 wing, tip airfoil")
						.type(AirfoilTypeEnum.CONVENTIONAL)
						.thicknessToChordRatio(0.15)
						.camberRatio(0.09)
						.radiusLeadingEdgeNormalized(0.01)
						.alphaZeroLift(Amount.valueOf(-1.1, NonSI.DEGREE_ANGLE))
						.alphaEndLinearTrait(Amount.valueOf(10.0, NonSI.DEGREE_ANGLE))
						.alphaStall(Amount.valueOf(18.0, NonSI.DEGREE_ANGLE))
						.clAlphaLinearTrait(6.02)
						.cdMin(0.00625)
						.clAtCdMin(0.1)
						.clAtAlphaZero(0.115)
						.clEndLinearTrait(1.2)
						.clMax(1.72)
						.kFactorDragPolar(0.075)
						.mExponentDragPolar(2.0)
						.cmAlphaQuarterChord(0.04214) // Clalpha * (c/4 - xac/c) 
						.xACNormalized(0.243)
						.cmAC(-0.0833)
						.cmACAtStall(-0.07)
						.machCritical(0.695)
						.build();

				LiftingSurfacePanelCreator panel2 =
						new LiftingSurfacePanelBuilder(
								"ATR72 wing, outer panel",
								Amount.valueOf(2.5759,SI.METER), // chordRoot, 
								Amount.valueOf(1.5906,SI.METER), // chordTip,
								airfoil1, // airfoilRoot, 
								airfoil2, // airfoilTip,
								Amount.valueOf(-2.0, NonSI.DEGREE_ANGLE), // twistGeometricTip,
								Amount.valueOf(8.83,SI.METER), // semiSpan, 
								Amount.valueOf(4.3, NonSI.DEGREE_ANGLE), // sweepLeadingEdge, 
								Amount.valueOf(0.0, NonSI.DEGREE_ANGLE), // dihedral
								Amount.valueOf(0.0, NonSI.DEGREE_ANGLE)  // angle of incidence (iw)
								)
						.build();

				__panels.add(panel1);
				__panels.add(panel2);

				SymmetricFlapCreator flap1 =
						new SymmetricFlapBuilder(
								"ATR72 wing, inner flap",
								FlapTypeEnum.SINGLE_SLOTTED,
								0.08,
								0.35,
								0.35,
								0.32,
								Amount.valueOf(0, NonSI.DEGREE_ANGLE),
								Amount.valueOf(40, NonSI.DEGREE_ANGLE)			
								)
						.build();

				SymmetricFlapCreator flap2 =
						new SymmetricFlapBuilder(
								"ATR72 wing, outer flap",
								FlapTypeEnum.SINGLE_SLOTTED,
								0.35,
								0.8,
								0.35,
								0.32,
								Amount.valueOf(0, NonSI.DEGREE_ANGLE),
								Amount.valueOf(40, NonSI.DEGREE_ANGLE)									
								)
						.build();

				__symmetricFlaps.add(flap1);
				__symmetricFlaps.add(flap2);

				AsymmetricFlapCreator aileron1 =
						new AsymmetricFlapBuilder(
								"ATR72 wing, left aileron",
								FlapTypeEnum.PLAIN,
								0.8,
								0.98,
								0.33,
								0.33,
								Amount.valueOf(-25.0, NonSI.DEGREE_ANGLE),
								Amount.valueOf(25.0, NonSI.DEGREE_ANGLE)
								)
						.build();

				AsymmetricFlapCreator aileron2 =
						new AsymmetricFlapBuilder(
								"ATR72 wing, right aileron",
								FlapTypeEnum.PLAIN,
								0.8,
								0.98,
								0.33,
								0.33,
								Amount.valueOf(-25.0, NonSI.DEGREE_ANGLE),
								Amount.valueOf(25.0, NonSI.DEGREE_ANGLE)
								)
						.build();

				__asymmetricFlaps.add(aileron1);
				__asymmetricFlaps.add(aileron2);

				break;

			case B747_100B:
				// TODO
				break;

			case AGILE_DC1:
				// TODO
				break;
			}
			// --- END OF INPUT DATA ------------------------------------------

		}

	}

	private LiftingSurfaceCreator(LiftingSurfaceBuilder builder){ // defaults to ATR72 

		this.id = builder.__id;
		
		this._panels = builder.__panels;
		this._symmetricFlaps = builder.__symmetricFlaps;
		this._asymmetricFlaps = builder.__asymmetricFlaps;
		this._slats = builder.__slats;
		this._spoilers = builder.__spoilers;

		this._eta = builder.__eta;
		this._yBreakPoints = builder.__yBreakPoints;
		this._etaBP = builder.__etaBP;
		this._xLEBreakPoints = builder.__xLEBreakPoints;
		this._zLEBreakPoints = builder.__zLEBreakPoints;
		this._chordsBreakPoints = builder.__chordsBreakPoints;
		this._twistsBreakPoints = builder.__twistsBreakPoints;
		this._yStationActual = builder.__yStationActual;
		this._panelToSpanwiseDiscretizedVariables = builder.__panelToSpanwiseDiscretizedVariables;
		this._spanwiseDiscretizedVariables = builder.__spanwiseDiscretizedVariables;

		this.calculateGeometry(30);

	}

	private void resetData() {

		_eta = new MyArray(Unit.ONE);
		//_eta.setDouble(MyArrayUtils.linspace(0., 1., _numberOfPointsChordDistribution));
		//
		// assign eta's when the shape of the planform is loaded and no. _panels are known

		_yBreakPoints =  new ArrayList<Amount<Length>>();
		_etaBP = new ArrayList<>();
		_xLEBreakPoints = new ArrayList<Amount<Length>>();
		_zLEBreakPoints = new ArrayList<Amount<Length>>();
		_chordsBreakPoints = new ArrayList<Amount<Length>>();
		_twistsBreakPoints = new ArrayList<Amount<Angle>>();

		_yStationActual = new ArrayList<Amount<Length>>();
		_panelToSpanwiseDiscretizedVariables = new ArrayList<>();
		_spanwiseDiscretizedVariables = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public static LiftingSurfaceCreator importFromXML(String pathToXML, String airfoilsDir) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading lifting surface data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing/@id");

		String equivalent = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing/@equivalent");		

		LiftingSurfaceCreator wing = new LiftingSurfaceCreator(id);

		wing.setEquivalentWingFlag(Boolean.valueOf(equivalent));

		//---------------------------------------------------------------------------------
		// EQUIVALENT WING
		if(equivalent.equalsIgnoreCase("TRUE")) {

			System.out.println("Reading equivalent wing data from XML doc ...");

			Amount<Area> surface = (Amount<Area>) reader.getXMLAmountWithUnitByPath("//equivalent_wing/surface");
			Double aspectRatio = Double.valueOf(reader.getXMLPropertyByPath("//equivalent_wing/aspect_ratio"));
			Double spanStationKink = Double.valueOf(reader.getXMLPropertyByPath("//equivalent_wing/non_dimensional_span_station_kink"));
			Amount<Angle> sweepQuarterChord = reader.getXMLAmountAngleByPath("//equivalent_wing/sweep_quarter_chord");
			Amount<Angle> angleOfIncidence = reader.getXMLAmountAngleByPath("//equivalent_wing/angle_of_incidence");
			Amount<Angle> dihedral = reader.getXMLAmountAngleByPath("//equivalent_wing/dihedral");
			Amount<Angle> twistAtTip = reader.getXMLAmountAngleByPath("//equivalent_wing/twist_at_tip");
			Double taperRatio = Double.valueOf(reader.getXMLPropertyByPath("//equivalent_wing/taper_ratio"));
			Amount<Length> xOffsetRootChordLE = reader.getXMLAmountLengthByPath("//equivalent_wing/x_offset_root_chord_leading_edge");
			Amount<Length> xOffsetRootChordTE = reader.getXMLAmountLengthByPath("//equivalent_wing/x_offset_root_chord_trailing_edge");

			String airfoilFileNameRoot =
					MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//equivalent_wing/airfoils/airfoil_root/@file");
			String airFoilPathRoot = airfoilsDir + File.separator + airfoilFileNameRoot;
			AirfoilCreator airfoilRoot = AirfoilCreator.importFromXML(airFoilPathRoot);

			String airfoilFileNameKink =
					MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//equivalent_wing/airfoils/airfoil_kink/@file");
			String airFoilPathKink = airfoilsDir + File.separator + airfoilFileNameKink;
			AirfoilCreator airfoilKink = AirfoilCreator.importFromXML(airFoilPathKink);

			String airfoilFileNameTip =
					MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//equivalent_wing/airfoils/airfoil_tip/@file");
			String airFoilPathTip = airfoilsDir + File.separator + airfoilFileNameTip;
			AirfoilCreator airfoilTip = AirfoilCreator.importFromXML(airFoilPathTip);

			// setting these variables to the related fields of the equivalent wing
			wing.setEquivalentWingSurface(surface);
			wing.setEquivalentWingAspectRatio(aspectRatio);
			wing.setNonDimensionalSpanStationKink(spanStationKink);
			wing.setSweepQuarterChordEquivalentWing(sweepQuarterChord);
			wing.setAngleOfIncidenceEquivalentWing(angleOfIncidence);
			wing.setDihedralEquivalentWing(dihedral);
			wing.setTwistAtTipEquivalentWing(twistAtTip);
			wing.setTaperRatioEquivalentWing(taperRatio);
			wing.setXOffsetEquivalentWingRootLE(xOffsetRootChordLE);
			wing.setXOffsetEquivalentWingRootTE(xOffsetRootChordTE);
			wing.setAirfoilRootEquivalentWing(airfoilRoot);
			wing.setAirfoilKinkEquivalentWing(airfoilKink);
			wing.setAirfoilTipEquivalentWing(airfoilTip);
		}

		//---------------------------------------------------------------------------------
		// PANELS

		if(equivalent.equalsIgnoreCase("FALSE")) {

			NodeList nodelistPanel = MyXMLReaderUtils
					.getXMLNodeListByPath(reader.getXmlDoc(), "//panels/panel");

			System.out.println("Panels found: " + nodelistPanel.getLength());

			for (int i = 0; i < nodelistPanel.getLength(); i++) {
				Node nodePanel  = nodelistPanel.item(i); // .getNodeValue();
				Element elementPanel = (Element) nodePanel;
				System.out.println("[" + i + "]\nPanel id: " + elementPanel.getAttribute("id"));
				if (elementPanel.getAttribute("linked_to").isEmpty()) {
					wing.addPanel(LiftingSurfacePanelCreator.importFromPanelNode(nodePanel, airfoilsDir));
				} else {
					LiftingSurfacePanelCreator panel0 = wing.getPanels().stream()
							.filter(p -> p.getId().equals(elementPanel.getAttribute("linked_to")))
							.findFirst()
							.get()
							;
					if (panel0 != null) {
						System.out.println("Panel linked_to: **" + elementPanel.getAttribute("linked_to") + "**");
						wing.addPanel(LiftingSurfacePanelCreator.importFromPanelNodeLinked(nodePanel, panel0, airfoilsDir));
					} else {
						System.out.println("WARNING: panel not parsed. Unable to find the ID of linked_to attribute!");
					}
				}
			}
		}
		// Update _panels' internal geometry variables
		// wing.calculateGeometry(); // shouldn't care about discretization
		// for now the user calculates the geometry from the outside of the class
		// via the wing object:
		//
		//     theWing.calculateGeometry(30);

		//---------------------------------------------------------------------------------
		// SYMMETRIC FLAPS
		NodeList nodelistFlaps = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//symmetric_flaps/symmetric_flap");

		System.out.println("Symmetric flaps found: " + nodelistFlaps.getLength());

		for (int i = 0; i < nodelistFlaps.getLength(); i++) {
			Node nodeFlap  = nodelistFlaps.item(i); // .getNodeValue();
			Element elementFlap = (Element) nodeFlap;
			System.out.println("[" + i + "]\nFlap id: " + elementFlap.getAttribute("id"));

			wing.addSymmetricFlaps(SymmetricFlapCreator.importFromSymmetricFlapNode(nodeFlap));
		}

		//---------------------------------------------------------------------------------
		// SYMMETRIC SLATS
		NodeList nodelistSlats = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//slats/slat");

		System.out.println("Slats found: " + nodelistSlats.getLength());

		for (int i = 0; i < nodelistSlats.getLength(); i++) {
			Node nodeSlat  = nodelistSlats.item(i); // .getNodeValue();
			Element elementSlat = (Element) nodeSlat;
			System.out.println("[" + i + "]\nSlat id: " + elementSlat.getAttribute("id"));

			wing.addSlats(SlatCreator.importFromSymmetricSlatNode(nodeSlat));
		}

		//---------------------------------------------------------------------------------
		// ASYMMETRIC FLAPS
		NodeList nodelistAsymmetricFlaps = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//asymmetric_flaps/asymmetric_flap");

		System.out.println("Asymmetric flaps found: " + nodelistAsymmetricFlaps.getLength());

		for (int i = 0; i < nodelistAsymmetricFlaps.getLength(); i++) {
			Node nodeAsymmetricFlap  = nodelistAsymmetricFlaps.item(i); // .getNodeValue();
			Element elementAsymmetricFlap = (Element) nodeAsymmetricFlap;
			System.out.println("[" + i + "]\nSlat id: " + elementAsymmetricFlap.getAttribute("id"));

			wing.addAsymmetricFlaps(AsymmetricFlapCreator.importFromAsymmetricFlapNode(nodeAsymmetricFlap));
		}

		//---------------------------------------------------------------------------------
		// SPOILERS
		NodeList nodelistSpoilers = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//spoilers/spoiler");

		System.out.println("Spoilers found: " + nodelistSpoilers.getLength());

		for (int i = 0; i < nodelistSpoilers.getLength(); i++) {
			Node nodeSpoiler  = nodelistSpoilers.item(i); // .getNodeValue();
			Element elementSpoiler = (Element) nodeSpoiler;
			System.out.println("[" + i + "]\nSlat id: " + elementSpoiler.getAttribute("id"));

			wing.addSpoilers(SpoilerCreator.importFromSpoilerNode(nodeSpoiler));
		}

		return wing;
	}

	@Override
	public void addPanel(LiftingSurfacePanelCreator panel) {
		_panels.add(panel);
	}

	@Override
	public void addSymmetricFlaps(SymmetricFlapCreator symmetricFlaps) {
		_symmetricFlaps.add(symmetricFlaps);
	}

	@Override
	public void addAsymmetricFlaps(AsymmetricFlapCreator asymmetricFlaps) {
		_asymmetricFlaps.add(asymmetricFlaps);
	}

	@Override
	public void addSlats(SlatCreator slats) {
		_slats.add(slats);
	}

	@Override
	public void addSpoilers(SpoilerCreator spoilers) {
		_spoilers.add(spoilers);
	}

	@Override
	public void calculateGeometry() {
		calculateGeometry(_numberOfSpanwisePoints);
	}

	@Override
	public void calculateGeometry(int numberSpanwiseStations) {

		System.out.println("[LiftingSurfaceCreator] Calculating derived geometry parameters of wing ...");

		resetData();
		
		
		//======================================================
		// Build 2 panels wing from the equivalent wing
		if(this._equivalentWingFlag)
			buildPlanform2Panels();

		// Update inner geometric variables of each panel
		this.getPanels().stream()
		.forEach(LiftingSurfacePanelCreator::calculateGeometry);

		// Total planform area
		Double surfPlanform = this.getPanels().stream()
				.mapToDouble(p -> p.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue())
				.sum();
		this.surfacePlanform = Amount.valueOf(surfPlanform,SI.SQUARE_METRE);

		// Total wetted area
		Double surfWetted = this.getPanels().stream()
				.mapToDouble(p -> p.getSurfaceWetted().to(SI.SQUARE_METRE).getEstimatedValue())
				.sum();
		this.surfaceWetted = Amount.valueOf(surfWetted,SI.SQUARE_METRE);
		
		//======================================================
		// Update semiSpan and span
		calculateSpans();

		//======================================================
		// Calculate break-points
		calculateVariablesAtBreakpoints();

		//======================================================
		// Discretize the wing spanwise
		discretizeGeometry(numberSpanwiseStations);

		//======================================================
		// Aspect-ratio
		this.aspectRatio = (this.span.pow(2)).divide(this.surfacePlanform).getEstimatedValue();

		//======================================================
		// Mean aerodynamic chord
		calculateMAC();

		//======================================================
		// Mean aerodynamic chord leading-edge coordinates
		calculateXYZleMAC();

		//======================================================
		// Equivalent wing 
		if(!this._equivalentWingFlag)
			calculateEquivalentWing();
		
	}

	private void calculateMAC() {

		// Mean Aerodynamic Chord

		//======================================================
		// Weighted sum on MACs of single _panels
//		Double mac0 = this.getPanels().stream()
//				.mapToDouble(p ->
//					p.getMeanAerodynamicChord().to(SI.METRE).getEstimatedValue()
//					*p.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue())
//				.sum();
//		mac0 = mac0 / this.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue();
//		this.meanAerodynamicChord = Amount.valueOf(mac0,SI.METRE);

		//======================================================
		// mac = (2/S) * int_0^(b/2) c^2 dy
		Double mac = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
					this.getDiscretizedYs()), // y
				MyArrayUtils.convertListOfAmountTodoubleArray(
					this.getDiscretizedChords().stream()
						.map(c -> c.pow(2))
						.collect(Collectors.toList())
				) // c^2
			);
		mac = 2.0 * mac / this.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue(); // *= 2/S
		this.meanAerodynamicChord = Amount.valueOf(mac,1e-9,SI.METRE);

	}

	private void calculateXYZleMAC() {

		//======================================================
		// x_le_mac = (2/S) * int_0^(b/2) xle(y) c(y) dy

		Tuple2<
			List<Amount<Length>>, // Xle
			List<Amount<Length>>  // c
		> xleTimeCTuple = Tuple.of(this.getDiscretizedXle(), this.getDiscretizedChords());
		
		List<Double> xleTimeC = IntStream.range(0, this.getDiscretizedYs().size())
				.mapToObj(i -> 
					xleTimeCTuple._1.get(i).doubleValue(SI.METRE)
					*xleTimeCTuple._2.get(i).doubleValue(SI.METRE)) // xle * c
				.collect(Collectors.toList());
		
		Double xle = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						this.getDiscretizedYs()), // y
				MyArrayUtils.convertToDoublePrimitive(xleTimeC) // xle * c
			);
		xle = 2.0 * xle / this.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue();
		this.meanAerodynamicChordLeadingEdgeX = Amount.valueOf(xle,1e-9,SI.METRE);

		//======================================================
		// y_le_mac = (2/S) * int_0^(b/2) yle(y) c(y) dy

		Tuple2<
			List<Amount<Length>>, // Xle
			List<Amount<Length>>  // c
		> yTimeCTuple = Tuple.of(this.getDiscretizedYs(), this.getDiscretizedChords());

		List<Double> yTimeC = IntStream.range(0, this.getDiscretizedYs().size())
				.mapToObj(i -> 
					yTimeCTuple._1.get(i).doubleValue(SI.METRE)
					*yTimeCTuple._2.get(i).doubleValue(SI.METRE)) // y * c
				.collect(Collectors.toList());
		
		Double yle = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						this.getDiscretizedYs()), // y
				MyArrayUtils.convertToDoublePrimitive(yTimeC) // y * c
			);
		yle = 2.0 * yle / this.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue(); // *= 2/S
		this.meanAerodynamicChordLeadingEdgeY = Amount.valueOf(yle,1e-9,SI.METRE);

		//======================================================
		// z_le_mac = (2/S) * int_0^(b/2) zle(y) c(y) dy

		Tuple2<
			List<Amount<Length>>, // Xle
			List<Amount<Length>>  // c
		> zTimeCTuple = Tuple.of(this.getDiscretizedZle(), this.getDiscretizedChords());

	List<Double> zTimeC = IntStream.range(0, this.getDiscretizedYs().size())
			.mapToObj(i -> 
				zTimeCTuple._1.get(i).doubleValue(SI.METRE)
				*zTimeCTuple._2.get(i).doubleValue(SI.METRE)) // z * c
			.collect(Collectors.toList());
		
		Double zle = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						this.getDiscretizedYs()), // z
				MyArrayUtils.convertToDoublePrimitive(zTimeC) // z * c
			);
		zle = 2.0 * zle / this.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue(); // *= 2/S
		this.meanAerodynamicChordLeadingEdgeZ = Amount.valueOf(zle,1e-9,SI.METRE);
	}

	@Override
	public void discretizeGeometry(int numberSpanwiseStations) {
		//======================================================
		// Eta's discretizing the whole planform,
		// in the middle of each panel,
		// and including break-point eta's

		List<Double> eta0 =
			Arrays.asList(
				ArrayUtils.toObject(
						MyArrayUtils.linspace(0., 1., numberSpanwiseStations)
				)
			);
//		System.out.println(eta0);

		List<Double> eta1 = ListUtils.union(eta0, _etaBP);
		Collections.sort(eta1);
//		System.out.println(eta1);

		List<Double> eta2 = eta1.stream()
			.distinct().collect(Collectors.toList());
//		System.out.println(eta2);

		_numberOfSpanwisePoints = eta2.size();

		// Now that break-points are known generate eta's, including
		// break-point eta's
		//_eta.setDouble(MyArrayUtils.linspace(0., 1., _numberOfPointsChordDistribution));
		_eta.setList(eta2);
//		System.out.println(_eta);

		//======================================================
		// Y's discretizing the whole planform,
		// in the middle of each panel,
		// and including break-point eta's

//		_yStationActual.setRealVector(
//				_eta.getRealVector().mapMultiply(this.semiSpan.doubleValue(SI.METER)));

		_yStationActual = _eta.getList().stream()
				.map(d -> this.semiSpan.times(d))
				.collect(Collectors.toList());
		
		//======================================================
		// Assign lists of Y's to each panel
		mapPanelsToYDiscretized();

		//======================================================
		// Map Y's to chord
		calculateChordsAtYDiscretized();

		//======================================================
		// Map Y's to (Xle, Zle, twist)
		calculateXZleTwistAtYDiscretized();

//		reportPanelsToSpanwiseDiscretizedVariables();

		//======================================================
		// fill the list of all discretized variables
		calculateDiscretizedGeometry();

	}

	@Override
	public List<Amount<Length>> getXYZ0() {
		return Arrays.asList(this.x0, this.y0, this.z0);
	}

	@Override
	public Amount<Length> getX0() {
		return this.x0;
	}

	@Override
	public Amount<Length> getY0() {
		return this.y0;
	}

	@Override
	public Amount<Length> getZ0() {
		return this.z0;
	}

	@Override
	public void setXYZ0(Amount<Length> x0, Amount<Length> y0, Amount<Length> z0) {
		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;
	}

	@Override
	public List<Amount<Length>> getXYZPole() {
		return Arrays.asList(this.xPole, this.yPole, this.zPole);
	}

	@Override
	public Amount<Length> getXPole() {
		return this.xPole;
	}

	@Override
	public Amount<Length> getYPole() {
		return this.yPole;
	}

	@Override
	public Amount<Length> getZPole() {
		return this.zPole;
	}

	@Override
	public void setXYZPole(Amount<Length> xp, Amount<Length> yp, Amount<Length> zp) {
		this.xPole = xp;
		this.yPole = yp;
		this.zPole = zp;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChord() {
		return this.meanAerodynamicChord;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChord(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.meanAerodynamicChord;
	}

	@Override
	public List<Amount<Length>> getMeanAerodynamicChordLeadingEdge() {
		return Arrays.asList(
				this.meanAerodynamicChordLeadingEdgeX,
				this.meanAerodynamicChordLeadingEdgeY,
				this.meanAerodynamicChordLeadingEdgeZ
				);
	}

	@Override
	public List<Amount<Length>> getMeanAerodynamicChordLeadingEdge(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return Arrays.asList(
				this.meanAerodynamicChordLeadingEdgeX,
				this.meanAerodynamicChordLeadingEdgeY,
				this.meanAerodynamicChordLeadingEdgeZ
				);
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeX() {
		return this.meanAerodynamicChordLeadingEdgeX;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeX(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.meanAerodynamicChordLeadingEdgeX;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeY() {
		return this.meanAerodynamicChordLeadingEdgeY;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeY(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.meanAerodynamicChordLeadingEdgeY;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeZ() {
		return this.meanAerodynamicChordLeadingEdgeZ;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeZ(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.meanAerodynamicChordLeadingEdgeZ;
	}

	@Override
	public Amount<Length> getSemiSpan() {
		return this.semiSpan;
	}
	@Override
	public Amount<Length> getSemiSpan(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.semiSpan;
	}

	@Override
	public Amount<Length> getSpan() {
		return this.span;
	}
	@Override
	public Amount<Length> getSpan(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.span;
	}

	@Override
	public Amount<Area> getSurfacePlanform() {
		return this.surfacePlanform;
	}
	@Override
	public Amount<Area> getSurfacePlanform(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.surfacePlanform;
	}

	@Override
	public Amount<Area> getSurfaceWetted() {
		return surfaceWetted;
	}
	@Override
	public Amount<Area> getSurfaceWetted(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return surfaceWetted;
	}

	@Override
	public Double getAspectRatio() {
		return this.aspectRatio;
	}

	@Override
	public Double getAspectRatio(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.aspectRatio;
	}

	@Override
	public Double getTaperRatio() {
		return this.taperRatio;
	}

	@Override
	public Double getTaperRatio(boolean recalculate) {
		if (recalculate) this.calculateGeometry();
		return this.taperRatio;
	}

	@Override
	public Amount<Angle> getAngleOfIncidence() {
		return _panels.get(0).getAngleOfIncidence();
	}
	/*****************************************************************************
	 * This method builds a 2 panel wing from the related equivalent wing data
	 */
	@SuppressWarnings("unchecked")
	private void buildPlanform2Panels() {
		
		// calculating each panel parameters from the equivalent wing ...
		Amount<Length> span = Amount.valueOf(
				Math.sqrt(this._equivalentWingSurface.doubleValue(SI.SQUARE_METRE)
						*this._equivalentWingAspectRatio),
				SI.METER
				);
		Amount<Length> chordRootEquivalentWing = Amount.valueOf(
				2*this._equivalentWingSurface.doubleValue(SI.SQUARE_METRE)/
				(span.doubleValue( SI.METER)*(1 + this._taperRatioEquivalentWing)),
				SI.METER
				);
		
		Amount<Length> semiSpanInnerPanel = Amount.valueOf(0.0, SI.METER);
		Amount<Length> semiSpanOuterPanel = Amount.valueOf(0.0, SI.METER);
		
		if (this._nonDimensionalSpanStationKink != 1.0){
			semiSpanInnerPanel = Amount.valueOf(
					this._nonDimensionalSpanStationKink *(span.doubleValue(SI.METER)/2),
					SI.METER
					);
			semiSpanOuterPanel = Amount.valueOf(
					span.doubleValue(SI.METER)/2 
					- semiSpanInnerPanel.doubleValue(SI.METER),
					SI.METER
					);
		} else {
			// if _spanStationKink=1.0 (simply tapered wing) outer panel doesn't exist: there is only the inner panel
			semiSpanInnerPanel = Amount.valueOf((span.doubleValue(SI.METER))/2,SI.METER);
			semiSpanOuterPanel = Amount.valueOf(0.,SI.METER);
		}
		
		Amount<Length> chordTip = Amount.valueOf(
				chordRootEquivalentWing.doubleValue(SI.METER)
				* this._taperRatioEquivalentWing,
				SI.METER
				);
		
		// _chordLinPanel = Root chord as if kink chord is extended linearly till wing root.
		Amount<Length> chordLinPanel = Amount.valueOf(
				(this._equivalentWingSurface.doubleValue(SI.SQUARE_METRE) - chordTip.doubleValue(SI.METER)
						*(span.doubleValue(SI.METER)/2))
				/((this._xOffsetEquivalentWingRootLE.getEstimatedValue() + this._xOffsetEquivalentWingRootTE.getEstimatedValue())
						*semiSpanInnerPanel.doubleValue(SI.METER) + span.doubleValue(SI.METER)/2),
				SI.METER
				);

		Amount<Length> chordRoot = Amount.valueOf(0.0, SI.METER);
		Amount<Length> chordKink = Amount.valueOf(0.0, SI.METER);
		// Cranked wing <==> _extensionLE/TERootChordLinPanel !=0 and _spanStationKink!=1.0 (if branch)
		// Constant chord (inner panel) + simply tapered (outer panel) wing <==> _extensionLE/TERootChordLinPanel = 0 and _spanStationKink!=1.0 (else branch)
		// Simply tapered wing <==> _extensionLE/TERootChordLinPanel = 0 and _spanStationKink=1.0 (if branch)
		if (((this._xOffsetEquivalentWingRootLE.getEstimatedValue() != 0.0
				| this._xOffsetEquivalentWingRootTE.getEstimatedValue() != 0.0)
				| this._nonDimensionalSpanStationKink == 1.0)){
			
			chordRoot = Amount.valueOf(
					chordLinPanel.doubleValue(SI.METER)
					*(1 + this._xOffsetEquivalentWingRootLE.getEstimatedValue()
					+ this._xOffsetEquivalentWingRootTE.getEstimatedValue()),
					SI.METER
					);
			
			chordKink = Amount.valueOf(
					chordLinPanel.doubleValue(SI.METER)
					*(1 - this._nonDimensionalSpanStationKink)
					+ chordTip.doubleValue(SI.METER) 
					* this._nonDimensionalSpanStationKink,
					SI.METER
					);

		} else {
			chordRoot = Amount.valueOf(
					2*(this._equivalentWingSurface.getEstimatedValue()/2 - 
					chordTip.getEstimatedValue() * semiSpanOuterPanel.getEstimatedValue()/2)/
					(span.getEstimatedValue()/2 + semiSpanInnerPanel.getEstimatedValue()),
					SI.METER
					);
		    chordKink = (Amount<Length>) JPADStaticWriteUtils.cloneAmount(chordRoot);
		}

		Amount<Angle> sweepLeadingEdgeEquivalent = Amount.valueOf(
				Math.atan(
						Math.tan(
								this._sweepQuarterChordEquivalentWing.doubleValue(SI.RADIAN)) 
						+ (1 - this._taperRatioEquivalentWing)
						/(this._equivalentWingAspectRatio 
								* (1 + this._taperRatioEquivalentWing)
								)
						),
				SI.RADIAN
				);

		// X coordinates of root, tip and kink chords
		Amount<Length> xLERoot = Amount.valueOf(0.0 ,SI.METER);
		
		Amount<Length> xLETip = Amount.valueOf(
				Math.tan(sweepLeadingEdgeEquivalent.doubleValue(SI.RADIAN)) * 
				span.doubleValue(SI.METER)/2 + (chordLinPanel.doubleValue(SI.METER) 
						* this._xOffsetEquivalentWingRootLE.getEstimatedValue()
						* (1 - this._nonDimensionalSpanStationKink)
						),
				SI.METER
				);
		
		Amount<Length> xLEKink = Amount.valueOf(0.0 ,SI.METER);
		if ((this._xOffsetEquivalentWingRootLE.getEstimatedValue() != 0.0 
				| this._xOffsetEquivalentWingRootTE.getEstimatedValue() != 0.0) 
				| this._nonDimensionalSpanStationKink == 1.0) {
			xLEKink = Amount.valueOf(chordLinPanel.doubleValue(SI.METER) * 
					this._xOffsetEquivalentWingRootLE.getEstimatedValue() 
					+ this._nonDimensionalSpanStationKink
					* (xLETip.doubleValue(SI.METER) - chordLinPanel.doubleValue(SI.METER) 
							* this._xOffsetEquivalentWingRootLE.getEstimatedValue()
							),
					SI.METER
					);
		} else {
			xLEKink = (Amount<Length>) JPADStaticWriteUtils.cloneAmount(xLERoot);
		}
		
		// Sweep of LE of inner panel
		Amount<Angle> sweepLeadingEdgeInnerPanel = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		if ((this._xOffsetEquivalentWingRootLE.getEstimatedValue() != 0.0
				| this._xOffsetEquivalentWingRootTE.getEstimatedValue() != 0.0) 
				| this._nonDimensionalSpanStationKink == 1.0) {
			
			sweepLeadingEdgeInnerPanel = Amount.valueOf(
					Math.atan(xLEKink.doubleValue(SI.METER)/
					semiSpanInnerPanel.doubleValue(SI.METER)),
					SI.RADIAN
					);
		} else {
			sweepLeadingEdgeInnerPanel = Amount.valueOf(0.0, SI.RADIAN);
		}
		
		// Outer panel LE sweep
		Amount<Angle> sweepLeadingEdgeOuterPanel = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		if(this._nonDimensionalSpanStationKink != 1.0){
			sweepLeadingEdgeOuterPanel = Amount.valueOf(Math.atan((xLETip.doubleValue(SI.METER) 
					- xLEKink.doubleValue(SI.METER))
					/(semiSpanOuterPanel.doubleValue(SI.METER))),
					SI.RADIAN
					);
		} else {
			sweepLeadingEdgeOuterPanel = (Amount<Angle>) JPADStaticWriteUtils.cloneAmount(sweepLeadingEdgeInnerPanel);
		}
		
		double[] xArray = new double[] {0.0, 1.0};
		double[] yArray = new double[] {0.0, this._twistAtTipEquivalentWing.doubleValue(NonSI.DEGREE_ANGLE)};
		Amount<Angle> twistAtKink = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						xArray,
						yArray,
						this._nonDimensionalSpanStationKink),
				NonSI.DEGREE_ANGLE
				);
		
		/*
		 *  since the mean dihedral is not enough to determine the two dihedral angles
		 *  of the two panels. The dihedral is considered constant. Regarding the twist,
		 *  the kink twist is taken as the interpolated value of the linear twist distribution
		 *  (from root to tip) at the kink station 
		 */
		// creating the 2 panels ...
		LiftingSurfacePanelCreator panel1 = new 
				LiftingSurfacePanelBuilder(
						"Inner panel from equivalent wing",
						chordRoot,
						chordKink,
						this.airfoilRootEquivalentWing,
						this.airfoilKinkEquivalentWing,
						twistAtKink,
						semiSpanInnerPanel,
						sweepLeadingEdgeInnerPanel.to(NonSI.DEGREE_ANGLE),
						this._dihedralEquivalentWing,
						this._angleOfIncidenceEquivalentWing)
				.build();

		LiftingSurfacePanelCreator panel2 = new 
				LiftingSurfacePanelBuilder(
						"Outer panel from equivalent wing",
						chordKink,
						chordTip,
						this.airfoilKinkEquivalentWing,
						this.airfoilTipEquivalentWing,
						this._twistAtTipEquivalentWing,
						semiSpanOuterPanel,
						sweepLeadingEdgeOuterPanel.to(NonSI.DEGREE_ANGLE),
						this._dihedralEquivalentWing,
						Amount.valueOf(0.0, NonSI.DEGREE_ANGLE))
				.build();

		// creating the wing ...
		_panels.add(panel1);;		_panels.add(panel2);	
		
	}
	
	/*************************************************************************************
	 * This method builds the equivalent wing from the actual wing panels
	 */
	private LiftingSurfaceCreator calculateEquivalentWing() {
		
		// Equivalent wing calculation --> sheet reference (Vittorio Trifari)
		
		//======================================================
		// integral_1 = ((b/2)*xle(b/2)) - int_0^(b/2) xle(y) dy
		// _xOffsetEquivalentWingRootLE = xle(b/2) - (2*A_1/(b/2))

		Double integral1 = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
					this.getDiscretizedYs()), // y
				MyArrayUtils.convertListOfAmountTodoubleArray(
					this.getDiscretizedXle()) // xle(y)
			);
		int nSec = this.getDiscretizedXle().size();
		Double xleAtTip = this.getDiscretizedXle().get(nSec - 1).doubleValue(SI.METER);
		Double area1 = (semiSpan.doubleValue(SI.METER) * xleAtTip) - integral1;
		
		_xOffsetEquivalentWingRootLE = Amount.valueOf(
				xleAtTip - (area1 / (0.5*semiSpan.doubleValue(SI.METER))),
				SI.METER
				);
		
		//======================================================
		// integral_2 = ((b/2)*(xte(0)-xte(b/2)) - [ (xte(0)*b/2) - (int_0^(b/2) [xle(y) + c(y)] dy)]
		// _xOffsetEquivalentWingRootTE = chord_root - xte(b/2) - (A_2/((b/2*)(1/2)))

		Tuple2<
			List<Amount<Length>>, // Xle
			List<Amount<Length>>  // c
			> xlePlusCTuple = Tuple.of(this.getDiscretizedXle(), this.getDiscretizedChords());

		List<Double> xlePlusC = IntStream.range(0, this.getDiscretizedYs().size())
				.mapToObj(i -> 
					xlePlusCTuple._1.get(i).doubleValue(SI.METRE)
					+ xlePlusCTuple._2.get(i).doubleValue(SI.METRE)) // xle + c
				.collect(Collectors.toList());

		Double integral2 = MyMathUtils.integrate1DSimpsonSpline(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						this.getDiscretizedYs()), // y
				MyArrayUtils.convertToDoublePrimitive(xlePlusC) // xle + c
				);
		Double xteAtRoot = this.getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		int nPan = this.getPanels().size();
		Double xteAtTip = xleAtTip + this.getPanels().get(nPan - 1).getChordTip().doubleValue(SI.METER);
		
		Double area2a = (xteAtRoot*semiSpan.doubleValue(SI.METER)) - integral2;
		Double area2 = semiSpan.doubleValue(SI.METER) * ( xteAtRoot - xteAtTip ) - area2a;
		
		_xOffsetEquivalentWingRootTE = Amount.valueOf(
				this.getPanels().get(0).getChordRoot().doubleValue(SI.METER)
				- xteAtTip - ((area2 / (0.5*semiSpan.doubleValue(SI.METER)))),
				SI.METER
				);
		
		//======================================================
		// Equivalent wing parameters: 
		Amount<Length> chordRootEquivalentWing = getPanels().get(0).getChordRoot()
				.minus(_xOffsetEquivalentWingRootLE.plus(_xOffsetEquivalentWingRootTE));
		
		Amount<Length> chordTipEquivalentWing = getPanels().get(getPanels().size()-1).getChordTip();
		
		AirfoilCreator airfoilRootEquivalent = getPanels().get(0).getAirfoilRoot();
		AirfoilCreator airfoilTipEquivalent = getPanels().get(getPanels().size()-1).getAirfoilTip();
		
		Amount<Angle> twistGeometricTipEquivalentWing = getPanels().get(getPanels().size()-1).getTwistGeometricAtTip();
		
		Amount<Angle> sweepLEEquivalentWing = Amount.valueOf(
				Math.atan(
						(getDiscretizedXle().get(getDiscretizedXle().size()-1).getEstimatedValue())
						/getSemiSpan().getEstimatedValue()
						),
				SI.RADIAN)
				.to(NonSI.DEGREE_ANGLE);
		
		Amount<Angle> angleOfIncidence = this.getAngleOfIncidence();
		
		Amount<Angle> dihedralEquivalentWing = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		for(int i=0; i<getPanels().size(); i++)
			dihedralEquivalentWing = dihedralEquivalentWing.plus(getPanels().get(i).getDihedral());
		dihedralEquivalentWing = dihedralEquivalentWing.divide(getPanels().size());
		
		//======================================================
		// creation of the equivalent wing:
		LiftingSurfacePanelCreator equivalentWingPanel = new 
				LiftingSurfacePanelBuilder(
						"Equivalent wing",
						chordRootEquivalentWing,
						chordTipEquivalentWing,
						airfoilRootEquivalent,
						airfoilTipEquivalent,
						twistGeometricTipEquivalentWing,
						getSemiSpan(),
						sweepLEEquivalentWing,
						dihedralEquivalentWing,
						angleOfIncidence)
				.build();
		
		_equivalentWing = new LiftingSurfaceCreator("Equivalent Wing");
		
		_equivalentWing.addPanel(equivalentWingPanel);
		
		return _equivalentWing;
	}


	@Override
	public LiftingSurfaceCreator getEquivalentWing(boolean recalculate) {
		if(recalculate)	this.calculateEquivalentWing();
		return this._equivalentWing;
	}

	/**
	 * Calculate wing's span and semi-span according to current values
	 * i.e. _panels' semi-spans and dihedral angles
	 */
	private void calculateSpans() {
		System.out.println("[LiftingSurfaceCreator] Wing span ...");
		Double bhalf = this.getPanels().stream()
				.mapToDouble(p ->
					p.getSemiSpan().to(SI.METRE).getEstimatedValue()
						*Math.cos(p.getDihedral().to(SI.RADIAN).getEstimatedValue())
				)
				.sum();
		this.semiSpan = Amount.valueOf(bhalf,SI.METRE);
		this.span = this.semiSpan.times(2.0);
	}

	private void calculateVariablesAtBreakpoints() {

		System.out.println("[LiftingSurfaceCreator] calculate variables at breakpoints ...");
		//======================================================
		// Break points Y's

		// root at symmetry plane
		_yBreakPoints.add(Amount.valueOf(0.0, 1e-8, SI.METRE));
		// Accumulate values and add
		
//		_yBreakPoints.addAll(
//			IntStream.range(1, this._panels.size())
//				.mapToObj(i ->
//					_yBreakPoints.get(i-1).plus( // semiSpan * cos( dihedral )
//						_panels.get(i-1).getSemiSpan()
//							.times(Math.cos(_panels.get(i-1).getDihedral().to(SI.RADIAN).getEstimatedValue()))
//					)
//				)
//				.collect(Collectors.toList())
//			);
		
		// TODO: CHECK THIS FOR MORE THAN 2 PANELS
		
		for(int i=1; i <= this._panels.size(); i++) {
			_yBreakPoints.add(
					_yBreakPoints.get(i-1).plus( // semiSpan * cos( dihedral )
							_panels.get(i-1).getSemiSpan()
								.times(Math.cos(_panels.get(i-1).getDihedral().to(SI.RADIAN).getEstimatedValue())
										)
								)
					);
		}
		_yBreakPoints.add(this.semiSpan);

		MyConfiguration.customizeAmountOutput();
		System.out.println("y Break-Points ->\n" + _yBreakPoints);

		// Leading-edge x at breakpoints
		_xLEBreakPoints.add(Amount.valueOf(0.0, 1e-8, SI.METRE));
		for (int i = 1; i <= this._panels.size(); i++) {
			Amount<Length> x0 = _xLEBreakPoints.get(i-1);
			Amount<Length> y = _yBreakPoints.get(i).minus(_yBreakPoints.get(i-1));
			Amount<Angle> sweepLE = _panels.get(i-1).getSweepLeadingEdge();
			_xLEBreakPoints.add(
				x0.plus(
						y.times(Math.tan(sweepLE.to(SI.RADIAN).getEstimatedValue()))
				));
		}

		MyConfiguration.customizeAmountOutput();
		System.out.println("xLE Break-Points ->\n" + _xLEBreakPoints);

		// Leading-edge z at breakpoints
		_zLEBreakPoints.add(Amount.valueOf(0.0, 1e-8, SI.METRE));
		for (int i = 1; i <= this._panels.size(); i++) {
			Amount<Length> z0 = _zLEBreakPoints.get(i-1);
			Amount<Length> y = _yBreakPoints.get(i).minus(_yBreakPoints.get(i-1));
			Amount<Angle> dihedral = _panels.get(i-1).getDihedral();
			_zLEBreakPoints.add(
				z0.plus(
						y.times(Math.tan(dihedral.to(SI.RADIAN).getEstimatedValue()))
				));
		}

		MyConfiguration.customizeAmountOutput();
		System.out.println("zLE Break-Points ->\n" + _zLEBreakPoints);

		// Chords at breakpoints
		_chordsBreakPoints.add(_panels.get(0).getChordRoot());
		for (int i = 0; i < this._panels.size(); i++) {
			_chordsBreakPoints.add(
					_panels.get(i).getChordTip()
				);
		}

		MyConfiguration.customizeAmountOutput();
		System.out.println("Chords Break-Points ->\n" + _chordsBreakPoints);

		// Twists at breakpoints
		_twistsBreakPoints.add(Amount.valueOf(0.0,1e-9,NonSI.DEGREE_ANGLE));
		for (int i = 0; i < this._panels.size(); i++) {
			_twistsBreakPoints.add(
					_panels.get(i).getTwistGeometricAtTip()
				);
		}

		MyConfiguration.customizeAmountOutput();
		System.out.println("Twists Break-Points ->\n" + _twistsBreakPoints);

		//======================================================
		// Break points eta's

		_etaBP = _yBreakPoints.stream()
			.mapToDouble(y ->
				y.to(SI.METRE).getEstimatedValue()/this.semiSpan.to(SI.METRE).getEstimatedValue())
			.boxed()
			.collect(Collectors.toList())
			;
//		System.out.println(etaBP);

	}

	/**
	 * Calculate Chord Distribution of the Actual Wing along y axis
	 */
	private void mapPanelsToYDiscretized() {

		System.out.println("[LiftingSurfaceCreator] Map _panels to spanwise discretized Ys ...");

		//======================================================
		// Map _panels with lists of Y's, c, Xle, Yle, Zle, twist
		// for each panel Y's of inner and outer break-points
		// are included, i.e. Y's are repeated

		Tuple2<
			List<LiftingSurfacePanelCreator>,
			List<Amount<Length>>
			> tuple0 = Tuple.of(_panels, _yStationActual);

		_panelToSpanwiseDiscretizedVariables.add(
			tuple0.map(
				p -> _panels.get(0),
				y -> Tuple.of(
					y.stream()
						// Innermost panel: Y's include 0 and panel's tip breakpoint Y
						.filter(y_ -> y_.isLessThan( _panels.get(0).getSemiSpan() ) || y_.equals( _panels.get(0).getSemiSpan()) )
						.collect(Collectors.toList())
					,
					y.stream()
						.filter(y_ -> y_.isLessThan( _panels.get(0).getSemiSpan() ) || y_.equals( _panels.get(0).getSemiSpan()) )
						.map(Y__ -> Amount.valueOf(0.0, SI.METRE))
						.collect(Collectors.toList()) // initialize Chords
					,
					y.stream()
						.filter(y_ -> y_.isLessThan( _panels.get(0).getSemiSpan() ) || y_.equals( _panels.get(0).getSemiSpan()) )
						.map(Y__ -> Amount.valueOf(0.0, SI.METRE))
						.collect(Collectors.toList()) // initialize Xle
					,
					y.stream()
						.filter(y_ -> y_.isLessThan( _panels.get(0).getSemiSpan() ) || y_.equals( _panels.get(0).getSemiSpan()) )
						.map(Y__ -> Amount.valueOf(0.0, SI.METRE))
						.collect(Collectors.toList()) // initialize Zle
					,
					y.stream()
						.filter(y_ -> y_.isLessThan( _panels.get(0).getSemiSpan() ) || y_.equals( _panels.get(0).getSemiSpan()) )
						.map(Y__ -> Amount.valueOf(0.0, SI.RADIAN))
						.collect(Collectors.toList()) // initialize twists
					)
				)
			);

		// All remaining _panels (innermost panel excluded)
		// Y's include only panel's tip breakpoint Y,
		// not including panel's root breakpoint Y
		for (int i=1; i < _panels.size(); i++) {
			final int i_ = i;
			_panelToSpanwiseDiscretizedVariables.add(
				tuple0.map(
					p -> _panels.get(i_),
					y -> Tuple.of(
						y.stream()
							.mapToDouble(a -> a.to(SI.METRE).getEstimatedValue())
							.filter(y_ -> (
									y_ > _yBreakPoints.get(i_).getEstimatedValue() )
									&& ( y_ <= _yBreakPoints.get(i_+1).getEstimatedValue() )
								)
							.mapToObj(y_ -> Amount.valueOf(y_, SI.METRE))
							.collect(Collectors.toList())
						,
						y.stream()
							.mapToDouble(a -> a.to(SI.METRE).getEstimatedValue())
							.filter(y_ -> (
									y_ > _yBreakPoints.get(i_).getEstimatedValue() )
									&& ( y_ <= _yBreakPoints.get(i_+1).getEstimatedValue() )
								)
							.mapToObj(y_ -> Amount.valueOf(0.0, SI.METRE))
							.collect(Collectors.toList()) // initialize Chords
						,
						y.stream()
							.mapToDouble(a -> a.to(SI.METRE).getEstimatedValue())
							.filter(y_ -> (
									y_ > _yBreakPoints.get(i_).getEstimatedValue() )
									&& ( y_ <= _yBreakPoints.get(i_+1).getEstimatedValue() )
								)
							.mapToObj(y_ -> Amount.valueOf(0.0, SI.METRE))
							.collect(Collectors.toList()) // initialize Xle
						,
						y.stream()
							.mapToDouble(a -> a.to(SI.METRE).getEstimatedValue())
							.filter(y_ -> (
									y_ > _yBreakPoints.get(i_).getEstimatedValue() )
									&& ( y_ <= _yBreakPoints.get(i_+1).getEstimatedValue() )
								)
							.mapToObj(y_ -> Amount.valueOf(0.0, SI.METRE))
							.collect(Collectors.toList()) // initialize Zle
						,
						y.stream()
							.mapToDouble(a -> a.to(SI.METRE).getEstimatedValue())
							.filter(y_ -> (
									y_ > _yBreakPoints.get(i_).getEstimatedValue() )
									&& ( y_ <= _yBreakPoints.get(i_+1).getEstimatedValue() )
								)
							.mapToObj(y_ -> Amount.valueOf(0.0, SI.RADIAN))
							.collect(Collectors.toList()) // initialize twists
						)
					)
				);
		}// end-of for

	}

	/**
	 * Calculate Chord Distribution of the Actual Wing along y axis
	 */
	private void calculateChordsAtYDiscretized() {

		System.out.println("[LiftingSurfaceCreator] Map _panels to spanwise discretized chords ...");

		//======================================================
		// Set chords versus Y's
		// according to location within _panels/yBP

		for (int k=0; k < _panelToSpanwiseDiscretizedVariables.size(); k++) {
			LiftingSurfacePanelCreator panel = _panelToSpanwiseDiscretizedVariables.get(k)._1();
			Amount<Length> y0 = _yBreakPoints.get(k);
			List<Amount<Length>> vY = _panelToSpanwiseDiscretizedVariables.get(k)._2()._1(); // Ys
			List<Amount<Length>> vC = _panelToSpanwiseDiscretizedVariables.get(k)._2()._2(); // Chords
			IntStream.range(0, vY.size())
				.forEach(i -> {
					Amount<Length> y = vY.get(i).minus(y0);
					// c(y) = cr + (2/b)*(ct - cr)*y
					Amount<Length> c = panel.getChordRoot().plus(
						y.times(
							panel.getChordTip().minus(panel.getChordRoot())
						).divide(panel.getSemiSpan())
						);
					// assign the chord
					vC.set(i, c);
				});
		}

	}

	private void calculateXZleTwistAtYDiscretized() {

		System.out.println("[LiftingSurfaceCreator] Map _panels to spanwise discretized Xle, Yle, twist ...");

		for (int k=0; k < _panelToSpanwiseDiscretizedVariables.size(); k++) {
			LiftingSurfacePanelCreator panel = _panelToSpanwiseDiscretizedVariables.get(k)._1();
			Amount<Length> y0 = _yBreakPoints.get(k);
			Amount<Length> x0 = _xLEBreakPoints.get(k);
			Amount<Length> z0 = _zLEBreakPoints.get(k);
			Amount<Angle> twist0 = _twistsBreakPoints.get(k);

			List<Amount<Length>> vY = _panelToSpanwiseDiscretizedVariables.get(k)._2()._1(); // Ys
			@SuppressWarnings("unused")
			List<Amount<Length>> vC = _panelToSpanwiseDiscretizedVariables.get(k)._2()._2(); // Chords
			List<Amount<Length>> vXLE = _panelToSpanwiseDiscretizedVariables.get(k)._2()._3(); // XLEs
			List<Amount<Length>> vZLE = _panelToSpanwiseDiscretizedVariables.get(k)._2()._4(); // ZLEs
			List<Amount<Angle>> vTwistsLE = _panelToSpanwiseDiscretizedVariables.get(k)._2()._5(); // Twists

			Amount<Angle> sweepLE = panel.getSweepLeadingEdge();
			Amount<Angle> dihedral = panel.getDihedral();

			IntStream.range(0, vY.size())
				.forEach(i -> {
					// y := Y - y0
					Amount<Length> y = vY.get(i).minus(y0);
					// xle = x0 + y * tan(sweepLE)
					Amount<Length> xle =
						x0.plus(
							y.times(Math.tan(sweepLE.to(SI.RADIAN).getEstimatedValue()))
						);
					// assign the xle
					vXLE.set(i, xle);
					// zle = z0 + y * tan(dihedral)
					Amount<Length> zle =
							z0.plus(
								y.times(Math.tan(dihedral.to(SI.RADIAN).getEstimatedValue()))
							);
					vZLE.set(i, zle);
					//
					// twist(y) = twist_r + (2/b)*(twist_t - twist_r)*y
					Amount<Angle> twist = twist0.plus(
						y.times(
							panel.getTwistGeometricAtTip().minus(twist0)
						).divide(panel.getSemiSpan())
						);
					// assign the chord
					vTwistsLE.set(i, twist);
				});
		}
	}

	private void calculateDiscretizedGeometry() {
		System.out.println("[LiftingSurfaceCreator] Map Ys to spanwise discretized variables ...");

		List<Amount<Length>> vy = new ArrayList<>();
		List<Amount<Length>> vc = new ArrayList<>();
		List<Amount<Length>> vxle = new ArrayList<>();
		List<Amount<Length>> vzle = new ArrayList<>();
		List<Amount<Angle>> vtwist = new ArrayList<>();

		for (int kp = 0; kp < _panelToSpanwiseDiscretizedVariables.size(); kp++) {

			// sublist indexing criteria
			int idxEnd = _panelToSpanwiseDiscretizedVariables.get(kp)
						._2()._1().size()
						// - 1 not necessary
						;
//			if (kp == (_panelToSpanwiseDiscretizedVariables.size() - 1))
//				idxEnd += 1;

			// System.out.println("kp=" + kp + ", end=" + idxEndExcluded);

			vy.addAll(
				_panelToSpanwiseDiscretizedVariables.get(kp)
					._2()._1() // Ys
					.subList(0, idxEnd)
					);
			vc.addAll(
					_panelToSpanwiseDiscretizedVariables.get(kp)
						._2()._2() // Chords
						.subList(0, idxEnd)
						);
			vxle.addAll(
					_panelToSpanwiseDiscretizedVariables.get(kp)
						._2()._3() // Xle's
						.subList(0, idxEnd)
						);
			vzle.addAll(
					_panelToSpanwiseDiscretizedVariables.get(kp)
						._2()._4() // Zle's
						.subList(0, idxEnd)
						);
			vtwist.addAll(
					_panelToSpanwiseDiscretizedVariables.get(kp)
						._2()._5() // Twists
						.subList(0, idxEnd)
						);
		}
//		System.out.println("== y ==> size: " + vy.size() + "\n" + vy);
//		System.out.println("== c ==> size: " + vc.size() + "\n" + vc);

		for(int i = 0; i < vy.size(); i++) {
			_spanwiseDiscretizedVariables.add(
				Tuple.of(
					vy.get(i),
					vc.get(i),
					vxle.get(i),
					vzle.get(i),
					vtwist.get(i)
					)
				);
		}
//		System.out.println("==*==> \n" + _spanwiseDiscretizedVariables);

	}

	public void reportPanelsToSpanwiseDiscretizedVariables(){

		System.out.println("=====================================================");
		System.out.println("List of Tuples, size " + _panelToSpanwiseDiscretizedVariables.size());
//		System.out.println(_panelToSpanwiseDiscretizedVariables);

		_panelToSpanwiseDiscretizedVariables.stream()
			.forEach( tup2 -> {
				StringBuilder sb = new StringBuilder();
				sb
				.append("=====================================================\n")
				.append("Panel '" + tup2._1().getId() + "'")
				.append("\n")
				.append("Ys: size ")
				.append(
					tup2
						._2() // Tuple6
						._1() // Ys
						.size()
					)
				.append("\n")
				.append(
					tup2
						._2() // Tuple5
						._1() // Ys
					);
				sb
				.append("\n")
				.append("Chords: size ")
				.append(
					tup2
						._2() // Tuple5
						._2() // Chords
						.size()
					)
				.append("\n")
				.append(
					tup2
						._2() // Tuple5
						._2() // Chords
					);
				sb
				.append("\n")
				.append("Xle's: size ")
				.append(
					tup2
						._2() // Tuple5
						._3() // Xle's
						.size()
					)
				.append("\n")
				.append(
					tup2
						._2() // Tuple5
						._3() // Xle's
					)
				;
				sb
				.append("\n")
				.append("Zle's: size ")
				.append(
					tup2
						._2() // Tuple5
						._4() // Zle's
						.size()
					)
				.append("\n")
				.append(
					tup2
						._2() // Tuple5
						._4() // Zle's
					)
				;
				sb
				.append("\n")
				.append("Twists: size ")
				.append(
					tup2
						._2() // Tuple5
						._5() // Twists
						.size()
					)
				.append("\n")
				.append(
					tup2
						._2() // Tuple5
						._5() // Twists
					)
				.append("\n")
				;

				// spit out the string
				System.out.println(sb.toString());
			}
		);

	}

	private void reportDiscretizedVariables(){

		System.out.println("=====================================================");
		System.out.println("Spanwise discretized wing, size " + _spanwiseDiscretizedVariables.size());

		System.out.println("Y, chord, Xle, Zle, twist");

		StringBuilder sb = new StringBuilder();

		_spanwiseDiscretizedVariables.stream()
			.forEach( t5 ->
				sb.append(
						t5.toString() + "\n"
				)
			);
		// spit out the string
		System.out.println(sb.toString());

	}

	@Override
	public List<Amount<Length>> getDiscretizedYs() {
		return _spanwiseDiscretizedVariables.stream()
			.mapToDouble(t5 ->
				t5._1()
				.to(SI.METRE).getEstimatedValue())
			.mapToObj(y -> Amount.valueOf(y, 1e-8, SI.METRE))
			.collect(Collectors.toList());
	}

	@Override
	public List<Amount<Length>> getDiscretizedChords() {
		return _spanwiseDiscretizedVariables.stream()
				.mapToDouble(t5 ->
					t5._2()
					.to(SI.METRE).getEstimatedValue())
				.mapToObj(y -> Amount.valueOf(y, 1e-8, SI.METRE))
				.collect(Collectors.toList());
	}

	@Override
	public List<Amount<Length>> getDiscretizedXle() {
		return _spanwiseDiscretizedVariables.stream()
				.mapToDouble(t5 ->
					t5._3()
					.to(SI.METRE).getEstimatedValue())
				.mapToObj(y -> Amount.valueOf(y, 1e-8, SI.METRE))
				.collect(Collectors.toList());
	}

	@Override
	public List<Amount<Length>> getDiscretizedZle() {
		return _spanwiseDiscretizedVariables.stream()
				.mapToDouble(t5 ->
					t5._4()
					.to(SI.METRE).getEstimatedValue())
				.mapToObj(y -> Amount.valueOf(y, 1e-8, SI.METRE))
				.collect(Collectors.toList());
	}

	@Override
	public Amount<Length> getXLEAtYActual(Double yStation) {
		return Amount.valueOf(
				GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(getDiscretizedXle()),
						yStation
						),
				SI.METER
				);
	}

	@Override
	public Amount<Angle> getDihedralAtYActual(Double yStation) {
		if (yStation >= 0) return getDihedralSemispanAtYActual(yStation);
		else return getDihedralSemispanAtYActual(-yStation);
	}
	
	@Override
	public Amount<Angle> getDihedralSemispanAtYActual(Double yStation) {
		
		Amount<Angle> dihedralAtY = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(yStation <= this._yBreakPoints.get(0).getEstimatedValue()) {
			System.err.println("INVALID Y STATION");
			dihedralAtY = null;
		}
		
		for(int i=1; i<this._yBreakPoints.size(); i++) {
			
			if(yStation <= this._yBreakPoints.get(i).getEstimatedValue()
					&& yStation > this._yBreakPoints.get(i-1).getEstimatedValue()
					)
				dihedralAtY = this._panels.get(i).getDihedral();
		}
		
		return dihedralAtY;
	}
	
	@Override
	public List<
		Tuple2<
			Amount<Length>, // Ys
			Amount<Length>  // Xs
			>
		> getDiscretizedTopViewAsList() {

		List<Tuple2<Amount<Length>,Amount<Length>>> listYX = new ArrayList<>();

		// leading edge (straight)
		IntStream.range(0, _spanwiseDiscretizedVariables.size())
			.forEach(i -> {
				listYX.add(Tuple.of(
						_spanwiseDiscretizedVariables.get(i)._1(), // y
						_spanwiseDiscretizedVariables.get(i)._3()  // xle
						)
					);
			});

		// trailing edge, reverse order
		int num = _spanwiseDiscretizedVariables.size() - 1;
		IntStream.rangeClosed(0, num)
			.forEach(i -> {
				listYX.add(Tuple.of(
						_spanwiseDiscretizedVariables.get(num - i)._1(),
						_spanwiseDiscretizedVariables.get(num - i)._3() // xle
							.plus(
								_spanwiseDiscretizedVariables.get(num - i)._2() // + chord
							)
						)
					);
			});

		return listYX;
	}

	@Override
	public Double[][] getDiscretizedTopViewAsArray() {
		// see:
		// http://stackoverflow.com/questions/26050530/filling-a-multidimensional-array-using-a-stream/26053236#26053236

		List<Tuple2<Amount<Length>,Amount<Length>>> listYX = getDiscretizedTopViewAsList();

		Double[][] array = new Double[listYX.size()][2];
		IntStream.range(0, listYX.size())
			.forEach(i -> {
				array[i][0] = listYX.get(i)._1().doubleValue(SI.METRE);
				array[i][1] = listYX.get(i)._2().doubleValue(SI.METRE);
			});
		return array;
	}

	@Override
	public List<Amount<Angle>> getDiscretizedTwists() {
		return _spanwiseDiscretizedVariables.stream()
				.mapToDouble(t5 ->
					t5._5()
					.to(SI.RADIAN).getEstimatedValue())
				.mapToObj(y -> Amount.valueOf(y, 1e-9, SI.RADIAN))
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder();
		if(!this._equivalentWingFlag) {
			sb.append("\t-------------------------------------\n")
			.append("\tLifting surface\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + id + "'\n")
			.append("\tNo. _panels " + _panels.size() + "\n")
			;
			for (LiftingSurfacePanelCreator panel : _panels) {
				sb.append(panel.toString());
			}

			sb.append("\t---------------------------------------\n")
			.append("\tEquivalent wing\n")
			.append("\t---------------------------------------\n")
			;
			sb.append(this._equivalentWing.getPanels().get(0).toString());

			if(!(_symmetricFlaps == null)) {
				for (SymmetricFlapCreator symmetricFlap : _symmetricFlaps) {
					sb.append(symmetricFlap.toString());
				}
			}
		}
		else {
			sb.append("\t-------------------------------------\n")
			.append("\tLifting surface\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + id + "'\n")
			.append("\t---------------------------------------\n")
			.append("\tEquivalent wing\n")
			.append("\t---------------------------------------\n")
			.append("\tSurface: " + this._equivalentWingSurface + "\n")
			.append("\tAspect ratio: " + this._equivalentWingAspectRatio + "\n")
			.append("\tSweep c/4: " + this._sweepQuarterChordEquivalentWing + "\n")
			.append("\tTaperRatio: " + this._taperRatioEquivalentWing + "\n")
			.append("\tKink station: " + this._nonDimensionalSpanStationKink + "\n")
			.append("\tAngle of incidence: " + this._angleOfIncidenceEquivalentWing + "\n")
			.append("\tTwist at tip: " + this._twistAtTipEquivalentWing + "\n")
			.append("\tDihedral: " + this._dihedralEquivalentWing + "\n")
			.append("\tX offset leanding edge at root: " + this._xOffsetEquivalentWingRootLE + "\n")
			.append("\tX offset trailing edge at root: " + this._xOffsetEquivalentWingRootTE + "\n")
			.append("\t---------------------------------------\n")
			.append("\tLifting surface (2 panels) from equivalent wing\n")
			.append("\t-------------------------------------\n")
			;
			
			for (LiftingSurfacePanelCreator panel : _panels) {
				sb.append(panel.toString());
			}
		}

		if(!(_slats == null)) {
			for (SlatCreator slats : _slats) {
				sb.append(slats.toString());
			}
		}
		
		if(!(_asymmetricFlaps == null)) {
			for (AsymmetricFlapCreator asymmetricFlaps : _asymmetricFlaps) {
				sb.append(asymmetricFlaps.toString());
			}
		}
		
		if(!(_spoilers == null)) {
			for (SpoilerCreator spoilers : _spoilers) {
				sb.append(spoilers.toString());
			}
		}
		
		reportDiscretizedVariables();

		sb
			.append("\t=====================================\n")
			.append("\tOverall wing derived data\n")
			.append("\tSpan: " + this.getSpan().to(SI.METRE) +"\n")
			.append("\tSemi-span: " + this.getSemiSpan().to(SI.METRE) +"\n")
			.append("\tSurface of planform: " + this.getSurfacePlanform().to(SI.SQUARE_METRE) +"\n")
			.append("\tSurface wetted: " + this.getSurfaceWetted().to(SI.SQUARE_METRE) + "\n")
			.append("\tAspect-ratio: " + this.getAspectRatio() +"\n")
			.append("\tMean aerodynamic chord: " + this.getMeanAerodynamicChord() +"\n")
			.append("\t(X,Y,Z)_le of mean aerodynamic chord: " + this.getMeanAerodynamicChordLeadingEdge() +"\n")
			;

		// TODO add more data in log message

		return sb.toString();
	}

	public Boolean getEquivalentWingFlag() {
		return _equivalentWingFlag;
	}

	public Amount<Area> getEquivalentWingSurface() {
		return _equivalentWingSurface;
	}

	public Double getEquivalentWingAspectRatio() {
		return _equivalentWingAspectRatio;
	}

	public Double getNonDimensionalSpanStationKink() {
		return _nonDimensionalSpanStationKink;
	}

	public Amount<Angle> getSweepQuarterChordEquivalentWing() {
		return _sweepQuarterChordEquivalentWing;
	}

	public Double getTaperRatioEquivalentWing() {
		return _taperRatioEquivalentWing;
	}
	
	public Amount<Length> getXOffsetEquivalentWingRootLE() {
		return _xOffsetEquivalentWingRootLE;
	}

	public Amount<Length> getXOffsetEquivalentWingRootTE() {
		return _xOffsetEquivalentWingRootTE;
	}

	public void setEquivalentWingFlag(Boolean _equivalentWingFlag) {
		this._equivalentWingFlag = _equivalentWingFlag;
	}
	
	public void setEquivalentWingSurface(Amount<Area> _equivalentWingSurface) {
		this._equivalentWingSurface = _equivalentWingSurface;
	}

	public void setEquivalentWingAspectRatio(Double _equivalentWingAspectRatio) {
		this._equivalentWingAspectRatio = _equivalentWingAspectRatio;
	}

	public void setNonDimensionalSpanStationKink(Double _nonDimensionalSpanStationKink) {
		this._nonDimensionalSpanStationKink = _nonDimensionalSpanStationKink;
	}

	public void setSweepQuarterChordEquivalentWing(Amount<Angle> _sweepQuarterChordEquivalentWing) {
		this._sweepQuarterChordEquivalentWing = _sweepQuarterChordEquivalentWing;
	}

	public void setXOffsetEquivalentWingRootLE(Amount<Length> _xOffsetEquivalentWingRootLE) {
		this._xOffsetEquivalentWingRootLE = _xOffsetEquivalentWingRootLE;
	}

	public void setXOffsetEquivalentWingRootTE(Amount<Length> _xOffsetEquivalentWingRootTE) {
		this._xOffsetEquivalentWingRootTE = _xOffsetEquivalentWingRootTE;
	}
	
	public void setTaperRatioEquivalentWing(Double _taperRatioEquivalentWing) {
		this._taperRatioEquivalentWing = _taperRatioEquivalentWing;
	}

	public AirfoilCreator getAirfoilRootEquivalentWing() {
		return airfoilRootEquivalentWing;
	}

	public AirfoilCreator getAirfoilKinkEquivalentWing() {
		return airfoilKinkEquivalentWing;
	}

	public AirfoilCreator getAirfoilTipEquivalentWing() {
		return airfoilTipEquivalentWing;
	}

	public void setAirfoilRootEquivalentWing(AirfoilCreator airfoilRootEquivalentWing) {
		this.airfoilRootEquivalentWing = airfoilRootEquivalentWing;
	}

	public void setAirfoilKinkEquivalentWing(AirfoilCreator airfoilKinkEquivalentWing) {
		this.airfoilKinkEquivalentWing = airfoilKinkEquivalentWing;
	}

	public void setAirfoilTipEquivalentWing(AirfoilCreator airfoilTipEquivalentWing) {
		this.airfoilTipEquivalentWing = airfoilTipEquivalentWing;
	}

	public Amount<Angle> getAngleOfIncidenceEquivalentWing() {
		return _angleOfIncidenceEquivalentWing;
	}

	public Amount<Angle> getDihedralEquivalentWing() {
		return _dihedralEquivalentWing;
	}

	public void setAngleOfIncidenceEquivalentWing(Amount<Angle> _angleOfIncidenceEquivalentWing) {
		this._angleOfIncidenceEquivalentWing = _angleOfIncidenceEquivalentWing;
	}

	public void setDihedralEquivalentWing(Amount<Angle> _dihedralEquivalentWing) {
		this._dihedralEquivalentWing = _dihedralEquivalentWing;
	}

	public Amount<Angle> getTwistAtTipEquivalentWing() {
		return _twistAtTipEquivalentWing;
	}

	public void setTwistAtTipEquivalentWing(Amount<Angle> _twistAtTipEquivalentWing) {
		this._twistAtTipEquivalentWing = _twistAtTipEquivalentWing;
	}

	public LiftingSurfaceCreator getWing2Panels() {
		return _wing2Panels;
	}
}
