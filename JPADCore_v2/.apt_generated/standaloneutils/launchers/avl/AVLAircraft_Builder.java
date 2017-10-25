// Autogenerated code. Do not modify.
package standaloneutils.launchers.avl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javax.annotation.Generated;
import standaloneutils.launchers.avl.AVLAircraft;
import standaloneutils.launchers.avl.AVLBody;
import standaloneutils.launchers.avl.AVLWing;

/**
 * Auto-generated superclass of {@link AVLAircraft.Builder},
 * derived from the API of {@link AVLAircraft}.
 */
@Generated("org.inferred.freebuilder.processor.CodeGenerator")
abstract class AVLAircraft_Builder {

  /**
   * Creates a new builder using {@code value} as a template.
   */
  public static AVLAircraft.Builder from(AVLAircraft value) {
    return new AVLAircraft.Builder().mergeFrom(value);
  }

  private static final Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();

  private enum Property {
    DESCRIPTION("description"),
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
  private final ArrayList<AVLWing> wings = new ArrayList<>();
  private final ArrayList<AVLBody> bodies = new ArrayList<>();
  private final EnumSet<AVLAircraft_Builder.Property> _unsetProperties =
      EnumSet.allOf(AVLAircraft_Builder.Property.class);

  /**
   * Sets the value to be returned by {@link AVLAircraft#getDescription()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code description} is null
   */
  public AVLAircraft.Builder setDescription(String description) {
    this.description = Preconditions.checkNotNull(description);
    _unsetProperties.remove(AVLAircraft_Builder.Property.DESCRIPTION);
    return (AVLAircraft.Builder) this;
  }

  /**
   * Replaces the value to be returned by {@link AVLAircraft#getDescription()}
   * by applying {@code mapper} to it and using the result.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mapper} is null or returns null
   * @throws IllegalStateException if the field has not been set
   */
  public AVLAircraft.Builder mapDescription(UnaryOperator<String> mapper) {
    Preconditions.checkNotNull(mapper);
    return setDescription(mapper.apply(getDescription()));
  }

  /**
   * Returns the value that will be returned by {@link AVLAircraft#getDescription()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public String getDescription() {
    Preconditions.checkState(
        !_unsetProperties.contains(AVLAircraft_Builder.Property.DESCRIPTION),
        "description not set");
    return description;
  }

  /**
   * Adds {@code element} to the list to be returned from {@link AVLAircraft#getWings()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code element} is null
   */
  public AVLAircraft.Builder addWings(AVLWing element) {
    this.wings.add(Preconditions.checkNotNull(element));
    return (AVLAircraft.Builder) this;
  }

  /**
   * Adds each element of {@code elements} to the list to be returned from
   * {@link AVLAircraft#getWings()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code elements} is null or contains a
   *     null element
   */
  public AVLAircraft.Builder addWings(AVLWing... elements) {
    wings.ensureCapacity(wings.size() + elements.length);
    for (AVLWing element : elements) {
      addWings(element);
    }
    return (AVLAircraft.Builder) this;
  }

  /**
   * Adds each element of {@code elements} to the list to be returned from
   * {@link AVLAircraft#getWings()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code elements} is null or contains a
   *     null element
   */
  public AVLAircraft.Builder addAllWings(Iterable<? extends AVLWing> elements) {
    if (elements instanceof Collection) {
      wings.ensureCapacity(wings.size() + ((Collection<?>) elements).size());
    }
    for (AVLWing element : elements) {
      addWings(element);
    }
    return (AVLAircraft.Builder) this;
  }

  /**
   * Applies {@code mutator} to the list to be returned from {@link AVLAircraft#getWings()}.
   *
   * <p>This method mutates the list in-place. {@code mutator} is a void
   * consumer, so any value returned from a lambda will be ignored. Take care
   * not to call pure functions, like {@link Collection#stream()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mutator} is null
   */
  public AVLAircraft.Builder mutateWings(Consumer<? super List<AVLWing>> mutator) {
    // If addWings is overridden, this method will be updated to delegate to it
    mutator.accept(wings);
    return (AVLAircraft.Builder) this;
  }

