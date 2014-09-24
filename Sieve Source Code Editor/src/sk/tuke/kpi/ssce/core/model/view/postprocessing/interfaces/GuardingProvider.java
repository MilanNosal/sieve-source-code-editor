package sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces;

import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.GuardingRequest;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.GuardingRequest;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;

/**
 *
 * @author Milan
 */
public interface GuardingProvider {
    
    public void injectCurrentProjection(CurrentProjection projection);
    public void injectConcernExtractor(ConcernExtractor extractor);
    
    public List<GuardingRequest> createGuards(
            CodeSnippet codeSnippet);
    
    public List<GuardingRequest> createGuards(
            JavaFile javaFile);
    
    public List<GuardingRequest> createGuards(
            ViewModel viewModel);
}
