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

import javolution.context.ConcurrentContext;
import javolution.context.ObjectFactory;
import javolution.lang.MathLib;
import javolution.util.FastTable;
import org.jscience.mathematics.structure.Field;

/**
 * <p> This class represents a matrix made of {@link DenseVector dense
 *     vectors} (as rows). To create a dense matrix made of column vectors the 
 *     {@link #transpose} method can be used. 
 *     For example:[code]
 *        DenseVector<Rational> column0 = DenseVector.valueOf(...);
 *        DenseVector<Rational> column1 = DenseVector.valueOf(...);
 *        DenseMatrix<Rational> M = DenseMatrix.valueOf(column0, column1).transpose();
 *     [/code]</p>
 * <p> As for any concrete {@link org.jscience.mathematics.structure.Structure
 *     structure}, this class is declared <code>final</code> (otherwise most
 *     operations would have to be overridden to return the appropriate type).
 *     Specialized dense matrix should sub-class {@link Matrix} directly.
 *     For example:[code]
 *        // Extension through composition.
 *        final class TriangularMatrix <F extends Field<F>> extends Matrix<F> {
 *             private DenseMatrix<F> _value; // Possible implementation.
 *             ...
 *             public TriangularMatrix opposite() { // Returns the right type.
 *                 return TriangularMatrix.valueOf(_value.opposite());
 *             }
 *             ...
 *        }[/code]
 *     </p>   
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 2, 2007
 */
public final class DenseMatrix<F extends Field<F>> extends Matrix<F> {

    /**
     * Holds the number of columns n.
     */
    int _n;

    /**
     * Indicates if this matrix is transposed (the rows are then the columns).
     */
    boolean _transposed;

    /**
     * Holds this matrix rows (or columns when transposed).
     */
    final FastTable<DenseVector<F>> _rows = new FastTable<DenseVector<F>>();

    /**
     * Returns a dense matrix from the specified 2-dimensional array.
     * The first dimension being the row and the second being the column.
     * 
     * @param elements this matrix elements.
     * @return the matrix having the specified elements.
     * @throws DimensionException if rows have different length.
     * @see    DenseMatrix 
     */
    public static <F extends Field<F>> DenseMatrix<F> valueOf(F[][] elements) {
        int m = elements.length;
        int n = elements[0].length;
        DenseMatrix<F> M = DenseMatrix.newInstance(n, false);
        for (int i = 0; i < m; i++) {
            DenseVector<F> row = DenseVector.valueOf(elements[i]);
            if (row.getDimension() != n)
                throw new DimensionException();
            M._rows.add(row);
        }
        return M;
    }

    /**
     * Returns a dense matrix holding the specified row vectors 
     * (column vectors if {@link #transpose transposed}).
     *
     * @param rows the row vectors.
     * @return the matrix having the specified rows.
     * @throws DimensionException if the rows do not have the same dimension.
     */
    public static <F extends Field<F>> DenseMatrix<F> valueOf(
            DenseVector<F>... rows) {
        final int n = rows[0].getDimension();
        DenseMatrix<F> M = DenseMatrix.newInstance(n, false);
        for (int i = 0, m = rows.length; i < m; i++) {
            DenseVector<F> rowi = rows[i];
            if (rowi.getDimension() != n)
                throw new DimensionException(
                        "All vectors must have the same dimension.");
            M._rows.add(rowi);
        }
        return M;
    }
    
    /**
     * Returns a dense matrix holding the row vectors from the specified 
     * collection (column vectors if {@link #transpose transposed}).
     *
     * @param rows the list of row vectors.
     * @return the matrix having the specified rows.
     * @throws DimensionException if the rows do not have the same dimension.
     */
    public static <F extends Field<F>> DenseMatrix<F> valueOf(
            List<DenseVector<F>> rows) {
        final int n = rows.get(0).getDimension();
        DenseMatrix<F> M = DenseMatrix.newInstance(n, false);
        Iterator<DenseVector<F>> iterator = rows.iterator();
        for (int i = 0, m = rows.size(); i < m; i++) {
            DenseVector<F> rowi = iterator.next();
            if (rowi.getDimension() != n)
                throw new DimensionException(
                        "All vectors must have the same dimension.");
            M._rows.add(rowi);
        }
        return M;
    }

