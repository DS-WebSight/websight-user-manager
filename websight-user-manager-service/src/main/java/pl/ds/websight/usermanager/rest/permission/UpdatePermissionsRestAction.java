package pl.ds.websight.usermanager.rest.permission;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.rest.requestparameters.Action;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static pl.ds.websight.usermanager.rest.permission.UpdatePermissionsRestModel.Record;

@Component
@SlingAction
public class UpdatePermissionsRestAction extends AbstractRestAction<UpdatePermissionsRestModel, Void>
        implements RestAction<UpdatePermissionsRestModel, Void> {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePermissionsRestAction.class);

    @Override
    protected RestActionResult<Void> performAction(UpdatePermissionsRestModel model) throws RepositoryException {
        Session session = model.getSession();
        Principal principal = model.getAuthorizable().getPrincipal();
        for (Record record : model.getRecords()) {
            String path = record.getPath();
            if (!session.nodeExists(path)) {
                LOG.warn("Could not update permissions. Node at {} does not exist", path);
                return RestActionResult.failure(Messages.UPDATE_PERMISSIONS_ERROR,
                        Messages.formatMessage(Messages.UPDATE_PERMISSIONS_ERROR_NODE_NOT_FOUND_DETAILS, path));
            }
            updatePermissions(session, record, principal);
        }
        session.save();
        return RestActionResult.success(Messages.UPDATE_PERMISSIONS_SUCCESS,
                Messages.formatMessage(Messages.UPDATE_PERMISSIONS_SUCCESS_DETAILS, model.getAuthorizableId()));
    }

    private static void updatePermissions(Session session, Record record, Principal principal) throws RepositoryException {
        String path = record.getPath();
        AccessControlManager acManager = session.getAccessControlManager();
        JackrabbitAccessControlList acl = AccessControlUtils.getAccessControlList(session, path);
        for (Map.Entry<Action, Boolean> entry : record.getActions().entrySet()) {
            Boolean isAllow = entry.getValue();
            Privilege[] actionPrivileges = getPrivileges(session, entry.getKey().getRequiredPrivileges());
            if (isAllow == null) {
                JackrabbitAccessControlEntry[] aclEntries = (JackrabbitAccessControlEntry[]) acl.getAccessControlEntries();
                for (JackrabbitAccessControlEntry aclEntry : aclEntries) {
                    if (aclEntry.getPrincipal().equals(principal)) {
                        removePrivilegesFromEntry(acl, aclEntry, Arrays.asList(actionPrivileges), principal);
                    }
                }
            } else {
                acl.addEntry(principal, actionPrivileges, isAllow);
            }
        }
        acManager.setPolicy(path, acl);
    }

    private static void removePrivilegesFromEntry(JackrabbitAccessControlList acl, JackrabbitAccessControlEntry aclEntry,
            List<Privilege> actionPrivileges, Principal principal) throws RepositoryException {
        Privilege[] aclEntryPrivileges = aclEntry.getPrivileges();
        if (aclEntryPrivileges != null && Arrays.asList(aclEntryPrivileges).containsAll(actionPrivileges)) {
            Privilege[] nonActionPrivileges = Arrays.stream(aclEntryPrivileges)
                    .filter(privilege -> !actionPrivileges.contains(privilege))
                    .toArray(Privilege[]::new);
            acl.removeAccessControlEntry(aclEntry);
            if (nonActionPrivileges.length > 0) {
                acl.addEntry(principal, nonActionPrivileges, aclEntry.isAllow());
            }
        }
    }

    private static Privilege[] getPrivileges(Session session, List<String> privilegeNames) throws RepositoryException {
        return AccessControlUtils.privilegesFromNames(session, privilegeNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.UPDATE_PERMISSIONS_ERROR;
    }
}
