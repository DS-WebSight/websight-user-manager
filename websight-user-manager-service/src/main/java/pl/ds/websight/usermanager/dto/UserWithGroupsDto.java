package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserWithGroupsDto extends UserDto {

    private final List<BasicGroupDto> groups;

    public UserWithGroupsDto(ResourceResolver resourceResolver, User user) throws RepositoryException {
        super(resourceResolver, user);
        this.groups = fetchGroups(resourceResolver, user);
    }

    private static List<BasicGroupDto> fetchGroups(ResourceResolver resourceResolver, User user) throws RepositoryException {
        List<BasicGroupDto> result = new ArrayList<>();
        Iterator<Group> groupIterator = user.declaredMemberOf();
        while (groupIterator.hasNext()) {
            Group group = groupIterator.next();
            result.add(new BasicGroupDto(resourceResolver, group));
        }
        return result;
    }

    public List<BasicGroupDto> getGroups() {
        return groups;
    }
}
