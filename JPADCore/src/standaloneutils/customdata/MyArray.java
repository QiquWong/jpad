package standaloneutils.customdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.function.Power;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.jscience.physics.amount.Amount;

import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;


/** 
 * A wrapper class which holds an array represented as a double[]
 * array and returns the desired kind of array. A MyArray instance
 * can be initialized with a double[], a Double[], a List,
 * a RealVector or a List<Amount>
 * 
 * @author Lorenzo Attanasio
 */
public class MyArray{

	private double[] doubleRaw, doubleInterpolated, xArray;
	private Double[] dDoubleRaw;
	private RealVector realVectorRaw;
	private List<Double> listRaw = new ArrayList<Double>();
	private Unit unit;
	private List amountListRaw = new ArrayList(); 

	public MyArray() {
		unit = Unit.ONE;
	}

	public MyArray(Unit u) {
		unit = u;
	}

	public MyArray(double[] d) {
		doubleRaw = d;
	}

	public MyArray(Double[] d) {
		setDouble(d);
	}

	public MyArray(double[] d, Unit u) {
		doubleRaw = d;
		unit = u;
	}

	public MyArray(Double[] d, Unit u) {
		setDouble(d);
		unit = u;
	}

	public MyArray(int dim){
		doubleRaw = new double[dim];
	}

	public MyArray(int dim, Unit u){
		doubleRaw = new double[dim];
		unit = u;
	}

	public MyArray(double d, int dim, Unit u) {
		fill(d, dim);
		unit = u;
	}

	public MyArray(MyArray myA) {
		doubleRaw = myA.doubleRaw;
		unit = myA.unit;
	}

	public MyArray(RealVector myA) {
		doubleRaw = myA.toArray();
	}

	public static MyArray createArray(double[] d) {
		return new MyArray(d); 
	}

	public static MyArray createArray(int dim) {
		return new MyArray(dim); 
	}

	public static MyArray createArray(int dim, Unit u) {
		return new MyArray(dim, u); 
	}

	public static MyArray createArray(Double[] d) {
		return new MyArray(d); 
	}

	public static MyArray createArray(double[] d, Unit u) {
		return new MyArray(d,u); 
	}

	public static MyArray createArray(double d, int dim, Unit u) {
		return new MyArray(d,dim,u); 
	}

	public MyArray getInterpolated() {
		//		double[] temp = doubleOriginal.clone();
		//		doubleOriginal = doubleRaw.clone();
		//		doubleRaw = temp.clone(); 
		//		setDouble(doubleOriginal);
		return this;
	}

	// Doubles
	public void setDouble(double[] d) {
		doubleRaw = d;
	}

	public void setDouble(Double[] D) {
		doubleRaw = ArrayUtils.toPrimitive(D);
	}


	public void setInteger(int[] i) {

		doubleRaw = new double[i.length];

		for (int k=0; k < i.length; k++) {
			doubleRaw[k] = (double) i[k];
		}
	}

	public void setInteger(Integer[] i) {

		doubleRaw = new double[i.length];

		for (int k=0; k < i.length; k++) {
			doubleRaw[k] = i[k].doubleValue();
		}
	}

	// Real vectors
	public void setRealVector(RealVector rv) {
		doubleRaw = rv.toArray();
	}

	// Amount lists
	public <T extends Quantity> void setAmountList(List<Amount<T>> list) {
		doubleRaw = MyArrayUtils.convertListOfAmountTodoubleArray(list);
	}

	// Double lists
	public void setList(List<Double> list) {
		doubleRaw = ArrayUtils.toPrimitive(list.toArray(new Double[list.size()]));
	}

	// Populate interpolated arrays --------------------------------

	public MyArray interpolate(double[] valuesWhereFunctionIsKnown, double[] valuesWhereYouWantToEvaluateTheFunction) {

		if(doubleRaw == null) toArray();
		doubleInterpolated = ArrayUtils.toPrimitive(
				(MyMathUtils.getInterpolatedValue1DLinear(
						valuesWhereFunctionIsKnown, 
						doubleRaw, 
						valuesWhereYouWantToEvaluateTheFunction)));

		return new MyArray(doubleInterpolated);
	}

