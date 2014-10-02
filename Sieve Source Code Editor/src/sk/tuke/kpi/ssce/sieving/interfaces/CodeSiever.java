package sk.tuke.kpi.ssce.sieving.interfaces;

import java.util.Set;
import java.util.Stack;
import org.netbeans.api.java.source.CompilationInfo;
import sk.tuke.kpi.ssce.annotations.concerns.CodeAnalysis;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;

/**
 *
 * @author Milan Nosal
 */
@SourceCodeSieving
@CodeAnalysis(output = RepresentationOf.VIEW)
public interface CodeSiever<T extends Concern> {
    public boolean sieveCode(
            Stack<Set<T>> contextOfConcerns,
            CurrentProjection<T> currentProjection,
            ConcernExtractor<T> concernExtractor,
            CompilationInfo info
    );
}
