package database.databasefunctions.aerodynamics.vedsc;

import java.io.File;

import database.databasefunctions.DatabaseReader;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import writers.JPADStaticWriteUtils;

public class VeDSCDatabaseReader extends DatabaseReader{

	private MyInterpolatingFunction kFv_vs_bv_over_dfv, kVf_vs_zw_over_dfv, kWv_vs_zw_over_rf, 
	kWf_vs_zw_over_rf, kHv_vs_zh_over_bv1_high_wing, kHv_vs_zh_over_bv1_mid_wing, 
	kHv_vs_zh_over_bv1_low_wing, kHf_vs_zh_over_bv1_high_wing, kHf_vs_zh_over_bv1_mid_wing, 
	kHf_vs_zh_over_bv1_low_wing;
	
	private double kFv, kVf, kWv, kWf, kHv, kHf;


	public VeDSCDatabaseReader(String databaseFolderPath, String databaseFileName) {
		
		super(databaseFolderPath, databaseFileName);
		
		kFv_vs_bv_over_dfv = database.interpolate2DFromDatasetFunction("KFv_vs_bv_over_dfv");
		kVf_vs_zw_over_dfv = database.interpolate2DFromDatasetFunction("KVf_vs_bv_over_dfv");
		kWv_vs_zw_over_rf = database.interpolate3DFromDatasetFunction("KWv_vs_zw_over_rf");
		kWf_vs_zw_over_rf = database.interpolate3DFromDatasetFunction("KWf_vs_zw_over_rf");
		kHv_vs_zh_over_bv1_high_wing = database.interpolate3DFromDatasetFunction("KHv_vs_zh_over_bv1_high_wing");
		kHv_vs_zh_over_bv1_mid_wing = database.interpolate3DFromDatasetFunction("KHv_vs_zh_over_bv1_mid_wing");
		kHv_vs_zh_over_bv1_low_wing = database.interpolate3DFromDatasetFunction("KHv_vs_zh_over_bv1_low_wing");
		kHf_vs_zh_over_bv1_high_wing = database.interpolate3DFromDatasetFunction("KHf_vs_zh_over_bv1_high_wing");
		kHf_vs_zh_over_bv1_mid_wing = database.interpolate3DFromDatasetFunction("KHf_vs_zh_over_bv1_mid_wing");
		kHf_vs_zh_over_bv1_low_wing = database.interpolate3DFromDatasetFunction("KHf_vs_zh_over_bv1_low_wing");
		
	}
	
	/**
	 * 
	 * @param wingAr
	 * @param wingPosition
	 * @param verticalTailAr
	 * @param verticalTailSpan (m)
	 * @param horizontalTailPositionOverVertical
	 * @param fuselageDiameterAtVerticalMAC
	 * @param tailconeShape
	 * @return 
	 */
	public void runAnalysis(
			double wingAr, double wingPosition, double verticalTailAr, 
			double verticalTailSpan, double horizontalTailPositionOverVertical, 
			double fuselageDiameterAtVerticalMAC, double tailconeShape) {

		kFv = get_KFv_vs_bv_over_dfv(verticalTailSpan, fuselageDiameterAtVerticalMAC, tailconeShape);
		kVf = get_KVf_vs_zw_over_dfv(verticalTailSpan, fuselageDiameterAtVerticalMAC, tailconeShape);
		kWv = get_KWv_vs_zw_over_rf(wingPosition, wingAr, tailconeShape);
		kWf = get_KWf_vs_zw_over_rf(wingPosition, wingAr, tailconeShape);
		kHv = get_KHv_vs_zh_over_bv1(horizontalTailPositionOverVertical, verticalTailAr, tailconeShape, wingPosition);
		kHf = get_KHf_vs_zh_over_bv1(horizontalTailPositionOverVertical, verticalTailAr, tailconeShape, wingPosition);
	}