  /**
   * Clears the list to be returned from {@link AVLAircraft#getWings()}.
   *
   * @return this {@code Builder} object
   */
  public AVLAircraft.Builder clearWings() {
    this.wings.clear();
    return (AVLAircraft.Builder) this;
  }

  /**
   * Returns an unmodifiable view of the list that will be returned by
   * {@link AVLAircraft#getWings()}.
   * Changes to this builder will be reflected in the view.
   */
  public List<AVLWing> getWings() {
    return Collections.unmodifiableList(wings);
  }

  /**
   * Adds {@code element} to the list to be returned from {@link AVLAircraft#getBodies()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code element} is null
   */
  public AVLAircraft.Builder addBodies(AVLBody element) {
    this.bodies.add(Preconditions.checkNotNull(element));
    return (AVLAircraft.Builder) this;
  }

  /**
   * Adds each element of {@code elements} to the list to be returned from
   * {@link AVLAircraft#getBodies()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code elements} is null or contains a
   *     null element
   */
  public AVLAircraft.Builder addBodies(AVLBody... elements) {
    bodies.ensureCapacity(bodies.size() + elements.length);
    for (AVLBody element : elements) {
      addBodies(element);
    }
    return (AVLAircraft.Builder) this;
  }

  /**
   * Adds each element of {@code elements} to the list to be returned from
   * {@link AVLAircraft#getBodies()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code elements} is null or contains a
   *     null element
   */
  public AVLAircraft.Builder addAllBodies(Iterable<? extends AVLBody> elements) {
    if (elements instanceof Collection) {
      bodies.ensureCapacity(bodies.size() + ((Collection<?>) elements).size());
    }
    for (AVLBody element : elements) {
      addBodies(element);
    }
    return (AVLAircraft.Builder) this;
  }

  /**
   * Applies {@code mutator} to the list to be returned from {@link AVLAircraft#getBodies()}.
   *
   * <p>This method mutates the list in-place. {@code mutator} is a void
   * consumer, so any value returned from a lambda will be ignored. Take care
   * not to call pure functions, like {@link Collection#stream()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code mutator} is null
   */
  public AVLAircraft.Builder mutateBodies(Consumer<? super List<AVLBody>> mutator) {
    // If addBodies is overridden, this method will be updated to delegate to it
    mutator.accept(bodies);
    return (AVLAircraft.Builder) this;
  }

  /**
   * Clears the list to be returned from {@link AVLAircraft#getBodies()}.
   *
   * @return this {@code Builder} object
   */
  public AVLAircraft.Builder clearBodies() {
    this.bodies.clear();
    return (AVLAircraft.Builder) this;
  }

  /**
   * Returns an unmodifiable view of the list that will be returned by
   * {@link AVLAircraft#getBodies()}.
   * Changes to this builder will be reflected in the view.
   */
  public List<AVLBody> getBodies() {
    return Collections.unmodifiableList(bodies);
  }

  /**
   * Sets all property values using the given {@code AVLAircraft} as a template.
   */
  public AVLAircraft.Builder mergeFrom(AVLAircraft value) {
    AVLAircraft_Builder _defaults = new AVLAircraft.Builder();
    if (_defaults._unsetProperties.contains(AVLAircraft_Builder.Property.DESCRIPTION)
        || !value.getDescription().equals(_defaults.getDescription())) {
      setDescription(value.getDescription());
    }
    addAllWings(value.getWings());
    addAllBodies(value.getBodies());
    return (AVLAircraft.Builder) this;
  }

  /**
   * Copies values from the given {@code Builder}.
   * Does not affect any properties not set on the input.
   */
  public AVLAircraft.Builder mergeFrom(AVLAircraft.Builder template) {
    // Upcast to access private fields; otherwise, oddly, we get an access violation.
    AVLAircraft_Builder base = (AVLAircraft_Builder) template;
    AVLAircraft_Builder _defaults = new AVLAircraft.Builder();
    if (!base._unsetProperties.contains(AVLAircraft_Builder.Property.DESCRIPTION)
        && (_defaults._unsetProperties.contains(AVLAircraft_Builder.Property.DESCRIPTION)
            || !template.getDescription().equals(_defaults.getDescription()))) {
      setDescription(template.getDescription());
    }
    addAllWings(((AVLAircraft_Builder) template).wings);
    addAllBodies(((AVLAircraft_Builder) template).bodies);
    return (AVLAircraft.Builder) this;
  }

