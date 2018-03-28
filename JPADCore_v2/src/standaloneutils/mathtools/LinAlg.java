/*
 *   LinPack  -- A collection of standard linear algebra routines.
 *
 *   Copyright (C) 2002-2014 by Joseph A. Huwaldt
 *   All rights reserved.
 *   
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
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
package standaloneutils.mathtools;


/**
*  A collection of useful static routines for doing linear
*  algebra (matrix math).  These are Java implementations of
*  some, but not all, of the famous Fortran BLAS and LINPACK algorithms.
*  Basically, I am implementing these algorithms as I need them.
*  If you happen to implement others, I'd love to add them to
*  my collection.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  April 23, 2002
*  @version  April 3, 2014
*/
public final class LinAlg {

    
	/**
	*  Prevent anyone from instantiating this utility class.
	*/
	private LinAlg() {}
	
	//-----------------------------------------------------------------------------------
	
	/**
	*  <p> Method that computes the inverse and determinant of a matrix. </p>
	*
	*  <p> This is a convenience method that allocates working space, and calls
	*      gefa() and gedi() for you.  If you intend to call this method
	*      from a loop, it is MUCH more efficient to call gefa() followed by gedi()
	*      directly re-using working storage space.  Also, if you only need the
	*      determinant or only the inverse, it is slightly faster to call gefa()
	*      and gedi() directly requesting only what you need.
	*  </p>
	*
	*  @param  a     The matrix to be inverted.  On output this matrix will
	*                contain the inverse of the original matrix.
	*  @return The determinant of the matrix is returned.
	*/
	public static float invert(float a[][]) {
		int lda = a[0].length;
		int n = a.length;
		
		//  Allocate work arrays.
		float[] work = new float[n];
		int[] ipvt = new int[n];
		
		//  Factor the matrix using gaussian elimination.
		float rcond = geco(a, lda, n, ipvt, work);
		if (1F + rcond == 1)
			throw new IllegalArgumentException("Matrix is singular (the determinant is zero).");
		
		//  Calculate the determinant and the inverse.
		float[] det = new float[2];
		gedi(a,lda,n,ipvt,work,11,det);
		float determinant = (float)(det[0]*Math.pow(10,det[1]));
		
		return determinant;
	}
	
