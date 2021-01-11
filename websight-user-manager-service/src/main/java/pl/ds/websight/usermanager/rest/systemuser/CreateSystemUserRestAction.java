package pl.ds.websight.usermanager.rest.systemuser;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.SystemUserWithGroupsDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;

import javax.jcr.RepositoryException;

@Component
@SlingAction
public class CreateSystemUserRestAction extends AbstractRestAction<SystemUserValidatableRestModel, SystemUserWithGroupsDto>
        implements RestAction<SystemUserValidatableRestModel, SystemUserWithGroupsDto> {

    private static final Logger LOG = LoggerFactory.getLogger(CreateSystemUserRestAction.class);

    @Override
    protected RestActionResult<SystemUserWithGroupsDto> performAction(SystemUserValidatableRestModel model)
            throws PersistenceException, RepositoryException {
        LOG.debug("Perform create system user action start");
        User user = createSystemUser(model);
        ResourceResolver resourceResolver = model.getResourceResolver();
        resourceResolver.commit();
        SystemUserWithGroupsDto userDto = new SystemUserWithGroupsDto(user, resourceResolver);
        LOG.debug("Perform create system user action end");
        return RestActionResult.success(
                Messages.CREATE_USER_SUCCESS,
                Messages.formatMessage(Messages.CREATE_USER_SUCCESS_DETAILS, userDto.getId()),
                userDto);
    }

    private static User createSystemUser(SystemUserValidatableRestModel model) throws RepositoryException {
        LOG.debug("Create system user {} start", model.getAuthorizableId());
        UserManager userManager = model.getUserManager();
        User systemUser = userManager.createSystemUser(model.getAuthorizableId(), null);
        LOG.debug("Create system user {} end", model.getAuthorizableId());
        return systemUser;
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.CREATE_USER_ERROR;
    }
}
