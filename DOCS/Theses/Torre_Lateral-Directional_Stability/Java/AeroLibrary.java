package it.unina.sandboxgt;

import sun.print.resources.serviceui;

public class AeroLibrary {

	
		
	/**
	 * 
	 * @param weight aircraft weight
	 * @param density air density at flight altitude
	 * @param mach mach number at flight altitude
	 * @param soundSpeed sound speed at flight altitude
	 * @param wingSurface wing surface
	 * @return Lift coefficient
	 */
	public static double calculateLiftCoefficient(double weight, double density, double mach, double soundSpeed, double wingSurface){
		double velocity = mach*soundSpeed;
		double a = Math.pow(velocity, 2);
		
		return ((2.0)*weight)/(density*a*wingSurface);
	}
	
		/**
	 	* 
	 	* @param cr chord root
	 	* @param lambda sweep angle at leading edge
	 	* @return mean chord
	 	*/
		public static double calculateMeanChord(double cr, double lambda) {
			double mac = (2./3.)*cr*(1.+lambda+lambda*lambda)/(1+lambda);
			
			return mac;
			
		}
		
		/**
		 * 
		 * @param lambdaLE sweep angle at leading edge 
		 * @param chord  percent chord  
		 * @param TaperRatio division between chord root and chord tip
		 * @param AspectRatio division between square of wing span and wing surface
		 * @return Sweep angle at a generic chord e.i(c/4, c/2, ecc)
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 44
		 */
		
		
		public static double calculateSweepAngle(double lambdaLE, double chord, double TaperRatio, double AspectRatio){
			
			double b = ((4*chord*(1-TaperRatio))/(AspectRatio*(1+TaperRatio)));
			double c = Math.tan(lambdaLE);
			double a = c - b ;
			
			return Math.atan(a);
			
		}
		/**
		 * 
		 * @param b span of aerodynamics surface
		 * @param TaperRatio taper ratio of aerodynamics surface
		 * @param lambdaLE Sweep angle at leading edge of aerodynamics surface
		 * @return position of mean chord leading edge
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 44

		 */
		public static double calculateMeanChordLeadingEdge(double b, double TaperRatio, double lambdaLE) {
			
			return (b/6.)*(1.+(2.)*TaperRatio)*(Math.tan(lambdaLE))/(1.+TaperRatio);		
		}

		/**
		 * 
		 * @param b span of aerodynamics surface
		 * @param cr chord root of aerodynamics surface
		 * @param TaperRatio taper ratio of aerodynamics surface
		 * @return wing surface for a non straight wing planform
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 44
		 */
		public static double calcWingSurface(double b, double cr, double TaperRatio) {
		
		return (b/2.)*cr*(1+TaperRatio);
		
		}
		
		/**
		 * 
		 * @param aspectRatio aspect ratio of aerodynamics surface
		 * @param lambdaLE Sweep angle at leading edge of aerodynamics surface
		 * @return k factor used in Polhamus' formula to find lift-scope gradient
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 46
		 */
		
		public static double calculateKFactorforCLalfa(double aspectRatio, double lambdaLE)
		{
			// NOTE: angle in radian
		  double k=0;
			if (aspectRatio < 4){
				
				k = 1. + (aspectRatio*(1.87-0.000233*lambdaLE))/100;
			}
			else if (aspectRatio >=4) {
				k =  1. + (8.2-2.3*lambdaLE-aspectRatio*(0.22-0.153*lambdaLE))/100;
			}
			 System.out.println("\n k =" + k );
			return k;
			
		}
		
		/**
		 * 	
		 * @param aspectRatio aspect ratio of aerodynamics surface
		 * @param lambdaLE Sweep angle at leading edge of aerodynamics surface
		 * @param mach mach number at flight altitude
		 * @return lift-scope gradient with Polhamus' formula
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 46
		 */
		public static double calculateCLalfaPolhamus(double aspectRatio, double lambdaLE, double mach, double taperRatio){
			
			double k=calculateKFactorforCLalfa(aspectRatio, lambdaLE);
			
			double lambdaMean = calculateSweepAngle(lambdaLE, 0.50, taperRatio, aspectRatio);
			
			double a = aspectRatio*aspectRatio*(1-mach*mach)/(k*k);
			
			double tan = Math.tan(lambdaMean);
			
			double b = 1 + (tan*tan)/(1 - Math.pow(mach, 2)); 
			
			double argument = a*b + 4;
			
			double denominator = 2 + Math.sqrt(argument);
			
			double clalfa = ((2.)*Math.PI*aspectRatio)/denominator;
			
			return clalfa/1.17171134;
			}
		
	/**
	 * 
	 * @param clalfah lift-curve slope gradient of the horizontal tail	
	 * @param etah dynamic pressure ratios
	 * @param s wing's surface	
	 * @param sh horizontal tail surface		
	 * @param xgc center of gravity expressed as a percentage of the mean aerodynamics chord
	 * @param xach aerodynamic center of horizontal tail expressed as a percentage of the mean aerodynamics chord
	 * @param downwash downwash gradient 
	 * @return lift variation with the rate of change in the angle of attack
	 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 94

	 */
		public static double calculateCLAlphaDotHTail(double clalfah, double etah, double s, double sh, double xgc, double xach,  double downwash ) {
	//CLalfapunto riferito all'horizontal tail
			
			
			return (2.)*clalfah*etah*(sh/s)*(xach-xgc)*downwash;
		}
		
		/**
		 * 
		 * @param mach Mach number
	     * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 94
		 * @return the compressibility factor
		 */
		
		public static double calculateCompressibilityFactor(double mach){
		//B
			
			double beta2 = 1 - (mach*mach); 
			
			return  Math.sqrt(beta2);
			//embraer 195
		}
		/**
		 * 
		 * @param mach Mach number
		 * @param lambda sweep angle at 25 percent of the mean aerodynamics cords
		 * @return the compressibility factor included sweep angle effect
	     * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 94


		 */
		
		public static double calculateCompressibilityFactorSweepAngle(double mach, double lambda){
		//B
			double c = Math.cos(lambda);
			
			double beta2 = 1 - (mach*mach)*(c*c); 
			
			return  Math.sqrt(beta2);
			//embraer 195
		}
		

		/**
		 * 
		 * @param xacw aerodynamics center of wing expressed as a percentage of the mean aerodynamics chord
		 * @param xgc center of gravity expressed as a percentage of the mean aerodynamics chord
		 * @param clalfa lift-curve slope gradient of the wing at mach = 0 condition	
		 * @return lift variation associated with pitch rate at zero lift condition (wings effect)
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 95

		 */
		
		public static double calculateCLPitchRateWingZLCondition(double xacw, double xgc, double clalfa){
		//CLq0L wing
			double a = 2*Math.abs(xacw-xgc); // 0.33212719082151937
			
			double b = (0.5 + a)*clalfa;
			
		
			
			return  b;
			
		}
		
		/**
		 * 
		 * @param ar AspectRatio
		 * @param LambdaLE Sweep angle at leading edge of wing
		 * @param mach Mach number
		 * @param xacw aerodynamics center of wing expressed as a percentage of the mean aerodynamics chord
		 * @param xgc center of gravity expressed as a percentage of the mean aerodynamics chord
		 * @param TaperRatio division between chord root and chord tip
		 * @return lift variation associated with pitch rate (wings effect)
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 95

		 */
		
