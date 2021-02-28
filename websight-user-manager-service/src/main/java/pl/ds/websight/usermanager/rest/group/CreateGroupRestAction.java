package pl.ds.websight.usermanager.rest.group;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.GroupWithMembersDto;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.util.AuthorizableUtil;

import javax.jcr.RepositoryException;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Component
@SlingAction
public class CreateGroupRestAction extends GroupBaseAction<CreateGroupRestModel, GroupWithMembersDto>
        implements RestAction<CreateGroupRestModel, GroupWithMembersDto> {

    private static final Logger LOG = LoggerFactory.getLogger(CreateGroupRestAction.class);

    @Override
    protected RestActionResult<GroupWithMembersDto> performAction(CreateGroupRestModel model)
            throws RepositoryException, PersistenceException {
        LOG.debug("Create group action begin");
        Group group = createGroup(model);
        ResourceResolver resourceResolver = model.getResourceResolver();
        resourceResolver.commit();
        GroupWithMembersDto groupDto = new GroupWithMembersDto(resourceResolver, group);
        LOG.debug("Create group action end");
        return RestActionResult.success(
                Messages.CREATE_GROUP_SUCCESS,
                Messages.formatMessage(Messages.CREATE_GROUP_SUCCESS_DETAILS, groupDto.getId()),
                groupDto);
    }

    private Group createGroup(CreateGroupRestModel model) throws RepositoryException {
        LOG.debug("Create group {} start", model.getAuthorizableId());
        UserManager userManager = model.getUserManager();
        Group group = userManager.createGroup(model.getAuthorizableId());
        AuthorizableUtil.assignToGroups(userManager, group, model.getParentGroups());
        addMembers(userManager, group, model.getMembers());
        updateGroupProperties(model, group);
        LOG.debug("Create group {} end", model.getAuthorizableId());
        return group;
    }

    private void addMembers(UserManager userManager, Group group, List<String> members) throws RepositoryException {
        for (String memberName : members) {
            Authorizable member = requireNonNull(userManager.getAuthorizable(memberName), "Cannot find member " + memberName);
            group.addMember(member);
        }
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.CREATE_GROUP_ERROR;
    }
}
