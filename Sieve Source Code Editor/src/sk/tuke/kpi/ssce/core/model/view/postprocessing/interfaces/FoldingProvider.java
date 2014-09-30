package sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces;

import java.util.List;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.FoldingRequest;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;

/**
 *
 * @author Milan
 */
public interface FoldingProvider {
    
    public void injectCurrentProjection(CurrentProjection<? extends Concern> projection);
    public void injectConcernExtractor(ConcernExtractor<? extends Concern> extractor);
    
    public List<FoldingRequest> createFolds(
            CodeSnippet<? extends Concern> codeSnippet);
    
    public List<FoldingRequest> createFolds(
            JavaFile<? extends Concern> javaFile);
    
    public List<FoldingRequest> createFolds(
            ViewModel<? extends Concern> viewModel);
}
