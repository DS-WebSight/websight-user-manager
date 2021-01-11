package pl.ds.websight.usermanager.rest.user;

import org.apache.jackrabbit.api.security.user.User;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.UserWithGroupsDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;

import javax.jcr.RepositoryException;

import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;

@Component
@SlingAction(GET)
public class GetUserRestAction extends AbstractRestAction<GetUserRestModel, UserWithGroupsDto>
        implements RestAction<GetUserRestModel, UserWithGroupsDto> {

    private static final Logger LOG = LoggerFactory.getLogger(GetUserRestAction.class);

    @Override
    protected RestActionResult<UserWithGroupsDto> performAction(GetUserRestModel model) throws RepositoryException {
        LOG.debug("Start of get authorizable {}", model.getAuthorizableId());
        User user = (User) model.getAuthorizable();
        UserWithGroupsDto userDto = new UserWithGroupsDto(model.getResourceResolver(), user);
        LOG.debug("End of get authorizable {}", model.getAuthorizableId());
        return RestActionResult.success(userDto);
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.GET_USER_ERROR;
    }

}
