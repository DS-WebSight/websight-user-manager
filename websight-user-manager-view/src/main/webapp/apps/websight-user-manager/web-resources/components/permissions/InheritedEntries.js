import React from 'react';
import Panel from '@atlaskit/panel';
import TableTree, { Header, Headers, Rows } from '@atlaskit/table-tree';
import styled from 'styled-components';

import { LoadingWrapper } from 'websight-admin/Wrappers';
import { NoTableDataContainer, TableTopAlignedContainer } from 'websight-admin/Containers';

import AceRow from './AceRow.js';

const InheritedEntriesContainer = styled.div`
    margin-top: 64px;
`;

export default class InheritedEntries extends React.Component {
    render() {
        const { inheritedEntries, isLoadingEntries } = this.props;

        const tableTreeHeaders = (
            <Headers>
                <Header width={'calc(90% - 700px)'}>Path</Header>
                <Header width={'110px'}>Permission</Header>
                <Header width={'260px'}>Privileges</Header>
                <Header width={'calc(330px + 10%)'}>Restrictions</Header>
            </Headers>
        );

        return (
            <InheritedEntriesContainer>
                <Panel
                    header='Inherited ACEs'
                >
                    <TableTree key='inheritedEntries'>
                        {tableTreeHeaders}
                        <LoadingWrapper isLoading={isLoadingEntries} spinnerSize='large'>
                            <TableTopAlignedContainer>
                                <Rows
                                    items={inheritedEntries}
                                    render={(row) => (
                                        <AceRow
                                            isReadOnly
                                            entry={row}
                                        />
                                    )}
                                />
                                {!inheritedEntries.length && !isLoadingEntries && (
                                    <NoTableDataContainer>
                                        <i>No Inherited Access Control Entries.</i>
                                    </NoTableDataContainer>
                                )}
                            </TableTopAlignedContainer>
                        </LoadingWrapper>
                    </TableTree>
                </Panel>
            </InheritedEntriesContainer>
        );
    }
}