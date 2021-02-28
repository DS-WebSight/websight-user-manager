import React from 'react';
import { Redirect } from 'react-router-dom';
import Button from '@atlaskit/button';
import Tabs from '@atlaskit/tabs';
import PageHeader from '@atlaskit/page-header';

import Breadcrumbs from 'websight-admin/Breadcrumbs';
import ConfirmationModal from 'websight-admin/ConfirmationModal';
import { colors } from 'websight-admin/theme';

import UserDetailsGeneral from '../components/UserDetailsGeneral.js';
import UserFormModal from '../components/UserFormModal.js';
import AdvancedPermissions from '../components/permissions/AdvancedPermissions.js';
import BasicPermissions from '../components/permissions/BasicPermissions.js';
import UserService from '../services/UserService.js';
import { getAuthorizableDisplayName, getTabIndex, setTabNameInHash } from '../utils/AuthorizableUtil.js';
import { USER_MANAGER_ROOT_PATH } from '../utils/UserManagerConstants.js';

const headerIconStyle = {
    fontSize: '31px',
    verticalAlign: 'bottom',
    paddingRight: '5px',
    color: colors.grey
};

export default class UserDetailsPage extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            currentTabIndex: 0,
            tabIndexToConfirm: null,
            redirect: false,
            user: {
                ...UserService.emptyUserData
            }
        }

        this.getUser = this.getUser.bind(this);
        this.onHashChange = this.onHashChange.bind(this);
        this.setUserFromResponseData = this.setUserFromResponseData.bind(this);
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
        this.getUser();
    }

    setTabInHash(tabIndex) {
        setTabNameInHash('users', this.props.match.params.userId, tabIndex);
    }

    getUser() {
        const params = {
            authorizableId: decodeURIComponent(this.props.match.params.userId)
        };

        const tabIndex = getTabIndex(this.props.match.params.tabName);
        this.setTabInHash(tabIndex);

        const onSuccess = (data) => {
            this.setState({ user: data, currentTabIndex: tabIndex });
            if (this.props.onAuthorizableAction) {
                this.props.onAuthorizableAction('read', data);
            }
        };

        const onFailure = () => {
            if (this.props.onAuthorizableAction) {
                this.props.onAuthorizableAction('out-of-date-view', { id: params.authorizableId, type: 'user' });
            }
            this.setState({ redirect: true });
        }

        UserService.getUser(params, onSuccess, onFailure);
    }

    setUserFromResponseData(data) {
        this.setState((prevState) => ({ user: { ...prevState.user, ...data } }));
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
        const { currentTabIndex, redirect, user } = this.state
        const tabs = [
            {
                label: 'Details',
                content: <UserDetailsGeneral user={user} />
            },
            {
                label: 'Permissions',
                content: (
                    <BasicPermissions
                        isAdmin={user.admin}
                        authorizableId={user.id}
                        ref={(element) => this.basicPermissionsTab = element}
                        onAuthorizableAction={this.props.onAuthorizableAction}
                        displayName={getAuthorizableDisplayName(user)}
                        type={user.type}
                    />
                )
            },
            {
                label: 'Advanced Permissions',
                content: (
                    <AdvancedPermissions
                        isAdmin={user.admin}
                        authorizableId={user.id}
                        onAuthorizableAction={this.props.onAuthorizableAction}
                        displayName={getAuthorizableDisplayName(user)}
                        type={user.type}
                    />
                )
            }
        ];

        return (
            <>
                {redirect && <Redirect to='/users' />}
                <PageHeader
                    actions={<Button onClick={() => this.userEditModal.open()}>Edit</Button>}
                    breadcrumbs={
                        <Breadcrumbs breadcrumbs={[
                            { text: 'User Manager', path: USER_MANAGER_ROOT_PATH, reactPath: '' },
                            { text: 'Users', reactPath: '/users' },
                            { text: user.displayName, reactPath: `/users/${user.id}` }
                        ]} />
                    }
                >
                    <i className='material-icons' style={headerIconStyle}>{user.icon}</i>
                    {user.enabled ? user.displayName : (<>
                        <del>{user.displayName}</del>
                        (disabled)</>)}
                </PageHeader>
                <UserFormModal
                    ref={(element) => this.userEditModal = element}
                    create={false}
                    onSaveSuccess={(data) => {
                        this.setUserFromResponseData(data);
                        if (this.props.onAuthorizableAction) {
                            this.props.onAuthorizableAction('edit', data);
                        }
                    }
                    }
                    user={user}
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
