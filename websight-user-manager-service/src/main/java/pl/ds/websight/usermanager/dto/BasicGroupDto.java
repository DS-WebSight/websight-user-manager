package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.oak.commons.PropertiesUtil;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;

import static pl.ds.websight.usermanager.rest.group.GroupBaseModel.DISPLAY_NAME_PROPERTY;

public class BasicGroupDto extends AuthorizableDto {

    private final String displayName;

    public BasicGroupDto(ResourceResolver resolver, Group group) throws RepositoryException {
        super(group, resolver);
        displayName = PropertiesUtil.toString(group.getProperty(DISPLAY_NAME_PROPERTY), group.getID());
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getType() {
        return "group";
    }
}
