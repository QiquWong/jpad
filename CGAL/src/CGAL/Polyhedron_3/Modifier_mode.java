/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Polyhedron_3;

public enum Modifier_mode {
  RELATIVE_INDEXING(0),
  ABSOLUTE_INDEXING(1);

  public final int swigValue() {
    return swigValue;
  }

  public static Modifier_mode swigToEnum(int swigValue) {
    Modifier_mode[] swigValues = Modifier_mode.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (Modifier_mode swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + Modifier_mode.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private Modifier_mode() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private Modifier_mode(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private Modifier_mode(Modifier_mode swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

