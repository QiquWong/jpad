/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_3;

public enum Mesh_facet_topology {
  FACET_VERTICES_ON_SURFACE(1),
  FACET_VERTICES_ON_SAME_SURFACE_PATCH,
  FACET_VERTICES_ON_SAME_SURFACE_PATCH_WITH_ADJACENCY_CHECK;

  public final int swigValue() {
    return swigValue;
  }

  public static Mesh_facet_topology swigToEnum(int swigValue) {
    Mesh_facet_topology[] swigValues = Mesh_facet_topology.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (Mesh_facet_topology swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + Mesh_facet_topology.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private Mesh_facet_topology() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private Mesh_facet_topology(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private Mesh_facet_topology(Mesh_facet_topology swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

