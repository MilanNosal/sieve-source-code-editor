package sk.tuke.kpi.ssce.concerns.annotations;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.CompilationUtilities;

/**
 *
 * @author Milan
 */
public class AnnotationBasedConcernExtractor implements ConcernExtractor<AnnotationBasedConcern> {
    
    @Override
    public AnnotationBasedConcern getNilConcern() {
        return new AnnotationBasedConcern(null);
    }

    @Override
    public Set<AnnotationBasedConcern> getConcernsFor(ClassTree node, BaseDocument document) {
        Set<AnnotationBasedConcern> retSet = new HashSet<AnnotationBasedConcern>();
        Set<DeclaredType> declTypes = null;//treeUtils.getAnnTypesFor(node);
        for(DeclaredType type : declTypes) {
            retSet.add(new AnnotationBasedConcern(type));
        }
        return retSet;
    }
    
    @Override
    public Set<AnnotationBasedConcern> getConcernsFor(MethodTree node, BaseDocument document) {
        Set<AnnotationBasedConcern> retSet = new HashSet<AnnotationBasedConcern>();
        Set<DeclaredType> declTypes = null;//treeUtils.getAnnTypesFor(node);
        for(DeclaredType type : declTypes) {
            retSet.add(new AnnotationBasedConcern(type));
        }
        return retSet;
    }
    
    @Override
    public Set<AnnotationBasedConcern> getConcernsFor(VariableTree node, BaseDocument document) {
        Set<AnnotationBasedConcern> retSet = new HashSet<AnnotationBasedConcern>();
        Set<DeclaredType> declTypes = null;//treeUtils.getAnnTypesFor(node);
        for(DeclaredType type : declTypes) {
            retSet.add(new AnnotationBasedConcern(type));
        }
        return retSet;
    }

    @Override
    public boolean isPresentOn(AnnotationBasedConcern searchable, VariableTree node, BaseDocument document) {
        return getConcernsFor(node, document).contains(searchable);
    }
    
    @Override
    public boolean isPresentOn(AnnotationBasedConcern searchable, ClassTree node, BaseDocument document) {
        return getConcernsFor(node, document).contains(searchable);
    }
    
    @Override
    public boolean isPresentOn(AnnotationBasedConcern searchable, MethodTree node, BaseDocument document) {
        return getConcernsFor(node, document).contains(searchable);
    }
    
    private Element getElementFor(Tree node, BaseDocument document) {
        CompilationInfo info = CompilationUtilities.getCompilationInfo(document);
        Trees trees = info.getTrees();
        TreePath path = TreePath.getPath(info.getCompilationUnit(), node);
        return trees.getElement(path);
    }

    private boolean isAnnotatedBy(Element element, DeclaredType type) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Poomcna metoda pre vytiahnutie anotacnych typov, ktorych anotacie anotuju
     * uvedeny uzol.
     *
     * @param node
     * @return
     */
    private Set<DeclaredType> getAnnTypesFor(Tree node, BaseDocument document) {
        try {
            Element element = this.getElementFor(node, document);
            Set<DeclaredType> annTypes = new HashSet<DeclaredType>();
            for (AnnotationMirror am : element.getAnnotationMirrors()) { //TODO: nullpointer pri pridani deprecated, variable
                annTypes.add(am.getAnnotationType());
            }
            return annTypes;
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            return new HashSet<DeclaredType>();
        }
    }
}