  /**
   * Resets the state of this builder.
   */
  public AVLAircraft.Builder clear() {
    AVLAircraft_Builder _defaults = new AVLAircraft.Builder();
    description = _defaults.description;
    wings.clear();
    bodies.clear();
    _unsetProperties.clear();
    _unsetProperties.addAll(_defaults._unsetProperties);
    return (AVLAircraft.Builder) this;
  }

  /**
   * Returns a newly-created {@link AVLAircraft} based on the contents of the {@code Builder}.
   *
   * @throws IllegalStateException if any field has not been set
   */
  public AVLAircraft build() {
    Preconditions.checkState(_unsetProperties.isEmpty(), "Not set: %s", _unsetProperties);
    return new AVLAircraft_Builder.Value(this);
  }

  /**
   * Returns a newly-created partial {@link AVLAircraft}
   * based on the contents of the {@code Builder}.
   * State checking will not be performed.
   * Unset properties will throw an {@link UnsupportedOperationException}
   * when accessed via the partial object.
   *
   * <p>Partials should only ever be used in tests.
   */
  @VisibleForTesting()
  public AVLAircraft buildPartial() {
    return new AVLAircraft_Builder.Partial(this);
  }

  private static final class Value implements AVLAircraft {
    private final String description;
    private final List<AVLWing> wings;
    private final List<AVLBody> bodies;

    private Value(AVLAircraft_Builder builder) {
      this.description = builder.description;
      this.wings = ImmutableList.copyOf(builder.wings);
      this.bodies = ImmutableList.copyOf(builder.bodies);
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public List<AVLWing> getWings() {
      return wings;
    }

    @Override
    public List<AVLBody> getBodies() {
      return bodies;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AVLAircraft_Builder.Value)) {
        return false;
      }
      AVLAircraft_Builder.Value other = (AVLAircraft_Builder.Value) obj;
      return Objects.equals(description, other.description)
          && Objects.equals(wings, other.wings)
          && Objects.equals(bodies, other.bodies);
    }

    @Override
    public int hashCode() {
      return Objects.hash(description, wings, bodies);
    }

    @Override
    public String toString() {
      return "AVLAircraft{"
          + "description="
          + description
          + ", "
          + "wings="
          + wings
          + ", "
          + "bodies="
          + bodies
          + "}";
    }
  }

  private static final class Partial implements AVLAircraft {
    private final String description;
    private final List<AVLWing> wings;
    private final List<AVLBody> bodies;
    private final EnumSet<AVLAircraft_Builder.Property> _unsetProperties;

    Partial(AVLAircraft_Builder builder) {
      this.description = builder.description;
      this.wings = ImmutableList.copyOf(builder.wings);
      this.bodies = ImmutableList.copyOf(builder.bodies);
      this._unsetProperties = builder._unsetProperties.clone();
    }

    @Override
    public String getDescription() {
      if (_unsetProperties.contains(AVLAircraft_Builder.Property.DESCRIPTION)) {
        throw new UnsupportedOperationException("description not set");
      }
      return description;
    }

    @Override
    public List<AVLWing> getWings() {
      return wings;
    }

    @Override
    public List<AVLBody> getBodies() {
      return bodies;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AVLAircraft_Builder.Partial)) {
        return false;
      }
      AVLAircraft_Builder.Partial other = (AVLAircraft_Builder.Partial) obj;
      return Objects.equals(description, other.description)
          && Objects.equals(wings, other.wings)
          && Objects.equals(bodies, other.bodies)
          && Objects.equals(_unsetProperties, other._unsetProperties);
    }

    @Override
    public int hashCode() {
      return Objects.hash(description, wings, bodies, _unsetProperties);
    }

    @Override
    public String toString() {
      return "partial AVLAircraft{"
          + COMMA_JOINER.join(
              (!_unsetProperties.contains(AVLAircraft_Builder.Property.DESCRIPTION)
                  ? "description=" + description
                  : null),
              "wings=" + wings,
              "bodies=" + bodies)
          + "}";
    }
  }
}
