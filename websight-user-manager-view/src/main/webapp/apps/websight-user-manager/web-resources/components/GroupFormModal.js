import React from 'react';
import Button, { ButtonGroup } from '@atlaskit/button';
import ModalDialog, { ModalTransition } from '@atlaskit/modal-dialog';
import TextArea from '@atlaskit/textarea';
import TextField from '@atlaskit/textfield';

import Form, { FormFooter } from 'websight-rest-atlaskit-client/Form';
import AuthorizableAutosuggestion from 'websight-autosuggestion-esm/AuthorizableAutosuggestion';
import GroupAutosuggestion from 'websight-autosuggestion-esm/GroupAutosuggestion';

import GroupService from './../services/GroupService.js';
import { PRINCIPAL_EVERYONE } from '../utils/UserManagerConstants.js';
import { filterOutEveryonePrincipal } from '../utils/AuthorizableUtil.js';

export default class GroupFormModal extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            isOpen: false,
            group: { ...GroupService.emptyGroupData }
        };

        this.open = this.open.bind(this);
        this.close = this.close.bind(this);
        this.onSubmitSuccess = this.onSubmitSuccess.bind(this);
        this.onFormSubmit = this.onFormSubmit.bind(this);
    }

    open() {
        this.setState({
            isOpen: true,
            group: {
                ...GroupService.emptyGroupData,
                ...(!this.props.create ? this.props.group : {})
            }
        })
    }

    close() {
        this.setState({
            isOpen: false
        })
    }

    onSubmitSuccess(data) {
        this.close();
        this.props.onSaveSuccess && this.props.onSaveSuccess(data);
    }

    onFormSubmit(...args) {
        this.setState({ group: args[0] });

        if (this.props.create) {
            GroupService.createGroup(...args);
        } else {
            GroupService.updateGroup(...args);
        }
    }

    render() {
        const { isOpen, group } = this.state;

        const form = (
            <Form
                onSubmit={this.onFormSubmit}
                onSuccess={this.onSubmitSuccess}
            >
                {({ submitted }) => (
                    <>
                        <TextField
                            autocomplete='off'
                            label='ID'
                            name='id'
                            isRequired
                            isDisabled={!this.props.create} defaultValue={group.id}
                        />

                        <TextField
                            autocomplete='off'
                            label='Display name'
                            name='displayName'
                            defaultValue={group.displayName}
                        />

                        <TextArea
                            autocomplete='off'
                            label='Description'
                            name='description'
                            defaultValue={group.description}
                        />

                        {group.id !== PRINCIPAL_EVERYONE && (
                            <>
                                <GroupAutosuggestion
                                    defaultValue={([...(filterOutEveryonePrincipal(group.parentGroups) || [])])}
                                    isMulti
                                    label='Parent groups'
                                    name='parentGroups'
                                    filterOut={[PRINCIPAL_EVERYONE]}
                                />
                                <AuthorizableAutosuggestion
                                    defaultValue={([...(filterOutEveryonePrincipal(group.members) || [])])}
                                    isMulti
                                    label='Members'
                                    name='members'
                                    noOptionsMessage={(inputValue) => `No members found for "${inputValue}"`}
                                    noOptionEmptyMessage='Start typing to find a member'
                                    filterOut={[PRINCIPAL_EVERYONE]}
                                />
                            </>
                        )}
                        <FormFooter>
                            <ButtonGroup>
                                <Button
                                    appearance='primary'
                                    type='submit'
                                    isLoading={submitted}
                                >
                                    {this.props.create ? 'Create' : 'Save'}
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
        );

        return (
            <ModalTransition>
                {isOpen && (
                    <ModalDialog
                        heading={this.props.create ? 'Create new group' : 'Edit group'}
                        onClose={this.close}
                    >
                        {form}
                    </ModalDialog>
                )}
            </ModalTransition>
        );
    }
}
