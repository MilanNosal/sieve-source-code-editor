package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.Direction;

/**
 * Code that implements synchronization between Java source files and 
 * the view document. A change in view is propagated to the source files
 * and vice versa.
 * @author Milan Nosal
 */
public @interface Synchronization {
    
    public Direction[] direction() default {Direction.JAVATOSJ, Direction.SJTOJAVA};
    
    
}
