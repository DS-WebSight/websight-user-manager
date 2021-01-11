import RestClient from 'websight-rest-atlaskit-client/RestClient';

import { getUserDisplayName } from '../utils/AuthorizableUtil.js';

const userRequestData = (user) => ({
    authorizableId: user.id,
    password: user.password,
    confirmedPassword: user.confirmedPassword,
    changePassword: user.changePassword,
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    groups: (user.groups || []).map((group) => group.value),
    enabled: getEnabled(user)
});

const getEnabled = (user) => {
    // 'enabled' flag should be always ignored for protected users
    if (user.protectedUser) {
        return undefined;
    }
    return typeof user.enabled === 'boolean' ?
        user.enabled : user.enabled === 'enabled'
}

const userFindParameters = (parameters) => ({
    ...parameters,
    filter: parameters.filter ? parameters.filter : null,
    groups: (parameters.groups || []).map((group) => group.value)
})

const groupDisplayName = (group) =>
    group.displayName || group.id;

const userResponseData = (user) => ({
    ...user,
    displayName: getUserDisplayName(user),
    icon: 'person',
    groups: user.groups
        .map((group) => ({
            ...group,
            displayName: groupDisplayName(group),
            icon: 'group',
            value: group.id
        }))
        .sort((a, b) => (a.displayName > b.displayName) ? 1 : -1)
})

class UserService {
    constructor() {
        this.client = new RestClient('websight-user-manager-service');
    }

    get emptyUserData() {
        return {
            id: '',
            password: '',
            firstName: '',
            lastName: '',
            email: '',
            enabled: true,
            groups: []
        }
    }

    getUser(parameters, onSuccess, onFailure) {
        this.client.get({
            action: 'get-user',
            parameters: parameters,
            onSuccess: ({ entity }) => onSuccess(userResponseData(entity)),
            onValidationFailure: onFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }

    getUsers(parameters, onSuccess, onComplete) {
        this.client.get({
            action: 'find-users',
            parameters: userFindParameters(parameters),
            onSuccess: ({ entity }) => {
                const users = entity.users.map((user) => userResponseData(user));
                onSuccess({ ...entity, users });
            },
            onValidationFailure: onComplete,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }

    createUser(userData, onSuccess, onValidationFailure, onComplete) {
        this.client.post({
            action: 'create-user',
            data: userRequestData(userData),
            onSuccess: ({ entity }) => onSuccess(userResponseData(entity)),
            onValidationFailure: onValidationFailure,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }

    updateUser(userData, onSuccess, onValidationFailure, onComplete) {
        this.client.post({
            action: 'update-user',
            data: userRequestData(userData),
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

    toggleUserStatus(userId, onSuccess, onComplete) {
        this.client.post({
            action: 'toggle-user-status',
            data: { authorizableId: userId },
            onSuccess: onSuccess,
            onValidationFailure: onComplete,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        })
    }
}

export default new UserService();
