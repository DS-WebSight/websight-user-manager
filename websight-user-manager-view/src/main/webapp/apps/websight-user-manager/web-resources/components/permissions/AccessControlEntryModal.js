import React from 'react';
import Button, { ButtonGroup } from '@atlaskit/button';
import { Checkbox } from '@atlaskit/checkbox';
import { ErrorMessage, Fieldset } from '@atlaskit/form';
import ModalDialog, { ModalTransition } from '@atlaskit/modal-dialog';
import Select from '@atlaskit/select';
import styled from 'styled-components';

import { colors } from 'websight-admin/theme';
import PathAutosuggestion from 'websight-autosuggestion-esm/PathAutosuggestion';
import Form, { FormFooter } from 'websight-rest-atlaskit-client/Form';

import PermissionsService from '../../services/PermissionsService.js';
import RestrictionsEdit from './RestrictionsEdit.js';

const PathAutosuggestionContainer = styled.div`
    flex: 1 1 auto;

    div > div > div:last-child > div > span {
        visibility: hidden
    }
`;

const emptyEntryData = {
    path: null,
    privileges: []
}

const checkboxLabelCss = () => {
    return (currentStyles) => {
        return {
            ...currentStyles,
            cursor: 'pointer'
        }
    };
}

const stringsToOptions = (privileges) => {
    return (privileges || []).map(privilege => ({ label: privilege, value: privilege }));
}

const optionsToStrings = (options) => {
    return (options || []).map(option => option.value);
}

const FieldErrors = (props) => {
    const { errors = [], path } = props;
    if (!errors.length) {
        return null;
    }
    return (
        <>
            {errors.map(error => error.path === path ? <ErrorMessage>{error.message}</ErrorMessage> : null)}
        </>
    )
}

const arrayToObject = (restrictions) => {
    const restrictionObject = {};
    restrictions.map(restriction => {
        restrictionObject[restriction.type] = restriction.values;
    });
    return restrictionObject;
}

const objectToArray = (restricitons) => {
    const restrictionsArray = []
    if (restricitons) {
        Object.entries(restricitons).map(([key, values]) => {
            restrictionsArray.push({ type: key, values: values });
        });
    }
    return restrictionsArray;
}

