import React from 'react';
import { Cell, Row } from '@atlaskit/table-tree';

import { TableRowActionButton } from 'websight-admin/Buttons';
import { TableRowActionButtonsContainer } from 'websight-admin/Containers';
import ConfirmationModal from 'websight-admin/ConfirmationModal';

import SystemUserService from '../services/SystemUserService.js';
import AuthorizableName from './AuthorizableName.js';

export default class SystemUserRow extends React.Component {
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
        return (
            <TableRowActionButtonsContainer>
                <TableRowActionButton
                    tooltipContent='Delete'
                    iconClassName='material-icons'
                    iconName='delete'
                    onClick={() => this.deleteConfirmationModal.open()}
                />
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