	public MyArray interpolate(
			MyArray valuesWhereFunctionIsKnown, 
			MyArray valuesWhereYouWantToEvaluateTheFunction) {

		return createArray(MyMathUtils.getInterpolatedValue1DLinear(
				valuesWhereFunctionIsKnown.doubleRaw, 
				doubleRaw, 
				valuesWhereYouWantToEvaluateTheFunction.doubleRaw));
	}

	public MyArray interpolate(MyArray valuesWhereYouWantToEvaluateTheFunction) {

		return createArray(MyMathUtils.getInterpolatedValue1DLinear(xArray, 
				doubleRaw, 
				valuesWhereYouWantToEvaluateTheFunction.doubleRaw));
	}

	// Utility functions ------------------------------------------------
	
	public void linspace(double from, double to, int n) {
		doubleRaw = MyArrayUtils.linspace(from, to, n);
	}
	
	// Size
	public int size() {
		if (doubleRaw != null) return doubleRaw.length;	
		if (dDoubleRaw != null) return dDoubleRaw.length;
		if (listRaw != null) return listRaw.size();
		if (realVectorRaw != null) return realVectorRaw.getDimension();
		return 0;
	}

	/** Get element at i-th position */
	public double get(int i){
		if (doubleRaw == null){
			return listRaw.get(i);
		} else 
			return doubleRaw[i];
	}

	/** Get element at i position as amount */
	public <T extends Quantity> Amount<T> getAsAmount(int i){
		if (amountListRaw.size() == 0){
			amountListRaw = getAmountList();
			return (Amount<T>) amountListRaw.get(i);
		} else 
			return (Amount<T>) amountListRaw.get(i);
	}

	/** Set element at i position */
	public void set(int i, double d){
		listRaw.add(i, d);
	}

	/** Add an element */
	public void add(double d){
		if (doubleRaw != null 
				&& (doubleRaw.length != listRaw.size()
				|| listRaw == null
				|| listRaw.size() == 0)) {
			listRaw = getList();
		}
		listRaw.add(d);
	}

	public void concat(Double[] a) {
		getList();
		for (int i=0; i<a.length; i++) {
			listRaw.add(a[i]);
		}

		toArray();
	}

	public void concat(List<Double[]> a){
		for (Double[] x : a) {
			concat(x);
		}
	}

	public double getFirst() {
		if (doubleRaw == null) return listRaw.get(0);
		return doubleRaw[0];
	}

	public double getLast() {
		if (doubleRaw != null) return doubleRaw[doubleRaw.length-1];
		return listRaw.get(listRaw.size()-1);
	}

	public double getMin() {
		return getRealVector().getMinValue(); 
	}

	public double getMax() {
		return getRealVector().getMaxValue(); 
	}

	// Element by element multiply by another MyArray
	public MyArray times(MyArray myA) {
		if (doubleRaw == null) toArray();
		return createArray(myA.getRealVector().ebeMultiply(this.getRealVector()).toArray());
	}

	public RealVector times(RealVector myA) {
		return myA.ebeMultiply(this.getRealVector());
	}

	public double[] times(double[] d) {
		return this.getRealVector().ebeMultiply(MatrixUtils.createRealVector(d)).toArray();
	}

	public double[] times(double d) {
		return this.getRealVector().mapMultiply(d).toArray();
	}

	// Element by element divide by another MyArray
	public double[] divide(MyArray myA) {
		return myA.getRealVector().ebeDivide(this.getRealVector()).toArray();
	}

	public double[] divide(double d) {
		return this.getRealVector().mapDivide(d).toArray();
	}

	// Element by element power 
	public double[] pow(double d) {
		return getRealVector().map(new Power(d)).toArray();
	}

	public double[] sqrt() {
		return MyArrayUtils.sqrt(doubleRaw);
	}

	public MyArray minus(MyArray myA) {
		return createArray(getRealVector().subtract(myA.getRealVector()).toArray());
	}

	public MyArray minus(RealVector myA) {
		return createArray(getRealVector().subtract(myA).toArray());
	}

	public double[] minus(double[] d) {
		return getRealVector().subtract(MatrixUtils.createRealVector(d)).toArray();
	}

	public MyArray minus(double d) {
		return createArray(this.getRealVector().mapSubtract(d).toArray());
	}