	/**
	 * This function returns the aerodynamic interference factor of the fuselage on the vertical tail.
	 * @param verticalSpan is the span of the vertical tail computed from root trailing edge to tip trailing edge.
	 * @param fuselageDiameterAtVerticalMAC is the height of the fuselage at the vertical tail 25% mac station.
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static stability derivative.
	 */
	public double get_KFv_vs_bv_over_dfv(double verticalSpan, double fuselageDiameterAtVerticalMAC, double tailconeShape) {
		if (fuselageDiameterAtVerticalMAC == 0.) return 0.;
		return kFv_vs_bv_over_dfv.value(verticalSpan/fuselageDiameterAtVerticalMAC, tailconeShape);
	}

	/**
	 * This function returns the aerodynamic interference factor of the vertical tail on the fuselage.
	 * @param verticalSpan is the span of the vertical tail computed from root trailing edge to tip trailing edge.
	 * @param fuselageDiameterAtVerticalMAC is the height of the fuselage at the vertical tail 25% mac station.
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static instability derivative.
	 */
	public double get_KVf_vs_zw_over_dfv(double verticalSpan, double fuselageDiameterAtVerticalMAC, double tailconeShape) {

		if (fuselageDiameterAtVerticalMAC == 0.) return 0.;
		return kVf_vs_zw_over_dfv.value(verticalSpan/fuselageDiameterAtVerticalMAC, tailconeShape);
	}

	/**
	 * This function returns the aerodynamic interference factor of the wing on the vertical.
	 * @param wingPosition is the wing vertical position on the fuselage. It varies between [-1, 1]. Low wing = -1, mid wing = 0, high wing = 1.
	 * @param arWing is the wing aspect ratio. Varies between [6, 14]
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static stability derivative.
	 */
	public double get_KWv_vs_zw_over_rf(double wingPosition, double arWing, double tailconeShape) {
		return kWv_vs_zw_over_rf.value(wingPosition, arWing, tailconeShape);
	}

	/**
	 * This function returns the aerodynamic interference factor of the wing on the fuselage.
	 * @param wingPosition is the wing vertical position on the fuselage. It varies between [-1, 1]. Low wing = -1, mid wing = 0, high wing = 1.
	 * @param arWing is the wing aspect ratio. Varies between [6, 14]
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static instability derivative.
	 */
	public double get_KWf_vs_zw_over_rf(double wingPosition, double arWing, double tailconeShape) {
		return kWf_vs_zw_over_rf.value(wingPosition, arWing, tailconeShape);
	}


	/**
	 * This function returns the aerodynamic interference factor of the horizontal tail on the vertical tail for a high wing configuration.
	 * @param horizPosOverVertical is the relative position of the horizontal tail over the vertical tail span, computed from a reference line (see database figure). Varies between [0, 1].
	 * @param arVertical is the vertical tail aspect ratio. Varies between [1, 2].
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static stability derivative.
	 */
	public double get_KHv_vs_zh_over_bv1_high_wing(double horizPosOverVertical, double arVertical, double tailconeShape) {
		return kHv_vs_zh_over_bv1_high_wing.value(horizPosOverVertical, arVertical, tailconeShape);
	}

	/**
	 * This function returns the aerodynamic interference factor of the horizontal tail on the vertical tail for a mid wing configuration.
	 * @param horizPosOverVertical is the relative position of the horizontal tail over the vertical tail span, computed from a reference line (see database figure). Varies between [0, 1]. Body mounted position is 0.
	 * @param arVertical is the vertical tail aspect ratio. Varies between [1, 2].
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static stability derivative.
	 */
	public double get_KHv_vs_zh_over_bv1_mid_wing(double horizPosOverVertical, double arVertical, double tailconeShape) {
		return kHv_vs_zh_over_bv1_mid_wing.value(horizPosOverVertical, arVertical, tailconeShape);
	}

