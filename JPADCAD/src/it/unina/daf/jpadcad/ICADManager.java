package it.unina.daf.jpadcad;

import org.inferred.freebuilder.FreeBuilder;

import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.XSpacingType;

@FreeBuilder
public interface ICADManager {

	// All input from file

	// FUSELAGE input
	boolean getGenerateFuselage();
	XSpacingType getSpacingTypeNoseTrunk();
	int getNumberNoseTrunkSections();
	XSpacingType getSpacingTypeTailTrunk();	
	int getNumberTailTrunkSections();

	// WING input
	boolean getGenerateWing();
	WingTipType getWingTipType();	
	
	double getWingletYOffsetFactor();
	double getWingletXOffsetFactor();
	double getWingletTaperRatio();

	// HTAIL input
	boolean getGenerateHTail();
	WingTipType getHTailTipType();

	// VTAIL input
	boolean getGenerateVTail();
	WingTipType getVTailTipType();

	// CANARD input
	boolean getGenerateCanard();
	WingTipType getCanardTipType();
	
	// WING-FUSELAGE FAIRING input
	boolean getGenerateWingFairing();
	double getWingFairingFrontLengthFactor();
	double getWingFairingBackLengthFactor();
	double getWingFairingWidthFactor();
	double getWingFairingHeightFactor();
	double getWingFairingHeightBelowReferenceFactor();
	double getWingFairingHeightAboveReferenceFactor();
	double getWingFairingFilletRadiusFactor();
	
	// CANARD-FUSELAGE FAIRING input
	boolean getGenerateCanardFairing();
	double getCanardFairingFrontLengthFactor();
	double getCanardFairingBackLengthFactor();
	double getCanardFairingWidthFactor();
	double getCanardFairingHeightFactor();
	double getCanardFairingHeightBelowReferenceFactor();
	double getCanardFairingHeightAboveReferenceFactor();
	double getCanardFairingFilletRadiusFactor();
	
	// Export to file
	boolean getExportToFile();
	
	FileExtension getFileExtension();
	boolean getExportWireframe();

	// Class builder
	class Builder extends ICADManager_Builder {
		public Builder() {
			
		}
	}
}
