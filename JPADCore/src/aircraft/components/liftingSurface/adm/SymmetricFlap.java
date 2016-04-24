package aircraft.components.liftingSurface.adm;

import java.io.File;
import java.util.ArrayList;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import aircraft.components.liftingSurface.adm.LiftingSurfacePanel.LiftingSurfacePanelBuilder;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.MyXMLReaderUtils;

public class SymmetricFlap implements ISymmetricFlap {
	
	private FlapTypeEnum _flapType;
	private Double _innerStationSpanwiseAdimensional, _outerStationSpanwiseAdimensional;
	private Double _chordFlapOverChordInner, _chordFlapOverChordOuter; 
	
	private Double[] _deltasFlap;

//  NOTE: treat these variables in a calculator class
//	// to be evaluated
//	private double _deltaCl0 = 0,
//			_deltaCL0 = 0,
//			_deltaClmax = 0,
//			_deltaCLmax = 0,
//			_cLalphaWithFlap = 0,
//			_deltaAlphaMax = 0,
//			_deltaCD = 0,
//			_deltaCM_c4 = 0;	

	
	public static SymmetricFlap importFromSymmetricFlapNode(Node nodePanel, String airfoilsDir) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(nodePanel, true);
			doc.appendChild(importedNode);
			return SymmetricFlap.importFromSymmetricFlapNodeImpl(doc, airfoilsDir);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static SymmetricFlap importFromSymmetricFlapNodeImpl(Document doc, String airfoilsDir) {

		System.out.println("Reading symmetric flap data from XML node ...");

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//symmetric_flap/@id");
		
		// TODO: implement the rest of the class, e.g. constructors, etc
		
		// type
		String type = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//symmetric_flap/@type");

//		if(type.equals("SINGLE_SLOTTED"))
//			// FlapTypeEnum.SINGLE_SLOTTED;
//		else if(type.equals("DOUBLE_SLOTTED"))
//			// FlapTypeEnum.DOUBLE_SLOTTED;
//		else if(type.equals("PLAIN"))
//			// FlapTypeEnum.PLAIN;
//		else if(type.equals("FOWLER"))
//			// FlapTypeEnum.FOWLER;
//		else if(type.equals("TRIPLE_SLOTTED"))
//			// FlapTypeEnum.TRIPLE_SLOTTED;
//		else {
//			// System.err.println("NO VALID FLAP TYPE!!");
//			// return;
//		}
		

		// create the wing panel via its builder
//		SymmetricFlap flap =
//			new SymmetricFlap(
//				id,
//				chordRoot, chordTip,
//				airfoilRoot, airfoilTip,
//				twistGeometricTip,
//				semiSpan, sweepLeadingEdge, dihedral
//				)
//			.build();
//		return flap;
		return null;
	}
	
}
