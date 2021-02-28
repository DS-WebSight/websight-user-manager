package pl.ds.websight.usermanager.rest.permission;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.usermanager.util.JcrSecurityUtil;
import pl.ds.websight.usermanager.util.PathAccessUtil;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.jackrabbit.oak.spi.security.authorization.accesscontrol.AccessControlConstants.REP_NODE_PATH;

final class AccessModifyFacade {

    private static final Logger LOG = LoggerFactory.getLogger(AccessModifyFacade.class);

    private final Authorizable authorizable;
    private final Session session;
    private final JackrabbitAccessControlManager acm;

    private AccessModifyFacade(Authorizable authorizable, Session session, JackrabbitAccessControlManager acm) {
        this.authorizable = authorizable;
        this.session = session;
        this.acm = acm;
    }

    public static AccessModifyFacade forAuthorizable(Authorizable authorizable, Session session) throws RepositoryException {
        JackrabbitAccessControlManager accessControlManager = (JackrabbitAccessControlManager) session.getAccessControlManager();
        return new AccessModifyFacade(authorizable, session, accessControlManager);
    }

    public boolean createAclEntry(String path, Privilege[] privileges, boolean isAllow, Map<String, List<String>> restrictions)
            throws RepositoryException {
        JackrabbitAccessControlList acl = AccessControlUtils.getAccessControlList(acm, path);
        if (acl == null) {
            LOG.warn("Could not get {} for path: {}", JackrabbitAccessControlList.class.getName(), path);
            return false;
        }
        Map<String, Value> simpleRestrictions = createSimpleRestrictionsMap(acl, session.getValueFactory(), restrictions);
        Map<String, Value[]> multiRestrictions = createMultiRestrictionsMap(acl, session.getValueFactory(), restrictions);
        if (acl.addEntry(authorizable.getPrincipal(), privileges, isAllow, simpleRestrictions, multiRestrictions)) {
            savePolicyChanges(acl);
        }
        return true;
    }

    public boolean updateAclEntry(String entryId, String policyId, String path, Privilege[] privileges, boolean isAllow,
            Map<String, List<String>> restrictions) throws RepositoryException {
        EntryReplacement replacer = (acl -> {
            Value nodePathValue = toRestrictionValue(path, acl, REP_NODE_PATH, session.getValueFactory());
            if (nodePathValue == null) {
                LOG.warn("Could not define path: {} for replacing restriction", path);
                return;
            }
            Map<String, Value> simpleRestrictionsMap = createSimpleRestrictionsMap(acl, session.getValueFactory(), restrictions);
            simpleRestrictionsMap.put(REP_NODE_PATH, nodePathValue);
            Map<String, Value[]> multiRestrictions = createMultiRestrictionsMap(acl, session.getValueFactory(), restrictions);

            if (acl.addEntry(authorizable.getPrincipal(), privileges, isAllow, simpleRestrictionsMap, multiRestrictions)) {
                savePolicyChanges(acl);
            }
        });
        return removeAclEntry(entryId, policyId, replacer);
    }

    private static Map<String, Value> createSimpleRestrictionsMap(JackrabbitAccessControlList acl, ValueFactory valueFactory,
            Map<String, List<String>> restrictions) throws RepositoryException {
        Map<String, String> simpleRestrictions = restrictions.entrySet().stream()
                .filter(restrictionEntry -> restrictionEntry.getValue().size() == 1)
                .collect(toMap(Map.Entry::getKey, restrictionsEntry -> restrictionsEntry.getValue().get(0)));
        Map<String, Value> restrictionsMap = new HashMap<>();
        for (Map.Entry<String, String> aclRestriction : simpleRestrictions.entrySet()) {
            String restrictionName = aclRestriction.getKey();
            Value restrictionValue = toRestrictionValue(aclRestriction.getValue(), acl, restrictionName, valueFactory);
            if (restrictionValue != null) {
                restrictionsMap.put(restrictionName, restrictionValue);
            }
        }
        return restrictionsMap;
    }