	public MyArray plus(MyArray myA) {
		if (doubleRaw == null) toArray();
		return createArray(this.getRealVector().add(myA.getRealVector()).toArray());
	}
	
	public double getMean() {
		if (dDoubleRaw == null) toArray();
		return MyArrayUtils.getMean(doubleRaw);
	}

	public void fillOnes(int dim) {
		for (int i=0; i<dim; i++) {
			listRaw.add(1.);
		}
		setList(listRaw);
	}

	public void fillOnes() {
		for (int i=0; i<doubleRaw.length; i++) {
			listRaw.add(1.);
		}
		setList(listRaw);
	}

	public void fillZeros(int dim) {
		for (int i=0; i<dim; i++) {
			listRaw.add(0.);
		}
		setList(listRaw);
	}

	public void fill(double d, int dim) {
		for (int i=0; i<dim; i++) {
			listRaw.add(d);
		}
		setList(listRaw);
	}

	public void round(){
		round(4);
	}

	public MyArray round(int r){
		if (doubleRaw == null) toArray();
		double[] d = new double[doubleRaw.length];
		
		for (int i=0; i < doubleRaw.length; i++) {
			d[i] = BigDecimal.valueOf(doubleRaw[i]).setScale(r, RoundingMode.HALF_UP).doubleValue();
		}
		
		return new MyArray(d);
	}

	public String toString(){
		if (doubleRaw == null) toArray();
		return Arrays.toString(doubleRaw);
	}

	// Convert to another unit
	public  <T extends Quantity> List<Amount<?>> to(Unit<?> unit){

		List<Amount<T>> list = getAmountList();
		List<Amount<?>> listNew = new ArrayList<Amount<?>>();
		for (int i=0; i < list.size(); i++) {
			listNew.add(list.get(i).to(unit));
		}
		return listNew;
	}

	public void clear() {
		if (doubleRaw != null) doubleRaw = null;
		if (listRaw != null) listRaw.clear();
	}

	@Override
	public MyArray clone() {
		if (doubleRaw == null) toArray();
		return createArray(doubleRaw);
	}

	public double getAbsoluteMax(MyArray myA) {

		double compare;

		if (MyArrayUtils.getMax(doubleRaw) <= MyArrayUtils.getMax(((MyArray) myA).toArray())) {
			compare = MyArrayUtils.getMax(((MyArray) myA).toArray());
		} else {
			compare = MyArrayUtils.getMax(doubleRaw);
		} 

		return compare;
	}

	public double getAbsoluteMin(MyArray myA) {

		double compare;

		if (MyArrayUtils.getMin(doubleRaw) <= MyArrayUtils.getMin(((MyArray) myA).toArray())) {
			compare = MyArrayUtils.getMin(doubleRaw);
		} else {
			compare = MyArrayUtils.getMin(((MyArray) myA).toArray());
		} 

		return compare;
	}


	//----------------------------------

	public double[] toArray() {
		if ((doubleRaw == null && listRaw != null)
				|| (doubleRaw != null && listRaw != null
				&& listRaw.size() > doubleRaw.length)){
			doubleRaw = ArrayUtils.toPrimitive(listRaw.toArray(new Double[listRaw.size()]));
		}
		return doubleRaw;
	}

	public Double[] getdDouble() {
		return ArrayUtils.toObject(doubleRaw);
	}

	public RealVector getRealVector() {
		return MatrixUtils.createRealVector(doubleRaw);
	}


	public  <T extends Quantity> List<Amount<T>> getAmountList() {
		if (unit != null)
			return MyArrayUtils.convertDoubleArrayToListOfAmount(doubleRaw, unit);
		else
			return null;
	}

	public List<Double> getList() {
		if (doubleRaw != null) {
			return new ArrayList<Double>(Arrays.asList(ArrayUtils.toObject(doubleRaw)));
		}
		return null;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public double[] getDoubleInterpolated() {
		return doubleInterpolated;
	}

	public void setDoubleInterpolated(double[] doubleInterpolated) {
		this.doubleInterpolated = doubleInterpolated;
	}

	public static MyArray createArray(MyArray myArray) {
		return new MyArray(myArray);
	}

	public double[] getxArray() {
		return xArray;
	}

	public void setxArray(double[] xArray) {
		this.xArray = xArray;
	}

}