	/**
	*  <p> Method that computes the inverse and determinant of a matrix. </p>
	*
	*  <p> This is a convenience method that allocates working space, and calls
	*      gefa() and gedi() for you.  If you intend to call this method
	*      from a loop, it is MUCH more efficient to call gefa() followed by gedi()
	*      directly re-using working storage space.  Also, if you only need the
	*      determinant or only the inverse, it is slightly faster to call gefa()
	*      and gedi() directly requesting only what you need.
	*  </p>
	*
	*  @param  a     The matrix to be inverted.  On output this matrix will
	*                contain the inverse of the original matrix.
	*  @return The determinant of the matrix is returned.
	*/
	public static double invert(double a[][]) {
		int lda = a[0].length;
		int n = a.length;
		
		//  Allocate work arrays.
		int[] ipvt = new int[n];
		
		//  Factor the matrix using gaussian elimination.
		int info = gefa(a, lda, n, ipvt);
		if (info != 0)
			throw new IllegalArgumentException("Matrix is singular (the determinant is zero).");
		
		//  Calculate the determinant and the inverse.
		double[] det = new double[2];
		double[] work = new double[n];
		gedi(a,lda,n,ipvt,work,11,det);
		double determinant = det[0]*Math.pow(10,det[1]);
		
		return determinant;
	}
	
	
	/**
	*  <p> Method that factors a double precision matrix by gaussian elimination
	*      and estimates the condition of the matrix.
	*      The method decomposes an n x lda matrix "a" into a product, LU, where
	*      L is a lower triangular matrix and U is an upper triangular matrix.
	*      If rcond is not needed, gefa() is slightly faster.  </p>
	*
	*  <p> To solve  a*x = b , follow geco by gesl.
	*      To compute  inverse(a)*c , follow geco by gesl.
	*      To compute  determinant(a) , follow geco by gedi.
	*      To compute  inverse(a) , follow geco by gedi.
	*  </p>
	*
	*  <p>  Taken from LINPACK.  This version dated 08/14/1978.
	*       Author:  Cleve Moler, University of New Mexico, Argonne National Lab. </p>
	*
	*  @param  a     The matrix to be factored:  a[n][lda].  On return contents are
	*                replaced with an upper triangular matrix and the multipliers
	*                which were used to obtain it.  The factorization can be written
	*                a = L*U where L is a product of permutation and unit lower
	*                triangular matrices and U is upper triangular.
	*  @param  lda   The leading dimension of the array a.
	*  @param  n     The order of the matrix a.
	*  @param  ipvt  On output, an integer vector of pivot indices:  ipvt[n].
	*  @param  z     A work vector, z[n] whose contents are usually unimportant.  If
	*                a is close to a singular matrix, then z is an approximate null
	*                vector in the sense that norm(a*z) = rcond*norm(a)*norm(z).
	*  @return An estimate of the reciprocal condition of the matrix  a .  For the system
	*          a*x = b , relative perturbations in  a  and  b  of size  epsilon  may cause
	*          relative perturbations in  x  of size  epsilon/rcond .  If  rcond  is so small
	*          that the logical expression, 1.0 + rcond == 1.0, is true, then  a  may be
	*          singular to working precision.  In particular,  rcond  is zero  if
	*          exact singularity is detected or the estimate underflows.
	*/
	public static double geco( double a[][], int lda, int n, int ipvt[], double z[]) {

		//	Compute 1-norm of a.
		double anorm = 0;
		for (int j=0; j < n; ++j)
			anorm = Math.max(anorm, asum(n, a[j], 0, 1));
		
		//	Factor.
		int info = gefa( a, lda, n, ipvt);
		
		/*	rcond = 1/(norm(a)*(estimate of norm(inverse(a)))) .
			estimate = norm(z)/norm(y) where  a*z = y  and  trans(a)*y = e .
			trans(a)  is the transpose of a .  the components of  e  are
			chosen to cause maximum local growth in the elements of w  where
			trans(u)*w = e .  the vectors are frequently rescaled to avoid
			overflow.
		*/
		
		//	Solve trans(U)*w = e
		double s;
		double ek=1;
		for (int j=0; j < n; ++j)
			z[j] = 0;
		
		for (int k=0; k < n; ++k) {
			double zk = z[k];
			if (zk != 0)	ek = sign(ek, -zk);
			double akk = a[k][k];
			if (Math.abs(ek - zk) > Math.abs(akk)) {
				s = Math.abs(akk)/Math.abs(ek - zk);
				scal(n, s, z, 0, 1);
				ek *= s;
			}
			
			zk = z[k];
			double wk = ek - zk;
			double wkm = -ek - zk;
			s = Math.abs(wk);
			double sm = Math.abs(wkm);
			if (akk != 0) {
				wk /= akk;
				wkm /= akk;
				
			} else {
				wk = 1;
				wkm = 1;
			}
			
			int kp1 = k + 1;
			if (kp1+1 <= n) {
				for (int j=kp1; j < n; ++j) {
					sm += Math.abs(z[j] + wkm*a[j][k]);
					z[j] += wk*a[j][k];
					s += Math.abs(z[j]);
				}
				if (s < sm) {
					double t = wkm - wk;
					wk = wkm;
					for (int j=kp1; j < n; ++j)
						z[j] += t*a[j][k];
				}
			}
			
			z[k] = wk;
			
		}	//	Next k
		s = 1./asum(n, z, 0, 1);
		scal(n, s, z, 0, 1);
		
		//	Solve trans(L)*y = w.
		for (int kb = 1; kb <= n; ++kb) {
			int k = n - kb;
			int kp1 = k + 1;
			if (kp1 < n)	z[k] += dot(n-kp1, a[k], kp1, 1, z, kp1, 1);
			if (Math.abs(z[k]) > 1.) {
				s = 1./Math.abs(z[k]);
				scal(n, s, z, 0, 1);
			}
			int l = ipvt[k];
			double t = z[l];
			z[l] = z[k];
			z[k] = t;
		}
		s = 1./asum(n, z, 0, 1);
		scal(n, s, z, 0, 1);
		
		//	Solve L*v = y
		double ynorm = 1;
		for (int k=0; k < n; ++k) {
			int l = ipvt[k];
			double t = z[l];
			z[l] = z[k];
			z[k] = t;
			int kp1 = k + 1;
			if (kp1 < n)	axpy(n-kp1, t, a[k], kp1, 1, z, kp1, 1);
			if (Math.abs(z[k]) > 1.) {
				s = 1./Math.abs(z[k]);
				scal(n, s, z, 0, 1);
				ynorm *= s;
			}
		}
		s = 1./asum(n, z, 0, 1);
		scal(n, s, z, 0, 1);
		ynorm *= s;
		
		//	Solve U*z = v
		for (int kb=1; kb <= n; ++kb) {
			int k = n - kb;
			double zk = z[k];
			double akk = a[k][k];
			if (Math.abs(zk) > Math.abs(akk)) {
				s = Math.abs(akk)/Math.abs(zk);
				scal(n, s, z, 0, 1);
				ynorm *= s;
			}
			if (akk != 0)
				z[k] /= akk;
			else
				z[k] = 1;
			axpy(k+1-1, -z[k], a[k], 0, 1, z, 0, 1);
		}
		
		//	Make znorm = 1;
		s = 1./asum(n, z, 0, 1);
		scal(n, s, z, 0, 1);
		ynorm *= s;
		
		double rcond = 0;
		if (anorm != 0)
			rcond = ynorm/anorm;
		
		return rcond;
	}
	
	
	/**
	*  <p> Method that factors a float precision matrix by Gaussian elimination
	*      and estimates the condition of the matrix.
	*      The method decomposes an n x lda matrix "a" into a product, LU, where
	*      L is a lower triangular matrix and U is an upper triangular matrix.
	*      If rcond is not needed, gefa() is slightly faster.  </p>
	*
	*  <p> To solve  a*x = b , follow geco by gesl.
	*      To compute  inverse(a)*c , follow geco by gesl.
	*      To compute  determinant(a) , follow geco by gedi.
	*      To compute  inverse(a) , follow geco by gedi.
	*  </p>
	*
	*  <p>  Taken from LINPACK.  This version dated 08/14/1978.
	*       Author:  Cleve Moler, University of New Mexico, Argonne National Lab. </p>
	*
	*  @param  a     The matrix to be factored:  a[n][lda].  On return contents are
	*                replaced with an upper triangular matrix and the multipliers
	*                which were used to obtain it.  The factorization can be written
	*                a = L*U where L is a product of permutation and unit lower
	*                triangular matrices and U is upper triangular.
	*  @param  lda   The leading dimension of the array a.
	*  @param  n     The order of the matrix a.
	*  @param  ipvt  On output, an integer vector of pivot indices:  ipvt[n].
	*  @param  z     A work vector, z[n] whose contents are usually unimportant.  If
	*                a is close to a singular matrix, then z is an approximate null
	*                vector in the sense that norm(a*z) = rcond*norm(a)*norm(z).
	*  @return An estimate of the reciprocal condition of the matrix  a .  For the system
	*          a*x = b , relative perturbations in  a  and  b  of size  epsilon  may cause
	*          relative perturbations in  x  of size  epsilon/rcond .  If  rcond  is so small
	*          that the logical expression, 1.0 + rcond == 1.0, is true, then  a  may be
	*          singular to working precision.  In particular,  rcond  is zero  if
	*          exact singularity is detected or the estimate underflows.
	*/
	public static float geco( float a[][], int lda, int n, int ipvt[], float z[]) {

		//	Compute 1-norm of a.
		float anorm = 0;
		for (int j=0; j < n; ++j)
			anorm = Math.max(anorm, asum(n, a[j], 0, 1));
		
		//	Factor.
		int info = gefa( a, lda, n, ipvt);
		
		/*	rcond = 1/(norm(a)*(estimate of norm(inverse(a)))) .
			estimate = norm(z)/norm(y) where  a*z = y  and  trans(a)*y = e .
			trans(a)  is the transpose of a .  the components of  e  are
			chosen to cause maximum local growth in the elements of w  where
			trans(u)*w = e .  the vectors are frequently rescaled to avoid
			overflow.
		*/
		
		//	Solve trans(U)*w = e
		float s;
		float ek=1;
		for (int j=0; j < n; ++j)
			z[j] = 0;
		
		for (int k=0; k < n; ++k) {
			float zk = z[k];
			if (zk != 0)	ek = sign(ek, -zk);
			float akk = a[k][k];
			if (Math.abs(ek - zk) > Math.abs(akk)) {
				s = Math.abs(akk)/Math.abs(ek - zk);
				scal(n, s, z, 0, 1);
				ek *= s;
			}
			
			zk = z[k];
			float wk = ek - zk;
			float wkm = -ek - zk;
			s = Math.abs(wk);
			float sm = Math.abs(wkm);
			if (akk != 0) {
				wk /= akk;
				wkm /= akk;
				
			} else {
				wk = 1;
				wkm = 1;
			}
			
			int kp1 = k + 1;
			if (kp1+1 <= n) {
				for (int j=kp1; j < n; ++j) {
					sm += Math.abs(z[j] + wkm*a[j][k]);
					z[j] += wk*a[j][k];
					s += Math.abs(z[j]);
				}
				if (s < sm) {
					float t = wkm - wk;
					wk = wkm;
					for (int j=kp1; j < n; ++j)
						z[j] += t*a[j][k];
				}
			}
			
			z[k] = wk;
			
		}	//	Next k
		s = 1/asum(n, z, 0, 1);
		scal(n, s, z, 0, 1);
		
		//	Solve trans(L)*y = w.
		for (int kb = 1; kb <= n; ++kb) {
			int k = n - kb;
			int kp1 = k + 1;
			if (kp1 < n)	z[k] += dot(n-kp1, a[k], kp1, 1, z, kp1, 1);
			if (Math.abs(z[k]) > 1.) {
				s = 1/Math.abs(z[k]);
				scal(n, s, z, 0, 1);
			}
			int l = ipvt[k];
			float t = z[l];
			z[l] = z[k];
			z[k] = t;
		}
		s = 1/asum(n, z, 0, 1);
		scal(n, s, z, 0, 1);
		
		//	Solve L*v = y
		float ynorm = 1;
		for (int k=0; k < n; ++k) {
			int l = ipvt[k];
			float t = z[l];
			z[l] = z[k];
			z[k] = t;
			int kp1 = k + 1;
			if (kp1 < n)	axpy(n-kp1, t, a[k], kp1, 1, z, kp1, 1);
			if (Math.abs(z[k]) > 1.) {
				s = 1/Math.abs(z[k]);
				scal(n, s, z, 0, 1);
				ynorm *= s;
			}
		}
		s = 1/asum(n, z, 0, 1);
		scal(n, s, z, 0, 1);
		ynorm *= s;
		
		//	Solve U*z = v
		for (int kb=1; kb <= n; ++kb) {
			int k = n - kb;
			float zk = z[k];
			float akk = a[k][k];
			if (Math.abs(zk) > Math.abs(akk)) {
				s = Math.abs(akk)/Math.abs(zk);
				scal(n, s, z, 0, 1);
				ynorm *= s;
			}
			if (akk != 0)
				z[k] /= akk;
			else
				z[k] = 1;
			axpy(k+1-1, -z[k], a[k], 0, 1, z, 0, 1);
		}
		
		//	Make znorm = 1;
		s = 1/asum(n, z, 0, 1);
		scal(n, s, z, 0, 1);
		ynorm *= s;
		
		float rcond = 0;
		if (anorm != 0)
			rcond = ynorm/anorm;
		
		return rcond;
	}
	
	
	/**
	*  Returns the absolute value of "a" times the sign of "b".
	*/
	private static double sign(double a, double b) {
		return Math.abs(a)*(b < 0 ? -1 : 1);
	}
	

