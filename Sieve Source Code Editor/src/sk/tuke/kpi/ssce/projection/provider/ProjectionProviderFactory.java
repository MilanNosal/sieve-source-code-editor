package sk.tuke.kpi.ssce.projection.provider;

import org.netbeans.api.project.Project;
import sk.tuke.kpi.ssce.annotations.concerns.ProvidersPluginSystem;
import sk.tuke.kpi.ssce.annotations.concerns.SSCE_UI;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;

/**
 * Public interface used for registering a new projections implementation.
 * Based on Netbeans OpenIDE Lookup API.
 * @author Milan
 * @param <T>
 */
@SSCE_UI
@ProvidersPluginSystem
public interface ProjectionProviderFactory<T extends Concern> {
    
    public String getDisplayName();
    
    /**
     * Using abstract projection provider since it extends JPanel class and thus
     * enables adding it to the gui.
     * @param projectContext
     * @return 
     */
    public ProjectionProvider<T> createProjectionProviderFor(Project projectContext);
}
