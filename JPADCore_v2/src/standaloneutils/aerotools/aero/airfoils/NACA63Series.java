/*
*   NACA63Series -- An arbitrary NACA 63 series airfoil.
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
*  <p> This class represents an arbitrary NACA 63 series
*      airfoil section such as a NACA 63-020 airfoil.
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
*  @author  Joseph A. Huwaldt   Date:  June 3, 2010
*  @version September 15, 2012
**/
public class NACA63Series extends NACA6Series {
	
	//-----------------------------------------------------------------------------------
	
	/**
	*  Create a NACA 63 series airfoil with the specified parameters.
	*
	*  @param  CLi        Design lift coefficient (e.g.: 63-206 has CLi = 0.2).
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.20 ==> 20% t/c).
	*  @param  length     The chord length.
	**/
	public NACA63Series(double CLi, double thickness, double length) {
		super(CLi, thickness, length);
	}
	
	/**
	*  Returns a String that represents the profile type of this airfoil.
	**/
    @Override
	public String getProfile() {
		return "63";
	}
	
	//	Phi & eps vectors for 63 series airfoil.
    private static final double[] philde = {
		0.,.01569,.03139,
		.04708,.06278,.07848,.09419,.1099,
		.12562,.14135,.15708,.17277,
		.18846,.20416,.21987,.23558,.25129,
		.26701,.28273,.29844,.31416,
		.32987,.34559,.3613,.377,.39271,
		.40842,.42412,.43983,.45553,
		.47124,.48695,.50266,.51837,.53407,
		.54978,.56549,.5812,.59691,
		.61261,.62832,.64403,.65974,.67545,
		.69116,.70687,.72257,.73828,
		.75399,.76969,.7854,.80111,.81682,
		.83253,.84824,.86395,.87965,
		.89536,.91107,.92677,.94248,.95819,
		.9739,.98961,1.00532,1.02103,
		1.03673,1.05244,1.06815,1.08385,
		1.09956,1.11527,1.13098,1.14669,
		1.1624,1.17811,1.19381,1.20952,
		1.22523,1.24093,1.25664,1.27235,
		1.28806,1.30376,1.31947,1.33518,
		1.35089,1.36659,1.3823,1.39801,
		1.41372,1.42942,1.44513,1.46084,
		1.47654,1.49225,1.50796,1.52367,
		1.53938,1.55509,1.5708,1.5865,1.60221,
		1.61791,1.63362,1.64933,1.66504,
		1.68075,1.69646,1.71217,1.72788,
		1.74358,1.75929,1.775,1.7907,1.80641,
		1.82212,1.83783,1.85354,1.86925,
		1.88496,1.90067,1.91637,1.93208,
		1.94779,1.9635,1.97921,1.99491,
		2.01062,2.02633,2.04204,2.05775,
		2.07346,2.08917,2.10487,2.12058,
		2.13629,2.152,2.1677,2.18341,2.19911,
		2.21482,2.23054,2.24625,2.26196,
		2.27767,2.29338,2.30908,2.32479,
		2.34049,2.35619,2.37191,2.38762,
		2.40334,2.41905,2.43476,2.45046,
		2.46617,2.48187,2.49757,2.51327,
		2.52899,2.5447,2.56042,2.57613,
		2.59184,2.60754,2.62325,2.63895,
		2.65465,2.67035,2.68607,2.70178,
		2.71749,2.7332,2.74891,2.76462,
		2.78032,2.79603,2.81173,2.82743,
		2.84314,2.85885,2.87456,2.89027,
		2.90598,2.92169,2.9374,2.9531,2.96881,
		2.98451,3.00022,3.01593,3.03164,
		3.04735,3.06305,3.07876,3.09447,
		3.11018,3.12588,3.14159 };
		
    private static final double[] epslde = {
		0.,.00164,.00327,
		.00487,.00641,.00789,.00928,.01057,
		.01174,.01278,.01367,.01439,
		.01497,.01542,.01576,.01601,.01619,
		.01632,.01642,.01651,.01661,
		.01673,.01688,.01705,.01725,.01747,
		.01771,.01797,.01824,.01853,
		.01884,.01916,.01949,.01984,.0202,
		.02058,.02097,.02137,.02179,
		.02223,.02268,.02315,.02363,.02413,
		.02464,.02517,.02571,.02626,
		.02683,.02741,.02801,.02862,.02924,
		.02988,.03052,.03118,.03185,
		.03253,.03323,.03393,.03465,.03538,
		.03611,.03686,.03762,.03839,
		.03917,.03995,.04075,.04156,.04237,
		.04319,.04402,.04486,.04571,
		.04657,.04743,.04831,.04919,.05008,
		.05098,.05189,.0528,.05372,
		.05464,.05556,.05648,.0574,.05831,
		.05921,.06011,.06099,.06187,
		.06273,.06357,.0644,.06522,.06602,
		.06681,.06757,.06832,.06905,
		.06976,.07044,.07111,.07176,.07238,
		.07298,.07356,.07411,.07464,
		.07514,.07562,.07607,.0765,.0769,
		.07727,.07761,.07793,.07822,
		.07848,.07871,.07891,.07908,.07922,
		.07933,.07941,.07945,.07946,
		.07944,.07938,.07929,.07916,.079,
		.0788,.07856,.07829,.07799,
		.07764,.07726,.07685,.0764,.07591,
		.07539,.07483,.07423,.07359,
		.07293,.07222,.07148,.0707,.06989,
		.06904,.06815,.06723,.06628,
		.06529,.06427,.06322,.06214,.06103,
		.05989,.05871,.05751,.05628,
		.05502,.05374,.05243,.05109,.04973,
		.04834,.04693,.04549,.04404,
		.04256,.04106,.03955,.03802,.03647,
		.03491,.03333,.03174,.03014,
		.02853,.0269,.02527,.02363,.02198,
		.02032,.01865,.01698,.0153,
		.01361,.01192,.01023,.00853,.00683,
		.00512,.00342,.00171,0. };
	
