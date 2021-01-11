package pl.ds.websight.usermanager.util;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.oak.spi.security.authorization.accesscontrol.AccessControlConstants;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.security.Privilege;

public final class PathAccessUtil {

    private PathAccessUtil() {
        // no instance
    }

    public static boolean hasPrivilege(String path, String privilegeName, JackrabbitAccessControlManager acm) throws RepositoryException {
        Privilege privilege = acm.privilegeFromName(privilegeName);
        return acm.hasPrivileges(path, new Privilege[]{privilege});
    }

    public static String getPath(JackrabbitAccessControlEntry readByPrincipalAclEntry) throws RepositoryException {
        Value pathVal = readByPrincipalAclEntry.getRestriction(AccessControlConstants.REP_NODE_PATH);
        return pathVal != null ? pathVal.getString() : null;
    }
}