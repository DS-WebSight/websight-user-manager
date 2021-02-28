import RestClient from 'websight-rest-atlaskit-client/RestClient';

import { getUserDisplayName } from '../utils/AuthorizableUtil.js';

const groupRequestData = (group) => ({
    authorizableId: group.id,
    displayName: group.displayName,
    description: group.description,
    parentGroups: (group.parentGroups || []).map((parentGroup) => parentGroup.value),
    members: (group.members || []).map((member) => member.value)
})

const groupFindParameters = (parameters) => ({
    ...parameters,
    filter: parameters.filter ? parameters.filter : null,
    parentGroups: (parameters.parentGroups || []).map((group) => group.value),
    members: (parameters.members || []).map((member) => member.value)
})

const authorizableDisplayName = (authorizable) => {
    if (authorizable.displayName) {
        return authorizable.displayName;
    } else if (authorizable.firstName || authorizable.lastName) {
        return getUserDisplayName(authorizable);
    }
    return authorizable.id;
}

const authorizableIcon = (member) => {
    switch (member.type) {
    case 'user':
        return 'person';
    case 'group':
        return 'group';
    case 'system_user':
        return 'settings';
    default:
        return '';
    }
}

const groupResponseData = (group) => ({
    ...group,
    displayName: group.displayName || group.id,
    parentGroups: group.parentGroups
        .map((parentGroup) => ({
            ...parentGroup,
            displayName: parentGroup.displayName || parentGroup.id,
            icon: 'group',
            value: parentGroup.id
        }))
        .sort((a, b) => (a.displayName > b.displayName) ? 1 : -1),
    members: group.members
        .map((member) => ({
            ...member,
            displayName: authorizableDisplayName(member),
            icon: authorizableIcon(member),
            value: member.id
        }))
        .sort((a, b) => (a.displayName > b.displayName) ? 1 : -1)
})

class GroupService {
    constructor() {
        this.client = new RestClient('websight-user-manager-service');
    }

    get emptyGroupData() {
        return {
            id: '',
            displayName: '',
            description: '',
            parentGroups: [],
            members: []
        }
    }

    getGroup(parameters, onSuccess, onFailure) {
        this.client.get({
            action: 'get-group',
            parameters: parameters,
            onSuccess: ({ entity }) => onSuccess(groupResponseData(entity)),
            onValidationFailure: onFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }

    getGroups(parameters, onSuccess, onComplete) {
        this.client.get({
            action: 'find-groups',
            parameters: groupFindParameters(parameters),
            onSuccess: ({ entity }) => {
                const groups = entity.groups.map((group) => groupResponseData(group));
                onSuccess({ ...entity, groups });
            },
            onValidationFailure: onComplete,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }

    createGroup(groupData, onSuccess, onValidationFailure, onComplete) {
        this.client.post({
            action: 'create-group',
            data: groupRequestData(groupData),
            onSuccess: ({ entity }) => onSuccess(groupResponseData(entity)),
            onValidationFailure: onValidationFailure,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }

    updateGroup(groupData, onSuccess, onValidationFailure, onComplete) {
        this.client.post({
            action: 'update-group',
            data: groupRequestData(groupData),
            onSuccess: ({ entity }) => onSuccess(groupResponseData(entity)),
            onValidationFailure: onValidationFailure,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }

    deleteGroup(groupData, onSuccess, onComplete) {
        this.client.post({
            action: 'delete-group',
            data: groupRequestData(groupData),
            onSuccess: onSuccess,
            onValidationFailure: onComplete,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }
}

export default new GroupService();
