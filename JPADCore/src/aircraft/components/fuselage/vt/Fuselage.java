package aircraft.components.fuselage.vt;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.adm.Airfoil;
import aircraft.components.liftingSurface.adm.LiftingSurfacePanel;
import aircraft.components.liftingSurface.adm.Airfoil.AirfoilBuilder;
import aircraft.components.liftingSurface.adm.LiftingSurfacePanel.LiftingSurfacePanelBuilder;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilTypeEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class Fuselage implements IFuselage {

	private String id;
	private int deckNumber;
	private Amount<Length> length;
	private Amount<Mass> massReference;
			
	public Fuselage(String id) {
		
		this.id = id;
	}

	@Override
	public void calculateGeometry(int numberSpanwiseStations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void calculateGeometry() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Amount<Length>> getXYZ0() {
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
	public List<Amount<Length>> getXYZPole() {
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
	public int getDeckNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDeckNumber(int dn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Amount<Length> getLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLength(Amount<Length> len) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Amount<Mass> getMassReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMassReference(Amount<Mass> massRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void discretizeGeometry(int numberSpanwiseStations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Amount<Area> getSurfaceWetted(boolean recalculate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Area> getSurfaceWetted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Amount<Length>> getDiscretizedYs() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Fuselage importFromXML(String pathToXML) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading fuselage data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage/@id");

		Amount<Length> len = reader.getXMLAmountLengthByPath("//global_data/length");
		
		// create the wing panel via its builder
		Fuselage fuselage = new FuselageBuilder(id)
				.length(len)
				.build();
		
		return fuselage;
	}
	
	// Builder pattern via a nested public static class
	public static class FuselageBuilder {
		// required parameters
		private String _id;
		
		// optional parameters ... defaults
		// ...
		private int _deckNumber = 1;
		private Amount<Length> _length = Amount.valueOf(20, SI.METER);
		private Amount<Mass> _massReference = Amount.valueOf(3300, SI.KILOGRAM);

		public FuselageBuilder(String id){
			
			this._id = id;
		}
		
		public FuselageBuilder length(Amount<Length> len) {
			_length = len;
			return this;
		}
		
		public Fuselage build() {
			return new Fuselage(this);
		}
	}
	
	private Fuselage(FuselageBuilder builder) {
		id = builder._id;
		
		deckNumber = builder._deckNumber;
		length = builder._length;
		massReference = builder._massReference;
		
		calculateGeometry();
	}
	
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tFuselage\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + id + "'\n")
				.append("\tNumber of decks: " + deckNumber + "\n")
				.append("\tLength: " + length + "\n")
				.append("\tMass reference: " + massReference + "\n")
				;
		return sb.toString();
	}
}