		public static double calculateCLPitchRateWing(double ar, double LambdaLE, double mach, double xacw, double xgc, double TaperRatio ){
			//CLq wing
			
			double Lambdaquarter = calculateSweepAngle(LambdaLE, 0.25, TaperRatio, ar);//27 deg
			
			double beta =  calculateCompressibilityFactorSweepAngle(mach, Lambdaquarter);
			
	        System.out.println("\n beta = " + beta);
	        
	        double clalfamachzero = calculateCLalfaPolhamus(ar, LambdaLE, 0, TaperRatio);

	        System.out.println(" CLalfa(M=0) = " + clalfamachzero );
	        
			double CLqwZL =  calculateCLPitchRateWingZLCondition(xacw, xgc, clalfamachzero);

	        System.out.println("\n clqw0L = " + CLqwZL);

	        double a = ar + 2.0*Math.cos(LambdaLE);
			
	        double b = ar*beta + 2.0*Math.cos(LambdaLE);
	        
			return a*CLqwZL/b;
			
			
		}	
		
		/** 
		 * 
		 * @param clalfah Lift-curve slope gradient of the horizontal tail	
		 * @param etah  Dynamic pressure ratios
		 * @param s Wing surface	
		 * @param sh Horizontal tail surface
		 * @param xgc Center of gravity expressed as a percentage of the mean aerodynamics chord
		 * @param xach Aerodynamics center of wing expressed as a percentage of the mean aerodynamics chord
		 * @return lift variation associated with pitch rate (horizzontal tail effect)
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 94
		 */
		
		public static double calculateCLPitchDotHTail(double clalfah, double etah, double s, double sh, double xgc, double xach ) {
	//CLq horizontal tail
			return (2.0)*clalfah*etah*(sh/s)*(xach-xgc);
	
		}
		
		/**
		 * 
		 * @param clqw
		 * @param clqht 
		 * @return lift variation associated with pitch rate (both wing and horizontal tail)
		 */
		
		public static double calculateCLPitchRate(double clqw, double clqht){
			//CLq
			return clqw + clqht;
			
		}
		/**
		 * @param clalfah Horizontal tail lift-slope gradient   
		 * @param etah dynamics pressure ratio
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param xgc gravity center position (over mean aerodynamics chord)
		 * @param xach aerodynamics center position ((over mean aerodynamics chord)
		 * @param downwash downwash gradient 
		 * @return Derivative of Aircraft Pitch Coefficient w.r.t. alpha-dot, due to horizontal tail
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 95
		 */

		public static double calculateCMAlphaDotHTail(double clalfah, double etah, double s, double sh, double xgc, double xach,  double downwash ) {
			//CMalfapunto

			
			return (-2.)*clalfah*etah*(sh/s)*(xach-xgc)*(xach-xgc)*downwash;
		}
		
		
		/**
		 * 
		* @param clalfah lift-slope gradient of the horizontal tail 
		 * @param etah dynamics pressure ratio
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param xgc gravity center position (over mean aerodynamics chord)
		 * @param xach horizontal tail aerodynamics center position ((over mean aerodynamics chord)
		 * @return longitudinal damping  coefficient pitch rate effect
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 96
		 */
		public static double calculateCMPitchRateHTail(double clalfah, double etah, double s, double sh, double xgc, double xach) {
			//CMqpunto

			return (-2.)*clalfah*etah*(sh/s)*(xach-xgc)*(xach-xgc);
		}
		
		/**
		 * 
		 * @param arw wings' aspect ratio
		 * @param xacw wings' aerodynamics center position ((over mean aerodynamics chord)
		 * @param xgc gravity center position (over mean aerodynamics chord)
		 * @param lambdaLE leading edge sweep angle of wings
		 * @param TaperRatio wings' taper ratio
		 * @return C coefficient used to calculate longitudinal damping  coefficient pitch rate effect of the wing
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 95
		 */
		
		public static double calculatecoefficientC(double arw, double xacw, double xgc, double lambdaLE, double TaperRatio){
			
			double lambda = calculateSweepAngle(lambdaLE, 0.25, TaperRatio, arw);
			// calcolare il coefficinete c pag 95 Napolitano che serve a valutare il coefficiente di momento dovuto a q
			
			double A= ((0.5)*Math.abs(xacw-xgc)+(2.)*Math.abs(xacw-xgc)*Math.abs(xacw-xgc));
			
			double B= arw+(2.)*Math.cos(lambda);
			
			double D= (Math.pow(arw, 3))*Math.tan(lambda)*Math.tan(lambda);
			
			double E= arw+(6.)*Math.cos(lambda);
			
			double C= (arw*A/B ) + (D / ((24.)*E)) + ((1.)/(8.));
			
			return C;
			
		}
		
		/**
		 * 
		 * @param ar wing aspect ratio
		 * @param clalfamachzero lift-slope gradient at mach = 0
		 * @param lambda sweep angle at quarter of chord
		 * @param c Coefficient described at page 95 of Napolitano_Aircraft_Dynamics_Wiley
		 * @return longitudinal damping  coefficient pitch rate effect of the wing at zero lift condition
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 95
		 */
		public static double calculateCMPitchRateWingMach0 (double ar, double clalfamachzero, double lambda, double c){
		
			double kp = DatabaseReader.get_C_m_q_w_kp_VS_AR(ar);
			
			System.out.println("\n kp = " + kp);
			//calcolo cmqw for mach=0;	
			return -kp*clalfamachzero*lambda*c;
			
		}
		
		//public static double calculateCMpitchDotWingMach0 (double clalfamachzero, double lambda, double c,double ar){
		//calcolo cmqw for mach=0;	
			//return calculateCMpitchDotWingMach0(kp(mettere la funzione dal database), clalfamachzero, lambda, c);
			//return 0.5;
		//}
		/**
		 * 
		 * @param arw wings' aspect ratio
		 * @param lambdaLE leading edge sweep angle of wings
		 * @param mach mach number
		 * @param xacw wings' aerodynamics center position ((over mean aerodynamics chord)
		 * @param xgc gravity center position (over mean aerodynamics chord)
		 * @param clalfamachzero lift-scope gradient at mach = 0 condition
		 * @param TaperRatio wing's taper ratio
         * @return longitudinal damping  coefficient pitch rate effect of the wing 
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 95
		 */
		
		
		public static double calculateCMPitchRateWing( double arw, double lambdaLE, double mach, double xacw, double xgc, double clalfamachzero, double TaperRatio) {
			//CMqw
			double lambda = calculateSweepAngle(lambdaLE, 0.25, TaperRatio, arw);

			double C = calculatecoefficientC( arw, xacw, xgc, lambda,TaperRatio);
			
			System.out.println("\n C= " + C);
			
			double Cmqwmachzero = calculateCMPitchRateWingMach0 ( arw, clalfamachzero, lambda, C);
			
			double B = calculateCompressibilityFactorSweepAngle( mach, lambda);
			
			double N1 = (Math.pow(arw, 3.0))*Math.tan(lambda)*Math.tan(lambda);
			
			double N2 = arw + 6*Math.cos(lambda);
			
			double N = (N1/N2) + ((3.)/B);
			
			double D = (N1/N2) + (3.);
			
			
			return (N/D)*Cmqwmachzero;
			
		}
		
		
		public static double calculateCMpitchDot(double cmqw, double cmqht){
			//cmq
			
			return cmqw + cmqht;
		}
		/**
		 * 
		 * @param bv vertical tail span
		 * @param rone Fuselage height at quarter vertical tail root chord
		 * @param taperRatio vertical tail taper ratio (chord tip over chord root)
		 * @param zh vertical position of horizontal tail over 
		 * @param xachv distance of horizontal tail aerodynamic center from h-tail apex
		 * @param chordVT vertical tail mean aerodynamic chord
		 * @param sh horizontal tail surface
		 * @param sv vertical tail surface
		 * @param arVT vertical tail aspect ratio
		 * @return vertical tail effective aspect ratio used to find lift-slope grandient of vertical tail
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 143-144-145		
		 *  */
		
