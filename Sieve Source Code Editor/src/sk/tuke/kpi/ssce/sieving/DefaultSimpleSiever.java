package sk.tuke.kpi.ssce.sieving;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.sieving.interfaces.CodeSiever;

/**
 *
 * @author Milan
 */
@SourceCodeSieving
public class DefaultSimpleSiever implements CodeSiever {

    @Override
    public boolean sieveCode(
            Stack<Set<Concern>> contextOfConcerns,
            CurrentProjection currentProjection,
            ConcernExtractor extractor) {
        Set<Concern> codeIntents = getConcernsForCode(contextOfConcerns);
        
        boolean match = false;

        Set<Concern> selectedIntents = new HashSet<Concern>(currentProjection.getCurrentlySelectedConcerns());
        if (selectedIntents.isEmpty()) {
            match = false;
        } else {
            if ("OR".equals(currentProjection.getParams().get("mode"))) {
                match = true;
                if (selectedIntents.contains(extractor.getNilConcern())) {
                    if (!codeIntents.isEmpty()) {
                        match = false;
                    }
                    selectedIntents.remove(extractor.getNilConcern());
                }
                for (Concern selectedIntent : selectedIntents) {
                    if (!codeIntents.contains(selectedIntent)) {
                        match = false;
                        break;
                    }
                }
            } else {//if (CurrentProjection.MODE_OR.equals(currentProjection.getParams())) {
                match = false;
                if (selectedIntents.contains(extractor.getNilConcern())) {
                    if (codeIntents.isEmpty()) {
                        match = true;
                    }
                }
                for (Concern selectedIntent : selectedIntents) {
                    if (codeIntents.contains(selectedIntent)) {
                        match = true;
                        break;
                    }
                }
            }
        }
        return match;
    }
    
    private Set<Concern> getConcernsForCode(Stack<Set<Concern>> contextOfConcerns) {
        Set<Concern> concerns = new HashSet<Concern>();
        for (int i = 0; i < contextOfConcerns.size(); i++) {
            concerns.addAll(contextOfConcerns.get(i));
        }
        return concerns;
    }
}
