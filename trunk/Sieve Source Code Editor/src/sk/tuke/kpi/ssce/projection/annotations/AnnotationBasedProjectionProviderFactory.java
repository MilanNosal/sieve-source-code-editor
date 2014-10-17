package sk.tuke.kpi.ssce.projection.annotations;

import org.netbeans.api.project.Project;
import org.openide.util.lookup.ServiceProvider;
import sk.tuke.kpi.ssce.annotations.concerns.AnnotationBasedProjections;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProvider;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProviderFactory;

/**
 *
 * @author Milan
 */
@AnnotationBasedProjections
@ServiceProvider(service = ProjectionProviderFactory.class)
public class AnnotationBasedProjectionProviderFactory
        implements ProjectionProviderFactory {

    @Override
    public String getDisplayName() {
        return "Annotation-based projections";
    }

    @Override
    public ProjectionProvider createProjectionProviderFor(Project projectContext) {
        return new AnnotationBasedProjectionProvider(projectContext);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
