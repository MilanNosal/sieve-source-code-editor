package sk.tuke.kpi.ssce.annotations.concerns;

/**
 * Code that changes available projections.
 * @author Milan
 */
public @interface AvailableProjectionsChange {
    public boolean propagation() default false;
}
