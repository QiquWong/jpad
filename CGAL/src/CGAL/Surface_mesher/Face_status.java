/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Surface_mesher;

public enum Face_status {
  NOT_IN_COMPLEX(0),
  ISOLATED(1),
  BOUNDARY,
  REGULAR,
  SINGULAR;

  public final int swigValue() {
    return swigValue;
  }

  public static Face_status swigToEnum(int swigValue) {
    Face_status[] swigValues = Face_status.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (Face_status swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + Face_status.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private Face_status() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private Face_status(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private Face_status(Face_status swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

