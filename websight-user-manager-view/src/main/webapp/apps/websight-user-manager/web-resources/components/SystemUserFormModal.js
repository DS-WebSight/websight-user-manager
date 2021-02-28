import React from 'react';
import Button, { ButtonGroup } from '@atlaskit/button';
import ModalDialog, { ModalTransition } from '@atlaskit/modal-dialog';
import TextField from '@atlaskit/textfield';

import Form, { FormFooter } from 'websight-rest-atlaskit-client/Form';

import SystemUserService from './../services/SystemUserService.js';

export default class SystemUserFormModal extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isOpen: false,
            user: { id: '' }
        };

        this.open = this.open.bind(this);
        this.close = this.close.bind(this);
        this.onSubmitSuccess = this.onSubmitSuccess.bind(this);
        this.onFormSubmit = this.onFormSubmit.bind(this);
    }

    open() {
        this.setState({
            isOpen: true,
            user: { id: '' }
        })
    }

    close() {
        this.setState({ isOpen: false })
    }

    onSubmitSuccess(data) {
        this.close();
        this.props.onSaveSuccess && this.props.onSaveSuccess(data);
    }

    onFormSubmit(...args) {
        this.setState({ user: args[0] });
        SystemUserService.createSystemUser(...args);
    }

    render() {
        const { isOpen, user } = this.state;

        const form = (
            <Form
                onSubmit={this.onFormSubmit}
                onSuccess={this.onSubmitSuccess}
            >
                {({ submitted }) => (
                    <>
                        <TextField
                            autocomplete='off'
                            defaultValue={user.id}
                            isRequired
                            label='ID'
                            name='id'
                        />
                        <FormFooter>
                            <ButtonGroup>
                                <Button
                                    appearance='primary'
                                    type='submit'
                                    isLoading={submitted}
                                >
                                    Create
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
                        heading={'Create new system user'}
                        onClose={this.close}
                    >
                        {form}
                    </ModalDialog>
                )}
            </ModalTransition>
        );
    }
}
