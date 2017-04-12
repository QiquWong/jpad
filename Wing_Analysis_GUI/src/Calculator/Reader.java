package Calculator;

import java.io.IOException;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;

import GUI.Views.VariablesInputData;
import configuration.enumerations.AirfoilFamilyEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class Reader {
	
	
	public void readInputFromXML(VariablesInputData theVariables, String pathToXML) throws IOException{
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		Amount<Length> altitude = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//altitude");
		Unit unitOfMeasurement = altitude.getUnit();
		theVariables.getAltitude().setText(Double.toString(altitude.doubleValue(unitOfMeasurement)));
		theVariables.getAltitudeUnits().setValue(unitOfMeasurement.toString());
		
		double machNumber =  Double.parseDouble(reader.getXMLPropertiesByPath("//mach_number").get(0));
		theVariables.getMachNumber().setText(Double.toString(machNumber));
		
		Amount<Angle> alphaInitial = (Amount<Angle>) reader.getXMLAmountWithUnitByPath("//alpha_initial");
		Unit unitOfMeasurementAlphaInitial = alphaInitial.getUnit();
		theVariables.getAlphaInitial().setText(Double.toString(alphaInitial.doubleValue(unitOfMeasurementAlphaInitial)));
		if(unitOfMeasurementAlphaInitial.toString() == "°")
		theVariables.getAlphaInitialUnits().setValue("deg");
		else
			theVariables.getAlphaInitialUnits().setValue(unitOfMeasurementAlphaInitial.toString());	

		
		Amount<Angle> alphaFinal = (Amount<Angle>) reader.getXMLAmountWithUnitByPath("//alpha_final");
		Unit unitOfMeasurementAlphaFinal = alphaFinal.getUnit();
		theVariables.getAlphaFinal().setText(Double.toString(alphaFinal.doubleValue(unitOfMeasurementAlphaFinal)));
		if(unitOfMeasurementAlphaFinal.toString() == "°")
			theVariables.getAlphaFinalUnits().setValue("deg");
		else
		theVariables.getAlphaFinalUnits().setValue(unitOfMeasurementAlphaFinal.toString());
		
		int numberOfAlphas =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_alpha").get(0));
		theVariables.getNumberOfAlphas().setText(Double.toString(numberOfAlphas));
		
		Amount<Area> surface = (Amount<Area>) reader.getXMLAmountWithUnitByPath("//surface");
		Unit unitOfMeasurementSurface = surface.getUnit();
		theVariables.getSurface().setText(Double.toString( surface.doubleValue(unitOfMeasurementSurface)));
		theVariables.getSurfaceUnits().setValue(unitOfMeasurementSurface.toString());

		double aspectRatio =  Double.parseDouble(reader.getXMLPropertiesByPath("//aspect_ratio").get(0));
		theVariables.getAspectRatio().setText(Double.toString(aspectRatio));
		
		int numberOfPointsSemiSpan =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_point_semispan").get(0));
		theVariables.getNumberOfPoints().setText(Double.toString(numberOfPointsSemiSpan));
		
		double adimensionalKinkStation =  Double.parseDouble(reader.getXMLPropertiesByPath("//adimensional_kink_station").get(0));
		theVariables.getAdimensionalKinkStation().setText(Double.toString(adimensionalKinkStation));
		
		double thickenssMeanAirfoil =  Double.parseDouble(reader.getXMLPropertiesByPath("//max_thickness_mean_airfoil").get(0));
		theVariables.getMaxThickness().setText(Double.toString(thickenssMeanAirfoil));
		
		List<String> airfoilFamilyProperty = reader.getXMLPropertiesByPath("//airfoil_family");
			theVariables.getAirfoilFamily().setValue(airfoilFamilyProperty.get(0));
		
	
			int numberOfSection =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_given_sections").get(0));
			String intNumberOfSection = String.valueOf(numberOfSection);
			theVariables.getNumberOfGivenSections().setValue(intNumberOfSection );
			
		theVariables.setNumberOfGivenSection();	
		
		
		// DISTRIBUTIONS
		
		List<String> stationsDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//y_adimensional_stations").get(0));
		for(int i=0; i<stationsDistribution.size(); i++)
			theVariables.getStationList().get(i).setText((stationsDistribution.get(i)));
		
		Unit unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//chord_distribution" + "/@unit"));
		List<String> chordDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//chord_distribution").get(0));
		for(int i=0; i<chordDistribution.size(); i++)
			theVariables.getChordList().get(i).setText((chordDistribution.get(i)));
		theVariables.getChordsUnits().setValue(unitOfMeas);
		
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//x_le_distribution" + "/@unit"));
		List<String> xleDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//x_le_distribution").get(0));
		for(int i=0; i<xleDistribution.size(); i++)
			theVariables.getXleList().get(i).setText((xleDistribution.get(i)));
		theVariables.getXleUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//twist_distribution" + "/@unit"));
		List<String> twistDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//twist_distribution").get(0));
		for(int i=0; i<twistDistribution.size(); i++)
			theVariables.getTwistList().get(i).setText((twistDistribution.get(i)));
		theVariables.getTwistUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//alpha_zero_lift_distribution" + "/@unit"));
		List<String> alphaZeroLiftDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_zero_lift_distribution").get(0));
		for(int i=0; i<alphaZeroLiftDistribution.size(); i++)
			theVariables.getAlphaZeroList().get(i).setText((alphaZeroLiftDistribution.get(i)));
		theVariables.getAlphaZeroLiftUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//alpha_star_distribution" + "/@unit"));
		List<String> alphaStarDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_star_distribution").get(0));
		for(int i=0; i<alphaStarDistribution.size(); i++)
			theVariables.getAlphaStarList().get(i).setText((alphaStarDistribution.get(i)));
		theVariables.getAlphaStarUnits().setValue(unitOfMeas);
		
		
		List<String> clMaxDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//maximum_lift_coefficient_distribution").get(0));
		for(int i=0; i<clMaxDistribution.size(); i++)
			theVariables.getClMaxList().get(i).setText((clMaxDistribution.get(i)));
	
	
		
	
	
	}
	

}
