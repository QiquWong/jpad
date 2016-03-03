package aircraft.components.liftingSurface.adm;

import java.io.File;
import java.util.ArrayList;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.liftingSurface.adm.LiftingSurfacePanel.LiftingSurfacePanelBuilder;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class LiftingSurface extends AbstractLiftingSurface {

	public LiftingSurface(String id) {
		this.id = id;
		panels = new ArrayList<LiftingSurfacePanel>();
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
		// TODO Auto-generated method stub

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
	public Amount<Length> getMeanAerodChord() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length>[] getMeanAerodChordLeadingEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodChordLeadingEdgeX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodChordLeadingEdgeY() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getMeanAerodChordLeadingEdgeZ() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Area> getSurfacePlanform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Area> getSurfaceWetted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getAspectRatio() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getTaperRatio() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LiftingSurface getEquivalentWing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
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
		// TODO add more data in log message

		return sb.toString();
	}

}
