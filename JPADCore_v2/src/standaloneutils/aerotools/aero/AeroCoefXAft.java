/*
 *   AeroCoefXAft -- Container for aerodynamic coefficients in an X-aft coordinate system.
 *   
 *   Copyright (C) 2002-2014 by Joseph A. Huwaldt
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
 */
package standaloneutils.aerotools.aero;

import javax.measure.unit.NonSI;

import standaloneutils.MyUnits;


/**
 * <p>
 * A container for aerodynamic dimensionless coefficients such as lift coefficient
 * (CL), drag coefficient (CD), etc. Coefficients are stored in this class as body axis
 * coefficients (CN, CA, CYb, etc), but can be retrieved and stored in wind axes
 * (CL,CD,CY, etc). This class may also contain the reference quantities used to
 * non-dimensionalize the coefficients (Sref, cbar, etc). </p>
 * <p>
 * This class assumes an
 * aircraft coordinate system where X increases (becomes more positive) as you go aft on
 * the vehicle, Z is positive out the top of the vehicle and Y goes out the starboard or
 * right wing (as viewed from the pilot's seat looking forward). The impact of the axis
 * system shows up when changing moment reference center locations. </p>
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: December 14, 1999
 * @version April 3, 2014
 */
public class AeroCoefXAft extends AeroCoefficients {

	/**
	*  Creates an AeroCoefXAft object with all the coefficients
	*  set to zero and no reference quantities defined.
	*/
	public AeroCoefXAft() { }

	/**
	*  Creates an AeroCoefXAft object with all the coefficients
	*  set to zero and the aero reference quantities set to
	*  the given values.
	*
	*  @param  refValues  Aero reference quantities (ref area, span, etc).
	*/
	public AeroCoefXAft(AeroReference refValues) {
		super(refValues);
	}

	/**
	*  Creates an AeroCoefXAft object with the body axis coefficients
	*  and reference quantities as specified.  The specified
	*  moment coefficients are moments about the reference
	*  moment location given in refValues.
	*
	*  @param refValues  Aero reference quantities (ref area, span, etc).
	*  @param CN         Body axis normal force coefficient.
	*  @param CA         Body axis axial force coefficient.
	*  @param CYb        Body axis side force coefficient.
	*  @param CMb        Body axis pitching moment coefficient at MRC.
	*  @param Cnb        Body axis yawing moment coefficient at MRC.
	*  @param Clb        Body axis rolling moment coefficient at MRC.
	*/
	public AeroCoefXAft(AeroReference refValues, double CN, double CA,
			double CYb, double CMb, double Cnb, double Clb) {
		super(refValues, CN,CA,CYb,CMb,Cnb,Clb);
	}

	/**
	*  Change the reference quantities used by this set of aero
	*  coefficients to those given.  The coefficients contained
	*  in this object are scaled (and moments transferred) to the
	*  new reference quantities.  Make sure your new moment reference
	*  location is consistent with the "X-Aft" coordinate system.
	*
	*  @param  newReference  New reference quantities to scale this
	*          object's aero coefficients to.
	*/
    @Override
	public void changeRef(AeroReference newReference) {
		if (newReference != null) {
			if (ref != null && !ref.equals(newReference)) {
				double oldSpan = ref.getRefSpanRU();
				double oldChord = ref.getRefLengthRU();
				double cratio = oldChord/newReference.getRefLengthRU();
				double bratio = oldSpan/newReference.getRefSpanRU();
				double Sratio = ref.getRefAreaRU()/newReference.getRefAreaRU();
				double oldCN = CN;
				double oldCA = CA;
				double oldCYb = CYb;

				// Scale the force coefficients.
				CN *= Sratio;
				CA *= Sratio;
				CYb *= Sratio;

				// Transfer moments to new MRC and scale them.
				double dx = newReference.getMomentRefXRU() - ref.getMomentRefXRU();
				double dy = newReference.getMomentRefYRU() - ref.getMomentRefYRU();
				double dz = newReference.getMomentRefZRU() - ref.getMomentRefZRU();

				double newM = Cnb + (oldCYb*dx - oldCA*dy)/oldSpan;
				Cnb = newM*Sratio*bratio;

				newM = CMb + (oldCN*dx - oldCA*dz)/oldChord;
				CMb = newM*Sratio*cratio;

				newM = Clb + (oldCYb*dz - oldCN*dy)/oldSpan;
				Clb = newM*Sratio*bratio;

			}

			// Change the references object.
			ref = newReference;

		} else {
			ref = null;
		}
	}

	/**
	*  Add the given aero coefficients to those contained in this
	*  object.  If the new coefficients have different reference
	*  quantities, then they will be scaled to this object's
	*  references before being added.  Make sure that the aero
	*  coefficients to be added (B) use the "X-Aft" coordinate
	*  system or a ClassCastException will be thrown.
	*
	*  @param  B  The aero coefficients to be added to this object's
	*             aero coefficients (this = this + B).
	*  @throws ClassCastException if coefficients to be added (B) do
	*          not use the XAft coordinate system.
	*/
    @Override
	public void add(AeroCoefficients B) {
		this.add((AeroCoefXAft)B);
	}

