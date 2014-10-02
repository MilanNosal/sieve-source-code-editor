package sk.tuke.kpi.ssce.concerns.interfaces;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.annotations.concerns.CodeAnalysis;
import sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;

/**
 *
 * @author Milan
 */
@CurrentProjection
@View(aspect = ViewAspect.CONCERN_EXTRACTION)
@CodeAnalysis
public interface ConcernExtractor<T extends Concern> {
    
    public T getNilConcern();
    
    public Set<T> getConcernsFor(ClassTree node, BaseDocument document);
    public Set<T> getConcernsFor(ClassTree node, CompilationInfo info);
    
    public Set<T> getConcernsFor(MethodTree node, BaseDocument document);
    public Set<T> getConcernsFor(MethodTree node, CompilationInfo info);
    
    public Set<T> getConcernsFor(VariableTree node, BaseDocument document);
    public Set<T> getConcernsFor(VariableTree node, CompilationInfo info);
    
//    public boolean isPresentOn(T concern, ClassTree node, BaseDocument document);
//    
//    public boolean isPresentOn(T concern, MethodTree node, BaseDocument document);
//    
//    public boolean isPresentOn(T concern, VariableTree node, BaseDocument document);
}
