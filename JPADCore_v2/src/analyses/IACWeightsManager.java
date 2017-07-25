package analyses;

import java.io.IOException;
import java.util.Map;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.VolumetricDensity;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;

public interface IACWeightsManager {

	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException;
	public void calculateAllMasses(
			Aircraft aircraft, 
			Map <ComponentEnum, MethodEnum> methodsMap
			);
	
	public void plotWeightBreakdown(String weightsFolderPath);
	
	public Amount<Mass> getMaximumTakeOffMass();
	public void setMaximumTakeOffMass(Amount<Mass> _MTOM);

	public Amount<Mass> getMaximumZeroFuelMass();

	public Amount<Force> getMaximumTakeOffWeight();
	
	public Amount<Force> getMaximumZeroFuelWeight();

	public Amount<Mass> getPaxMass();
	
	public Amount<Mass> getCrewMass();

	public Amount<Mass> getEmptyMass();
	public void setEmptyMass(Amount<Mass> _emptyMass);

	public void setEmptyWeight(Amount<Force> _emptyWeight);

	public Amount<Mass> getStructuralMass();
	public void setStructuralMass(Amount<Mass> _structureMass);

	public Amount<Force> getStructuralWeight();
	
	public Amount<VolumetricDensity> getMaterialDensity();
	public void setMaterialDensity(Amount<VolumetricDensity> _materialDensity);

	public Amount<Force> getMaximumLandingWeight();
	public void setMaximumLandingWeight(Amount<Force> _MLW);

	public Amount<Mass> getMaximumLangingMass();
	public void setMaximumLandingMass(Amount<Mass> _MLM);

	public Amount<Mass> getOperatingItemMass();
	public void setOperatingItemMass(Amount<Mass> _OIM);

	public Amount<Force> getOperatingItemWeight();
	public void setOperatingItemWeight(Amount<Force> _operatingItemWeight);

	public Amount<Mass> getManufacturerEmptyMass();
	public void setManufacturerEmptyMass(Amount<Mass> _manufacturerEmptyMass);

	public Amount<Force> getManufacturerEmptyWeight();
	public void setManufacturerEmptyWeight(Amount<Force> _manufacturerEmptyWeight);

	public Amount<Mass> getOperatingEmptyMass();
	public void setOperatingEmptyMass(Amount<Mass> _OEM);

	public Amount<Force> getOperatingEmptyWeight();
	public void setOperatingEmptyWeight(Amount<Force> _operatingEmptyWeight);

	public Amount<Mass> getTrappedFuelOilMass();
	public void setTrappedFuelOilMass(Amount<Mass> _trappedFuelOilMass);

	public Amount<Force> getTrappedFuelOilWeight();
	public void setTrappedFuelOilWeight(Amount<Force> _trappedFuelOilWeight);

	public Amount<Mass> getZeroFuelMass();
	public void setZeroFuelMass(Amount<Mass> _ZFM);

	public Amount<Force> getZeroFuelWeight();
	public void setZeroFuelWeight(Amount<Force> _zeroFuelWeight);

	public Amount<Mass> getPaxMassMax();
	public void setMaximumZeroFuelMass(Amount<Mass> _MZFM);

	public Amount<Mass> getTakeOffMass();
	public void setTakeOffMass(Amount<Mass> _TOM);

	public Amount<Force> getTakeOffWeight();
	public void setTakeOffWeight(Amount<Force> _takeOffWeight);

	public Amount<Mass> getPaxSingleMass();
	public void setPaxSingleMass(Amount<Mass> _paxSingleMass);

	public String getId();
	public void setId(String id);

	public Aircraft getTheAircraft();
	public void setTheAircraft(Aircraft _theAircraft);

	public Amount<Length> getRange();
	public void setRange(Amount<Length> _range);
	
}
