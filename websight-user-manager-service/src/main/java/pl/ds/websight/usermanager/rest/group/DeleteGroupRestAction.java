package pl.ds.websight.usermanager.rest.group;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.resource.PersistenceException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;

import javax.jcr.RepositoryException;

@Component
@SlingAction
public class DeleteGroupRestAction extends AbstractRestAction<DeleteGroupRestModel, Void>
        implements RestAction<DeleteGroupRestModel, Void> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteGroupRestAction.class);

    @Override
    protected RestActionResult<Void> performAction(DeleteGroupRestModel model) throws RepositoryException, PersistenceException {
        LOG.debug("Start of delete group");
        Authorizable authorizable = model.getAuthorizable();
        authorizable.remove();
        model.getResourceResolver().commit();
        LOG.debug("End of delete group");
        return RestActionResult.success(Messages.DELETE_GROUP_SUCCESS,
                Messages.formatMessage(Messages.DELETE_GROUP_SUCCESS_DETAILS, model.getAuthorizableId()));
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.DELETE_GROUP_ERROR;
    }

}