	/**
	 * This function returns the aerodynamic interference factor of the horizontal tail on the vertical tail for a low wing configuration.
	 * @param horizPosOverVertical is the relative position of the horizontal tail over the vertical tail span, computed from a reference line (see database figure). Varies between [0, 1]. Body mounted position is 0.
	 * @param arVertical is the vertical tail aspect ratio. Varies between [1, 2].
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static stability derivative.
	 */
	public double get_KHv_vs_zh_over_bv1_low_wing(double horizPosOverVertical, double arVertical, double tailconeShape) {
		return kHv_vs_zh_over_bv1_low_wing.value(horizPosOverVertical, arVertical, tailconeShape);
	}

	/**
	 * This function returns the aerodynamic interference factor of the horizontal tail on the fuselage for a high wing configuration.
	 * @param horizPosOverVertical is the relative position of the horizontal tail over the vertical tail span, computed from a reference line (see database figure). Varies between [0, 1].
	 * @param arVertical is the vertical tail aspect ratio. Varies between [1, 2].
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static instability derivative.
	 */
	public double get_KHf_vs_zh_over_bv1_high_wing(double horizPosOverVertical, double arVertical, double tailconeShape) {
		return kHf_vs_zh_over_bv1_high_wing.value(horizPosOverVertical, arVertical, tailconeShape);
	}

	/**
	 * This function returns the aerodynamic interference factor of the horizontal tail on the fuselage for a mid wing configuration.
	 * @param horizPosOverVertical is the relative position of the horizontal tail over the vertical tail span, computed from a reference line (see database figure). Varies between [0, 1].
	 * @param arVertical is the vertical tail aspect ratio. Varies between [1, 2].
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static instability derivative.
	 */
	public double get_KHf_vs_zh_over_bv1_mid_wing(double horizPosOverVertical, double arVertical, double tailconeShape) {
		return kHf_vs_zh_over_bv1_mid_wing.value(horizPosOverVertical, arVertical, tailconeShape);
	}

	/**
	 * This function returns the aerodynamic interference factor of the horizontal tail on the fuselage for a low wing configuration.
	 * @param horizPosOverVertical is the relative position of the horizontal tail over the vertical tail span, computed from a reference line (see database figure). Varies between [0, 1].
	 * @param arVertical is the vertical tail aspect ratio. Varies between [1, 2].
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static instability derivative.
	 */
	public double get_KHf_vs_zh_over_bv1_low_wing(double horizPosOverVertical, double arVertical, double tailconeShape) {
		return kHf_vs_zh_over_bv1_low_wing.value(horizPosOverVertical, arVertical, tailconeShape);
	}

	/**
	 * This function interpolate between values of KHv if the wing is in intermediate position between low and mid or mid and high.
	 * @param horizPosOverVertical is the relative position of the horizontal tail over the vertical tail span, computed from a reference line (see database figure). Varies between [0, 1].
	 * @param arVertical is the vertical tail aspect ratio. Varies between [1, 2].
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @param wingPosition is the wing vertical position on the fuselage. It varies between [-1, 1]. Low wing = -1, mid wing = 0, high wing = 1.
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static stability derivative.
	 */
	public double get_KHv_vs_zh_over_bv1(double horizPosOverVertical, double arVertical, double tailconeShape, double wingPosition) {

		double x1, x2, x  = wingPosition, y1, y2, y = 0.0;
		if(horizPosOverVertical < 0.0)
			horizPosOverVertical = 0.0;

		if (wingPosition <= -1.) { 
			y = get_KHv_vs_zh_over_bv1_low_wing(horizPosOverVertical, arVertical, tailconeShape);

		} else if ((wingPosition > -1.) && (wingPosition < 0.)) {
			// Linear interpolation
			x1 = -1.;			// Low wing
			x2 = 0.;				// Mid wing

			y1 = get_KHv_vs_zh_over_bv1_low_wing(horizPosOverVertical, arVertical, tailconeShape);
			y2 = get_KHv_vs_zh_over_bv1_mid_wing(horizPosOverVertical, arVertical, tailconeShape);
			y = MyMathUtils.interpolateLinear(x1, y1, x2, y2, x);

		} else if (wingPosition == 0.) {
			y = get_KHv_vs_zh_over_bv1_mid_wing(horizPosOverVertical, arVertical, tailconeShape);

		} else if ((wingPosition > 0.) && (wingPosition < 1.)) {
			// Linear interpolation
			x1 = 0.;				// Mid wing
			x2 = 1.;				// High wing

			y1 = get_KHv_vs_zh_over_bv1_mid_wing(horizPosOverVertical, arVertical, tailconeShape);
			y2 = get_KHv_vs_zh_over_bv1_high_wing(horizPosOverVertical, arVertical, tailconeShape);
			y = MyMathUtils.interpolateLinear(x1, y1, x2, y2, x);

		} else {
			y = get_KHv_vs_zh_over_bv1_high_wing(horizPosOverVertical, arVertical, tailconeShape);
		} 

		return y;
	}

