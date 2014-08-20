package sk.tuke.kpi.ssce.concerns.interfaces;

import sk.tuke.kpi.ssce.annotations.concerns.CurrentProjection;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;

/**
 * Rozhranie vyhladavanych elementov (alebo vlastnosti) v zdrojovom kode.
 * @author Milan
 */
@CurrentProjection
@View(aspect = ViewAspect.CONCERN_EXTRACTION)
public interface Concern extends Comparable<Concern> {
    
    @Override
    boolean equals(Object o);
    
    @Override
    int hashCode();
    
    @Override
    String toString();
}
