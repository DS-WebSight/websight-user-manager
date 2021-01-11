package pl.ds.websight.usermanager.rest.permission;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.jackrabbit.oak.spi.security.authorization.accesscontrol.AccessControlConstants;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.PermissionsDto;
import pl.ds.websight.usermanager.dto.PermissionsDto.Rule;
import pl.ds.websight.usermanager.dto.PermissionsDto.Rules;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.rest.requestparameters.Action;
import pl.ds.websight.usermanager.util.JcrSecurityUtil;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;

@Component
@SlingAction(GET)
public class GetPermissionsRestAction extends AbstractRestAction<GetPermissionsRestModel, List<PermissionsDto>>
        implements RestAction<GetPermissionsRestModel, List<PermissionsDto>> {

    private static final Logger LOG = LoggerFactory.getLogger(GetPermissionsRestAction.class);

    @Override
    protected RestActionResult<List<PermissionsDto>> performAction(GetPermissionsRestModel model) throws RepositoryException {
        List<String> paths = model.getPaths();
        Session session = model.getSession();
        List<PermissionsDto> permissions = new LinkedList<>();
        for (String path : paths) {
            Node rootNode = session.getNode(path);
            if (rootNode == null) {
                LOG.warn("Could not get permissions. Node at {} does not exist", path);
                return RestActionResult.failure(Messages.GET_PERMISSIONS_ERROR,
                        Messages.formatMessage(Messages.GET_PERMISSIONS_ERROR_NODE_NOT_FOUND_DETAILS, path));
            }

            Set<Principal> principals = model.getPrincipals();
            PermissionsDto permissionDto = createPermissionDto(session, principals, rootNode);
            NodeIterator rootChildrenIterator = rootNode.getNodes();
            while (rootChildrenIterator.hasNext()) {
                Node child = rootChildrenIterator.nextNode();
                if (isValidNode(child)) {
                    PermissionsDto childPermissionDto = createPermissionDto(session, principals, child);
                    permissionDto.addChild(childPermissionDto);
                }
            }
            if (permissionDto.isHasChildren() && shouldSortAlphabetically(rootNode, path)) {
                permissionDto.getChildren().sort(Comparator.comparing(PermissionsDto::getName, String.CASE_INSENSITIVE_ORDER));
            }
            permissions.add(permissionDto);
        }
        return RestActionResult.success(permissions);
    }

    private static PermissionsDto createPermissionDto(Session session, Set<Principal> principals, Node node)
            throws RepositoryException {
        String path = node.getPath();
        AccessControlManager acManager = session.getAccessControlManager();
        Map<String, Boolean> actions = getActions(acManager, path, principals);
        Map<String, Rules> declaredActions = getDeclaredActionRules(session, actions, principals, path);
        return new PermissionsDto(node.getName(), path, hasChildren(node), actions, declaredActions);
    }

    private static Map<String, Boolean> getActions(AccessControlManager acManager, String path, Set<Principal> principals)
            throws RepositoryException {
        Map<String, Boolean> actions = new HashMap<>();
        Set<String> allowedActions = getAllowedActions(acManager, path, principals);
        for (Action action : Action.values()) {
            actions.put(action.getName(), allowedActions.contains(action.getName()));
        }
        return actions;
    }

    private static Set<String> getAllowedActions(AccessControlManager acManager, String path, Set<Principal> principals)
            throws RepositoryException {
        Set<String> allowedActions = new HashSet<>();
        Set<Privilege> nodePrivileges = getPrivileges(acManager, path, principals);
        for (Action action : Action.values()) {
            Set<Privilege> requiredPrivileges = getAggregateRequiredPrivileges(acManager, action);
            if (nodePrivileges.containsAll(requiredPrivileges)) {
                allowedActions.add(action.getName());
            }
        }
        return allowedActions;
    }

    private static Set<Privilege> getPrivileges(AccessControlManager acManager, String path, Set<Principal> principals)
            throws RepositoryException {
        Privilege[] privileges;
        if (principals.isEmpty()) {
            privileges = acManager.getPrivileges(path);
        } else {
            JackrabbitAccessControlManager jackrabbitAcManager = (JackrabbitAccessControlManager) acManager;
            privileges = jackrabbitAcManager.getPrivileges(path, principals);
        }
        return getAggregatePrivileges(privileges);
    }

    private static Map<String, Rules> getDeclaredActionRules(Session session, Map<String, Boolean> actions, Set<Principal> principals,
            String path) throws RepositoryException {
        Map<String, Rules> declaredActionRules = new HashMap<>();
        AccessControlList aclPolicy = AccessControlUtils.getAccessControlList(session, path);
        for (AccessControlEntry acEntry : aclPolicy.getAccessControlEntries()) {
            addRulesFromAcEntry(declaredActionRules, session, actions, principals, acEntry);
        }
        return declaredActionRules;
    }

    private static void addRulesFromAcEntry(Map<String, Rules> declaredActionRules, Session session, Map<String, Boolean> actions,
            Set<Principal> principals, AccessControlEntry acEntry) throws RepositoryException {
        UserManager userManager = AccessControlUtil.getUserManager(session);
        AccessControlManager acManager = session.getAccessControlManager();

        Principal acEntryPrincipal = acEntry.getPrincipal();
        Authorizable authorizable = userManager.getAuthorizable(acEntryPrincipal);
        if (principals.contains(acEntryPrincipal)) {
            boolean isAllow = isDeclaredAllow(acEntry);
            boolean isUnrestricted = isUnrestricted(acEntry);

            Set<Privilege> acEntryPrivileges = getAggregatePrivileges(acEntry.getPrivileges());
            for (Action action : Action.values()) {
                Set<Privilege> requiredPrivileges = getAggregateRequiredPrivileges(acManager, action);
                if (acEntryPrivileges.containsAll(requiredPrivileges)) {
                    boolean isEffective = (isAllow == actions.get(action.getName())) && isUnrestricted;
                    Set<Rule> declaredRules = getOrCreateDeclaredRules(declaredActionRules, action.getName(), isEffective);
                    Map<String, List<String>> allRestrictions = JcrSecurityUtil.getRestrictions(acEntry, true);
                    if (authorizable == null) {
                        declaredRules.add(new Rule(acEntryPrincipal.getName(), isAllow, true, allRestrictions));
                    } else {
                        declaredRules.add(new Rule(authorizable.getID(), isAllow, authorizable.isGroup(), allRestrictions));
                    }
                }
            }
        }
    }

    private static boolean isDeclaredAllow(AccessControlEntry acEntry) {
        if (acEntry instanceof JackrabbitAccessControlEntry) {
            JackrabbitAccessControlEntry jacEntry = (JackrabbitAccessControlEntry) acEntry;
            return jacEntry.isAllow();
        }
        return true;
    }

    private static boolean isUnrestricted(AccessControlEntry acEntry) throws RepositoryException {
        if (acEntry instanceof JackrabbitAccessControlEntry) {
            JackrabbitAccessControlEntry jacEntry = (JackrabbitAccessControlEntry) acEntry;
            if (ArrayUtils.isNotEmpty(jacEntry.getRestrictionNames())) {
                Value repGlob = jacEntry.getRestriction(AccessControlConstants.REP_GLOB);
                return repGlob != null && (StringUtils.isEmpty(repGlob.getString()) || StringUtils.equals(repGlob.getString(), "*"));
            }
        }
        return true;
    }

    private static Set<Privilege> getAggregatePrivileges(Privilege[] privileges) {
        Set<Privilege> aggregatePrivileges = new HashSet<>();
        for (Privilege privilege : privileges) {
            addAggregatePrivileges(aggregatePrivileges, privilege);
        }
        return aggregatePrivileges;
    }

    private static Set<Privilege> getAggregateRequiredPrivileges(AccessControlManager acManager, Action action)
            throws RepositoryException {
        Set<Privilege> aggregatePrivileges = new HashSet<>();
        for (String name : action.getRequiredPrivileges()) {
            Privilege privilege = acManager.privilegeFromName(name);
            addAggregatePrivileges(aggregatePrivileges, privilege);
        }
        return aggregatePrivileges;
    }

    private static void addAggregatePrivileges(Set<Privilege> privileges, Privilege privilege) {
        if (privilege.isAggregate()) {
            privileges.addAll(Arrays.asList(privilege.getAggregatePrivileges()));
        } else {
            privileges.add(privilege);
        }
    }

    private static Set<Rule> getOrCreateDeclaredRules(Map<String, Rules> declaredActionRules, String action, boolean isEffective) {
        declaredActionRules.putIfAbsent(action, new Rules());
        Rules actionRules = declaredActionRules.get(action);
        return isEffective ? actionRules.getEffective() : actionRules.getIneffective();
    }

    private static boolean hasChildren(Node node) throws RepositoryException {
        if (node.hasNodes()) {
            NodeIterator childrenIterator = node.getNodes();
            while (childrenIterator.hasNext()) {
                if (isValidNode(childrenIterator.nextNode())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isValidNode(Node node) throws RepositoryException {
        boolean isValidNodeType = node.isNodeType(JcrConstants.NT_HIERARCHYNODE) || node.isNodeType(JcrConstants.NT_UNSTRUCTURED);
        boolean definesContent = Arrays.stream(node.getPrimaryNodeType().getChildNodeDefinitions())
                .anyMatch(child -> StringUtils.equals(child.getName(), JcrConstants.JCR_CONTENT));
        return isValidNodeType && (!StringUtils.equals(node.getName(), JcrConstants.JCR_CONTENT) || definesContent);
    }

    public static boolean shouldSortAlphabetically(Node node, String path) {
        if (node != null) {
            try {
                return !hasOrderableChildNodes(node);
            } catch (RepositoryException e) {
                LOG.warn("Could not get Node Type Definition for Node: {}", path, e);
            }
        }
        return true;
    }

    private static boolean hasOrderableChildNodes(Node node) throws RepositoryException {
        return node.getPrimaryNodeType().hasOrderableChildNodes() ||
                Arrays.stream(node.getMixinNodeTypes()).anyMatch(NodeTypeDefinition::hasOrderableChildNodes);
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.GET_PERMISSIONS_ERROR;
    }

}
