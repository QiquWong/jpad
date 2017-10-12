// Autogenerated code. Do not modify.
package standaloneutils.launchers.avl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.UnaryOperator;
import javax.annotation.Generated;

/**
 * Auto-generated superclass of {@link AVLMainInputData.Builder},
 * derived from the API of {@link AVLMainInputData}.
 */
@Generated("org.inferred.freebuilder.processor.CodeGenerator")
abstract class AVLMainInputData_Builder {

  /**
   * Creates a new builder using {@code value} as a template.
   */
  public static AVLMainInputData.Builder from(AVLMainInputData value) {
    return new AVLMainInputData.Builder().mergeFrom(value);
  }

  private static final Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();

  private enum Property {
    DESCRIPTION("description"),
    MACH("mach"),
    I_YSYM("IYsym"),
    I_ZSYM("IZsym"),
    ZSYM("zsym"),
    SREF("sref"),
    CREF("cref"),
    BREF("bref"),
    XREF("xref"),
    YREF("yref"),
    ZREF("zref"),
    C_D0REF("CD0ref"),
    ;

    private final String name;

    private Property(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private String description;
  private Double mach;
  private Integer IYsym;
  private Integer IZsym;
  private Double zsym;
  private Double sref;
  private Double cref;
  private Double bref;
  private Double xref;
  private Double yref;
  private Double zref;
  private Double CD0ref;
  private final EnumSet<AVLMainInputData_Builder.Property> _unsetProperties =
      EnumSet.allOf(AVLMainInputData_Builder.Property.class);

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getDescription()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code description} is null
   */
  public AVLMainInputData.Builder setDescription(String description) {
    this.description = Preconditions.checkNotNull(description);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.DESCRIPTION);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getDescription()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapDescription(UnaryOperator<String> mapper) {
    Preconditions.checkNotNull(mapper);
    return setDescription(mapper.apply(getDescription()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getDescription()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public String getDescription() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.DESCRIPTION),
        "description not set");
    return description;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getMach()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mach} is null
   */
  public AVLMainInputData.Builder setMach(Double mach) {
    this.mach = Preconditions.checkNotNull(mach);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.MACH);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getMach()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapMach(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setMach(mapper.apply(getMach()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getMach()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getMach() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.MACH), "mach not set");
    return mach;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getIYsym()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code IYsym} is null
   */
  public AVLMainInputData.Builder setIYsym(Integer IYsym) {
    this.IYsym = Preconditions.checkNotNull(IYsym);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.I_YSYM);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getIYsym()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapIYsym(UnaryOperator<Integer> mapper) {
    Preconditions.checkNotNull(mapper);
    return setIYsym(mapper.apply(getIYsym()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getIYsym()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Integer getIYsym() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.I_YSYM), "IYsym not set");
    return IYsym;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getIZsym()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code IZsym} is null
   */
  public AVLMainInputData.Builder setIZsym(Integer IZsym) {
    this.IZsym = Preconditions.checkNotNull(IZsym);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.I_ZSYM);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getIZsym()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapIZsym(UnaryOperator<Integer> mapper) {
    Preconditions.checkNotNull(mapper);
    return setIZsym(mapper.apply(getIZsym()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getIZsym()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Integer getIZsym() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.I_ZSYM), "IZsym not set");
    return IZsym;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getZsym()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code zsym} is null
   */
  public AVLMainInputData.Builder setZsym(Double zsym) {
    this.zsym = Preconditions.checkNotNull(zsym);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.ZSYM);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getZsym()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapZsym(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setZsym(mapper.apply(getZsym()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getZsym()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getZsym() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.ZSYM), "zsym not set");
    return zsym;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getSref()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code sref} is null
   */
  public AVLMainInputData.Builder setSref(Double sref) {
    this.sref = Preconditions.checkNotNull(sref);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.SREF);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getSref()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapSref(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setSref(mapper.apply(getSref()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getSref()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getSref() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.SREF), "sref not set");
    return sref;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getCref()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code cref} is null
   */
  public AVLMainInputData.Builder setCref(Double cref) {
    this.cref = Preconditions.checkNotNull(cref);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.CREF);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getCref()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapCref(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setCref(mapper.apply(getCref()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getCref()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getCref() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.CREF), "cref not set");
    return cref;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getBref()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code bref} is null
   */
  public AVLMainInputData.Builder setBref(Double bref) {
    this.bref = Preconditions.checkNotNull(bref);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.BREF);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getBref()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapBref(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setBref(mapper.apply(getBref()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getBref()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getBref() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.BREF), "bref not set");
    return bref;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getXref()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code xref} is null
   */
  public AVLMainInputData.Builder setXref(Double xref) {
    this.xref = Preconditions.checkNotNull(xref);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.XREF);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getXref()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapXref(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setXref(mapper.apply(getXref()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getXref()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getXref() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.XREF), "xref not set");
    return xref;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getYref()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code yref} is null
   */
  public AVLMainInputData.Builder setYref(Double yref) {
    this.yref = Preconditions.checkNotNull(yref);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.YREF);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getYref()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapYref(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setYref(mapper.apply(getYref()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getYref()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getYref() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.YREF), "yref not set");
    return yref;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getZref()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code zref} is null
   */
  public AVLMainInputData.Builder setZref(Double zref) {
    this.zref = Preconditions.checkNotNull(zref);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.ZREF);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getZref()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapZref(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setZref(mapper.apply(getZref()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getZref()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getZref() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.ZREF), "zref not set");
    return zref;
  }

