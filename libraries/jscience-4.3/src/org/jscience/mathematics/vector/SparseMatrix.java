/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience.mathematics.vector;

import java.util.Iterator;
import java.util.List;

import javolution.context.ObjectFactory;
import javolution.lang.MathLib;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import javolution.util.FastTable;
import javolution.util.Index;

import org.jscience.mathematics.structure.Field;

/**
 * <p> This class represents a matrix made of {@link SparseVector sparse
 *     vectors} (as rows). To create a sparse matrix made of column vectors the 
 *     {@link #transpose} method can be used. 
 *     For example:[code]
 *        SparseVector<Rational> column0 = SparseVector.valueOf(...);
 *        SparseVector<Rational> column1 = SparseVector.valueOf(...);
 *        SparseMatrix<Rational> M = SparseMatrix.valueOf(column0, column1).transpose();
 *     [/code]</p>
 * <p> As for any concrete {@link org.jscience.mathematics.structure.Structure
 *     structure}, this class is declared <code>final</code> (otherwise most
 *     operations would have to be overridden to return the appropriate type).
 *     Specialized dense matrix should sub-class {@link Matrix} directly.
 *     For example:[code]
 *        // Extension through composition.
 *        final class BandMatrix <F extends Field<F>> extends Matrix<F> {
 *             private SparseMatrix<F> _value;
 *             ...
 *             public BandMatrix opposite() { // Returns the right type.
 *                 return BandMatrix.valueOf(_value.opposite());
 *             }
 *             ...
 *        }[/code]
 *     </p>   
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 2, 2007
 */
public final class SparseMatrix<F extends Field<F>> extends Matrix<F> {

    /**
     * Holds the number of columns n or the number of rows m if transposed.
     */
    int _n;

    /**
     * Holds the zero.
     */
    F _zero;;

    /**
     * Indicates if this matrix is transposed (the rows are then the columns).
     */
    boolean _transposed;

    /**
     * Holds this matrix rows (or columns when transposed).
     */
    final FastTable<SparseVector<F>> _rows = new FastTable<SparseVector<F>>();

    /**
     * Returns the sparse square matrix having the specified diagonal
     * vector. This method is typically used to create an identity matrix.
     * For example:[code]
     *      SparseMatrix<Real> IDENTITY = Matrix.valueOf(
     *           DenseVector.valueOf({Real.ONE, Real.ONE, Real.ONE}), Real.ZERO);
     * [/code]          
     *
     * @param  diagonal the diagonal vector.
     * @param  zero value of non-diagonal elements.
     * @return a square matrix with <code>diagonal</code> on the diagonal and
     *         <code>zero</code> elsewhere.
     */
    public static <F extends Field<F>> SparseMatrix<F> valueOf(
            Vector<F> diagonal, F zero) {
        int n = diagonal.getDimension();
        SparseMatrix<F> M = SparseMatrix.newInstance(n, zero, false);
        for (int i = 0; i < n; i++) {
            SparseVector<F> row = SparseVector.valueOf(n, zero, i, diagonal
                    .get(i));
            M._rows.add(row);
        }
        return M;
    }

    /**
     * Returns a sparse matrix holding the specified row vectors 
     * (column vectors if {@link #transpose transposed}).
     *
     * @param rows the row vectors.
     * @return the matrix having the specified rows.
     * @throws DimensionException if the rows do not have the same dimension.
     */
    public static <F extends Field<F>> SparseMatrix<F> valueOf(
            SparseVector<F>... rows) {
        final int n = rows[0]._dimension;
        final F zero = rows[0]._zero;
        SparseMatrix<F> M = SparseMatrix.newInstance(n, zero, false);
        for (int i = 0, m = rows.length; i < m; i++) {
            SparseVector<F> rowi = rows[i];
            if (rowi._dimension != n)
                throw new DimensionException(
                        "All vectors must have the same dimension.");
            if (!zero.equals(rowi._zero))
                throw new DimensionException(
                        "All vectors must have the same zero element.");
            M._rows.add(rowi);
        }
        return M;
    }

