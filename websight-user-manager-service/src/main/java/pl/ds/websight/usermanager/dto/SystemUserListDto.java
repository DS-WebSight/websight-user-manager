package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

public class SystemUserListDto {

    private final List<SystemUserWithGroupsDto> systemUsers;

    private final long numberOfFoundUsers;

    private final long numberOfPages;

    public SystemUserListDto(List<User> users, ResourceResolver resourceResolver, long numberOfFoundUsers, long numberOfPages)
            throws RepositoryException {
        this.systemUsers = fetchUsers(resourceResolver, users);
        this.numberOfFoundUsers = numberOfFoundUsers;
        this.numberOfPages = numberOfPages;
    }

    private static List<SystemUserWithGroupsDto> fetchUsers(ResourceResolver resourceResolver, List<User> users) throws
            RepositoryException {
        List<SystemUserWithGroupsDto> usersDtos = new ArrayList<>();
        for (User user : users) {
            usersDtos.add(new SystemUserWithGroupsDto(user, resourceResolver));
        }
        return usersDtos;
    }

    public List<SystemUserWithGroupsDto> getSystemUsers() {
        return systemUsers;
    }

    public long getNumberOfFoundUsers() {
        return numberOfFoundUsers;
    }

    public long getNumberOfPages() {
        return numberOfPages;
    }
}
