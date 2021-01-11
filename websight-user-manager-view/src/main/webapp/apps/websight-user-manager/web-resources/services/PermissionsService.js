import RestClient from 'websight-rest-atlaskit-client/RestClient';

const permissionsRequestData = (parameters) => ({
    authorizableId: parameters.authorizableId || parameters.id,
    path: parameters.path || parameters.paths
});

const permissionsResponseData = (responseData) => {
    return responseData.map((data) => ({
        ...data,
        id: data.path,
        children: (data.children || []).map(child => ({ ...child, id: child.path, children: [] })),
        isExpanded: true
    }))
}

const updateRequestDate = (data) => ({
    authorizableId: data.authorizableId,
    changelog: data.changelog
        .map(item => Object.entries(item).reduce((string, [key, value]) => `${string}${string ? ',' : ''}${key}:${value}`, ''))
})

class PermissionsService {
    constructor() {
        this.client = new RestClient('websight-user-manager-service');
    }

    getPermissions(parameters, onSuccess, onFailure) {
        this.client.get({
            action: 'get-permissions',
            parameters: permissionsRequestData(parameters),
            onSuccess: ({ entity }) => onSuccess(permissionsResponseData(entity)),
            onValidationFailure: onFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }

    updatePermissions(requestData, onSuccess, onComplete) {
        this.client.post({
            action: 'update-permissions',
            data: updateRequestDate(requestData),
            onSuccess: ({ entity }) => onSuccess(entity),
            onValidationFailure: onComplete,
            onFailure: onComplete,
            onError: onComplete,
            onNonFrameworkError: onComplete
        });
    }

    findAclEntries(parameters, onSuccess, onFailure) {
        this.client.get({
            action: 'find-acl-entries',
            parameters: parameters,
            onSuccess: ({ entity }) => onSuccess(entity),
            onValidationFailure: onFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }

    createAclEntry(requestData, onSuccess, onValidationFailure, onFailure) {
        this.client.post({
            action: 'create-acl-entry',
            data: requestData,
            onSuccess: ({ entity }) => onSuccess(entity),
            onValidationFailure: onValidationFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }

    updateAclEntry(requestData, onSuccess, onValidationFailure, onFailure) {
        this.client.post({
            action: 'update-acl-entry',
            data: requestData,
            onSuccess: ({ entity }) => onSuccess(entity),
            onValidationFailure: onValidationFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }

    deleteAclEntry(requestData, onSuccess, onFailure) {
        this.client.post({
            action: 'delete-acl-entry',
            data: requestData,
            onSuccess: ({ entity }) => onSuccess(entity),
            onValidationFailure: onFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }

    getAclEntryEditOptions(parameters, onSuccess, onFailure) {
        this.client.get({
            action: 'get-acl-entry-edit-options',
            parameters: parameters,
            onSuccess: ({ entity }) => onSuccess(entity),
            onValidationFailure: onFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }
}

export default new PermissionsService();