    /**
     * Returns a sparse matrix holding the row vectors from the specified 
     * collection (column vectors if {@link #transpose transposed}).
     *
     * @param rows the list of row vectors.
     * @return the matrix having the specified rows.
     * @throws DimensionException if the rows do not have the same dimension.
     */
    public static <F extends Field<F>> SparseMatrix<F> valueOf(
            List<SparseVector<F>> rows) {
        final int n = rows.get(0)._dimension;
        final F zero = rows.get(0)._zero;
        SparseMatrix<F> M = SparseMatrix.newInstance(n, zero, false);
        Iterator<SparseVector<F>> iterator = rows.iterator();
        for (int i = 0, m = rows.size(); i < m; i++) {
            SparseVector<F> rowi = iterator.next();
            if (rowi.getDimension() != n)
                throw new DimensionException(
                        "All vectors must have the same dimension.");
            if (!zero.equals(rowi._zero))
                throw new DimensionException(
                        "All vectors must have the same zero element.");
            M._rows.add(rowi);
        }
        return M;
    }

    /**
     * Returns a sparse matrix equivalent to the specified matrix but with 
     * the zero elements removed using the default object equality comparator.
     *
     * @param that the matrix to convert.
     * @param zero the zero element for the sparse vector to return.
     * @return <code>SparseMatrix.valueOf(that, zero, FastComparator.DEFAULT)</code> or a dense matrix holding the same elements
     */
    public static <F extends Field<F>> SparseMatrix<F> valueOf(Matrix<F> that, F zero) {
        return SparseMatrix.valueOf(that, zero, FastComparator.DEFAULT);
    }

    /**
     * Returns a sparse matrix equivalent to the specified matrix but with 
     * the zero elements removed using the specified object equality comparator.
     *
     * @param that the matrix to convert.
     * @param zero the zero element for the sparse vector to return.
     * @param comparator the comparator used to determinate zero equality. 
     * @return <code>that</code> or a dense matrix holding the same elements
     *         as the specified matrix.
     */
    public static <F extends Field<F>> SparseMatrix<F> valueOf(Matrix<F> that,
            F zero, FastComparator<? super F> comparator) {
        if (that instanceof SparseMatrix)
            return (SparseMatrix<F>) that;
        int n = that.getNumberOfColumns();
        int m = that.getNumberOfRows();
        SparseMatrix<F> M = SparseMatrix.newInstance(n, zero, false);
        for (int i = 0; i < m; i++) {
            SparseVector<F> rowi = SparseVector.valueOf(that.getRow(i), zero,
                    comparator);
            M._rows.add(rowi);
        }
        return M;
    }

    /**
     * Returns the value of the non-set elements for this sparse matrix.
     * 
     * @return the element corresponding to zero.
     */
    public F getZero() {
        return _zero;
    }

    @Override
    public int getNumberOfRows() {
        return _transposed ? _n : _rows.size();
    }

    @Override
    public int getNumberOfColumns() {
        return _transposed ? _rows.size() : _n;
    }

    @Override
    public F get(int i, int j) {
        return _transposed ? _rows.get(j).get(i) : _rows.get(i).get(j);
    }

    @Override
    public SparseVector<F> getRow(int i) {
        if (!_transposed)
            return _rows.get(i);
        // Else transposed.
        int n = _rows.size();
        int m = _n;
        if ((i < 0) || (i >= m))
            throw new DimensionException();
        SparseVector<F> V = SparseVector.newInstance(n, _zero);
        for (int j = 0; j < n; j++) {
            SparseVector<F> row = _rows.get(j);
            F e = row._elements.get(Index.valueOf(i));
            if (e != null) {
                V._elements.put(Index.valueOf(j), e);
            }
        }
        return V;
    }

    @Override
    public SparseVector<F> getColumn(int j) {
        if (_transposed)
            return _rows.get(j);
        int m = _rows.size();
        if ((j < 0) || (j >= _n))
            throw new DimensionException();
        SparseVector<F> V = SparseVector.newInstance(_n, _zero);
        for (int i = 0; i < m; i++) {
            SparseVector<F> row = _rows.get(i);
            F e = row._elements.get(Index.valueOf(j));
            if (e != null) {
                V._elements.put(Index.valueOf(i), e);
            }
        }
        return V;
    }

    @Override
    public SparseVector<F> getDiagonal() {
        int m = this.getNumberOfRows();
        int n = this.getNumberOfColumns();
        int dimension = MathLib.min(m, n);
        SparseVector<F> V = SparseVector.newInstance(_n, _zero);
        for (int i = 0; i < dimension; i++) {
            SparseVector<F> row = _rows.get(i);
            F e = row._elements.get(Index.valueOf(i));
            if (e != null) {
                V._elements.put(Index.valueOf(i), e);
            }
        }
        return V;
    }