	/**
	*  Returns the absolute value of "a" times the sign of "b".
	*/
	private static float sign(float a, float b) {
		return Math.abs(a)*(b < 0 ? -1 : 1);
	}
	

	/**
	*  <p> Method that factors a double precision matrix by Gaussian elimination.
	*      The method decomposes an n x lda matrix "a" into a product, LU, where
	*      L is a lower triangular matrix and U is an upper triangular matrix.
	*      This is usually called by geco(), but it can be called
	*      directly with a saving in time if rcond is not needed.
	*      (time for geco) = (1 + 9/n)*(time for gefa).  </p>
	*
	*  <p> To solve  a*x = b , follow gefa by gesl.
	*      To compute  inverse(a)*c , follow gefa by gesl.
	*      To compute  determinant(a) , follow gefa by gedi.
	*      To compute  inverse(a) , follow gefa by gedi.
	*  </p>
	*
	*  <p>  Taken from LINPACK.  This version dated 08/14/1978.
	*       Author:  Cleve Moler, University of New Mexico, Argonne National Lab. </p>
	*
	*  @param  a     The matrix to be factored:  a[n][lda].  On return contents are
	*                replaced with an upper triangular matrix and the multipliers
	*                which were used to obtain it.  The factorization can be written
	*                a = L*U where L is a product of permutation and unit lower
	*                triangular matrices and U is upper triangular.
	*  @param  lda   The leading dimension of the array a.
	*  @param  n     The order of the matrix a.
	*  @param  ipvt  On output, an integer vector of pivot indices:  ipvt[n].
	*  @return A return value of 0 indicates a normal value.  A value "k"
	*          is returned if u[k][k] == 0.0.  This is not an error condition
	*          for this subroutine, but it does indicate that gesl or gedi will
	*          divide by zero if called.  Use rcond in geco for reliable indication
	*          of singularity.
	*/
	public static int gefa( double a[][], int lda, int n, int ipvt[]) {

		// Gaussian elimination with partial pivoting

		int info = 0;
		int nm1 = n - 1;
		if (nm1 >=  0) {
			for (int k = 0; k < nm1; k++) {
				double[] col_k = a[k];
				int kp1 = k + 1;

				// Find l = pivot index

				int l = iamax(n-k, col_k, k, 1) + k;
				ipvt[k] = l;

				// Zero pivot implies this column already triangularized

				if (col_k[l] != 0) {

					// Interchange if necessary

					double t;
					if (l != k) {
						t = col_k[l];
						col_k[l] = col_k[k];
						col_k[k] = t;
					}

					// Compute multipliers

					t = -1.0/col_k[k];
					scal(n-kp1, t, col_k, kp1, 1);

					// Row elimination with column indexing

					for (int j = kp1; j < n; j++) {
						double[] col_j = a[j];
						t = col_j[l];
						if (l != k) {
							col_j[l] = col_j[k];
							col_j[k] = t;
						}
						axpy(n-kp1, t, col_k, kp1, 1, col_j, kp1, 1);
					}
				} else {
					info = k;
				}
			}
		}

		--n;
		ipvt[n] = n;
		if (a[n][n] == 0) info = n;

		return info;
	}


	/**
	*  <p> Method that factors a single precision matrix by gaussian elimination.
	*      The method decomposes an n x lda matrix "a" into a product, LU, where
	*      L is a lower triangular matrix and U is an upper triangular matrix.
	*      This is usually called by geco(), but it can be called
	*      directly with a saving in time if rcond is not needed.
	*      (time for geco) = (1 + 9/n)*(time for gefa).  </p>
	*
	*  <p> To solve  a*x = b , follow gefa by gesl.
	*      To compute  inverse(a)*c , follow gefa by gesl.
	*      To compute  determinant(a) , follow gefa by gedi.
	*      To compute  inverse(a) , follow gefa by gedi.
	*  </p>
	*
	*  <p>  Taken from LINPACK.  This version dated 08/14/1978.
	*       Author:  Cleve Moler, University of New Mexico, Argonne National Lab. </p>
	*
	*  @param  a     The matrix to be factored:  a[n][lda].  On return contents are
	*                replaced with an upper triangular matrix and the multipliers
	*                which were used to obtain it.  The factorization can be written
	*                a = L*U where L is a product of permutation and unit lower
	*                triangular matrices and U is upper triangular.
	*  @param  lda   The leading dimension of the array a.
	*  @param  n     The order of the matrix a.
	*  @param  ipvt  On output, an integer vector of pivot indices:  ipvt[n].
	*  @return A return value of 0 indicates a normal value.  A value "k"
	*          is returned if u[k][k] == 0.0.  This is not an error condition
	*          for this subroutine, but it does indicate that gesl or gedi will
	*          divide by zero if called.  Use rcond in geco for reliable indication
	*          of singularity.
	*/
	public static int gefa( float a[][], int lda, int n, int ipvt[]) {

		// Gaussian elimination with partial pivoting

		int info = 0;
		int nm1 = n - 1;
		if (nm1 >=  0) {
			for (int k = 0; k < nm1; k++) {
				float[] col_k = a[k];
				int kp1 = k + 1;

				// Find l = pivot index

				int l = iamax(n-k, col_k, k, 1) + k;
				ipvt[k] = l;

				// Zero pivot implies this column already triangularized

				if (col_k[l] != 0) {

					// Interchange if necessary

					float t;
					if (l != k) {
						t = col_k[l];
						col_k[l] = col_k[k];
						col_k[k] = t;
					}

					// Compute multipliers

					t = -1/col_k[k];
					scal(n-kp1, t, col_k, kp1, 1);

					// Row elimination with column indexing

					for (int j = kp1; j < n; j++) {
						float[] col_j = a[j];
						t = col_j[l];
						if (l != k) {
							col_j[l] = col_j[k];
							col_j[k] = t;
						}
						axpy(n-kp1, t, col_k, kp1, 1, col_j, kp1, 1);
					}
				} else {
					info = k;
				}
			}
		}

		--n;
		ipvt[n] = n;
		if (a[n][n] == 0) info = n;

		return info;
	}


