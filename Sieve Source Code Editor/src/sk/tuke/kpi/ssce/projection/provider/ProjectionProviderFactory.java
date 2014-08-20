package sk.tuke.kpi.ssce.projection.provider;

import org.netbeans.api.project.Project;
import sk.tuke.kpi.ssce.annotations.concerns.ProvidersPluginSystem;
import sk.tuke.kpi.ssce.annotations.concerns.SSCE_UI;

/**
 * Public interface used for registering a new projections implementation.
 * Based on Netbeans OpenIDE Lookup API.
 * @author Milan
 */
@SSCE_UI
@ProvidersPluginSystem
public interface ProjectionProviderFactory {
    
    public String getDisplayName();
    
    /**
     * Using abstract projection provider since it extends JPanel class and thus
     * enables adding it to the gui.
     * @param projectContext
     * @return 
     */
    public AbstractProjectionProvider createProjectionProviderFor(Project projectContext);
}
