package pl.ds.websight.usermanager.rest.group;

import com.google.common.collect.Lists;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@SlingAction
public class UpdateGroupRestAction extends GroupBaseAction<UpdateGroupRestModel, GroupWithMembersDto>
        implements RestAction<UpdateGroupRestModel, GroupWithMembersDto> {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateGroupRestAction.class);

    @Override
    protected RestActionResult<GroupWithMembersDto> performAction(UpdateGroupRestModel model)
            throws RepositoryException, PersistenceException {
        LOG.debug("Start update group {} ", model.getAuthorizableId());
        Group group = updateGroup(model);
        ResourceResolver resourceResolver = model.getResourceResolver();
        resourceResolver.commit();
        GroupWithMembersDto groupDto = new GroupWithMembersDto(resourceResolver, group);
        LOG.debug("End update group {} ", model.getAuthorizableId());
        return RestActionResult.success(
                Messages.UPDATE_GROUP_SUCCESS,
                Messages.formatMessage(Messages.UPDATE_GROUP_SUCCESS_DETAILS, groupDto.getId()),
                groupDto);
    }

    private Group updateGroup(GroupBaseModel model) throws RepositoryException {
        LOG.debug("Update group {} start", model.getAuthorizableId());
        Authorizable authorizable = model.getAuthorizable();
        Group group = (Group) authorizable;
        AuthorizableUtil.modifyParentGroups(model.getUserManager(), group, model.getParentGroups());
        modifyGroupMembers(model, group);
        updateGroupProperties(model, group);
        LOG.debug("Update group {} end", model.getAuthorizableId());
        return group;
    }

    private void modifyGroupMembers(GroupBaseModel model, Group group) throws RepositoryException {
        List<String> requestedMembers = model.getMembers();
        if (requestedMembers.isEmpty()) {
            removeAllMembers(group);
            return;
        }
        List<Authorizable> groupMembers = Lists.newArrayList(group.getDeclaredMembers());
        List<Authorizable> authorizablesToAdd = getAuthorizablesToAdd(model, requestedMembers, groupMembers);
        List<Authorizable> authorizablesToRemove = getAuthorizablesToRemove(requestedMembers, groupMembers);
        for (Authorizable authorizable : authorizablesToAdd) {
            group.addMember(authorizable);
        }
        for (Authorizable authorizable : authorizablesToRemove) {
            group.removeMember(authorizable);
        }
    }

    private void removeAllMembers(Group group) throws RepositoryException {
        Iterator<Group> declaredMembers = group.declaredMemberOf();
        while (declaredMembers.hasNext()) {
            Group member = declaredMembers.next();
            group.removeMember(member);
        }
    }

    private List<Authorizable> getAuthorizablesToAdd(GroupBaseModel model, List<String> requestedMembers, List<Authorizable> groupMembers)
            throws RepositoryException {
        List<String> groupMemberNames = new ArrayList<>();
        UserManager userManager = model.getUserManager();
        for (Authorizable groupMember : groupMembers) {
            groupMemberNames.add(groupMember.getID());
        }
        List<Authorizable> toAdd = new ArrayList<>();
        for (String memberName : requestedMembers) {
            if (!groupMemberNames.contains(memberName)) {
                Authorizable authorizable = userManager.getAuthorizable(memberName);
                toAdd.add(authorizable);
            }
        }
        return toAdd;
    }

    private List<Authorizable> getAuthorizablesToRemove(List<String> requestedMembers, List<Authorizable> groupMembers)
            throws RepositoryException {
        List<Authorizable> toRemove = new ArrayList<>();
        for (Authorizable member : groupMembers) {
            String memberName = member.getID();
            if (!requestedMembers.contains(memberName)) {
                toRemove.add(member);
            }
        }
        return toRemove;
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.UPDATE_GROUP_ERROR;
    }
}
