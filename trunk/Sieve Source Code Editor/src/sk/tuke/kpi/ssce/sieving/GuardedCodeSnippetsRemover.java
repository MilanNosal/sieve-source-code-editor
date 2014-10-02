package sk.tuke.kpi.ssce.sieving;

import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import sk.tuke.kpi.ssce.annotations.concerns.PostProcessing;
import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.sieving.interfaces.PostProcessingSiever;

/**
 * Added by default.
 * @author Milan
 */
@PostProcessing(type = PostProcessingType.SIEVING)
public class GuardedCodeSnippetsRemover<T extends Concern> implements PostProcessingSiever<T> {

    @Override
    public JavaFile<T> process(JavaFile<T> file, BaseDocument doc) {
        if (doc instanceof GuardedDocument) {
            List<CodeSnippet<T>> codes = file.getCodeSnippets();
            MarkBlock chain = ((GuardedDocument) doc).getGuardedBlockChain().getChain();

            MarkBlock blk = chain;
            while (blk != null) {
                for (int i = 0; i < codes.size();) {
                    if (codes.get(i).getCodeBinding().getStartPositionJavaDocument() <= blk.getEndOffset() && blk.getStartOffset() <= codes.get(i).getCodeBinding().getEndPositionJavaDocument()) {
                        codes.remove(i);
                        continue;
                    }
                    i++;
                }
                blk = blk.getNext();
            }
        }
        return file;
    }
}
