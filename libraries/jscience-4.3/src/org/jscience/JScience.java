/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.jscience;

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

import javax.measure.quantity.*;
import javax.measure.unit.*;

import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.geography.coordinates.Altitude;
import org.jscience.geography.coordinates.CompoundCoordinates;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.Time;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.XYZ;
import org.jscience.geography.coordinates.crs.CoordinatesConverter;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Complex;
import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.ModuloInteger;
import org.jscience.mathematics.number.Rational;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.ComplexMatrix;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;
import org.jscience.mathematics.vector.Float64Matrix;
import org.jscience.mathematics.vector.Matrix;
import org.jscience.mathematics.vector.Vector;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import org.jscience.physics.model.RelativisticModel;

import javolution.lang.Configurable;
import javolution.lang.MathLib;
import javolution.text.TextBuilder;
import javolution.context.ConcurrentContext;
import javolution.context.LocalContext;
import javolution.context.StackContext;
import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;
import static org.jscience.economics.money.Currency.*;

/**
 * <p> This class represents the <b>J</b>Science library; it contains the
 *    {@link #main} method for versionning, self-tests, and performance 
 *    analysis.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 13, 2006
 */
public final class JScience {
  
    /**
     * Holds the version information.
     */
    public final static String VERSION = "@VERSION@";

    public static Configurable<Character> MODULO = new Configurable<Character>('w');

    
    /**
     * Default constructor.
     */
    private JScience() {// Forbids derivation.
    }
    
