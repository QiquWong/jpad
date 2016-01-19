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

import org.jscience.mathematics.number.Float64;

/**
 * <p> This class represents an optimized {@link Matrix matrix} implementation
 *     for {@link Float64 64 bits floating-point} numbers.</p>
 *     
 * <p> Instances of this class can be created from {@link Float64Vector}, 
 *     either as rows or columns if the matrix is transposed. For example:[code]
 *        Float64Vector<Rational> column0 = Float64Vector.valueOf(...);
 *        Float64Vector<Rational> column1 = Float64Vector.valueOf(...);
 *        Float64Matrix<Rational> M = Float64Matrix.valueOf(column0, column1).transpose();
 *     [/code]</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 2, 2007
 */
public final class Float64Matrix extends Matrix<Float64> {

    /**
     * Holds the number of columns n.
     */
    int _n;;

    /**
     * Indicates if this matrix is transposed (the rows are then the columns).
     */
    boolean _transposed;

    /**
     * Holds this matrix rows (or columns when transposed).
     */
    final FastTable<Float64Vector> _rows = new FastTable<Float64Vector>();

    /**
     * Returns a dense matrix from a 2-dimensional array of <code>double</code>
     * values. The first dimension being the row and the second being the 
     * column.
     *
     * @param  values the array of <code>double</code> values.
     * @return the matrix having the specified elements.
     * @throws DimensionException if rows have different length.
     * @see    Float64Vector 
     */
    public static Float64Matrix valueOf(double[][] values) {
        int m = values.length;
        int n = values[0].length;
        Float64Matrix M = Float64Matrix.newInstance(n, false);
        for (int i = 0; i < m; i++) {
            Float64Vector row = Float64Vector.valueOf(values[i]);
            if (row.getDimension() != n)
                throw new DimensionException();
            M._rows.add(row);
        }
        return M;
    }

    /**
     * Returns a complex matrix holding the specified row vectors 
     * (column vectors if {@link #transpose transposed}).
     *
     * @param rows the row vectors.
     * @return the matrix having the specified rows.
     * @throws DimensionException if the rows do not have the same dimension.
     */
    public static Float64Matrix valueOf(Float64Vector... rows) {
        final int n = rows[0].getDimension();
        Float64Matrix M = Float64Matrix.newInstance(n, false);
        for (int i = 0, m = rows.length; i < m; i++) {
            Float64Vector rowi = rows[i];
            if (rowi.getDimension() != n)
                throw new DimensionException(
                        "All vectors must have the same dimension.");
            M._rows.add(rowi);
        }
        return M;
    }

    /**
     * Returns a complex matrix holding the row vectors from the specified 
     * collection (column vectors if {@link #transpose transposed}).
     *
     * @param rows the list of row vectors.
     * @return the matrix having the specified rows.
     * @throws DimensionException if the rows do not have the same dimension.
     */
    public static Float64Matrix valueOf(List<Float64Vector> rows) {
        final int n = rows.get(0).getDimension();
        Float64Matrix M = Float64Matrix.newInstance(n, false);
        Iterator<Float64Vector> iterator = rows.iterator();
        for (int i = 0, m = rows.size(); i < m; i++) {
            Float64Vector rowi = iterator.next();
            if (rowi.getDimension() != n)
                throw new DimensionException(
                        "All vectors must have the same dimension.");
            M._rows.add(rowi);
        }
        return M;
    }

