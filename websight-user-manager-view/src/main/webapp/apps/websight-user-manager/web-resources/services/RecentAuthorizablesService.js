const RECENT_USERS_KEY = 'websight.user-manager.recent.users';
const RECENT_GROUPS_KEY = 'websight.user-manager.recent.groups';
const RECENT_SYSTEM_USER_KEY = 'websight.user-manager.recent.system.user';
const RECENT_ITEMS_LIMIT = 10;

const RECENT_KEYS = {
    user: RECENT_USERS_KEY,
    group: RECENT_GROUPS_KEY,
    'system-user': RECENT_SYSTEM_USER_KEY
}

const AUTHORIZABLE_TYPES = {
    user: 'user',
    group: 'group',
    systemUser: 'system-user'
}

class RecentAuthorizablesService {
    loadRecentAuthorizablesFromLocalStorage() {
        const recentAuthorizables = {};
        Object.values(AUTHORIZABLE_TYPES).forEach(authorizableType => {
            recentAuthorizables[authorizableType] = JSON.parse(localStorage.getItem(RECENT_KEYS[authorizableType])) || [];
        });
        return recentAuthorizables;
    }

    updateRecent(authorizable, type, actionType, prevRecentAuthorizables = {}) {
        const recentTypedAuthorizables = (prevRecentAuthorizables[type] || []).filter((recentAuthorizable) => recentAuthorizable.id !== authorizable.id);
        if (recentTypedAuthorizables.length >= RECENT_ITEMS_LIMIT) {
            recentTypedAuthorizables.pop();
        }
        recentTypedAuthorizables.unshift({ ...authorizable, actionType });
        const recentAuthorizables = prevRecentAuthorizables;
        recentAuthorizables[type] = recentTypedAuthorizables;
        localStorage.setItem(RECENT_KEYS[type], JSON.stringify(recentTypedAuthorizables));
        return recentAuthorizables;
    }

    removeFromRecent(id, type, prevRecentAuthorizables = {}) {
        const recentTypedAuthorizables = (prevRecentAuthorizables[type] || []).filter((authorizable) => authorizable.id !== id);

        const recentAuthorizables = prevRecentAuthorizables;
        recentAuthorizables[type] = recentTypedAuthorizables;
        localStorage.setItem(RECENT_KEYS[type], JSON.stringify(recentTypedAuthorizables));
        return recentAuthorizables;
    }
}

export default new RecentAuthorizablesService();