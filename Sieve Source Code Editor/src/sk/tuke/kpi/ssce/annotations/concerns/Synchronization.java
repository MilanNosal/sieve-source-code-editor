package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.Direction;

/**
 * 
 * @author Milan Nosal
 */
public @interface Synchronization {
    
    public Direction[] direction() default {Direction.JAVATOSJ, Direction.SJTOJAVA};
    
    
}
