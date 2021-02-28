package pl.ds.websight.usermanager.rest.systemuser;

import org.apache.jackrabbit.api.security.user.User;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.SystemUserWithGroupsDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.rest.user.GetUserRestModel;

import javax.jcr.RepositoryException;

import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;

@Component
@SlingAction(GET)
public class GetSystemUserRestAction extends AbstractRestAction<GetSystemUserRestModel, SystemUserWithGroupsDto>
        implements RestAction<GetSystemUserRestModel, SystemUserWithGroupsDto> {

    private static final Logger LOG = LoggerFactory.getLogger(GetSystemUserRestAction.class);

    @Override
    protected RestActionResult<SystemUserWithGroupsDto> performAction(GetSystemUserRestModel model) throws RepositoryException {
        LOG.debug("Start of get authorizable {}", model.getAuthorizableId());
        SystemUserWithGroupsDto authorizableDto = new SystemUserWithGroupsDto((User) model.getAuthorizable(), model.getResourceResolver());
        LOG.debug("End of get authorizable {}", model.getAuthorizableId());
        return RestActionResult.success(authorizableDto);
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.GET_USER_ERROR;
    }

}
