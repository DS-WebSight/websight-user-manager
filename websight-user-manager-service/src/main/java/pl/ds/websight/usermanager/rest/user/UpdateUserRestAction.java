package pl.ds.websight.usermanager.rest.user;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
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
import pl.ds.websight.usermanager.util.PropertiesUtil;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
@SlingAction
public class UpdateUserRestAction extends AbstractRestAction<UpdateUserRestModel, UserWithGroupsDto>
        implements RestAction<UpdateUserRestModel, UserWithGroupsDto> {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateUserRestAction.class);

    @Override
    protected RestActionResult<UserWithGroupsDto> performAction(UpdateUserRestModel model)
            throws PersistenceException, RepositoryException {
        LOG.debug("Start update user {} ", model.getAuthorizableId());
        Authorizable authorizable = model.getAuthorizable();
        if (authorizable.isGroup()) {
            return RestActionResult.failure(
                    Messages.UPDATE_USER_ERROR,
                    Messages.formatMessage(Messages.UPDATE_USER_ERROR_USER_IS_GROUP_DETAILS, model.getAuthorizableId()));
        }
        User user = (User) authorizable;
        if (user.isSystemUser()) {
            return RestActionResult.failure(
                    Messages.UPDATE_USER_ERROR,
                    Messages.formatMessage(Messages.UPDATE_USER_ERROR_SYSTEM_USER_DETAILS, model.getAuthorizableId()));
        }
        User updatedUser = updateUser(model);
        ResourceResolver resourceResolver = model.getResourceResolver();
        resourceResolver.commit();
        UserWithGroupsDto userDto = new UserWithGroupsDto(resourceResolver, updatedUser);
        LOG.debug("End update user {} ", model.getAuthorizableId());
        return RestActionResult.success(
                Messages.UPDATE_USER_SUCCESS,
                Messages.formatMessage(Messages.UPDATE_USER_SUCCESS_DETAILS, userDto.getId()),
                userDto);
    }

    private User updateUser(UpdateUserRestModel model) throws RepositoryException, PersistenceException {
        LOG.debug("Update user {} start", model.getAuthorizableId());
        Authorizable authorizable = model.getAuthorizable();
        User user = (User) authorizable;
        if (model.isChangingPassword() && StringUtils.isNotBlank(model.getPassword())) {
            user.changePassword(model.getPassword());
        }
        List<String> updatedGroups = model.getGroups();
        AuthorizableUtil.modifyParentGroups(model.getUserManager(), user, updatedGroups);
        Boolean enabled = model.isEnabled();
        if (enabledStatusUpdated(enabled, user)) {
            if (enabled) {
                user.disable(null);
            } else {
                user.disable("User disabled by " + model.getSession().getUserID());
            }
        }
        updateUserProfile(model, user);
        LOG.debug("Update user {} end", model.getAuthorizableId());
        return user;
    }

    private boolean enabledStatusUpdated(Boolean enabled, User user) throws RepositoryException {
        return enabled != null && user.isDisabled() == enabled;
    }

    private void updateUserProfile(UpdateUserRestModel model, User user) throws RepositoryException, PersistenceException {
        Resource profileResource = AuthorizableUtil.getOrCreateUserProfileResource(model.getResourceResolver(), user,
                model.getUserProfileProperties());
        ModifiableValueMap profileValueMap = profileResource.adaptTo(ModifiableValueMap.class);
        if (profileValueMap != null) {
            Map<String, Object> properties = model.getUserProfileProperties();
            PropertiesUtil.putEveryIfChangedIgnoreNulls(profileValueMap, properties);
        }
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.UPDATE_USER_ERROR;
    }

}
