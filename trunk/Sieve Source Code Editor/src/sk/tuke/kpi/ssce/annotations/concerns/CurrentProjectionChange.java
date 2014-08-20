package sk.tuke.kpi.ssce.annotations.concerns;

/**
 *
 * @author Milan Nosal
 */
public @interface CurrentProjectionChange {
    public boolean propagation() default false;
}
