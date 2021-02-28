package pl.ds.websight.usermanager.util;

import org.apache.jackrabbit.api.security.user.QueryBuilder;

public final class QueryUtil {

    private QueryUtil() {
        //no instance
    }

    public static <Q> Q caseInsensitiveLike(QueryBuilder<Q> builder, String property, String value) {
        return builder.like("fn:lower-case(" + property + ")", "%" + value.toLowerCase() + "%");
    }

    public static String searchProperty(String propertyName) {
        return '@' + propertyName;
    }

}
