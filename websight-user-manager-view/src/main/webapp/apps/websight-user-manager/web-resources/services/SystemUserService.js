import RestClient from 'websight-rest-atlaskit-client/RestClient';

const userFindParameters = (parameters) => ({
    ...parameters,
    filter: parameters.filter ? parameters.filter : null
})

const userResponseData = (user) => ({
    ...user,
    icon: 'settings',
    groups: user.groups
        .map((group) => ({
            ...group,
            displayName: groupDisplayName(group),
            icon: 'group',
            value: group.id
        }))
        .sort((a, b) => (a.displayName > b.displayName) ? 1 : -1)
})

const groupDisplayName = (group) =>
    group.displayName || group.id;

class SystemUserService {
    constructor() {
        this.client = new RestClient('websight-user-manager-service');
    }

    get emptyUserData() {
        return {
            id: ''
        }
    }

    getSystemUser(parameters, onSuccess, onFailure) {
        this.client.get({
            action: 'get-system-user',
            parameters: parameters,
            onSuccess: ({ entity }) => onSuccess(userResponseData(entity)),
            onValidationFailure: onFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }

    getSystemUsers(parameters, onSuccess, onComplete) {
        this.client.get({
            action: 'find-system-users',
            parameters: userFindParameters(parameters),
            onSuccess: ({ entity }) => {
                const users = entity.systemUsers.map((user) => userResponseData(user));
                onSuccess({ ...entity, users });
            },
            onValidationFailure: onComplete,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }

    createSystemUser(userData, onSuccess, onValidationFailure, onComplete) {
        this.client.post({
            action: 'create-system-user',
            data: { authorizableId: userData.id },
            onSuccess: ({ entity }) => onSuccess(userResponseData(entity)),
            onValidationFailure: onValidationFailure,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }

    deleteUser(userId, onSuccess, onComplete) {
        this.client.post({
            action: 'delete-user',
            data: { authorizableId: userId },
            onSuccess: onSuccess,
            onValidationFailure: onComplete,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }
}

export default new SystemUserService();
