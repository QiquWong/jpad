// Autogenerated code. Do not modify.
package standaloneutils.launchers.avl;

import aircraft.auxiliary.airfoil.Airfoil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javax.annotation.Generated;
import javax.annotation.Nullable;
import org.apache.commons.math3.linear.RealMatrix;
import standaloneutils.launchers.avl.AVLWingSection;
import standaloneutils.launchers.avl.AVLWingSectionControlSurface;

/**
 * Auto-generated superclass of {@link AVLWingSection.Builder},
 * derived from the API of {@link AVLWingSection}.
 */
@Generated("org.inferred.freebuilder.processor.CodeGenerator")
abstract class AVLWingSection_Builder {

  /**
   * Creates a new builder using {@code value} as a template.
   */
  public static AVLWingSection.Builder from(AVLWingSection value) {
    return new AVLWingSection.Builder().mergeFrom(value);
  }

  private static final Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();

  private enum Property {
    DESCRIPTION("description"),
    ORIGIN("origin"),
    CHORD("chord"),
    TWIST("twist"),
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
  private java.lang.Double[] origin;
  private Double chord;
  private Double twist;
  // Store a nullable object instead of an Optional. Escape analysis then
  // allows the JVM to optimize away the Optional objects created by and
  // passed to our API.
  private File airfoilCoordFile = null;
  // Store a nullable object instead of an Optional. Escape analysis then
  // allows the JVM to optimize away the Optional objects created by and
  // passed to our API.
  private Airfoil airfoilObject = null;
  // Store a nullable object instead of an Optional. Escape analysis then
  // allows the JVM to optimize away the Optional objects created by and
  // passed to our API.
  private RealMatrix airfoilSectionInline = null;
  private final ArrayList<AVLWingSectionControlSurface> controlSurfaces = new ArrayList<>();
  private final EnumSet<AVLWingSection_Builder.Property> _unsetProperties =
      EnumSet.allOf(AVLWingSection_Builder.Property.class);

  /**
   * Sets the value to be returned by {@link AVLWingSection#getDescription()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code description} is null
   */
  public AVLWingSection.Builder setDescription(String description) {
    this.description = Preconditions.checkNotNull(description);
    _unsetProperties.remove(AVLWingSection_Builder.Property.DESCRIPTION);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLWingSection#getDescription()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLWingSection.Builder mapDescription(UnaryOperator<String> mapper) {
    Preconditions.checkNotNull(mapper);
    return setDescription(mapper.apply(getDescription()));
  }

  /**
   * Returns the value that will be returned by {@link AVLWingSection#getDescription()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public String getDescription() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLWingSection_Builder.Property.DESCRIPTION),
        "description not set");
    return description;
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getOrigin()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code origin} is null
   */
  public AVLWingSection.Builder setOrigin(java.lang.Double[] origin) {
    this.origin = Preconditions.checkNotNull(origin);
    _unsetProperties.remove(AVLWingSection_Builder.Property.ORIGIN);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLWingSection#getOrigin()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLWingSection.Builder mapOrigin(UnaryOperator<java.lang.Double[]> mapper) {
    Preconditions.checkNotNull(mapper);
    return setOrigin(mapper.apply(getOrigin()));
  }

  /**
   * Returns the value that will be returned by {@link AVLWingSection#getOrigin()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public java.lang.Double[] getOrigin() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLWingSection_Builder.Property.ORIGIN), "origin not set");
    return origin;
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getChord()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code chord} is null
   */
  public AVLWingSection.Builder setChord(Double chord) {
    this.chord = Preconditions.checkNotNull(chord);
    _unsetProperties.remove(AVLWingSection_Builder.Property.CHORD);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLWingSection#getChord()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLWingSection.Builder mapChord(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setChord(mapper.apply(getChord()));
  }

  /**
   * Returns the value that will be returned by {@link AVLWingSection#getChord()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getChord() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLWingSection_Builder.Property.CHORD), "chord not set");
    return chord;
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getTwist()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code twist} is null
   */
  public AVLWingSection.Builder setTwist(Double twist) {
    this.twist = Preconditions.checkNotNull(twist);
    _unsetProperties.remove(AVLWingSection_Builder.Property.TWIST);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLWingSection#getTwist()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLWingSection.Builder mapTwist(UnaryOperator<Double> mapper) {
    Preconditions.checkNotNull(mapper);
    return setTwist(mapper.apply(getTwist()));
  }

