package sk.tuke.kpi.ssce.core.model.view.postprocessing.providers;

import java.util.LinkedList;
import java.util.List;
import sk.tuke.kpi.ssce.annotations.concerns.PostProcessing;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.FoldingRequest;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces.FoldingProvider;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;

/**
 *
 * @author Milan
 */
@PostProcessing(type = PostProcessingType.FOLDING)
@View(aspect = ViewAspect.PRESENTATION)
public class StandardFoldingProvider implements FoldingProvider {

    @Override
    public void injectCurrentProjection(CurrentProjection<? extends Concern> projection) {
        // we dont need it here
    }

    @Override
    public void injectConcernExtractor(ConcernExtractor<? extends Concern> extractor) {
        // we dont need it here
    }

    @Override
    public List<FoldingRequest> createFolds(CodeSnippet<? extends Concern> codeSnippet) {
        List<FoldingRequest> folds = new LinkedList<FoldingRequest>();
        if (codeSnippet.getCodeBinding().isInitialized()) {
            int startInSieve = codeSnippet.getCodeBinding().getStartPositionSieveDocument();
            folds.add(FoldingRequest.create(
                    startInSieve + 1 - (codeSnippet.getStartTextForSJDoc().length() - CodeSnippet.START_TEXT_1_IN_SJDOC.length()),
                    codeSnippet.getCodeBinding().getEndPositionSieveDocument() + 1,
                    " " + codeSnippet.getFullElementName() + " : " + codeSnippet.getElementType() + " "));
        }
        return folds;
    }

    @Override
    public List<FoldingRequest> createFolds(JavaFile<? extends Concern> javaFile) {
        List<FoldingRequest> folds = new LinkedList<FoldingRequest>();
        if (javaFile.getNecessaryImports().getCount() > 0) {
            folds.add(FoldingRequest.create(
                    javaFile.getImportsBinding().getStartPositionSieveDocument(),
                    javaFile.getImportsBinding().getEndPositionSieveDocument(),
                    "imports"));
        }
        return folds;
    }

    @Override
    public List<FoldingRequest> createFolds(ViewModel<? extends Concern> viewModel) {
        return new LinkedList<FoldingRequest>();
    }
    
}