export default class AccessControlEntryModal extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            availablePrivileges: [],
            availableRestrictions: [],
            defaultEntryData: emptyEntryData,
            isCreateMode: true,
            isPermissionTypeAllow: true,
            isOpen: false,
            restrictions: [],
            restrictionType: null,
            restrictionValue: null,
            validationErrors: []
        }
        this.open = this.open.bind(this);
        this.close = this.close.bind(this);
        this.updateRestrictions = this.updateRestrictions.bind(this);
        this.onFormSubmit = this.onFormSubmit.bind(this);
        this.onSubmitSuccess = this.onSubmitSuccess.bind(this);
    }

    open() {
        const { entryToEdit } = this.props;
        const isCreateMode = !entryToEdit;
        let entryData = emptyEntryData;
        if (!isCreateMode) {
            entryData = {
                ...entryData,
                path: entryToEdit.path,
                privileges: stringsToOptions(entryToEdit.privileges)
            }
        }
        this.setState({
            availablePrivileges: [],
            defaultEntryData: entryData,
            isCreateMode: isCreateMode,
            isOpen: true,
            isPermissionTypeAllow: isCreateMode ? true : entryToEdit.allow,
            restrictions: isCreateMode ? [] : objectToArray(entryToEdit.restrictions),
            validationErrors: []
        })
        const params = isCreateMode ? {} : { path: this.props.entryToEdit.path };
        PermissionsService.getAclEntryEditOptions(
            params,
            ({ privileges, restrictions }) => {
                this.setState({
                    availablePrivileges: privileges,
                    availableRestrictions: restrictions
                })
            })
    }

    close() {
        this.setState({
            isOpen: false
        })
    }

    getCheckboxTheme(tokens, allow) {
        const setColor = (colorsObject, color) => {
            Object.keys(colorsObject).forEach((key) => colorsObject[key] = color);
        }

        const setColors = (borderColor, boxColor, tickColor) => {
            setColor(tokens.icon.borderColor, borderColor);
            setColor(tokens.icon.boxColor, boxColor);
            setColor(tokens.icon.tickColor, tickColor);
        }

        if (allow) {
            setColors(colors.green, colors.green, colors.white);
        } else {
            setColors(colors.red, colors.red, colors.white);
        }
        return tokens;
    }

    onPermissionTypeChange() {
        this.setState(prevState => ({
            isPermissionTypeAllow: !prevState.isPermissionTypeAllow
        }))
    }

    updateRestrictions(restrictions) {
        this.setState({
            restrictions: restrictions
        })
    }

    onFormSubmit(requestData, onSuccess, onValidationFailure, onComplete) {
        requestData = {
            ...requestData,
            authorizableId: this.props.authorizableId,
            privileges: JSON.stringify(optionsToStrings(requestData.privileges)),
            restrictions: JSON.stringify(arrayToObject(this.state.restrictions))
        }
        const originalOnValidationFailure = onValidationFailure;
        onValidationFailure = (data) => {
            this.setState({
                validationErrors: data.entity || []
            });
            originalOnValidationFailure(data);
        }
        const originalOnComplete = onComplete;
        onComplete = (data) => {
            this.setState({
                validationErrors: []
            });
            originalOnComplete(data);
        }
        if (this.state.isCreateMode) {
            PermissionsService.createAclEntry(requestData, onSuccess, onValidationFailure, onComplete);
        } else {
            requestData = {
                ...requestData,
                entryId: this.props.entryToEdit.entryId,
                policyId: this.props.entryToEdit.policyId
            }
            PermissionsService.updateAclEntry(requestData, onSuccess, onValidationFailure, onComplete);
        }
    }

    onSubmitSuccess(data) {
        this.close();
        this.props.onSaveSuccess && this.props.onSaveSuccess(data);
    }

    render() {
        const { isPermissionTypeAllow, validationErrors, restrictions, availableRestrictions } = this.state;

        return (
            <ModalTransition>
                {this.state.isOpen && (
                    <ModalDialog
                        heading={this.state.isCreateMode ? 'Add Access Control Entry' : 'Edit Access Control Entry'}
                        onClose={this.close}
                        ref={(elementRef) => this.modalRef = elementRef}
                    >
                        <Form
                            onSubmit={this.onFormSubmit}
                            onSuccess={this.onSubmitSuccess}
                        >
                            {({ submitted }) => (
                                <>
                                    <PathAutosuggestionContainer>
                                        <PathAutosuggestion
                                            label='Path'
                                            name='path'
                                            isRequired
                                            parameters={{ autosuggestionType: 'jcr-path' }}
                                            noOptionsMessage={(inputValue) => `No resource found for "${inputValue}"`}
                                            noOptionEmptyMessage='Start typing to find a resource'
                                            placeholder='Choose a path (eg: /home)'
                                            defaultOptions={true}
                                            defaultValue={this.state.defaultEntryData.path || '/'}
                                        />
                                    </PathAutosuggestionContainer>
                                    <Select
                                        label='Privileges'
                                        name='privileges'
                                        isRequired
                                        className='multi-select'
                                        classNamePrefix='react-select'
                                        options={stringsToOptions(this.state.availablePrivileges)}
                                        defaultValue={this.state.defaultEntryData.privileges}
                                        isMulti
                                        placeholder='Select...'
                                        menuPortalTarget={document.body}
                                        styles={{
                                            menuPortal: base => ({
                                                ...base,
                                                zIndex: 9999
                                            })
                                        }}
                                    />
                                    <Fieldset legend='Permission Type'>
                                        <Checkbox
                                            name='allow'
                                            label={isPermissionTypeAllow ? 'Allow' : 'Deny'}
                                            hideLabel={true}
                                            defaultValue={isPermissionTypeAllow}
                                            isIndeterminate={!isPermissionTypeAllow}
                                            onChange={() => this.onPermissionTypeChange()}
                                            overrides={{
                                                Label: {
                                                    cssFn: checkboxLabelCss()
                                                }
                                            }}
                                            theme={(current, props) => this.getCheckboxTheme(current(props), isPermissionTypeAllow)}
                                        />
                                    </Fieldset>
                                    <Fieldset legend='Restrictions'>
                                        <FieldErrors errors={validationErrors} path='restrictions' />
                                        <RestrictionsEdit
                                            restrictions={restrictions}
                                            availableRestrictionTypes={availableRestrictions}
                                            onChange={this.updateRestrictions}
                                        />
                                    </Fieldset>
                                    <FormFooter>
                                        <ButtonGroup>
                                            <Button
                                                appearance='primary'
                                                type='submit'
                                                isLoading={submitted}
                                            >
                                                {this.state.isCreateMode ? 'Add' : 'Save'}
                                            </Button>
                                            <Button
                                                appearance='subtle'
                                                onClick={this.close}
                                                isDisabled={submitted}
                                            >
                                                Cancel
                                            </Button>
                                        </ButtonGroup>
                                    </FormFooter>
                                </>
                            )}
                        </Form>
                    </ModalDialog>
                )}
            </ModalTransition>
        );
    }
}