import React from 'react';
import { Cell, Row } from '@atlaskit/table-tree';
import styled from 'styled-components';
import Tooltip from '@atlaskit/tooltip';

import { TableRowActionButton } from 'websight-admin/Buttons';
import ConfirmationModal from 'websight-admin/ConfirmationModal';
import { TableRowActionButtonsContainer } from 'websight-admin/Containers';
import { colors } from 'websight-admin/theme';
import ContentTag from 'websight-admin/components/ContentTag';
import LabelValueTable from 'websight-admin/components/LabelValueTable';
import { WhiteTooltip } from 'websight-admin/Tooltips';

import PermissionsService from '../../services/PermissionsService.js';
import EditAcEntryModal from './AccessControlEntryModal.js';

const REP_GLOB = 'rep:glob';

const permissionTypeDotStyle = {
    fontSize: '14px',
    verticalAlign: 'text-bottom',
    marginRight: '6px'
}

const headerIconStyle = {
    fontSize: '22px',
    verticalAlign: 'middle',
    paddingRight: '5px',
    color: colors.grey
};

const HierarchyTooltipIcon = styled.i`
    font-size: 21px;
    margin-left: 8px;
    margin-top: 3px;
    color: ${colors.grey};
    cursor: pointer;
`;

const TableSectionHeaderContainer = styled.div`
    width: 100%;
    height: 50px;
    display: flex;
    justify-content: flex-start;
    align-items: center;
`;

const PermissionType = (props) => {
    const { allow } = props;
    const text = allow ? 'allow' : 'deny';
    const color = allow ? colors.green : colors.red;
    return (
        <div style={{ display: 'inline-block' }}>
            <i
                className='material-icons'
                style={{ ...permissionTypeDotStyle, color: color }}
            >
                fiber_manual_record
            </i>
            {text}
        </div>
    )
}

const Privileges = (props) => {
    const { privileges = [] } = props;
    return (
        <>
            {(privileges).map(privilege => (
                <div key={privilege}>
                    <ContentTag>{privilege}</ContentTag>
                </div>
            ))}
        </>
    )
}

const Restrictions = (props) => {
    const { restrictions = {} } = props;

    const appendRestriction = (restrictionsString, key, value) => {
        let newRestrictions = restrictionsString;
        if (newRestrictions) {
            newRestrictions += ', ';
        }
        newRestrictions += `"${value}"`;
        return newRestrictions;
    }

    return (
        <span>
            {Object.entries(restrictions).map(([type, value]) => {
                let stringValue = '';
                if (value instanceof Array) {
                    value.forEach((arrayValue) => {
                        stringValue = appendRestriction(stringValue, type, arrayValue);
                    })
                    if (type !== REP_GLOB) {
                        stringValue = '[' + stringValue + ']';
                    }
                } else {
                    stringValue = appendRestriction(stringValue, type, value);
                }
                return (
                    <span key={type}>
                        <b>{type}</b>={stringValue}<br />
                    </span>
                );
            })}
        </span>
    )
}

export default class AceRow extends React.Component {

    constructor(props) {
        super(props);
        this.deleteAcEntry = this.deleteAcEntry.bind(this);
    }

    deleteAcEntry(entry) {
        PermissionsService.deleteAclEntry(
            {
                authorizableId: this.props.authorizableId,
                entryId: entry.entryId,
                policyId: entry.policyId
            },
            () => {
                this.deleteConfirmationModal.close();
                this.props.onUpdate();
            },
            () => {
                this.deleteConfirmationModal.close();
            }
        )
    }

    cells(entry) {
        let cellsArray = [
            <Cell key='path'>
                <span style={{ overflowWrap: 'anywhere' }}>{entry.path}</span>
            </Cell>,
            <Cell key='type' singleLine>
                <PermissionType allow={entry.allow} />
            </Cell>,
            <Cell key='privileges'>
                <Privileges privileges={entry.privileges} />
            </Cell>,
            <Cell key='restrictions'>
                <Restrictions restrictions={entry.restrictions} />
            </Cell>
        ]
        if (!this.props.isReadOnly) {
            cellsArray = [...cellsArray, (
                <Cell key='actions' singleLine>
                    {this.actionButtons(entry)}
                </Cell>
            )]
        }
        return cellsArray;
    }

    actionButtons(entry) {
        return (
            <TableRowActionButtonsContainer>
                <TableRowActionButton
                    tooltipContent='Edit'
                    iconClassName='material-icons'
                    iconName='edit'
                    onClick={() => this.editAcEntryModal.open()}
                />
                <TableRowActionButton
                    tooltipContent='Delete'
                    iconClassName='material-icons'
                    iconName='close'
                    onClick={() => this.deleteConfirmationModal.open()}
                />
                <EditAcEntryModal
                    ref={(element) => this.editAcEntryModal = element}
                    authorizableId={this.props.authorizableId}
                    entryToEdit={entry}
                    onSaveSuccess={this.props.onUpdate}
                />
                <ConfirmationModal
                    buttonText={'Delete'}
                    heading={'Delete Access Control Entry'}
                    appearance='danger'
                    message={this.deleteConfirmationContent(entry)}
                    onConfirm={() => this.deleteAcEntry(entry)}
                    ref={(element) => this.deleteConfirmationModal = element}
                />
            </TableRowActionButtonsContainer>
        )
    }

    deleteConfirmationContent(entry) {
        const entryData = [
            { label: 'Authorizable ID', value: this.props.authorizableId },
            { label: 'Path', value: entry.path },
            { label: 'Permission Type', value: <PermissionType allow={entry.allow} /> },
            { label: 'Privileges', value: <Privileges privileges={entry.privileges} /> },
            { label: 'Restrictions', value: <Restrictions restrictions={entry.restrictions} /> }
        ]

        return (
            <>
                Are you sure you want to delete this Access Control Entry?
                <LabelValueTable
                    data={entryData}
                    labelWidth='120px'
                />
            </>
        )
    }

    hierarchyTooltipContent(sectionHeader, hierarchy) {
        return (<>
            Hierarchy: { [sectionHeader, ...hierarchy].join(' \u2192 ') }
        </>);
    }

    render() {
        const { entry } = this.props;
        const { sectionHeader, sectionHierarchy } = entry;

        return (
            <Row
                key={entry.path}
                itemId={entry.path}
            >
                {sectionHeader
                    ? (
                        <TableSectionHeaderContainer>
                            <span style={{ paddingLeft: '25px' }}>
                                <i className='material-icons' style={headerIconStyle}>people</i>
                                {sectionHeader}
                            </span>
                            {sectionHierarchy && sectionHierarchy.length > 0 && (
                                <Tooltip
                                    component={WhiteTooltip}
                                    tag='span'
                                    content={this.hierarchyTooltipContent(sectionHeader, sectionHierarchy)}>
                                    <HierarchyTooltipIcon className='material-icons'>info_outline</HierarchyTooltipIcon>
                                </Tooltip>
                            )}
                        </TableSectionHeaderContainer>
                    )
                    : this.cells(entry)
                }
            </Row>
        );
    }
}