    /**
     * Returns a complex matrix equivalent to the specified matrix.
     *
     * @param that the matrix to convert.
     * @return <code>that</code> or a complex matrix holding the same elements
     *         as the specified matrix.
     */
    public static Float64Matrix valueOf(Matrix<Float64> that) {
        if (that instanceof Float64Matrix)
            return (Float64Matrix) that;
        int n = that.getNumberOfColumns();
        int m = that.getNumberOfRows();
        Float64Matrix M = Float64Matrix.newInstance(n, false);
        for (int i = 0; i < m; i++) {
            Float64Vector rowi = Float64Vector.valueOf(that.getRow(i));
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
    public Float64 get(int i, int j) {
        return _transposed ? _rows.get(j).get(i) : _rows.get(i).get(j);
    }

    @Override
    public Float64Vector getRow(int i) {
        if (!_transposed)
            return _rows.get(i);
        // Else transposed.
        int n = _rows.size();
        int m = _n;
        if ((i < 0) || (i >= m))
            throw new DimensionException();
        Float64Vector V = Float64Vector.newInstance(n);
        for (int j = 0; j < n; j++) {
            V.set(j, _rows.get(j).get(i).doubleValue());
        }
        return V;
    }

    @Override
    public Float64Vector getColumn(int j) {
        if (_transposed)
            return _rows.get(j);
        int m = _rows.size();
        if ((j < 0) || (j >= _n))
            throw new DimensionException();
        Float64Vector V = Float64Vector.newInstance(m);
        for (int i = 0; i < m; i++) {
            V.set(i, _rows.get(i).get(j).doubleValue());
        }
        return V;
    }

    @Override
    public Float64Vector getDiagonal() {
        int m = this.getNumberOfRows();
        int n = this.getNumberOfColumns();
        int dimension = MathLib.min(m, n);
        Float64Vector V = Float64Vector.newInstance(dimension);
        for (int i = 0; i < dimension; i++) {
            V.set(i, this.get(i, i).doubleValue());
        }
        return V;
    }

    @Override
    public Float64Matrix opposite() {
        Float64Matrix M = Float64Matrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).opposite());
        }
        return M;
    }

    @Override
    public Float64Matrix plus(Matrix<Float64> that) {
        if (this.getNumberOfRows() != that.getNumberOfRows())
            throw new DimensionException();
        Float64Matrix M = Float64Matrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).plus(
                    _transposed ? that.getColumn(i) : that.getRow(i)));
        }
        return M;
    }

    @Override
    public Float64Matrix minus(Matrix<Float64> that) { // Returns more specialized type.
        return this.plus(that.opposite());
    }

    @Override
    public Float64Matrix times(Float64 k) {
        Float64Matrix M = Float64Matrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).times(k));
        }
        return M;
    }

    @Override
    public Float64Vector times(Vector<Float64> v) {
        if (v.getDimension() != this.getNumberOfColumns())
            throw new DimensionException();
        final int m = this.getNumberOfRows();
        Float64Vector V = Float64Vector.newInstance(m);
        for (int i = 0; i < m; i++) {
            V.set(i, this.getRow(i).times(v).doubleValue());
        }
        return V;
    }

    @Override
    public Float64Matrix times(Matrix<Float64> that) {
        final int n = this.getNumberOfColumns();
        final int m = this.getNumberOfRows();
        final int p = that.getNumberOfColumns();
        if (that.getNumberOfRows() != n)
            throw new DimensionException();
        // Creates a mxp matrix in transposed form (p columns vectors of size m)
        Float64Matrix M = Float64Matrix.newInstance(m, true); // Transposed.
        M._rows.setSize(p);
        Multiply multiply = Multiply.valueOf(this, that, 0, p, M._rows);
        multiply.run();
        Multiply.recycle(multiply);
        return M;
    }

    // Logic to multiply two matrices. 
    private static class Multiply implements Runnable {
        private static final ObjectFactory<Multiply> FACTORY = new ObjectFactory<Multiply>() {

            @Override
            protected Multiply create() {
                return new Multiply();
            }
        };

        private Float64Matrix _left;

        private Matrix<Float64> _right;

        private int _rightColumnStart;

        private int _rightColumnEnd;

        private FastTable<Float64Vector> _columnsResult;

        static Multiply valueOf(Float64Matrix left, Matrix<Float64> right,
                int rightColumnStart, int rightColumnEnd,
                FastTable<Float64Vector> columnsResult) {
            Multiply multiply = Multiply.FACTORY.object();
            multiply._left = left;
            multiply._right = right;
            multiply._rightColumnStart = rightColumnStart;
            multiply._rightColumnEnd = rightColumnEnd;
            multiply._columnsResult = columnsResult;
            return multiply;
        }

        static void recycle(Multiply multiply) {
            multiply._left = null;
            multiply._right = null;
            multiply._columnsResult = null;
            Multiply.FACTORY.recycle(multiply);
        }

        public void run() {
            if (_rightColumnEnd - _rightColumnStart < 32) { // Direct calculation.
                FastTable<Float64Vector> rows = _left.getRows();
                final int m = rows.size();
                for (int j = _rightColumnStart; j < _rightColumnEnd; j++) {
                    Vector<Float64> thatColj = _right.getColumn(j);
                    Float64Vector column = Float64Vector.newInstance(m);
                    _columnsResult.set(j, column);
                    for (int i = 0; i < m; i++) {
                        column.set(i, rows.get(i).times(thatColj)
                                        .doubleValue());
                    }
                }
            } else { // Concurrent/Recursive calculation.
                int halfIndex = (_rightColumnStart + _rightColumnEnd) >> 1;
                Multiply firstHalf = Multiply.valueOf(_left, _right,
                        _rightColumnStart, halfIndex, _columnsResult);
                Multiply secondHalf = Multiply.valueOf(_left, _right,
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

    private FastTable<Float64Vector> getRows() {
        if (!_transposed)
            return _rows;
        FastTable<Float64Vector> rows = FastTable.newInstance();
        for (int i = 0; i < _n; i++) {
            rows.add(this.getRow(i));
        }
        return rows;
    }

    @Override
    public Float64Matrix inverse() {
        if (!isSquare())
            throw new DimensionException("Matrix not square");
        return Float64Matrix.valueOf(LUDecomposition.valueOf(this).inverse());
    }

    @Override
    public Float64 determinant() {
        return LUDecomposition.valueOf(this).determinant();
    }

    @Override
    public Float64Matrix transpose() {
        Float64Matrix M = Float64Matrix.newInstance(_n, !_transposed);
        M._rows.addAll(this._rows);
        return M;
    }

    @Override
    public Float64 cofactor(int i, int j) {
        if (_transposed) {
            int k = i;
            i = j;
            j = k; // Swaps i,j
        }
        int m = _rows.size();
        Float64Matrix M = Float64Matrix.newInstance(m - 1, _transposed);
        for (int k1 = 0; k1 < m; k1++) {
            if (k1 == i)
                continue;
            Float64Vector row = _rows.get(k1);
            Float64Vector V = Float64Vector.newInstance(_n - 1);
            M._rows.add(V);
            for (int k2 = 0, k = 0; k2 < _n; k2++) {
                if (k2 == j)
                    continue;
                V.set(k++, row.get(k2).doubleValue());
            }
        }
        return M.determinant();
    }

    @Override
    public Float64Matrix adjoint() {
        Float64Matrix M = Float64Matrix.newInstance(_n, _transposed);
        int m = _rows.size();
        for (int i = 0; i < m; i++) {
            Float64Vector row = Float64Vector.newInstance(_n);
            M._rows.add(row);
            for (int j = 0; j < _n; j++) {
                Float64 cofactor = _transposed ? cofactor(j, i)
                        : cofactor(i, j);
                row.set(j, ((i + j) % 2 == 0) ? cofactor.doubleValue()
                        : cofactor.opposite().doubleValue());
            }
        }
        return M.transpose();
    }

    @Override
    public Float64Matrix tensor(Matrix<Float64> that) {
        return Float64Matrix.valueOf(DenseMatrix.valueOf(this).tensor(that));
    }

    @Override
    public Float64Vector vectorization() {
        return Float64Vector.valueOf(DenseMatrix.valueOf(this).vectorization());
    }

    @Override
    public Float64Matrix copy() {
        Float64Matrix M = Float64Matrix.newInstance(_n, _transposed);
        for (Float64Vector row : _rows) {
            M._rows.add(row.copy());
        }
        return M;
    }

    ///////////////////////
    // Factory creation. //
    ///////////////////////

    static Float64Matrix newInstance(int n, boolean transposed) {
        Float64Matrix M = FACTORY.object();
        M._n = n;
        M._transposed = transposed;
        return M;
    }

    private static ObjectFactory<Float64Matrix> FACTORY = new ObjectFactory<Float64Matrix>() {
        @Override
        protected Float64Matrix create() {
            return new Float64Matrix();
        }

        @Override
        protected void cleanup(Float64Matrix matrix) {
            matrix._rows.reset();
        }
    };

    private Float64Matrix() {
    }

    private static final long serialVersionUID = 1L;

}