package ir.hfj.library.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ExclusionGsonStrategies implements ExclusionStrategy
{

    public boolean shouldSkipClass(Class<?> arg0)
    {
        return false;
    }

    public boolean shouldSkipField(FieldAttributes fieldAttributes)
    {
        ExcludeGson annotation = fieldAttributes.getAnnotation(ExcludeGson.class);

        return annotation != null;

    }

}