	/**
	*  <p> Solves the double precision system a * x = b  or  trans(a) * x = b
	*      using the LU decomposition computed by geco or gefa.  On return
	*      from this method, b will be replaced with the solution vector x.  </p>
	*
	*  <p> To compute  inverse(a) * c  where  c  is a matrix
	*      with  p  columns:  <p>
	*  <code>
	*      geco(a, lda, n, ipvt, rcond, z)
	*      if (!rcond is too small){
	*          for (j=0, j < p, j++)
	*              gesl(a, lda, n, ipvt, c[j][0], 0);
	*      }
	*  </code>
	*
	*  <p>  Taken from LINPACK.  This version dated 08/14/1978.
	*       Author:  Cleve Moler, University of New Mexico, Argonne National Lab. </p>
	*
	*  @param  a    The output from geco or gefa:  a[n][lda].
	*  @param  lda  The leading dimension of the array a.
	*  @param  n    The order of the matrix a.
	*  @param  ipvt The pivot vector output from geco or gefa: ipvt[n].
	*
	*  @param  b    The input right hand side vector:  b[n].  b is replaced with the
	*               solution vector "x" on completion.
	*  @param  job  Set == 0 to solve a*x = b.  Make nonzero to
	*               solve trans(a)*x = b where trans(a) is the transpose of a.
	*  @throws  A division by zero will occur if the input factor contains a
	*          zero on the diagonal.  Technically this indicates singularity
	*          but it is often caused by improper arguments or improper
	*          setting of lda .  It will not occur if the subroutines are
	*          called correctly and if geco has set rcond > 0.0
	*          or gefa has returned a value > 0.
	*/
	public static void gesl( double a[][], int lda, int n, int ipvt[], double b[], int job)  {
		double t;
		int k,kb,l,kp1;

		int nm1 = n - 1;
		if (job == 0) {

			// job = 0 , solve  a * x = b.  first solve  L*y = b

			if (nm1 >= 1) {
				for (k = 0; k < nm1; k++) {
					l = ipvt[k];
					t = b[l];
					if (l != k){
						b[l] = b[k];
						b[k] = t;
					}
					kp1 = k + 1;
					axpy(n-kp1, t, a[k], kp1, 1, b, kp1, 1);
				}
			}

			// Now solve  U*x = y

			for (kb = 1; kb <= n; kb++) {
				k = n - kb;
				b[k] /= a[k][k];
				t = -b[k];
				axpy(k, t, a[k], 0, 1, b, 0, 1);
			}
			
		} else {

			// job = nonzero, solve  trans(a) * x = b.  first solve  trans(U)*y = b

			for (k = 0; k < n; k++) {
				t = dot(k, a[k], 0, 1, b, 0, 1);
				b[k] = (b[k] - t)/a[k][k];
			}

			// Now solve trans(L)*x = y

			if (nm1 >= 1) {
				for (kb = 1; kb < nm1; kb++) {
					k = n - (kb+1);
					kp1 = k + 1;
					b[k] += dot(n-kp1, a[k], kp1, 1, b, kp1, 1);
					l = ipvt[k];
					if (l != k) {
						t = b[l];
						b[l] = b[k];
						b[k] = t;
					}
				}
			}
		}
	}


	/**
	*  <p> Solves the single precision system a * x = b  or  trans(a) * x = b
	*      using the LU decomposition computed by geco or gefa.  On return
	*      from this method, b will be replaced with the solution vector x.  </p>
	 *
	 *  <p> To compute  inverse(a) * c  where  c  is a matrix
	 *      with  p  columns:  <p>
	 *  <code>
	 *      geco(a, lda, n, ipvt, rcond, z)
	 *      if (!rcond is too small){
	 *          for (j=0, j < p, j++)
	 *              gesl(a, lda, n, ipvt, c[j][0], 0);
	 *      }
	 *  </code>
	 *
	 *  <p>  Taken from LINPACK.  This version dated 08/14/1978.
	 *       Author:  Cleve Moler, University of New Mexico, Argonne National Lab. </p>
	 *
	 *  @param  a    The output from geco or gefa:  a[n][lda].
	 *  @param  lda  The leading dimension of the array a.
	 *  @param  n    The order of the matrix a.
	 *  @param  ipvt The pivot vector output from geco or gefa: ipvt[n].
	 *
	 *  @param  b    The input right hand side vector:  b[n].  b is replaced with the
	 *               solution vector "x" on completion.
	 *  @param  job  Set == 0 to solve a*x = b.  Make nonzero to
	 *               solve trans(a)*x = b where trans(a) is the transpose of a.
	 *  @throws  A division by zero will occur if the input factor contains a
	 *          zero on the diagonal.  Technically this indicates singularity
	 *          but it is often caused by improper arguments or improper
	 *          setting of lda .  It will not occur if the subroutines are
	 *          called correctly and if geco has set rcond > 0.0
	 *          or gefa has returned a value > 0.
	 */
	public static void gesl( float a[][], int lda, int n, int ipvt[], float b[], int job)  {
		float t;
		int k,kb,l,kp1;

		int nm1 = n - 1;
		if (job == 0) {

			// job = 0 , solve  a * x = b.  first solve  L*y = b

			if (nm1 >= 1) {
				for (k = 0; k < nm1; k++) {
					l = ipvt[k];
					t = b[l];
					if (l != k){
						b[l] = b[k];
						b[k] = t;
					}
					kp1 = k + 1;
					axpy(n-kp1, t, a[k], kp1, 1, b, kp1, 1);
				}
			}

			// Now solve  U*x = y

			for (kb = 1; kb <= n; kb++) {
				k = n - kb;
				b[k] /= a[k][k];
				t = -b[k];
				axpy(k, t, a[k], 0, 1, b, 0, 1);
			}

		} else {

			// job = nonzero, solve  trans(a) * x = b.  first solve  trans(U)*y = b

			for (k = 0; k < n; k++) {
				t = dot(k, a[k], 0, 1, b, 0, 1);
				b[k] = (b[k] - t)/a[k][k];
			}

			// Now solve trans(L)*x = y

			if (nm1 >= 1) {
				for (kb = 1; kb < nm1; kb++) {
					k = n - (kb+1);
					kp1 = k + 1;
					b[k] += dot(n-kp1, a[k], kp1, 1, b, kp1, 1);
					l = ipvt[k];
					if (l != k) {
						t = b[l];
						b[l] = b[k];
						b[k] = t;
					}
				}
			}
		}
	}