  /**
   * Returns the value that will be returned by {@link AVLWingSection#getTwist()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public Double getTwist() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLWingSection_Builder.Property.TWIST), "twist not set");
    return twist;
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilCoordFile()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code airfoilCoordFile} is null
   */
  public AVLWingSection.Builder setAirfoilCoordFile(File airfoilCoordFile) {
    this.airfoilCoordFile = Preconditions.checkNotNull(airfoilCoordFile);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilCoordFile()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder setAirfoilCoordFile(Optional<? extends File> airfoilCoordFile) {
    if (airfoilCoordFile.isPresent()) {
      return setAirfoilCoordFile(airfoilCoordFile.get());
    } else {
      return clearAirfoilCoordFile();
    }
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilCoordFile()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder setNullableAirfoilCoordFile(@Nullable File airfoilCoordFile) {
    if (airfoilCoordFile != null) {
      return setAirfoilCoordFile(airfoilCoordFile);
    } else {
      return clearAirfoilCoordFile();
    }
  }

  /**
   * If the value to be returned by {@link AVLWingSection#getAirfoilCoordFile()} is present,
   * replaces it by applying {@code mapper} to it and using the result.
   *
   * <p>If the result is null, clears the value.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null
   */
  public AVLWingSection.Builder mapAirfoilCoordFile(UnaryOperator<File> mapper) {
    return setAirfoilCoordFile(getAirfoilCoordFile().map(mapper));
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilCoordFile()}
   * to {@link Optional#empty() Optional.empty()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder clearAirfoilCoordFile() {
    this.airfoilCoordFile = null;
    return (AVLWingSection.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link AVLWingSection#getAirfoilCoordFile()}.
   */
  public Optional<File> getAirfoilCoordFile() {
    return Optional.ofNullable(airfoilCoordFile);
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilObject()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code airfoilObject} is null
   */
  public AVLWingSection.Builder setAirfoilObject(Airfoil airfoilObject) {
    this.airfoilObject = Preconditions.checkNotNull(airfoilObject);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilObject()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder setAirfoilObject(Optional<? extends Airfoil> airfoilObject) {
    if (airfoilObject.isPresent()) {
      return setAirfoilObject(airfoilObject.get());
    } else {
      return clearAirfoilObject();
    }
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilObject()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder setNullableAirfoilObject(@Nullable Airfoil airfoilObject) {
    if (airfoilObject != null) {
      return setAirfoilObject(airfoilObject);
    } else {
      return clearAirfoilObject();
    }
  }

  /**
   * If the value to be returned by {@link AVLWingSection#getAirfoilObject()} is present,
   * replaces it by applying {@code mapper} to it and using the result.
   *
   * <p>If the result is null, clears the value.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null
   */
  public AVLWingSection.Builder mapAirfoilObject(UnaryOperator<Airfoil> mapper) {
    return setAirfoilObject(getAirfoilObject().map(mapper));
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilObject()}
   * to {@link Optional#empty() Optional.empty()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder clearAirfoilObject() {
    this.airfoilObject = null;
    return (AVLWingSection.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link AVLWingSection#getAirfoilObject()}.
   */
  public Optional<Airfoil> getAirfoilObject() {
    return Optional.ofNullable(airfoilObject);
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilSectionInline()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code airfoilSectionInline} is null
   */
  public AVLWingSection.Builder setAirfoilSectionInline(RealMatrix airfoilSectionInline) {
    this.airfoilSectionInline = Preconditions.checkNotNull(airfoilSectionInline);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilSectionInline()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder setAirfoilSectionInline(
      Optional<? extends RealMatrix> airfoilSectionInline) {
    if (airfoilSectionInline.isPresent()) {
      return setAirfoilSectionInline(airfoilSectionInline.get());
    } else {
      return clearAirfoilSectionInline();
    }
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilSectionInline()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder setNullableAirfoilSectionInline(
      @Nullable RealMatrix airfoilSectionInline) {
    if (airfoilSectionInline != null) {
      return setAirfoilSectionInline(airfoilSectionInline);
    } else {
      return clearAirfoilSectionInline();
    }
  }

  /**
   * If the value to be returned by {@link AVLWingSection#getAirfoilSectionInline()} is present,
   * replaces it by applying {@code mapper} to it and using the result.
   *
   * <p>If the result is null, clears the value.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null
   */
  public AVLWingSection.Builder mapAirfoilSectionInline(UnaryOperator<RealMatrix> mapper) {
    return setAirfoilSectionInline(getAirfoilSectionInline().map(mapper));
  }

  /**
   * Sets the value to be returned by {@link AVLWingSection#getAirfoilSectionInline()}
   * to {@link Optional#empty() Optional.empty()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder clearAirfoilSectionInline() {
    this.airfoilSectionInline = null;
    return (AVLWingSection.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link AVLWingSection#getAirfoilSectionInline()}.
   */
  public Optional<RealMatrix> getAirfoilSectionInline() {
    return Optional.ofNullable(airfoilSectionInline);
  }

  /**
   * Adds {@code element} to the list to be returned from {@link AVLWingSection#getControlSurfaces()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code element} is null
   */
  public AVLWingSection.Builder addControlSurfaces(AVLWingSectionControlSurface element) {
    this.controlSurfaces.add(Preconditions.checkNotNull(element));
    return (AVLWingSection.Builder) this;
  }

  /**
   * Adds each element of {@code elements} to the list to be returned from
   * {@link AVLWingSection#getControlSurfaces()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code elements} is null or contains a
   *     null element
   */
  public AVLWingSection.Builder addControlSurfaces(AVLWingSectionControlSurface... elements) {
    controlSurfaces.ensureCapacity(controlSurfaces.size() + elements.length);
    for (AVLWingSectionControlSurface element : elements) {
      addControlSurfaces(element);
    }
    return (AVLWingSection.Builder) this;
  }

  /**
   * Adds each element of {@code elements} to the list to be returned from
   * {@link AVLWingSection#getControlSurfaces()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code elements} is null or contains a
   *     null element
   */
  public AVLWingSection.Builder addAllControlSurfaces(
      Iterable<? extends AVLWingSectionControlSurface> elements) {
    if (elements instanceof Collection) {
      controlSurfaces.ensureCapacity(controlSurfaces.size() + ((Collection<?>) elements).size());
    }
    for (AVLWingSectionControlSurface element : elements) {
      addControlSurfaces(element);
    }
    return (AVLWingSection.Builder) this;
  }

  /**
   * Applies {@code mutator} to the list to be returned from {@link AVLWingSection#getControlSurfaces()}.
   *
   * <p>This method mutates the list in-place. {@code mutator} is a void
   * consumer, so any value returned from a lambda will be ignored. Take care
   * not to call pure functions, like {@link Collection#stream()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mutator} is null
   */
  public AVLWingSection.Builder mutateControlSurfaces(
      Consumer<? super List<AVLWingSectionControlSurface>> mutator) {
    // If addControlSurfaces is overridden, this method will be updated to delegate to it
    mutator.accept(controlSurfaces);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Clears the list to be returned from {@link AVLWingSection#getControlSurfaces()}.
   *
   * @return this {@code Builder} object
   */
  public AVLWingSection.Builder clearControlSurfaces() {
    this.controlSurfaces.clear();
    return (AVLWingSection.Builder) this;
  }

  /**
   * Returns an unmodifiable view of the list that will be returned by
   * {@link AVLWingSection#getControlSurfaces()}.
   * Changes to this builder will be reflected in the view.
   */
  public List<AVLWingSectionControlSurface> getControlSurfaces() {
    return Collections.unmodifiableList(controlSurfaces);
  }

  /**
   * Sets all property values using the given {@code AVLWingSection} as a template.
   */
  public AVLWingSection.Builder mergeFrom(AVLWingSection value) {
    AVLWingSection_Builder _defaults = new AVLWingSection.Builder();
    if (_defaults._unsetProperties.contains(AVLWingSection_Builder.Property.DESCRIPTION)
        || !value.getDescription().equals(_defaults.getDescription())) {
      setDescription(value.getDescription());
    }
    if (_defaults._unsetProperties.contains(AVLWingSection_Builder.Property.ORIGIN)
        || !value.getOrigin().equals(_defaults.getOrigin())) {
      setOrigin(value.getOrigin());
    }
    if (_defaults._unsetProperties.contains(AVLWingSection_Builder.Property.CHORD)
        || !value.getChord().equals(_defaults.getChord())) {
      setChord(value.getChord());
    }
    if (_defaults._unsetProperties.contains(AVLWingSection_Builder.Property.TWIST)
        || !value.getTwist().equals(_defaults.getTwist())) {
      setTwist(value.getTwist());
    }
    value.getAirfoilCoordFile().ifPresent(this::setAirfoilCoordFile);
    value.getAirfoilObject().ifPresent(this::setAirfoilObject);
    value.getAirfoilSectionInline().ifPresent(this::setAirfoilSectionInline);
    addAllControlSurfaces(value.getControlSurfaces());
    return (AVLWingSection.Builder) this;
  }

  /**
   * Copies values from the given {@code Builder}.
   * Does not affect any properties not set on the input.
   */
  public AVLWingSection.Builder mergeFrom(AVLWingSection.Builder template) {
    // Upcast to access private fields; otherwise, oddly, we get an access violation.
    AVLWingSection_Builder base = (AVLWingSection_Builder) template;
    AVLWingSection_Builder _defaults = new AVLWingSection.Builder();
    if (!base._unsetProperties.contains(AVLWingSection_Builder.Property.DESCRIPTION)
        && (_defaults._unsetProperties.contains(AVLWingSection_Builder.Property.DESCRIPTION)
            || !template.getDescription().equals(_defaults.getDescription()))) {
      setDescription(template.getDescription());
    }
    if (!base._unsetProperties.contains(AVLWingSection_Builder.Property.ORIGIN)
        && (_defaults._unsetProperties.contains(AVLWingSection_Builder.Property.ORIGIN)
            || !template.getOrigin().equals(_defaults.getOrigin()))) {
      setOrigin(template.getOrigin());
    }
    if (!base._unsetProperties.contains(AVLWingSection_Builder.Property.CHORD)
        && (_defaults._unsetProperties.contains(AVLWingSection_Builder.Property.CHORD)
            || !template.getChord().equals(_defaults.getChord()))) {
      setChord(template.getChord());
    }
    if (!base._unsetProperties.contains(AVLWingSection_Builder.Property.TWIST)
        && (_defaults._unsetProperties.contains(AVLWingSection_Builder.Property.TWIST)
            || !template.getTwist().equals(_defaults.getTwist()))) {
      setTwist(template.getTwist());
    }
    template.getAirfoilCoordFile().ifPresent(this::setAirfoilCoordFile);
    template.getAirfoilObject().ifPresent(this::setAirfoilObject);
    template.getAirfoilSectionInline().ifPresent(this::setAirfoilSectionInline);
    addAllControlSurfaces(((AVLWingSection_Builder) template).controlSurfaces);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Resets the state of this builder.
   */
  public AVLWingSection.Builder clear() {
    AVLWingSection_Builder _defaults = new AVLWingSection.Builder();
    description = _defaults.description;
    origin = _defaults.origin;
    chord = _defaults.chord;
    twist = _defaults.twist;
    airfoilCoordFile = _defaults.airfoilCoordFile;
    airfoilObject = _defaults.airfoilObject;
    airfoilSectionInline = _defaults.airfoilSectionInline;
    controlSurfaces.clear();
    _unsetProperties.clear();
    _unsetProperties.addAll(_defaults._unsetProperties);
    return (AVLWingSection.Builder) this;
  }

  /**
   * Returns a newly-created {@link AVLWingSection} based on the contents of the {@code Builder}.
   *
   * @throws IllegalStateException if any field has not been set
   */
  public AVLWingSection build() {
    Preconditions.checkState(_unsetProperties.isEmpty(), "Not set: %s", _unsetProperties);
    return new AVLWingSection_Builder.Value(this);
  }

  /**
   * Returns a newly-created partial {@link AVLWingSection}
   * based on the contents of the {@code Builder}.
   * State checking will not be performed.
   * Unset properties will throw an {@link UnsupportedOperationException}
   * when accessed via the partial object.
   *
   * <p>Partials should only ever be used in tests.
   */
  @VisibleForTesting()
  public AVLWingSection buildPartial() {
    return new AVLWingSection_Builder.Partial(this);
  }

  private static final class Value implements AVLWingSection {
    private final String description;
    private final java.lang.Double[] origin;
    private final Double chord;
    private final Double twist;
    // Store a nullable object instead of an Optional. Escape analysis then
    // allows the JVM to optimize away the Optional objects created by our
    // getter method.
    private final File airfoilCoordFile;
    // Store a nullable object instead of an Optional. Escape analysis then
    // allows the JVM to optimize away the Optional objects created by our
    // getter method.
    private final Airfoil airfoilObject;
    // Store a nullable object instead of an Optional. Escape analysis then
    // allows the JVM to optimize away the Optional objects created by our
    // getter method.
    private final RealMatrix airfoilSectionInline;
    private final List<AVLWingSectionControlSurface> controlSurfaces;

    private Value(AVLWingSection_Builder builder) {
      this.description = builder.description;
      this.origin = builder.origin;
      this.chord = builder.chord;
      this.twist = builder.twist;
      this.airfoilCoordFile = builder.airfoilCoordFile;
      this.airfoilObject = builder.airfoilObject;
      this.airfoilSectionInline = builder.airfoilSectionInline;
      this.controlSurfaces = ImmutableList.copyOf(builder.controlSurfaces);
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public java.lang.Double[] getOrigin() {
      return origin;
    }

    @Override
    public Double getChord() {
      return chord;
    }

    @Override
    public Double getTwist() {
      return twist;
    }

    @Override
    public Optional<File> getAirfoilCoordFile() {
      return Optional.ofNullable(airfoilCoordFile);
    }

    @Override
    public Optional<Airfoil> getAirfoilObject() {
      return Optional.ofNullable(airfoilObject);
    }

    @Override
    public Optional<RealMatrix> getAirfoilSectionInline() {
      return Optional.ofNullable(airfoilSectionInline);
    }

    @Override
    public List<AVLWingSectionControlSurface> getControlSurfaces() {
      return controlSurfaces;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AVLWingSection_Builder.Value)) {
        return false;
      }
      AVLWingSection_Builder.Value other = (AVLWingSection_Builder.Value) obj;
      return Objects.equals(description, other.description)
          && Objects.equals(origin, other.origin)
          && Objects.equals(chord, other.chord)
          && Objects.equals(twist, other.twist)
          && Objects.equals(airfoilCoordFile, other.airfoilCoordFile)
          && Objects.equals(airfoilObject, other.airfoilObject)
          && Objects.equals(airfoilSectionInline, other.airfoilSectionInline)
          && Objects.equals(controlSurfaces, other.controlSurfaces);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          description,
          origin,
          chord,
          twist,
          airfoilCoordFile,
          airfoilObject,
          airfoilSectionInline,
          controlSurfaces);
    }

    @Override
    public String toString() {
      return "AVLWingSection{"
          + COMMA_JOINER.join(
              "description=" + description,
              "origin=" + origin,
              "chord=" + chord,
              "twist=" + twist,
              (airfoilCoordFile != null ? "airfoilCoordFile=" + airfoilCoordFile : null),
              (airfoilObject != null ? "airfoilObject=" + airfoilObject : null),
              (airfoilSectionInline != null
                  ? "airfoilSectionInline=" + airfoilSectionInline
                  : null),
              "controlSurfaces=" + controlSurfaces)
          + "}";
    }
  }

  private static final class Partial implements AVLWingSection {
    private final String description;
    private final java.lang.Double[] origin;
    private final Double chord;
    private final Double twist;
    // Store a nullable object instead of an Optional. Escape analysis then
    // allows the JVM to optimize away the Optional objects created by our
    // getter method.
    private final File airfoilCoordFile;
    // Store a nullable object instead of an Optional. Escape analysis then
    // allows the JVM to optimize away the Optional objects created by our
    // getter method.
    private final Airfoil airfoilObject;
    // Store a nullable object instead of an Optional. Escape analysis then
    // allows the JVM to optimize away the Optional objects created by our
    // getter method.
    private final RealMatrix airfoilSectionInline;
    private final List<AVLWingSectionControlSurface> controlSurfaces;
    private final EnumSet<AVLWingSection_Builder.Property> _unsetProperties;

    Partial(AVLWingSection_Builder builder) {
      this.description = builder.description;
      this.origin = builder.origin;
      this.chord = builder.chord;
      this.twist = builder.twist;
      this.airfoilCoordFile = builder.airfoilCoordFile;
      this.airfoilObject = builder.airfoilObject;
      this.airfoilSectionInline = builder.airfoilSectionInline;
      this.controlSurfaces = ImmutableList.copyOf(builder.controlSurfaces);
      this._unsetProperties = builder._unsetProperties.clone();
    }

    @Override
    public String getDescription() {
      if (_unsetProperties.contains(AVLWingSection_Builder.Property.DESCRIPTION)) {
        throw new UnsupportedOperationException("description not set");
      }
      return description;
    }

    @Override
    public java.lang.Double[] getOrigin() {
      if (_unsetProperties.contains(AVLWingSection_Builder.Property.ORIGIN)) {
        throw new UnsupportedOperationException("origin not set");
      }
      return origin;
    }

    @Override
    public Double getChord() {
      if (_unsetProperties.contains(AVLWingSection_Builder.Property.CHORD)) {
        throw new UnsupportedOperationException("chord not set");
      }
      return chord;
    }

    @Override
    public Double getTwist() {
      if (_unsetProperties.contains(AVLWingSection_Builder.Property.TWIST)) {
        throw new UnsupportedOperationException("twist not set");
      }
      return twist;
    }

    @Override
    public Optional<File> getAirfoilCoordFile() {
      return Optional.ofNullable(airfoilCoordFile);
    }

    @Override
    public Optional<Airfoil> getAirfoilObject() {
      return Optional.ofNullable(airfoilObject);
    }

    @Override
    public Optional<RealMatrix> getAirfoilSectionInline() {
      return Optional.ofNullable(airfoilSectionInline);
    }

    @Override
    public List<AVLWingSectionControlSurface> getControlSurfaces() {
      return controlSurfaces;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AVLWingSection_Builder.Partial)) {
        return false;
      }
      AVLWingSection_Builder.Partial other = (AVLWingSection_Builder.Partial) obj;
      return Objects.equals(description, other.description)
          && Objects.equals(origin, other.origin)
          && Objects.equals(chord, other.chord)
          && Objects.equals(twist, other.twist)
          && Objects.equals(airfoilCoordFile, other.airfoilCoordFile)
          && Objects.equals(airfoilObject, other.airfoilObject)
          && Objects.equals(airfoilSectionInline, other.airfoilSectionInline)
          && Objects.equals(controlSurfaces, other.controlSurfaces)
          && Objects.equals(_unsetProperties, other._unsetProperties);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          description,
          origin,
          chord,
          twist,
          airfoilCoordFile,
          airfoilObject,
          airfoilSectionInline,
          controlSurfaces,
          _unsetProperties);
    }

    @Override
    public String toString() {
      return "partial AVLWingSection{"
          + COMMA_JOINER.join(
              (!_unsetProperties.contains(AVLWingSection_Builder.Property.DESCRIPTION)
                  ? "description=" + description
                  : null),
              (!_unsetProperties.contains(AVLWingSection_Builder.Property.ORIGIN)
                  ? "origin=" + origin
                  : null),
              (!_unsetProperties.contains(AVLWingSection_Builder.Property.CHORD)
                  ? "chord=" + chord
                  : null),
              (!_unsetProperties.contains(AVLWingSection_Builder.Property.TWIST)
                  ? "twist=" + twist
                  : null),
              (airfoilCoordFile != null ? "airfoilCoordFile=" + airfoilCoordFile : null),
              (airfoilObject != null ? "airfoilObject=" + airfoilObject : null),
              (airfoilSectionInline != null
                  ? "airfoilSectionInline=" + airfoilSectionInline
                  : null),
              "controlSurfaces=" + controlSurfaces)
          + "}";
    }
  }
}
