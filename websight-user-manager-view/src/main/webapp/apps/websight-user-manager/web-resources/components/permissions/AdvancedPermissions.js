import React from 'react';
import Button from '@atlaskit/button';
import TableTree, { Header, Headers, Rows } from '@atlaskit/table-tree';
import styled from 'styled-components';

import { NoTableDataContainer, TableTopAlignedContainer } from 'websight-admin/Containers';
import { colors } from 'websight-admin/theme';
import { LoadingWrapper } from 'websight-admin/Wrappers';

import PermissionsService from '../../services/PermissionsService.js';
import CreateAcEntryModal from './AccessControlEntryModal.js';
import AceRow from './AceRow.js';
import InheritedEntries from './InheritedEntries.js';

const addEntryButtonStyle = {
    margin: '0 20px 0 0'
};

const PermissionsSectionContainer = styled.div`
    display: block;
    margin: 10px 0 0;
    width: 100%;
    margin-bottom: 40px;
`;

const StickyContentContainer = styled.div`
    background: ${colors.white}
    padding-top: 10px;
    position: sticky;
    top: 56px;
    z-index: 1;
`;

const AddEntryButtonContainer = styled.div`
    align-items: flex-end;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    width: 100%;
    margin-bottom: 20px;
`;

const groupEntries = (entries) => {
    const groupedEntries = {};
    (entries || []).forEach(entry => {
        const { authorizableId } = entry;
        if (authorizableId in groupedEntries) {
            groupedEntries[authorizableId] = [...groupedEntries[authorizableId], entry];
        } else {
            groupedEntries[authorizableId] = [entry];
        }
    })
    let entriesWithSections = [];
    Object.entries(groupedEntries).forEach(([authorizableId, entriesArray]) => {
        entriesWithSections = [
            ...entriesWithSections,
            {
                sectionHeader: authorizableId,
                sectionHierarchy: entriesArray.length ? entriesArray[0].membershipHierarchy || [] : []
            }
        ];
        (entriesArray || []).forEach(entry => {
            entriesWithSections = [...entriesWithSections, entry];
        })
    })
    return entriesWithSections;
}

export default class AdvancedPermissions extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isLoadingEntries: false,
            entries: [],
            inheritedEntries: []
        }
        this.loadEntries = this.loadEntries.bind(this);
    }

    componentDidMount() {
        this.loadEntries();
    }

    loadEntries() {
        const { authorizableId } = this.props;

        this.setState({
            isLoadingEntries: true
        }, () => PermissionsService.findAclEntries(
            { authorizableId: authorizableId },
            (data) => {
                this.setState({
                    entries: data.entries,
                    inheritedEntries: groupEntries(data.inheritedEntries),
                    isLoadingEntries: false
                });
            },
            () => {
                this.setState({
                    isLoadingEntries: false
                })
            }
        ));
    }

    render() {
        const { isAdmin, authorizableId, displayName, type, onAuthorizableAction } = this.props;
        const { inheritedEntries, isLoadingEntries } = this.state;

        const tableTreeHeaders = (
            <Headers>
                <Header width={'calc(90% - 700px)'}>Path</Header>
                <Header width={'110px'}>Permission</Header>
                <Header width={'260px'}>Privileges</Header>
                <Header width={'calc(200px + 10%)'}>Restrictions</Header>
                <Header width={'130px'}>Actions</Header>
            </Headers>
        );

        return (
            <>
                <PermissionsSectionContainer>
                    <TableTree key='entries'>
                        <StickyContentContainer>
                            <div style={{ display: 'flex' }}>
                                <AddEntryButtonContainer>
                                    {!isAdmin && (
                                        <Button
                                            onClick={() => this.createAcEntryModal.open()}
                                            style={addEntryButtonStyle}
                                        >
                                            Add ACE
                                        </Button>
                                    )}
                                    {isAdmin && (
                                        <i style={{ marginRight: '20px' }}>
                                            Admin user has full access to the repository
                                        </i>
                                    )}
                                </AddEntryButtonContainer>
                            </div>
                            {tableTreeHeaders}
                        </StickyContentContainer>
                        <LoadingWrapper isLoading={this.state.isLoadingEntries} spinnerSize='large'>
                            <TableTopAlignedContainer>
                                <Rows
                                    items={this.state.entries}
                                    render={(row) => (
                                        <AceRow
                                            authorizableId={this.props.authorizableId}
                                            entry={row}
                                            onUpdate={() => {
                                                this.loadEntries();
                                                if (onAuthorizableAction) {
                                                    onAuthorizableAction('edit', {
                                                        id: authorizableId,
                                                        displayName,
                                                        type
                                                    });
                                                }
                                            }}
                                        />
                                    )}
                                />
                            </TableTopAlignedContainer>
                            {!this.state.entries.length && !this.state.isLoadingEntries && (
                                <NoTableDataContainer>
                                    <i>No Access Control Entries.</i>
                                </NoTableDataContainer>
                            )}
                        </LoadingWrapper>
                    </TableTree>
                    <InheritedEntries inheritedEntries={inheritedEntries} isLoadingEntries={isLoadingEntries} />
                    <CreateAcEntryModal
                        ref={(element) => this.createAcEntryModal = element}
                        authorizableId={this.props.authorizableId}
                        onSaveSuccess={() => {
                            this.loadEntries();
                            if (onAuthorizableAction) {
                                onAuthorizableAction('edit', {
                                    id: authorizableId,
                                    displayName,
                                    type
                                });
                            }
                        }}
                    />
                </PermissionsSectionContainer>
            </>
        );
    }
}
