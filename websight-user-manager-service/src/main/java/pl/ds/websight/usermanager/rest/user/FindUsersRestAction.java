package pl.ds.websight.usermanager.rest.user;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
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
import pl.ds.websight.usermanager.dto.UserListDto;
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
import static org.apache.jackrabbit.oak.spi.security.user.UserConstants.NT_REP_USER;
import static org.apache.jackrabbit.oak.spi.security.user.UserConstants.REP_AUTHORIZABLE_ID;
import static org.apache.jackrabbit.oak.spi.security.user.UserConstants.REP_DISABLED;
import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.EMAIL_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.FIRST_NAME_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.ID_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.LAST_LOGGED_IN_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.LAST_NAME_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.LOGIN_COUNT_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.META_INFO_NODE_NAME;
import static pl.ds.websight.usermanager.util.QueryUtil.searchProperty;

@Component
@SlingAction(GET)
public class FindUsersRestAction extends AbstractRestAction<FindUsersRestModel, UserListDto>
        implements RestAction<FindUsersRestModel, UserListDto> {

    private static final Logger LOG = LoggerFactory.getLogger(FindUsersRestAction.class);

    public static final long PAGE_SIZE = 25L;

    @Override
    protected RestActionResult<UserListDto> performAction(FindUsersRestModel model) throws RepositoryException {
        LOG.debug("Find users action start");
        Objects.requireNonNull(model.getResourceResolver().adaptTo(Session.class), "Error occurred during fetching users");
        List<User> users = findUsers(model);
        long numberOfFoundUsers = users.size();
        LOG.debug("Found {} users", numberOfFoundUsers);
        long offset = PaginationUtil.getOffset(model.getPageNumber(), PAGE_SIZE);
        List<User> usersPage = users.stream()
                .skip(offset)
                .limit(PAGE_SIZE)
                .collect(toList());
        long numberOfPages = PaginationUtil.countPages(numberOfFoundUsers, PAGE_SIZE);
        UserListDto userListDto = new UserListDto(usersPage, model.getResourceResolver(), numberOfFoundUsers, numberOfPages);
        LOG.debug("Find users action end");
        return RestActionResult.success(userListDto);
    }

    private static List<User> findUsers(FindUsersRestModel model) throws RepositoryException {
        ResourceResolver resourceResolver = model.getResourceResolver();
        ValueFactory valueFactory = Objects.requireNonNull(resourceResolver.adaptTo(Session.class)).getValueFactory();
        Iterator<? extends Authorizable> resourceIterator = model.getUserManager().findAuthorizables(new Query() {
            @Override
            public <Q> void build(QueryBuilder<Q> builder) {
                builder.setSelector(User.class);
                Stream.of(
                        getFilterCondition(builder, model.getFilter()),
                        getEnabledCondition(builder, model.getEnabled()),
                        getRegularUserTypeCondition(builder, valueFactory),
                        getLoggedInCondition(builder, model.getEverLoggedIn(), valueFactory))
                        .filter(Objects::nonNull)
                        .reduce(builder::and)
                        .ifPresent(builder::setCondition);
                builder.setSortOrder(getSortingProperty(model.getSortBy()), model.getSortDirection());
                builder.setLimit(0, -1);
            }
        });
        // builder.setSelector(User.class) guarantees we will get User objects
        @SuppressWarnings("unchecked")
        Iterator<User> foundUsers = (Iterator<User>) resourceIterator;
        return filterByRequestedParentGroups(foundUsers, model);
    }

    private static <Q> Q getFilterCondition(QueryBuilder<Q> builder, String filter) {
        if (StringUtils.isNotBlank(filter)) {
            return Stream.of(
                    QueryUtil.caseInsensitiveLike(builder, searchProperty(REP_AUTHORIZABLE_ID), filter),
                    QueryUtil.caseInsensitiveLike(builder, searchProfileProperty(FIRST_NAME_PROPERTY_NAME), filter),
                    QueryUtil.caseInsensitiveLike(builder, searchProfileProperty(LAST_NAME_PROPERTY_NAME), filter),
                    QueryUtil.caseInsensitiveLike(builder, searchProfileProperty(EMAIL_PROPERTY_NAME), filter))
                    .reduce(builder::or)
                    .orElse(null);
        }
        return null;
    }

    private static String searchProfileProperty(String propertyName) {
        return UserBaseModel.PROFILE_NODE_NAME + "/@" + propertyName;
    }

    private static <Q> Q getEnabledCondition(QueryBuilder<Q> builder, Boolean enabled) {
        if (enabled == null) {
            return null;
        }
        return enabled ?
                builder.not(builder.exists(searchProperty(REP_DISABLED))) :
                builder.exists(searchProperty(REP_DISABLED));
    }

    private static <Q> Q getRegularUserTypeCondition(QueryBuilder<Q> builder, ValueFactory valueFactory) {
        return builder.eq(searchProperty(JCR_PRIMARYTYPE), valueFactory.createValue(NT_REP_USER));
    }

    private static <Q> Q getLoggedInCondition(QueryBuilder<Q> builder, Boolean everLoggedIn, ValueFactory valueFactory) {
        if (everLoggedIn != null) {
            String lastLoginSearchProperty = META_INFO_NODE_NAME + "/@" + LOGIN_COUNT_PROPERTY_NAME;
            if (everLoggedIn) {
                return builder.gt(lastLoginSearchProperty, valueFactory.createValue(0));
            } else {
                Q metainfoNotExists = builder.not(builder.exists(searchProperty(META_INFO_NODE_NAME)));
                Q loginCountIsZero = builder.eq(lastLoginSearchProperty, valueFactory.createValue(0));
                return builder.or(metainfoNotExists, loginCountIsZero);
            }
        }
        return null;
    }

    private static String getSortingProperty(String requestedSortingField) {
        switch (requestedSortingField) {
            case LAST_NAME_PROPERTY_NAME:
                return "profile/@" + LAST_NAME_PROPERTY_NAME;
            case LAST_LOGGED_IN_PROPERTY_NAME:
                return "metainfo/@" + LAST_LOGGED_IN_PROPERTY_NAME;
            case LOGIN_COUNT_PROPERTY_NAME:
                return "metainfo/@" + LOGIN_COUNT_PROPERTY_NAME;
            case ID_PROPERTY_NAME:
            default:
                return '@' + REP_AUTHORIZABLE_ID;
        }
    }

    private static List<User> filterByRequestedParentGroups(Iterator<User> foundUsers, FindUsersRestModel model)
            throws RepositoryException {
        List<User> users = new ArrayList<>();
        List<String> requestedGroups = model.getGroups();
        while (foundUsers.hasNext()) {
            User foundUser = foundUsers.next();
            if (isMemberOfGroups(foundUser, requestedGroups)) {
                users.add(foundUser);
            }
        }
        return users;
    }

    private static boolean isMemberOfGroups(User user, List<String> requestedGroups) throws RepositoryException {
        if (requestedGroups.isEmpty()) {
            return true;
        }
        List<String> userGroupsNames = getUserGroupsNames(user.declaredMemberOf());
        if (userGroupsNames.isEmpty()) {
            return false;
        }
        return userGroupsNames.containsAll(requestedGroups);
    }

    private static List<String> getUserGroupsNames(Iterator<Group> memberOf) throws RepositoryException {
        List<String> names = new ArrayList<>();
        while (memberOf.hasNext()) {
            names.add(memberOf.next().getID());
        }
        return names;
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.FIND_USERS_ERROR;
    }

}
