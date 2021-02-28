import React from 'react';
import TableTree, { Header, Headers, Rows } from '@atlaskit/table-tree';

import { LoadingWrapper } from 'websight-admin/Wrappers';
import { NoTableDataContainer, TableTopAlignedContainer } from 'websight-admin/Containers';

import UserRow from './UserRow.js';

export default class UsersTable extends React.Component {
    constructor(props) {
        super(props);

        this.onUserUpdate = this.onUserUpdate.bind(this);
    }

    onUserUpdate() {
        this.props.refreshUsers();
    }

    render() {
        const { users } = this.props;

        const tableTreeHeaders = (
            <Headers>
                <Header width={'calc(40% - 130px)'} style={{ minWidth: '200px', paddingLeft: '58px' }}>Name</Header>
                <Header width={'16%'}>Username</Header>
                <Header width={'21%'}>Login details</Header>
                <Header width={'23%'}>Groups</Header>
                <Header width={'130px'}>Actions</Header>
            </Headers>
        );

        return (
            <>
                <TableTree>
                    {tableTreeHeaders}
                    <LoadingWrapper isLoading={this.props.isLoading} spinnerSize='large'>
                        <TableTopAlignedContainer>
                            <Rows
                                items={users}
                                render={(user) => (
                                    <UserRow
                                        user={user}
                                        onUpdate={this.onUserUpdate}
                                        onAuthorizableAction={this.props.onAuthorizableAction}
                                        extraActions={this.props.extraActions}
                                    />
                                )}
                            />
                        </TableTopAlignedContainer>
                        {!this.props.users.length && this.props.isInitialized &&
                            <NoTableDataContainer>
                                <h3>No users found.</h3>
                            </NoTableDataContainer>
                        }
                    </LoadingWrapper>

                </TableTree>
            </>
        )
    }
}
