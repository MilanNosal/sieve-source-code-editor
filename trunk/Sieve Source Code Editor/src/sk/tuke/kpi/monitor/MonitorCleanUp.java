package sk.tuke.kpi.monitor;

import org.openide.modules.ModuleInstall;

public class MonitorCleanUp extends ModuleInstall {
    @Override
    public boolean closing() {
        for (RunInteractionMonitor rim : RunInteractionMonitor.getInstances()) {
            rim.endLogging();
        }
        return super.closing();
    }
}