		public static double calculateEffectiveAspectRatioVerticalTail(double bV,double rone, double taperRatio, double zH, double xACHV,double chordVT,  double sh, double sv, double arVT) {
			
			double cone = DatabaseReader.get_C1_bvOver2R1(bV, rone, taperRatio);
						
			double ctwo = DatabaseReader.get_AR_v_eff_c2_vs_Z_h_over_b_v_x_ac_h_v_over_c_bar_v(zH, bV, xACHV, chordVT);
			
			System.out.println("\n zH/bV = "+zH/bV + "\n xACHV/MeanAerodynamicChordVT = " + xACHV/chordVT+"\n VTSpan/(2*r1) = "+ bV/(2.0*rone));
			
			double kappaHW = DatabaseReader.getkhvCoefficient(sh, sv);
			
			System.out.println("\n c1 = "+cone + "\n c2 = "+ctwo+"\n kHW = "+kappaHW);
			
			return cone*arVT*(1 + kappaHW*(ctwo - 1)) ;
					
		}
		/**
		 * wing's contribution to side force is significant only if the dihedral angle is positive
		 * @param gamma gamma is dihedral angle of wing
		 * @return wing's contribution to side force variation with beta
         * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 140
		 */
		
		public static double calculateCYBetaWing(double gamma){
			//levato57.3
			double a = Math.pow(57.3, 2);
			return -0.0001*Math.abs(gamma)*a;
		}
		/**
		 * 
		 * @param kint wing - body interference factor (extracted from figure 4.8 Napolitano_Aircraft_Dynamics_Wiley_2012 page 140 )
		 * @param spv cross section at location of the fuselage where the flow ceases to be potential
		 * @param sw wing surface
		 * @return body's contribution to side force variation with beta
         * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 140
		 */
		
		public static double calculateCYBetaBody(double  kint, double spv, double sw){
			
			return -2*kint*spv/sw;
		}
		
		//public static double calculateCYBetaHT(double zw, double d, double s)
		/** 
		 * @param s wing surface
		 * @param sh vertical tail tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param d maximum fuselage height
		 * @param ar wing aspect ratio
		 * @return sidewash factor with dynamics pressure ratio effect
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 142
		 */
		
		public static double calculateSidewash(double s, double svt, double lambda, double zW, double d, double ar ){
			
			return 0.724 + (3.06)*(0.5*svt/s)/(1+Math.cos(lambda)) + (0.4)*(zW/d) + 0.009*ar;
		}
		
		/**
		 * @param s wing surface
		 * @param sh vertical tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param d maximum fuselage height
		 * @param ar wing aspect ratio
		 * @param gamma dihedral angle of horizontal tail
		 * @return horizontal tail contribution to side force variation with beta
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 pag 142
		 */
		
		public static double calculateCYBetaHT(double s, double sh, double lambda, double zW, double d, double ar, double gamma){
			
			double sidewash = calculateSidewash(s, sh, lambda, zW, d, ar);
			
			return -0.0001*(Math.abs(gamma))*sidewash*57.3*57.3*sh/s;
		}
		
		/**
		 * 
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh vertical tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord	
		 * @param ar wing aspect ratio
		 * @return vertical tail contribution  to side force variation with beta
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 143
		 */
		public static double calculateCYBetaVT(double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT, double rOne, double ar ){
			
			double kvy = DatabaseReader.getkyvCoefficient(spanVT, rOne);
			
			double u = spanVT/(2.0*rOne);
			System.out.println("spanVT/r1 = "+u);
			System.out.println("\nkvy = " + kvy);
			double sidewash = calculateSidewash(s, sv, lambda, zW, d, ar);
			
			System.out.println("\n Siedewash = " + sidewash);
			
			double a = sv/s;
			
			double b = Math.abs(clalfa);
			
			return -kvy*b*sidewash*a;
			
		}
		
		
		
		//public static double calculateCYBetaVT(double clalfa, double s, double sv, double lambda, double d, double zW, double bv, double r, double ar, double gamma ){
			
		//}
		
		//public static double calculateVerticalTailAspectRatioEffective(double cone, double arv, double kappaHV, double ctwo){
			
			//return cone*arv*(1 + kappaHV*(1 + ctwo));
			
		//}
		
		//		public static double calculateVerticalTailAspectRatioEffective(double bv, double r1, double lambdav, double zH, double meanChordVT, double xachv, double sh, double sv, double arv, double kappaHV, double ctwo){
		//		}

		/**
		 * 
		 * @param clAlfa Vertical Tail lift-scope gradient 
		 * @param etav dynamics pressure ratio on the Vertical tail
		 * @param sv vertical tail surface
		 * @param s wing surface
		 * @param chordCS Mean aerodynamic chord of the control (i.e flaps airelons or rudder)
		 * @param macVT Mean aerodynamic chord of the Vertical Tail
		 * @param etainner Rudder inner station
		 * @param etaouter Rudder outer station
		 * @param taperRatioVT taper Ratio of vertical tail
		 * @return contribution to lateral force coefficient with the deflection of the rudder
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 149
		 */

		public static double calculateCYDeltaRudder(double clAlfa, double etav, double sv, double s, double chordCS, double macVT, double etainner,double etaouter,double taperRatioVT){
			
			double a = Math.abs(clAlfa);
			
			double c = (0.5*sv)/s;
			
			double dK = DatabaseReader.get_k_Rudder_vs_ChordRudderOverMAC_VT(etainner, etaouter, taperRatioVT);
			
			double taur = DatabaseReader.tau_ControlSurface_VS_chordCSOverMACVT(chordCS, macVT);
			
			System.out.println("\n dK = "+ dK + "\n taur = "+ taur);
			
			return a*etav*c*taur*dK;
		}
			
		
		// public static double calculateCYDeltaRudder(double clAlfa, double etav, double sv, double s, double cr, double cv, double lambdav, double etai, double etaf){
		
		//}
		/**
		 * 
		 * @param zV vertical distances of the point of application of lateral force of vertical tail
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param b wing span
		 * @param alfa attack angle 
		 * @return dimensionless moment arm associated with the dihedral effect from  the vertical tail
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 160
		 */
		public static double calculateMomentArm(double zV, double xV, double b, double alfa){
			
			return ((zV*(Math.cos(alfa)))-(xV*(Math.sin(alfa))))/b;
			
			
		}
	
		/**
		 * 
		 * @param arWing wing aspect ratio
		 * @param wingSpan wing span
		 * @param zWing vertical distance from center of gravity of 25 percent of the wings root
		 * @param db  average diameter of fuselage
		 * @return  correction to wing-body dihedral effect factor associated with the location of the fuselage with respect to the wing modeled using
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 156
		 */
		public static double calculateCLBetaBody ( double arWing , double wingSpan, double zWing, double db){
			double a = Math.sqrt(arWing);
			
			double d = 1.2/57.3;
			
			double b = zWing/wingSpan;
			
			double c = (2.0)*db/wingSpan;
			
			return 57.3*(a*d)*b*c;
			
		}
		
		/**
		 * 
		 * @param ar wings'aspect ratio
		 * @param db the average cross-sectional area along the fuselage
		 * @param b wing span
		 * @return correction factor associated with the size of the fuselage model
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 156
		 */
		public static double calculateCLBetaBodyCorrection (double ar, double db , double b){
			//Vedere
			
			return -0.0005*ar*Math.pow((db/b), 2);
		}
		
		/**
		 * 
		 * @param TaperRatio wings taper ratio (chord tip over chord root)
		 * @param lambdamean sweep angle at 0.5 station (in radiants)
		 * @param gamma wing dihedral angle
		 * @param mach mach number
		 * @param ar wing aspect raatio
		 * @param db mean diameter of fuselage
		 * @param b wing span 
		 * @return contribution to wing-body dihedral effect due to wing dihedral angle with a correction factor associated with the size of the fuselage model effect 
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 155-156-158
		 */
		