	/**
	*  <p> Method that computes the determinant and inverse of a matrix using the
	*      factors computed by geco or gefa.
	*
	*  <p> To compute  determinant(a) , follow geco by gedi.
	*      To compute  inverse(a) , follow geco by gedi.
	*  </p>
	*
	*  <p> A division by zero will occur if the input factor contains
    *      a zero on the diagonal and the inverse is requested.
    *      It will not occur if the subroutines are called correctly
    *      and if geco has set rcond > 0.0 or gefa has set
    *      info == 0 .
	*  </p>
	*
	*  <p>  Taken from LINPACK.  This version dated 08/14/1978.
	*       Author:  Cleve Moler, University of New Mexico, Argonne National Lab. </p>
	*
	*  @param  a     The matrix output from geco or gefa:  a[n][lda].  On output, if requested,
	*                this will contain the inverse of the original matrix input into geco or gefa.
	*  @param  lda   The leading dimension of the array a.
	*  @param  n     The order of the matrix a.
	*  @param  ipvt  The pivot vector from geco or gefa.
	*  @param  work  A work vector, work[n] whose contents will be destroyed.
	*  @param  job   Job code defined as follows:   job=11 -- both determinant and inverse.
	*												job=01 -- inverse only.
	*												job=10 -- determinant only.
	*  @param  det   The determinant of the original matrix if requested.  Otherwise, it
	*                will not be accessed.  determinant = det[0] * 10.0**det[1]
	*                                       with  1.0 <= abs(det[0]) < 10.0
	*                                       or  det[0] == 0.0
	*/
	public static void gedi( float a[][], int lda, int n, int ipvt[], float work[], int job, float[] det) {
		
		if (job/10 != 0) {
			//  Compute the determinant.
			
			det[0] = 1;
			det[1] = 0;
			float ten = 10;
			for (int i=0; i < n; ++i) {
				if (ipvt[i] != i)   det[0] *= -1;
				det[0] = a[i][i]*det[0];
				if (det[0] != 0) {
					while (Math.abs(det[0]) < 1) {
						det[0] = ten*det[0];
						det[1] -= 1;
					}
					while (Math.abs(det[0]) >= ten) {
						det[0] /= 10;
						det[1] += 1;
					}
				}
			}
		}
		
		if (job == 1 || job == 11) {
			//  Compute the inverse.
			
			for (int k=0; k < n; ++k) {
				a[k][k] = 1/a[k][k];
				float t = -a[k][k];
				scal(k+1-1,t,a[k],0,1);
				int kp1 = k + 1;
				if (n >= kp1+1) {
					for (int j=kp1; j < n; ++j) {
						t = a[j][k];
						a[j][k] = 0;
						axpy(k+1, t, a[k], 0, 1, a[j], 0, 1);
					}
				}
			}
			
			//  Form inverse(u)*inverse(l)
			int nm1 = n - 1;
			if (nm1 >= 1) {
				for (int kb=1; kb <= nm1; ++kb) {
					int k = n - kb - 1;
					int kp1 = k + 1;
					for (int i=kp1; i < n; ++i) {
						work[i] = a[k][i];
						a[k][i] = 0;
					}
					for (int j=kp1; j < n; ++j) {
						float t = work[j];
						axpy(n,t,a[j],0,1,a[k],0,1);
					}
					int l = ipvt[k];
					if (l != k) swap(n,a[k],0,1,a[l],0,1);
				}
			}
		}
		
	}
	
	/**
	*  <p> Method that computes the determinant and inverse of a matrix using the
	*      factors computed by geco or gefa.
	*
	*  <p> To compute  determinant(a) , follow geco by gedi.
	*      To compute  inverse(a) , follow geco by gedi.
	*  </p>
	*
	*  <p> A division by zero will occur if the input factor contains
    *      a zero on the diagonal and the inverse is requested.
    *      It will not occur if the subroutines are called correctly
    *      and if geco has set rcond > 0.0 or gefa has set
    *      info == 0 .
	*  </p>
	*
	*  <p>  Taken from LINPACK.  This version dated 08/14/1978.
	*       Author:  Cleve Moler, University of New Mexico, Argonne National Lab. </p>
	*
	*  @param  a     The matrix output from geco or gefa:  a[n][lda].  On output, if requested,
	*                this will contain the inverse of the original matrix input into geco or gefa.
	*  @param  lda   The leading dimension of the array a.
	*  @param  n     The order of the matrix a.
	*  @param  ipvt  The pivot vector from geco or gefa.
	*  @param  work  A work vector, work[n] whose contents will be destroyed.
	*  @param  job   Job code defined as follows:   job=11 -- both determinant and inverse.
	*												job=01 -- inverse only.
	*												job=10 -- determinant only.
	*  @param  det   The determinant of the original matrix if requested.  Otherwise, it
	*                will not be accessed.  determinant = det[0] * 10.0**det[1]
	*                                       with  1.0 <= abs(det[0]) < 10.0
	*                                       or  det[0] == 0.0
	*/
	public static void gedi( double a[][], int lda, int n, int ipvt[], double work[], int job, double[] det) {
		
		if (job/10 != 0) {
			//  Compute the determinant.
			
			det[0] = 1;
			det[1] = 0;
			double ten = 10;
			for (int i=0; i < n; ++i) {
				if (ipvt[i] != i)   det[0] *= -1;
				det[0] = a[i][i]*det[0];
				if (det[0] != 0) {
					while (Math.abs(det[0]) < 1) {
						det[0] = ten*det[0];
						det[1] -= 1;
					}
					while (Math.abs(det[0]) >= ten) {
						det[0] /= 10;
						det[1] += 1;
					}
				}
			}
		}
		
		if (job == 1 || job == 11) {
			//  Compute the inverse.
			
			for (int k=0; k < n; ++k) {
				a[k][k] = 1/a[k][k];
				double t = -a[k][k];
				scal(k+1-1,t,a[k],0,1);
				int kp1 = k + 1;
				if (n >= kp1+1) {
					for (int j=kp1; j < n; ++j) {
						t = a[j][k];
						a[j][k] = 0;
						axpy(k+1, t, a[k], 0, 1, a[j], 0, 1);
					}
				}
			}
			
			//  Form inverse(u)*inverse(l)
			int nm1 = n - 1;
			if (nm1 >= 1) {
				for (int kb=1; kb <= nm1; ++kb) {
					int k = n - kb - 1;
					int kp1 = k + 1;
					for (int i=kp1; i < n; ++i) {
						work[i] = a[k][i];
						a[k][i] = 0;
					}
					for (int j=kp1; j < n; ++j) {
						double t = work[j];
						axpy(n,t,a[j],0,1,a[k],0,1);
					}
					int l = ipvt[k];
					if (l != k) swap(n,a[k],0,1,a[l],0,1);
				}
			}
		}
		
	}
	
	
	/**
	*  Multiplies a constant times a portion of a vector and adds the result to a
	*  portion of another vector with double precision.
	*  y = a*x + y
	*  Translation of FORTRAN BLAS routine DAXPY by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n      The number of elements to be operated on in the vectors.
	*  @param  a      The constant to multiply into a portion of the vector.
	*  @param  x      The vector being multiplied by the constant: x[0..x_off...n+x_off-1].
	*  @param  x_off  Offset into the x vector to begin operating.
	*  @param  incx   The step size to be used when moving through the x vector.
	*  @param  y      The vector to add to the a*x vector: y[0..y_off...n+y_off-1].
	*  @param  y_off  Offset into the y vector to begin operating with.
	*  @param  incy   The step size to be used when moving through the y vector.
	*/
	private static void axpy( int n, double a, double x[], int x_off, int incx,
				   double y[], int y_off, int incy)  {
		int i;

		if ((n > 0) && (a != 0)) {
			if (incx != 1 || incy != 1) {

				// Code for unequal increments or equal increments not equal to 1

				int ix = x_off;
				int iy = y_off;
				if (incx < 0) ix = (-n+1)*incx + x_off;
				if (incy < 0) iy = (-n+1)*incy + y_off;
				for (i = 0;i < n; i++) {
					y[iy] += a*x[ix];
					ix += incx;
					iy += incy;
				}
				
			} else {

				// Code for both increments equal to 1
				
				for (i=0; i < n; i++)
					y[i + y_off] += a*x[i + x_off];
			}
		}
	}


