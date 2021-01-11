import React from 'react';
import Button from '@atlaskit/button';
import { Checkbox } from '@atlaskit/checkbox';
import TableTree, { Cell, Header, Headers, Row, Rows } from '@atlaskit/table-tree';
import styled from 'styled-components';

import { colors } from 'websight-admin/theme';

import PermissionDetails from './PermissionDetails.js';
import PermissionsService from '../../services/PermissionsService.js';
import EllipsisSpanWithTooltip from '../EllipsisSpanWithTooltip.js';

const notAllowedStyle = {
    cursor: 'not-allowed'
}

const blockedContentStyle = {
    pointerEvents: 'none'
}

const centerContentStyle = {
    justifyContent: 'center'
};

const checkboxStyle = {
    width: '25px',
    cursor: 'pointer'
};

const iconStyle = {
    fontSize: '20px',
    display: 'inline',
    verticalAlign: 'bottom',
    padding: '0 5px 0 0',
    color: colors.grey
};

const saveButtonStyle = {
    margin: '0 20px 0 0'
};

const PathCellContainer = styled.span`
    cursor: pointer;
    display: flex;
`;

const PermissionsSectionContainer = styled.div`
    display: block;
    margin: 10px 0 0;
    width: 100%;
    margin-bottom: 40px;

    div[class^='styled__TreeRowContainer']:hover {
        background-color: ${colors.veryLightGrey};
    }
`;

const SaveButtonContainer = styled.div`
    align-items: flex-end;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    width: 100%;
`;

const StickyContentContainer = styled.div`
    background: ${colors.white}
    padding-top: 10px;
    position: sticky;
    top: 56px;
    z-index: 1;
`;

const EllipsisCell = styled(Cell)`
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
`;

const IS_EXPANDED_FIELD = 'isExpanded';
const CHILDREN_FIELD = 'children';
const ACTIONS_FIELD = 'actions';
const DECLARED_ACTIONS_FIELD = 'declaredActions';
const ACTION_FIELDS = ['read', 'modify', 'create', 'delete', 'admin'];


