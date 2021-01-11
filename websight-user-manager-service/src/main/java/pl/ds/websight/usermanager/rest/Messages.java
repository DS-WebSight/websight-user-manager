package pl.ds.websight.usermanager.rest;

public final class Messages {

    // Create user:
    public static final String CREATE_USER_SUCCESS = "User created";
    public static final String CREATE_USER_SUCCESS_DETAILS = "User '%s' has been created";
    public static final String CREATE_USER_ERROR = "Could not create user";

    // Create group:
    public static final String CREATE_GROUP_SUCCESS = "Group created";
    public static final String CREATE_GROUP_SUCCESS_DETAILS = "Group '%s' has been created";
    public static final String CREATE_GROUP_ERROR = "Could not create group";

    // Update user:
    public static final String UPDATE_USER_SUCCESS = "User updated";
    public static final String UPDATE_USER_SUCCESS_DETAILS = "User '%s' has been updated";
    public static final String UPDATE_USER_ERROR = "Could not update user";
    public static final String UPDATE_USER_ERROR_USER_IS_GROUP_DETAILS = "'%s' is a group, not a user";
    public static final String UPDATE_USER_ERROR_SYSTEM_USER_DETAILS = "'%s' is a system user";

    // Update group:
    public static final String UPDATE_GROUP_SUCCESS = "Group updated";
    public static final String UPDATE_GROUP_SUCCESS_DETAILS = "Group '%s' has been updated";
    public static final String UPDATE_GROUP_ERROR = "Could not update group";

    // Delete user:
    public static final String DELETE_USER_SUCCESS = "User deleted";
    public static final String DELETE_USER_SUCCESS_DETAILS = "User '%s' has been deleted";
    public static final String DELETE_USER_ERROR = "Could not delete user";

    // Delete group:
    public static final String DELETE_GROUP_SUCCESS = "Group deleted";
    public static final String DELETE_GROUP_SUCCESS_DETAILS = "Group '%s' has been deleted";
    public static final String DELETE_GROUP_ERROR = "Group deleted";

    // Find users:
    public static final String FIND_USERS_ERROR = "Could not fetch users";

    // Find groups:
    public static final String FIND_GROUPS_ERROR = "Could not fetch groups";

    // Get user:
    public static final String GET_USER_ERROR = "Could not fetch user";

    // Get group:
    public static final String GET_GROUP_ERROR = "Could not fetch group";

    // Toggle user status:
    public static final String TOGGLE_USER_STATUS_SUCCESS = "User %s";
    public static final String TOGGLE_USER_STATUS_SUCCESS_DETAILS = "User '%s' has been %s";
    public static final String TOGGLE_USER_STATUS_ERROR = "Could not %s user";
    public static final String TOGGLE_USER_STATUS_UNKNOWN_ERROR = "Could not change user status";
    public static final String TOGGLE_USER_STATUS_ERROR_DOES_NOT_EXIST_DETAILS = "Could not find user '%s'";
    public static final String TOGGLE_USER_STATUS_ERROR_SYSTEM_USER_DETAILS = "User '%s' is a system user";
    public static final String TOGGLE_USER_STATUS_ERROR_PROTECTED_USER_DETAILS = "User '%s' is a protected user";
    public static final String TOGGLE_USER_STATUS_ERROR_USER_IS_GROUP_DETAILS = "'%s' is a group, not a user";

    // Get permissions:
    public static final String GET_PERMISSIONS_ERROR = "Could not fetch permissions";
    public static final String GET_PERMISSIONS_ERROR_NODE_NOT_FOUND_DETAILS = "Could not find node '%s'";

    // Update permissions:
    public static final String UPDATE_PERMISSIONS_SUCCESS = "Permissions updated";
    public static final String UPDATE_PERMISSIONS_SUCCESS_DETAILS = "Permissions for '%s' have been updated";
    public static final String UPDATE_PERMISSIONS_ERROR = "Could not update permissions";
    public static final String UPDATE_PERMISSIONS_ERROR_NODE_NOT_FOUND_DETAILS = "Could not find node '%s'";

    // Get ACL Entry update options:
    public static final String GET_AVAILABLE_PRIVILEGES_ERROR = "Could not fetch available privileges";

    // Find ACL Entries:
    public static final String FIND_ACL_ENTRIES_ERROR = "Could not list all ACL Entries";

    // Create ACL Entry:
    public static final String CREATE_ACL_ENTRY_SUCCESS = "ACL Entry added";
    public static final String CREATE_ACL_ENTRY_SUCCESS_DETAILS = "ACL Entry %s for path '%s' has been created";
    public static final String CREATE_ACL_ENTRY_ERROR = "Could not add ACL Entry";
    public static final String CREATE_ACL_ENTRY_ERROR_NO_LIST_CHANGE = "Creation of ACL Entry %s for path '%s' did not provide any change";
    public static final String CREATE_ACL_ENTRY_ERROR_PATH_NOT_EXIST_DETAILS = "Could not find node '%s'";

    // Update ACL Entry:
    public static final String UPDATE_ACL_ENTRY_SUCCESS = "ACL Entry updated";
    public static final String UPDATE_ACL_ENTRY_SUCCESS_DETAILS = "ACL Entry %s for path '%s' has been updated";
    public static final String UPDATE_ACL_ENTRY_ERROR = "Could not update ACL Entry";
    public static final String UPDATE_ACL_ENTRY_ERROR_PATH_NOT_EXIST_DETAILS = "Could not find node '%s'";
    public static final String UPDATE_ACL_ENTRY_ERROR_ACL_ENTRY_NOT_FOUND_DETAILS = "ACL Entry was not found or has been already modified";

    // Delete ACL Entry
    public static final String DELETE_ACL_ENTRY_SUCCESS = "ACL Entry deleted";
    public static final String DELETE_ACL_ENTRY_SUCCESS_DETAILS = "ACL Entry for user '%s' has been deleted";
    public static final String DELETE_ACL_ENTRY_ERROR = "Could not delete ACL Entry";
    public static final String DELETE_ACL_ENTRY_ERROR_ACL_ENTRY_NOT_FOUND_DETAILS = "ACL Entry was not found or has been already modified";

    private Messages() {
        // no instances
    }

    public static String formatMessage(String message, Object... args) {
        return String.format(message, args);
    }
}