    /**
     * The library {@link #main} method. The archive <codejscience.jar</code>
     * is auto-executable.
     * <ul>
     * <li><code>java [-cp javolution.jar] -jar jscience.jar version</code>
     * to output version information.</li>
     * <li><code>java [-cp javolution.jar] -jar jscience.jar test</code> to
     * perform self-tests.</li>
     * <li><code>java [-cp javolution.jar] -jar jscience.jar perf</code> for
     * performance analysis.</li>
     * </ul>
     * 
     * @param args the option arguments.
     * @throws Exception if a problem occurs.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Version " + VERSION + " (http://jscience.org)");
        System.out.println("");
        if (args.length > 0) {
            if (args[0].equals("version")) {
                System.out.println("Version " + VERSION);
                return;
            } else if (args[0].equals("test")) {
                testing();
                return;
            } else if (args[0].equals("perf")) {
                benchmark();
                return;
            }
        }
        System.out
                .println("Usage: java [-cp javolution.jar] -jar jscience.jar [arg]");
        System.out.println("where arg is one of:");
        System.out.println("    version (to show version information)");
        System.out.println("    test    (to perform self-tests)");
        System.out.println("    perf    (to run benchmark)");
    }
    
    /**
     * Performs simple tests.
     * 
     * @throws Exception if a problem occurs.
     */
    private static void testing() throws Exception {
        System.out.println("Load Configurable Parameters from System.getProperties()...");
        Configurable.read(System.getProperties());
        System.out.println("");
        
        System.out.println("Testing...");   
        {
            System.out.println("");
            System.out.println("Exact Measurements");
            Amount<Mass> m0 = Amount.valueOf(100, POUND);
            Amount<Mass> m1 = m0.times(33).divide(2);
            Amount<ElectricCurrent> m2 = Amount.valueOf("234 mA").to(
                    MICRO(AMPERE));
            System.out.println("m0 = " + m0);
            System.out.println("m1 = " + m1);
            System.out.println("m2 = " + m2);

            System.out.println("");
            System.out.println("Inexact Measurements");
            Amount<Mass> m3 = Amount.valueOf(100.0, POUND);
            Amount<Mass> m4 = m0.divide(3);
            Amount<ElectricCurrent> m5 = Amount.valueOf("234 mA").to(AMPERE);
            Amount<Temperature> t0 = Amount.valueOf(-7.3, 0.5, CELSIUS);
            System.out.println("m3 = " + m3);
            System.out.println("m4 = " + m4);
            System.out.println("m5 = " + m5);
            System.out.println("t0 = " + t0);

            System.out.println("");
            System.out.println("Interval measurements");
            Amount<Volume> m6 = Amount.valueOf(20, 0.1, LITRE);
            Amount<Frequency> m7 = Amount.rangeOf(10, 11, KILO(HERTZ));
            System.out.println("m6 = " + m6);
            System.out.println("m7 = " + m7);

            System.out.println("");
            System.out.println("Amount.equals (identical) / Amount.approximates " +
                    "(takes into account errors such as numeric errors)");
            Amount<Frequency> m8 = Amount.valueOf(9000, HERTZ);
            Amount<Frequency> m10 = m8.divide(3).times(3); // Still exact.
            Amount<Frequency> m11 = m8.divide(7).times(7); // No more exact.
            System.out.println("m8 = " + m8);
            System.out.println("m10 = " + m10);
            System.out.println("m11 = " + m11);
            System.out.println("(m10 == m8) = " + m10.equals(m8));
            System.out.println("(m10 ≅ m8) = " + m10.approximates(m8));
            System.out.println("(m11 == m8) = " + m11.equals(m8));
            System.out.println("(m11 ≅ m8) = " + m11.approximates(m8));

            System.out.println("");
            System.out.println("AmountFormat - Plus/Minus Error (3 digits error)");
            AmountFormat.setInstance(AmountFormat
                    .getPlusMinusErrorInstance(3));
            System.out.println("m3 = " + m3);
            System.out.println("m4 = " + m4);
            System.out.println("m5 = " + m5);

            System.out.println("");
            System.out.println("AmountFormat - Bracket Error (2 digits error)");
            AmountFormat.setInstance(AmountFormat.getBracketErrorInstance(2));
            System.out.println("m3 = " + m3);
            System.out.println("m4 = " + m4);
            System.out.println("m5 = " + m5);

            System.out.println("");
            System.out.println("AmountFormat - Exact Digits Only");
            AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
            System.out.println("m3 = " + m3);
            System.out.println("m4 = " + m4);
            System.out.println("m5 = " + m5);

            System.out.println("");
            System.out.println("Numeric Errors");
            {
                Amount<Length> x = Amount.valueOf(1.0, METRE);
                Amount<Velocity> v = Amount.valueOf(0.01, METRES_PER_SECOND);
                Amount<Duration> t = Amount.valueOf(1.0, MICRO(SECOND));
                long ns = System.nanoTime();
                for (int i = 0; i < 10000000; i++) {
                    x = x.plus(v.times(t));
                }
                ns = System.nanoTime() - ns;
                AmountFormat.setInstance(AmountFormat
                        .getExactDigitsInstance());
                System.out.println(x
                        + " ("
                        + Amount.valueOf(ns, 0.5, NANO(SECOND)).to(
                                MILLI(SECOND)) + ")");
            }
            {
                double x = 1.0; // m
                double v = 0.01; // m/s
                double t = 1E-6; // s
                for (int i = 0; i < 10000000; i++) {
                    x += v * t; // Note: Most likely the compiler get v * t out of the loop.
                }
                System.out.println(x);
            }
            AmountFormat.setInstance(AmountFormat
                    .getPlusMinusErrorInstance(2));
        }
        {
            System.out.println("");
            System.out.println("Physical Models");
            // Selects a relativistic model for dimension checking (typically at start-up).
            RelativisticModel.select(); 

            // Length and Duration can be added.
            Amount<Length> x = Amount.valueOf(100, NonSI.INCH);
            x = x.plus(Amount.valueOf("2.3 µs")).to(METRE); 
            System.out.println(x); 
               
            // Energy is compatible with mass (E=mc2)
            Amount<Mass> m = Amount.valueOf("12 GeV").to(KILOGRAM); 
            System.out.println(m); 
        }

        {
            System.out.println("");
            System.out.println("Money/Currencies");
            ///////////////////////////////////////////////////////////////////////
            // Calculates the cost of a car trip in Europe for an American tourist.
            ///////////////////////////////////////////////////////////////////////

            // Use currency symbols instead of ISO-4217 codes.
            UnitFormat.getInstance().label(USD, "$"); // Use "$" symbol instead of currency code ("USD")
            UnitFormat.getInstance().label(EUR, "€"); // Use "€" symbol instead of currency code ("EUR")

            // Sets exchange rates.
            Currency.setReferenceCurrency(USD);
            EUR.setExchangeRate(1.17); // 1.0 € = 1.17 $

            // Calculates trip cost.
            Amount<?> carMileage = Amount.valueOf(20, MILE
                    .divide(GALLON_LIQUID_US)); // 20 mi/gal.
            Amount<?> gazPrice = Amount.valueOf(1.2, EUR.divide(LITRE)); // 1.2 €/L
            Amount<Length> tripDistance = Amount.valueOf(400, KILO(SI.METRE)); // 400 km
            Amount<Money> tripCost = tripDistance.divide(carMileage).times(
                    gazPrice).to(USD);
            // Displays cost.
            System.out.println("Trip cost = " + tripCost + " ("
                    + tripCost.to(EUR) + ")");
        }
        {
            System.out.println("");
            System.out.println("Matrices/Vectors");

            Amount<ElectricResistance> R1 = Amount.valueOf(100, 1, OHM); // 1% precision. 
            Amount<ElectricResistance> R2 = Amount.valueOf(300, 3, OHM); // 1% precision.
            Amount<ElectricPotential> U0 = Amount.valueOf(28, 0.01, VOLT); // ±0.01 V fluctuation.

            // Equations:  U0 = U1 + U2       |1  1  0 |   |U1|   |U0|
            //             U1 = R1 * I    =>  |-1 0  R1| * |U2| = |0 |
            //             U2 = R2 * I        |0 -1  R2|   |I |   |0 |
            //
            //                                    A      *  X   =  B
            //
            DenseMatrix<Amount<?>> A = DenseMatrix.valueOf(new Amount<?>[][] {
                { Amount.ONE,            Amount.ONE,            Amount.valueOf(0, OHM) },
                { Amount.ONE.opposite(), Amount.ZERO,           R1 },
                { Amount.ZERO,           Amount.ONE.opposite(), R2 } });
            DenseVector<Amount<?>> B = DenseVector.valueOf(new Amount<?>[] 
                { U0, Amount.valueOf(0, VOLT), Amount.valueOf(0, VOLT) });
            Vector<Amount<?>> X = A.solve(B);
            System.out.println(X);
            System.out.println(X.get(2).to(MILLI(AMPERE)));
        }
        {
            System.out.println("");
            System.out.println("Polynomials");

            // Defines two local variables (x, y).
            Variable<Complex> varX = new Variable.Local<Complex>("x");
            Variable<Complex> varY = new Variable.Local<Complex>("y");

            // f(x) = 1 + 2x + ix²
            Polynomial<Complex> x = Polynomial.valueOf(Complex.ONE, varX);
            Polynomial<Complex> fx = x.pow(2).times(Complex.I).plus(
                    x.times(Complex.valueOf(2, 0)).plus(Complex.ONE));
            System.out.println(fx);
            System.out.println(fx.pow(2));
            System.out.println(fx.differentiate(varX));
            System.out.println(fx.integrate(varY));
            System.out.println(fx.compose(fx));

            // Calculates expression.
            varX.set(Complex.valueOf(2, 3));
            System.out.println(fx.evaluate());
        }

        {
            System.out.println("");
            System.out.println("Coordinates Conversions");

            // Simple Lat/Long to UTM conversion.
            CoordinatesConverter<LatLong, UTM> latLongToUTM = LatLong.CRS
                    .getConverterTo(UTM.CRS);
            LatLong latLong = LatLong.valueOf(34.34, 23.56, DEGREE_ANGLE);
            UTM utm = latLongToUTM.convert(latLong);
            System.out.println(utm);

            // Lat/Long to XYZ conversion (assume height of zero).
            CoordinatesConverter<LatLong, XYZ> latLongToXYZ = LatLong.CRS
                    .getConverterTo(XYZ.CRS);
            XYZ xyz = latLongToXYZ.convert(latLong);
            System.out.println(xyz);

            // Compound coordinates - Lat/Long/Alt to XYZ conversion.
            Altitude alt = Altitude.valueOf(2000, FOOT);
            CompoundCoordinates<LatLong, Altitude> latLongAlt = 
                CompoundCoordinates.valueOf(latLong, alt);
            xyz = latLongAlt.getCoordinateReferenceSystem().getConverterTo(
                    XYZ.CRS).convert(latLongAlt);
            System.out.println(xyz);

            // Even more compounding...
            Time time = Time.valueOf(new Date());
            CompoundCoordinates<CompoundCoordinates<LatLong, Altitude>, Time> 
                latLongAltTime = CompoundCoordinates.valueOf(latLongAlt, time);
            System.out.println(latLongAltTime);
        }

        {
            System.out.println("");
            System.out.println("Numbers");

            Real two = Real.valueOf(2); // 2.0000..00 
            Real three = Real.valueOf(3);
            Real.setExactPrecision(100); // Assumes 100 exact digits for exact numbers.

            System.out.println("2/3       = " + two.divide(three));
            Real sqrt2 = two.sqrt();
            System.out.println("sqrt(2)   = " + sqrt2);
            System.out.println("Precision = " + sqrt2.getPrecision()
                    + " digits.");

            LargeInteger dividend = LargeInteger.valueOf("3133861182986538201");
            LargeInteger divisor = LargeInteger.valueOf("25147325102501733369");
            Rational rational = Rational.valueOf(dividend, divisor);
            System.out.println("rational  = " + rational);

            ModuloInteger m = ModuloInteger.valueOf("233424242346");
            LocalContext.enter(); // Avoids impacting others threads.
            try {
                ModuloInteger.setModulus(LargeInteger.valueOf("31225208137"));
                ModuloInteger inv = m.inverse();
                System.out.println("inverse modulo = " + inv);

                ModuloInteger one = inv.times(m);
                System.out.println("verification: one = " + one);

            } finally {
                LocalContext.exit();
            }

        }
    }
    
