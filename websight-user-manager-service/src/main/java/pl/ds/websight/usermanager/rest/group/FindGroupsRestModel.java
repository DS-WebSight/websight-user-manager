package pl.ds.websight.usermanager.rest.group;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.QueryBuilder.Direction;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.List;

import static org.apache.jackrabbit.api.security.user.QueryBuilder.Direction.ASCENDING;
import static org.apache.jackrabbit.api.security.user.QueryBuilder.Direction.DESCENDING;
import static org.apache.sling.models.annotations.DefaultInjectionStrategy.OPTIONAL;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = OPTIONAL)
public class FindGroupsRestModel {

    @SlingObject
    private ResourceResolver resourceResolver;

    private UserManager userManager;

    @RequestParameter
    private String filter;

    @RequestParameter
    private List<String> parentGroups = Collections.emptyList();

    @RequestParameter
    private List<String> members = Collections.emptyList();

    @RequestParameter
    private String sortBy;

    @RequestParameter
    @Default(values = "ASC")
    private String sortDirection;

    @RequestParameter
    @Default(longValues = 1L)
    private Long pageNumber;

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public Session getSession() {
        return getResourceResolver().adaptTo(Session.class);
    }

    public UserManager getUserManager() throws RepositoryException {
        if (userManager == null) {
            userManager = AccessControlUtil.getUserManager(getSession());
        }
        return userManager;
    }

    public String getFilter() {
        return filter;
    }

    public List<String> getParentGroups() {
        return parentGroups;
    }

    public List<String> getMembers() {
        return members;
    }

    public String getSortBy() {
        return StringUtils.defaultIfBlank(sortBy, GroupBaseModel.ID_PROPERTY_NAME);
    }

    public Direction getSortDirection() {
        return StringUtils.isNotBlank(sortDirection) && "DESC".equals(sortDirection) ? DESCENDING : ASCENDING;
    }

    public Long getPageNumber() {
        return pageNumber;
    }

}
