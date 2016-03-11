package aircraft.components.liftingSurface.adm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.util.SystemOutLogger;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.liftingSurface.adm.LiftingSurfacePanel.LiftingSurfacePanelBuilder;
import configuration.MyConfiguration;
import javaslang.Tuple;
import javaslang.Tuple2;
import javolution.text.TypeFormat;
import javolution.text.TextFormat.Cursor;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.MyArray;

public class LiftingSurface extends AbstractLiftingSurface {

	int _numberOfPointsChordDistribution = 30;

	public LiftingSurface(String id) {

		this.id = id;
		panels = new ArrayList<LiftingSurfacePanel>();

		_eta = new MyArray(Unit.ONE);

		//_eta.setDouble(MyArrayUtils.linspace(0., 1., _numberOfPointsChordDistribution));
		//
		// assign eta's when the shape of the planform is loaded and no. panels are known

		_yBreakPoints =  new ArrayList<Amount<Length>>();
		_xLEBreakPoints = new ArrayList<Amount<Length>>();
		_zLEBreakPoints = new ArrayList<Amount<Length>>();
		_chordsBreakPoints = new ArrayList<Amount<Length>>();
		_twistsBreakPoints = new ArrayList<Amount<Angle>>();
		
//		_panelToYStations =
//			    new HashMap<LiftingSurfacePanel, List<Amount<Length>>>();
		
		_yStationActual = new ArrayList<Amount<Length>>(); // new MyArray(SI.METER); // new MyArray(SI.METER);

		_panelToSpanwiseDiscretizedVariables = new ArrayList<>();
		
//		_chordsVsYActual = new MyArray(SI.METER);
//		_xLEvsYActual = new MyArray(SI.METER);
//		_xTEvsYActual = new MyArray(SI.METER);

	}

	public static LiftingSurface importFromXML(String pathToXML, String airfoilsDir) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading lifting surface data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing/@id");

