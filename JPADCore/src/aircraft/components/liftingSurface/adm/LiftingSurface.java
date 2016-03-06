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

		_yStationActual = new MyArray(SI.METER);
		_chordsVsYActual = new MyArray(SI.METER);
		_xLEvsYActual = new MyArray(SI.METER);
		_xTEvsYActual = new MyArray(SI.METER);
		
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

		// Semispan, span
		Double bhalf = this.getPanels().stream()
				.mapToDouble(p -> p.getSemiSpan().to(SI.METRE).getEstimatedValue())
				.sum();
		this.semiSpan = Amount.valueOf(bhalf,SI.METRE);
		this.span = this.semiSpan.times(2.0);

		// Assign variables along span
		calculateChordYAxisActual();
		
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
	 * Calculate Chord Distribution of the Actual Wing along y axis
	 */
	private void calculateChordYAxisActual() {

		List<Double> chordsActualVsYList = new ArrayList<Double>();
		List<Amount<Length>> _xLEvsY = new ArrayList<Amount<Length>>();
		List<Amount<Length>> _xTEvsY = new ArrayList<Amount<Length>>();

		//======================================================
		// Break points Y's
		List<Amount<Length>> yBP = new ArrayList<>();
		// root at symmetry plane
		yBP.add(Amount.valueOf(0.0, SI.METRE));
		// Accumulate values and add
		yBP.addAll(
			IntStream.range(1, this.panels.size())
				.mapToObj(i -> yBP.get(i-1).plus( panels.get(i-1).getSemiSpan()) )
				.collect(Collectors.toList())
			);
		yBP.add(this.semiSpan);
		// TODO make this yBP a member variable _yBreakPoints
		
		MyConfiguration.customizeAmountOutput();
//		System.out.println("yBP:" + yBP);
		
		List<Double> etaBP = yBP.stream()
			.mapToDouble(y -> y.to(SI.METRE).getEstimatedValue()/this.semiSpan.to(SI.METRE).getEstimatedValue())
			.boxed()
			.collect(Collectors.toList())
			;
//		System.out.println(etaBP);
		
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
		
		_yStationActual.setRealVector(
				_eta.getRealVector().mapMultiply(this.semiSpan.doubleValue(SI.METER)));
		
		System.out.println("____________________");
		
		//======================================================
		// Set chords versus Y's 
		// according to location within panels/yBP
		
		Map<LiftingSurfacePanel, List<Amount<Length>>> panelToYStations = 
			    new HashMap<LiftingSurfacePanel, List<Amount<Length>>>();
		
		panelToYStations.put(
			panels.get(0),
			_yStationActual.getList().stream()
				.filter(y -> y < panels.get(0).getSemiSpan().getEstimatedValue())
				.mapToDouble(y -> y)
				.mapToObj(y -> Amount.valueOf(y, SI.METRE))
				.collect(Collectors.toList())
			);
		
		for (int i=0; i < panels.size(); i++) {
			final int i_ = i;
			panelToYStations.put(
				panels.get(i), // key 
				_yStationActual.getList().stream()
					.filter(y -> ( y >= yBP.get(i_).getEstimatedValue() ) && ( y <= yBP.get(i_+1).getEstimatedValue() ) )
					.mapToDouble(y_ -> y_)
					.mapToObj(y__ -> Amount.valueOf(y__, SI.METRE))
					.collect(Collectors.toList()
				) // value
			);
		}
		
		System.out.println("Map: panel(0) ->\n" + panelToYStations.get(panels.get(0)));
		System.out.println("Map: panel(1) ->\n" + panelToYStations.get(panels.get(1)));
		
//		chordsActualVsYList.addAll(
//			IntStream.range(1, yBP.size())
//				.mapToDouble(i -> {
//					IntStream.range(0, _yStationActual.size())
//						.mapToObj(j -> panels.get(index))
//						.filter(
//							p -> p.get
//								)
//					if ( ) { 
//						
//					}
//				})
//			};
		
		
//		for (int i=0; i < _numberOfPointsChordDistribution; i++) {
//
//			if(_eta.get(i) <= _spanStationKink){
//				_xLEvsY.add(Amount.valueOf(
//						_xLERoot.doubleValue(SI.METER) +
//						Math.tan(_sweepLEInnerPanel.doubleValue(SI.RADIAN)) * 
//						_yStationActual.get(i)
//						,SI.METER));
//
//				_xTEvsY.add(Amount.valueOf((_xLERoot.doubleValue(SI.METER)+
//						_chordRoot.doubleValue(SI.METER))+
//						Math.tan(_sweepTEInnerPanel.doubleValue(SI.RADIAN)) * 
//						_yStationActual.get(i),SI.METER));
//
//			} else if(_spanStationKink !=1.) { // Handle simply tapered wing
//				_xLEvsY.add(Amount.valueOf(_xLEKink.doubleValue(SI.METER) +
//						Math.tan(_sweepLEOuterPanel.doubleValue(SI.RADIAN)) * 
//						(_yStationActual.get(i)-
//								_semiSpanInnerPanel.doubleValue(SI.METER)),SI.METER));
//
//				_xTEvsY.add(Amount.valueOf(_xTEKink.doubleValue(SI.METER) +
//						Math.tan(_sweepTEOuterPanel.doubleValue(SI.RADIAN)) * 
//						(_yStationActual.get(i) -
//								_semiSpanInnerPanel.doubleValue(SI.METER)),SI.METER));
//
//			}
//
//			chordsActualVsYList.add(_xTEvsY.get(_xTEvsY.size()-1).doubleValue(SI.METER)-
//					_xLEvsY.get(_xLEvsY.size()-1).doubleValue(SI.METER));
//
//		}
//
//		_chordsVsYActual.setList(chordsActualVsYList);
//		_xLEvsYActual.setAmountList(_xLEvsY);
//		_xTEvsYActual.setAmountList(_xTEvsY);

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
