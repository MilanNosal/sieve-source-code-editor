package sk.tuke.kpi.ssce.concerns.interfaces;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.util.Set;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionConfiguration;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;

/**
 *
 * @author Milan
 */
@ProjectionConfiguration
@View(aspect = ViewAspect.CONCERN_EXTRACTION)
public interface ConcernExtractor<T extends Concern> {
    
    public T getNilConcern();
    
    public Set<T> getConcernsFor(ClassTree node);
    
    public Set<T> getConcernsFor(MethodTree node);
    
    public Set<T> getConcernsFor(VariableTree node);
    
    public boolean isPresentOn(T concern, ClassTree node);
    
    public boolean isPresentOn(T concern, MethodTree node);
    
    public boolean isPresentOn(T concern, VariableTree node);
}
