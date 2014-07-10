package sk.tuke.kpi.ssce.concerns.interfaces;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.util.Set;
import org.netbeans.editor.BaseDocument;
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
    
    public Set<T> getConcernsFor(ClassTree node, BaseDocument document);
    
    public Set<T> getConcernsFor(MethodTree node, BaseDocument document);
    
    public Set<T> getConcernsFor(VariableTree node, BaseDocument document);
    
    public boolean isPresentOn(T concern, ClassTree node, BaseDocument document);
    
    public boolean isPresentOn(T concern, MethodTree node, BaseDocument document);
    
    public boolean isPresentOn(T concern, VariableTree node, BaseDocument document);
}
