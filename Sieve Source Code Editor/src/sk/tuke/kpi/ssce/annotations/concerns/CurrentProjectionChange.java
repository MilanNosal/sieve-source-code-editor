package sk.tuke.kpi.ssce.annotations.concerns;

/**
 * Code that changes currently used projection.
 * @author Milan Nosal
 */
public @interface CurrentProjectionChange {
    public boolean propagation() default false;
}
