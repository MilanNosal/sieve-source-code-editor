package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.MonitoringRole;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Source;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Type;

/**
 * Annotated code monitors changes in the source code of either the Java, or the
 * .sj files.
 * @author Milan Nosal
 */
public @interface DocumentChangeMonitoring {
    public Source monitoredSource();
    
    public Type typeOfEvents() default Type.GENERAL_CHANGE;
}