		public static double calculateRollingCoefficientDihedralWingContribution(double taperRatio, double lambdamean, double gamma, double mach, double ar, double db, double b){
			
			double a = Math.pow(57.3, 2);

			double body = gamma*a*calculateCLBetaBodyCorrection(ar, db, b);
			
	        System.out.println("\n Correction factor associated with the size oh the fuselage modeled using = "+ body);

	        double lambda=57.3*lambdamean;
	        
	        double cDW = DatabaseReader.get_cl_beta_over_gamma_vs_AR_and_TaperRatio(ar, lambda, taperRatio);
			
	        double kMGamma = DatabaseReader.get_kMGamma_vs_Mach_and_AR(ar, mach, lambdamean);
	        
	        System.out.println("\n cDW = "+cDW + "\n kMGamma = "+kMGamma);
	        
			return gamma*a*(cDW*kMGamma) + body ;
		
		}
		
		//public static double calculateRollingCoefficientDihedralWingContribution(double lambda mean, double gamma, double mach, double lambda, double ar, double sb, double b){
		
		//double body = calculateCLBetaBodyCorrection(ar, sb, b);
		
		//}
		
		/**
		 * 
		 * @param TaperRatio wings taper ratio (chord tip over chord root)
		 * @param ar wing aspect ratio
		 * @param lambdamean sweep angle at 0.5 station (in radiants)		 
		 * @param cl lift coefficient
		 * @param mach mach number
		 * @param wingspan wing span
		 * @param lenght distances beetwen fuselage nose at wings tip leading edge
		 * @return contribution to wing-body dihedral effect associated with the wing sweep angle with correction due to mach number and the lenght of the forward portion of the fuselage to the wing-body dihedral effect
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 155-156-157
		 */


		public static double calculateCLBetaWBSweepAngleEffect(double taperRatio,double ar, double lambdamean, double cl, double mach, double wingspan, double lenght) {
			
			double lambda = 57.3*lambdamean;
			
			double cSW = DatabaseReader.get_clbetaOverCl(taperRatio, ar, lambda);
			
			double kMach = DatabaseReader.get_KM_due_to_sweep_angle(ar, lambdamean, mach);
			
			double kF = DatabaseReader.get_KF_due_to_Sweep_Angle(lenght, wingspan, ar, lambdamean);
			
			System.out.println("\n cSW = "+cSW+"\n kMach = "+kMach + "\n kF = "+kF);
			
			return 57.3*cl*cSW*kF*kMach;
			
		}


		// 		public static double calculateCLBetaWBSweepAngleEffect(double lambdadamean, double taperRatio, double ar , double cl, double mach, double A, double b) {
		//}
		
		/**
		 * 
		 * @param cl lift coefficient
		 * @param ar wing aspect ratio
		 * @param taperRatio wing taper ratio (chord tip over chord root)
		 * @return contribution associated with the wing aspect ratio to the wing-body dihedral effect
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 155-157
		 */
		
		public static double calculateCLBetaWBAspectRatioEffect(double cl, double ar, double taperRatio){
			
			double cAR = DatabaseReader.get_cL_Beta_over_cl_Sweep_angle(ar, taperRatio);
			
			System.out.println("\n cAR = "+cAR);
			
			return 57.3*cl*cAR;
		}

		//public static double calculateCLBetaWBAspectRatioEffect(double cl, double lambda, double ar){
			
		//}
		/**
		 * 
		 * @param epsilon Twist angle at wing tip
		 * @param lambdaquarter Sweep angle at  25 percent 
		 * @param ar wing aspect ratio
		 * @param taperRatio wing taper ratio (chord tip over chord root)
		 * @return contribution associated with the twist angle between the zero-lift lines of the wing section at the tip and at the root station to the wing-body dihedral effect
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 155-159
		 */
		public static double calculateCLBetaWingTwistEffect(double epsilon, double lambdaquarter, double ar, double taperRatio){
			
			double a = Math.pow(57.3, 2);

			double cEpsWing =  DatabaseReader.get_deltaCL_Beta_over_eps_tan_VS_AR_and_taper_ratio(ar, taperRatio);
			
			System.out.println("\n cEpsWing = "+ cEpsWing);
			
			return -a*cEpsWing*epsilon*(Math.tan(lambdaquarter));
		}

		
		//public static double calculateCLBetaWingTwistEffect (double epsilon, double lambdaquarter, double ar, double taperRatio){
		//}
		/**
		 * 
		 * @param epsilon Twist angle at wing tip
		 * @param lambdaquarter Sweep angle at  25 percent 
		 * @param cl lift coefficient
		 * @param TaperRatio wings taper ratio (chord tip over chord root)
		 * @param lambdamean sweep angle at 0.5 station (in radiants)		 
		 * @param mach mach number
		 * @param wingspan wing span
		 * @param lenght distances beetwen fuselage nose at wings tip leading edge		 
		 * @param TaperRatio wings taper ratio (chord tip over chord root)
		 * @param lambdamean sweep angle at 0.5 station (in radiants)		 
		 * @param gamma wing dihedral angle
		 * @param mach mach number		 
		 * @param ar wing aspect raatio
		 * @param sb the average cross-sectional area along the fuselage
		 * @param b wing span 
		 * @param zWing vertical distance from center of gravity of 25 percent of the wings root
		 * @param db  mean diameter of fuselage
		 * @return dihedral effect of the wing-body
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 155-156-157-158-159
		 */
		
		public static double calculateCLBetaWingBody(double epsilon, double lambdaquarter,double cl, double lenght, double taperRatio, double lambdamean, double gamma, double mach,double ar, double b, double zWing, double db){
	        System.out.println("---------------------------------------------------\n"); 
			double a = calculateCLBetaWingTwistEffect ( epsilon, lambdaquarter, ar,taperRatio);
			double c = calculateCLBetaWBAspectRatioEffect(cl,ar,taperRatio);
			double d = calculateCLBetaWBSweepAngleEffect(taperRatio,ar,lambdamean,cl,mach,b,lenght);
			double e = calculateRollingCoefficientDihedralWingContribution(taperRatio,lambdamean,gamma,mach,ar,db,b);
			double f = calculateCLBetaBody(ar, b , zWing, db);
	        System.out.println("\n Dihedral Angle effect on rolling coefficient = "+ e);
			System.out.println("\n Twist Angle effect on rolling coefficient = "+ a);
	        System.out.println("\n Aspect Ratio on rolling coefficient = "+ c);
	        System.out.println("\n Sweep Angle on rolling coefficient = "+ d);
	        System.out.println("\n Body Effect on rolling coefficient = "+ f);


			return a+e+c+d;
		}
		/**
		 * Same parameters of the dihedral effect of the wing but with horizontal tail parameters, the plus parameters is used to adjust HT result to the wing
		 * @param epsilonHT
		 * @param lambdaquarterHT
		 * @param clHT
		 * @param TaperRatioHT
		 * @param lambdameanHT sweep angle at 0.5 station (in radiants)		 
		 * @param gammaht
		 * @param mach
		 * @param arht
		 * @param sb
		 * @param spanHT
		 * @param zHT
		 * @param db
		 * @param etah
		 * @param zWing
		 * @param sHT
		 * @param swing wing surface
		 * @param wingpan wing span
		 * @return dihedral effect of the HT-body
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 155-156-157-158-159
		 */
		public static double calculateCLBetaHorizontalTail (double epsilonHT, double lambdaquarterHT,double clHT, double lenghtHT, double taperRatioHT, double lambdameanHT, double gammaHT, double mach,double arHT, double spanHT, double zHT, double db, double etah, double zWing,double sHT, double swing,double wingspan )
		{
			double a = calculateCLBetaWingBody(epsilonHT,lambdaquarterHT,clHT,lenghtHT,taperRatioHT,lambdameanHT,gammaHT,mach,arHT,spanHT, zWing, db);
			
			return a*etah*(sHT/swing)*(spanHT/wingspan); 
			
		}
		
		
		/**
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh vertical tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord
		 * @param ar wing aspect ratio
		 * @param zV vertical distances of the point of application of lateral force of vertical tail
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param b wing span
		 * @param alfa angle of attack
		 * @return  dihedral effect of the Vertical Tail
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 160
		 */
		