    /**
     * Returns a dense matrix equivalent to the specified matrix.
     *
     * @param that the matrix to convert.
     * @return <code>that</code> or a dense matrix holding the same elements
     *         as the specified matrix.
     */
    public static <F extends Field<F>> DenseMatrix<F> valueOf(Matrix<F> that) {
        if (that instanceof DenseMatrix)
            return (DenseMatrix<F>) that;
        int n = that.getNumberOfColumns();
        int m = that.getNumberOfRows();
        DenseMatrix<F> M = DenseMatrix.newInstance(n, false);
        for (int i = 0; i < m; i++) {
            DenseVector<F> rowi = DenseVector.valueOf(that.getRow(i));
            M._rows.add(rowi);
        }
        return M;
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
    public DenseVector<F> getRow(int i) {
        if (!_transposed)
            return _rows.get(i);
        // Else transposed.
        int n = _rows.size();
        int m = _n;
        if ((i < 0) || (i >= m))
            throw new DimensionException();
        DenseVector<F> V = DenseVector.newInstance();
        for (int j = 0; j < n; j++) {
            V._elements.add(_rows.get(j).get(i));
        }
        return V;
    }

    @Override
    public DenseVector<F> getColumn(int j) {
        if (_transposed)
            return _rows.get(j);
        int m = _rows.size();
        if ((j < 0) || (j >= _n))
            throw new DimensionException();
        DenseVector<F> V = DenseVector.newInstance();
        for (int i = 0; i < m; i++) {
            V._elements.add(_rows.get(i).get(j));
        }
        return V;
    }

    @Override
    public DenseVector<F> getDiagonal() {
        int m = this.getNumberOfRows();
        int n = this.getNumberOfColumns();
        int dimension = MathLib.min(m, n);
        DenseVector<F> V = DenseVector.newInstance();
        for (int i = 0; i < dimension; i++) {
            V._elements.add(this.get(i, i));
        }
        return V;
    }

    @Override
    public DenseMatrix<F> opposite() {
        DenseMatrix<F> M = DenseMatrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).opposite());
        }
        return M;
    }

    @Override
    public DenseMatrix<F> plus(Matrix<F> that) {
        if (this.getNumberOfRows() != that.getNumberOfRows())
            throw new DimensionException();
        DenseMatrix<F> M = DenseMatrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).plus(
                    _transposed ? that.getColumn(i) : that.getRow(i)));
        }
        return M;
    }

    @Override
    public DenseMatrix<F> minus(Matrix<F> that) { // Returns more specialized type.
        return this.plus(that.opposite());
    }

    @Override
    public DenseMatrix<F> times(F k) {
        DenseMatrix<F> M = DenseMatrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).times(k));
        }
        return M;
    }

    @Override
    public DenseVector<F> times(Vector<F> v) {
        if (v.getDimension() != this.getNumberOfColumns())
            throw new DimensionException();
        final int m = this.getNumberOfRows();
        DenseVector<F> V = DenseVector.newInstance();
        for (int i = 0; i < m; i++) {
            V._elements.add(this.getRow(i).times(v));
        }
        return V;
    }

    @Override
    public DenseMatrix<F> times(Matrix<F> that) {
        final int n = this.getNumberOfColumns();
        final int m = this.getNumberOfRows();
        final int p = that.getNumberOfColumns();
        if (that.getNumberOfRows() != n)
            throw new DimensionException();
        // Creates a mxp matrix in transposed form (p columns vectors of size m)
        DenseMatrix<F> M = DenseMatrix.newInstance(m, true); // Transposed.
        M._rows.setSize(p);
        Multiply<F> multiply = Multiply.valueOf(this, that, 0, p, M._rows);
        multiply.run();
        Multiply.recycle(multiply);
        return M;
    }

    // Logic to multiply two matrices. 
    private static class Multiply<F extends Field<F>> implements Runnable {
        private static final ObjectFactory<Multiply> FACTORY = new ObjectFactory<Multiply>() {

            @Override
            protected Multiply create() {
                return new Multiply();
            }
        };

        private DenseMatrix<F> _left;

        private Matrix<F> _right;

        private int _rightColumnStart;

        private int _rightColumnEnd;

        private FastTable<DenseVector<F>> _columnsResult;

        @SuppressWarnings("unchecked")
        static <F extends Field<F>> Multiply<F> valueOf(DenseMatrix<F> left, Matrix<F> right,
                int rightColumnStart, int rightColumnEnd,
                FastTable<DenseVector<F>> columnsResult) {
            Multiply<F> multiply = Multiply.FACTORY.object();
            multiply._left = left;
            multiply._right = right;
            multiply._rightColumnStart = rightColumnStart;
            multiply._rightColumnEnd = rightColumnEnd;
            multiply._columnsResult = columnsResult;
            return multiply;
        }

        static <F extends Field<F>> void recycle(Multiply<F> multiply) {
            multiply._left = null;
            multiply._right = null;
            multiply._columnsResult = null;
            Multiply.FACTORY.recycle(multiply);
        }

        public void run() {
            if (_rightColumnEnd - _rightColumnStart < 32) { // Direct calculation.
                FastTable<DenseVector<F>> rows = _left.getRows();
                final int m = rows.size();
                for (int j = _rightColumnStart; j < _rightColumnEnd; j++) {
                    Vector<F> thatColj = _right.getColumn(j);
                    DenseVector<F> column = DenseVector.newInstance();
                    _columnsResult.set(j, column);
                    for (int i = 0; i < m; i++) {
                        column._elements.add(rows.get(i).times(thatColj));
                    }
                }
            } else { // Concurrent/Recursive calculation.
                int halfIndex = (_rightColumnStart + _rightColumnEnd) >> 1;
                Multiply<F> firstHalf = Multiply.valueOf(_left, _right,
                        _rightColumnStart, halfIndex, _columnsResult);
                Multiply<F> secondHalf = Multiply.valueOf(_left, _right,
                        halfIndex, _rightColumnEnd, _columnsResult);
                ConcurrentContext.enter();
                try {
                    ConcurrentContext.execute(firstHalf);
                    ConcurrentContext.execute(secondHalf);
                } finally {
                    ConcurrentContext.exit();
                }
                Multiply.recycle(firstHalf);
                Multiply.recycle(secondHalf);
            }
        }
    }

    private FastTable<DenseVector<F>> getRows() {
        if (!_transposed)
            return _rows;
        FastTable<DenseVector<F>> rows = FastTable.newInstance();
        for (int i = 0; i < _n; i++) {
            rows.add(this.getRow(i));
        }
        return rows;
    }

    @Override
    public DenseMatrix<F> inverse() {
        if (!isSquare())
            throw new DimensionException("Matrix not square");
        return LUDecomposition.valueOf(this).inverse();
    }

    @Override
    public F determinant() {
        return LUDecomposition.valueOf(this).determinant();
    }

    @Override
    public DenseMatrix<F> transpose() {
        DenseMatrix<F> M = DenseMatrix.newInstance(_n, !_transposed);
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
        DenseMatrix<F> M = DenseMatrix.newInstance(m - 1, _transposed);
        for (int k1 = 0; k1 < m; k1++) {
            if (k1 == i)
                continue;
            DenseVector<F> row = _rows.get(k1);
            DenseVector<F> V = DenseVector.newInstance();
            M._rows.add(V);
            for (int k2 = 0; k2 < _n; k2++) {
                if (k2 == j)
                    continue;
                V._elements.add(row.get(k2));
            }
        }
        return M.determinant();
    }

    @Override
    public DenseMatrix<F> adjoint() {
        DenseMatrix<F> M = DenseMatrix.newInstance(_n, _transposed);
        int m = _rows.size();
        for (int i = 0; i < m; i++) {
            DenseVector<F> row = DenseVector.newInstance();
            M._rows.add(row);
            for (int j = 0; j < _n; j++) {
                F cofactor = _transposed ? cofactor(j, i) : cofactor(i, j);
                row._elements.add(((i + j) % 2 == 0) ? cofactor : cofactor
                        .opposite());
            }
        }
        return M.transpose();
    }

    @Override
    public Matrix<F> tensor(Matrix<F> that) {
        final int thism = this.getNumberOfRows();
        final int thisn = this.getNumberOfColumns();
        final int thatm = that.getNumberOfRows();
        final int thatn = that.getNumberOfColumns();
        int n = thisn * thatn; // Number of columns,
        int m = thism * thatm; // Number of rows.
        DenseMatrix<F> M = DenseMatrix.newInstance(n, false);
        for (int i = 0; i < m; i++) { // Row index.
            final int i_rem_thatm = i % thatm;
            final int i_div_thatm = i / thatm;
            DenseVector<F> row = DenseVector.newInstance();
            M._rows.add(row);
            for (int j = 0; j < thisn; j++) {
                F a = this.get(i_div_thatm, j);
                for (int k = 0; k < thatn; k++) {
                    row._elements.add(a.times(that.get(i_rem_thatm, k)));
                }
            }
        }
        return M;
    }

    @Override
    public Vector<F> vectorization() {
        DenseVector<F> V = DenseVector.newInstance();
        for (int j = 0, n = this.getNumberOfColumns(); j < n; j++) {
            Vector<F> column = this.getColumn(j);
            for (int i = 0, m = column.getDimension(); i < m; i++) {
                V._elements.add(column.get(i));
            }
        }
        return V;
    }

    @Override
    public DenseMatrix<F> copy() {
        DenseMatrix<F> M = DenseMatrix.newInstance(_n, _transposed);
        for (DenseVector<F> row : _rows) {
            M._rows.add(row.copy());
        }
        return M;
    }

    ///////////////////////////////
    // Package Private Utilities //
    ///////////////////////////////

    void set(int i, int j, F e) {
        if (_transposed) {
            _rows.get(j)._elements.set(i, e);
        } else {
            _rows.get(i)._elements.set(j, e);
        }
    }

    ///////////////////////
    // Factory creation. //
    ///////////////////////

    @SuppressWarnings("unchecked")
    static <F extends Field<F>> DenseMatrix<F> newInstance(int n,
            boolean transposed) {
        DenseMatrix<F> M = FACTORY.object();
        M._n = n;
        M._transposed = transposed;
        return M;
    }

    private static ObjectFactory<DenseMatrix> FACTORY = new ObjectFactory<DenseMatrix>() {
        @Override
        protected DenseMatrix create() {
            return new DenseMatrix();
        }

        @Override
        protected void cleanup(DenseMatrix matrix) {
            matrix._rows.reset();
        }
    };

    private DenseMatrix() {
    }

    private static final long serialVersionUID = 1L;

}