import React from 'react';
import DropdownMenu, { DropdownItem } from '@atlaskit/dropdown-menu';
import { Cell, Row } from '@atlaskit/table-tree';
import Tooltip from '@atlaskit/tooltip';

import { TableRowActionButton } from 'websight-admin/Buttons';
import { TableRowActionButtonsContainer } from 'websight-admin/Containers';
import ConfirmationModal from 'websight-admin/ConfirmationModal';

import AuthorizablesTags from './AuthorizablesTags.js';
import AuthorizableName from './AuthorizableName.js';
import UserFormModal from './UserFormModal.js';
import UserService from '../services/UserService.js';
import { fetchApplicableActions } from 'websight-admin/utils/ExtraActionsUtil';

export default class UserRow extends React.Component {
    constructor(props) {
        super(props);
        this.deleteUser = this.deleteUser.bind(this);
        this.state = {
            extraActions: props.extraActions
        }
    }

    toggleUserEnabled() {
        const { user, onAuthorizableAction } = this.props;

        UserService.toggleUserStatus(
            user.id,
            (data) => {
                this.props.onUpdate();
                if (onAuthorizableAction) {
                    onAuthorizableAction((data.entity.enabled ? 'enable' : 'disable'), data.entity);
                }
            }
        );
    }

    deleteUser() {
        const { user } = this.props;

        UserService.deleteUser(
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

    editButton() {
        const { user } = this.props;
        return (
            <TableRowActionButton
                tooltipContent='Edit'
                iconClassName='material-icons'
                iconName='edit'
                onClick={() => {
                    this.userEditModal.open();
                    if (this.props.onAuthorizableAction) {
                        this.props.onAuthorizableAction('read', user);
                    }
                }}
            />
        )
    }

    actionButtons() {
        const { user } = this.props;
        const { extraActions } = this.state;
        return (
            <TableRowActionButtonsContainer>
                {this.editButton()}
                {
                    (!user.protectedUser || (this.props.extraActions && this.props.extraActions.length > 0)) &&
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
                                    onClick={() => action.onClick(user.path)}>{action.label}
                                </DropdownItem>
                            )
                        })}
                        {!user.protectedUser &&
                            <>
                                <DropdownItem
                                    onClick={() => this.toggleUserEnabled()}>{this.props.user.enabled ? 'Disable' : 'Enable'}
                                </DropdownItem>
                                <DropdownItem
                                    onClick={() => this.deleteConfirmationModal.open()}>Delete
                                </DropdownItem>
                            </>
                        }
                    </DropdownMenu>
                }
                {extraActions && extraActions.map((action) => {
                    return action.modal(user);
                })}
                <ConfirmationModal
                    buttonText={'Delete'}
                    heading={'Delete user'}
                    appearance='danger'
                    message={(<span>Are you sure you want to permanently delete <b>{this.props.user.displayName}</b>?</span>)}
                    onConfirm={this.deleteUser}
                    ref={(element) => this.deleteConfirmationModal = element}
                />
            </TableRowActionButtonsContainer>
        )
    }

    render() {
        const { user, onUpdate } = this.props;

        const userRowCells = () => [
            <Cell key={`${user.id}-name`}>
                <AuthorizableName
                    authorizable={user}
                    icon='person'
                    enabledAware
                />
            </Cell>,
            <Cell singleLine key={`${user.id}-id`}>
                {user.enabled ? user.id :
                    <>
                        <del>{user.id}</del>
                        <div>(Disabled)</div>
                    </>
                }
            </Cell>,
            <Cell singleLine key={`${user.id}-statistics`}>
                {
                    <>
                        <div><strong>Count:</strong> {user.loginCount || '-'}</div>
                        <div>
                            <strong>Last: </strong>
                            {user.lastLoggedInRelative && (
                                <Tooltip content={user.lastLoggedIn} tag='span'>
                                    <span>{user.lastLoggedInRelative}</span>
                                </Tooltip>)
                            }
                            {!user.lastLoggedInRelative && (user.lastLoggedIn || '-')}
                        </div>
                    </>
                }
            </Cell>,
            <Cell key={`${user.id}-groups`}>
                <AuthorizablesTags
                    authorizables={this.props.user.groups}
                    expandable
                />
            </Cell>,
            <Cell singleLine key={`${user.id}-actions`}>
                {this.actionButtons()}
            </Cell>,
            <UserFormModal
                key={`${user.id}-modal`}
                ref={(element) => this.userEditModal = element}
                onSaveSuccess={(data) => {
                    onUpdate();
                    if (this.props.onAuthorizableAction) {
                        this.props.onAuthorizableAction('edit', data);
                    }
                }}
                create={false}
                user={user}
            />
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