    @Override
    public SparseMatrix<F> opposite() {
        SparseMatrix<F> M = SparseMatrix.newInstance(_n, _zero, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).opposite());
        }
        return M;
    }

    @Override
    public SparseMatrix<F> plus(Matrix<F> that) {
        if (this.getNumberOfRows() != that.getNumberOfRows())
            throw new DimensionException();
        SparseMatrix<F> M = SparseMatrix.newInstance(_n, _zero, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).plus(
                    _transposed ? that.getColumn(i) : that.getRow(i)));
        }
        return M;
    }

    @Override
    public SparseMatrix<F> minus(Matrix<F> that) { // Returns more specialized type.
        return this.plus(that.opposite());
    }

    @Override
    public SparseMatrix<F> times(F k) {
        SparseMatrix<F> M = SparseMatrix.newInstance(_n, _zero, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).times(k));
        }
        return M;
    }

    @Override
    public SparseVector<F> times(Vector<F> v) {
        if (v.getDimension() != this.getNumberOfColumns())
            throw new DimensionException();
        final int m = this.getNumberOfRows();
        SparseVector<F> V = SparseVector.newInstance(m, _zero);
        for (int i = 0; i < m; i++) {
            F e = this.getRow(i).times(v);
            if (!_zero.equals(e)) {
                V._elements.put(Index.valueOf(i), e);
            }
        }
        return V;
    }

    @Override
    public SparseMatrix<F> times(Matrix<F> that) {
        final int m = this.getNumberOfRows();
        final int n = this.getNumberOfColumns();
        final int p = that.getNumberOfColumns();
        if (that.getNumberOfRows() != n)
            throw new DimensionException();
        // Creates a mxp matrix in transposed form (p columns vectors of size m)
        FastTable<SparseVector<F>> rows = this.getRows();
        SparseMatrix<F> M = SparseMatrix.newInstance(m, _zero, true);   
        for (int j = 0; j < p; j++) { 
            Vector<F> thatColj = that.getColumn(j);
            SparseVector<F> column = SparseVector.newInstance(m, _zero);
            M._rows.add(column); // M is transposed.
            for (int i = 0; i < m; i++) {
                F e = rows.get(i).times(thatColj);
                if (!_zero.equals(e)) {
                    column._elements.put(Index.valueOf(i), e);
                }
            }
        }
        return M;
    }
    private FastTable<SparseVector<F>> getRows() {
        if (!_transposed)
            return _rows;
        FastTable<SparseVector<F>> rows = FastTable.newInstance();
        for (int i = 0; i < _n; i++) {
            rows.add(this.getRow(i));
        }
        return rows;
    }

    @Override
    public SparseMatrix<F> inverse() {
        if (!isSquare())
            throw new DimensionException("Matrix not square");
        F detInv = this.determinant().inverse();
        SparseMatrix<F> A = this.adjoint();
        // Multiply adjoint elements with 1 / determinant.
        for (int i = 0, m = A._rows.size(); i < m; i++) {
            SparseVector<F> row = A._rows.get(i);
            for (FastMap.Entry<Index, F> e = row._elements.head(), end = row._elements
                    .tail(); (e = e.getNext()) != end;) {
                F element = e.getValue();
                e.setValue(detInv.times(element));
            }
        }
        return A;
    }

    @Override
    public F determinant() {
        if (!isSquare())
            throw new DimensionException("Matrix not square");
        if (_n == 1)
            return this.get(0, 0);
        // Expansion by minors (also known as Laplacian)
        // This algorithm is division free but too slow for dense matrix.
        SparseVector<F> row0 = this.getRow(0);
        F det = null;
        for (FastMap.Entry<Index, F> e = row0._elements.head(), end = row0._elements
                .tail(); (e = e.getNext()) != end;) {
            int i = e.getKey().intValue();
            F d = e.getValue().times(cofactor(0, i));
            if (i % 2 != 0) {
                d = d.opposite();
            }
            det = (det == null) ? d : det.plus(d);
        }
        return det == null ? _zero : det;
    }

    @Override
    public Matrix<F> solve(Matrix<F> y) {
        return this.inverse().times(y);
    }

    @Override
    public SparseMatrix<F> transpose() {
        SparseMatrix<F> M = SparseMatrix.newInstance(_n, _zero, !_transposed);
        M._rows.addAll(this._rows);
        return M;
    }

    @Override
    public F cofactor(int i, int j) {
        if (_transposed) {
            int k = i;
            i = j;
            j = k; // Swaps i,j
        }
        int m = _rows.size();
        SparseMatrix<F> M = SparseMatrix.newInstance(m - 1, _zero, _transposed);
        for (int k1 = 0; k1 < m; k1++) {
            if (k1 == i)
                continue;
            SparseVector<F> row = _rows.get(k1);
            SparseVector<F> V = SparseVector.newInstance(_n - 1, _zero);
            M._rows.add(V);
            for (FastMap.Entry<Index, F> e = row._elements.head(), end = row._elements
                    .tail(); (e = e.getNext()) != end;) {
                int index = e.getKey().intValue();
                if (index < j) {
                    V._elements.put(e.getKey(), e.getValue());
                } else if (index > j) { // Position shifted (index minus one).
                    V._elements.put(Index.valueOf(index - 1), e.getValue());
                } // Else don't copy element at j.
            }
        }
        return M.determinant();
    }

    @Override
    public SparseMatrix<F> adjoint() {
        SparseMatrix<F> M = SparseMatrix.newInstance(_n, _zero, _transposed);
        int m = _rows.size();
        for (int i = 0; i < m; i++) {
            SparseVector<F> row = SparseVector.newInstance(_n, _zero);
            M._rows.add(row);
            for (int j = 0; j < _n; j++) {
                F cofactor = _transposed ? cofactor(j, i) : cofactor(i, j);
                if (!_zero.equals(cofactor)) {
                    row._elements
                            .put(Index.valueOf(j),
                                    ((i + j) % 2 == 0) ? cofactor : cofactor
                                            .opposite());
                }
            }
        }
        return M.transpose();
    }

    @Override
    public SparseMatrix<F> tensor(Matrix<F> that) {
        final int thism = this.getNumberOfRows();
        final int thisn = this.getNumberOfColumns();
        final int thatm = that.getNumberOfRows();
        final int thatn = that.getNumberOfColumns();
        int n = thisn * thatn; // Number of columns,
        int m = thism * thatm; // Number of rows.
        SparseMatrix<F> M = SparseMatrix.newInstance(n, _zero, false);
        for (int i=0; i < m; i++) { // Row index.
            final int i_rem_thatm = i % thatm;
            final int i_div_thatm = i / thatm;
            SparseVector<F> row = SparseVector.newInstance(n, _zero);
            M._rows.add(row);
            SparseVector<F> thisRow = this.getRow(i_div_thatm);
            for (FastMap.Entry<Index, F> e = thisRow._elements.head(),
                    end = thisRow._elements.tail(); (e = e.getNext()) != end;) {
                F a = e.getValue();
                int j = e.getKey().intValue();
                for (int k=0; k < thatn; k++) {
                    F b = that.get(i_rem_thatm, k);
                    if (!b.equals(_zero)) {
                        row._elements.put(Index.valueOf(j * thatn + k), a.times(b));
                    }
                }
            }
        }
        return M;
    }

    @Override
    public SparseVector<F> vectorization() {
        SparseVector<F> V = SparseVector.newInstance(_n
                * this.getNumberOfRows(), _zero);
        int offset = 0;
        for (int j = 0, n = this.getNumberOfColumns(); j < n; j++) {
            SparseVector<F> column = this.getColumn(j);
            for (FastMap.Entry<Index, F> e = column._elements.head(), end = column._elements
                    .tail(); (e = e.getNext()) != end;) {
                V._elements.put(Index.valueOf(e.getKey().intValue() + offset),
                        e.getValue());
            }
            offset += this.getNumberOfRows();
        }
        return V;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SparseMatrix<F> copy() {
        SparseMatrix<F> M = newInstance(_n, (F)_zero.copy(), _transposed);
        for (SparseVector<F> row : _rows) {
            M._rows.add(row.copy());
        }
        return M;
    }

    ///////////////////////
    // Factory creation. //
    ///////////////////////

    @SuppressWarnings("unchecked")
    static <F extends Field<F>> SparseMatrix<F> newInstance(int n, F zero,
            boolean transposed) {
        SparseMatrix<F> M = FACTORY.object();
        M._n = n;
        M._zero = zero;
        M._transposed = transposed;
        return M;
    }

    private static final ObjectFactory<SparseMatrix> FACTORY = new ObjectFactory<SparseMatrix>() {
        @Override
        protected SparseMatrix create() {
            return new SparseMatrix();
        }

        @Override
        protected void cleanup(SparseMatrix matrix) {
            matrix._rows.reset();
        }
    };

    private SparseMatrix() {
    }

    private static final long serialVersionUID = 1L;

}