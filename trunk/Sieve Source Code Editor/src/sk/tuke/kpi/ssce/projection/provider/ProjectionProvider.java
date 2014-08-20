package sk.tuke.kpi.ssce.projection.provider;

import org.netbeans.api.project.Project;
import sk.tuke.kpi.ssce.annotations.concerns.ProvidersPluginSystem;
import sk.tuke.kpi.ssce.annotations.concerns.SSCE_UI;

/**
 * Projections will be done by the core and the implementation of
 * this interface, only this little communication will be needed (as far
 * as we can tell now).
 * @author Milan
 */
@SSCE_UI
@ProvidersPluginSystem
public interface ProjectionProvider {
    public Project getProjectContext();
    
    public void restart();
    
    public void destroy();
}
