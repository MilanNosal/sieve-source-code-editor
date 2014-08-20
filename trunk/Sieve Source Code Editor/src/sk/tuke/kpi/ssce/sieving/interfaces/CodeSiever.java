package sk.tuke.kpi.ssce.sieving.interfaces;

import java.util.Set;
import java.util.Stack;
import org.netbeans.api.java.source.CompilationInfo;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;

/**
 *
 * @author Milan Nosal
 */
@SourceCodeSieving
public interface CodeSiever {
    public boolean sieveCode(
            Stack<Set<Concern>> contextOfConcerns, 
            CurrentProjection currentProjection,
            ConcernExtractor concernExtractor,
            CompilationInfo info
    );
}
