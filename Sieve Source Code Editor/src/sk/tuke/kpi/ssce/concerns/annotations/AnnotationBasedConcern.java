package sk.tuke.kpi.ssce.concerns.annotations;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.DeclaredType;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;

/**
 * Implementacia searchable pre anotacny typ.
 * @author Milan
 */
public class AnnotationBasedConcern implements Concern {
    
    // so far only for later
    private final DeclaredType annotationType;
    private final String uniquePresentation;
    private final AnnotationMirror annotation;

    /**
     * Konstruktor.
     * @param annotationType ak je null, vytvori "no annotations"
     */
    public AnnotationBasedConcern(AnnotationMirror annotation) {
        if(annotation != null) {
            this.annotation = annotation;
            this.annotationType = annotation.getAnnotationType();
            uniquePresentation = "@" + annotationType.asElement().getSimpleName().toString();
        } else {
            this.annotation = null;
            this.annotationType = null;
            uniquePresentation = "@";
        }        
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof AnnotationBasedConcern)) {
            return false;
        } else if(this.uniquePresentation.equals(((AnnotationBasedConcern) o).uniquePresentation)) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return uniquePresentation.hashCode();
    }
    
    @Override
    public String toString() {
        return uniquePresentation;
    }

    @Override
    public int compareTo(Concern o) {
        if(o == null || !(o instanceof AnnotationBasedConcern)) {
            return -1;
        } else {
            return this.uniquePresentation.compareTo(((AnnotationBasedConcern)o).uniquePresentation);
        }
    }

    public DeclaredType getAnnotationType() {
        return annotationType;
    }

    public String getUniquePresentation() {
        return uniquePresentation;
    }

    public AnnotationMirror getAnnotation() {
        return annotation;
    }
}
