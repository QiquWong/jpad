/*
 *   AeroCoefficients -- Container for nondimensional aerodynamic coefficients.
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

import static java.lang.Math.*;


/**
 * A container for aerodynamic dimensionless coefficients such as lift coefficient (CL),
 * drag coefficient (CD), etc. Coefficients are stored in this class as body axis
 * coefficients (CN, CA, CYB, etc), but can be set and retrieved in wind axes and
 * stability axes. This class may also contain the reference quantities used to
 * non-dimensionalize the coefficients (Sref, cbar, etc).
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: December 8, 1999
 * @version April 3, 2014
 */
public abstract class AeroCoefficients implements java.io.Serializable, Cloneable {

	/**
	*  Aerodynamic reference quantities are stored here.
	*/
	protected AeroReference ref;

	/**
	*  Body axes force coefficients.
	*/
	protected double CN, CA, CYb;

	/**
	*  Body axes moment coefficients.
	*/
	protected double CMb, Cnb, Clb;

	/**
	*  Flow angles used to transform between axis systems.
	*/
	protected double alpha, beta, phi;

    /**
    *  Wind axis to body axis transformation matrix.
    *  Marked transient so that it will not get serialized.  The
    *  data in it can be reconstructed from the alpha, beta, and
    *  phi angles at de-serialization time.
    */
    private transient double[][] wind2body = new double[3][3];
    
	/**
	*  Creates an AeroCoefficients object with all the coefficients
	*  set to zero and with no reference quantities defined.
	*/
	protected AeroCoefficients() { }

	/**
	*  Creates an AeroCoefficients object with all the coefficients
	*  set to zero and the aero reference quantities set to
	*  the given values.
	*
	*  @param  refValues  Aero reference quantities (ref area, span, etc).
	*/
	protected AeroCoefficients(AeroReference refValues) {
		ref = refValues;
	}

	/**
	*  Creates an AeroCoefficients object with the coefficients
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
	protected AeroCoefficients(AeroReference refValues, double CN, double CA,
			                        double CYb, double CMb, double Cnb, double Clb) {
		ref = refValues;
		this.CN = CN;
		this.CA = CA;
		this.CYb = CYb;
		this.CMb = CMb;
		this.Cnb = Cnb;
		this.Clb = Clb;
	}

	/**
	*  Change the reference quantities used by this set of aero
	*  coefficients to those given.  The coefficients contained
	*  in this object are scaled (and moments transferred) to the
	*  new reference quantities.
	*
	*  @param  newReference  New reference quantities to scale this
	*          object's aero coefficients to.
	*/
	public abstract void changeRef(AeroReference newReference);


	/**
	*  Return the reference quantities used by this set of aero
	*  coefficients.
	*
	*  @return The reference quantities to used to scale this object's
	*          aero coefficients.
	*/
	public AeroReference getRef() {
		return ref;
	}


	/**
	*  Add the given aero coefficients to those contained in this
	*  object.  When sub-classing, make sure that you scale the
	*  coefficients to the same reference quantities first if needed.
	*
	*  @param  B  The aero coefficients to be added to this object's
	*             aero coefficients (this = this + B).
	*/
	public abstract void add(AeroCoefficients B);

	/**
	*  Subtract the given aero coefficients from those contained in
	*  object.  When sub-classing, make sure that you scale the
	*  coefficients to the same reference quantities first if needed.
	*
	*  @param  B  The aero coefficients to be subtracted from this
	*             object's aero coefficients (this = this - B).
	*/
	public abstract void subtract(AeroCoefficients B);

