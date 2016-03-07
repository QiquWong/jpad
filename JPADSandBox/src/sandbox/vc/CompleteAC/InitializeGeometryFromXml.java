package sandbox.vc.CompleteAC;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.adm.LiftingSurface;
import configuration.enumerations.DirStabEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.database.io.DatabaseIOmanager;

public class InitializeGeometryFromXml {
	
//	public static void fuselageDataFromXml() {
//
//		DatabaseIOmanager<DirStabEnum> ioManager = new DatabaseIOmanager<DirStabEnum>();
//
//		ioManager.addElement(DirStabEnum.Mach_number, Amount.valueOf(0., Unit.ONE), "Mach number.");
//	}
		
		public static Fuselage importFromXML(String pathToXML) {

			JPADXmlReader reader = new JPADXmlReader(pathToXML);

			System.out.println("Reading lifting surface data ...");

			String id = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage/@id");

//			Fuselage fuselage = new Fuselage(id);
			Fuselage fuselage = new Fuselage();
			
			return fuselage;
	}

}
