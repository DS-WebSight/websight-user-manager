package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.oak.spi.security.principal.EveryonePrincipal;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GroupWithMembersDto extends GroupDto {

    private final List<BasicGroupDto> parentGroups;
    private final List<AuthorizableDto> members;

    public GroupWithMembersDto(ResourceResolver resolver, Group group) throws RepositoryException {
        super(resolver, group);
        this.parentGroups = fetchParentGroups(resolver, group);
        this.members = fetchMembers(resolver, group);
    }

    private static List<BasicGroupDto> fetchParentGroups(ResourceResolver resolver, Group group) throws RepositoryException {
        List<BasicGroupDto> parentGroups = new ArrayList<>();
        for (Iterator<Group> iterator = group.declaredMemberOf(); iterator.hasNext(); ) {
            Group parentGroup = iterator.next();
            parentGroups.add(new BasicGroupDto(resolver, parentGroup));
        }
        return parentGroups;
    }

    private static List<AuthorizableDto> fetchMembers(ResourceResolver resourceResolver, Group group) throws RepositoryException {
        if (EveryonePrincipal.NAME.equals(group.getID())) {
            return Collections.emptyList();
        }
        List<AuthorizableDto> groupMembers = new ArrayList<>();
        Iterator<Authorizable> membersIterator = group.getDeclaredMembers();
        while (membersIterator.hasNext()) {
            Authorizable authorizable = membersIterator.next();
            if (authorizable.isGroup()) {
                groupMembers.add(new BasicGroupDto(resourceResolver, (Group) authorizable));
            } else {
                User user = (User) authorizable;
                if (user.isSystemUser()) {
                    groupMembers.add(new BasicSystemUserDto(resourceResolver, user));
                } else {
                    groupMembers.add(new BasicUserDto(resourceResolver, user));
                }
            }
        }
        return groupMembers;
    }

    public List<BasicGroupDto> getParentGroups() {
        return parentGroups;
    }

    public List<AuthorizableDto> getMembers() {
        return members;
    }
}
