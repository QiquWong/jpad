package sandbox.vt.HighLift_Test;

import java.util.ArrayList;
import java.util.List;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.Aircraft;
import configuration.enumerations.FlapTypeEnum;

/**
 * This class calculate high lift devices effects upon a wing in terms of CL and CD. To do
 * this, the calculation starts, at first, from the airfoil by evaluating DeltaCl0 and DClmax
 * for flaps and slats; from them, wing high lift devices effects are evaluated by calculating
 * DeltaCL0, DeltaCLmax for flaps and slats and the new CLalpha.  
 * Last but not least is the drag coefficient variation due to high lift devices which calculation
 * is made upon a semi-empirical formula.
 * 
 * @author Vittorio Trifari
 *
 */
public class CalcHighLiftDevices {
	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	private Aircraft aircraft;
	private MyAirfoil meanAirfoil;
	private List<Double[]> deltaFlap; 	    
	private List<Double> flapType_index, deltaSlat, eta_in_flap, eta_out_flap, 
				 eta_in_slat, eta_out_slat, cf_c, cs_c, leRadius_c_slat, cExt_c_slat;
	private final List<Double> deltaFlap_ref;
	private List<FlapTypeEnum> flapType;
	
	//to evaluate:
	private double deltaCl0_flap = 0,
			deltaCL0_flap = 0,
			deltaClmax_flap = 0,
			deltaCLmax_flap = 0,
			deltaClmax_slat = 0,
			deltaCLmax_slat = 0,
			cLalpha_new = 0,
			deltaAlphaMax = 0,
			deltaCD = 0,
			deltaCM_c4 = 0;
	private ArrayList<Double> deltaCl0_flap_list,
			deltaCL0_flap_list,
			deltaClmax_flap_list,
			deltaCLmax_flap_list,
			deltaClmax_slat_list,
			deltaCLmax_slat_list,
			cLalpha_new_list,
			deltaAlphaMax_list,
			deltaCD_list,
			deltaCM_c4_list;

	
	//-------------------------------------------------------------------------------------
	// BUILDER:
	