		public static double calculateCLBetaVerticalTail (double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,double r0ne, double ar, double zV, double xV, double b, double alfa ){
			
			double cYAlfaVT = calculateCYBetaVT(clalfa, s, sv, lambda, d, zW, spanVT,r0ne, ar );
			
			double a = zV*Math.cos(alfa);
			
			double c = xV*Math.sin(alfa);
			
			double l =  ( a - c )/b;
			
			double e = 0.5*sv/s;

			double sidewash = calculateSidewash(s, sv, lambda, zW, d, ar);

			System.out.println("\n moment's arm = " + l +"\n sidewash = " + sidewash);
			
			return cYAlfaVT*sidewash*e*l;
			

		}

		//chord è la percentuale di corda 0.25
		/**
		 * 
		 * @param lambdaLE wing sweep angle 
		 * @param machmach number
		 * @param taperRatio wing taper ratio
		 * @param aspectRatio wing aspect ratio
		 * @param chord percent of the chord (0.25 for this parameter)
		 * @return lambda beta//domandare come si chiama
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 162
		 */
		public static double calculateLambdaBeta(double lambdaLE, double mach, double taperRatio, double aspectRatio, double chord){
			
			double lambda = calculateSweepAngle(lambdaLE, chord, taperRatio, aspectRatio);
			
			double tan = Math.tan(lambda); 
						
			double beta = Math.sqrt((1-mach*mach));
			
			return Math.atan((tan/beta));
		}
		/**
		 * 
		 * @param aspectRatio wing aspect ratio
		 * @param lambdaLE wing sweep angle at leading edge
		 * @param mach mach number
		 * @param taperRatio wing taper ratio
 		 * @return coefficient k //domandare
 		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 162 
		 */
//		public static double calculateKappaDeltaAirelons(double aspectRatio, double lambdaLE, double mach, double taperRatio){
//			
//			double beta = Math.sqrt((1-mach*mach));
//			
//			double clAlfa = calculateCLalfaPolhamus(aspectRatio, lambdaLE, mach, taperRatio);
//			
//			System.out.println("\n clalfaWingSecionnMach = "+clAlfa);
//			
//			return clAlfa*beta/(2*Math.PI);
//			
//		}
		public static double calculateKappaDeltaAirelons(double aspectRatio, double lambdaLE, double mach, double taperRatio){
			
			double beta = Math.sqrt((1-mach*mach));
			
			double clAlfa = calculateCLalfaPolhamus(aspectRatio, lambdaLE, mach, taperRatio);
			
			System.out.println("\n clalfaWingSecionnMach = "+clAlfa);
			
			return 4.85*beta/(2*Math.PI);
			
		}
		//domandare quello di sotto se mettere lambda c/4 o usare la funzione
		/**
		 * 
		 * @param aspectRatio wing aspect ratio
		 * @param lambdaLE wing sweep angle at leading edge
		 * @param mach mach number
		 * @param taperRatio wing taper ratio
		 * @param lambdaquarter wing sweep angle at percent of the chord
		 * @param etaInner Aileron inner station over wing semi-span
		 * @param etaOuter Aileron outer station over wing semi-span
		 * @param chordCS Mean aerodynamic chord of the control (i.e flaps airelons or rudder)
		 * @param macWing Mean aerodynamic chord of the Wing		 
		 * @return contribution of airelons' deflection  to rolling moment
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 162 to 166
		 */
		public static double calculateClAirelons(double aspectRatio, double lambdaLE, double mach, double taperRatio, double lambdaquarter,double etaInner, double etaOuter, double chordCS,double macWing){
		
			double beta = Math.sqrt((1-mach*mach));
						
			double k = calculateKappaDeltaAirelons(aspectRatio, lambdaLE, mach, taperRatio);
			
			double tauAirelons = DatabaseReader.tau_ControlSurface_VS_chordCSOverMACVT(chordCS, macWing);
			
	        double lambdabeta = 57.3*AeroLibrary.calculateLambdaBeta(lambdaLE, mach, taperRatio, aspectRatio, 0.25);

			double betaAROverKappa = beta*aspectRatio/k;
	        
			double rME = DatabaseReader.get_RME(etaInner, etaOuter, lambdabeta, aspectRatio, k, beta, taperRatio);
			
			System.out.println("\n TauAirelons = "+tauAirelons+"\n RME = "+rME+"\n k = "+k+"\n lambdabeta = "+lambdabeta + "\n beta = "+beta+"betaAROverKappa = "+betaAROverKappa);
			
			return ((rME*k)/beta)*tauAirelons;
		
		}
		 //Finire con i diagrammi
		/**
		 * 
		 * @param clAlfa vertical tail lift-slope gradient
		 * @param etav dynamic presssure ratio at Vertical tail
		 * @param sv vertical tail surface
		 * @param s wing surface
		 * @param chordCS Mean aerodynamic chord of the control (i.e flaps airelons or rudder)
		 * @param macVT Mean aerodynamic chord of the Vertical Tail		 
		 * @param etainner Rudder inner station
		 * @param etaouter Rudder outer station
		 * @param taperRatioVT taper Ratio of vertical tail
		 * @param wingspan wing span
		 * @param xr longitudinal distances of the point of application of lateral force, due to rudder deflection, from aircraft center of gravity
		 * @param zr vertical distances of the point of application of lateral force, due to rudder deflection, from aircraft center of gravity
		 * @param alfa angle of attack
		 * @return  contribution of rudder' deflection  to rolling moment
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 168
		 */
		public static double calculateCLRudder(double clAlfa, double etav, double sv, double s, double chordCS,double macVT, double etainner,double etaouter,double taperRatioVT , double wingspan , double xr , double zr, double alfa) {
			
			double force = calculateCYDeltaRudder(clAlfa, etav, sv, s, chordCS,macVT, etainner,etaouter,taperRatioVT);
			
			double arm = calculateMomentArm(zr, xr, wingspan, alfa);
			
			return force*arm;
		}
		
		/**
		 * 
		 * @param reynolds	reynnolds number of the fuselage
		 * @param sB fuselage side width
		 * @param sW wing span
		 * @param lB fuselage lenght
		 * @param b wing span
		 * @param lcg distances beetwen fuselage nose and center of gravity
		 * @param z1 Fuselage vertical width at 0.25 fuselage lenght
		 * @param z2 Fuselage vertical width at 0.75 fuselage lenght
		 * @param zMax Max fuselage vertical width
		 * @param omegaMax Max fuselage vertical width from top view
		 * @return body contribution to weathercock effect
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 172-173
		 */
		public static double calculateCNBetaBody( double reynolds, double sB, double sW, double lB, double b, double lcg, double z1, double z2, double zMax, double omegaMax){
			
			double kn = DatabaseReader.get_kn_body(lB, sB, 15.91,z1,z2,zMax,omegaMax) ;
	        
			double kR = DatabaseReader.get_kRE_vs_RE(reynolds);
			
			System.out.println("\n kn1  = " + kn+"\n kRE = "+ kR );
	        System.out.println("---------------------------------------------------\n");
			
			
			return -57.3*kn*kR*(sB/sW)*(lB/b);
			
		}
		
		//public static double calculateCNBetaBody(double lCG, double zOne, double zTwo, double zMax, double wMax , double re, double sB, double sW, double lB, double b){
			
			//return -57.3*kN*kR*(sB/sW)*(lB/b);
			
