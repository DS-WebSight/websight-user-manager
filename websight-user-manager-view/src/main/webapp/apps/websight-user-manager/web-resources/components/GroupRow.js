import React from 'react';
import DropdownMenu, { DropdownItem } from '@atlaskit/dropdown-menu';
import { Cell, Row } from '@atlaskit/table-tree';
import Tooltip from '@atlaskit/tooltip';

import { TableRowActionButton } from 'websight-admin/Buttons';
import ConfirmationModal from 'websight-admin/ConfirmationModal';
import { TableRowActionButtonsContainer } from 'websight-admin/Containers';
import { WhiteTooltip } from 'websight-admin/Tooltips';

import AuthorizablesTags from './AuthorizablesTags.js';
import GroupFormModal from './GroupFormModal.js';
import GroupService from '../services/GroupService.js';
import AuthorizableName from './AuthorizableName.js';
import { fetchApplicableActions } from 'websight-admin/utils/ExtraActionsUtil';

const groupName = (group) =>
    group.displayName ? group.displayName : group.id;

export default class GroupRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            extraActions: props.extraActions
        }
        this.deleteGroup = this.deleteGroup.bind(this);
    }

    deleteGroup() {
        const { group } = this.props;

        GroupService.deleteGroup(
            { id: group.id },
            () => {
                if (this.props.onAuthorizableAction) {
                    this.props.onAuthorizableAction('delete', group);
                }
                this.props.onUpdate();
            }
        )
    }

    editButton() {
        const { group } = this.props;
        return (
            <TableRowActionButton
                tooltipContent='Edit'
                iconClassName='material-icons'
                iconName='edit'
                onClick={() => {
                    this.groupEditModal.open();
                    if (this.props.onAuthorizableAction) {
                        this.props.onAuthorizableAction('read', group);
                    }
                }}
            />
        )
    }

    actionButtons() {
        const { extraActions } = this.state;
        return (
            <TableRowActionButtonsContainer>
                {this.editButton()}
                <DropdownMenu trigger='' triggerType='button' onOpenChange={(event) => {
                    if (event.isOpen) {
                        fetchApplicableActions(this.props.extraActions, this.props.group.path, (actions) => {
                            this.setState({ extraActions : actions })
                        })
                    }
                }}>
                    {extraActions && extraActions.map((action, index) => {
                        return (
                            <DropdownItem
                                key={index}
                                onClick={() => action.onClick(this.props.group.path)}>{action.label}
                            </DropdownItem>
                        )
                    })}
                    <DropdownItem onClick={() => this.deleteConfirmationModal.open()}>Delete</DropdownItem>
                </DropdownMenu>
                {extraActions && extraActions.map((action) => {
                    return action.modal(this.props.group);
                })}
                <ConfirmationModal
                    buttonText={'Delete'}
                    heading={'Delete group'}
                    appearance='danger'
                    message={(<span>Are you sure you want to permanently delete <b>{groupName(this.props.group)}</b>?</span>)}
                    onConfirm={this.deleteGroup}
                    ref={(element) => this.deleteConfirmationModal = element}
                />
            </TableRowActionButtonsContainer>
        );
    }

    render() {
        const { group, onUpdate } = this.props;

        const groupRowCells = () => (
            [
                <Cell key={`${group.id}-name`}>
                    <AuthorizableName
                        authorizable={group}
                        icon='group'
                    />
                </Cell>,
                <Cell singleLine key={`${group.id}-id`}>
                    {group.id}
                </Cell>,
                <Cell key={`${group.id}-parentGroups`}>
                    <AuthorizablesTags
                        authorizables={this.props.group.parentGroups}
                        expandable
                    />
                </Cell>,
                <Cell key={`${group.id}-members`}>
                    <AuthorizablesTags
                        authorizableId={group.id}
                        authorizables={this.props.group.members}
                        expandable
                    />
                </Cell>,
                <Cell singleLine key={`${group.id}-description`}>
                    <Tooltip component={WhiteTooltip} content={group.description} delay={0}>
                        <div style={{ whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>
                            {group.description}
                        </div>
                    </Tooltip>
                </Cell>,
                <Cell singleLine key={`${group.id}-buttons`}>
                    {this.actionButtons()}
                </Cell>,
                <GroupFormModal
                    key={`${group.id}-modal`}
                    ref={(element) => this.groupEditModal = element}
                    onSaveSuccess={(data) => {
                        onUpdate();
                        if (this.props.onAuthorizableAction) {
                            this.props.onAuthorizableAction('edit', data);
                        }
                    }}
                    create={false}
                    group={group}
                />
            ]
        );

        return (
            <Row
                key={group.id}
                itemId={group.id}
            >
                {groupRowCells()}
            </Row>
        )
    }
}