	/**
	*  Multiplies a constant times a portion of a vector and adds the result to a
	*  portion of another vector with single precision.
	*  y = a*x + y
	*  Translation of FORTRAN BLAS routine SAXPY by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n      The number of elements to be operated on in the vectors.
	*  @param  a      The constant to multiply into a portion of the vector.
	*  @param  x      The vector being multipled by the constant: x[0..x_off...n+x_off-1].
	*  @param  x_off  Offset into the x vector to begin operating.
	*  @param  incx   The step size to be used when moving through the x vector.
	*  @param  y      The vector to add to the a*x vector: y[0..y_off...n+y_off-1].
	*  @param  y_off  Offset into the y vector to begin operating with.
	*  @param  incy   The step size to be used when moving through the y vector.
	*/
	private static void axpy( int n, float a, float x[], int x_off, int incx,
								 float y[], int y_off, int incy)  {
		int i;

		if ((n > 0) && (a != 0)) {
			if (incx != 1 || incy != 1) {

				// Code for unequal increments or equal increments not equal to 1

				int ix = x_off;
				int iy = y_off;
				if (incx < 0) ix = (-n+1)*incx + x_off;
				if (incy < 0) iy = (-n+1)*incy + y_off;
				for (i = 0;i < n; i++) {
					y[iy] += a*x[ix];
					ix += incx;
					iy += incy;
				}

			} else {

				// Code for both increments equal to 1

				for (i=0; i < n; i++)
					y[i + y_off] += a*x[i + x_off];
			}
		}
	}


	/**
	*  Forms the dot product of portions of two vectors with double precision.
	*  Translated from FORTRAN BLAS routine DDOT by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n       The number of elements to work with in the vectors.
	*  @param  x       The 1st vector:  x[0..x_off...n+x_off-1].
	*  @param  x_off   The offset into the x vector to beginning working with.
	*  @param  incx    The step size to use when moving through the x vector.
	*  @param  y       The 2nd vector:  y[0..y_off...n+y_off-1].
	*  @param  y_off   The offset into the y vector to begin working with.
	*  @param  incy    The step size to use when moving through the y vector.
	*  @return The dot product of the portions of the two vectors specified.
	*/
	public static double dot( int n, double x[], int x_off, int incx, double y[],
					int y_off, int incy)  {
		int i;

		double temp = 0;

		if (n > 0) {

			if (incx != 1 || incy != 1) {

				// Code for unequal increments or equal increments not equal to 1

				int ix = x_off;
				int iy = y_off;
				if (incx < 0) ix = (-n+1)*incx + x_off;
				if (incy < 0) iy = (-n+1)*incy + y_off;
				for (i = 0; i < n; i++) {
					temp += x[ix]*y[iy];
					ix += incx;
					iy += incy;
				}
			} else {

				// Code for both increments equal to 1

				for (i=0;i < n; i++)
					temp += x[i + x_off]*y[i + y_off];
			}
		}
		
		return temp;
	}


	/**
	*  Forms the dot product of portions of two vectors with single precision.
	*  Translated from FORTRAN BLAS routine SDOT by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n       The number of elements to work with in the vectors.
	*  @param  x       The 1st vector:  x[0..x_off...n+x_off-1].
	*  @param  x_off   The offset into the x vector to beginning working with.
	*  @param  incx    The step size to use when moving through the x vector.
	*  @param  y       The 2nd vector:  y[0..y_off...n+y_off-1].
	*  @param  y_off   The offset into the y vector to begin working with.
	*  @param  incy    The step size to use when moving through the y vector.
	*  @return The dot product of the portions of the two vectors specified.
	*/
	public static float dot( int n, float x[], int x_off, int incx, float y[],
					int y_off, int incy)  {
		int i;

		float temp = 0;

		if (n > 0) {

			if (incx != 1 || incy != 1) {

				// Code for unequal increments or equal increments not equal to 1

				int ix = x_off;
				int iy = y_off;
				if (incx < 0) ix = (-n+1)*incx + x_off;
				if (incy < 0) iy = (-n+1)*incy + y_off;
				for (i = 0; i < n; i++) {
					temp += x[ix]*y[iy];
					ix += incx;
					iy += incy;
				}
			} else {

				// Code for both increments equal to 1

				for (i=0;i < n; i++)
					temp += x[i + x_off]*y[i + y_off];
			}
		}
		
		return temp;
	}


	/**
	*  Scales a portion of a vector by a constant:  x = a*x with double precision.
	*  Translation of FORTRAN BLAS routine by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n     The number of elements in the vector x to scale.
	*  @param  a     The factor to multiply the elements in x by.
	*  @param  x     The vector to multiply a portion of:  x[0..x_off...n+x_off-1].
	*  @param  x_off Offset into the vector to begin working with.
	*  @param  incx  Step size to use when moving through x vector.
	*/
	public static void scal( int n, double a, double x[], int x_off, int incx) {

		if (n > 0 && incx > 0) {
			if (incx != 1) {

				// Code for increment not equal to 1

				int nincx = n*incx + x_off;
				for (int i = x_off; i < nincx; i += incx)
					x[i] *= a;
					
			} else {

				// code for increment equal to 1

				n += x_off;
				for (int i = x_off; i < n; i++)
					x[i] *= a;
			}
		}
	}


	/**
	*  Scales a portion of a vector by a constant:  x = a*x with single precision.
	*  Translation of FORTRAN BLAS routine by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n     The number of elements in the vector x to scale.
	*  @param  a     The factor to multiply the elements in x by.
	*  @param  x     The vector to multiply a portion of:  x[0..x_off...n+x_off-1].
	*  @param  x_off Offset into the vector to begin working with.
	*  @param  incx  Step size to use when moving through x vector.
	*/
	public static void scal( int n, float a, float x[], int x_off, int incx) {
		int i;

		if (n > 0 && incx > 0) {
			if (incx != 1) {

				// Code for increment not equal to 1

				int nincx = n*incx + x_off;
				for (i = x_off; i < nincx; i += incx)
					x[i] *= a;
					
			} else {

				// code for increment equal to 1

				n += x_off;
				for (i = x_off; i < n; i++)
					x[i] *= a;
			}
		}
	}


	/**
	*  Interchanges a portion of two vectors.
	*  Translation of FORTRAN BLAS routine by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n      The number of elements to be operated on in the vectors.
	*  @param  x      The 1st vector to swap: x[0..x_off...n+x_off-1].
	*  @param  x_off  Offset into the x vector to begin operating.
	*  @param  incx   The step size to be used when moving through the x vector.
	*  @param  y      The 2nd vector to swap: y[0..y_off...n+y_off-1].
	*  @param  y_off  Offset into the y vector to begin operating with.
	*  @param  incy   The step size to be used when moving through the y vector.
	*/
	private static void swap( int n, double x[], int x_off, int incx, double y[], int y_off, int incy)  {
		int i;

		if (n > 0) {
			if (incx != 1 || incy != 1) {

				// Code for unequal increments or equal increments not equal to 1

				int ix = x_off;
				int iy = y_off;
				if (incx < 0) ix = (-n+1)*incx + x_off;
				if (incy < 0) iy = (-n+1)*incy + y_off;
				for (i = 0;i < n; i++) {
					double temp = x[ix];
					x[ix] = y[iy];
					y[iy] = temp;
					ix += incx;
					iy += incy;
				}
				
			} else {

				// Code for both increments equal to 1

				int ix = x_off;
				int iy = y_off;
				for (i=0; i < n; ++i, ++ix, ++iy) {
					double temp = x[ix];
					x[ix] = y[iy];
					y[iy] = temp;
				}
			}
		}
	}

