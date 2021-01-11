package pl.ds.websight.usermanager.rest.user;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.PersistenceException;
import org.osgi.service.component.annotations.Component;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.UserDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.util.AuthorizableUtil;

import javax.jcr.RepositoryException;

@Component
@SlingAction
public class ToggleUserStatusRestAction extends AbstractRestAction<AuthorizableBaseModel, UserDto>
        implements RestAction<AuthorizableBaseModel, UserDto> {

    @Override
    protected RestActionResult<UserDto> performAction(AuthorizableBaseModel model) throws RepositoryException, PersistenceException {
        String authorizableId = model.getAuthorizableId();
        Authorizable authorizable = model.getAuthorizable();
        if (authorizable == null) {
            return RestActionResult.failure(
                    Messages.TOGGLE_USER_STATUS_UNKNOWN_ERROR,
                    Messages.formatMessage(Messages.TOGGLE_USER_STATUS_ERROR_DOES_NOT_EXIST_DETAILS, authorizableId));
        }
        if (authorizable.isGroup()) {
            return RestActionResult.failure(
                    Messages.TOGGLE_USER_STATUS_UNKNOWN_ERROR,
                    Messages.formatMessage(Messages.TOGGLE_USER_STATUS_ERROR_USER_IS_GROUP_DETAILS, authorizableId));
        }
        User user = (User) model.getAuthorizable();
        boolean isEnablingRequested = user.isDisabled();
        String requestedActionName = isEnablingRequested ? "enable" : "disable";
        if (AuthorizableUtil.isProtected(authorizableId)) {
            return RestActionResult.failure(
                    Messages.formatMessage(Messages.TOGGLE_USER_STATUS_ERROR, requestedActionName),
                    Messages.formatMessage(Messages.TOGGLE_USER_STATUS_ERROR_PROTECTED_USER_DETAILS, authorizableId));
        }
        if (user.isSystemUser()) {
            return RestActionResult.failure(
                    Messages.formatMessage(Messages.TOGGLE_USER_STATUS_ERROR, requestedActionName),
                    Messages.formatMessage(Messages.TOGGLE_USER_STATUS_ERROR_SYSTEM_USER_DETAILS, authorizableId));
        }
        String finalStatusName;
        if (isEnablingRequested) {
            user.disable(null);
            finalStatusName = "enabled";
        } else {
            user.disable("Disabled by user-manager");
            finalStatusName = "disabled";
        }
        model.getResourceResolver().commit();
        UserDto userDto = new UserDto(model.getResourceResolver(), user);
        return RestActionResult.success(
                Messages.formatMessage(Messages.TOGGLE_USER_STATUS_SUCCESS, finalStatusName),
                Messages.formatMessage(Messages.TOGGLE_USER_STATUS_SUCCESS_DETAILS, userDto.getId(), finalStatusName),
                userDto);
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.TOGGLE_USER_STATUS_UNKNOWN_ERROR;
    }

}
