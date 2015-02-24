package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.MonitoringRole;

/**
 * Design annotation for observer design pattern.
 * @author Milan
 */
public @interface Listening {
    
    public MonitoringRole[] monitoringRole() default {MonitoringRole.LISTENER, MonitoringRole.PUBLISHER};
}
