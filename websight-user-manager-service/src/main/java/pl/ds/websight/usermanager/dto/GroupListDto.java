package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

public class GroupListDto {

    private final List<GroupWithMembersDto> groups;

    private final long numberOfFoundGroups;

    private final long numberOfPages;

    public GroupListDto(ResourceResolver resourceResolver, List<Group> groups, long numberOfFoundGroups, long numberOfPages)
            throws RepositoryException {
        this.groups = fetchGroupsWithMembers(resourceResolver, groups);
        this.numberOfFoundGroups = numberOfFoundGroups;
        this.numberOfPages = numberOfPages;
    }

    private static List<GroupWithMembersDto> fetchGroupsWithMembers(ResourceResolver resourceResolver, List<Group> groups) throws
            RepositoryException {
        List<GroupWithMembersDto> groupsDtos = new ArrayList<>();
        for (Group group : groups) {
            groupsDtos.add(new GroupWithMembersDto(resourceResolver, group));
        }
        return groupsDtos;
    }

    public List<GroupWithMembersDto> getGroups() {
        return groups;
    }

    public long getNumberOfFoundGroups() {
        return numberOfFoundGroups;
    }

    public long getNumberOfPages() {
        return numberOfPages;
    }
}