    private static Map<String, Value[]> createMultiRestrictionsMap(JackrabbitAccessControlList acl, ValueFactory valueFactory,
            Map<String, List<String>> restrictions) throws RepositoryException {
        Map<String, List<String>> multiRestrictions = restrictions.entrySet().stream()
                .filter(restrictionEntry -> restrictionEntry.getValue().size() > 1)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Value[]> restrictionsMap = new HashMap<>();
        for (Map.Entry<String, List<String>> aclRestriction : multiRestrictions.entrySet()) {
            String restrictionName = aclRestriction.getKey();
            addMultiRestrictions(restrictionsMap, restrictionName, aclRestriction.getValue(), acl, valueFactory);
        }
        return restrictionsMap;
    }

    private static void addMultiRestrictions(Map<String, Value[]> restrictionsMap, String restrictionName,
            List<String> restrictionStrValues, JackrabbitAccessControlList acl, ValueFactory valueFactory)
            throws RepositoryException {
        List<Value> restrictionValues = new ArrayList<>();
        for (String restrictionValStr : restrictionStrValues) {
            Value restrictionValue = toRestrictionValue(restrictionValStr, acl, restrictionName, valueFactory);
            if (restrictionValue != null) {
                restrictionValues.add(restrictionValue);
            }
        }
        restrictionsMap.put(restrictionName, restrictionValues.toArray(new Value[]{}));
    }

    private static Value toRestrictionValue(String value, JackrabbitAccessControlList acl, String restrictionName,
            ValueFactory valueFactory) throws RepositoryException {
        int restrictionType = acl.getRestrictionType(restrictionName);
        if (PropertyType.UNDEFINED == restrictionType) {
            LOG.warn("Could not find a type of restriction: {}", restrictionName);
            return null;
        }
        return valueFactory.createValue(value, restrictionType);
    }

    public boolean removeAclEntry(String entryId, String policyId) throws RepositoryException {
        return removeAclEntry(entryId, policyId, null);
    }

    private boolean removeAclEntry(String entryId, String policyId, EntryReplacement replacer) throws RepositoryException {
        for (JackrabbitAccessControlPolicy policy : acm.getPolicies(authorizable.getPrincipal())) {
            if (matchesPolicyId(policy, policyId)) {
                JackrabbitAccessControlList acl = (JackrabbitAccessControlList) policy;
                JackrabbitAccessControlEntry entryToDelete = getAclEntry(entryId, acl);
                if (entryToDelete == null) {
                    return false;
                }
                String path = PathAccessUtil.getPath(entryToDelete);
                if (!canEditAcl(path)) {
                    LOG.warn("Could not remove ACL Entry due to lack of edit permission for path: {} and user: {}", path,
                            session.getUserID());
                    return false;
                }
                acl.removeAccessControlEntry(entryToDelete);
                savePolicyChanges(acl);
                if (replacer != null) {
                    replacer.replace(acl);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean matchesPolicyId(AccessControlPolicy policy, String policyId) throws RepositoryException {
        return policy instanceof JackrabbitAccessControlList && JcrSecurityUtil.getPolicyId(policy).equals(policyId);
    }

    private static JackrabbitAccessControlEntry getAclEntry(String entryId, JackrabbitAccessControlList acl) throws RepositoryException {
        AccessControlEntry[] accessControlEntries = acl.getAccessControlEntries();
        return (JackrabbitAccessControlEntry) Arrays.stream(accessControlEntries)
                .filter(accessControlEntry -> JcrSecurityUtil.getEntryId(accessControlEntry).equals(entryId))
                .findFirst()
                .orElse(null);
    }

    private boolean canEditAcl(String path) {
        try {
            return PathAccessUtil.hasPrivilege(path, Privilege.JCR_MODIFY_ACCESS_CONTROL, acm);
        } catch (RepositoryException e) {
            LOG.warn("Could not check if Access Control Manager could edit ACLs", e);
            return false;
        }
    }

    private void savePolicyChanges(JackrabbitAccessControlList acl) throws RepositoryException {
        acm.setPolicy(acl.getPath(), acl);
        session.save();
    }

    private interface EntryReplacement {
        void replace(JackrabbitAccessControlList acl) throws RepositoryException;
    }
}
