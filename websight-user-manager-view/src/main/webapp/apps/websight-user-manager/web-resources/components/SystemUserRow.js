import React from 'react';
import { Cell, Row } from '@atlaskit/table-tree';
import DropdownMenu, { DropdownItem } from '@atlaskit/dropdown-menu';
import { TableRowActionButtonsContainer } from 'websight-admin/Containers';
import ConfirmationModal from 'websight-admin/ConfirmationModal';

import SystemUserService from '../services/SystemUserService.js';
import AuthorizableName from './AuthorizableName.js';
import { fetchApplicableActions } from 'websight-admin/utils/ExtraActionsUtil';

export default class SystemUserRow extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            extraActions: props.extraActions
        }
    }
    deleteUser() {
        const { user } = this.props;

        SystemUserService.deleteUser(
            user.id,
            () => {
                if (this.props.onAuthorizableAction) {
                    this.props.onAuthorizableAction('delete', user);
                }
                this.props.onUpdate();
            },
            () => this.deleteConfirmationModal.close()
        )
    }

    actionButtons() {
        const { extraActions } = this.state;
        return (
            <TableRowActionButtonsContainer>
                <DropdownMenu trigger='' triggerType='button' onOpenChange={(event) => {
                    if (event.isOpen) {
                        fetchApplicableActions(this.props.extraActions, this.props.user.path, (actions) => {
                            this.setState({ extraActions : actions })
                        })
                    }
                }}>
                    {extraActions && extraActions.map((action, index) => {
                        return (
                            <DropdownItem
                                key={index}
                                onClick={() => action.onClick(this.props.user.path)}>{action.label}
                            </DropdownItem>
                        )
                    })}
                    <DropdownItem onClick={() => this.deleteConfirmationModal.open()}>Delete</DropdownItem>
                </DropdownMenu>
                {extraActions && extraActions.map((action) => {
                    return action.modal(this.props.user);
                })}
                <ConfirmationModal
                    buttonText={'Delete'}
                    heading={'Delete user'}
                    appearance='danger'
                    message={(<span>Are you sure you want to permanently delete <b>{this.props.user.id}</b>?</span>)}
                    onConfirm={() => this.deleteUser()}
                    ref={(element) => this.deleteConfirmationModal = element}
                />
            </TableRowActionButtonsContainer>
        )
    }

    render() {
        const { user } = this.props;

        const userRowCells = () => [
            <Cell key={`${user.id}-id`}>
                <AuthorizableName
                    authorizable={user}
                    icon='settings'
                />
            </Cell>,
            <Cell singleLine key={`${user.id}-actions`}>
                {this.actionButtons()}
            </Cell>
        ];

        return (
            <Row
                key={user.id}
                itemId={user.id}
            >
                {userRowCells()}
            </Row>
        )
    }
}
