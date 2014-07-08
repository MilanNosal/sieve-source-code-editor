package sk.tuke.kpi.ssce.concerns.interfaces;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.Set;

/**
 *
 * @author Milan
 */
public interface SearchableFactory<T extends Searchable> {
    
    T getNilSearchable();
    
    Set<T> getSearchablesFor(ClassTree node);
    
    Set<T> getSearchablesFor(MethodTree node);
    
    Set<T> getSearchablesFor(VariableTree node);
    
    boolean isPresentOn(T searchable, ClassTree node);
    
    boolean isPresentOn(T searchable, MethodTree node);
    
    boolean isPresentOn(T searchable, VariableTree node);
}