		//}
		/**
		 * 
		 * @param clalfa lift-scope gradient of vertical tail
		 * @param s wing surface
		 * @param sv vertical tail surface
		 * @param lambda vertical tail leading edge sweep angle 
		 * @param d maximum fuselage
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord
		 * @param ar wing aspect ratio
		 * @param zV vertical distances of the point of application of lateral force of vertical tail
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param alfa angle of attack
		 * @param b wing span
		 * @return vertical tail contribution to weathercock effect
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 143 and 174
		 */
		public static double calculateCNVerticalTail(double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,double rOne, double ar, double zV, double xV, double alfa, double b ){
		
			double force = calculateCYBetaVT( clalfa, s, sv, lambda, d, zW, spanVT,rOne, ar );	
						
			double a = zV*Math.sin(alfa);
			
			double c = xV*Math.cos(alfa);
			
			double arm = (a+c)/b;
			
			return (-force)*arm;
			
		}
		//modificare con cldelataa
		/**
		 * 
		 * @param delta correlation coefficient for yawing moment due to deflection of airelons (extracted from figure 4.72 Napolitano_Aircraft_Dynamics_Wiley_2012 page 175)
		 * @param cL lift coefficient 
		 * @param aspectRatio wing aspect ratio
		 * @param lambdaLE wing sweep angle at leading edge
		 * @param mach mach number
		 * @param taperRatio wing taper ratio
		 * @param lambdaquarter wing sweep angle at percent of the chord
		 * @param etaInner Aileron inner station over wing semi-span
		 * @param etaOuter Aileron outer station over wing semi-span		 
		 * @param chordCS Mean aerodynamic chord of the control (i.e flaps airelons or rudder)
		 * @param macWing Mean aerodynamic chord of the Wing	 
		 * @return contribution of airelons' deflection  to yawing moment
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 162 to 166 and 175
		 */
		
		
		public static double calculateCNAirelons(double ar, double etain,double etaout, double cL, double aspectRatio, double lambdaLE, double mach, double taperRatio, double lambdaquarter,double etaInner, double etaOuter, double chordCS,double machWing){
			
			double cLdA = calculateClAirelons(aspectRatio, lambdaLE, mach, taperRatio, lambdaquarter, etaInner, etaOuter, chordCS, machWing);
			
			double delta = DatabaseReader.get_kN_VS_eta_taper_ratio_ar(etain, etaout, ar, taperRatio);
			
			System.out.println("\n deltaKNa = "+ delta);
			
			return delta*cL*cLdA;
		}
		
		/**
		 * 
	     * @param clAlfa vertical tail lift-slope gradient
		 * @param etav dynamic pressure ratio at Vertical tail
		 * @param sv vertical tail surface
		 * @param s wing surface
		 * @param chordCS Mean aerodynamic chord of the control (i.e flaps airelons or rudder)
		 * @param macVT Mean aerodynamic chord of the Vertical Tail		 
		 * @param etainner Rudder inner station
		 * @param etaouter Rudder outer station
		 * @param taperRatioVT taper Ratio of vertical tail
		 * @param wingspan wing span
		 * @param xr longitudinal distances of the point of application of lateral force, due to rudder deflection, from aircraft center of gravity
		 * @param zr vertical distances of the point of application of lateral force, due to rudder deflection, from aircraft center of gravity
		 * @param alfa angle of attack
		 * @param kvy empirical factor for the lateral force at the vertical tail due to beta (extracted from figure 4.13 Napolitano_Aircraft_Dynamics_Wiley_2012 page 143)
		 * @return  contribution of rudder' deflection  to yawing moment
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 168-177
		 */
		public static double calculateCNRudder(double clAlfa, double etav, double sv, double s, double chordCS,double macVT, double etainner,double etaouter,double taperRatioVT,  double zV, double xV, double alfa, double b){
			
			double force = calculateCYDeltaRudder( clAlfa, etav, sv, s, chordCS,macVT, etainner,etaouter,taperRatioVT );	
			
			double sin = Math.sin(alfa);
			
			double cos = Math.cos(alfa);
			
			double arm =zV*sin+xV*cos;
			
			return -force*arm/b;
		}
		/**
		 * 
		 * @param aspectRatio wing aspect ratio
		 * @param lambdaLE wing sweep angle at leading edge
		 * @param mach mach number
		 * @param taperRatio taper ratio of wing (chord tip over chord root)
		 * @return contribution of wing to lateral force due  to the roll rate 
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 181 		
		 *  */
		
		public static double calculateCLRollRateWing(double aspectRatio, double lambdaLE, double mach, double taperRatio) {
			
			double beta = Math.sqrt((1-mach*mach));
			
			double k = calculateKappaDeltaAirelons(aspectRatio, lambdaLE, mach, taperRatio);
			
			double lambdabeta = 57.3*calculateLambdaBeta(lambdaLE, mach, taperRatio, aspectRatio, 0.25);
			
			double RDP = DatabaseReader.get_RDP_vs_lambdaBeta_taperRatio_betaAROverKappa(lambdabeta, taperRatio, beta, k, aspectRatio);

			
			System.out.println("\n lambdabeta = "+ lambdabeta+"\nbeta = "+beta+"\n kappa = "+k+"\n RDP = "+RDP);
			
			return RDP*k/beta;
			
		}
		/**
		 * 
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord		 
		 * @param ar wing aspect ratio
   		 * @param zV vertical distances of the point of application of lateral force of vertical tail
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param b wing span
		 * @param alfa angle of attack
		 * @return contribution of vertical tail to lateral force due  to the roll rate 
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 143-181 
		 */
		public static double calculateCYRollPitch(double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,double rOne, double ar , double zV, double xV, double b , double alfa){
			
			double cYBeta = calculateCYBetaVT(clalfa, s, sv, lambda, d, zW, spanVT,rOne, ar);
			
			return 2*cYBeta*(zV*Math.cos(alfa)-xV*Math.sin(alfa))/b;
			
		}
		
		
		
		
		/**
		 * 
		 * @param aspectRatio horizontal tail aspect ratio
		 * @param lambdaLE horizontal tail sweep angle at leading edge
		 * @param mach mach number
		 * @param taperRatio wing taper ratio
		 * @param sh horizontal tail surface
		 * @param s wing surface
		 * @param bh horizontal tail span
		 * @param b wing span
		 * @return contribution of horizontal tail to the rolling  moment due to the roll rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 143-182 		 
		 */
		public static double calculateCLHTRollPitch(double aspectRatio, double lambdaLE, double mach, double taperRatio, double rdp, double sh,double s, double bh, double b){
		
		double a = calculateCLRollRateWing(aspectRatio, lambdaLE, mach, taperRatio);
		
		return 0.5*a*(sh/s)*Math.pow(bh/b, 2);
		
		}
		
		
		/**
		 * 
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord		
		 * @param ar wing aspect ratio
   		 * @param zV vertical distances of the point of application of lateral force of vertical tail
		 * @param b wing span
         * @return contribution of vertical tail to the rolling  moment due to the roll rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 143-183 				
		 */
		public static double calculateCLRollPitchVT(double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,double rOne, double ar, double zV, double b ){
			
			double a = calculateCYBetaVT(clalfa, s, sv, lambda, d, zW, spanVT,rOne, ar );
			System.out.println("\n Cy_beta_VT = " + a);
			return 2*a*Math.pow(zV/b, 2);
			
		}

		/**
		 * 
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord		 
		 * @param ar wing aspect ratio
   		 * @param zV vertical distances of the point of application of lateral force of vertical tail
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param alfa angle of attack
		 * @param b wing span 
		 * @return contribution of vertical tail to the lateral force due to the yaw rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page 143-185 	 
		 */
		public static double calculateCYYawRateVT (double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,double rOne, double ar, double zV,double xV,double alfa, double b ) {
			
			double force = calculateCYBetaVT(clalfa, s, sv, lambda, d, zW, spanVT,rOne, ar );
			
			double cos = Math.cos(alfa);
			
			double sin = Math.sin(alfa);
			
			double arm = xV*cos+zV*sin;
			
			return -2*force*arm/b;
		}
		
