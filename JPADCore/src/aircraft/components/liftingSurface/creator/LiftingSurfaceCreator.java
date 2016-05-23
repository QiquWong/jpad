package aircraft.components.liftingSurface.creator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.poi.util.SystemOutLogger;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator.LiftingSurfacePanelBuilder;
import configuration.MyConfiguration;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple5;
import javolution.text.TypeFormat;
import javolution.text.TextFormat.Cursor;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.MyArray;

public class LiftingSurfaceCreator extends AbstractLiftingSurface {

	int _numberOfSpanwisePoints = 15;

	public LiftingSurfaceCreator(String id) {
		this.id = id;
		resetData();
	}

	// use this to generate the equivalent wing or a simple wing
	public LiftingSurfaceCreator(String id, LiftingSurfacePanelCreator panel) {
		this.id = id;
		resetData();

		panels.add(panel);

		//---------------------------------------------------------------------------------
		// SYMMETRIC FLAPS
		// TODO

		//---------------------------------------------------------------------------------
		// SYMMETRIC SLATS
		// TODO

		//---------------------------------------------------------------------------------
		// ASYMMETRIC FLAPS
		// TODO

		//---------------------------------------------------------------------------------
		// SPOILERS
		// TODO

	}

	private void resetData() {
		panels = new ArrayList<LiftingSurfacePanelCreator>();

		_eta = new MyArray(Unit.ONE);
		//_eta.setDouble(MyArrayUtils.linspace(0., 1., _numberOfPointsChordDistribution));
		//
		// assign eta's when the shape of the planform is loaded and no. panels are known

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

	public static LiftingSurfaceCreator importFromXML(String pathToXML, String airfoilsDir) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading lifting surface data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing/@id");

		LiftingSurfaceCreator wing = new LiftingSurfaceCreator(id);

		//---------------------------------------------------------------------------------
		// PANELS

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

		// Update panels' internal geometry variables
		// wing.calculateGeometry(); // shouldn't care about discretization
		// for now the user calculates the geometry from the outside of the class
		// via the wing object:
		//
		//     theWing.calculateGeometry(30);

		//---------------------------------------------------------------------------------
		// SYMMETRIC FLAPS
		// TODO

		//---------------------------------------------------------------------------------
		// SYMMETRIC SLATS
		// TODO

		//---------------------------------------------------------------------------------
		// ASYMMETRIC FLAPS
		// TODO

		//---------------------------------------------------------------------------------
		// SPOILERS
		// TODO



		return wing;
	}

	@Override
	public void addPanel(LiftingSurfacePanelCreator panel) {
		panels.add(panel);
	}

	@Override
	public void calculateGeometry() {
		calculateGeometry(_numberOfSpanwisePoints);
	}

	@Override
	public void calculateGeometry(int numberSpanwiseStations) {

		System.out.println("[LiftingSurfaceCreator] Calculating derived geometry parameters of wing ...");

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

	}

