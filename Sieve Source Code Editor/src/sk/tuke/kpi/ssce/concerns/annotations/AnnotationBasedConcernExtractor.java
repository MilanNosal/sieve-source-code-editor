package sk.tuke.kpi.ssce.concerns.annotations;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
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
        List<? extends AnnotationMirror> annotations = this.getAnnotationsFor(node, document);
        for(AnnotationMirror annotation : annotations) {
            retSet.add(new AnnotationBasedConcern(annotation));
        }
        return retSet;
    }
    
    @Override
    public Set<AnnotationBasedConcern> getConcernsFor(MethodTree node, BaseDocument document) {
        Set<AnnotationBasedConcern> retSet = new HashSet<AnnotationBasedConcern>();
        List<? extends AnnotationMirror> annotations = this.getAnnotationsFor(node, document);
        for(AnnotationMirror annotation : annotations) {
            retSet.add(new AnnotationBasedConcern(annotation));
        }
        return retSet;
    }
    
    @Override
    public Set<AnnotationBasedConcern> getConcernsFor(VariableTree node, BaseDocument document) {
        Set<AnnotationBasedConcern> retSet = new HashSet<AnnotationBasedConcern>();
        List<? extends AnnotationMirror> annotations = this.getAnnotationsFor(node, document);
        for(AnnotationMirror annotation : annotations) {
            retSet.add(new AnnotationBasedConcern(annotation));
        }
        return retSet;
    }

//    @Override
//    public boolean isPresentOn(AnnotationBasedConcern searchable, VariableTree node, BaseDocument document) {
//        return getConcernsFor(node, document).contains(searchable);
//    }
//    
//    @Override
//    public boolean isPresentOn(AnnotationBasedConcern searchable, ClassTree node, BaseDocument document) {
//        return getConcernsFor(node, document).contains(searchable);
//    }
//    
//    @Override
//    public boolean isPresentOn(AnnotationBasedConcern searchable, MethodTree node, BaseDocument document) {
//        return getConcernsFor(node, document).contains(searchable);
//    }
    
    private Element getElementFor(Tree node, BaseDocument document) {
        CompilationInfo info = CompilationUtilities.getCompilationInfo(document);
        Trees trees = info.getTrees();
        TreePath path = TreePath.getPath(info.getCompilationUnit(), node);
        System.out.println(">>>> " + document.toString());
        System.out.println(">>>> " + node.toString());
        return trees.getElement(path);
    }
    
    private Element getElementFor(Tree node, CompilationInfo info) {
        Trees trees = info.getTrees();
        TreePath path = TreePath.getPath(info.getCompilationUnit(), node);
        return trees.getElement(path);
    }

    /**
     * Pomocna metoda pre vytiahnutie anotacnych typov, ktorych anotacie anotuju
     * uvedeny uzol.
     *
     * @param node
     * @return
     */
    private List<? extends AnnotationMirror> getAnnotationsFor(Tree node, BaseDocument document) {
        //try {
            Element element = this.getElementFor(node, document);
            return element.getAnnotationMirrors();
        /*} catch (Throwable ex) {
            ex.printStackTrace(System.err);
            return new ArrayList<AnnotationMirror>();
        }*/
    }
    
    private List<? extends AnnotationMirror> getAnnotationsFor(Tree node, CompilationInfo info) {
        //try {
            Element element = this.getElementFor(node, info);
            return element.getAnnotationMirrors();
        /*} catch (Throwable ex) {
            ex.printStackTrace(System.err);
            return new ArrayList<AnnotationMirror>();
        }*/
    }

    @Override
    public Set<AnnotationBasedConcern> getConcernsFor(ClassTree node, CompilationInfo info) {
        Set<AnnotationBasedConcern> retSet = new HashSet<AnnotationBasedConcern>();
        List<? extends AnnotationMirror> annotations = this.getAnnotationsFor(node, info);
        for(AnnotationMirror annotation : annotations) {
            retSet.add(new AnnotationBasedConcern(annotation));
        }
        return retSet;
    }

    @Override
    public Set<AnnotationBasedConcern> getConcernsFor(MethodTree node, CompilationInfo info) {
        Set<AnnotationBasedConcern> retSet = new HashSet<AnnotationBasedConcern>();
        List<? extends AnnotationMirror> annotations = this.getAnnotationsFor(node, info);
        for(AnnotationMirror annotation : annotations) {
            retSet.add(new AnnotationBasedConcern(annotation));
        }
        return retSet;
    }

    @Override
    public Set<AnnotationBasedConcern> getConcernsFor(VariableTree node, CompilationInfo info) {
        Set<AnnotationBasedConcern> retSet = new HashSet<AnnotationBasedConcern>();
        List<? extends AnnotationMirror> annotations = this.getAnnotationsFor(node, info);
        for(AnnotationMirror annotation : annotations) {
            retSet.add(new AnnotationBasedConcern(annotation));
        }
        return retSet;
    }
}
