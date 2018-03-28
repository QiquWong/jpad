/*
*   NACA64Series -- An arbitrary NACA 64 series airfoil.
*
*   Copyright (C) 2010-2012 by Joseph A. Huwaldt
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public
*   License as published by the Free Software Foundation; either
*   version 2.1 of the License, or (at your option) any later version.
*   
*   This library is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*   Lesser General Public License for more details.
*
*   You should have received a copy of the GNU Lesser General Public License
*   along with this program; if not, write to the Free Software
*   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*   Or visit:  http://www.gnu.org/licenses/lgpl.html
**/
package standaloneutils.aerotools.aero.airfoils;

import java.util.List;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
*  <p> This class represents an arbitrary NACA 64 series
*      airfoil section such as a NACA 64-020 airfoil.
*  </p>
*
*  <p> Ported from FORTRAN "NACA6.FOR" to Java by:
*                Joseph A. Huwaldt, June 4, 2010     </p>
*
*  <p> Original FORTRAN "NACA4" code had the following note:  </p>
*
*  <pre>
*         AUTHORS - Charles L.Ladson and Cuyler W. Brooks, NASA Langley
*                   Liam Hardy, NASA Ames
*                   Ralph Carmichael, Public Domain Aeronautical Software
*         Last FORTRAN version:  23Nov96  2.0   RLC
*
*         NOTES - This program has also been known as LADSON and SIXSERIES and
*                 as SIXSERIE on systems with a 8-letter name limit.
*         REFERENCES-  NASA Technical Memorandum TM X-3069 (September, 1974),
*                      by Charles L. Ladson and Cuyler W. Brooks, Jr., NASA Langley Research Center.
*
*                      "Theory of Wing Sections", by Ira Abbott and Albert Von Doenhoff.
*  </pre>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  June 5, 2010
*  @version September 15, 2012
**/
public class NACA64Series extends NACA6Series {
	
	//-----------------------------------------------------------------------------------
	
	/**
	*  Create a NACA 64 series airfoil with the specified parameters.
	*
	*  @param  CLi        Design lift coefficient (e.g.: 64-206 has CLi = 0.2).
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.20 ==> 20% t/c).
	*  @param  length     The chord length.
	**/
	public NACA64Series(double CLi, double thickness, double length) {
		super(CLi, thickness, length);
	}
	
	/**
	*  Returns a String that represents the profile type of this airfoil.
	**/
    @Override
	public String getProfile() {
		return "64";
	}
	
	//	Phi & eps vectors for 64 series airfoil.
    private static final double[] philde = {
    	0.,.01568,.03136,
		.04705,.06274,.07843,.09414,.10985,
		.12558,.14132,.15708,.17274,
		.18842,.20411,.21981,.23552,.25124,
		.26697,.2827,.29843,.31416,
		.32987,.34558,.36129,.377,.39271,
		.40842,.42412,.43983,.45553,
		.47124,.48695,.50266,.51837,.53408,
		.54979,.56549,.5812,.59691,
		.61262,.62832,.64403,.65975,.67546,
		.69117,.70688,.72258,.73829,
		.75399,.7697,.7854,.80111,.81683,
		.83254,.84825,.86396,.87967,
		.89537,.91108,.92678,.94248,.9582,
		.97391,.98962,1.00533,1.02104,
		1.03675,1.05245,1.06816,1.08386,
		1.09956,1.11528,1.13099,1.14671,
		1.16242,1.17813,1.19384,1.20954,
		1.22524,1.24094,1.25664,1.27236,
		1.28807,1.30378,1.31949,1.3352,
		1.35091,1.36661,1.38232,1.39802,
		1.41372,1.42942,1.44512,1.46083,
		1.47653,1.49223,1.50794,1.52365,
		1.53936,1.55508,1.5708,1.58649,
		1.60218,1.61788,1.63358,1.64929,1.665,
		1.68072,1.69643,1.71216,1.72788,
		1.74358,1.75928,1.77498,1.79068,
		1.80639,1.8221,1.83781,1.85353,
		1.86924,1.88496,1.90066,1.91637,
		1.93207,1.94778,1.96349,1.9792,
		1.99491,2.01062,2.02633,2.04204,
		2.05775,2.07346,2.08917,2.10488,
		2.12059,2.1363,2.152,2.16771,2.18341,
		2.19911,2.21483,2.23055,2.24627,
		2.26198,2.2777,2.2934,2.30911,2.32481,
		2.3405,2.35619,2.37192,2.38765,
		2.40337,2.41909,2.4348,2.45051,
		2.46621,2.4819,2.49759,2.51327,
		2.52901,2.54474,2.56047,2.57619,
		2.5919,2.6076,2.6233,2.63899,2.65467,
		2.67035,2.68609,2.70182,2.71755,
		2.73327,2.74898,2.76468,2.78038,
		2.79607,2.81175,2.82743,2.84316,
		2.85889,2.87461,2.89032,2.90603,
		2.92173,2.93743,2.95313,2.96882,
		2.98451,3.00023,3.01594,3.03165,
		3.04736,3.06307,3.07878,3.09448,
		3.11018,3.12589,3.14159 };
		
