package pl.ds.websight.usermanager.rest.systemuser;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Query;
import org.apache.jackrabbit.api.security.user.QueryBuilder;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.SystemUserListDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;
import pl.ds.websight.usermanager.util.PaginationUtil;
import pl.ds.websight.usermanager.util.QueryUtil;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;
import static org.apache.jackrabbit.oak.spi.security.user.UserConstants.NT_REP_SYSTEM_USER;
import static org.apache.jackrabbit.oak.spi.security.user.UserConstants.REP_AUTHORIZABLE_ID;
import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;
import static pl.ds.websight.usermanager.util.QueryUtil.searchProperty;

@Component
@SlingAction(GET)
public class FindSystemUsersRestAction extends AbstractRestAction<FindSystemUsersRestModel, SystemUserListDto>
        implements RestAction<FindSystemUsersRestModel, SystemUserListDto> {

    private static final Logger LOG = LoggerFactory.getLogger(FindSystemUsersRestAction.class);

    public static final long PAGE_SIZE = 25L;

    @Override
    protected RestActionResult<SystemUserListDto> performAction(FindSystemUsersRestModel model) throws RepositoryException {
        LOG.debug("Find system users action start");
        List<User> users = findSystemUsers(model);
        long numberOfFoundUsers = users.size();
        LOG.debug("Found {} system users", numberOfFoundUsers);
        long offset = PaginationUtil.getOffset(model.getPageNumber(), PAGE_SIZE);
        List<User> usersPage = users.stream()
                .skip(offset)
                .limit(PAGE_SIZE)
                .collect(toList());
        long numberOfPages = PaginationUtil.countPages(numberOfFoundUsers, PAGE_SIZE);
        SystemUserListDto userListDto = new SystemUserListDto(usersPage, model.getResourceResolver(), numberOfFoundUsers, numberOfPages);
        LOG.debug("Find system users action end");
        return RestActionResult.success(userListDto);
    }

    private static List<User> findSystemUsers(FindSystemUsersRestModel model) throws RepositoryException {
        ResourceResolver resourceResolver = model.getResourceResolver();
        ValueFactory valueFactory = Objects.requireNonNull(resourceResolver.adaptTo(Session.class)).getValueFactory();
        Iterator<? extends Authorizable> resourceIterator = model.getUserManager().findAuthorizables(new Query() {
            @Override
            public <Q> void build(QueryBuilder<Q> builder) {
                builder.setSelector(User.class);
                Stream.of(
                        getFilterCondition(builder, model.getFilter()),
                        getSystemUserTypeCondition(builder, valueFactory))
                        .filter(Objects::nonNull)
                        .reduce(builder::and)
                        .ifPresent(builder::setCondition);
                builder.setSortOrder('@' + REP_AUTHORIZABLE_ID, model.getSortDirection());
                builder.setLimit(0, -1);
            }
        });
        @SuppressWarnings("unchecked")
        Iterator<User> foundUsers = (Iterator<User>) resourceIterator;
        List<User> users = new ArrayList<>();
        foundUsers.forEachRemaining(users::add);
        return users;
    }

    private static <Q> Q getFilterCondition(QueryBuilder<Q> builder, String filter) {
        if (StringUtils.isNotBlank(filter)) {
            return QueryUtil.caseInsensitiveLike(builder, searchProperty(REP_AUTHORIZABLE_ID), filter);
        }
        return null;
    }

    private static <Q> Q getSystemUserTypeCondition(QueryBuilder<Q> builder, ValueFactory valueFactory) {
        return builder.eq(searchProperty(JCR_PRIMARYTYPE), valueFactory.createValue(NT_REP_SYSTEM_USER));
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.FIND_USERS_ERROR;
    }

}
