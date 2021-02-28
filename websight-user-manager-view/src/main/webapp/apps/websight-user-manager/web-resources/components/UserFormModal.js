import React from 'react';
import Button, { ButtonGroup } from '@atlaskit/button';
import { Checkbox } from '@atlaskit/checkbox';
import ModalDialog, { ModalTransition } from '@atlaskit/modal-dialog';
import TextField from '@atlaskit/textfield';

import Form, { FormFooter } from 'websight-rest-atlaskit-client/Form';
import GroupAutosuggestion from 'websight-autosuggestion-esm/GroupAutosuggestion';

import UserService from './../services/UserService.js';
import { PRINCIPAL_EVERYONE } from '../utils/UserManagerConstants.js';
import { filterOutEveryonePrincipal } from '../utils/AuthorizableUtil.js';

import styled from 'styled-components';

const PasswordActionsContainer = styled.div`
    display: flex;
    position: relative;
    padding-top: 6px;
    width: 100%;
`;

const PasswordActionButtonsContainer = styled.div`
    position: absolute;
    right: 0
    display: flex
    marginTop: -3px;
`;

export default class UserFormModal extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            changePassword: false,
            isOpen: false,
            user: { ...UserService.emptyUserData },
            createAnother: false
        };

        this.open = this.open.bind(this);
        this.close = this.close.bind(this);
        this.onSubmitSuccess = this.onSubmitSuccess.bind(this);
        this.onFormSubmit = this.onFormSubmit.bind(this);
        this.createAnotherUser = this.createAnotherUser.bind(this);
        this.cancel = this.cancel.bind(this);
        this.generatePassword = this.generatePassword.bind(this);
    }

    open() {
        this.setState({
            password: '',
            changePassword: false,
            isOpen: true,
            user: {
                ...UserService.emptyUserData,
                ...(!this.props.create ? this.props.user : {})
            }
        })
    }

    openAnother() {
        this.setState(prevState => ({
            changePassword: false,
            isOpen: true,
            user: {
                id: '',
                password: '',
                firstName: '',
                lastName: '',
                email: '',
                enabled: prevState.user.enabled,
                groups: prevState.user.groups
            }
        }), () => {
            if (this.form) {
                this.form.reset();
            }
        });
    }

    close() {
        this.setState({ isOpen: false })
    }

    cancel() {
        this.setState({ isOpen: false, createAnother: false })
    }

    onSubmitSuccess(data) {
        this.close();
        this.props.onSaveSuccess && this.props.onSaveSuccess(data);
        if (this.state.createAnother) {
            this.openAnother();
        }
    }

    onFormSubmit(...args) {
        this.setState({ user: args[0] });

        if (this.props.create) {
            UserService.createUser(...args);
        } else {
            args[0].protectedUser = this.state.user.protectedUser;
            UserService.updateUser(...args);
        }
    }

    createAnotherUser(event) {
        this.setState({
            createAnother: event.target.checked
        })
    }

    shouldChangePasswordFieldShowUp() {
        return (this.props.create ||
            !this.props.create && this.state.changePassword);
    }

    shouldConfirmedPasswordFieldShowUp() {
        return (!this.props.create && this.state.changePassword);
    }

    generatePassword() {
        let stringInclude = '';
        stringInclude += '!"#$%&\'()*+,-./:;<=>?@[]^_`{|}~';
        stringInclude += '0123456789';
        stringInclude += 'abcdefghijklmnopqrstuvwxyz';
        stringInclude += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        let password ='';
        for (let i = 0; i < 40; i++) {
            password += stringInclude.charAt(Math.floor(Math.random() * stringInclude.length));
        }
        this.setState({ password: password });
    }

    render() {
        const { isOpen, user } = this.state;

        const form = (
            <Form
                ref={(element) => this.form = element}
                onSubmit={this.onFormSubmit}
                onSuccess={this.onSubmitSuccess}
            >
                {({ submitted }) => (
                    <>
                        <TextField
                            autocomplete='off'
                            defaultValue={user.id}
                            isRequired
                            isDisabled={!this.props.create}
                            label='Username'
                            name='id'
                        />

                        {!this.props.create && (
                            <Checkbox
                                defaultChecked={this.changePassword}
                                defaultValue={false}
                                hideLabel={true}
                                label='Change Password'
                                name='changePassword'
                                onChange={(e) => {
                                    this.setState({ changePassword: e.target.checked });
                                    if (!e.target.checked) {
                                        this.form.removeField('password');
                                        this.form.removeField('confirmedPassword');
                                    }
                                }}
                            />
                        )}

                        {this.shouldChangePasswordFieldShowUp() && (
                            <>
                                <TextField
                                    autocomplete='new-password'
                                    isRequired
                                    type='password'
                                    label='Password'
                                    name='password'
                                    value={this.state.password}
                                    onChange={(e) => {
                                        this.setState({ password: e.target.value })
                                    }}
                                    ref={(element) => this.passwordInput = element}
                                />
                                <PasswordActionsContainer>
                                    <PasswordActionButtonsContainer>
                                        <Button
                                            appearance='subtle'
                                            onClick={this.generatePassword}
                                        >
                                            Generate password
                                        </Button>
                                        <Button
                                            appearance='subtle'
                                            onClick={() => {
                                                navigator.clipboard.writeText(this.passwordInput.value)
                                            }}
                                        >
                                            Copy password
                                        </Button>
                                    </PasswordActionButtonsContainer>
                                    <Checkbox
                                        defaultChecked={false}
                                        defaultValue={false}
                                        label='Show Password'
                                        onChange={(e) => {
                                            this.passwordInput.type = e.target.checked ? 'text' : 'password';
                                        }}
                                    />
                                </PasswordActionsContainer>
                            </>
                        )}

                        {this.shouldConfirmedPasswordFieldShowUp() && (
                            <TextField
                                autocomplete='off'
                                isRequired
                                type='password'
                                label='Confirm Password'
                                name='confirmedPassword'
                            />
                        )}

                        <TextField
                            autocomplete='off'
                            defaultValue={user.firstName}
                            label='First name'
                            name='firstName'
                        />

                        <TextField
                            autocomplete='off'
                            defaultValue={user.lastName}
                            label='Last name'
                            name='lastName'
                        />

                        <TextField
                            autoComplete='off'
                            defaultValue={user.email}
                            label='Email'
                            name='email'
                        />

                        <GroupAutosuggestion
                            defaultValue={([...(filterOutEveryonePrincipal(user.groups) || [])])}
                            isMulti
                            label='Groups'
                            name='groups'
                            filterOut={[PRINCIPAL_EVERYONE]}
                        />
                        {
                            !user.protectedUser &&
                            <Checkbox
                                name='enabled'
                                defaultValue={user.enabled ? 'enabled' : ''}
                                defaultChecked={user.enabled}
                                label='Enabled'
                                hideLabel={true}
                            />
                        }
                        <FormFooter>
                            <ButtonGroup>
                                {this.props.create && (
                                    <Checkbox
                                        label='Create another'
                                        onChange={(event) => this.createAnotherUser(event)}
                                        defaultChecked={this.state.createAnother}
                                        overrides={{
                                            Label: {
                                                cssFn: (defaultStyles) => ({
                                                    ...defaultStyles,
                                                    cursor: 'pointer',
                                                    marginTop: '5px'
                                                })
                                            }
                                        }}
                                    />
                                )}
                                <Button
                                    appearance='primary'
                                    type='submit'
                                    isLoading={submitted}
                                >
                                    {this.props.create ? 'Create' : 'Save'}
                                </Button>
                                <Button
                                    appearance='subtle'
                                    onClick={this.cancel}
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
                        heading={this.props.create ? 'Create new user' : 'Edit user'}
                        onClose={this.close}
                    >
                        {form}
                    </ModalDialog>
                )}
            </ModalTransition>
        );
    }
}
