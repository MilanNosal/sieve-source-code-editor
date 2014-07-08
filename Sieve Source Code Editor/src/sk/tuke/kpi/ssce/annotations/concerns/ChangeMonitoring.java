package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.Source;

/**
 * Annotated code monitors changes in the source code of either the Java, or the
 * .sj files.
 * @author Milan Nosal
 */
public @interface ChangeMonitoring {
    public Source monitoredSource();
}
