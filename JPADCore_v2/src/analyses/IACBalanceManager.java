package analyses;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

public interface IACBalanceManager {

	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException;
	public void createBalanceCharts(String balanceOutputFolderPath);
	public void calculateBalance();
	public void calculateBalance(Map<ComponentEnum, List<MethodEnum>> methodsMap);
	public void calculateTotalCG();
	
	public Double getXCGMeanAtOEM();
	public void setXCGMeanAtOEM(Double _xCGMeanAtOEM);

	public Double getXCGMaxAftAtOEM();
	public void setXCGMaxAftAtOEM(Double _xCGMaxAftAtOEM);

	public Double getXCGMaxForAtOEM();
	public void setXCGMaxForAtOEM(Double _xCGMaxForAtOEM);

	public List<CenterOfGravity> getCGList();
	public void setCGList(List<CenterOfGravity> _cgList);

	public CenterOfGravity getCGStructure();
	public void setCGStructure(CenterOfGravity _cgStructure);

	public CenterOfGravity getCGStructureAndPower();
	public void setCGStructureAndPower(CenterOfGravity _cgStructureAndPower);

	public CenterOfGravity getCGMZFM();
	public void setCGMZFM(CenterOfGravity _cgMZFM);

	public CenterOfGravity getCGMTOM();
	public void setCGMTOM(CenterOfGravity _cgMTOM);

	public String getId();
	public void setId(String id);

	public Amount<Mass> getMaximumTakeOffMass();
	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass);

	public Amount<Mass> getMaximumZeroFuelMass();
	public void setMaximumZeroFuelMass(Amount<Mass> _maximumZeroFuelMass);

	public Amount<Mass> getOperatingEmptyMass();
	public void setOperatingEmptyMass(Amount<Mass> _operatingEmptyMass);

	public Amount<Mass> getPassengersTotalMass();
	public void setPassengersTotalMass(Amount<Mass> _passengerTotalMass);

	public Amount<Mass> getFuselageMass();
	public void setFuselageMass(Amount<Mass> _fuselageMass);

	public Amount<Mass> getWingMass();
	public void setWingMass(Amount<Mass> _wingMass);

	public Amount<Mass> getHorizontalTailMass();
	public void setHorizontalTailMass(Amount<Mass> _horizontalTailMass);

	public Amount<Mass> getVerticalTailMass();
	public void setVerticalTailMass(Amount<Mass> _verticalTailMass);

	public Amount<Mass> getCanardMass();
	public void setCanardMass(Amount<Mass> _canardMass);

	public List<Amount<Mass>> getNacellesMassList();
	public void setNacellesMassList(List<Amount<Mass>> _nacellesMassList);

	public List<Amount<Mass>> getEnginesMassList();
	public void setEnginesMassList(List<Amount<Mass>> _enginesMassList);

	public Amount<Mass> getLandingGearsMass();
	public void setLandingGearsMass(Amount<Mass> _landingGearsMass);
	
}
