package sk.tuke.kpi.ssce.annotations.concerns;

/**
 *
 * @author Milan Nosal
 */
public @interface ProjectionConfigurationChange {
    public boolean propagation() default false;
}
