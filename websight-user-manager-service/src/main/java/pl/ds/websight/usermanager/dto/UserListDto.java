package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

public class UserListDto {

    private final List<UserWithGroupsDto> users;

    private final long numberOfFoundUsers;

    private final long numberOfPages;

    public UserListDto(List<User> users, ResourceResolver resourceResolver, long numberOfFoundUsers, long numberOfPages)
            throws RepositoryException {
        this.users = fetchUsersWithGroups(resourceResolver, users);
        this.numberOfFoundUsers = numberOfFoundUsers;
        this.numberOfPages = numberOfPages;
    }

    private static List<UserWithGroupsDto> fetchUsersWithGroups(ResourceResolver resourceResolver, List<User> users) throws
            RepositoryException {
        List<UserWithGroupsDto> usersDtos = new ArrayList<>();
        for (User user : users) {
            usersDtos.add(new UserWithGroupsDto(resourceResolver, user));
        }
        return usersDtos;
    }

    public List<UserWithGroupsDto> getUsers() {
        return users;
    }

    public long getNumberOfFoundUsers() {
        return numberOfFoundUsers;
    }

    public long getNumberOfPages() {
        return numberOfPages;
    }
}