    /**
     * Measures performance.
     */
    private static void benchmark() throws Exception {
        System.out.println("Load Configurable Parameters from System.getProperties()...");
        Configurable.read(System.getProperties());
        System.out.println("");

        System.out.println("Benchmark...");
 
        Object[] results = new Object[10000];

        System.out.println("");
        System.out.println("Numerics Operations");

        System.out.print("Float64 add: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            StackContext.enter();
            Float64 x = Float64.ONE;
            for (int j = 0; j < results.length; j++) {
                results[j] = x.plus(x);
            }
            StackContext.exit();
        }
        endTime(10000 * results.length);

        System.out.print("Float64 multiply: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            StackContext.enter();
            Float64 x = Float64.valueOf(1.0);
            for (int j = 0; j < results.length; j++) {
                results[j] = x.times(x);
            }
            StackContext.exit();
        }
        endTime(10000 * results.length);

        System.out.print("Complex add: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            StackContext.enter();
            Complex x = Complex.valueOf(1.0, 2.0);
            for (int j = 0; j < results.length; j++) {
                results[j] = x.plus(x);
            }
            StackContext.exit();
        }
        endTime(10000 * results.length);

        System.out.print("Complex multiply: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            StackContext.enter();
            Complex x = Complex.valueOf(1.0, 2.0);
            for (int j = 0; j < results.length; j++) {
                results[j] = x.times(x);
            }
            StackContext.exit();
        }
        endTime(10000 * results.length);

        System.out.print("Amount<Mass> add: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            StackContext.enter();
            Amount<Mass> x = Amount.valueOf(1.0, SI.KILOGRAM);
            for (int j = 0; j < results.length; j++) {
                results[j] = x.plus(x);
            }
            StackContext.exit();
        }
        endTime(10000 * results.length);

        System.out.print("Amount<Mass> multiply: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            StackContext.enter();
            Amount<Mass> x = Amount.valueOf(1.0, SI.KILOGRAM);
            for (int j = 0; j < results.length; j++) {
                results[j] = x.times(x);
            }
            StackContext.exit();
        }
        endTime(10000 * results.length);

        System.out.println();
        System.out.println("LargeInteger (StackContext) versus BigInteger");
        BigInteger big = BigInteger.probablePrime(1024, new Random());
        byte[] bytes = big.toByteArray();
        LargeInteger large = LargeInteger.valueOf(bytes, 0, bytes.length);

        System.out.print("LargeInteger (1024 bits) addition: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            StackContext.enter();
            for (int j = 0; j < results.length; j++) {
                results[j] = large.plus(large);
            }
            StackContext.exit();
        }
        endTime(1000 * results.length);

        System.out.print("LargeInteger (1024 bits) multiplication: ");
        startTime();
        for (int i = 0; i < 100; i++) {
            StackContext.enter();
            for (int j = 0; j < results.length; j++) {
                results[j] = large.times(large);
            }
            StackContext.exit();
        }
        endTime(100 * results.length);

        System.out.print("BigInteger (1024 bits) addition: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < results.length; j++) {
                results[j] = big.add(big);
            }
        }
        endTime(1000 * results.length);

