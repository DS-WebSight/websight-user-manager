import React from 'react';
import TableTree, { Header, Headers, Rows } from '@atlaskit/table-tree';

import { LoadingWrapper } from 'websight-admin/Wrappers';
import { NoTableDataContainer, TableTopAlignedContainer } from 'websight-admin/Containers';

import SystemUserRow from './SystemUserRow.js';

export default class SystemUsersTable extends React.Component {
    render() {
        const { users } = this.props;

        const tableTreeHeaders = (
            <Headers>
                <Header width={'calc(100% - 90px)'} style={{ minWidth: '200px', paddingLeft: '58px' }}>ID</Header>
                <Header width={'90px'}>Actions</Header>
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
                                    <SystemUserRow
                                        user={user}
                                        onUpdate={() => this.props.refreshUsers()}
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
