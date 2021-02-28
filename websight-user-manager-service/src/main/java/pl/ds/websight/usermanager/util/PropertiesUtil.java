package pl.ds.websight.usermanager.util;

import org.apache.sling.api.resource.ModifiableValueMap;

import java.util.Map;

public final class PropertiesUtil {

    private PropertiesUtil() {
        // no instances
    }

    public static void putIfChangedIgnoreNull(ModifiableValueMap valueMap, String propertyName, Object value) {
        if (value != null && !value.equals(valueMap.get(propertyName))) {
            valueMap.put(propertyName, value);
        }
    }

    public static void putEveryIfChangedIgnoreNulls(ModifiableValueMap valueMap, Map<String, Object> properties) {
        properties.forEach((propertyName, value) -> putIfChangedIgnoreNull(valueMap, propertyName, value));
    }
}