	/**
	*  Interchanges a portion of two vectors.
	*  Translation of FORTRAN BLAS routine by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n      The number of elements to be operated on in the vectors.
	*  @param  x      The 1st vector to swap: x[0..x_off...n+x_off-1].
	*  @param  x_off  Offset into the x vector to begin operating.
	*  @param  incx   The step size to be used when moving through the x vector.
	*  @param  y      The 2nd vector to swap: y[0..y_off...n+y_off-1].
	*  @param  y_off  Offset into the y vector to begin operating with.
	*  @param  incy   The step size to be used when moving through the y vector.
	*/
	private static void swap( int n, float x[], int x_off, int incx, float y[], int y_off, int incy)  {
		int i;

		if (n > 0) {
			if (incx != 1 || incy != 1) {

				// Code for unequal increments or equal increments not equal to 1

				int ix = x_off;
				int iy = y_off;
				if (incx < 0) ix = (-n+1)*incx + x_off;
				if (incy < 0) iy = (-n+1)*incy + y_off;
				for (i = 0;i < n; i++) {
					float temp = x[ix];
					x[ix] = y[iy];
					y[iy] = temp;
					ix += incx;
					iy += incy;
				}
				
			} else {

				// Code for both increments equal to 1

				int ix = x_off;
				int iy = y_off;
				for (i=0; i < n; ++i, ++ix, ++iy) {
					float temp = x[ix];
					x[ix] = y[iy];
					y[iy] = temp;
				}
			}
		}
	}


	/**
	*  Finds the index of the element in a portion of a vector having
	*  max. absolute value with double precision.
	*  Translation of FORTRAN BLAS routine by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n       Number of elements in vector x to search.
	*  @param  x       The vector x being searched for maximum value:  x[0..x_off...n+x_off-1].
	*  @param  x_off   Offset into vector x to begin searching.
	*  @param  incx    Step size to use when moving through vector x.
	*  @return The maximum value found in the range x[x_off...x_off+n-1].
	*/
	public static int iamax( int n, double x[], int x_off, int incx) {
		double max, temp;
		int i, itemp=0;

		if (n < 1) {
			itemp = -1;
		} else if (n ==1) {
			itemp = 0;
		} else if (incx != 1) {

			// Code for increment not equal to 1

			max = Math.abs(x[x_off]);
			int ix = x_off + incx;
			for (i = 1; i < n; i++) {
				temp = Math.abs(x[ix]);
				if (temp > max)  {
					itemp = i;
					max = temp;
				}
				ix += incx;
			}
		} else {

			// Code for increment equal to 1

			max = Math.abs(x[x_off]);
			for (i = 1; i < n; i++) {
				temp = Math.abs(x[i + x_off]);
				if (temp > max) {
					itemp = i;
					max = temp;
				}
			}
		}
		
		return itemp;
	}


	/**
	*  Finds the index of the element in a portion of a vector having
	*  max. absolute value with single precision.
	*  Translation of FORTRAN BLAS routine by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n       Number of elements in vector x to search.
	*  @param  x       The vector x being searched for maximum value:  x[0..x_off...n+x_off-1].
	*  @param  x_off   Offset into vector x to begin searching.
	*  @param  incx    Step size to use when moving through vector x.
	*  @return The maximum value found in the range x[x_off...x_off+n-1].
	*/
	public static int iamax( int n, float x[], int x_off, int incx) {
		float max, temp;
		int i, itemp=0;

		if (n < 1) {
			itemp = -1;
		} else if (n ==1) {
			itemp = 0;
		} else if (incx != 1) {

			// Code for increment not equal to 1

			max = Math.abs(x[x_off]);
			int ix = x_off + incx;
			for (i = 1; i < n; i++) {
				temp = Math.abs(x[ix]);
				if (temp > max)  {
					itemp = i;
					max = temp;
				}
				ix += incx;
			}
		} else {

			// Code for increment equal to 1

			max = Math.abs(x[x_off]);
			for (i = 1; i < n; i++) {
				temp = Math.abs(x[i + x_off]);
				if (temp > max) {
					itemp = i;
					max = temp;
				}
			}
		}
		
		return itemp;
	}


	/**
	*  Finds the sum of the absolute values of the elements of a double vector.
	*  Translation of FORTRAN BLAS routine by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n       Number of elements in vector x to sum.
	*  @param  x       The vector x being summed:  x[0..x_off...n+xoff-1].
	*  @param  x_off   Offset into vector x to begin summing.
	*  @param  incx    Step size to use when moving through the x vector.
	*  @return The sum of the absolute values of the n elements of the
	*           vector x.
	*/
	public static double asum( int n, double x[], int x_off, int incx) {
		double asum = 0;
		if (n > 0 && incx > 0) {
			if ( incx != 1) {
			
				//	Code for increment not equal to 1.
				int nincx = n*incx + x_off;
				for (int i=x_off; i < nincx; i += incx)
					asum += Math.abs(x[i]);
				
			} else {
				
				//	Code for increment equal to 1.
				n += x_off;
				for (int i=x_off; i < n; ++i)
					asum += Math.abs(x[i]);
			}
			
		}
		
		return asum;
	}
	
	
	/**
	*  Finds the sum of the absolute values of the elements of a float vector.
	*  Translation of FORTRAN BLAS routine by Jack Dongarra, LINPACK, 3/11/1978.
	*
	*  @param  n       Number of elements in vector x to sum.
	*  @param  x       The vector x being summed:  x[0..x_off...n+xoff-1].
	*  @param  x_off   Offset into vector x to begin summing.
	*  @param  incx    Step size to use when moving through the x vector.
	*  @return The sum of the absolute values of the n elements of the
	*           vector x.
	*/
	public static float asum( int n, float x[], int x_off, int incx) {
		float asum = 0;
		if (n > 0 && incx > 0) {
			if ( incx != 1) {
			
				//	Code for increment not equal to 1.
				int nincx = n*incx + x_off;
				for (int i=x_off; i < nincx; i += incx)
					asum += Math.abs(x[i]);
				
			} else {
				
				//	Code for increment equal to 1.
				n += x_off;
				for (int i=x_off; i < n; ++i)
					asum += Math.abs(x[i]);
			}
			
		}
		
		return asum;
	}
	
	
	/**
	*  Multiply matrix m times vector x and add the result to vector y with double precision.
	*
	*  @param  n1  Number of elements in vector y, and number of rows in matrix m.
	*  @param  y   Vector of length n1 to which is added the product m*x.
	*  @param  n2  Number of elements in vector x, and number of columns in matrix m.
	*  @param  x   Vector of length n2
	*  @param  m   Matrix of n1 rows and n2 columns.
	*/
	public static void mxpy( int n1, double y[], int n2, double x[], double m[][]) {

		for (int j = 0; j < n2; j++) {
			for (int i = 0; i < n1; i++) {
				y[i] += x[j]*m[j][i];
			}
		}
		
	}

	
	/**
	*  Multiply matrix m times vector x and add the result to vector y with single precision.
	*
	*  @param  n1  Number of elements in vector y, and number of rows in matrix m.
	*  @param  y   Vector of length n1 to which is added the product m*x.
	*  @param  n2  Number of elements in vector x, and number of columns in matrix m.
	*  @param  x   Vector of length n2
	*  @param  m   Matrix of n1 rows and n2 columns.
	*/
	public static void mxpy( int n1, float y[], int n2, float x[], float m[][]) {

		for (int j = 0; j < n2; j++) {
			for (int i = 0; i < n1; i++) {
				y[i] += x[j]*m[j][i];
			}
		}
		
	}

	
	/**
	*  Run's the LINPACK benchmark on our implementation.
	*/
	private static void run_gefa_benchmark() {

		double mflops_result;
		double residn_result;
		double time_result;
		double eps_result;

		double a[][] = new double[200][201];
		double b[] = new double[200];
		double x[] = new double[200];
		double ops,total,norma,normx;
		double resid,time;
		double kf;
		int n,i,lda;
		int ipvt[] = new int[200];

		lda = 201;
		n = 100;

		ops = (2.0e0*(n*n*n))/3.0 + 2.0*(n*n);

		matgen(a,lda,n,b);
		time = second();
		gefa(a,lda,n,ipvt);
		gesl(a,lda,n,ipvt,b,0);
		total = second() - time;

		for (i = 0; i < n; i++)
			x[i] = b[i];
		norma = matgen(a,lda,n,b);
		for (i = 0; i < n; i++)
			b[i] = -b[i];
		mxpy(n,b,n,x,a);
		resid = 0.0;
		normx = 0.0;
		for (i = 0; i < n; i++) {
			resid = (resid > Math.abs(b[i])) ? resid : Math.abs(b[i]);
			normx = (normx > Math.abs(x[i])) ? normx : Math.abs(x[i]);
		}

		eps_result = epslon((double)1.0);

		residn_result = resid/( n*norma*normx*eps_result );
		residn_result += 0.005; // for rounding
		residn_result = (int)(residn_result*100);
		residn_result /= 100;

		time_result = total;
		time_result += 0.005; // for rounding
		time_result = (int)(time_result*100);
		time_result /= 100;

		mflops_result = ops/(1.0e6*total);
		mflops_result += 0.0005; // for rounding
		mflops_result = (int)(mflops_result*1000);
		mflops_result /= 1000;

		System.out.println("GEFA:  Mflops/s: " + mflops_result +
					 "  Time: " + time_result + " secs" +
					 "  Norm Res: " + residn_result +
					 "  Precision: " + eps_result);
	}
	

