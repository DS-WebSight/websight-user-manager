import React from 'react';
import { Redirect } from 'react-router-dom';
import Button from '@atlaskit/button';
import Tabs from '@atlaskit/tabs';
import PageHeader from '@atlaskit/page-header';

import Breadcrumbs from 'websight-admin/Breadcrumbs';
import ConfirmationModal from 'websight-admin/ConfirmationModal';
import { colors } from 'websight-admin/theme';

import GroupDetailsGeneral from '../components/GroupDetailsGeneral.js'
import GroupFormModal from '../components/GroupFormModal.js';
import AdvancedPermissions from '../components/permissions/AdvancedPermissions.js';
import BasicPermissions from '../components/permissions/BasicPermissions.js';
import GroupService from '../services/GroupService.js';
import { getAuthorizableDisplayName, getTabIndex, setTabNameInHash } from '../utils/AuthorizableUtil.js';
import { USER_MANAGER_ROOT_PATH } from '../utils/UserManagerConstants.js';

const headerIconStyle = {
    fontSize: '31px',
    verticalAlign: 'bottom',
    paddingRight: '5px',
    color: colors.grey
};

export default class GroupDetailsPage extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            currentTabIndex: 0,
            tabIndexToConfirm: null,
            group: {
                ...GroupService.emptyGroupData
            },
            redirect: false
        }

        this.getGroup = this.getGroup.bind(this);
        this.onHashChange = this.onHashChange.bind(this);
        this.setGroupFromResponseData = this.setGroupFromResponseData.bind(this);
        this.changeTab = this.changeTab.bind(this);
        this.checkIfCurrentTabDataChanged = this.checkIfCurrentTabDataChanged.bind(this);
    }

    componentDidMount() {
        window.addEventListener('hashchange', this.onHashChange);
        this.loadDataFromUrl();
    }

    componentWillUnmount() {
        window.removeEventListener('hashchange', this.onHashChange);
    }

    onHashChange() {
        this.loadDataFromUrl();
    }

    loadDataFromUrl() {
        this.getGroup();
    }

    setTabInHash(tabIndex) {
        setTabNameInHash('groups', this.props.match.params.groupId, tabIndex);
    }

    getGroup() {
        const params = {
            authorizableId: decodeURIComponent(this.props.match.params.groupId)
        };

        const tabIndex = getTabIndex(this.props.match.params.tabName);
        this.setTabInHash(tabIndex);

        const onSuccess = (data) => {
            this.setState({ group: data, currentTabIndex: tabIndex });
            if (this.props.onAuthorizableAction) {
                this.props.onAuthorizableAction('read', data);
            }
        };

        const onFailure = () => {
            if (this.props.onAuthorizableAction) {
                this.props.onAuthorizableAction('out-of-date-view', { id: params.authorizableId, type: 'group' });
            }
            this.setState({ redirect: true });
        }

        GroupService.getGroup(params, onSuccess, onFailure);
    }

    setGroupFromResponseData(data) {
        this.setState((prevState) => ({ group: { ...prevState.group, ...data } }));
    }

    checkIfCurrentTabDataChanged() {
        const basicPermissionsTab = 1;
        return (this.state.currentTabIndex === basicPermissionsTab
            && this.basicPermissionsTab !== null
            && this.basicPermissionsTab.changesMade());
    }

    changeTab(changeConfirmed, destinationTabIndex = this.state.tabIndexToConfirm) {
        if (destinationTabIndex === this.state.currentTabIndex) {
            return;
        }
        if (changeConfirmed || !this.checkIfCurrentTabDataChanged()) {
            this.setState(
                {
                    currentTabIndex: destinationTabIndex,
                    tabIndexToConfirm: null
                },
                () => {
                    this.changeTabConfirmationModal.close();
                    this.setTabInHash(destinationTabIndex);
                }
            )
        } else {
            this.setState(
                { tabIndexToConfirm: destinationTabIndex },
                this.changeTabConfirmationModal.open()
            )
        }
    }

    render() {
        const { currentTabIndex, group, redirect } = this.state
        const tabs = [
            {
                label: 'Details',
                content: <GroupDetailsGeneral group={group} />
            },
            {
                label: 'Permissions',
                content: (
                    <BasicPermissions
                        authorizableId={group.id}
                        ref={(element) => this.basicPermissionsTab = element}
                        onAuthorizableAction={this.props.onAuthorizableAction}
                        displayName={getAuthorizableDisplayName(group)}
                        type={group.type}
                    />
                )
            },
            {
                label: 'Advanced Permissions',
                content: (
                    <AdvancedPermissions
                        authorizableId={group.id}
                        onAuthorizableAction={this.props.onAuthorizableAction}
                        displayName={getAuthorizableDisplayName(group)}
                        type={group.type}
                    />
                )
            }
        ];

        return (
            <>
                {redirect && <Redirect to='/groups' />}
                <PageHeader
                    actions={<Button onClick={() => this.groupEditModal.open()}>Edit</Button>}
                    breadcrumbs={
                        <Breadcrumbs breadcrumbs={[
                            { text: 'User Manager', path: USER_MANAGER_ROOT_PATH, reactPath: '' },
                            { text: 'Groups', reactPath: '/groups' },
                            { text: group.displayName, reactPath: `/groups/${group.id}` }
                        ]} />
                    }
                >
                    <i className='material-icons' style={headerIconStyle}>group</i>
                    {group.displayName}
                </PageHeader>
                <GroupFormModal
                    ref={(element) => this.groupEditModal = element}
                    create={false}
                    onSaveSuccess={this.setGroupFromResponseData}
                    group={group}
                />
                <ConfirmationModal
                    buttonText={'Change Tab'}
                    heading={'Change Tab'}
                    appearance='warning'
                    message={(
                        <>
                            You will lost all your unsaved changes. Are you sure you want change current tab?
                        </>
                    )}
                    onConfirm={() => this.changeTab(true)}
                    ref={(element) => this.changeTabConfirmationModal = element}
                />
                <Tabs
                    tabs={tabs}
                    selected={currentTabIndex}
                    onSelect={(_tab, index) => this.changeTab(false, index)}
                />

            </>
        )
    }
}
