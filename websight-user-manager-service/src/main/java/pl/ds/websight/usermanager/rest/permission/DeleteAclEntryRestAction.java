package pl.ds.websight.usermanager.rest.permission;

import org.osgi.service.component.annotations.Component;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;

@SlingAction
@Component
public class DeleteAclEntryRestAction extends AbstractRestAction<DeleteAclEntryRestModel, Void>
        implements RestAction<DeleteAclEntryRestModel, Void> {

    @Override
    protected RestActionResult<Void> performAction(DeleteAclEntryRestModel model) throws Exception {
        AccessModifyFacade acHelper = AccessModifyFacade.forAuthorizable(model.getAuthorizable(), model.getSession());
        return acHelper.removeAclEntry(model.getEntryId(), model.getPolicyId()) ?
                RestActionResult.success(Messages.DELETE_ACL_ENTRY_SUCCESS,
                        Messages.formatMessage(Messages.DELETE_ACL_ENTRY_SUCCESS_DETAILS, model.getAuthorizableId())) :
                RestActionResult.failure(Messages.DELETE_ACL_ENTRY_ERROR, Messages.DELETE_ACL_ENTRY_ERROR_ACL_ENTRY_NOT_FOUND_DETAILS);
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.DELETE_ACL_ENTRY_ERROR;
    }
}
