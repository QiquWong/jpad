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

import org.jscience.mathematics.number.Complex;

/**
 * <p> This class represents an optimized {@link Matrix matrix} implementation
 *     for {@link Complex complex} numbers.</p>
 *     
 * <p> Instances of this class can be created from {@link ComplexVector}, 
 *     either as rows or columns if the matrix is transposed. For example:[code]
 *        ComplexVector<Rational> column0 = ComplexVector.valueOf(...);
 *        ComplexVector<Rational> column1 = ComplexVector.valueOf(...);
 *        ComplexMatrix<Rational> M = ComplexMatrix.valueOf(column0, column1).transpose();
 *     [/code]</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, January 2, 2007
 */
public final class ComplexMatrix extends Matrix<Complex> {

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
    final FastTable<ComplexVector> _rows = new FastTable<ComplexVector>();

    /**
     * Returns a complex matrix from the specified 2-dimensional array.
     * The first dimension being the row and the second being the column.
     * 
     * @param elements this matrix elements.
     * @return the matrix having the specified elements.
     * @throws DimensionException if rows have different length.
     * @see    ComplexVector
     */
    public static ComplexMatrix valueOf(Complex[][] elements) {
        int m = elements.length;
        int n = elements[0].length;
        ComplexMatrix M = ComplexMatrix.newInstance(n, false);
        for (int i = 0; i < m; i++) {
            ComplexVector row = ComplexVector.valueOf(elements[i]);
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
    public static ComplexMatrix valueOf(ComplexVector... rows) {
        final int n = rows[0].getDimension();
        ComplexMatrix M = ComplexMatrix.newInstance(n, false);
        for (int i = 0, m = rows.length; i < m; i++) {
            ComplexVector rowi = rows[i];
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
    public static ComplexMatrix valueOf(List<ComplexVector> rows) {
        final int n = rows.get(0).getDimension();
        ComplexMatrix M = ComplexMatrix.newInstance(n, false);
        Iterator<ComplexVector> iterator = rows.iterator();
        for (int i = 0, m = rows.size(); i < m; i++) {
            ComplexVector rowi = iterator.next();
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
    public static ComplexMatrix valueOf(Matrix<Complex> that) {
        if (that instanceof ComplexMatrix)
            return (ComplexMatrix) that;
        int n = that.getNumberOfColumns();
        int m = that.getNumberOfRows();
        ComplexMatrix M = ComplexMatrix.newInstance(n, false);
        for (int i = 0; i < m; i++) {
            ComplexVector rowi = ComplexVector.valueOf(that.getRow(i));
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
    public Complex get(int i, int j) {
        return _transposed ? _rows.get(j).get(i) : _rows.get(i).get(j);
    }

    @Override
    public ComplexVector getRow(int i) {
        if (!_transposed)
            return _rows.get(i);
        // Else transposed.
        int n = _rows.size();
        int m = _n;
        if ((i < 0) || (i >= m))
            throw new DimensionException();
        ComplexVector V = ComplexVector.newInstance(n);
        for (int j = 0; j < n; j++) {
            V.set(j, _rows.get(j).get(i));
        }
        return V;
    }

    @Override
    public ComplexVector getColumn(int j) {
        if (_transposed)
            return _rows.get(j);
        int m = _rows.size();
        if ((j < 0) || (j >= _n))
            throw new DimensionException();
        ComplexVector V = ComplexVector.newInstance(m);
        for (int i = 0; i < m; i++) {
            V.set(i, _rows.get(i).get(j));
        }
        return V;
    }

    @Override
    public ComplexVector getDiagonal() {
        int m = this.getNumberOfRows();
        int n = this.getNumberOfColumns();
        int dimension = MathLib.min(m, n);
        ComplexVector V = ComplexVector.newInstance(dimension);
        for (int i = 0; i < dimension; i++) {
            V.set(i, this.get(i, i));
        }
        return V;
    }

    @Override
    public ComplexMatrix opposite() {
        ComplexMatrix M = ComplexMatrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).opposite());
        }
        return M;
    }

    @Override
    public ComplexMatrix plus(Matrix<Complex> that) {
        if (this.getNumberOfRows() != that.getNumberOfRows())
            throw new DimensionException();
        ComplexMatrix M = ComplexMatrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).plus(
                    _transposed ? that.getColumn(i) : that.getRow(i)));
        }
        return M;
    }

    @Override
    public ComplexMatrix minus(Matrix<Complex> that) { // Returns more specialized type.
        return this.plus(that.opposite());
    }

    @Override
    public ComplexMatrix times(Complex k) {
        ComplexMatrix M = ComplexMatrix.newInstance(_n, _transposed);
        for (int i = 0, p = _rows.size(); i < p; i++) {
            M._rows.add(_rows.get(i).times(k));
        }
        return M;
    }

    @Override
    public ComplexVector times(Vector<Complex> v) {
        if (v.getDimension() != this.getNumberOfColumns())
            throw new DimensionException();
        final int m = this.getNumberOfRows();
        ComplexVector V = ComplexVector.newInstance(m);
        for (int i = 0; i < m; i++) {
            V.set(i, this.getRow(i).times(v));
        }
        return V;
    }

    @Override
    public ComplexMatrix times(Matrix<Complex> that) {
        final int n = this.getNumberOfColumns();
        final int m = this.getNumberOfRows();
        final int p = that.getNumberOfColumns();
        if (that.getNumberOfRows() != n)
            throw new DimensionException();
        // Creates a mxp matrix in transposed form (p columns vectors of size m)
        ComplexMatrix M = ComplexMatrix.newInstance(m, true); // Transposed.
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

        private ComplexMatrix _left;

        private Matrix<Complex> _right;

        private int _rightColumnStart;

        private int _rightColumnEnd;

        private FastTable<ComplexVector> _columnsResult;

        static Multiply valueOf(ComplexMatrix left, Matrix<Complex> right,
                int rightColumnStart, int rightColumnEnd,
                FastTable<ComplexVector> columnsResult) {
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
                FastTable<ComplexVector> rows = _left.getRows();
                final int m = rows.size();
                for (int j = _rightColumnStart; j < _rightColumnEnd; j++) {
                    Vector<Complex> thatColj = _right.getColumn(j);
                    ComplexVector column = ComplexVector.newInstance(m);
                    _columnsResult.set(j, column);
                    for (int i = 0; i < m; i++) {
                        column.set(i, rows.get(i).times(thatColj));
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

    private FastTable<ComplexVector> getRows() {
        if (!_transposed)
            return _rows;
        FastTable<ComplexVector> rows = FastTable.newInstance();
        for (int i = 0; i < _n; i++) {
            rows.add(this.getRow(i));
        }
        return rows;
    }

    @Override
    public ComplexMatrix inverse() {
        if (!isSquare())
            throw new DimensionException("Matrix not square");
        return ComplexMatrix.valueOf(LUDecomposition.valueOf(this).inverse());
    }

    @Override
    public Complex determinant() {
        return LUDecomposition.valueOf(this).determinant();
    }

    @Override
    public ComplexMatrix transpose() {
        ComplexMatrix M = ComplexMatrix.newInstance(_n, !_transposed);
        M._rows.addAll(this._rows);
        return M;
    }

    @Override
    public Complex cofactor(int i, int j) {
        if (_transposed) {
            int k = i;
            i = j;
            j = k; // Swaps i,j
        }
        int m = _rows.size();
        ComplexMatrix M = ComplexMatrix.newInstance(m - 1, _transposed);
        for (int k1 = 0; k1 < m; k1++) {
            if (k1 == i)
                continue;
            ComplexVector row = _rows.get(k1);
            ComplexVector V = ComplexVector.newInstance(_n - 1);
            M._rows.add(V);
            for (int k2 = 0, k = 0; k2 < _n; k2++) {
                if (k2 == j)
                    continue;
                V.set(k++, row.get(k2));
            }
        }
        return M.determinant();
    }

    @Override
    public ComplexMatrix adjoint() {
        ComplexMatrix M = ComplexMatrix.newInstance(_n, _transposed);
        int m = _rows.size();
        for (int i = 0; i < m; i++) {
            ComplexVector row = ComplexVector.newInstance(_n);
            M._rows.add(row);
            for (int j = 0; j < _n; j++) {
                Complex cofactor = _transposed ? cofactor(j, i)
                        : cofactor(i, j);
                row.set(j, ((i + j) % 2 == 0) ? cofactor : cofactor.opposite());
            }
        }
        return M.transpose();
    }

    @Override
    public ComplexMatrix tensor(Matrix<Complex> that) {
        return ComplexMatrix.valueOf(DenseMatrix.valueOf(this).tensor(that));
    }

    @Override
    public ComplexVector vectorization() {
        return ComplexVector.valueOf(DenseMatrix.valueOf(this).vectorization());
    }

    @Override
    public ComplexMatrix copy() {
        ComplexMatrix M = ComplexMatrix.newInstance(_n, _transposed);
        for (ComplexVector row : _rows) {
            M._rows.add(row.copy());
        }
        return M;
    }

    ///////////////////////
    // Factory creation. //
    ///////////////////////

    static ComplexMatrix newInstance(int n, boolean transposed) {
        ComplexMatrix M = FACTORY.object();
        M._rows.clear();
        M._n = n;
        M._transposed = transposed;
        return M;
    }

    private static ObjectFactory<ComplexMatrix> FACTORY = new ObjectFactory<ComplexMatrix>() {
        @Override
        protected ComplexMatrix create() {
            return new ComplexMatrix();
        }
    };

    private ComplexMatrix() {
    }

    private static final long serialVersionUID = 1L;

}