package configuration.enumerations;

public enum CostsEnum {
	//--------------------------------
	// Input
	//--------------------------------
	// Fixed Charged
	Residual_Value,
	Total_Investiment,
	Depreciation_period,
	//Utilization, //it depends on block_time 
	Interest, //per year 
	Insurance, // Year Rate
	num_cabin_crew_members,
	//--------------------------------
	// Trip Charged,
	MTOW,
	Block_Time,
	Range,
	Payload,
	Manufacturer_Empty_Weight, // It can be taken equal to OEM (Operating Empty Mass)
	//Time_Flight, it depends on block_time
	Labor_Manhour_Rate,
	Price,// of A/C without engines price
	OAPR,
	T0,//Thrust sea level
	BPR, // By Pass Ratio
	num_compressor_stages,
	K, // It's equal to 0.50, 0.57, 0.64 respectively for one, two three shafts 
	Block_Fuel,
	Fuel_costs,
	//--------------------------------
	// Output
	//--------------------------------
	total_TripCharges,
	total_FixedCharges,
	DOC;
}
