package pl.ds.websight.usermanager.util;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.oak.spi.security.authorization.accesscontrol.AccessControlConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.NamedAccessControlPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JcrSecurityUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JcrSecurityUtil.class);

    private JcrSecurityUtil() {
        // no instance
    }

    public static String getEntryId(AccessControlEntry aclEntry) {
        return aclEntry.hashCode() + "";
    }

    public static String getPolicyId(AccessControlPolicy policy) throws RepositoryException {
        if (policy instanceof NamedAccessControlPolicy) {
            return ((NamedAccessControlPolicy) policy).getName();
        } else if (policy instanceof AccessControlList) {
            return Arrays.stream(((AccessControlList) policy).getAccessControlEntries())
                    .map(JcrSecurityUtil::getEntryId)
                    .reduce("[", (partialStr, entryId) -> partialStr + '_' + entryId);
        }
        return policy.getClass().getName();
    }

    public static Map<String, List<String>> getRestrictions(AccessControlEntry ace, boolean isReadByPath) throws RepositoryException {
        if (!(ace instanceof JackrabbitAccessControlEntry)) {
            return null;
        }
        JackrabbitAccessControlEntry jackrabbitAce = (JackrabbitAccessControlEntry) ace;
        String[] restrictionNames = jackrabbitAce.getRestrictionNames();
        if (restrictionNames == null) {
            return null;
        }
        Map<String, List<String>> restrictionsMap = new HashMap<>();
        for (String restriction : restrictionNames) {
            if (isReadByPath || !AccessControlConstants.REP_NODE_PATH.equals(restriction)) {
                restrictionsMap.put(restriction, getRestrictionPatterns(jackrabbitAce, restriction));
            }
        }
        return restrictionsMap.isEmpty() ? null : restrictionsMap;
    }

    private static List<String> getRestrictionPatterns(JackrabbitAccessControlEntry aclEntry, String restrictionName)
            throws RepositoryException {
        List<String> patterns = new ArrayList<>();
        for (Value restrictionVal : aclEntry.getRestrictions(restrictionName)) {
            try {
                patterns.add(restrictionVal.getString());
            } catch (ValueFormatException e) {
                LOG.warn("Could not convert pattern value to String: {} for restriction: {}", restrictionVal, restrictionName);
            }
        }
        return patterns;
    }
}