	/**
	*  Fill in phi, eps vectors for 63 series airfoil.
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
	
	
	//	Phi & eps vectors for 63 series airfoil.
    private static final double[] philds = {
		0.,.01571,.03142,
		.04713,.06284,.07855,.09426,.10997,
		.12567,.14138,.15708,.17279,
		.1885,.2042,.21991,.23561,.25132,
		.26703,.28274,.29845,.31416,
		.32986,.34556,.36127,.37698,.39268,
		.40839,.42411,.43982,.45553,
		.47124,.48695,.50266,.51837,.53408,
		.54978,.56549,.5812,.59691,
		.61261,.62832,.64403,.65974,.67544,
		.69115,.70686,.72257,.73828,
		.75398,.76969,.7854,.80111,.81682,
		.83252,.84823,.86394,.87965,
		.89536,.91106,.92677,.94248,.95819,
		.9739,.98961,1.00531,1.02102,
		1.03673,1.05244,1.06815,1.08385,
		1.09956,1.11527,1.13098,1.14669,
		1.1624,1.17811,1.19381,1.20952,
		1.22523,1.24093,1.25664,1.27235,
		1.28807,1.30378,1.31949,1.3352,1.3509,
		1.36661,1.38231,1.39802,1.41372,
		1.42944,1.44515,1.46086,1.47657,
		1.49228,1.50799,1.52369,1.5394,1.5551,
		1.5708,1.58652,1.60223,1.61794,
		1.63365,1.64936,1.66507,1.68077,
		1.69648,1.71218,1.72788,1.74359,
		1.75931,1.77502,1.79073,1.80644,
		1.82214,1.83785,1.85355,1.86926,
		1.88496,1.90067,1.91638,1.93209,
		1.9478,1.96351,1.97922,1.99493,
		2.01063,2.02634,2.04204,2.05775,
		2.07346,2.08916,2.10487,2.12058,
		2.13628,2.15199,2.1677,2.1834,2.19911,
		2.21481,2.23052,2.24623,2.26193,
		2.27764,2.29335,2.30906,2.32477,
		2.34048,2.35619,2.37189,2.38759,
		2.4033,2.419,2.43471,2.45042,2.46613,
		2.48184,2.49755,2.51327,2.52897,
		2.54467,2.56037,2.57608,2.59179,
		2.60749,2.62321,2.63892,2.65463,
		2.67035,2.68605,2.70175,2.71745,
		2.73316,2.74887,2.76458,2.78029,2.796,
		2.81171,2.82743,2.84313,2.85884,
		2.87454,2.89025,2.90596,2.92167,
		2.93737,2.95309,2.9688,2.98451,
		3.00022,3.01592,3.03163,3.04734,
		3.06305,3.07875,3.09446,3.11017,
		3.12588,3.14159 };
		
    private static final double[] psilds = {
		.15066,.15058,.15035,
		.14999,.1495,.14891,.14823,
		.14748,.14668,.14583,.14497,.1441,
		.14323,.14238,.14155,.14074,
		.13998,.13927,.13862,.13804,.13753,
		.13711,.13676,.13648,.13627,
		.1361,.13598,.1359,.13584,.13579,
		.13576,.13573,.1357,.13567,
		.13564,.13561,.13558,.13555,.13552,
		.1355,.13547,.13544,.13542,
		.13539,.13536,.13533,.13529,.13525,
		.13521,.13516,.13511,.13505,
		.13499,.13491,.13483,.13475,.13465,
		.13454,.13442,.13428,.13414,
		.13398,.13381,.13363,.13343,.13321,
		.13299,.13275,.13249,.13222,
		.13194,.13164,.13133,.131,.13065,
		.13028,.12988,.12947,.12903,
		.12857,.12808,.12756,.12702,.12644,
		.12584,.12521,.12455,.12385,
		.12313,.12238,.1216,.12079,.11994,
		.11907,.11817,.11724,.11628,
		.11529,.11428,.11324,.11218,.11109,
		.10998,.10884,.10768,.1065,
		.1053,.10407,.10283,.10157,.10029,
		.09899,.09767,.09634,.09499,
		.09363,.09224,.09085,.08944,.08801,
		.08657,.08512,.08365,.08217,
		.08068,.07917,.07766,.07614,.07461,
		.07307,.07153,.06998,.06842,
		.06687,.0653,.06374,.06217,.0606,
		.05904,.05747,.05591,.05435,
		.0528,.05125,.0497,.04817,.04664,
		.04512,.04362,.04213,.04065,
		.03919,.03774,.03631,.0349,.0335,
		.03213,.03077,.02943,.02811,
		.02682,.02555,.0243,.02308,.02188,
		.02071,.01956,.01844,.01735,
		.0163,.01527,.01428,.01331,.01239,
		.01149,.01062,.00979,.00899,
		.00823,.0075,.0068,.00614,.00551,
		.00491,.00435,.00382,.00332,
		.00286,.00244,.00205,.00169,.00137,
		.00108,8.3e-4,6.1e-4,4.2e-4,
		2.7e-4,1.5e-4,7e-5,2e-5,0. };
	
	/**
	*  Fill in the psi vector for a 63 series airfoil.
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
		
		System.out.println("Start NACA63Series...");
		
		System.out.println("Creating a NACA 63-206 airfoil...");
		Airfoil af = new NACA63Series(0.2, 0.06, 1);
		
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


