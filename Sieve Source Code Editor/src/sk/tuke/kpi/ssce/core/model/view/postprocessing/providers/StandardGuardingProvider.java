package sk.tuke.kpi.ssce.core.model.view.postprocessing.providers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.GuardingRequest;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces.GuardingProvider;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;

/**
 *
 * @author Milan
 */
public class StandardGuardingProvider implements GuardingProvider {

    @Override
    public void injectCurrentProjection(CurrentProjection projection) {
        // we dont need it here
    }

    @Override
    public void injectConcernExtractor(ConcernExtractor extractor) {
        // we dont need it here
    }

    @Override
    public List<GuardingRequest> createGuards(CodeSnippet codeSnippet) {
        List<GuardingRequest> guards = new ArrayList<GuardingRequest>();
        guards.add(GuardingRequest.create(
                codeSnippet.getCodeBinding().getStartPositionSieveDocument() - codeSnippet.getStartTextForSJDoc().length(),
                codeSnippet.getCodeBinding().getStartPositionSieveDocument()
        ));
        guards.add(GuardingRequest.create(
                codeSnippet.getCodeBinding().getEndPositionSieveDocument() + 1,
                codeSnippet.getCodeBinding().getEndPositionSieveDocument() + codeSnippet.getEndTextForSJDoc().length()
        ));
        return guards;
    }

    @Override
    public List<GuardingRequest> createGuards(JavaFile javaFile) {
        List<GuardingRequest> guards = new ArrayList<GuardingRequest>();
        guards.add(GuardingRequest.create(
                javaFile.getBeginInSJ(), javaFile.getImportsBinding().getStartPositionSieveDocument()
        ));
        if (javaFile.getCodeSnippets().isEmpty()) {
            guards.add(GuardingRequest.create(
                    javaFile.getImportsBinding().getEndPositionSieveDocument() + 1,
                    javaFile.getEndInSJ() + 1
            ));
        } else {
            guards.add(GuardingRequest.create(
                    javaFile.getImportsBinding().getEndPositionSieveDocument() + 1,
                    javaFile.getCodeSnippets().get(0).getCodeBinding().getStartPositionSieveDocument() - 1
            ));
            // codesnippets are handled by themselves, however, we have to handle the end
            guards.add(GuardingRequest.create(
                    javaFile.getCodeSnippets().get(javaFile.getCodeSnippets().size() - 1).getCodeBinding().getEndPositionSieveDocument() + 2,
                    javaFile.getEndInSJ() + 1
            ));
        }
        return guards;
    }

    @Override
    public List<GuardingRequest> createGuards(ViewModel viewModel) {
        return new LinkedList<GuardingRequest>();
    }
    
}
