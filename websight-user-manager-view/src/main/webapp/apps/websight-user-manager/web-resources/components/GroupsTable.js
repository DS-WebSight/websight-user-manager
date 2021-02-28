import React from 'react';
import TableTree, { Header, Headers, Rows } from '@atlaskit/table-tree';
import GroupRow from './GroupRow.js';

import { NoTableDataContainer, TableTopAlignedContainer } from 'websight-admin/Containers';
import { LoadingWrapper } from 'websight-admin/Wrappers';

export default class GroupsTable extends React.Component {
    constructor(props) {
        super(props);

        this.onGroupUpdate = this.onGroupUpdate.bind(this);
    }

    onGroupUpdate() {
        this.props.refreshGroups();
    }

    render() {
        const { groups } = this.props;

        const tableTreeHeaders = (
            <Headers>
                <Header width={'calc(30% - 120px)'} style={{ minWidth: '200px', paddingLeft: '58px' }}>Name</Header>
                <Header width={'14%'}>ID</Header>
                <Header width={'18%'}>Parent groups</Header>
                <Header width={'18%'}>Members</Header>
                <Header width={'20%'}>Description</Header>
                <Header width={'120px'}>Actions</Header>
            </Headers>
        );

        return (
            <>
                <TableTree>
                    {tableTreeHeaders}
                    <LoadingWrapper isLoading={this.props.isLoading} spinnerSize='large'>
                        <TableTopAlignedContainer>
                            <Rows
                                items={groups}
                                render={(group) => (
                                    <GroupRow
                                        group={group}
                                        onUpdate={this.onGroupUpdate}
                                        onAuthorizableAction={this.props.onAuthorizableAction}
                                        extraActions={this.props.extraActions}
                                    />
                                )}
                            />
                        </TableTopAlignedContainer>
                        {!this.props.groups.length && this.props.isInitialized &&
                            <NoTableDataContainer>
                                <h3>No groups found.</h3>
                            </NoTableDataContainer>
                        }
                    </LoadingWrapper>
                </TableTree>
            </>
        )
    }
}