    private static final double[] epslde = {
		0.,.00233,.00464,
		.00692,.00914,.01129,.01336,.01531,
		.01714,.01883,.02035,.02169,
		.02287,.0239,.0248,.02557,.02624,
		.02682,.02731,.02774,.02812,
		.02846,.02877,.02905,.02931,.02957,
		.02982,.03007,.03033,.0306,
		.0309,.03122,.03158,.03196,.03236,
		.0328,.03326,.03375,.03427,
		.03481,.03538,.03598,.0366,.03725,
		.03792,.03862,.03935,.0401,
		.04087,.04167,.0425,.04335,.04423,
		.04512,.04605,.04699,.04796,
		.04896,.04998,.05102,.05208,.05317,
		.05428,.05541,.05657,.05774,
		.05894,.06016,.0614,.06267,.06395,
		.06526,.06658,.06793,.06931,
		.0707,.07213,.07357,.07505,.07655,
		.07808,.07964,.08123,.08284,
		.08447,.08613,.0878,.08949,.09119,
		.0929,.09462,.09635,.09808,
		.0998,.10151,.10321,.10488,.10653,
		.10815,.10972,.11125,.11273,
		.11415,.11553,.11686,.11814,.11938,
		.12057,.12171,.12281,.12386,
		.12487,.12583,.12675,.12762,.12844,
		.12922,.12994,.13062,.13125,
		.13182,.13234,.13281,.13322,.13358,
		.13389,.13414,.13434,.13448,
		.13456,.13459,.13456,.13447,.13433,
		.13413,.13387,.13354,.13316,
		.13272,.13222,.13166,.13104,.13035,
		.1296,.12879,.12792,.12698,
		.12598,.12492,.1238,.12261,.12136,
		.12004,.11866,.11722,.11572,
		.11416,.11254,.11087,.10914,.10735,
		.1055,.1036,.10164,.09963,
		.09757,.09545,.09328,.09105,.08878,
		.08645,.08406,.08162,.07913,
		.0766,.07402,.07139,.06873,.06603,
		.06329,.06052,.0577,.05486,
		.05198,.04907,.04614,.04318,.0402,
		.0372,.03417,.03113,.02807,
		.02499,.02189,.01879,.01567,.01255,
		.00942,.00628,.00314,0. };
	
	/**
	*  Fill in phi, eps vectors for 64 series airfoil.
	*
	*  @param phi  An existing array with 201 elements to be filled in
	*              by this method.
	*  @param eps  An existing array with 201 elements to be filled in
	*              by this method.
	**/
    @Override
	protected final void phep(double[] phi, double[] eps) {
		System.arraycopy(philde, 0, phi, 0, 201);
		System.arraycopy(epslde, 0, eps, 0, 201);
	}
	
	
	//	Phi & eps vectors for 64 series airfoil.
    private static final double[] philds = {
		0.,.01573,.03145,
		.04717,.06289,.07861,.09432,.11002,
		.12572,.1414,.15708,.1728,
		.18851,.20422,.21992,.23562,.25132,
		.26702,.28272,.29844,.31416,
		.32984,.34553,.36122,.37693,.39264,
		.40836,.42408,.4398,.45552,
		.47124,.48695,.50266,.51837,.53408,
		.54979,.56549,.5812,.59691,
		.61261,.62832,.64403,.65973,.67544,
		.69115,.70686,.72257,.73827,
		.75398,.76969,.7854,.80111,.81682,
		.83252,.84823,.86394,.87965,
		.89536,.91106,.92677,.94248,.95819,
		.9739,.98961,1.00531,1.02102,
		1.03673,1.05244,1.06815,1.08385,
		1.09956,1.11527,1.13098,1.14669,
		1.1624,1.17811,1.19381,1.20952,
		1.22523,1.24093,1.25664,1.27236,
		1.28808,1.30379,1.31951,1.33522,
		1.35093,1.36663,1.38233,1.39803,
		1.41372,1.42946,1.44519,1.46091,
		1.47663,1.49234,1.50804,1.52374,
		1.53943,1.55512,1.5708,1.58653,
		1.60226,1.61798,1.6337,1.64941,
		1.66511,1.68081,1.69651,1.7122,
		1.72788,1.74361,1.75933,1.77505,
		1.79076,1.80647,1.82218,1.83788,
		1.85357,1.86927,1.88496,1.90068,
		1.9164,1.93211,1.94782,1.96353,
		1.97924,1.99494,2.01064,2.02634,
		2.04204,2.05775,2.07346,2.08917,
		2.10488,2.12059,2.13629,2.152,2.1677,
		2.18341,2.19911,2.21481,2.23051,
		2.24622,2.26192,2.27763,2.29334,
		2.30905,2.32476,2.34047,2.35619,
		2.37188,2.38757,2.40327,2.41897,
		2.43468,2.45039,2.4661,2.48182,
		2.49754,2.51327,2.52895,2.54464,
		2.56034,2.57604,2.59174,2.60745,
		2.62317,2.63889,2.65462,2.67035,
		2.68603,2.70172,2.71741,2.73311,
		2.74882,2.76453,2.78024,2.79597,
		2.8117,2.82743,2.84312,2.85881,
		2.87451,2.89021,2.90591,2.92163,
		2.93734,2.95306,2.96878,2.98451,
		3.00021,3.01591,3.03162,3.04732,
		3.06303,3.07874,3.09445,3.11016,
		3.12588,3.14159 };
		
