package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.oak.commons.PropertiesUtil;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;

import static pl.ds.websight.usermanager.rest.group.GroupBaseModel.DESCRIPTION_PROPERTY;

public class GroupDto extends BasicGroupDto {

    private final String description;

    public GroupDto(ResourceResolver resolver, Group group) throws RepositoryException {
        super(resolver, group);
        description = PropertiesUtil.toString(group.getProperty(DESCRIPTION_PROPERTY), null);
    }

    public String getDescription() {
        return description;
    }
}
