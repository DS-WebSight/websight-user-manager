package pl.ds.websight.usermanager.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.PROFILE_NODE_NAME;

public final class AuthorizableUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizableUtil.class);

    private static final Set<String> PROTECTED_USERS = ImmutableSet.of("admin", "anonymous");

    private AuthorizableUtil() {
        // no instances
    }

    public static Resource getOrCreateUserProfileResource(ResourceResolver resourceResolver, User user)
            throws RepositoryException, PersistenceException {
        return getOrCreateUserProfileResource(resourceResolver, user, Maps.newHashMap());
    }

    public static Resource getOrCreateUserProfileResource(ResourceResolver resourceResolver, User user, Map<String, Object> properties)
            throws RepositoryException, PersistenceException {
        Resource userResource = requireNonNull(resourceResolver.getResource(user.getPath()),
                "User resource " + user.getPath() + " does not exist");
        Resource profileResource = resourceResolver.getResource(user.getPath() + '/' + PROFILE_NODE_NAME);
        if (profileResource == null) {
            return resourceResolver.create(userResource, PROFILE_NODE_NAME, properties);
        } else {
            return profileResource;
        }
    }

    public static boolean isProtected(String username) {
        return PROTECTED_USERS.contains(username);
    }

    public static void assignToGroups(UserManager userManager, Authorizable authorizable, List<String> groups) throws RepositoryException {
        for (String groupName : groups) {
            assignToGroup(userManager, authorizable, groupName);
        }
    }

    private static void assignToGroup(UserManager userManager, Authorizable authorizable, String groupName) throws RepositoryException {
        if (StringUtils.isNotEmpty(groupName)) {
            Authorizable potentialGroup = userManager.getAuthorizable(groupName);
            if (potentialGroup.isGroup()) {
                LOG.debug("Adding member to group {}", groupName);
                ((Group) potentialGroup).addMember(authorizable);
            } else {
                LOG.warn("{} is not a group name", groupName);
                throw new IllegalStateException("Cannot assign " + authorizable.getID() + " to a group " + groupName);
            }
        } else {
            LOG.warn("Group name is empty");
        }
    }

    public static void modifyParentGroups(UserManager userManager, Authorizable authorizable, List<String> idsOfGroupsToSet)
            throws RepositoryException {
        List<Group> currentParentGroups = Lists.newArrayList(authorizable.declaredMemberOf());
        if (idsOfGroupsToSet.isEmpty()) {
            unassignFromGroups(authorizable, currentParentGroups);
            return;
        }
        unassignFromGroups(authorizable, getGroupsToUnassignFrom(currentParentGroups, idsOfGroupsToSet));
        assignToGroups(userManager, authorizable, currentParentGroups, idsOfGroupsToSet);
    }

    private static void unassignFromGroups(Authorizable authorizable, List<Group> groupsToUnassignFrom) throws RepositoryException {
        for (Group group : groupsToUnassignFrom) {
            group.removeMember(authorizable);
        }
    }

    private static List<Group> getGroupsToUnassignFrom(List<Group> currentParentGroups, List<String> idsOfGroupsToUnassignFrom)
            throws RepositoryException {
        List<Group> groupsToUnassignFrom = new ArrayList<>();
        for (Group group : currentParentGroups) {
            if (!idsOfGroupsToUnassignFrom.contains(group.getID())) {
                groupsToUnassignFrom.add(group);
            }
        }
        return groupsToUnassignFrom;
    }

    private static void assignToGroups(UserManager userManager, Authorizable authorizable, List<Group> currentParentGroups,
            List<String> idsOfGroupsToAssignTo) throws RepositoryException {
        List<String> currentParentGroupsIds = new ArrayList<>();
        for (Group parentGroup : currentParentGroups) {
            currentParentGroupsIds.add(parentGroup.getID());
        }
        for (String groupId : idsOfGroupsToAssignTo) {
            if (!currentParentGroupsIds.contains(groupId)) {
                assignToGroup(userManager, authorizable, groupId);
            }
        }
    }

}
