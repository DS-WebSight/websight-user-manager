package pl.ds.websight.usermanager.rest.permission;

import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.osgi.service.component.annotations.Component;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;

import javax.jcr.Session;
import javax.jcr.security.Privilege;

@SlingAction
@Component
public class CreateAclEntryRestAction extends AbstractRestAction<AclEntryRestModel, Void> implements RestAction<AclEntryRestModel, Void> {

    @Override
    protected RestActionResult<Void> performAction(AclEntryRestModel model) throws Exception {
        Session session = model.getSession();
        String absPath = model.getPath();
        if (!session.nodeExists(absPath)) {
            return RestActionResult.failure(Messages.CREATE_ACL_ENTRY_ERROR,
                    Messages.formatMessage(Messages.CREATE_ACL_ENTRY_ERROR_PATH_NOT_EXIST_DETAILS, absPath));
        }
        AccessModifyFacade acHelper = AccessModifyFacade.forAuthorizable(model.getAuthorizable(), session);
        boolean isAllow = model.isAllow();
        String rule = AclEntryRestModel.getRuleType(isAllow);
        Privilege[] privileges = AccessControlUtils.privilegesFromNames(session, model.getPrivilegesNames());
        if (acHelper.createAclEntry(absPath, privileges, isAllow, model.getRestrictions())) {
            return RestActionResult.success(Messages.CREATE_ACL_ENTRY_SUCCESS,
                    Messages.formatMessage(Messages.CREATE_ACL_ENTRY_SUCCESS_DETAILS, rule, absPath));
        }
        return RestActionResult.failure(Messages.CREATE_ACL_ENTRY_ERROR,
                Messages.formatMessage(Messages.CREATE_ACL_ENTRY_ERROR_NO_LIST_CHANGE, rule, absPath));
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.CREATE_ACL_ENTRY_ERROR;
    }
}
