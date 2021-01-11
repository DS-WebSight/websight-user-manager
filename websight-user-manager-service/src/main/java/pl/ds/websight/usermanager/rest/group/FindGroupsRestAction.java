package pl.ds.websight.usermanager.rest.group;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.Query;
import org.apache.jackrabbit.api.security.user.QueryBuilder;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.GroupListDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.util.PaginationUtil;
import pl.ds.websight.usermanager.util.QueryUtil;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.jackrabbit.oak.spi.security.user.UserConstants.REP_AUTHORIZABLE_ID;
import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;
import static pl.ds.websight.usermanager.rest.group.GroupBaseModel.DISPLAY_NAME_PROPERTY;
import static pl.ds.websight.usermanager.rest.group.GroupBaseModel.ID_PROPERTY_NAME;
import static pl.ds.websight.usermanager.util.QueryUtil.searchProperty;

@Component
@SlingAction(GET)
public class FindGroupsRestAction extends AbstractRestAction<FindGroupsRestModel, GroupListDto>
        implements RestAction<FindGroupsRestModel, GroupListDto> {

    private static final Logger LOG = LoggerFactory.getLogger(FindGroupsRestAction.class);

    public static final long PAGE_SIZE = 25L;
    private static final boolean IGNORE_CASE = true;

    @Override
    protected RestActionResult<GroupListDto> performAction(FindGroupsRestModel model) throws RepositoryException {
        LOG.debug("Find groups action start");
        List<Group> groups = findGroups(model);
        long numberOfFoundGroups = groups.size();
        LOG.debug("Found {} groups", numberOfFoundGroups);
        long offset = PaginationUtil.getOffset(model.getPageNumber(), PAGE_SIZE);
        List<Group> groupsPage = groups.stream()
                .skip(offset)
                .limit(PAGE_SIZE)
                .collect(toList());
        long numberOfPages = PaginationUtil.countPages(numberOfFoundGroups, PAGE_SIZE);
        GroupListDto groupListDto = new GroupListDto(model.getResourceResolver(), groupsPage, numberOfFoundGroups, numberOfPages);
        LOG.debug("Find groups action end");
        return RestActionResult.success(groupListDto);
    }

    private List<Group> findGroups(FindGroupsRestModel model) throws RepositoryException {
        Iterator<? extends Authorizable> resourceIterator = model.getUserManager().findAuthorizables(new Query() {
            @Override
            public <Q> void build(QueryBuilder<Q> builder) {
                builder.setSelector(Group.class);
                Q filterCondition = getFilterCondition(builder, model.getFilter());
                if (filterCondition != null) {
                    LOG.debug("Set filter condition for {}", model.getFilter());
                    builder.setCondition(filterCondition);
                }
                builder.setSortOrder(getSortingProperty(model.getSortBy()), model.getSortDirection(), IGNORE_CASE);
                builder.setLimit(0, -1);
            }
        });
        // builder.setSelector(Group.class) guarantees we will get Group objects
        @SuppressWarnings("unchecked")
        Iterator<Group> foundGroups = (Iterator<Group>) resourceIterator;
        return filterByRequestedParentGroupsOrMembers(foundGroups, model);
    }

    private static <Q> Q getFilterCondition(QueryBuilder<Q> builder, String filter) {
        if (StringUtils.isNotBlank(filter)) {
            return builder.or(
                    QueryUtil.caseInsensitiveLike(builder, searchProperty(REP_AUTHORIZABLE_ID), filter),
                    QueryUtil.caseInsensitiveLike(builder, searchProperty(DISPLAY_NAME_PROPERTY), filter));
        }
        return null;
    }

    private static String getSortingProperty(String requestedSortingField) {
        switch (requestedSortingField) {
            case DISPLAY_NAME_PROPERTY:
                return '@' + DISPLAY_NAME_PROPERTY;
            case ID_PROPERTY_NAME:
            default:
                return '@' + REP_AUTHORIZABLE_ID;
        }
    }

    private List<Group> filterByRequestedParentGroupsOrMembers(Iterator<Group> foundGroups, FindGroupsRestModel model)
            throws RepositoryException {
        List<String> requestedParentGroups = model.getParentGroups();
        List<String> requestedMembers = model.getMembers();
        List<Group> groups = new ArrayList<>();
        while (foundGroups.hasNext()) {
            Group foundGroup = foundGroups.next();
            Iterator<Group> parentGroups = foundGroup.declaredMemberOf();
            Iterator<Authorizable> groupMembers = foundGroup.getDeclaredMembers();
            if (isMemberOf(requestedParentGroups, parentGroups) && hasMembers(requestedMembers, groupMembers)) {
                groups.add(foundGroup);
            }
        }
        return groups;
    }

    private boolean isMemberOf(List<String> requestedGroups, Iterator<Group> authorizables) throws RepositoryException {
        return containsAllIds(authorizables, requestedGroups);
    }

    private boolean hasMembers(List<String> requestedMembers, Iterator<Authorizable> authorizables) throws RepositoryException {
        return containsAllIds(authorizables, requestedMembers);
    }

    private boolean containsAllIds(Iterator<? extends Authorizable> authorizables, List<String> idsToCheck) throws RepositoryException {
        if (idsToCheck == null || idsToCheck.isEmpty()) {
            return true;
        }
        List<String> names = new ArrayList<>();
        while (authorizables.hasNext()) {
            names.add(authorizables.next().getID());
        }
        return names.containsAll(idsToCheck);
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.FIND_GROUPS_ERROR;
    }

}
