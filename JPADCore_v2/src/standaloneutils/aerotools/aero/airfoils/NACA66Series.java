/*
*   NACA66Series -- An arbitrary NACA 66 series airfoil.
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
*  <p> This class represents an arbitrary NACA 66 series
*      airfoil section such as a NACA 66-020 airfoil.
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
public class NACA66Series extends NACA6Series {
	
	//-----------------------------------------------------------------------------------
	
	/**
	*  Create a NACA 66 series airfoil with the specified parameters.
	*
	*  @param  CLi        Design lift coefficient (e.g.: 66-206 has CLi = 0.2).
	*  @param  thickness  The thickness to chord ratio (e.g.: 0.20 ==> 20% t/c).
	*  @param  length     The chord length.
	**/
	public NACA66Series(double CLi, double thickness, double length) {
		super(CLi, thickness, length);
	}
	
	/**
	*  Returns a String that represents the profile type of this airfoil.
	**/
    @Override
	public String getProfile() {
		return "66";
	}
	
	//	Phi & eps vectors for 66 series airfoil.
    private static final double[] philde = {
		0.,.0157,.03139,
		.04709,.06279,.07849,.0942,.10991,
		.12563,.14135,.15708,.17277,
		.18847,.20417,.21987,.23559,.2513,
		.26701,.28273,.29844,.31416,
		.32987,.34558,.36129,.37699,.3927,
		.40841,.42411,.43982,.45553,
		.47124,.48694,.50265,.51836,.53406,
		.54977,.56548,.58119,.5969,
		.61261,.62832,.64403,.65974,.67545,
		.69116,.70686,.72257,.73828,
		.75399,.76969,.7854,.80111,.81682,
		.83253,.84824,.86394,.87965,
		.89536,.91107,.92677,.94248,.95819,
		.9739,.98961,1.00532,1.02103,
		1.03673,1.05244,1.06815,1.08385,
		1.09956,1.11527,1.13098,1.14669,
		1.1624,1.17811,1.19381,1.20952,
		1.22523,1.24093,1.25664,1.27235,
		1.28806,1.30377,1.31948,1.33519,
		1.3509,1.3666,1.38231,1.39802,1.41372,
		1.42943,1.44514,1.46085,1.47656,
		1.49227,1.50798,1.52368,1.53939,
		1.5551,1.5708,1.58651,1.60223,1.61794,
		1.63365,1.64936,1.66507,1.68077,
		1.69648,1.71218,1.72788,1.74359,
		1.75931,1.77501,1.79072,1.80643,
		1.82214,1.83784,1.85355,1.86925,
		1.88496,1.90066,1.91636,1.93206,
		1.94776,1.96347,1.97918,1.99489,
		2.0106,2.02632,2.04204,2.05773,
		2.07343,2.08913,2.10484,2.12054,
		2.13625,2.15196,2.16768,2.18339,
		2.19911,2.21482,2.23052,2.24623,
		2.26194,2.27765,2.29336,2.30906,
		2.32477,2.34048,2.35619,2.37191,
		2.38762,2.40333,2.41905,2.43476,
		2.45046,2.46617,2.48187,2.49757,
		2.51327,2.529,2.54473,2.56045,2.57616,
		2.59188,2.60758,2.62328,2.63898,
		2.65467,2.67035,2.68609,2.70183,
		2.71756,2.73328,2.74899,2.76469,
		2.78039,2.79608,2.81176,2.82743,
		2.84317,2.85891,2.87463,2.89035,
		2.90606,2.92176,2.93746,2.95315,
		2.96883,2.98451,3.00023,3.01595,
		3.03167,3.04738,3.06309,3.07879,
		3.09449,3.11019,3.12589,3.14159 };
		
    private static final double[] epslde = {
		0.,.00145,.0029,
		.00433,.00574,.00712,.00847,.00978,
		.01105,.01225,.0134,.01447,
		.01547,.01638,.01719,.01789,.01847,
		.01893,.01924,.0194,.0194,
		.01924,.01893,.0185,.01799,.01741,
		.01679,.01616,.01556,.01499,
		.0145,.0141,.01379,.01356,.0134,
		.01331,.01327,.01328,.01333,
		.0134,.0135,.01361,.01373,.01387,
		.01402,.01419,.01438,.01458,
		.0148,.01504,.0153,.01558,.01588,
		.0162,.01654,.01689,.01726,
		.01765,.01805,.01847,.0189,.01934,
		.0198,.02026,.02074,.02124,
		.02174,.02226,.02279,.02334,.0239,
		.02447,.02506,.02566,.02627,
		.0269,.02754,.02819,.02885,.02952,
		.0302,.03089,.0316,.03231,
		.03304,.03378,.03453,.0353,.03608,
		.03688,.0377,.03853,.03938,
		.04025,.04113,.04202,.04293,.04386,
		.04479,.04574,.0467,.04767,
		.04866,.04966,.05067,.05171,.05277,
		.05386,.05498,.05612,.0573,
		.05851,.05976,.06103,.06231,.06362,
		.06493,.06625,.06758,.06889,
		.0702,.07149,.07277,.07402,.07524,
		.07644,.0776,.07872,.07979,
		.08082,.0818,.08272,.08359,.0844,
		.08515,.08585,.08649,.08708,
		.08761,.08808,.0885,.08886,.08916,
		.08941,.08959,.08972,.08978,
		.08978,.08972,.08959,.0894,.08914,
		.08882,.08843,.08797,.08745,
		.08687,.08622,.08551,.08474,.0839,
		.083,.08203,.08101,.07991,
		.07875,.07752,.07622,.07485,.07341,
		.0719,.07031,.06864,.06691,
		.0651,.06323,.06129,.05928,.05722,
		.05509,.0529,.05064,.04833,
		.04596,.04354,.04108,.03856,.03601,
		.03341,.03077,.0281,.02539,
		.02264,.01987,.01707,.01426,.01143,
		.00858,.00573,.00287,0. };
	
	/**
	*  Fill in phi, eps vectors for 66 series airfoil.
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
	
	
	//	Phi & eps vectors for 66 series airfoil.
    private static final double[] philds = {
		0.,.01573,.03145,
		.04718,.0629,.07862,.09433,.11003,
		.12572,.14141,.15708,.1728,
		.18851,.20422,.21992,.23562,.25131,
		.26701,.28272,.29844,.31416,
		.32984,.34552,.36122,.37693,.39264,
		.40835,.42407,.4398,.45552,
		.47124,.48696,.50267,.51838,.53409,
		.54979,.5655,.58121,.59691,
		.61262,.62832,.64403,.65973,.67544,
		.69115,.70686,.72257,.73827,
		.75398,.76969,.7854,.80111,.81682,
		.83252,.84823,.86394,.87965,
		.89536,.91106,.92677,.94248,.95819,
		.9739,.9896,1.00531,1.02102,
		1.03673,1.05244,1.06814,1.08385,
		1.09956,1.11527,1.13098,1.14668,
		1.16239,1.1781,1.19381,1.20952,
		1.22522,1.24093,1.25664,1.27235,
		1.28806,1.30376,1.31947,1.33518,
		1.35089,1.3666,1.3823,1.39801,1.41372,
		1.42943,1.44514,1.46085,1.47655,
		1.49226,1.50797,1.52368,1.53939,
		1.55509,1.5708,1.58651,1.60222,
		1.61793,1.63364,1.64934,1.66505,
		1.68076,1.69647,1.71217,1.72788,
		1.7436,1.75932,1.77504,1.79075,
		1.80647,1.82217,1.83788,1.85358,
		1.86927,1.88496,1.9007,1.91642,
		1.93214,1.94786,1.96357,1.97927,
		1.99497,2.01067,2.02636,2.04204,
		2.05777,2.07349,2.0892,2.10492,
		2.12062,2.13633,2.15203,2.16773,
		2.18342,2.19911,2.21483,2.23055,
		2.24626,2.26197,2.27768,2.29338,
		2.30909,2.32479,2.34049,2.35619,
		2.3719,2.38761,2.40331,2.41902,
		2.43473,2.45043,2.46614,2.48185,
		2.49756,2.51327,2.52897,2.54467,
		2.56037,2.57607,2.59178,2.60749,
		2.6232,2.63891,2.65463,2.67035,
		2.68604,2.70173,2.71742,2.73312,
		2.74882,2.76453,2.78025,2.79597,
		2.8117,2.82743,2.84311,2.8588,2.87449,
		2.89019,2.9059,2.92161,2.93733,
		2.95305,2.96878,2.98451,3.0002,3.0159,
		3.0316,3.04731,3.06302,3.07873,
		3.09444,3.11016,3.12587,3.14159 };
		
    private static final double[] psilds = {
		.16457,.16455,.16449,
		.16437,.16416,.16386,.16345,
		.16292,.16223,.16139,.16037,.15916,
		.15779,.15631,.15475,.15316,
		.15157,.15002,.14856,.14722,.14604,
		.14506,.14427,.14364,.14316,
		.14281,.14257,.14242,.14235,.14233,
		.14236,.14241,.14248,.14257,
		.14267,.1428,.14294,.1431,.14327,
		.14346,.14366,.14387,.1441,
		.14433,.14457,.14481,.14506,.1453,
		.14554,.14578,.14601,.14623,
		.14645,.14665,.14685,.14704,.14722,
		.1474,.14757,.14774,.1479,
		.14806,.14821,.14835,.14849,.14862,
		.14875,.14886,.14897,.14908,
		.14917,.14925,.14933,.1494,.14945,
		.1495,.14954,.14957,.14959,
		.14961,.14961,.1496,.14959,.14956,
		.14953,.14948,.14943,.14936,
		.14928,.14918,.14908,.14896,.14883,
		.14869,.14853,.14835,.14816,
		.14796,.14774,.1475,.14725,.14698,
		.14669,.14638,.14606,.14571,
		.14533,.14494,.14452,.14407,.1436,
		.1431,.14256,.14198,.14135,
		.14067,.13992,.1391,.1382,.13722,
		.13615,.13498,.13371,.13236,
		.13093,.12942,.12786,.12623,.12456,
		.12284,.12108,.11929,.11746,
		.11561,.11373,.11182,.10987,.1079,
		.1059,.10386,.1018,.0997,.09758,
		.09542,.09325,.09106,.08885,
		.08662,.08439,.08214,.07989,.07763,
		.07537,.07311,.07085,.06859,
		.06633,.06407,.06182,.05957,.05733,
		.0551,.05287,.05066,.04846,
		.04627,.04411,.04196,.03983,.03773,
		.03566,.03362,.03161,.02964,
		.0277,.0258,.02395,.02215,.02039,
		.01869,.01705,.01547,.01396,
		.0125,.01112,.00981,.00857,.0074,
		.00631,.00531,.00439,.00356,
		.00281,.00216,.00159,.0011,7.1e-4,
		4e-4,1.8e-4,4e-5,0. };
	
	/**
	*  Fill in the psi vector for a 66 series airfoil.
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
		
		System.out.println("Start NACA66Series...");
		
		System.out.println("Creating a NACA 66-212 airfoil...");
		Airfoil af = new NACA66Series(0.2, 0.12, 1);
		
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