	/**
	*  Run's the LINPACK benchmark on our implementation.
	*/
	private static void run_geco_benchmark() {

		double mflops_result;
		double residn_result;
		double time_result;
		double eps_result;

		double a[][] = new double[200][201];
		double b[] = new double[200];
		double x[] = new double[200];
		double z[] = new double[200];
		double ops,total,norma,normx;
		double resid,time;
		int n,i,lda;
		int ipvt[] = new int[200];

		lda = 201;
		n = 100;

		ops = (2.0e0*(n*n*n))/3.0 + 2.0*(n*n);

		matgen(a,lda,n,b);
		time = second();
		double rcond = geco(a,lda,n,ipvt,z);
		gesl(a,lda,n,ipvt,b,0);
		total = second() - time;

		for (i = 0; i < n; i++)
			x[i] = b[i];
		norma = matgen(a,lda,n,b);
		for (i = 0; i < n; i++)
			b[i] = -b[i];
		mxpy(n,b,n,x,a);
		resid = 0.0;
		normx = 0.0;
		for (i = 0; i < n; i++) {
			resid = (resid > Math.abs(b[i])) ? resid : Math.abs(b[i]);
			normx = (normx > Math.abs(x[i])) ? normx : Math.abs(x[i]);
		}

		eps_result = epslon((double)1.0);

		residn_result = resid/( n*norma*normx*eps_result );
		residn_result += 0.005; // for rounding
		residn_result = (int)(residn_result*100);
		residn_result /= 100;

		time_result = total;
		time_result += 0.005; // for rounding
		time_result = (int)(time_result*100);
		time_result /= 100;

		mflops_result = ops/(1.0e6*total);
		mflops_result += 0.0005; // for rounding
		mflops_result = (int)(mflops_result*1000);
		mflops_result /= 1000;

		System.out.println("GECO:  Mflops/s: " + mflops_result +
					 "  Time: " + time_result + " secs" +
					 "  Norm Res: " + residn_result + " 1+RCond = " + (1+rcond) +
					 "  Precision: " + eps_result);
	}


	private static double second_orig = -1;
	private static double second() {
		if (second_orig==-1)
			second_orig = System.currentTimeMillis();
		
		return (System.currentTimeMillis() - second_orig)/1000;
	}

	/**
	*  Method to create a standard dense matrix for the LINPACK benchmark.
	*/
	private static double matgen (double a[][], int lda, int n, double b[]) {
		double norma;
		int i, j;

		int init = 1325;
		norma = 0.0;
		/*  Next two for() statements switched.  Solver wants
			matrix in column order. --dmd 3/3/97
		*/
		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++) {
				init = 3125*init % 65536;
				a[j][i] = (init - 32768.0)/16384.0;
				norma = (a[j][i] > norma) ? a[j][i] : norma;
			}
		}
		for (i = 0; i < n; i++)
			b[i] = 0.0;
		
		for (j = 0; j < n; j++) {
			for (i = 0; i < n; i++)
				b[i] += a[j][i];
		}

		return norma;
	}


	/**
	*  Estimate unit roundoff in quantities of size x.
    *
	*  This program should function properly on all systems
	*  satisfying the following two assumptions,
	*  1.  The base used in representing dfloating point
	*      numbers is not a power of three.
	*  2.  The quantity  a in the code is represented to
	*      the accuracy used in floating point variables
	*      that are stored in memory.
	*      Under these assumptions, it should be true that,
	*      a  is not exactly equal to four-thirds,
	*      b  has a zero for its last bit or digit,
	*      c  is not exactly equal to one,
	*  eps  measures the separation of 1.0 from
	*  the next larger floating point number.
	*  The developers of eispack would appreciate being informed
	*  about any systems where these assumptions do not hold.
    *
	******************************************************************
	*  This routine is one of the auxiliary routines used by eispack iii
	*  to avoid machine dependencies.
	******************************************************************
	*/
	private static double epslon (double x) {

		double a = 4.0e0/3.0e0;
		double eps = 0;
		while (eps == 0) {
			double b = a - 1.0;
			double c = b + b + b;
			eps = Math.abs(c-1.0);
		}
		
		return(eps*Math.abs(x));
	}


	/**
	*  Used to test out the methods in this class.
	*/
	public static void main(String args[]) {
	
		System.out.println();
		System.out.println("Testing LinAlg...");
		
		System.out.println("Running benchmarks:");
		run_geco_benchmark();
		run_gefa_benchmark();

		//  Create a matrix and compute it's inverse and determinant.
		double[][] matrix = {{5,3,7},{2,4,9},{3,6,4}};
		System.out.println("\nTesting gedi():\nmatrix = ");
		System.out.println("\t" + matrix[0][0] + "\t" + matrix[0][1] + "\t" + matrix[0][2]);
		System.out.println("\t" + matrix[1][0] + "\t" + matrix[1][1] + "\t" + matrix[1][2]);
		System.out.println("\t" + matrix[2][0] + "\t" + matrix[2][1] + "\t" + matrix[2][2]);
		
		try {
			double det = invert(matrix);
			
			System.out.println("determinant = " + (float)(det) + "\tshould be = -133.0");
			System.out.println("inverse = ");
			for (int i=0; i < 3; ++i) {
				System.out.print("\t");
				for (int j=0; j < 3; ++j) {
					System.out.print((float)(matrix[i][j]) + "\t");
				}
				System.out.println();
			}
			System.out.println("Should be = ");
			System.out.println("\t" + (2/7F) + "\t" + (-30/133F) + "\t" + (1/133F));
			System.out.println("\t" + (-1/7F) + "\t" + (1/133F) + "\t" + (31/133F));
			System.out.println("\t0.0\t\t" + (3/19F) + "\t" + (-2/19F));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Done!");
	}

}


