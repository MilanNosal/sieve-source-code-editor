package sk.tuke.kpi.ssce.sieving.interfaces;

import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.annotations.concerns.PostProcessing;
import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;

/**
 * Provides additional sieving. The codeSnippets in the JavaFile can overlapp each other, the task
 * of this component is to remove decide which code snippets should stay and
 * which not. Can also destroy the JavaFile as well to decide that
 * none of the snippets should be included.
 * @author Milan
 */
@PostProcessing(type = PostProcessingType.SIEVING)
public interface PostProcessingSiever<T extends Concern> {
    public JavaFile<T> process(JavaFile<T> file, BaseDocument doc);
}