	/**
	*  Add the given aero coefficients to those contained in this
	*  object.  If the new coefficients have different reference
	*  quantities, then they will be scaled to this object's
	*  references before being added.  This version takes an
	*  AeroCoefXAft object rather than an AeroCoefficients
	*  object in order to provide type safety and avoid unnecessary
	*  casts.
	*
	*  @param  B  The aero coefficients to be added to this object's
	*             aero coefficients (this = this + B).
	*/
	public void add(AeroCoefXAft B) {

		if ( !ref.equals(B.ref) ) {
			// Make a copy of B that we can mess with.
			B = (AeroCoefXAft)B.clone();

			// Scale B to this object's reference parameters.
			B.changeRef(this.ref);
		}
		
		this.CN += B.CN;
		this.CA += B.CA;
		this.CYb += B.CYb;
		this.CMb += B.CMb;
		this.Cnb += B.Cnb;
		this.Clb += B.Clb;
	}

	/**
	*  Subtract the given aero coefficients from those contained in
	*  this object.  If the new coefficients have different reference
	*  quantities, then they will be scaled to this object's
	*  references before being subtracted.  Make sure that the aero
	*  coefficients to be subtracted (B) use the "XAft" coordinate
	*  system or a ClassCastException will be thrown.
	*
	*  @param  B  The aero coefficients to be subtracted from this
	*             object's aero coefficients (this = this - B).
	*/
    @Override
	public void subtract(AeroCoefficients B) {
		this.subtract((AeroCoefXAft)B);
	}

	/**
	*  Subtract the given aero coefficients from those contained in
	*  this object.  If the new coefficients have different reference
	*  quantities, then they will be scaled to this object's
	*  references before being subtracted.  This version takes an
	*  AeroCoefXAft object rather than an AeroCoefficients
	*  object in order to provide type safety and avoid unnecessary casts.
	*
	*  @param  B  The aero coefficients to be subtracted from this
	*             object's aero coefficients (this = this - B).
	*/
	public void subtract(AeroCoefXAft B) {

		if ( !ref.equals(B.ref) ) {
			// Make a copy of B that we can mess with.
			B = (AeroCoefXAft)B.clone();

			// Scale B to this object's reference parameters.
			B.changeRef(this.ref);
		}
		
		this.CN -= B.CN;
		this.CA -= B.CA;
		this.CYb -= B.CYb;
		this.CMb -= B.CMb;
		this.Cnb -= B.Cnb;
		this.Clb -= B.Clb;
	}


	/**
	*  A simple method to test the methods in this class.
	*/
	public static void main(String args[]) {
	
		System.out.println("\nTesting AeroCoefXAft class:");
		
		try {
			//	Create an aero reference object.
			double Sref = 1200, bref = 40, cref = 4.5F;
			double Xref = 5, Yref = 0, Zref = -1.2F;
			AeroReference ref = new AeroReference(Sref, MyUnits.FOOT2, cref, bref,
									Xref, Yref, Zref, NonSI.FOOT);
			
			System.out.println("    ref = " + ref);
			
			//	Create body axis aero coefficients.
			double CN = 1.3F, CA = 0.045F, CYb=0.1F, CMb=-1.36F, Cnb=-0.003F, Clb=0.0001F;
			AeroCoefficients data = new AeroCoefXAft(ref, CN, CA, CYb, CMb, Cnb, Clb);
			data.setAngles(10*Math.PI/180, 5*Math.PI/180, 0);
			System.out.println("    AOA = 10 deg, Beta = 5 deg, Phi = 0 deg.");
			
			System.out.println("    CN = " + data.getCN() + ", CA = " + data.getCA() + ", CYb = " + data.getCYb() + ",");
			System.out.println("    CMb = " + data.getCMb() + ", Cnb = " + data.getCnb() + ", Clb = " + data.getClb() + ".");
			
			//	Convert to wind axis coefficients.
			System.out.println("    CL = " + data.getCL() + ", CD = " + data.getCD() + ", CY = " + data.getCY() + ",");
			System.out.println("    CM = " + data.getCM() + ", Cn = " + data.getCn() + ", Cl = " + data.getCl() + ".");

			//	Convert to stability axis coefficients.
			System.out.println("    CLSA = " + data.getCLSA() + ", CDSA = " + data.getCDSA() + ", CYSA = " + data.getCYSA() + ",");
			System.out.println("    CMSA = " + data.getCMSA() + ", CnSA = " + data.getCnSA() + ", ClSA = " + data.getClSA() + ".");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Done!");
	}
}