  /**
   * Sets the value to be returned by {@link AVLMainInputData#getCD0ref()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code CD0ref} is null
   */
  public AVLMainInputData.Builder setCD0ref(Double CD0ref) {
    this.CD0ref = Preconditions.checkNotNull(CD0ref);
    _unsetProperties.remove(AVLMainInputData_Builder.Property.C_D0REF);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLMainInputData#getCD0ref()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLMainInputData.Builder mapCD0ref(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setCD0ref(mapper.apply(getCD0ref()));
  }

  /**
   * Returns the value that will be returned by {@link AVLMainInputData#getCD0ref()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getCD0ref() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLMainInputData_Builder.Property.C_D0REF), "CD0ref not set");
    return CD0ref;
  }

  /**
   * Sets all property values using the given {@code AVLMainInputData} as a template.
   */
  public AVLMainInputData.Builder mergeFrom(AVLMainInputData value) {
    AVLMainInputData_Builder _defaults = new AVLMainInputData.Builder();
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.DESCRIPTION)
        || !value.getDescription().equals(_defaults.getDescription())) {
      setDescription(value.getDescription());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.MACH)
        || !value.getMach().equals(_defaults.getMach())) {
      setMach(value.getMach());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.I_YSYM)
        || !value.getIYsym().equals(_defaults.getIYsym())) {
      setIYsym(value.getIYsym());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.I_ZSYM)
        || !value.getIZsym().equals(_defaults.getIZsym())) {
      setIZsym(value.getIZsym());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.ZSYM)
        || !value.getZsym().equals(_defaults.getZsym())) {
      setZsym(value.getZsym());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.SREF)
        || !value.getSref().equals(_defaults.getSref())) {
      setSref(value.getSref());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.CREF)
        || !value.getCref().equals(_defaults.getCref())) {
      setCref(value.getCref());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.BREF)
        || !value.getBref().equals(_defaults.getBref())) {
      setBref(value.getBref());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.XREF)
        || !value.getXref().equals(_defaults.getXref())) {
      setXref(value.getXref());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.YREF)
        || !value.getYref().equals(_defaults.getYref())) {
      setYref(value.getYref());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.ZREF)
        || !value.getZref().equals(_defaults.getZref())) {
      setZref(value.getZref());
    }
    if (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.C_D0REF)
        || !value.getCD0ref().equals(_defaults.getCD0ref())) {
      setCD0ref(value.getCD0ref());
    }
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Copies values from the given {@code Builder}.
   * Does not affect any properties not set on the input.
   */
  public AVLMainInputData.Builder mergeFrom(AVLMainInputData.Builder template) {
    // Upcast to access private fields; otherwise, oddly, we get an access violation.
    AVLMainInputData_Builder base = (AVLMainInputData_Builder) template;
    AVLMainInputData_Builder _defaults = new AVLMainInputData.Builder();
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.DESCRIPTION)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.DESCRIPTION)
            || !template.getDescription().equals(_defaults.getDescription()))) {
      setDescription(template.getDescription());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.MACH)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.MACH)
            || !template.getMach().equals(_defaults.getMach()))) {
      setMach(template.getMach());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.I_YSYM)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.I_YSYM)
            || !template.getIYsym().equals(_defaults.getIYsym()))) {
      setIYsym(template.getIYsym());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.I_ZSYM)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.I_ZSYM)
            || !template.getIZsym().equals(_defaults.getIZsym()))) {
      setIZsym(template.getIZsym());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.ZSYM)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.ZSYM)
            || !template.getZsym().equals(_defaults.getZsym()))) {
      setZsym(template.getZsym());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.SREF)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.SREF)
            || !template.getSref().equals(_defaults.getSref()))) {
      setSref(template.getSref());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.CREF)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.CREF)
            || !template.getCref().equals(_defaults.getCref()))) {
      setCref(template.getCref());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.BREF)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.BREF)
            || !template.getBref().equals(_defaults.getBref()))) {
      setBref(template.getBref());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.XREF)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.XREF)
            || !template.getXref().equals(_defaults.getXref()))) {
      setXref(template.getXref());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.YREF)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.YREF)
            || !template.getYref().equals(_defaults.getYref()))) {
      setYref(template.getYref());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.ZREF)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.ZREF)
            || !template.getZref().equals(_defaults.getZref()))) {
      setZref(template.getZref());
    }
    if (!base._unsetProperties.contains(AVLMainInputData_Builder.Property.C_D0REF)
        && (_defaults._unsetProperties.contains(AVLMainInputData_Builder.Property.C_D0REF)
            || !template.getCD0ref().equals(_defaults.getCD0ref()))) {
      setCD0ref(template.getCD0ref());
    }
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Resets the state of this builder.
   */
  public AVLMainInputData.Builder clear() {
    AVLMainInputData_Builder _defaults = new AVLMainInputData.Builder();
    description = _defaults.description;
    mach = _defaults.mach;
    IYsym = _defaults.IYsym;
    IZsym = _defaults.IZsym;
    zsym = _defaults.zsym;
    sref = _defaults.sref;
    cref = _defaults.cref;
    bref = _defaults.bref;
    xref = _defaults.xref;
    yref = _defaults.yref;
    zref = _defaults.zref;
    CD0ref = _defaults.CD0ref;
    _unsetProperties.clear();
    _unsetProperties.addAll(_defaults._unsetProperties);
    return (AVLMainInputData.Builder) this;
  }

  /**
   * Returns a newly-created {@link AVLMainInputData} based on the contents of the {@code Builder}.
   *
   * @throws IllegalStateException if any field has not been set
   */
  public AVLMainInputData build() {
    Preconditions.checkState(_unsetProperties.isEmpty(), "Not set: %s", _unsetProperties);
    return new AVLMainInputData_Builder.Value(this);
  }

  /**
   * Returns a newly-created partial {@link AVLMainInputData}
   * based on the contents of the {@code Builder}.
   * State checking will not be performed.
   * Unset properties will throw an {@link UnsupportedOperationException}
   * when accessed via the partial object.
   *
   * <p>Partials should only ever be used in tests.
   */
  @VisibleForTesting()
  public AVLMainInputData buildPartial() {
    return new AVLMainInputData_Builder.Partial(this);
  }

  private static final class Value implements AVLMainInputData {
    private final String description;
    private final Double mach;
    private final Integer IYsym;
    private final Integer IZsym;
    private final Double zsym;
    private final Double sref;
    private final Double cref;
    private final Double bref;
    private final Double xref;
    private final Double yref;
    private final Double zref;
    private final Double CD0ref;

    private Value(AVLMainInputData_Builder builder) {
      this.description = builder.description;
      this.mach = builder.mach;
      this.IYsym = builder.IYsym;
      this.IZsym = builder.IZsym;
      this.zsym = builder.zsym;
      this.sref = builder.sref;
      this.cref = builder.cref;
      this.bref = builder.bref;
      this.xref = builder.xref;
      this.yref = builder.yref;
      this.zref = builder.zref;
      this.CD0ref = builder.CD0ref;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public Double getMach() {
      return mach;
    }

    @Override
    public Integer getIYsym() {
      return IYsym;
    }

    @Override
    public Integer getIZsym() {
      return IZsym;
    }

    @Override
    public Double getZsym() {
      return zsym;
    }

    @Override
    public Double getSref() {
      return sref;
    }

    @Override
    public Double getCref() {
      return cref;
    }

    @Override
    public Double getBref() {
      return bref;
    }

    @Override
    public Double getXref() {
      return xref;
    }

    @Override
    public Double getYref() {
      return yref;
    }

    @Override
    public Double getZref() {
      return zref;
    }

    @Override
    public Double getCD0ref() {
      return CD0ref;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AVLMainInputData_Builder.Value)) {
        return false;
      }
      AVLMainInputData_Builder.Value other = (AVLMainInputData_Builder.Value) obj;
      return Objects.equals(description, other.description)
          && Objects.equals(mach, other.mach)
          && Objects.equals(IYsym, other.IYsym)
          && Objects.equals(IZsym, other.IZsym)
          && Objects.equals(zsym, other.zsym)
          && Objects.equals(sref, other.sref)
          && Objects.equals(cref, other.cref)
          && Objects.equals(bref, other.bref)
          && Objects.equals(xref, other.xref)
          && Objects.equals(yref, other.yref)
          && Objects.equals(zref, other.zref)
          && Objects.equals(CD0ref, other.CD0ref);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          description, mach, IYsym, IZsym, zsym, sref, cref, bref, xref, yref, zref, CD0ref);
    }

    @Override
    public String toString() {
      return "AVLMainInputData{"
          + "description="
          + description
          + ", "
          + "mach="
          + mach
          + ", "
          + "IYsym="
          + IYsym
          + ", "
          + "IZsym="
          + IZsym
          + ", "
          + "zsym="
          + zsym
          + ", "
          + "sref="
          + sref
          + ", "
          + "cref="
          + cref
          + ", "
          + "bref="
          + bref
          + ", "
          + "xref="
          + xref
          + ", "
          + "yref="
          + yref
          + ", "
          + "zref="
          + zref
          + ", "
          + "CD0ref="
          + CD0ref
          + "}";
    }
  }

  private static final class Partial implements AVLMainInputData {
    private final String description;
    private final Double mach;
    private final Integer IYsym;
    private final Integer IZsym;
    private final Double zsym;
    private final Double sref;
    private final Double cref;
    private final Double bref;
    private final Double xref;
    private final Double yref;
    private final Double zref;
    private final Double CD0ref;
    private final EnumSet<AVLMainInputData_Builder.Property> _unsetProperties;

    Partial(AVLMainInputData_Builder builder) {
      this.description = builder.description;
      this.mach = builder.mach;
      this.IYsym = builder.IYsym;
      this.IZsym = builder.IZsym;
      this.zsym = builder.zsym;
      this.sref = builder.sref;
      this.cref = builder.cref;
      this.bref = builder.bref;
      this.xref = builder.xref;
      this.yref = builder.yref;
      this.zref = builder.zref;
      this.CD0ref = builder.CD0ref;
      this._unsetProperties = builder._unsetProperties.clone();
    }

    @Override
    public String getDescription() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.DESCRIPTION)) {
        throw new UnsupportedOperationException("description not set");
      }
      return description;
    }

    @Override
    public Double getMach() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.MACH)) {
        throw new UnsupportedOperationException("mach not set");
      }
      return mach;
    }

    @Override
    public Integer getIYsym() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.I_YSYM)) {
        throw new UnsupportedOperationException("IYsym not set");
      }
      return IYsym;
    }

    @Override
    public Integer getIZsym() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.I_ZSYM)) {
        throw new UnsupportedOperationException("IZsym not set");
      }
      return IZsym;
    }

    @Override
    public Double getZsym() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.ZSYM)) {
        throw new UnsupportedOperationException("zsym not set");
      }
      return zsym;
    }

    @Override
    public Double getSref() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.SREF)) {
        throw new UnsupportedOperationException("sref not set");
      }
      return sref;
    }

    @Override
    public Double getCref() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.CREF)) {
        throw new UnsupportedOperationException("cref not set");
      }
      return cref;
    }

    @Override
    public Double getBref() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.BREF)) {
        throw new UnsupportedOperationException("bref not set");
      }
      return bref;
    }

    @Override
    public Double getXref() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.XREF)) {
        throw new UnsupportedOperationException("xref not set");
      }
      return xref;
    }

    @Override
    public Double getYref() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.YREF)) {
        throw new UnsupportedOperationException("yref not set");
      }
      return yref;
    }

    @Override
    public Double getZref() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.ZREF)) {
        throw new UnsupportedOperationException("zref not set");
      }
      return zref;
    }

    @Override
    public Double getCD0ref() {
      if (_unsetProperties.contains(AVLMainInputData_Builder.Property.C_D0REF)) {
        throw new UnsupportedOperationException("CD0ref not set");
      }
      return CD0ref;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AVLMainInputData_Builder.Partial)) {
        return false;
      }
      AVLMainInputData_Builder.Partial other = (AVLMainInputData_Builder.Partial) obj;
      return Objects.equals(description, other.description)
          && Objects.equals(mach, other.mach)
          && Objects.equals(IYsym, other.IYsym)
          && Objects.equals(IZsym, other.IZsym)
          && Objects.equals(zsym, other.zsym)
          && Objects.equals(sref, other.sref)
          && Objects.equals(cref, other.cref)
          && Objects.equals(bref, other.bref)
          && Objects.equals(xref, other.xref)
          && Objects.equals(yref, other.yref)
          && Objects.equals(zref, other.zref)
          && Objects.equals(CD0ref, other.CD0ref)
          && Objects.equals(_unsetProperties, other._unsetProperties);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          description,
          mach,
          IYsym,
          IZsym,
          zsym,
          sref,
          cref,
          bref,
          xref,
          yref,
          zref,
          CD0ref,
          _unsetProperties);
    }

    @Override
    public String toString() {
      return "partial AVLMainInputData{"
          + COMMA_JOINER.join(
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.DESCRIPTION)
                  ? "description=" + description
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.MACH)
                  ? "mach=" + mach
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.I_YSYM)
                  ? "IYsym=" + IYsym
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.I_ZSYM)
                  ? "IZsym=" + IZsym
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.ZSYM)
                  ? "zsym=" + zsym
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.SREF)
                  ? "sref=" + sref
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.CREF)
                  ? "cref=" + cref
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.BREF)
                  ? "bref=" + bref
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.XREF)
                  ? "xref=" + xref
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.YREF)
                  ? "yref=" + yref
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.ZREF)
                  ? "zref=" + zref
                  : null),
              (!_unsetProperties.contains(AVLMainInputData_Builder.Property.C_D0REF)
                  ? "CD0ref=" + CD0ref
                  : null))
          + "}";
    }
  }
}
