package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;

/**
 *
 * @author Milan
 */
public @interface PostProcessing {
    public PostProcessingType[] type();
}
