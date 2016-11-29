-----------------------------------------------------------------
SFC UNIT CONVERSION FROM THE DATABASE
-----------------------------------------------------------------
	.............................................................
	TURBOPROP
	.............................................................
		SFC given with respect to thrust. In order to obtain SFC
		in kg/min, the following conversion is required:
		
			SFC_database*T_actual(N)*(2.20462/9.81)*(0.454/60)
	.............................................................
	TURBOFAN
	.............................................................
		SFC given with respect to thrust. In order to obtain SFC
		in kg/min, the following conversion is required:
		
			SFC_database*T_actual(N)*(0.45392/(4.4482*60))
------------------------------------------------------------------