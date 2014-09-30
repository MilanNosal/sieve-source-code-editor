package sk.tuke.kpi.ssce.sieving;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.netbeans.api.java.source.CompilationInfo;
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
public class DefaultSimpleSiever<T extends Concern> implements CodeSiever<T> {

    @Override
    public boolean sieveCode(
            Stack<Set<T>> contextOfConcerns,
            CurrentProjection<T> currentProjection,
            ConcernExtractor<T> extractor,
            CompilationInfo info) {
        Set<Concern> codeConcerns = getConcernsForCode(contextOfConcerns);
        
        boolean match = false;

        Set<Concern> selectedConcerns = new HashSet<Concern>(currentProjection.getCurrentlySelectedConcerns());
        if (selectedConcerns.isEmpty()) {
            match = false;
        } else {
            if ("AND".equals(currentProjection.getParams().get("mode"))) {
                match = true;
                if (selectedConcerns.contains(extractor.getNilConcern())) {
                    if (!codeConcerns.isEmpty()) {
                        match = false;
                    }
                    selectedConcerns.remove(extractor.getNilConcern());
                }
                for (Concern selectedConcern : selectedConcerns) {
                    if (!codeConcerns.contains(selectedConcern)) {
                        match = false;
                        break;
                    }
                }
            } else {//if (CurrentProjection.MODE_OR.equals(currentProjection.getParams())) {
                match = false;
                if (selectedConcerns.contains(extractor.getNilConcern())) {
                    if (codeConcerns.isEmpty()) {
                        match = true;
                    }
                }
                for (Concern selectedConcern : selectedConcerns) {
                    if (codeConcerns.contains(selectedConcern)) {
                        match = true;
                        break;
                    }
                }
            }
        }
        return match;
    }
    
    private Set<Concern> getConcernsForCode(Stack<Set<T>> contextOfConcerns) {
        Set<Concern> concerns = new HashSet<Concern>();
        for (int i = 0; i < contextOfConcerns.size(); i++) {
            concerns.addAll(contextOfConcerns.get(i));
        }
        return concerns;
    }
}
