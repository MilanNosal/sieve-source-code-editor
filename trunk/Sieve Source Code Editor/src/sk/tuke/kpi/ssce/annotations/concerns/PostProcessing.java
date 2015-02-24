package sk.tuke.kpi.ssce.annotations.concerns;

import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;

/**
 * Code post-processing the view to either guard view fragments or to
 * fold them.
 * @author Milan
 */
public @interface PostProcessing {
    public PostProcessingType[] type();
}
