package sk.tuke.kpi.ssce.projection.annotations;

import org.netbeans.api.project.Project;
import org.openide.util.lookup.ServiceProvider;
import sk.tuke.kpi.ssce.projection.provider.AbstractProjectionProvider;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProviderFactory;

/**
 *
 * @author Milan
 */
@ServiceProvider(service = ProjectionProviderFactory.class)
public class AnnotationBasedProjectionProviderFactory
        implements ProjectionProviderFactory {

    @Override
    public String getDisplayName() {
        return "Annotation-based projections";
    }

    @Override
    public AbstractProjectionProvider createProjectionProviderFor(Project projectContext) {
        return new AnnotationBasedProjectionProvider(projectContext);
    }

}