	/**
	 * This function interpolate between values of KHv if the wing is in intermediate position between low and mid or mid and high.
	 * @param horizPosOverVertical is the relative position of the horizontal tail over the vertical tail span, computed from a reference line (see database figure). Varies between [0, 1].
	 * @param arVertical is the vertical tail aspect ratio. Varies between [1, 2].
	 * @param tailconeShape is a parameter that accounts for the fuselage tailcone shape. It varies between [0, 1].
	 * @param wingPosition is the wing vertical position on the fuselage. It varies between [-1, 1]. Low wing = -1, mid wing = 0, high wing = 1.
	 * @return a number that if bigger than 1 means an increase aerodynamic interference effect, i.e. increases the component contribution to sideslip static instability derivative.
	 */
	public double get_KHf_vs_zh_over_bv1(double horizPosOverVertical, double arVertical, double tailconeShape, double wingPosition) {

		double x1, x2, x, y1, y2, y = 0.0;

		if (wingPosition <= -1.) { 
			y = get_KHf_vs_zh_over_bv1_low_wing(horizPosOverVertical, arVertical, tailconeShape);

		} else if ((wingPosition > -1.) && (wingPosition < 0.)) {
			// Linear interpolation
			x1 = -1.;			// Low wing
			x2 = 0.;				// Mid wing
			x = wingPosition;

			y1 = get_KHf_vs_zh_over_bv1_low_wing(horizPosOverVertical, arVertical, tailconeShape);
			y2 = get_KHf_vs_zh_over_bv1_mid_wing(horizPosOverVertical, arVertical, tailconeShape);
			y = MyMathUtils.interpolateLinear(x1, y1, x2, y2, x);

		} else if (wingPosition == 0.) {
			y = get_KHf_vs_zh_over_bv1_mid_wing(horizPosOverVertical, arVertical, tailconeShape);

		} else if ((wingPosition > 0.) && (wingPosition < 1.)) {
			// Linear interpolation
			x1 = 0.;				// Mid wing
			x2 = 1.;				// High wing
			x = wingPosition;

			y1 = get_KHf_vs_zh_over_bv1_mid_wing(horizPosOverVertical, arVertical, tailconeShape);
			y2 = get_KHf_vs_zh_over_bv1_high_wing(horizPosOverVertical, arVertical, tailconeShape);
			y = MyMathUtils.interpolateLinear(x1, y1, x2, y2, x);

		} else {
			y = get_KHf_vs_zh_over_bv1_high_wing(horizPosOverVertical, arVertical, tailconeShape);
		} 

		return y;
	}
	
	public double getkFv() {
		return kFv;
	}

	public double getkVf() {
		return kVf;
	}

	public double getkWv() {
		return kWv;
	}

	public double getkWf() {
		return kWf;
	}

	public double getkHv() {
		return kHv;
	}

	public double getkHf() {
		return kHf;
	}


}
