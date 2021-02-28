import { PRINCIPAL_EVERYONE } from './UserManagerConstants.js';

export const getAuthorizableHash = (authorizable = {}) => {
    return '#' + getAuthorizableHref(authorizable);
}

export const getAuthorizableHref = (authorizable = {}) => {
    let prefix;
    switch (authorizable.type) {
    case 'user':
        prefix = '/users/';
        break;
    case 'group':
        prefix = '/groups/';
        break;
    case 'system_user':
        prefix = '/system-users/';
        break;
    default:
        return '';
    }
    return prefix + encodeURIComponent(authorizable.id);
}

export const getUserDisplayName = (user) => user.firstName || user.lastName ?
    `${user.firstName || ''} ${user.lastName || ''}` : user.id;

export const filterOutEveryonePrincipal = (authorizables) => {
    return (authorizables || []).filter(authorizable => authorizable.id !== PRINCIPAL_EVERYONE);
}

export const getIconNameForAuthorizableType = (type = '') => {
    switch (type) {
    case 'user':
        return 'person';
    case 'group':
        return 'people'
    case 'system-user':
        return 'settings';
    default:
        return '';
    }
}

export const getAuthorizableTypeFromHash = (hash) => {
    let result = hash;
    if (result.charAt(0) === '/') {
        result = result.substr(1);
    }
    return ['user', 'group', 'system-user'].find(type => {
        if (result.startsWith(type)) {
            return type;
        }
    });
}

const tabNames = ['', 'permissions', 'acl'];

const getTabName = (tabIndex) => {
    return tabNames[tabIndex] || '';
}

export const getTabIndex = (tabName) => {
    return tabNames.indexOf(tabName) || 0;
}

export const setTabNameInHash = (authorizablesType, authorizableId, tabIndex) => {
    let tabName = getTabName(tabIndex);
    tabName = tabName === '' ? tabName : '/' + tabName;
    window.history.replaceState(
        {},
        document.title,
        `${window.location.pathname}#/${authorizablesType}/${authorizableId}${tabName}`
    );
}

export const getAuthorizableDisplayName = (authorizable) => {
    if (authorizable.displayName) {
        return authorizable.displayName;
    } else if (authorizable.firstName || authorizable.lastName) {
        return getUserDisplayName(authorizable);
    }
    return authorizable.id;
}