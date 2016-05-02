package configuration.enumerations;

public enum HighLiftExexutableEnum {

	/*
	 * Input variable names
	 */
	
	//FLIGHT CONDITION:
	AlphaCurrent,
	
	//WING:
	//Geometry
	AspectRatio,
	Span,
	Surface,
	SweepQuarterChordEq,
	TaperRatioEq,
	DeltaYPercent,
	
	//Clean configuration parameters
	AlphaMaxClean,
	AlphaStarClean,
	CL0Clean,
	CLAlphaClean,
	CLmaxClean,
	CLStarClean,
	
	//Mean airfoil
	ClAlphaMeanAirfoil,
	LERadiusMeanAirfoil,
	MaxThicknessMeanAirfoil,
	
	//FLAPS DATA:
	FlapType,
	DeltaFlap,
	Cfc,
	EtaInFlap,
	EtaOutFlap,
	
	// SLATS DATA:
	DeltaSlat,
	Csc,
	CextCSlat,
	EtaInSlat,
	EtaOutSlat,
	
	/*
	 * Output variable names
	 */
	AlphaMaxFlapSlat,
	AlphaStarFlapSlat,
	CLAlphaFlapList,
	CLAlphaFlap,
	CLmaxFlapSlat,
	CLStarFlapSlat,
	DeltaCD0List,
	DeltaCD0,
	DeltaCl0FlapList,
	DeltaCl0Flap,
	DeltaCL0FlapList,
	DeltaCL0Flap,
	DeltaClmaxFlapList,
	DeltaClmaxFlap,
	DeltaCLmaxFlapList,
	DeltaCLmaxFlap,
	DeltaClmaxSlatList,
	DeltaClmaxSlat,
	DeltaCLmaxSlatList,
	DeltaCLmaxSlat,
	DeltaCMc4List,
	DeltaCMc4
}
