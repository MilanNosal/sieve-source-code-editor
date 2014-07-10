package sk.tuke.kpi.ssce.annotations.concerns;

/**
 *
 * @author Milan
 */
public @interface SourceCodeSieving {
    public boolean postProcessing() default false;
}
