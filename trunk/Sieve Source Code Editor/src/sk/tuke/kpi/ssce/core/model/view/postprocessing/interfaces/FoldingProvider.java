package sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces;

import java.util.List;
import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.FoldingRequest;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.FoldingRequest;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;

/**
 *
 * @author Milan
 */
public interface FoldingProvider {
    
    public void injectCurrentProjection(CurrentProjection projection);
    public void injectConcernExtractor(ConcernExtractor extractor);
    
    public List<FoldingRequest> createFolds(
            CodeSnippet codeSnippet);
    
    public List<FoldingRequest> createFolds(
            JavaFile javaFile);
    
    public List<FoldingRequest> createFolds(
            ViewModel viewModel);
}