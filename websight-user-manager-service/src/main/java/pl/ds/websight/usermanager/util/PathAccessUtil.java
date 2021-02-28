package pl.ds.websight.usermanager.util;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.oak.spi.security.authorization.accesscontrol.AccessControlConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.Privilege;

public final class PathAccessUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PathAccessUtil.class);

    private PathAccessUtil() {
        // no instance
    }

    public static boolean hasPrivilege(String path, String privilegeName, JackrabbitAccessControlManager acm) throws RepositoryException {
        boolean result = false;
        try {
            Privilege privilege = acm.privilegeFromName(privilegeName);
            result = acm.hasPrivileges(path, new Privilege[] { privilege });
            if (!result && privilege.isAggregate()) {
                result = acm
                        .hasPrivileges(path, privilege.getAggregatePrivileges());
            }

            return acm.hasPrivileges(path, new Privilege[] { privilege }) || acm.hasPrivileges(path, privilege.getAggregatePrivileges());
        } catch (AccessControlException e) {
            LOG.warn("Failed to chek privileges", e);
        }
        return result;
    }

    public static String getPath(JackrabbitAccessControlEntry readByPrincipalAclEntry) throws RepositoryException {
        Value pathVal = readByPrincipalAclEntry.getRestriction(AccessControlConstants.REP_NODE_PATH);
        return pathVal != null ? pathVal.getString() : null;
    }
}