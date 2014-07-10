package sk.tuke.kpi.ssce.annotations.concerns;

/**
 *
 * @author Milan
 */
public @interface AvailableProjectionsChange {
    public boolean propagation() default false;
}
