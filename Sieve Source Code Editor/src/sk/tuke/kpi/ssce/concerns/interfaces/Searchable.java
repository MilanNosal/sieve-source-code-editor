package sk.tuke.kpi.ssce.concerns.interfaces;

/**
 * Rozhranie vyhladavanych elementov (alebo vlastnosti) v zdrojovom kode.
 * @author Milan
 */
public interface Searchable extends Comparable<Searchable> {
    String getUniquePresentation();
    
    boolean equalsPresentation(String presenation);
    
    @Override
    boolean equals(Object o);
    
    @Override
    int hashCode();
    
    @Override
    String toString();
}