export default class BasicPermissions extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            changesMade: false,
            permissions: [],
            changelog: [],
            expandedPermissions: []
        };

        this.getPermissionsOnExpand = this.getPermissionsOnExpand.bind(this);
        this.getPermissionsOnSaveSuccess = this.getPermissionsOnSaveSuccess.bind(this);
        this.updatePermission = this.updatePermission.bind(this);
        this.updatePermissionActions = this.updatePermissionActions.bind(this);
        this.onTogglePermissionExpand = this.onTogglePermissionExpand.bind(this);
        this.onPermissionActionChange = this.onPermissionActionChange.bind(this);
    }

    componentDidMount() {
        const { authorizableId } = this.props;

        PermissionsService.getPermissions(
            { authorizableId, path: '/' },
            (data) => {
                this.setState({ permissions: data, expandedPermissions: [] });
            })
    }

    resetChanges() {
        const { authorizableId } = this.props;
        const { expandedPermissions } = this.state;

        const paths = ['/', ...expandedPermissions];
        PermissionsService.getPermissions(
            { authorizableId, paths },
            (data) => {
                this.setState({ changesMade: false, changelog: [] });
                this.updatePermissions(data, [ACTIONS_FIELD, DECLARED_ACTIONS_FIELD]);
            })
    }

    changesMade() {
        return this.state.changesMade;
    }

    getPermissionsOnExpand(path) {
        const { authorizableId } = this.props;

        PermissionsService.getPermissions(
            { authorizableId, path },
            (data) => this.updatePermissions(data, [CHILDREN_FIELD])
        );
    }

    getPermissionsOnSaveSuccess() {
        const { changelog, expandedPermissions } = this.state;
        const { authorizableId, onAuthorizableAction, displayName, type } = this.props;

        const paths = [...changelog.map((log) => log.path), ...expandedPermissions];

        this.setState(
            { changelog: [] },
            () => {
                PermissionsService.getPermissions(
                    { authorizableId, paths },
                    (data) => {
                        this.updatePermissions(data, [ACTIONS_FIELD, DECLARED_ACTIONS_FIELD]);
                        if (onAuthorizableAction) {
                            onAuthorizableAction('edit', {
                                id: authorizableId,
                                displayName,
                                type
                            });
                        }
                    }
                );
            }
        )
    }

    updatePermission(permission, field, value, permissions = this.state.permissions) {
        let rowFound = false;

        permissions.forEach((row) => {
            if (row.id === permission.id) {
                row[field] = value;
                rowFound = true;

            }
        });

        if (rowFound) {
            return permissions;
        } else {
            return permissions.map((row) => {
                if (row.hasChildren && row.children.length) {
                    row.children = this.updatePermission(permission, field, value, row.children)
                }
                return row;
            });
        }
    }

    updatePermissions(changedPermissions, fields = []) {
        this.setState((prevState) => {
            let { permissions } = prevState;

            const updatePermissionForFields = (permission) => {
                fields.forEach((field => {
                    permissions = this.updatePermission(permission, field, permission[field], permissions);
                }));
            }

            changedPermissions.forEach((row) => {
                updatePermissionForFields(row);

                if (row.hasChildren) {
                    row.children.forEach((childRow) =>
                        updatePermissionForFields(childRow)
                    );
                }

            });

            return ({ permissions: permissions });
        });
    }

    updatePermissionActions(permission, field, value) {
        const actions = { ...permission.actions, [field]: value };
        this.updateChangelog(permission, field, value);
        return this.updatePermission(permission, ACTIONS_FIELD, actions);
    }

    updateChangelog(row, action, value) {
        this.setState((prevState) => {
            let { changelog } = prevState;

            const permissionInChangelog = changelog
                .find(({ path }) => path === row.path);

            if (permissionInChangelog) {
                changelog = changelog.map((item) => {
                    if (item.path === row.path) {
                        return { ...item, [action]: value };
                    } else {
                        return item;
                    }
                });
            } else {
                changelog.push({ path: row.path, [action]: value })
            }

            return { changelog: changelog.filter((item) => Object.keys(item).length > 1) }
        });
    }

    onTogglePermissionExpand(row) {
        this.setState(
            (prevState) => {
                const addPermissionToExpandedPermissions = () => {
                    const { expandedPermissions } = prevState;

                    if (!expandedPermissions.includes(row.path)) {
                        expandedPermissions.push(row.path)
                    }
                    return expandedPermissions;
                }

                const permissions = this.updatePermission(row, IS_EXPANDED_FIELD, !row[IS_EXPANDED_FIELD]);

                return { permissions, expandedPermissions: addPermissionToExpandedPermissions() };
            },
            () => {
                if (row[IS_EXPANDED_FIELD] && row.hasChildren && !row.children.length) {
                    this.getPermissionsOnExpand(row.path);
                }
            });
    }

    onPermissionActionChange(row, field) {
        const currentValue = row.actions[field];
        const updatedValue = currentValue === true ? false : (currentValue === false ? null : true);
        const permissions = this.updatePermissionActions(row, field, updatedValue);
        this.setState({ changesMade: true, permissions });
    }

    onSavePermissions() {
        const { authorizableId } = this.props;
        const { changelog } = this.state;

        this.setState(
            { changesMade: false },
            () => PermissionsService.updatePermissions({ changelog, authorizableId }, this.getPermissionsOnSaveSuccess)
        );
    }

    render() {
        const { changesMade, permissions } = this.state;
        const { isAdmin } = this.props;

        const checkboxTheme = (tokens, row, field) => {
            const action = row.actions[field];
            const declaredAction = row.declaredActions[field];
            const isDeclared = declaredAction && declaredAction.effective &&
                Object.values(declaredAction.effective)
                    .some((value) => value.authorizableId === this.props.authorizableId);

            const isUpdated = this.state.changelog
                .some((entry) => entry.path === row.path && entry[field] !== undefined);

            const setColor = (colorsObject, color) => {
                Object.keys(colorsObject).forEach((key) => colorsObject[key] = color);
            }

            const setColors = (borderColor, boxColor, tickColor) => {
                setColor(tokens.icon.borderColor, borderColor);
                setColor(tokens.icon.boxColor, boxColor);
                setColor(tokens.icon.tickColor, tickColor);
            }

            if (action !== null && !isDeclared && !isUpdated) {
                setColors(colors.lightGrey, colors.lightGrey, colors.white);
            } else if (action === false) {
                setColors(colors.red, colors.red, colors.white);
            } else if (action === true) {
                setColors(colors.green, colors.green, colors.white);
            }
            return tokens;
        }

        const permissionRow = (row) => {
            return (
                <Row
                    key={row.path}
                    itemId={row.path}
                    items={row.children}
                    hasChildren={row.hasChildren}
                    isExpanded={row.isExpanded}
                    onExpand={() => this.onTogglePermissionExpand(row)}
                    onCollapse={() => this.onTogglePermissionExpand(row)}
                >
                    <EllipsisCell singleLine>
                        <PathCellContainer onClick={() => this.onTogglePermissionExpand(row)}>
                            <i
                                className='material-icons-outlined'
                                style={iconStyle}
                            >
                                {(row.hasChildren) ? 'folder' : 'insert_drive_file'}
                            </i>
                            <EllipsisSpanWithTooltip text={row.name} />
                        </PathCellContainer>
                    </EllipsisCell>
                    {ACTION_FIELDS.map((field) => (
                        <Cell key={`${field}-${row.path}`} singleLine style={centerContentStyle}>
                            <Checkbox
                                isChecked={row.actions[field]}
                                isIndeterminate={row.actions[field] === false}
                                label={(
                                    <PermissionDetails
                                        row={row} field={field}
                                        authorizableId={this.props.authorizableId}
                                    />
                                )}
                                onChange={() => this.onPermissionActionChange(row, field)}
                                theme={(current, props) => checkboxTheme(current(props), row, field)}
                                style={checkboxStyle}
                            />
                        </Cell>
                    ))}
                </Row>
            );
        }

        return (
            <>
                <PermissionsSectionContainer className='permissions-table'>
                    <TableTree>
                        <StickyContentContainer>
                            <SaveButtonContainer>
                                {!isAdmin && (
                                    <>
                                        <div>
                                            {changesMade && <Button
                                                appearance='subtle'
                                                onClick={() => this.resetChanges()}
                                                style={{ marginRight: '4px' }}
                                            >
                                                Cancel
                                            </Button>
                                            }
                                            <Button
                                                appearance={changesMade ? 'primary' : 'default'}
                                                isDisabled={!changesMade}
                                                onClick={() => this.onSavePermissions()}
                                                style={saveButtonStyle}
                                            >
                                                Save
                                            </Button>
                                        </div>
                                        <div style={saveButtonStyle}>
                                            <small>
                                                Save is required to recalculate effective permissions
                                            </small>
                                        </div>
                                    </>
                                )}
                                {isAdmin && (
                                    <i style={{ margin: '0 20px 20px 0' }}>
                                        Admin user has full access to the repository
                                    </i>
                                )}
                            </SaveButtonContainer>
                            <Headers>
                                <Header width={'calc(100% - 400px)'}>Path</Header>
                                <Header width={'80px'} style={centerContentStyle}>Read</Header>
                                <Header width={'80px'} style={centerContentStyle}>Modify</Header>
                                <Header width={'80px'} style={centerContentStyle}>Create</Header>
                                <Header width={'80px'} style={centerContentStyle}>Delete</Header>
                                <Header width={'80px'} style={centerContentStyle}>All</Header>
                            </Headers>
                        </StickyContentContainer>
                        <div style={isAdmin ? notAllowedStyle : null}>
                            <div style={isAdmin ? blockedContentStyle : null}>
                                <Rows
                                    items={permissions}
                                    render={(row) => permissionRow(row)}
                                />
                            </div>
                        </div>
                    </TableTree>
                </PermissionsSectionContainer>
            </>
        );
    }
}