		LiftingSurface wing = new LiftingSurface(id);

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
            	wing.addPanel(LiftingSurfacePanel.importFromPanelNode(nodePanel, airfoilsDir));
            } else {
            	LiftingSurfacePanel panel0 = wing.getPanels().stream()
            			.filter(p -> p.getId().equals(elementPanel.getAttribute("linked_to")))
            			.findFirst()
            			.get()
            			;
            	if (panel0 != null) {
                	System.out.println("Panel linked_to: **" + elementPanel.getAttribute("linked_to") + "**");
            		wing.addPanel(LiftingSurfacePanel.importFromPanelNodeLinked(nodePanel, panel0, airfoilsDir));
            	} else {
            		System.out.println("WARNING: panel not parsed. Unable to find the ID of linked_to attribute!");
            	}
            }
		}

		// Update panels' internal geometry variables
		wing.calculateGeometry();

		//---------------------------------------------------------------------------------
		// SYMMETRIC FLAPS


		//---------------------------------------------------------------------------------
		// SYMMETRIC SLATS

		//---------------------------------------------------------------------------------
		// ASYMMETRIC FLAPS

		//---------------------------------------------------------------------------------
		// SPOILERS



		return wing;
	}


	@Override
	public void addPanel(LiftingSurfacePanel panel) {
		panels.add(panel);
	}

	@Override
	public void calculateGeometry() {
		// Update inner geometric variables of each panel
		this.getPanels().stream()
			.forEach(LiftingSurfacePanel::calculateGeometry);

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
		// Assign lists of Y's to each panel
		mapPanelsToYDiscretized();

		//======================================================
		// Map Y's to chord
		calculateChordsAtYDiscretized();
		
		//======================================================
		// Map Y's to (Xle, Zle)
		calculateXZLEAtYDiscretized();
		
		reportPanesToSpanwiseDiscretizedVariables();

		// Aspect-ratio
		this.aspectRatio = (this.span.pow(2)).divide(this.surfacePlanform).getEstimatedValue();

		// Mean Aerodynamic Chord
		Double mac = this.getPanels().stream()
				.mapToDouble(p ->
					p.getMeanAerodynamicChord().to(SI.METRE).getEstimatedValue()
					*p.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue())
				.sum();
		mac = mac / this.getSurfacePlanform().to(SI.SQUARE_METRE).getEstimatedValue();
		this.meanAerodynamicChord = Amount.valueOf(mac,SI.METRE);


	}

	@Override
	public Amount<Length>[] getXYZ0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getX0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getY0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getZ0() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXYZ0(Amount<Length> x0, Amount<Length> y0, Amount<Length> z0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Amount<Length>[] getXYZPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getXPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getYPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getZPole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXYZPole(Amount<Length> xp, Amount<Length> yp, Amount<Length> zp) {
		// TODO Auto-generated method stub

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
	public Amount<Length>[] getMeanAerodynamicChordLeadingEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length>[] getMeanAerodynamicChordLeadingEdge(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeX(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeY() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeY(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeZ() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodynamicChordLeadingEdgeZ(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getTaperRatio(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LiftingSurface getEquivalentWing() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public LiftingSurface getEquivalentWing(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Calculate wing's span and semi-span according to current values
	 * i.e. panels' semi-spans and dihedral angles
	 */
	private void calculateSpans() {
		Double bhalf = this.getPanels().stream()
				.mapToDouble(p ->
					p.getSemiSpan().to(SI.METRE).getEstimatedValue()
						*Math.cos(p.getDihedral().to(SI.RADIAN).getEstimatedValue())
				)
				.sum();
		this.semiSpan = Amount.valueOf(bhalf,SI.METRE);
		this.span = this.semiSpan.times(2.0);
	}

	/**
	 * Calculate Chord Distribution of the Actual Wing along y axis
	 */
	private void mapPanelsToYDiscretized() {

		List<Double> chordsActualVsYList = new ArrayList<Double>();
		List<Amount<Length>> _xLEvsY = new ArrayList<Amount<Length>>();
		List<Amount<Length>> _xTEvsY = new ArrayList<Amount<Length>>();

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

		List<Double> etaBP = _yBreakPoints.stream()
			.mapToDouble(y ->
				y.to(SI.METRE).getEstimatedValue()/this.semiSpan.to(SI.METRE).getEstimatedValue())
			.boxed()
			.collect(Collectors.toList())
			;
//		System.out.println(etaBP);

		//======================================================
		// Eta's discretizing the whole planform,
		// in the middle of each panel,
		// and including break-point eta's

		List<Double> eta0 =
			Arrays.asList(
				ArrayUtils.toObject(
						MyArrayUtils.linspace(0., 1., _numberOfPointsChordDistribution)
				)
			);
//		System.out.println(eta0);

		List<Double> eta1 = ListUtils.union(eta0, etaBP);
		Collections.sort(eta1);
//		System.out.println(eta1);

		List<Double> eta2 = eta1.stream()
			.distinct().collect(Collectors.toList());
//		System.out.println(eta2);

		_numberOfPointsChordDistribution = eta2.size();

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

/*		
		//======================================================
		// Map panels with lists of Y's
		// for each panel Y's of inner and outer break-points
		// are included, i.e. Y's are repeated

		// Innermost panel: Y's include 0 and panel's tip breakpoint Y
		_panelToYStations.put(
			panels.get(0),
			_yStationActual.stream()
				.filter(y -> y.isLessThan( panels.get(0).getSemiSpan() ) || y.equals( panels.get(0).getSemiSpan()) )
				.collect(Collectors.toList())
			);
		
		// All remaining panels (innermost panel excluded)
		// Y's include only panel's tip breakpoint Y, 
		// not including panel's root breakpoint Y
		for (int i=1; i < panels.size(); i++) {
			final int i_ = i;
			_panelToYStations.put(
				panels.get(i), // key
				_yStationActual.stream()
					.mapToDouble(a -> a.to(SI.METRE).getEstimatedValue())
					.filter(y -> ( y > _yBreakPoints.get(i_).getEstimatedValue() ) && ( y <= _yBreakPoints.get(i_+1).getEstimatedValue() ) )
					.mapToObj(y_ -> Amount.valueOf(y_, SI.METRE))
					.collect(Collectors.toList()
				) // value
			);
		}

		System.out.println("=====================================================");
		System.out.println("Map: panel(0) ->\n" + _panelToYStations.get(panels.get(0)));
		System.out.println("Map: panel(1) ->\n" + _panelToYStations.get(panels.get(1)));
		System.out.println("=====================================================");
		
*/
		
		// TODO experiment with Javaslang tuples
		
		//======================================================
		// Map panels with lists of Y's, c, Xle, Yle, Zle, twist
		// for each panel Y's of inner and outer break-points
		// are included, i.e. Y's are repeated
		
		Tuple2<
			List<LiftingSurfacePanel>,
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

		//======================================================
		// Set chords versus Y's
		// according to location within panels/yBP

		for (int k=0; k < _panelToSpanwiseDiscretizedVariables.size(); k++) {
			LiftingSurfacePanel panel = _panelToSpanwiseDiscretizedVariables.get(k)._1();
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

	private void calculateXZLEAtYDiscretized() {
		
		for (int k=0; k < _panelToSpanwiseDiscretizedVariables.size(); k++) {
			LiftingSurfacePanel panel = _panelToSpanwiseDiscretizedVariables.get(k)._1();
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

	private void reportPanesToSpanwiseDiscretizedVariables(){
		
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
		for (LiftingSurfacePanel panel : panels) {
			sb.append(panel.toString());
		}

		sb
			.append("\t=====================================\n")
			.append("\tDerived data\n")
			.append("\tSpan: " + this.getSpan().to(SI.METRE) +"\n")
			.append("\tSemi-span: " + this.getSemiSpan().to(SI.METRE) +"\n")
			.append("\tSurface of planform: " + this.getSurfacePlanform().to(SI.SQUARE_METRE) +"\n")
			.append("\tSurface wetted: " + this.getSurfaceWetted().to(SI.SQUARE_METRE) + "\n")
			.append("\tAspect-ratio: " + this.getAspectRatio() +"\n")
			.append("\tMean aerodynamic chord: " + this.getMeanAerodynamicChord() +"\n")
			;

		// TODO add more data in log message

		return sb.toString();
	}

}
