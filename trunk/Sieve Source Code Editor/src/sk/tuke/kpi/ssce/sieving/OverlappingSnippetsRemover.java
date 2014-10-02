package sk.tuke.kpi.ssce.sieving;

import java.util.Collections;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.annotations.concerns.PostProcessing;
import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.sieving.interfaces.PostProcessingSiever;

/**
 * Removes overlapping code snippets, leaves just the root codes. Added by default.
 * @author Milan
 */
@PostProcessing(type = PostProcessingType.SIEVING)
public class OverlappingSnippetsRemover<T extends Concern> implements PostProcessingSiever<T> {

    @Override
    public JavaFile<T> process(JavaFile<T> file, BaseDocument doc) {
        List<CodeSnippet<T>> codes = file.getCodeSnippets();
        Collections.sort(codes);
        for (int i = 1; i < codes.size();) {
            if (codes.get(i - 1).getCodeBinding().getStartPositionJavaDocument() <= codes.get(i).getCodeBinding().getStartPositionJavaDocument()
                    && codes.get(i - 1).getCodeBinding().getEndPositionJavaDocument() >= codes.get(i).getCodeBinding().getStartPositionJavaDocument()) {
                codes.remove(i);
                continue;
            }
            i++;
        }
        return file;
    }
    
}
