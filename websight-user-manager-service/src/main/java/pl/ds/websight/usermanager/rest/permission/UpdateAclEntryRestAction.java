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
public class UpdateAclEntryRestAction extends AbstractRestAction<UpdateAclEntryRestModel, Void>
        implements RestAction<UpdateAclEntryRestModel, Void> {

    @Override
    protected RestActionResult<Void> performAction(UpdateAclEntryRestModel model) throws Exception {
        Session session = model.getSession();
        String absPath = model.getPath();
        if (!session.nodeExists(absPath)) {
            return RestActionResult.failure(Messages.UPDATE_ACL_ENTRY_ERROR,
                    Messages.formatMessage(Messages.UPDATE_ACL_ENTRY_ERROR_PATH_NOT_EXIST_DETAILS, absPath));
        }
        AccessModifyFacade acHelper = AccessModifyFacade.forAuthorizable(model.getAuthorizable(), model.getSession());
        String[] privilegesNames = model.getPrivilegesNames();
        Privilege[] privileges = AccessControlUtils.privilegesFromNames(session, privilegesNames);
        boolean updateResult = acHelper.updateAclEntry(model.getEntryId(), model.getPolicyId(), absPath, privileges, model.isAllow(),
                model.getRestrictions());
        return updateResult ?
                RestActionResult.success(Messages.UPDATE_ACL_ENTRY_SUCCESS,
                        Messages.formatMessage(Messages.UPDATE_ACL_ENTRY_SUCCESS_DETAILS, AclEntryRestModel.getRuleType(model.isAllow()),
                                absPath)) :
                RestActionResult.failure(Messages.UPDATE_ACL_ENTRY_ERROR, Messages.UPDATE_ACL_ENTRY_ERROR_ACL_ENTRY_NOT_FOUND_DETAILS);
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.UPDATE_ACL_ENTRY_ERROR;
    }
}
