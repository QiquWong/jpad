/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_2;

public enum Face_badness {
  NOT_BAD,
  BAD,
  IMPERATIVELY_BAD;

  public final int swigValue() {
    return swigValue;
  }

  public static Face_badness swigToEnum(int swigValue) {
    Face_badness[] swigValues = Face_badness.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (Face_badness swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + Face_badness.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private Face_badness() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private Face_badness(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private Face_badness(Face_badness swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

