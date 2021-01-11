package pl.ds.websight.usermanager.rest.group;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.GroupWithMembersDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;

import javax.jcr.RepositoryException;

import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;

@Component
@SlingAction(GET)
public class GetGroupRestAction extends AbstractRestAction<GetGroupRestModel, GroupWithMembersDto>
        implements RestAction<GetGroupRestModel, GroupWithMembersDto> {

    private static final Logger LOG = LoggerFactory.getLogger(GetGroupRestAction.class);

    @Override
    protected RestActionResult<GroupWithMembersDto> performAction(GetGroupRestModel model) throws RepositoryException {
        LOG.debug("Start fetch get group {}", model.getAuthorizableId());
        Authorizable authorizable = model.getAuthorizable();
        Group group = (Group) authorizable;
        GroupWithMembersDto groupDto = new GroupWithMembersDto(model.getResourceResolver(), group);
        LOG.debug("End of fetch group {}", model.getAuthorizableId());
        return RestActionResult.success(groupDto);
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.GET_GROUP_ERROR;
    }
}
