package it.unina.daf.jpadcad;

import org.inferred.freebuilder.FreeBuilder;

import it.unina.daf.jpadcad.utils.AircraftUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftUtils.XSpacingType;

@FreeBuilder
public interface ICADManager {

	// All input from file

	// Fuselage input
	boolean getGenerateFuselage();

	double getNoseCapSectionFactor1();
	double getNoseCapSectionFactor2();
	int getNumberNoseCapSections();	
	int getNumberNoseTrunkSections();
	XSpacingType getSpacingTypeNoseTrunk();
	int getNumberTailTrunkSections();
	XSpacingType getSpacingTypeTailTrunk();	
	double getTailCapSectionFactor1();
	double getTailCapSectionFactor2();
	int getNumberTailCapSections();	
	boolean getExportFuselageSupportShapes();

	// Wing input
	boolean getGenerateWing();
	double getWingTipTolerance();
	boolean getExportWingSupportShapes();

	// Horizontal tail input
	boolean getGenerateHorizontal();
	double getHorizontalTipTolerance();
	boolean getExportHorizontalSupportShapes();

	// Vertical tail input
	boolean getGenerateVertical();
	double getVerticalTipTolerance();
	boolean getExportVerticalSupportShapes();

	// Canard input
	boolean getGenerateCanard();
	double getCanardTipTolerance();
	boolean getExportCanardSupportShapes();
	
	// Wing/Fuselage fairing input
	boolean getGenerateWingFairing();
	double getWingFairingFrontLengthFactor();
	double getWingFairingBackLengthFactor();
	double getWingFairingSideSizeFactor();
	double getWingFairingHeightFactor();
	double getWingFairingHeightBelowContactFactor();
	double getWingFairingHeightAboveContactFactor();
	double getWingFairingFilletRadiusFactor();
	
	// Canard/Fuselage fairing input
	boolean getGenerateCanardFairing();
	double getCanardFairingFrontLengthFactor();
	double getCanardFairingBackLengthFactor();
	double getCanardFairingSideSizeFactor();
	double getCanardFairingHeightFactor();
	double getCanardFairingHeightBelowContactFactor();
	double getCanardFairingHeightAboveContactFactor();
	double getCanardFairingFilletRadiusFactor();
	
	// Export to file
	boolean getExportToFile();
	FileExtension getFileExtension();

	class Builder extends ICADManager_Builder {
		public Builder() {
			
		}
	}
}