        System.out.print("BigInteger (1024 bits) multiplication: ");
        startTime();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < results.length; j++) {
                results[j] = big.multiply(big);
            }
        }
        endTime(100 * results.length);

        System.out.println();
        System.out.println("Matrix<Float64> and Matrix<Complex> versus "
                + "non-parameterized matrix (double)");
        final int size = 500;
        double[][] values = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                values[i][j] = MathLib.random();
            }
        }

        System.out.println("Javolution Concurrency Disabled");
        LocalContext.enter(); // Local setting.
        try {
            ConcurrentContext.setConcurrency(0);
            multiplyMatrices(values);
        } finally {
            LocalContext.exit();
        }
        
        System.out.println("Javolution Concurrency: " + ConcurrentContext.getConcurrency());
        multiplyMatrices(values);

        System.out.println();
        System.out.println("More performance analysis in future versions...");
    }
    
    private static void multiplyMatrices(double[][] values) {
        
        int size = values.length;
        
        System.out.print("Non-parameterized matrix (double based)"
                + " 500x500 multiplication: ");
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                values[i][j] = MathLib.random();
            }
        }
        MatrixDouble PM = new MatrixDouble(values);
        for (int i=0; i < 5; i++) PM.times(PM); // Warming up.
        startTime();
        MatrixDouble R1 = PM.times(PM);
        endTime(1);

        System.out.print("Matrix<Float64> 500x500 multiplication: ");
        Matrix<Float64> FM = Float64Matrix.valueOf(values);
        for (int i=0; i < 5; i++) FM.times(FM); // Warming up.
        startTime();
        Matrix<Float64> R2 = FM.times(FM);
        endTime(1);
        
        // Checks results.
        if (!R2.equals(Float64Matrix.valueOf(R1.o))) 
                throw new Error("Error in matrix multiplication");

        System.out.print("Matrix<Complex> 500x500 multiplication: ");
        Complex[][] complexes = new Complex[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                complexes[i][j] = Complex.valueOf(MathLib.random(), MathLib
                        .random());
            }
        }
        Matrix<Complex> CM = ComplexMatrix.valueOf(complexes);
        for (int i=0; i < 5; i++) CM.times(CM); // Warming up.
        startTime();
        CM.times(CM);
        endTime(1);

        System.out.print("Matrix<Amount> 500x500 multiplication: ");
        Amount<?>[][] measures = new Amount<?>[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                measures[i][j] = Amount.valueOf(
                        MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE), Unit.ONE);
            }
        }
        DenseMatrix<Amount<?>> MM = DenseMatrix.valueOf(measures);
        startTime();
        MM.times(MM);
        endTime(1);
        
    }

    private static final class MatrixDouble {
        double[][] o;

        int m; // Nbr of rows.

        int n; // Nbr of columns.

        MatrixDouble(double[][] elements) {
            o = elements;
            m = elements.length;
            n = elements[0].length;
        }

        MatrixDouble times(MatrixDouble that) {
            if (that.m != this.n) 
                throw new Error("Wrong dimensions");
            MatrixDouble M = new MatrixDouble(new double[this.m][that.n]);
            double[] thatColj = new double[n];
            for (int j = 0; j < that.n; j++) {
               for (int k = 0; k < n; k++) {
                  thatColj[k] = that.o[k][j];
               }
               for (int i = 0; i < m; i++) {
                  double[] thisRowi = o[i];
                  double s = 0;
                  for (int k = 0; k < n; k++) {
                     s += thisRowi[k]*thatColj[k];
                  }
                  M.o[i][j] = s;
               }
            }
            return M;
        }
    }

    private static void startTime() {
        _time = System.nanoTime();
    }

    /**
     * Ends measuring time and display the execution time per iteration.
     * 
     * @param iterations
     *            the number iterations performed since {@link #startTime}.
     */
    public static void endTime(int iterations) {
        long nanoSeconds = System.nanoTime() - _time;
        long picoDuration = nanoSeconds * 1000 / iterations;
        long divisor;
        String unit;
        if (picoDuration > 1000 * 1000 * 1000 * 1000L) { // 1 s
            unit = " s";
            divisor = 1000 * 1000 * 1000 * 1000L;
        } else if (picoDuration > 1000 * 1000 * 1000L) {
            unit = " ms";
            divisor = 1000 * 1000 * 1000L;
        } else if (picoDuration > 1000 * 1000L) {
            unit = " us";
            divisor = 1000 * 1000L;
        } else {
            unit = " ns";
            divisor = 1000L;
        }
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(picoDuration / divisor);
        int fracDigits = 4 - tb.length(); // 4 digits precision.
        tb.append(".");
        for (int i = 0, j = 10; i < fracDigits; i++, j *= 10) {
            tb.append((picoDuration * j / divisor) % 10);
        }
        System.out.println(tb.append(unit));
    }

    private static long _time;

}