package sk.tuke.kpi.ssce.sieving.interfaces;

import java.util.List;
import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProvider;

/**
 * Provides additional sieving. The codeSnippets in the JavaFile can overlapp each other, the task
 * of this component is to remove decide which code snippets should stay and
 * which not. Can also destroy the JavaFile as well to decide that
 * none of the snippets should be included.
 * @author Milan
 */
@SourceCodeSieving(postProcessing = true)
public interface PostProcessingSiever<T extends Concern> {
    public JavaFile<T> process(JavaFile<T> file, BaseDocument doc);
}
