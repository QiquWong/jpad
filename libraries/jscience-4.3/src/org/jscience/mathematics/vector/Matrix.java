/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.vector;

import java.util.Comparator;

import javolution.context.StackContext;
import javolution.lang.MathLib;
import javolution.lang.Realtime;
import javolution.lang.ValueType;
import javolution.text.Text;
import javolution.text.TextBuilder;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jscience.mathematics.structure.Field;
import org.jscience.mathematics.structure.Ring;
import org.jscience.mathematics.structure.VectorSpace;

/**
 * <p> This class represents a rectangular table of elements of a ring-like 
 *     algebraic structure.</p>
 *     
 * <p> Instances of this class can be used to resolve system of linear equations
 *     involving <i>any kind</i> of {@link Field Field} elements
 *     (e.g. {@link org.jscience.mathematics.number.Real Real}, 
 *     {@link org.jscience.mathematics.number.Complex Complex}, 
 *     {@link org.jscience.physics.amount.Amount Amount&lt;?&gt;},
 *     {@link org.jscience.mathematics.function.Function Function}, etc).
 *     For example:[code]
 *        // Creates a dense matrix (2x2) of Rational numbers.
 *        DenseMatrix<Rational> M = DenseMatrix.valueOf(
 *            { Rational.valueOf(23, 45), Rational.valueOf(33, 75) },
 *            { Rational.valueOf(15, 31), Rational.valueOf(-20, 45)});
 *            
 *        // Creates a sparse matrix (16x2) of Real numbers.
 *        SparseMatrix<Real> M = SparseMatrix.valueOf(
 *            SparseVector.valueOf(16, Real.ZERO, 0, Real.valueOf(5)),
 *            SparseVector.valueOf(16, Real.ZERO, 15, Real.valueOf(-3)));
 *            
 *        // Creates a floating-point (64 bits) matrix (3x2).
 *        Float64Matrix M = Float64Matrix.valueOf(
 *           {{ 1.0, 2.0, 3.0}, { 4.0, 5.0, 6.0}});
 *            
 *        // Creates a complex single column matrix (1x2).
 *        ComplexMatrix M = ComplexMatrix.valueOf(
 *           {{ Complex.valueOf(1.0, 2.0), Complex.valueOf(4.0, 5.0)}}).transpose();
 *            
 *        // Creates an identity matrix (2x2) for modulo integer.
 *        SparseMatrix<ModuloInteger> IDENTITY = SparseMatrix.valueOf(
 *           DenseVector.valueOf(ModuloInteger.ONE, ModuloInteger.ONE), ModuloInteger.ZERO);
 *     [/code]</p>
 *     
 * <p> Non-commutative field multiplication is supported. Invertible square 
 *     matrices may form a non-commutative field (also called a division
 *     ring). In which case this class may be used to resolve system of linear
 *     equations with matrix coefficients.</p>
 *     
 * <p> Implementation Note: Matrices may use {@link 
 *     javolution.context.StackContext StackContext} and {@link 
 *     javolution.context.ConcurrentContext ConcurrentContext} in order to 
 *     minimize heap allocation and accelerate calculations on multi-core 
 *     systems.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, December 24, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Matrix_%28mathematics%29">
 *      Wikipedia: Matrix (mathematics)</a>
 */
