package pl.ds.websight.usermanager.rest.user;

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
import pl.ds.websight.usermanager.dto.UserWithGroupsDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.util.AuthorizableUtil;

import javax.jcr.RepositoryException;

@Component
@SlingAction
public class CreateUserRestAction extends AbstractRestAction<CreateUserRestModel, UserWithGroupsDto>
        implements RestAction<CreateUserRestModel, UserWithGroupsDto> {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserRestAction.class);

    @Override
    protected RestActionResult<UserWithGroupsDto> performAction(CreateUserRestModel model)
            throws PersistenceException, RepositoryException {
        LOG.debug("Perform create user action start");
        User user = createUser(model);
        ResourceResolver resourceResolver = model.getResourceResolver();
        resourceResolver.commit();
        UserWithGroupsDto userDto = new UserWithGroupsDto(resourceResolver, user);
        LOG.debug("Perform create user action end");
        return RestActionResult.success(
                Messages.CREATE_USER_SUCCESS,
                Messages.formatMessage(Messages.CREATE_USER_SUCCESS_DETAILS, userDto.getId()),
                userDto);
    }

    private User createUser(CreateUserRestModel model) throws RepositoryException, PersistenceException {
        LOG.debug("Create user {} start", model.getAuthorizableId());
        UserManager userManager = model.getUserManager();
        User user = userManager.createUser(model.getAuthorizableId(), model.getPassword());
        AuthorizableUtil.getOrCreateUserProfileResource(model.getResourceResolver(), user, model.getUserProfileProperties());
        AuthorizableUtil.assignToGroups(userManager, user, model.getGroups());
        if (Boolean.FALSE.equals(model.isEnabled())) {
            user.disable("User disabled by " + model.getSession().getUserID());
        }
        LOG.debug("Create user {} end", model.getAuthorizableId());
        return user;
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.CREATE_USER_ERROR;
    }
}
