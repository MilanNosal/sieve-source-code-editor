package sk.tuke.kpi.ssce.concerns.annotations;

import javax.lang.model.type.DeclaredType;
import sk.tuke.kpi.ssce.concerns.interfaces.Searchable;

/**
 * Implementacia searchable pre anotacny typ.
 * @author Milan
 */
public class AnnotationSearchable implements Searchable {
    
    // so far only for later
    private final DeclaredType annotationType;
    private final String uniquePresentation;

    /**
     * Konstruktor.
     * @param annotationType ak je null, vytvori "no annotations"
     */
    public AnnotationSearchable(DeclaredType annotationType) {
        if(annotationType != null) {
            this.annotationType = annotationType;
            uniquePresentation = "@" + annotationType.asElement().getSimpleName().toString();
        } else {
            this.annotationType = null;
            uniquePresentation = "NO ANNOTATIONS";
        }
        
    }
    
    @Override
    public String getUniquePresentation() {
        return uniquePresentation;
    }
    
    @Override
    public boolean equalsPresentation(String presenation) {
        return uniquePresentation.equals(presenation);
    }
    
    
    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof AnnotationSearchable)) {
            return false;
        } else if(this.equalsPresentation(((AnnotationSearchable) o).getUniquePresentation())) {
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
        return getUniquePresentation();
    }

    @Override
    public int compareTo(Searchable o) {
        if(o == null) {
            return -1;
        } else {
            return this.getUniquePresentation().compareTo(o.getUniquePresentation());
        }
    }
}