public abstract class Matrix<F extends Field<F>> 
        implements VectorSpace<Matrix<F>, F>, Ring<Matrix<F>>, ValueType, Realtime {

    /**
     * Holds the default XML representation for matrices. For example:[code]
     *    <DenseMatrix rows="2" columns="2">
     *        <Complex real="1.0" imaginary="0.0" />
     *        <Complex real="0.0" imaginary="1.0" />
     *        <Complex real="0.0" imaginary="0.4" />
     *        <Complex real="-5.0" imaginary="-1.0" />
     *    </DenseMatrix>[/code]
     */
    @SuppressWarnings("unchecked")
    protected static final XMLFormat<Matrix> XML = new XMLFormat<Matrix>(
            Matrix.class) {

        @Override
        public void read(InputElement xml, Matrix M) throws XMLStreamException {
            // Nothing to do.
        }

        @Override
        public void write(Matrix M, OutputElement xml)
                throws XMLStreamException {
            final int m = M.getNumberOfRows();
            final int n = M.getNumberOfColumns();
            xml.setAttribute("rows", m);
            xml.setAttribute("columns", n);
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    xml.add(M.get(i, j));
                }
            }
        }
    };

    /**
     * Default constructor (for sub-classes).
     */
    protected Matrix() {
    }

    /**
     * Returns the number of rows <code>m</code> for this matrix.
     *
     * @return m, the number of rows.
     */
    public abstract int getNumberOfRows();

    /**
     * Returns the number of columns <code>n</code> for this matrix.
     *
     * @return n, the number of columns.
     */
    public abstract int getNumberOfColumns();
 
    /**
     * Returns a single element from this matrix.
     *
     * @param  i the row index (range [0..m[).
     * @param  j the column index (range [0..n[).
     * @return the element read at [i,j].
     * @throws IndexOutOfBoundsException <code>
     *         ((i < 0) || (i >= m)) || ((j < 0) || (j >= n))</code>
     */
    public abstract F get(int i, int j);

    /**
     * Returns the row identified by the specified index in this matrix.
     *
     * @param  i the row index (range [0..m[).
     * @return the vector holding the specified row.
     * @throws IndexOutOfBoundsException <code>(i < 0) || (i >= m)</code>
     */
    public abstract Vector<F> getRow(int i);

    /**
     * Returns the column identified by the specified index in this matrix.
     *
     * @param  j the column index (range [0..n[).
     * @return the vector holding the specified column.
     * @throws IndexOutOfBoundsException <code>(j < 0) || (j >= n)</code>
     */
    public abstract Vector<F> getColumn(int j);

    /**
     * Returns the diagonal vector.
     *
     * @return the vector holding the diagonal elements.
     */
    public abstract Vector<F> getDiagonal();

    /**
     * Returns the negation of this matrix.
     *
     * @return <code>-this</code>.
     */
    public abstract Matrix<F> opposite();

    /**
     * Returns the sum of this matrix with the one specified.
     *
     * @param   that the matrix to be added.
     * @return  <code>this + that</code>.
     * @throws  DimensionException matrices's dimensions are different.
     */
    public abstract Matrix<F> plus(Matrix<F> that);

    /**
     * Returns the difference between this matrix and the one specified.
     *
     * @param  that the matrix to be subtracted.
     * @return <code>this - that</code>.
     * @throws  DimensionException matrices's dimensions are different.
     */
    public Matrix<F> minus(Matrix<F> that) {
        return this.plus(that.opposite());
    }

    /**
     * Returns the product of this matrix by the specified factor.
     *
     * @param  k the coefficient multiplier.
     * @return <code>this · k</code>
     */
    public abstract Matrix<F> times(F k);
    
    /**
     * Returns the product of this matrix by the specified vector.
     *
     * @param  v the vector.
     * @return <code>this · v</code>
     * @throws DimensionException if <code>
     *         v.getDimension() != this.getNumberOfColumns()<code>
     */
    public abstract Vector<F> times(Vector<F> v);
    
    /**
     * Returns the product of this matrix with the one specified.
     *
     * @param  that the matrix multiplier.
     * @return <code>this · that</code>.
     * @throws DimensionException if <code>
     *         this.getNumberOfColumns() != that.getNumberOfRows()</code>.
     */
    public abstract Matrix<F> times(Matrix<F> that);    

    /**
     * Returns the inverse of this matrix (must be square).
     *
     * @return <code>1 / this</code>
     * @throws DimensionException if this matrix is not square.
     */
    public abstract Matrix<F> inverse();

    /**
     * Returns this matrix divided by the one specified.
     *
     * @param  that the matrix divisor.
     * @return <code>this / that</code>.
     * @throws DimensionException if that matrix is not square or dimensions 
     *         do not match.
     */
    public Matrix<F> divide(Matrix<F> that) {
        return this.times(that.inverse());
    }

    /**
     * Returns the inverse or pseudo-inverse if this matrix if not square.
     *
     * <p> Note: To resolve the equation <code>A * X = B</code>,
     *           it is usually faster to calculate <code>A.lu().solve(B)</code>
     *           rather than <code>A.inverse().times(B)</code>.</p>
     *
     * @return  the inverse or pseudo-inverse of this matrix.
     */
    public Matrix<F> pseudoInverse() {
        if (isSquare())
            return this.inverse();
        Matrix<F> thisTranspose = this.transpose();
        return (thisTranspose.times(this)).inverse().times(thisTranspose);
    }

    /**
     * Returns the determinant of this matrix.
     *
     * @return this matrix determinant.
     * @throws DimensionException if this matrix is not square.
     */
    public abstract F determinant();

    /**
     * Returns the transpose of this matrix.
     *
     * @return <code>A'</code>.
     */
    public abstract Matrix<F> transpose();

    /**
     * Returns the cofactor of an element in this matrix. It is the value
     * obtained by evaluating the determinant formed by the elements not in
     * that particular row or column.
     *
     * @param  i the row index.
     * @param  j the column index.
     * @return the cofactor of <code>THIS[i,j]</code>.
     * @throws DimensionException matrix is not square or its dimension
     *         is less than 2.
     */
    public abstract F cofactor(int i, int j);

    /**
     * Returns the adjoint of this matrix. It is obtained by replacing each
     * element in this matrix with its cofactor and applying a + or - sign
     * according (-1)**(i+j), and then finding the transpose of the resulting
     * matrix.
     *
     * @return the adjoint of this matrix.
     * @throws DimensionException if this matrix is not square or if
     *         its dimension is less than 2.
     */
    public abstract Matrix<F> adjoint();
    
    /**
     * Indicates if this matrix is square.
     *
     * @return <code>getNumberOfRows() == getNumberOfColumns()</code>
     */
    public boolean isSquare() {
        return getNumberOfRows() == getNumberOfColumns();
    }

    /**
     * Solves this matrix for the specified vector (returns <code>x</code>
     * such as <code>this · x = y</code>).
     * 
     * @param  y the vector for which the solution is calculated.
     * @return <code>x</code> such as <code>this · x = y</code>
     * @throws DimensionException if that matrix is not square or dimensions 
     *         do not match.
     */
    public Vector<F> solve(Vector<F> y) {
        DenseMatrix<F> M = DenseMatrix.newInstance(y.getDimension(), true);
        M._rows.add(DenseVector.valueOf(y));
        return solve(M).getColumn(0);
    }

    /**
     * Solves this matrix for the specified matrix (returns <code>x</code>
     * such as <code>this · x = y</code>).
     * 
     * @param  y the matrix for which the solution is calculated.
     * @return <code>x</code> such as <code>this · x = y</code>
     * @throws DimensionException if that matrix is not square or dimensions 
     *         do not match.
     */
    public Matrix<F> solve(Matrix<F> y) {
        return LUDecomposition.valueOf(this).solve(y); // Default implementation.
    }

    /**
     * Returns this matrix raised at the specified exponent.
     *
     * @param  exp the exponent.
     * @return <code>this<sup>exp</sup></code>
     * @throws DimensionException if this matrix is not square.
     */
    public Matrix<F> pow(int exp) {
        if (exp > 0) {
            StackContext.enter();
            try {
                Matrix<F> pow2 = this;
                Matrix<F> result = null;
                while (exp >= 1) { // Iteration.
                    if ((exp & 1) == 1) {
                        result = (result == null) ? pow2 : result.times(pow2);
                    }
                    pow2 = pow2.times(pow2);
                    exp >>>= 1;
                }
                return StackContext.outerCopy(result);
            } finally {
                StackContext.exit();
            }
        } else if (exp == 0) {
            return this.times(this.inverse()); // Identity.
        } else {
            return this.pow(-exp).inverse();
        }
    }

    /**
     * Returns the trace of this matrix.
     *
     * @return the sum of the diagonal elements.
     */
    public F trace() {
        F sum = this.get(0, 0);
        for (int i = MathLib.min(getNumberOfColumns(), getNumberOfRows()); --i > 0;) {
            sum = sum.plus(get(i, i));
        }
        return sum;
    }

    /**
     * Returns the linear algebraic matrix tensor product of this matrix
     * and another (Kronecker product). The default implementation returns
     * a {@link DenseMatrix}. 
     *
     * @param  that the second matrix.
     * @return <code>this &otimes; that</code>
     * @see    <a href="http://en.wikipedia.org/wiki/Kronecker_product">
     *         Wikipedia: Kronecker Product</a>
     */
    public abstract Matrix<F> tensor(Matrix<F> that);

    /**
     * Returns the vectorization of this matrix. The vectorization of 
     * a matrix is the column vector obtain by stacking the columns of the
     * matrix on top of one another. The default implementation returns 
     * a {@link DenseVector}.
     *
     * @return the vectorization of this matrix.
     * @see    <a href="http://en.wikipedia.org/wiki/Vectorization_%28mathematics%29">
     *         Wikipedia: Vectorization.</a>
     */
    public abstract Vector<F> vectorization();
    
    /**
     * Returns the text representation of this matrix.
     *
     * @return the text representation of this matrix.
     */
    public Text toText() {
        final int m = this.getNumberOfRows();
        final int n = this.getNumberOfColumns();
        TextBuilder tmp = TextBuilder.newInstance();
        tmp.append('{');
        for (int i = 0; i < m; i++) {
            tmp.append('{');
            for (int j = 0; j < n; j++) {
                tmp.append(get(i, j));
                if (j != n - 1) {
                    tmp.append(", ");
                }
            }
            tmp.append("}");
            if (i != m - 1) {
                tmp.append(",\n");
            }            
        }
        tmp.append("}");
        Text txt = tmp.toText();
        TextBuilder.recycle(tmp);
        return txt;
    }

    /**
     * Returns the text representation of this matrix as a 
     * <code>java.lang.String</code>.
     * 
     * @return <code>toText().toString()</code>
     */
    public final String toString() {
        return toText().toString();
    }

    /**
     * Indicates if this matrix can be considered equals to the one 
     * specified using the specified comparator when testing for 
     * element equality. The specified comparator may allow for some 
     * tolerance in the difference between the matrix elements.
     *
     * @param  that the matrix to compare for equality.
     * @param  cmp the comparator to use when testing for element equality.
     * @return <code>true</code> if this matrix and the specified matrix are
     *         both matrices with equal elements according to the specified
     *         comparator; <code>false</code> otherwise.
     */
    public boolean equals(Matrix<F> that, Comparator<F> cmp) {
        if (this == that)
            return true;
        final int m = this.getNumberOfRows();
        final int n = this.getNumberOfColumns();
        if ((that.getNumberOfRows() != m) || (that.getNumberOfColumns() != n))
            return false;
        for (int i = m; --i >= 0;) {
            for (int j = n; --j >= 0;) {
                if (cmp.compare(this.get(i, j), that.get(i, j)) != 0)
                    return false;
            }
        }
        return true;
    }

    /**
     * Indicates if this matrix is strictly equal to the object specified.
     *
     * @param  that the object to compare for equality.
     * @return <code>true</code> if this matrix and the specified object are
     *         both matrices with equal elements; <code>false</code> otherwise.
     * @see    #equals(Matrix, Comparator)
     */
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (!(that instanceof Matrix))
            return false;
        final int m = this.getNumberOfRows();
        final int n = this.getNumberOfColumns();
        Matrix<?> M = (Matrix<?>) that;
        if ((M.getNumberOfRows() != m) || (M.getNumberOfColumns() != n))
            return false;
        for (int i = m; --i >= 0;) {
            for (int j = n; --j >= 0;) {
                if (!this.get(i, j).equals(M.get(i, j)))
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash code value for this matrix.
     * Equals objects have equal hash codes.
     *
     * @return this matrix hash code value.
     * @see    #equals
     */
    public int hashCode() {
        final int m = this.getNumberOfRows();
        final int n = this.getNumberOfColumns();
        int code = 0;
        for (int i = m; --i >= 0;) {
            for (int j = n; --j >= 0;) {
                code += get(i, j).hashCode();
            }
        }
        return code;
    }

    /**
     * Returns a copy of this matrix 
     * {@link javolution.context.AllocatorContext allocated} 
     * by the calling thread (possibly on the stack).
     *     
     * @return an identical and independant copy of this matrix.
     */
    public abstract Matrix<F> copy();
    
    
}