		/**
		 * 
		 * @param ar wing aspect ratio
		 * @param mach mach number
		 * @param lambdaquarter sweep angle at 25 percent of the mean aerodynamics cords
		 * @return D coefficient used to calculate wing body contribution to the rolling moment due to the yaw rate (Mach influence)
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 185 
		 */
		
		public static double calculateCoefficientD(double ar, double mach, double lambdaquarter){
			
			double beta = calculateCompressibilityFactorSweepAngle(mach, lambdaquarter);
			
			double tan = Math.tan(lambdaquarter);
			
			double powtan = Math.pow(tan, 2)/8;
			
			double a = (ar*(1-beta*beta))/(2*beta*(ar*beta+2*Math.cos(lambdaquarter)));
	
			double b = (ar*beta + 2*Math.cos(lambdaquarter))*powtan/(ar*beta + 4*Math.cos(lambdaquarter));
			
			double c = (ar + 2*Math.cos(lambdaquarter))*powtan/(ar + 4*Math.cos(lambdaquarter));
			
			return (1. + a + b )/(1. + c);
			
		}
		
		/**
		 * 
		 * @param ar wing aspect ratio
		 * @param mach mach number
		 * @param lambdaquarter sweep angle at 25 percent of the mean aerodynamics cords
		 * @param taperRato wing taper ratio (chord tip over chord root)
		 * @return Effect of mach number on wing body contribution to the rolling moment due to the yaw rate 
 		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 185 - 186 
		 */
		public static double calculateCLYawRateMachInfluence(double ar, double mach, double lambdaquarter, double taperRatio){
			
			double D = calculateCoefficientD(ar, mach, lambdaquarter);
			
			double clr = DatabaseReader.get_Clr_over_Cl_Mach0_cl0_vs_AR_TaperRatio_LambdaQuarter(ar, taperRatio, lambdaquarter);
			
			System.out.println("\n Coefficient D = " + D+"\n clr/cL|Mach=0,CL1 = "+ clr);
			
			return clr*D;
			
		} 
		/**
		 * 
		 * @param ar wing aspect ratio
		 * @param lambdaquarter wing sweep angle at 25 percent of the mean aerodynamics cords
		 * @return Effect of wing dihedral angle on wing body contribution to the rolling moment due to the yaw rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 186 
		 */
		public static double calculateCLYawRateDiheralEffect(double  ar, double lambdaquarter)
		{
			
			double A = Math.PI*ar*Math.sin(lambdaquarter);
			
			double B = ar + 4*Math.cos(lambdaquarter);
			
			double C = B*12.0;
			
			return A/C;
			
		}
		
		//public static double calculateCLYawRateTwistAngleEffect(double  ar, double TaperRatio)
		
		/**
		 * 
		 * @param ar wing aspect ratio
		 * @param mach mach number
		 * @param lambdaquarter sweep angle at 25 percent of the mean aerodynamics cords
		 * @param twistAngle Twist angle at wing tip
		 * @param taperRatio wing taper ratio (chord tip over chord root)
		 * @param clWing lift coefficient
		 * @param gamma  wing dihedral angle
		 * @return wing body contribution to the rolling moment due to the yaw rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 185 - 186 
		 */
		public static double calculateCLYawRateWingTotal(double ar, double mach, double lambdaquarter, double twistAngle, double taperRatio, double clWing, double gamma){
			
			double A = gamma*calculateCLYawRateDiheralEffect(ar,lambdaquarter);
			
			System.out.println("\n CLYawRateDiheralEffect = "+ A);
			
			double B = clWing*calculateCLYawRateMachInfluence(ar, mach, lambdaquarter, taperRatio);
			
			System.out.println("\n CLYawRateMachInfluence = "+ B/clWing);

			double deltaClR = DatabaseReader.get_deltaClROverTwist_vs_ar_taperRatio(ar, taperRatio);
			
			System.out.println("\n DeltaClR = "+deltaClR );
			
			double C = twistAngle*deltaClR ;
			
			return A + B + C ; 
			
		}
		/**
		 * 
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord		 
		 * @param ar wing aspect ratio
         * @param b wing span 
	     * @param alfaone angle of attack
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param zV vertical distances of the point of application of lateral force of vertical tail
		 * @return vertical tail contribution to the rolling moment due to the yaw rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 187-143 
		 */
		public static double calculateCLYawRateVerticalTail (double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,Double rOne, double ar , double b, double alfaOne, double xV, double zV ){
			
			double cyV = calculateCYBetaVT(clalfa, s, sv, lambda, d, zW, spanVT,rOne, ar );
			
			double cos = Math.cos(alfaOne);
			
			double sin = Math.sin(alfaOne);
			
			return -2*cyV*(xV*cos + zV*sin)*(zV*cos - xV*sin)/(Math.pow(b, 2));
		}
		/**
		 * 
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param lambdaquarter sweep angle at 25 percent of the mean aerodynamics cords
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord		 
		 * @param ar wing aspect ratio
	     * @param gamma  wing dihedral angle
         * @param b wing span 
	     * @param alfaone angle of attack
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param zV vertical distances of the point of application of lateral force of vertical tail
		 * @param mach mach number
		 * @param twistAngle Twist angle at wing tip
		 * @param taperRatio wing taper rat (chord tip over chord root)
		 * @param clWing lift coefficient
		 * @return rolling moment coefficient due to the yaw rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 185-186-187-143
		 */
		public static double calculateCLYawRateTotal (double clalfaVT, double s, double sv, double lambdaquarterWing, double d, double zW,double spanVT,double rOne, double ar, double gamma , double b, double alfaOne, double xV, double zV, double mach, double twistAngleWing, double taperRatio, double clWing ){
			
			double A = calculateCLYawRateWingTotal(ar,mach,lambdaquarterWing,twistAngleWing,taperRatio,clWing,gamma);
			
			double B = calculateCLYawRateVerticalTail (clalfaVT,s,sv,lambdaquarterWing,d,zW,spanVT, rOne,ar ,b,alfaOne,xV,zV );
			  
			System.out.println("\n CL_YAWRateW  = " + A + "\n CL_YawRateVT  = " + B  );
		      
			System.out.println("---------------------------------------------------\n");
			
			return A + B ;
			
		}
		
		/**
		 * 
		 * @param ar wing aspect ratio
		 * @param mach mach number 
		 * @param lambdaquarter sweep angle at quarter of chord of mean aerodynamics chord of wing
		 * @return C coefficient used to calculate yawing moment due to the roll rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 183
		 */
		public static double calculateCoefficientCRollRate(double ar, double mach, double lambdaquarter ) {
			
			double b = calculateCompressibilityFactorSweepAngle(mach, lambdaquarter);
			
			double a = 4*Math.cos(lambdaquarter);
			
			double tan = Math.tan(lambdaquarter);

			double c = Math.pow(tan, 2);
			
			double d = ar + a;
			
			double e = ar*b + a;
			
			double firstmember = d/e;
			
			double secondmember = (ar*b + 0.5*e*c)/(ar + 0.5*(d*c));
			
			return firstmember*secondmember;
		}
		
		/**
		 * 
		 * @param ar wing aspect ratio
		 * @param lambdaquarter sweep angle at quarter of chord of mean aerodynamics chord of wing
		 * @param xgc center of gravity expressed as a percentage of the mean aerodynamics chord
	     * @param xacw aerodynamic center of wing-body expressed as a percentage of the mean aerodynamics chord
		 * @return coefficient used to calculate CN_p Over CL Coefficient 
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 183 
		 */
		public static double calculateCNpOverCLCoefficient(double ar, double lambdaquarter, double xcg, double xacw ) {
			
			double cos = Math.cos(lambdaquarter);
			
			double tan = Math.tan(lambdaquarter);
			
			double pow = Math.pow(tan,2);
			
			double a = ar + cos;
			
			double denominator = 6*a;
			
			double numerator = ar + 6*a*((xcg-xacw)*(tan/ar)+(pow/(12.0)));
			
			return -numerator/denominator;
			
		}
		