    private static final double[] psilds = {
		.25269,.25265,.25251,
		.25227,.25193,.25147,.2509,
		.2502,.24937,.24841,.2473,.24605,
		.24467,.24321,.2417,.24016,
		.23864,.23715,.23573,.23442,.23325,
		.23224,.23138,.23066,.23006,
		.22956,.22916,.22884,.22858,.22836,
		.22818,.22802,.22788,.22775,
		.22764,.22755,.22747,.2274,.22736,
		.22732,.2273,.22729,.2273,
		.22731,.22733,.22736,.22739,.22742,
		.22745,.22748,.22751,.22753,
		.22755,.22756,.22756,.22755,.22753,
		.22751,.22747,.22742,.22736,
		.22729,.2272,.22709,.22697,.22683,
		.22668,.2265,.2263,.22608,
		.22584,.22557,.22528,.22497,.22462,
		.22426,.22386,.22345,.223,
		.22253,.22203,.2215,.22094,.22034,
		.21969,.21899,.21823,.21741,
		.21652,.21554,.21449,.21334,.21211,
		.2108,.2094,.20794,.20641,
		.20481,.20315,.20143,.19966,.19784,
		.19596,.19405,.19209,.19008,
		.18804,.18596,.18384,.18169,.1795,
		.17727,.17501,.17273,.17041,
		.16806,.16569,.16329,.16087,.15843,
		.15596,.15347,.15095,.14842,
		.14587,.1433,.14072,.13812,.13551,
		.13288,.13024,.12759,.12492,
		.12225,.11957,.11688,.11418,.11149,
		.10878,.10608,.10338,.10068,
		.09798,.09529,.0926,.08992,.08725,
		.08459,.08195,.07932,.07671,
		.07412,.07156,.06901,.06649,.06399,
		.06152,.05908,.05667,.05428,
		.05193,.04962,.04734,.0451,.04289,
		.04073,.0386,.03651,.03447,
		.03247,.03051,.02861,.02675,.02494,
		.02318,.02148,.01983,.01825,
		.01672,.01525,.01385,.01252,.01125,
		.01006,.00892,.00786,.00686,
		.00593,.00506,.00426,.00353,.00287,
		.00227,.00174,.00128,8.9e-4,
		5.7e-4,3.2e-4,1.4e-4,4e-5,0. };
	
	/**
	*  Fill in the psi vector for a 64 series airfoil.
	*
	*  @param phi  An array filled in by phep().
	*  @param psi  An existing array with 201 elements to be filled in
	*              by this method.
	**/
    @Override
	protected final void phps(double[] phi, double[] psi) {
		double[] bb = new double[201];
		double[] cc = new double[201];
		double[] dd = new double[201];
		spline(201, philds, psilds, bb, cc, dd);
		for (int i=0; i < 201; ++i) {
			psi[i] = seval(201, phi[i], philds, psilds, bb, cc, dd); 
		}
	}
	
	/**
	*  Simple method to test this class.
	**/
	public static void main(String[] args) {
	
		DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		
		System.out.println("Start NACA64Series...");
		
		System.out.println("Creating a NACA 64-012 airfoil...");
		Airfoil af = new NACA64Series(0.0, 0.12, 1);
		
		System.out.println("Airfoil = " + af.toString());
		
		//	Output the upper surface of the airfoil.
		List<Point2D> upper = af.getUpper();
		List<Double> ypArr = af.getUpperYp();
		System.out.println("upper.size() = " + upper.size() + ", ypArr.size() = " + ypArr.size());
		
		System.out.println("        X    \t    Y    \t    dy/dx");
		int length = upper.size();
		for (int i=0; i < length; ++i) {
			Point2D o = upper.get(i);
			System.out.println("    " + nf.format(o.getX()) + "\t" + nf.format(o.getY()) +
									"\t" + nf.format(ypArr.get(i)));
		}
		
		System.out.println("# ordinates = " + length);
		System.out.println("Done!");
	}
}