	public CalcHighLiftDevices(
			Aircraft aircraft,
			List<Double[]> deltaFlap,
			List<FlapTypeEnum> flapType,
			List<Double> deltaSlat,
			List<Double> eta_in_flap,
			List<Double> eta_out_flap,
			List<Double> eta_in_slat,
			List<Double> eta_out_slat, 
			List<Double> cf_c,
			List<Double> cs_c,
			List<Double> leRadius_slat,
			List<Double> cExt_c_slat
			) {
		
		this.aircraft = aircraft;
		this.meanAirfoil = aircraft.get_wing()
				.getAerodynamics()
				.new MeanAirfoil()
				.calculateMeanAirfoil(
						aircraft.get_wing()
						);
		this.deltaFlap = deltaFlap;
		this.flapType = flapType;
		this.deltaSlat = deltaSlat;
		this.eta_in_flap = eta_in_flap;
		this.eta_out_flap = eta_out_flap;
		this.eta_in_slat = eta_in_slat;
		this.eta_out_slat = eta_out_slat;
		this.cf_c = cf_c;
		this.cs_c = cs_c;
		this.leRadius_c_slat = leRadius_slat;
		this.cExt_c_slat = cExt_c_slat;
		
		flapType_index = new ArrayList<Double>();
		deltaFlap_ref = new ArrayList<Double>();
		
		for(int i=0; i<flapType.size(); i++) {
			if(flapType.get(i) == FlapTypeEnum.SINGLE_SLOTTED) {
				flapType_index.add(1.0);
				deltaFlap_ref.add(45.0);
			}
			else if(flapType.get(i) == FlapTypeEnum.DOUBLE_SLOTTED) {
				flapType_index.add(2.0);
				deltaFlap_ref.add(50.0);
			}
			else if(flapType.get(i) == FlapTypeEnum.PLAIN) {
				flapType_index.add(3.0);
				deltaFlap_ref.add(60.0);
			}
			else if(flapType.get(i) == FlapTypeEnum.FOWLER) {
				flapType_index.add(4.0);
				deltaFlap_ref.add(40.0);
			}
			else if(flapType.get(i) == FlapTypeEnum.TRIPLE_SLOTTED) {
				flapType_index.add(5.0);
				deltaFlap_ref.add(50.0);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------
	// METHODS:
	
	/**
	 * This method calculate high lift devices effects on lift coefficient curve of the 
	 * airfoil and wing throughout semi-empirical formulas; in particular DeltaCl0, DeltaCL0
	 * DeltaCLmax and DeltaClmax are calculated for flaps when only DeltaClmax and DeltaCLmax
	 * are calculated for slats. Moreover an evaluation of new CLapha slope and CD are performed
	 * for the wing. 
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateHighLiftDevicesEffects() {
		
		//--------------------------------------------
		// initialization of flap type map
		
		//---------------------------------------------
		// deltaCl0 (flap)
		List<Double> theta_f = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++) 
			theta_f.add(Math.acos((2*cf_c.get(i))-1));
		
		List<Double> alphaDelta = new ArrayList<Double>();
		for(int i=0; i<theta_f.size(); i++)
			alphaDelta.add(1-((theta_f.get(i)-Math.sin(theta_f.get(i)))/Math.PI));
		
		Double[] deltaFlap_total = new Double[flapType_index.size()];
		for(int i=0; i<deltaFlap.size(); i++) {
			deltaFlap_total[i] = 0.0;
			for(int j=0; j<deltaFlap.get(i).length; j++) {
				deltaFlap_total[i] += deltaFlap.get(i)[j];
			}
		}
		
		List<Double> eta_delta_flap = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++) {
			if(flapType_index.get(i) == 3.0)
				eta_delta_flap.add(
						aircraft
						.get_theAerodynamics()
						.get_highLiftDatabaseReader()
						.get_eta_delta_vs_delta_flap_plain(deltaFlap_total[i], cf_c.get(i)));
			else
				eta_delta_flap.add(
						aircraft
						.get_theAerodynamics()
						.get_highLiftDatabaseReader()
						.get_eta_delta_vs_delta_flap(deltaFlap_total[i], flapType_index.get(i))
						);
		}
		
		List<Double> deltaCl0_first = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaCl0_first.add(
					alphaDelta.get(i).doubleValue()
					*eta_delta_flap.get(i).doubleValue()
					*deltaFlap_total[i]
					*meanAirfoil.getAerodynamics().get_clAlpha()*(Math.PI/180)
					);
		
		List<Double> deltaC_Cf_flap = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaC_Cf_flap.add(
					aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_deltaC_Cf_vs_delta_flap(deltaFlap_total[i],flapType_index.get(i))
					);
		
		List<Double> c_first_c_flap = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			c_first_c_flap.add(1+(deltaC_Cf_flap.get(i).doubleValue()*cf_c.get(i).doubleValue()));
		
		deltaCl0_flap_list = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaCl0_flap_list.add(
					(deltaCl0_first.get(i).doubleValue()*c_first_c_flap.get(i).doubleValue())
					+(meanAirfoil.getAerodynamics().calculateClAtAlpha(0.0)*(c_first_c_flap.get(i).doubleValue()-1))
					);
		
		for(int i=0; i<flapType_index.size(); i++)
			deltaCl0_flap += deltaCl0_flap_list.get(i).doubleValue();
		
		//---------------------------------------------------------------
		// deltaClmax (flap)
		List<Double> deltaClmax_base = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaClmax_base.add(
					aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_deltaCLmaxBase_vs_tc(
							meanAirfoil.getGeometry().get_maximumThicknessOverChord(),
							flapType_index.get(i)
							)
					);
		
		List<Double> k1 = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			k1.add(aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_K1_vs_flapChordRatio(cf_c.get(i), flapType_index.get(i))
					);
		

		List<Double> k2 = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			k2.add(aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_K2_vs_delta_flap(deltaFlap_total[i], flapType_index.get(i))
					);
		
		List<Double> k3 = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			k3.add(aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_K3_vs_df_dfRef(
							deltaFlap_total[i],
							deltaFlap_ref.get(i),
							flapType_index.get(i)
							)
					);
		
		deltaClmax_flap_list = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaClmax_flap_list.add(k1.get(i).doubleValue()
					*k2.get(i).doubleValue()
					*k3.get(i).doubleValue()
					*deltaClmax_base.get(i).doubleValue()
					);
		
		for(int i=0; i<flapType_index.size(); i++)
			deltaClmax_flap += deltaClmax_flap_list.get(i).doubleValue();
		
		//---------------------------------------------------------------
		// deltaClmax (slat)
		if(deltaSlat != null) {
			
			List<Double> dCl_dDelta = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				dCl_dDelta.add(aircraft
						.get_theAerodynamics()
						.get_highLiftDatabaseReader()
						.get_dCl_dDelta_vs_cs_c(cs_c.get(i))
						);

			List<Double> eta_max_slat = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				eta_max_slat.add(aircraft
						.get_theAerodynamics()
						.get_highLiftDatabaseReader()
						.get_etaMax_vs_LEradius_tickness_ratio(
								leRadius_c_slat.get(i),
								meanAirfoil.getGeometry().get_maximumThicknessOverChord())
						);

			List<Double> eta_delta_slat = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				eta_delta_slat.add(
						aircraft
						.get_theAerodynamics()
						.get_highLiftDatabaseReader()
						.get_eta_delta_vs_delta_slat(deltaSlat.get(i))
						);

			deltaClmax_slat_list = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				deltaClmax_slat_list.add(
						dCl_dDelta.get(i).doubleValue()
						*eta_max_slat.get(i).doubleValue()
						*eta_delta_slat.get(i).doubleValue()
						*deltaSlat.get(i).doubleValue()
						*cExt_c_slat.get(i).doubleValue()
						);

			for(int i=0; i<deltaSlat.size(); i++)
				deltaClmax_slat += deltaClmax_slat_list.get(i).doubleValue();
			
		}
				
		//---------------------------------------------------------------
		// deltaCL0 (flap)
		List<Double> kc = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			kc.add(aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_Kc_vs_AR(
							aircraft.get_wing().get_aspectRatio(),
							alphaDelta.get(i))	
					);
		
		List<Double> kb = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			kb.add(aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_Kb_vs_flapSpanRatio(eta_in_flap.get(i), eta_out_flap.get(i))	
					);
		
		deltaCL0_flap_list = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaCL0_flap_list.add(
					kb.get(i).doubleValue()
					*kc.get(i).doubleValue()
					*deltaCl0_flap_list.get(i).doubleValue()
					*(aircraft.get_wing().getAerodynamics().getCalculateCLAlpha().integralMean2D())
					/meanAirfoil.getAerodynamics().get_clAlpha()
					);
		
		for(int i=0; i<flapType_index.size(); i++)
			deltaCL0_flap += deltaCL0_flap_list.get(i).doubleValue();
		
		//---------------------------------------------------------------
		// deltaCLmax (flap)
		List<Double> flapSurface = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			flapSurface.add(
					Math.abs(
							 aircraft.get_wing().get_span().getEstimatedValue()							
							 /2*aircraft.get_wing().get_chordRootEquivalentWing().getEstimatedValue()
							 *(2-((1-aircraft.get_wing().get_taperRatioEquivalent())*(eta_in_flap.get(i)-eta_out_flap.get(i))))
							*(eta_in_flap.get(i)-eta_out_flap.get(i))
							)
					);
		
		List<Double> kLambda_flap = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			kLambda_flap.add(
					Math.pow(Math.cos(aircraft.get_wing().get_sweepQuarterChordEq().getEstimatedValue()),0.75)
					*(1-(0.08*Math.pow(Math.cos(aircraft.get_wing().get_sweepQuarterChordEq().getEstimatedValue()), 2)))
					);
		
		deltaCLmax_flap_list = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaCLmax_flap_list.add(deltaClmax_flap_list.get(i)
					*(flapSurface.get(i)/aircraft.get_wing().get_surface().getEstimatedValue())
					*kLambda_flap.get(i)
					);
		
		for(int i=0; i<flapType_index.size(); i++)
			deltaCLmax_flap += deltaCLmax_flap_list.get(i).doubleValue();
		
		//---------------------------------------------------------------
		// deltaCLmax (slat)
		if(deltaSlat != null) {
			
			List<Double> kLambda_slat = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				kLambda_slat.add(
						Math.pow(Math.cos(aircraft.get_wing().get_sweepQuarterChordEq().getEstimatedValue()),0.75)
						*(1-(0.08*Math.pow(Math.cos(aircraft.get_wing().get_sweepQuarterChordEq().getEstimatedValue()), 2)))
						);
			
			List<Double> slatSurface = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				slatSurface.add(
						Math.abs(aircraft.get_wing().get_span().getEstimatedValue()
								/2*aircraft.get_wing().get_chordRootEquivalentWing().getEstimatedValue()
								*(2-(1-aircraft.get_wing().get_taperRatioEquivalent())*(eta_in_slat.get(i)-eta_out_slat.get(i)))
								*(eta_in_slat.get(i)-eta_out_slat.get(i))
								)
						);

			deltaCLmax_slat_list = new ArrayList<Double>();
			for(int i=0; i<deltaSlat.size(); i++)
				deltaCLmax_slat_list.add(deltaClmax_slat_list.get(i)
						*(slatSurface.get(i)/aircraft.get_wing().get_surface().getEstimatedValue())
						*kLambda_slat.get(i)
						);

			for(int i=0; i<deltaSlat.size(); i++)
				deltaCLmax_slat += deltaCLmax_slat_list.get(i).doubleValue();

		}
		//---------------------------------------------------------------
		// new CLalpha
		cLalpha_new_list = new ArrayList<Double>();
		List<Double> swf_s = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++) {
			cLalpha_new_list.add(
					aircraft.get_wing().getAerodynamics().getCalculateCLAlpha().integralMean2D()*(Math.PI/180)
					*(1+((deltaCL0_flap_list.get(i)/deltaCl0_flap_list.get(i))
							*(c_first_c_flap.get(i)*(1-((cf_c.get(i))*(c_first_c_flap.get(i))
									*Math.pow(Math.sin(deltaFlap_total[i]*Math.PI/180), 2)))-1))));
			swf_s.add(flapSurface.get(i)/aircraft.get_wing().get_surface().getEstimatedValue());
		}
		
		double swf_s_tot = 0;
		for(int i=0; i<swf_s.size(); i++)
			swf_s_tot += swf_s.get(i);
		
		for(int i=0; i<flapType_index.size(); i++)
			cLalpha_new += cLalpha_new_list.get(i)*swf_s.get(i);
		
		cLalpha_new /= swf_s_tot;
		
		//---------------------------------------------------------------
		// Delta alpha max (flap)
		deltaAlphaMax_list = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaAlphaMax_list.add(aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_DeltaAlphaMax_vs_DeltaFlap(deltaFlap_total[i]));

		for(int i=0; i<flapType_index.size(); i++)
			setDeltaAlphaMax(getDeltaAlphaMax() + deltaAlphaMax_list.get(i).doubleValue());
				
		//---------------------------------------------------------------
		// deltaCD
		deltaCD_list = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			deltaCD_list.add(
					0.9
					*(Math.pow(cf_c.get(i), 1.38))
					*(flapSurface.get(i)/aircraft.get_wing().get_surface().getEstimatedValue())
					*(Math.pow(Math.sin(deltaFlap_total[i]), 2))
					);
		
		for(int i=0; i<flapType_index.size(); i++)
			deltaCD += deltaCD_list.get(i).doubleValue();	
		
		//---------------------------------------------------------------
		// deltaCM_c/4
		List<Double> mu_1 = new ArrayList<Double>();
		for (int i=0; i<flapType_index.size(); i++)
			if(flapType_index.get(i) == 3.0)
				mu_1.add(
						aircraft
						.get_theAerodynamics()
						.get_highLiftDatabaseReader()
						.get_mu_1_vs_cf_c_First_Plain(
								1/c_first_c_flap.get(i),
								deltaFlap_total[i]
										)
						);
			else
				mu_1.add(aircraft
						.get_theAerodynamics()
						.get_highLiftDatabaseReader()
						.get_mu_1_vs_cf_c_First_Slotted_Fowler(1/c_first_c_flap.get(i))
						);
		
		List<Double> mu_2 = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			mu_2.add(aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_mu_2_vs_bf_b(
							eta_in_flap.get(i),
							eta_out_flap.get(i),
							aircraft.get_wing().get_taperRatioEquivalent()
							)
					);
		
		List<Double> mu_3 = new ArrayList<Double>();
		for(int i=0; i<flapType_index.size(); i++)
			mu_3.add(aircraft
					.get_theAerodynamics()
					.get_highLiftDatabaseReader()
					.get_mu_3_vs_bf_b(
							eta_in_flap.get(i),
							eta_out_flap.get(i),
							aircraft.get_wing().get_taperRatioEquivalent()
							)
					);
		
		// SEE LSAerodynamicManager From MANUELA) --> CalcHighLiftDevices Class
//		deltaCM_c4_list = new ArrayList<Double>();
//		for(int i=0; i<flapType_index.size(); i++)
//			deltaCM_c4_list.add(
//					(mu_2.get(i)*(-(mu_1.get(i)*deltaClmax_flap_list.get(i)*cf_c.get(i)
//							*c_first_c_flap.get(i))-(cf_c.get(i)*c_first_c_flap.get(i)
//									*((cf_c.get(i)*c_first_c_flap.get(i))-1)
//									*(cL + (deltaClmax_flap_list.get(i)
//											*(1-(flapSurface.get(i)/aircraft
//													.get_wing()
//													.get_surface()
//													.getEstimatedValue()))))
//									*(1/8)))) + (0.7*(aircraft
//											.get_wing()
//											.get_aspectRatio()/(1+(aircraft
//													.get_wing()
//													.get_aspectRatio()/2)))
//											*mu_3.get(i)*deltaClmax_flap_list.get(i)
//											*Math.tan(aircraft
//													.get_wing()
//													.get_sweepQuarterChordEq()
//													.getEstimatedValue()))
//					);
//		
//		for(int i=0; i<flapType_index.size(); i++)
//			deltaCM_c4 += deltaCM_c4_list.get(i).doubleValue();	
	}
	
	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:
	
	public ArrayList<Double> getDeltaCl0_flap_list() {
		return deltaCl0_flap_list;
	}
	
	public double getDeltaCl0_flap() {
		return deltaCl0_flap;
	}
	
	public ArrayList<Double> getDeltaCL0_flap_list() {
		return deltaCL0_flap_list;
	}

	public double getDeltaCL0_flap() {
		return deltaCL0_flap;
	}

	public ArrayList<Double> getDeltaClmax_flap_list() {
		return deltaClmax_flap_list;
	}
	
	public double getDeltaClmax_flap() {
		return deltaClmax_flap;
	}
	
	public ArrayList<Double> getDeltaCLmax_flap_list() {
		return deltaCLmax_flap_list;
	}

	public double getDeltaCLmax_flap() {
		return deltaCLmax_flap;
	}
	
	public ArrayList<Double> getDeltaClmax_slat_list() {
		return deltaClmax_slat_list;
	}

	public double getDeltaClmax_slat() {
		return deltaClmax_slat;
	}
	
	public ArrayList<Double> getDeltaCLmax_slat_list() {
		return deltaCLmax_slat_list;
	}

	public double getDeltaCLmax_slat() {
		return deltaCLmax_slat;
	}
	
	public ArrayList<Double> getcLalpha_new_list() {
		return cLalpha_new_list;
	}

	public double getcLalpha_new() {
		return cLalpha_new;
	}
	
	public ArrayList<Double> getDeltaAlphaMax_list() {
		return deltaAlphaMax_list;
	}

	public void setDeltaAlphaMax_list(ArrayList<Double> deltaAlphaMax_list) {
		this.deltaAlphaMax_list = deltaAlphaMax_list;
	}

	public double getDeltaAlphaMax() {
		return deltaAlphaMax;
	}

	public void setDeltaAlphaMax(double deltaAlphaMax) {
		this.deltaAlphaMax = deltaAlphaMax;
	}

	public ArrayList<Double> getDeltaCD_list() {
		return deltaCD_list;
	}

	public double getDeltaCD() {
		return deltaCD;
	}

	public List<Double[]> getDeltaFlap() {
		return deltaFlap;
	}

	public void setDeltaFlap(List<Double[]> deltaFlap) {
		this.deltaFlap = deltaFlap;
	}

	public List<Double> getFlapType() {
		return flapType_index;
	}

	public void setFlapType(List<Double> flapType) {
		this.flapType_index = flapType;
	}

	public List<Double> getDeltaSlat() {
		return deltaSlat;
	}

	public void setDeltaSlat(List<Double> deltaSlat) {
		this.deltaSlat = deltaSlat;
	}

	public List<Double> getEta_in_flap() {
		return eta_in_flap;
	}

	public void setEta_in_flap(List<Double> eta_in_flap) {
		this.eta_in_flap = eta_in_flap;
	}

	public List<Double> getEta_out_flap() {
		return eta_out_flap;
	}

	public void setEta_out_flap(List<Double> eta_out_flap) {
		this.eta_out_flap = eta_out_flap;
	}

	public List<Double> getEta_in_slat() {
		return eta_in_slat;
	}

	public void setEta_in_slat(List<Double> eta_in_slat) {
		this.eta_in_slat = eta_in_slat;
	}

	public List<Double> getEta_out_slat() {
		return eta_out_slat;
	}

	public void setEta_out_slat(List<Double> eta_out_slat) {
		this.eta_out_slat = eta_out_slat;
	}

	public List<Double> getCf_c() {
		return cf_c;
	}

	public void setCf_c(List<Double> cf_c) {
		this.cf_c = cf_c;
	}

	public List<Double> getCs_c() {
		return cs_c;
	}

	public void setCs_c(List<Double> cs_c) {
		this.cs_c = cs_c;
	}

	public List<Double> getLeRadius_c_slat() {
		return leRadius_c_slat;
	}

	public void setLeRadius_c_slat(List<Double> leRadius_c_slat) {
		this.leRadius_c_slat = leRadius_c_slat;
	}

	public List<Double> getcExt_c_slat() {
		return cExt_c_slat;
	}

	public void setcExt_c_slat(List<Double> cExt_c_slat) {
		this.cExt_c_slat = cExt_c_slat;
	}
}