		/**
		 * 
		 * @param ar wing aspect ratio
		 * @param lambdaquarter sweep angle at quarter of chord of mean aerodynamics chord of wing
		 * @param xgc center of gravity expressed as a percentage of the mean aerodynamics chord
	     * @param xacw aerodynamic center of wing-body expressed as a percentage of the mean aerodynamics chord
		 * @param mach mach number
		 * @param cl lift coefficient
         * @return wing-body contribution to yawing moment due to the roll rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 183 	
		 */
		public static double  calculateCNRollRateWB(double ar, double lambdaquarter, double xcg, double xacw, double mach, double cl,double taperRatio,double twist) {
			
			double a = calculateCNpOverCLCoefficient(ar, lambdaquarter, xcg, xacw);
			
			System.out.println("\n CNpOverCL = "+ a);
			double b = calculateCoefficientCRollRate(ar, mach, lambdaquarter);
			System.out.println("\n CoefficientCRollRate = "+ b);
			
			double delta = DatabaseReader.get_DeltaCnpOverTwist(ar, taperRatio);
			
			System.out.println("Delta = "+delta+" twist = "+twist);
			
			return a*b*cl+delta*twist;
		}
		
		/**
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord		 
		 * @param ar wing aspect ratio
	     * @param gamma  wing dihedral angle
         * @param b wing span 
	     * @param alfaone angle of attack
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param zV vertical distances of the point of application of lateral force of vertical tail
         * @return vertical tail contribution to yawing moment due to the roll rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 184
		 */
		public static double calculateCNRollRateVerticalTail (double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,double rOne, double ar , double b, double alfaOne, double xV, double zV ){
			
			double cyV = calculateCYBetaVT(clalfa, s, sv, lambda, d, zW, spanVT,rOne, ar );

			double cos = Math.cos(alfaOne);
			
			double sin = Math.sin(alfaOne);
			
			double a = xV*cos;
			
			double c = zV*sin;
			
			double e = zV*cos;
			
			double f = xV*sin;
			
			double A = (a + c)/b ;
			
			double B = (e - f - zV)/b;
			
			System.out.println("cyV = "+cyV);
					
			return -2*cyV*A*B;
		
		}
		/**
		 * 
		 * @param taperRatio wing taper ratio (chord tip over chord root)
		 * @param cL lift coefficient
		 * @param ar wing aspect ratio
		 * @param lambdaquarter sweep angle at quarter of chord of wing
		 * @param xac distances beetwen aerodynamic center and leading edge of mean aerodynamic chord 
		 * @param xcg distances beetwen gravity center and leading edge of mean aerodynamic chord
		 * @return wing-body contribution to yawing moment due to yaw rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 187-188
		 */
		public static double calculateCNYawRateWingBody(double taperRatio, double cL,double ar, double lambdaquarter, double xac, double xcg, double cdo) {
			
			double a = Math.pow(cL, 2) ;
			
			double cnrOverCdo = DatabaseReader.get_Cnr_over_Cdo_vs_AR_and_LambdaQuarter_arm(ar, lambdaquarter, xac, xcg);
			
			double cNROverCL = DatabaseReader.get_Cnr_over_Cl_Square_vs_lambda_cl0_vs_AR_TaperRatio_LambdaQuarter(ar, taperRatio, lambdaquarter, xac, xcg);
			
			System.out.println("\n cnrOverCdo = " + cnrOverCdo+"\n cNROverCL = "+cNROverCL+
					"\n cNROverCL*a = "+cNROverCL*a);
			
			return cNROverCL*a + cnrOverCdo*cdo;
			
		}
		
		/**
		 * 
		 * @param clalfa vertical tail lift-scope gradient
		 * @param s wing surface
		 * @param sh horizontal tail surface
		 * @param lambda sweep angle at quarter of chord of wing
		 * @param d maximum fuselage height
		 * @param zW vertical distance from center of gravity of 25 percent of the wings root
		 * @param spanVT vertical tail span
		 * @param rOne Fuselage height at quarter vertical tail root chord		 
		 * @param ar wing aspect ratio
	     * @param gamma  wing dihedral angle
         * @param b wing span 
	     * @param alfaone angle of attack
		 * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		 * @param zV vertical distances of the point of application of lateral force of vertical tail
         * @return vertical tail contribution to yawing moment due to the yaw rate
		 * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 188-143
		 */
		public static double calculateCNYawRateVerticalTail (double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,double rOne, double ar , double b, double alfaOne, double xV, double zV ){
			
			double cyV = calculateCYBetaVT(clalfa, s, sv, lambda, d, zW, spanVT,rOne, ar );

			System.out.println("\n CYBetaVT = " + cyV);
			double cos = xV*Math.cos(alfaOne);
			
			double sin = zV*Math.sin(alfaOne);
			
			double A = (cos + sin)/b ;
			
			double B = Math.pow(A, 2);
			
			return 2*cyV*B;
		
		}
		 /**
		  * 
		  * @param taperRatio wing taper ratio (chord tip over chord root)
		  * @param cL lift coefficient
		  * @param xac distances beetwen aerodynamic center and leading edge of mean aerodynamic chord 
		  * @param xcg distances beetwen gravity center and leading edge of mean aerodynamic chord
		  * @param lambdaquarter sweep angle at quarter of chord of wing
		  * @param cd0 drug coefficient at zero lift condition
		  * @param clalfa vertical tail lift-scope gradient
		  * @param s wing surface
		  * @param sh vertical tail surface
		  * @param lambdaquarter sweep angle at quarter of chord of Vertical tail
		  * @param d maximum fuselage height
		  * @param zW vertical distance from center of gravity of 25 percent of the wings root
		  * @param spanVT vertical tail span
		  * @param rOne Fuselage height at quarter vertical tail root chord		  
		  * @param ar wing aspect ratio
          * @param b wing span 
	      * @param alfaone angle of attack
		  * @param xV longitudinal distances of the point of application of lateral force of vertical tail
		  * @param zV vertical distances of the point of application of lateral force of vertical tail
          * @return  yawing moment coefficient due to the yaw rate
		  * @see Napolitano_Aircraft_Dynamics_Wiley_2012 page from 143-187-188
		  */
		 
		public static double calculateCNYawRateTotal(double taperRatio, double cL, double xac, double xcg,double lambdawing, double cdo,double clalfa, double s, double sv, double lambda, double d, double zW,double spanVT,double rOne, double ar , double b, double alfaOne, double xV, double zV) {
			
			double cNWB =  calculateCNYawRateWingBody(taperRatio,cL,ar,lambdawing,xac,xcg,cdo);
			
			double cNVT = calculateCNYawRateVerticalTail (clalfa, s, sv, lambda, d, zW, spanVT,rOne, ar , b, alfaOne, xV, zV );
			
			System.out.println("\n CN_YAWRateWB  = " + cNWB + "\n CN_YawRateVT  = " + cNVT);
		      
			System.out.println("---------------------------------------------------\n");

			
			return cNVT + cNWB;
		}
		
		public static double calculateAilerons(double product, double taperRatio, double lambdabeta,double etaInner, double etaOuter){
			
		
	        
			double rME = DatabaseReader.get_RME1( product,  taperRatio,  lambdabeta, etaInner,  etaOuter);
			
			System.out.println("\n RME = "+rME+"\n k = "+"\n lambdabeta = "+lambdabeta);
			
			return rME;
		
		}
		
}
