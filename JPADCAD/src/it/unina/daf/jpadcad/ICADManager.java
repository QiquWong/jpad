package it.unina.daf.jpadcad;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import it.unina.daf.jpadcad.enums.EngineCADComponentsEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.enums.WingTipType;
import it.unina.daf.jpadcad.enums.XSpacingType;

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
	
	// ENGINES input
	boolean getGenerateEngines();
	List<Map<EngineCADComponentsEnum, String>> getEngineTemplatesList();
	
	List<Amount<Angle>> getPropellerBladePitchAngleList();
	
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
