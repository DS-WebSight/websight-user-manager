package pl.ds.websight.usermanager.rest.permission;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.jackrabbit.oak.spi.security.privilege.PrivilegeConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.AclEntriesDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.util.JcrSecurityUtil;
import pl.ds.websight.usermanager.util.PathAccessUtil;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.security.Principal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;

@SlingAction(GET)
@Component
public class FindAclEntriesRestAction extends AbstractRestAction<PrincipalValidatableRestModel, AclEntriesDto>
        implements RestAction<PrincipalValidatableRestModel, AclEntriesDto> {

    private static final Logger LOG = LoggerFactory.getLogger(FindAclEntriesRestAction.class);

    @Override
    protected RestActionResult<AclEntriesDto> performAction(PrincipalValidatableRestModel model) throws Exception {
        Session session = model.getSession();
        JackrabbitAccessControlManager acm = (JackrabbitAccessControlManager) session.getAccessControlManager();
        Authorizable authorizable = model.getAuthorizable();
        return RestActionResult.success(new AclEntriesDto(getPoliciesMap(authorizable.getPrincipal(), acm),
                getInheritedMembershipPolicies(authorizable, acm, session)));
    }

    private static Set<InheritedMembershipInfo> getInheritedMembershipPolicies(Authorizable authorizable,
            JackrabbitAccessControlManager acm, Session session) throws RepositoryException {
        Principal everyone = AccessControlUtils.getEveryonePrincipal(session);
        if (everyone.equals(authorizable.getPrincipal())) {
            LOG.debug("'Everyone' group doesn't contain any inherited policies");
            return Collections.emptySet();
        }
        Set<InheritedMembershipInfo> analyzedInfos = new LinkedHashSet<>();
        Queue<InheritedMembershipInfo> infos = new ArrayDeque<>();
        InheritedMembershipInfo root = InheritedMembershipInfo.wrapRoot(authorizable);
        infos.add(root);

        while (!infos.isEmpty()) {
            InheritedMembershipInfo currentInfo = infos.remove();
            LOG.debug("Added info about inherited entries for authorizable {} with root: {}", currentInfo.getAuthorizable().getID(),
                    root.getAuthorizable().getID());
            analyzedInfos.add(currentInfo);
            for (Iterator<Group> membershipIterator = currentInfo.getAuthorizable().declaredMemberOf(); membershipIterator
                    .hasNext(); ) {
                Group membership = membershipIterator.next();
                InheritedMembershipInfo inheritedMemberPolicies = InheritedMembershipInfo.wrap(membership, currentInfo, null);
                if (!analyzedInfos.contains(inheritedMemberPolicies)) {
                    inheritedMemberPolicies.setPolicies(getPoliciesMap(membership.getPrincipal(), acm));
                    infos.add(inheritedMemberPolicies);
                }
            }
        }
        analyzedInfos.remove(root);
        analyzedInfos.add(InheritedMembershipInfo.wrapEveryoneGroup(getPoliciesMap(everyone, acm)));
        return analyzedInfos;
    }

    private static Map<String, JackrabbitAccessControlEntry[]> getPoliciesMap(Principal principal, JackrabbitAccessControlManager acm)
            throws RepositoryException {
        Map<String, JackrabbitAccessControlEntry[]> principalEntries = new LinkedHashMap<>();
        for (JackrabbitAccessControlPolicy policy : acm.getPolicies(principal)) {
            if (policy instanceof JackrabbitAccessControlList) {
                addReadableEntries(principalEntries, (JackrabbitAccessControlList) policy, acm);
            }
        }
        return principalEntries;
    }

    private static void addReadableEntries(Map<String, JackrabbitAccessControlEntry[]> principalEntries, JackrabbitAccessControlList acl,
            JackrabbitAccessControlManager acm) throws RepositoryException {
        JackrabbitAccessControlEntry[] aclEntries = (JackrabbitAccessControlEntry[]) acl.getAccessControlEntries();
        List<JackrabbitAccessControlEntry> filteredEntries = new ArrayList<>();
        for (JackrabbitAccessControlEntry aclEntry : aclEntries) {
            if (canReadAcl(PathAccessUtil.getPath(aclEntry), acm)) {
                filteredEntries.add(aclEntry);
            }
        }
        if (!filteredEntries.isEmpty()) {
            principalEntries.merge(JcrSecurityUtil.getPolicyId(acl), filteredEntries.toArray(new JackrabbitAccessControlEntry[0]),
                    ArrayUtils::addAll);
        }
    }

    private static boolean canReadAcl(String path, JackrabbitAccessControlManager acm) {
        try {
            return PathAccessUtil.hasPrivilege(path, PrivilegeConstants.JCR_READ_ACCESS_CONTROL, acm);
        } catch (RepositoryException e) {
            LOG.warn("Could not check if Access Control Manager could read ACLs", e);
            return false;
        }
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.FIND_ACL_ENTRIES_ERROR;
    }
}