	/**
	*  Returns true if the input AeroCoefficients object has the same
	*  numerical values (same reference quantities, and same body axis
	*  coefficient values) as this one.
	*
	*  @param obj  The AeroCoefficients object we are comparing this one to.
	*  @return True if the given AeroCoefficients object is equal to this one.
	*/
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (obj.getClass() != this.getClass()))
			return false;

        AeroCoefficients that = (AeroCoefficients)obj;
        if (this.ref == null && that.ref != null)
            return false;
        else if (!this.ref.equals(that.ref))
            return false;
        if (this.CN != that.CN)
            return false;
        if (this.CYb != that.CYb)
            return false;
        if (this.CA != that.CA)
            return false;
        if (this.CMb != that.CMb)
            return false;
        if (this.Cnb != that.Cnb)
            return false;
        
        return this.Clb == that.Clb;
	}

    /**
    *  Returns a hash code value for the object.
    */
    @Override
    public int hashCode() {
        int hash = 7;
        
        hash = hash * 31 + (ref == null ? 0 : ref.hashCode());
        hash = hash * 31 + makeVarCode(CN);
        hash = hash * 31 + makeVarCode(CYb);
        hash = hash * 31 + makeVarCode(CA);
        hash = hash * 31 + makeVarCode(CMb);
        hash = hash * 31 + makeVarCode(Cnb);
        hash = hash * 31 + makeVarCode(Clb);
        
        return hash;
    }

    private static int makeVarCode(double value) {
        long bits = Double.doubleToLongBits(value);
        int var_code = (int)(bits ^ (bits >>> 32));
        return var_code;
    }
    
	/**
	*  Make a copy of this AeroCoefficients object.
	*
	*  @return A copy of this AeroCoefficients object.
	*/
    @Override
	public Object clone() {
		AeroCoefficients result=null;

		try {
			// First, make a shallow clone.
			result = (AeroCoefficients)super.clone();

			// Now make a deep clone.
			if (this.ref != null)
			    result.ref = (AeroReference)ref.clone();

		} catch ( Exception e) {
			// Should not happen if this object implements Cloneable.
			System.err.println("Can not clone this object!");
			e.printStackTrace();
		}

		return result;
	}

	//-------------------------------------------------------------
	/**
	*  Set the body axis force coefficients.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.
	*
	*  @param CN  Body axis normal force coefficient.
	*  @param CA  Body axis axial force coefficient.
	*  @param CYb Body axis side force coefficient.
	*/
	public void setBodyAxesForces(double CN, double CA, double CYb) {
		this.CN = CN;
		this.CA = CA;
		this.CYb = CYb;
	}

	/**
	*  Set the body axis normal force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.
	*
	*  @param value  Normal force coefficient.
	*/
	public void setCN(double value) {
		CN = value;
	}

	/**
	*  Set the body axis axial force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.
	*
	*  @param value  Axial force coefficient.
	*/
	public void setCA(double value) {
		CA = value;
	}

	/**
	*  Set the body axis side force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.
	*
	*  @param value  Side force coefficient.
	*/
	public void setCYb(double value) {
		CYb = value;
	}

	/**
	*  Set the body axis moment coefficients.
	*
	*  @param CMb  Body axis pitching moment coefficient.
	*  @param Cnb  Body axis yawing moment coefficient.
	*  @param Clb  Body axis rolling moment coefficient.
	*/
	public void setBodyAxesMoments(double CMb, double Cnb, double Clb) {
		this.CMb = CMb;
		this.Cnb = Cnb;
		this.Clb = Clb;
	}

	/**
	*  Set the body axis pitching moment coefficient.  Non-dimensional
	*  pitching moment coefficient is defined as:
	*  CM = PM/(qbar*Sref*cref)  where PM is the pitching moment,
	*  (defined as positive nose up), qbar is dynamic pressure,
	*  Sref is the reference area, and cref is the reference
	*  chord length.
	*
	*  @param value Body axis pitching moment coefficient at the moment reference
	*               center.
	*/
	public void setCMb(double value) {
		CMb = value;
	}

	/**
	*  Set the body axis yawing moment coefficient.  Non-dimensional
	*  yawing moment coefficient is defined as:
	*  Cn = YM/(qbar*Sref*bref)  where YM is the yawing moment,
	*  (defined as positive nose right), qbar is dynamic pressure,
	*  Sref is the reference area, and bref is the reference
	*  span length.
	*
	*  @param value Body axis yawing moment coefficient at the moment reference
	*               center.
	*/
	public void setCnb(double value) {
		Cnb = value;
	}

	/**
	*  Set the body axis rolling moment coefficient.  Non-dimensional
	*  rolling moment coefficient is defined as:
	*  Cl = RM/(qbar*Sref*bref)  where RM is the rolling moment,
	*  (defined as positive right wing up), qbar is dynamic pressure,
	*  Sref is the reference area, and bref is the reference
	*  span length.
	*
	*  @param value Body axis rolling moment coefficient at the moment reference
	*               center.
	*/
	public void setClb(double value) {
		Clb = value;
	}

	/**
	*  Get the body axis normal force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.
	*
	*  @return Normal force coefficient.
	*/
	public double getCN() {
		return CN;
	}

	/**
	*  Get the body axis axial force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.
	*
	*  @return Axial force coefficient.
	*/
	public double getCA() {
		return CA;
	}

	/**
	*  Get the body axis side force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.
	*
	*  @return Side force coefficient.
	*/
	public double getCYb() {
		return CYb;
	}

	/**
	*  Get the body axis pitching moment coefficient.  Non-dimensional
	*  pitching moment coefficient is defined as:
	*  CM = PM/(qbar*Sref*cref)  where PM is the pitching moment,
	*  (defined as positive nose up), qbar is dynamic pressure,
	*  Sref is the reference area, and cref is the reference
	*  chord length.
	*
	*  @return  Pitching moment coefficient at the moment reference
	*           center.
	*/
	public double getCMb() {
		return CMb;
	}

	/**
	*  Get the body axis yawing moment coefficient.  Non-dimensional
	*  yawing moment coefficient is defined as:
	*  Cn = YM/(qbar*Sref*bref)  where YM is the yawing moment,
	*  (defined as positive nose right), qbar is dynamic pressure,
	*  Sref is the reference area, and bref is the reference
	*  span length.
	*
	*  @return  Yawing moment coefficient at the moment reference
	*           center.
	*/
	public double getCnb() {
		return Cnb;
	}

	/**
	*  Get the body axis rolling moment coefficient.  Non-dimensional
	*  rolling moment coefficient is defined as:
	*  Cl = RM/(qbar*Sref*bref)  where RM is the rolling moment,
	*  (defined as positive right wing up), qbar is dynamic pressure,
	*  Sref is the reference area, and bref is the reference
	*  span length.
	*
	*  @return  Rolling moment coefficient at the moment reference
	*           center.
	*/
	public double getClb() {
		return Clb;
	}

	/**
	*  Add the given increment to the current value of the body
	*  axis normal force coefficient.
	*
	*  @param  delta  The amount to increase the body axis normal
	*                 force coefficient by.
	*/
	public void addCN(double delta) {
		CN += delta;
	}

	/**
	*  Add the given increment to the current value of the body
	*  axis axial force coefficient.
	*
	*  @param  delta  The amount to increase the body axis axial
	*                 force coefficient by.
	*/
	public void addCA(double delta) {
		CA += delta;
	}

	/**
	*  Add the given increment to the current value of the body
	*  axis side force coefficient.
	*
	*  @param  delta  The amount to increase the body axis side
	*                 force coefficient by.
	*/
	public void addCYb(double delta) {
		CYb += delta;
	}

	/**
	*  Add the given increment to the current value of the
	*  body axis pitching moment coefficient.
	*
	*  @param  delta  The amount to increase the pitching moment
	*                 coefficient by.
	*/
	public void addCMb(double delta) {
		CMb += delta;
	}

	/**
	*  Add the given increment to the current value of the
	*  body axis yawing moment coefficient.
	*
	*  @param  delta  The amount to increase the yawing moment
	*                 coefficient by.
	*/
	public void addCnb(double delta) {
		Cnb += delta;
	}

	/**
	*  Add the given increment to the current value of the
	*  body axis rolling moment coefficient.
	*
	*  @param  delta  The amount to increase the rolling moment
	*                 coefficient by.
	*/
	public void addClb(double delta) {
		Clb += delta;
	}

	//-------------------------------------------------------------
	/**
	*  Set the free stream flow angles used to resolve body axis
	*  coefficients to wind and stability axes and vis-versa.  Make sure you
	*  set the correct flow angles before calling methods that
	*  deal with wind or stability axis coefficients!
	*
	*  @param   AOA      Angle of attack in radians.
	*  @param   sideslip Sideslip angle in radians.
	*  @param   roll     Roll angle in radians.
	*/
	public void setAngles(double AOA, double sideslip, double roll) {
		alpha = AOA;
		beta = sideslip;
		phi = roll;

        //  Calculate the wind axis to body axis transformation matrix.
		calculateW2B();
	}

	/**
	*  Set the angle of attack used for resolving body axis
	*  coefficients to wind and stability axes and back again.
	*
	*  @param   value  Angle of attack in radians.
	*/
	public void setAOA(double value) {
		alpha = value;
        
        //  Calculate the wind axis to body axis transformation matrix.
		calculateW2B();
	}

	/**
	*  Set the sideslip angle used for resolving body axis
	*  coefficients to wind and stability axes and back again.
	*
	*  @param   value  Sideslip angle in radians.
	*/
	public void setSideslip(double value) {
		beta = value;
        
        //  Calculate the wind axis to body axis transformation matrix.
		calculateW2B();
	}

	/**
	*  Set the roll angle used for resolving body axis
	*  coefficients to wind and stability axes and back again.
	*
	*  @param   value  Roll angle in radians.
	*/
	public void setRoll(double value) {
		phi = value;
        
        //  Calculate the wind axis to body axis transformation matrix.
		calculateW2B();
	}

	/**
	*  Get the angle of attack used for resolving body axis
	*  coefficients to wind and stability axes and back again.
	*
	*  @return The angle of attack in radians.
	*/
	public double getAOA() {
		return alpha;
	}

	/**
	*  Get the sideslip angle used for resolving body axis
	*  coefficients to wind and stability axes and back again.
	*
	*  @return The sideslip angle in radians.
	*/
	public double getSideslip() {
		return beta;
	}

	/**
	*  Get the roll angle used for resolving body axis
	*  coefficients to wind and stability axes and back again.
	*
	*  @return The roll angle in radians.
	*/
	public double getRoll() {
		return phi;
	}

	/**
	*  Set the wind axis force coefficients.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.  Angle inputs are used to resolve
	*  components from wind axes to body axes.  Make sure you
	*  call setAngles() before calling this method!
	*
	*  @param CL  Wind axis lift force coefficient.
	*  @param CD  Wind axis drag force coefficient.
	*  @param CY  Wind axis side force coefficient.
	*  @see   #setAngles
	*/
	public void setWindAxesForces(double CL, double CD, double CY) {
		CN =  CL*wind2body[0][0] + CD*wind2body[0][1] + CY*wind2body[0][2];
		CA =  CL*wind2body[1][0] + CD*wind2body[1][1] + CY*wind2body[1][2];
		CYb = CL*wind2body[2][0] + CD*wind2body[2][1] + CY*wind2body[2][2];
	}

	/**
	*  Set the wind axis moment coefficients.
	*  Angle inputs are used to resolve components from
	*  wind axes to body axes.  Make sure you
	*  call setAngles() before calling this method!
	*
	*  @param CM  Wind axis pitching moment coefficient.
	*  @param Cn  Wind axis yawing moment coefficient.
	*  @param Cl  Wind axis rolling moment coefficient.
	*  @see   #setAngles
	*/
	public void setWindAxesMoments(double CM, double Cn, double Cl) {
	    //  Get references.
	    double cref = getRefLength();
	    double bref = getRefSpan();
	    
	    //  Calculate coefficients.
		Cnb = Cn*wind2body[0][0] + Cl*wind2body[0][1] - CM*cref/bref*wind2body[0][2];
		Clb = Cn*wind2body[1][0] + Cl*wind2body[1][1] - CM*cref/bref*wind2body[1][2];
		CMb = Cn*bref/cref*wind2body[2][0] - Cl*bref/cref*wind2body[2][1] + CM*wind2body[2][2];
	}

	/**
	*  Get the wind axis lift force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.  Angle inputs are used to resolve
	*  components from body axes to wind axes.
	*
	*  @return Wind axis lift force coefficient.
	*  @see     #setAngles
	*/
	public double getCL() {
		double cosA = cos(alpha);

		double CL = CN*cos(phi)*cosA;
		CL -= CA*sin(alpha);
		CL -= CYb*sin(phi)*cosA;

		return CL;
	}

	/**
	*  Get the wind axis drag force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.  Angle inputs are used to resolve
	*  components from body axes to wind axes.
	*
	*  @return Wind axis drag force coefficient.
	*  @see     #setAngles
	*/
	public double getCD() {
		double sinP = sin(phi);
		double cosP = cos(phi);
		double sinB = sin(beta);
		double cosB = cos(beta);
		double sinA = sin(alpha);
		double cosA = cos(alpha);

		double CD = CA*cosA*cosB;
		CD -= CYb*(sinP*sinA*cosB + cosP*sinB);
		CD += CN*(cosP*sinA*cosB - sinP*sinB);

		return CD;
	}

	/**
	*  Get the wind axis side force coefficient (CY).  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.  Angle inputs are used to resolve
	*  components from body axes to wind axes.
	*
	*  @return Wind axis drag force coefficient.
	*  @see     #setAngles
	*/
	public double getCY() {
		double sinP = sin(phi);
		double cosP = cos(phi);
		double sinB = sin(beta);
		double cosB = cos(beta);
		double sinA = sin(alpha);
		double cosA = cos(alpha);

		double CY = CA*cosA*sinB;
		CY += CYb*(cosP*cosB - sinP*sinA*sinB);
		CY += CN*(cosP*sinA*sinB + sinP*cosB);

		return CY;
	}

	/**
	*  Get the wind axis pitching moment coefficient.  Non-dimensional
	*  pitching moment coefficient is defined as:
	*  CM = PM/(qbar*Sref*cref)  where PM is the pitching moment,
	*  (defined as positive nose up), qbar is dynamic pressure,
	*  Sref is the reference area, and cref is the reference
	*  chord length.  Angle inputs are used to resolve
	*  components from body axes to wind axes.
	*
	*  @return  Wind axis pitching moment coefficient at the moment reference
	*           center.
	*/
	public double getCM() {
		double sinP = sin(phi);
		double cosP = cos(phi);
		double sinB = sin(beta);
		double cosB = cos(beta);
		double sinA = sin(alpha);
		double cosA = cos(alpha);

	    //  Get references.
	    double cref = getRefLength();
	    double bref = getRefSpan();
	    
	    //  Convert body axis to wind axis.
		double CM = -Clb*bref/cref*cosA*sinB;
		CM += CMb*(cosP*cosB - sinP*sinA*sinB);
		CM -= Cnb*bref/cref*(cosP*sinA*sinB + sinP*cosB);

		return CM;
	}

	/**
	*  Get the wind axis yawing moment coefficient.  Non-dimensional
	*  yawing moment coefficient is defined as:
	*  Cn = YM/(qbar*Sref*bref)  where YM is the yawing moment,
	*  (defined as positive nose right), qbar is dynamic pressure,
	*  Sref is the reference area, and bref is the reference
	*  span length.  Angle inputs are used to resolve
	*  components from body axes to wind axes.
	*
	*  @return  Wind axis yawing moment coefficient at the moment reference
	*           center.
	*/
	public double getCn() {
		double cosA = cos(alpha);

	    //  Get references.
	    double cref = getRefLength();
	    double bref = getRefSpan();
	    
		double Cn = Cnb*cos(phi)*cosA;
		Cn -= Clb*sin(alpha);
		Cn += CMb*cref/bref*sin(phi)*cosA;

		return Cn;
	}

	/**
	*  Get the wind axis rolling moment coefficient.  Non-dimensional
	*  rolling moment coefficient is defined as:
	*  Cl = RM/(qbar*Sref*bref)  where RM is the rolling moment,
	*  (defined as positive right wing up), qbar is dynamic pressure,
	*  Sref is the reference area, and bref is the reference
	*  span length.  Angle inputs are used to resolve
	*  components from body axes to wind axes.
	*
	*  @return  Wind axis rolling moment coefficient at the moment reference
	*           center.
	*/
	public double getCl() {
		double sinP = sin(phi);
		double cosP = cos(phi);
		double sinB = sin(beta);
		double cosB = cos(beta);
		double sinA = sin(alpha);
		double cosA = cos(alpha);

	    //  Get references.
	    double cref = getRefLength();
	    double bref = getRefSpan();
	    
	    //  Convert body axis to wind axis.
		double Cl = Clb*cosA*cosB;
		Cl += CMb*cref/bref*(sinP*sinA*cosB + cosP*sinB);
		Cl += Cnb*(cosP*sinA*cosB - sinP*sinB);

		return Cl;
	}


	/**
	*  Set the stability axis force coefficients.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.  Angle inputs are used to resolve
	*  components from stability axes to body axes.  Make sure you
	*  call setAngles() before calling this method!
	*
	*  @param CLSA  Stability axis lift force coefficient.
	*  @param CDSA  Stability axis drag force coefficient.
	*  @param CYSA  Stability axis side force coefficient.
	*  @see   #setAngles
	*/
	public void setStabilityAxesForces(double CLSA, double CDSA, double CYSA) {
		double sinA = sin(alpha);
		double cosA = cos(alpha);

		CA = CDSA*cosA - CLSA*sinA;
		CYb = CYSA;
		CN = CLSA*cosA + CDSA*sinA;
	}

	/**
	*  Set the stability axis moment coefficients.
	*  Angle inputs are used to resolve components from
	*  stability axes to body axes.  Make sure you
	*  call setAngles() before calling this method!  Moment
	*  axis transfers also depend on having correct
	*  reference span and reference chord values set!
	*
	*  @param CMSA  Stability axis pitching moment coefficient.
	*  @param CnSA  Stability axis yawing moment coefficient.
	*  @param ClSA  Stability axis rolling moment coefficient.
	*  @see   #setAngles
	*/
	public void setStabilityAxesMoments(double CMSA, double CnSA, double ClSA) {
		double sinA = sin(alpha);
		double cosA = cos(alpha);

	   Clb = ClSA*cosA - CnSA*sinA;
	   CMb = CMSA;
	   Cnb = CnSA*cosA + ClSA*sinA;
	}


	/**
	*  Get the stability axis lift force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.  Angle inputs are used to resolve
	*  components from body axes to stability axes.
	*
	*  @return Stability axis lift force coefficient.
	*  @see     #setAngles
	*/
	public double getCLSA() {
		double sinA = sin(alpha);
		double cosA = cos(alpha);
		
		double CLSA = CN*cosA - CA*sinA;
		
		return CLSA;
	}

	/**
	*  Get the stability axis drag force coefficient.  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.  Angle inputs are used to resolve
	*  components from body axes to stability axes.
	*
	*  @return Stability axis drag force coefficient.
	*  @see     #setAngles
	*/
	public double getCDSA() {
		double sinA = sin(alpha);
		double cosA = cos(alpha);

        double CDSA = CA*cosA + CN*sinA;
        
		return CDSA;
	}

	/**
	*  Get the stability axis side force coefficient (CYSA).  Non-dimensional
	*  force coefficients are defined as:  C = F/(qbar*Sref)
	*  where F is force, qbar is dynamic pressure and Sref
	*  is the reference area.  Angle inputs are used to resolve
	*  components from body axes to stability axes.
	*
	*  @return Stability axis drag force coefficient.
	*  @see     #setAngles
	*/
	public double getCYSA() {
		return CYb;
	}

	/**
	*  Get the stability axis pitching moment coefficient.  Non-dimensional
	*  pitching moment coefficient is defined as:
	*  CM = PM/(qbar*Sref*cref)  where PM is the pitching moment,
	*  (defined as positive nose up), qbar is dynamic pressure,
	*  Sref is the reference area, and cref is the reference
	*  chord length.  Angle inputs are used to resolve
	*  components from body axes to stability axes.
	*
	*  @return  Stability axis pitching moment coefficient at the moment reference
	*           center.
	*/
	public double getCMSA() {
		return CMb;
	}

	/**
	*  Get the stability axis yawing moment coefficient.  Non-dimensional
	*  yawing moment coefficient is defined as:
	*  Cn = YM/(qbar*Sref*bref)  where YM is the yawing moment,
	*  (defined as positive nose right), qbar is dynamic pressure,
	*  Sref is the reference area, and bref is the reference
	*  span length.  Angle inputs are used to resolve
	*  components from body axes to stability axes.
	*
	*  @return  Stability axis yawing moment coefficient at the moment reference
	*           center.
	*/
	public double getCnSA() {
		double sinA = sin(alpha);
		double cosA = cos(alpha);
		
		double CnSA = Cnb*cosA - Clb*sinA;
		
		return CnSA;
	}

	/**
	*  Get the stability axis rolling moment coefficient.  Non-dimensional
	*  rolling moment coefficient is defined as:
	*  Cl = RM/(qbar*Sref*bref)  where RM is the rolling moment,
	*  (defined as positive right wing up), qbar is dynamic pressure,
	*  Sref is the reference area, and bref is the reference
	*  span length.  Angle inputs are used to resolve
	*  components from body axes to stability axes.
	*
	*  @return  Stability axis rolling moment coefficient at the moment reference
	*           center.
	*/
	public double getClSA() {
		double sinA = sin(alpha);
		double cosA = cos(alpha);

        double ClSA = Clb*cosA + Cnb*sinA;
        
		return ClSA;
	}


    /**
    *  Method that fills in the wind axis to body axis transformation
    *  matrix contents.
    */
    private void calculateW2B() {

		double sinP = sin(phi);
		double cosP = cos(phi);
		double sinB = sin(beta);
		double cosB = cos(beta);
		double sinA = sin(alpha);
		double cosA = cos(alpha);

        //  Calculate the wind axis to body axis transformation matrix.
		wind2body[0][0] = cosP*cosA;
		wind2body[0][1] = cosP*sinA*cosB - sinP*sinB;
		wind2body[0][2] = cosP*sinA*sinB + sinP*cosB;
		wind2body[1][0] = -sinA;
		wind2body[1][1] = cosA*cosB;
		wind2body[1][2] = cosA*sinB;
		wind2body[2][0] = sinP*cosA;
		wind2body[2][1] = -(sinP*sinA*cosB + cosP*sinB);
		wind2body[2][2] = cosP*cosB - sinP*sinA*sinB;
		
    }
    
	//-----------------------------------------------------------------------------
    /**
    *  Returns the reference chord length in reference units (meters) if it is
    *  defined.  Returns 1.0 if the reference chord is not defined.
    */
    private double getRefLength() {
	    double value = 1;
	    if (ref != null)
	        value = ref.getRefLengthRU();

	    return value;
    }

    /**
    *  Returns the reference span length in reference units (meters) if it is
    *  defined.  Returns 1.0 if the reference span is not defined.
    */
    private double getRefSpan() {
	    double value = 1;
	    if (ref != null)
	        value = ref.getRefSpanRU();

	    return value;
    }

    
	/**
	*  During de-serialization, this will handle the reconstruction
	*  of the wind axis to body axis transformation matrix.  This
	*  method is ONLY called by the Java Serialization mechanism and is
	*  not called by any of my code.
	*/
	private void readObject( java.io.ObjectInputStream in ) throws java.io.IOException, ClassNotFoundException {
	
		// Call the default read object method.
		in.defaultReadObject();

		// Re-allocate the transformation matrix.
		wind2body = new double[3][3];
		
		//  Re-create the contents of the transformation matrix.
		calculateW2B();
	}
	
}