	private void calculateMAC() {

		// Mean Aerodynamic Chord

		//======================================================
		// Weighted sum on MACs of single panels
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
	public LiftingSurfaceCreator getEquivalentWing() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public LiftingSurfaceCreator getEquivalentWing(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Calculate wing's span and semi-span according to current values
	 * i.e. panels' semi-spans and dihedral angles
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
		_yBreakPoints.addAll(
			IntStream.range(1, this.panels.size())
				.mapToObj(i ->
					_yBreakPoints.get(i-1).plus( // semiSpan * cos( dihedral )
						panels.get(i-1).getSemiSpan()
							.times(Math.cos(panels.get(i-1).getDihedral().to(SI.RADIAN).getEstimatedValue()))
					)
				)
				.collect(Collectors.toList())
			);
		_yBreakPoints.add(this.semiSpan);

		MyConfiguration.customizeAmountOutput();
		System.out.println("y Break-Points ->\n" + _yBreakPoints);

		// Leading-edge x at breakpoints
		_xLEBreakPoints.add(Amount.valueOf(0.0, 1e-8, SI.METRE));
		for (int i = 1; i <= this.panels.size(); i++) {
			Amount<Length> x0 = _xLEBreakPoints.get(i-1);
			Amount<Length> y = _yBreakPoints.get(i).minus(_yBreakPoints.get(i-1));
			Amount<Angle> sweepLE = panels.get(i-1).getSweepLeadingEdge();
			_xLEBreakPoints.add(
				x0.plus(
						y.times(Math.tan(sweepLE.to(SI.RADIAN).getEstimatedValue()))
				));
		}

		MyConfiguration.customizeAmountOutput();
		System.out.println("xLE Break-Points ->\n" + _xLEBreakPoints);

		// Leading-edge z at breakpoints
		_zLEBreakPoints.add(Amount.valueOf(0.0, 1e-8, SI.METRE));
		for (int i = 1; i <= this.panels.size(); i++) {
			Amount<Length> z0 = _zLEBreakPoints.get(i-1);
			Amount<Length> y = _yBreakPoints.get(i).minus(_yBreakPoints.get(i-1));
			Amount<Angle> dihedral = panels.get(i-1).getDihedral();
			_zLEBreakPoints.add(
				z0.plus(
						y.times(Math.tan(dihedral.to(SI.RADIAN).getEstimatedValue()))
				));
		}

		MyConfiguration.customizeAmountOutput();
		System.out.println("zLE Break-Points ->\n" + _zLEBreakPoints);

		// Chords at breakpoints
		_chordsBreakPoints.add(panels.get(0).getChordRoot());
		for (int i = 0; i < this.panels.size(); i++) {
			_chordsBreakPoints.add(
					panels.get(i).getChordTip()
				);
		}

		MyConfiguration.customizeAmountOutput();
		System.out.println("Chords Break-Points ->\n" + _chordsBreakPoints);

		// Twists at breakpoints
		_twistsBreakPoints.add(Amount.valueOf(0.0,1e-9,NonSI.DEGREE_ANGLE));
		for (int i = 0; i < this.panels.size(); i++) {
			_twistsBreakPoints.add(
					panels.get(i).getTwistGeometricAtTip()
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

		System.out.println("[LiftingSurfaceCreator] Map panels to spanwise discretized Ys ...");

		//======================================================
		// Map panels with lists of Y's, c, Xle, Yle, Zle, twist
		// for each panel Y's of inner and outer break-points
		// are included, i.e. Y's are repeated

		Tuple2<
			List<LiftingSurfacePanelCreator>,
			List<Amount<Length>>
			> tuple0 = Tuple.of(panels, _yStationActual);

		_panelToSpanwiseDiscretizedVariables.add(
			tuple0.map(
				p -> panels.get(0),
				y -> Tuple.of(
					y.stream()
						// Innermost panel: Y's include 0 and panel's tip breakpoint Y
						.filter(y_ -> y_.isLessThan( panels.get(0).getSemiSpan() ) || y_.equals( panels.get(0).getSemiSpan()) )
						.collect(Collectors.toList())
					,
					y.stream()
						.filter(y_ -> y_.isLessThan( panels.get(0).getSemiSpan() ) || y_.equals( panels.get(0).getSemiSpan()) )
						.map(Y__ -> Amount.valueOf(0.0, SI.METRE))
						.collect(Collectors.toList()) // initialize Chords
					,
					y.stream()
						.filter(y_ -> y_.isLessThan( panels.get(0).getSemiSpan() ) || y_.equals( panels.get(0).getSemiSpan()) )
						.map(Y__ -> Amount.valueOf(0.0, SI.METRE))
						.collect(Collectors.toList()) // initialize Xle
					,
					y.stream()
						.filter(y_ -> y_.isLessThan( panels.get(0).getSemiSpan() ) || y_.equals( panels.get(0).getSemiSpan()) )
						.map(Y__ -> Amount.valueOf(0.0, SI.METRE))
						.collect(Collectors.toList()) // initialize Zle
					,
					y.stream()
						.filter(y_ -> y_.isLessThan( panels.get(0).getSemiSpan() ) || y_.equals( panels.get(0).getSemiSpan()) )
						.map(Y__ -> Amount.valueOf(0.0, SI.RADIAN))
						.collect(Collectors.toList()) // initialize twists
					)
				)
			);

		// All remaining panels (innermost panel excluded)
		// Y's include only panel's tip breakpoint Y,
		// not including panel's root breakpoint Y
		for (int i=1; i < panels.size(); i++) {
			final int i_ = i;
			_panelToSpanwiseDiscretizedVariables.add(
				tuple0.map(
					p -> panels.get(i_),
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

		System.out.println("[LiftingSurfaceCreator] Map panels to spanwise discretized chords ...");

		//======================================================
		// Set chords versus Y's
		// according to location within panels/yBP

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

		System.out.println("[LiftingSurfaceCreator] Map panels to spanwise discretized Xle, Yle, twist ...");

		for (int k=0; k < _panelToSpanwiseDiscretizedVariables.size(); k++) {
			LiftingSurfacePanelCreator panel = _panelToSpanwiseDiscretizedVariables.get(k)._1();
			Amount<Length> y0 = _yBreakPoints.get(k);
			Amount<Length> x0 = _xLEBreakPoints.get(k);
			Amount<Length> z0 = _zLEBreakPoints.get(k);
			Amount<Angle> twist0 = _twistsBreakPoints.get(k);

			List<Amount<Length>> vY = _panelToSpanwiseDiscretizedVariables.get(k)._2()._1(); // Ys
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

		StringBuilder sb = new StringBuilder()
			.append("\t-------------------------------------\n")
			.append("\tLifting surface\n")
			.append("\t-------------------------------------\n")
			.append("\tID: '" + id + "'\n")
			.append("\tNo. panels " + panels.size() + "\n")
			;
		for (LiftingSurfacePanelCreator panel : panels) {
			sb.append(panel.toString());
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

}
