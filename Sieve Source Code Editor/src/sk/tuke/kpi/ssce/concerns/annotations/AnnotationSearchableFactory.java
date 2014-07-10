package sk.tuke.kpi.ssce.concerns.annotations;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.type.DeclaredType;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;

/**
 *
 * @author Milan
 */
public class AnnotationSearchableFactory implements ConcernExtractor<AnnotationSearchable> {
    
    private CompilerTreeUtils treeUtils;

    public AnnotationSearchableFactory(CompilerTreeUtils treeUtils) {
        this.treeUtils = treeUtils;
    }
    
    @Override
    public AnnotationSearchable getNilConcern() {
        return new AnnotationSearchable(null);
    }

    @Override
    public Set<AnnotationSearchable> getConcernsFor(ClassTree node) {
        Set<AnnotationSearchable> retSet = new HashSet<AnnotationSearchable>();
        Set<DeclaredType> declTypes = treeUtils.getAnnTypesFor(node);
        for(DeclaredType type : declTypes) {
            retSet.add(new AnnotationSearchable(type));
        }
        return retSet;
    }
    
    @Override
    public Set<AnnotationSearchable> getConcernsFor(MethodTree node) {
        Set<AnnotationSearchable> retSet = new HashSet<AnnotationSearchable>();
        Set<DeclaredType> declTypes = treeUtils.getAnnTypesFor(node);
        for(DeclaredType type : declTypes) {
            retSet.add(new AnnotationSearchable(type));
        }
        return retSet;
    }
    
    @Override
    public Set<AnnotationSearchable> getConcernsFor(VariableTree node) {
        Set<AnnotationSearchable> retSet = new HashSet<AnnotationSearchable>();
        Set<DeclaredType> declTypes = treeUtils.getAnnTypesFor(node);
        for(DeclaredType type : declTypes) {
            retSet.add(new AnnotationSearchable(type));
        }
        return retSet;
    }

    @Override
    public boolean isPresentOn(AnnotationSearchable searchable, VariableTree node) {
        return getConcernsFor(node).contains(searchable);
    }
    
    @Override
    public boolean isPresentOn(AnnotationSearchable searchable, ClassTree node) {
        return getConcernsFor(node).contains(searchable);
    }
    
    @Override
    public boolean isPresentOn(AnnotationSearchable searchable, MethodTree node) {
        return getConcernsFor(node).contains(searchable);
    }
}
