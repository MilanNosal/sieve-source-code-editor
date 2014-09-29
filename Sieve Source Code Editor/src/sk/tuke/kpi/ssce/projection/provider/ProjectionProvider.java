package sk.tuke.kpi.ssce.projection.provider;

import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import sk.tuke.kpi.ssce.annotations.concerns.Disposal;
import sk.tuke.kpi.ssce.annotations.concerns.ProvidersPluginSystem;
import sk.tuke.kpi.ssce.annotations.concerns.SSCE_UI;
import sk.tuke.kpi.ssce.core.SSCEditorCore;

/**
 * Projections will be done by the core and the implementation of
 * this interface, only this little communication will be needed (as far
 * as we can tell now).
 * @author Milan
 */
@SSCE_UI
@ProvidersPluginSystem
public interface ProjectionProvider {
    /**
     * View of the provider that is used to manage projections.
     * @return 
     */
    public JPanel getView();
    
    public Project getProjectContext();
    
    public SSCEditorCore getSSCECore();
    
    /**
     * Each projection provider is required to implement a dispose method that
     * invalidates the projectional editing of a single project, the dispose
     * method HAS TO call the dispose method of the core. It has to 
     * use the dispose() method of the SSCEditorCore and to take care of the 
     * view invalidation.
     */
    @Disposal
    public void dispose();
}
