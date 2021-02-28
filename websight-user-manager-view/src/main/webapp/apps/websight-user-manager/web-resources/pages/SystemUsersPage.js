import React from 'react';
import Button, { ButtonGroup } from '@atlaskit/button';
import Pagination from '@atlaskit/pagination';
import PageHeader from '@atlaskit/page-header';

import Breadcrumbs from 'websight-admin/Breadcrumbs';
import { PaginationContainer } from 'websight-admin/Containers';
import { LoadingWrapper } from 'websight-admin/Wrappers';
import TableItemsCountInfo from 'websight-admin/components/TableItemsCountInfo';
import { getUrlParamValues, setUrlParamValues } from 'websight-admin/services/SearchParamsService';
import { AUTH_CONTEXT_UPDATED } from 'websight-rest-atlaskit-client/RestClient';

import SystemUserFormModal from '../components/SystemUserFormModal.js';
import SystemUserHeaderBottomBar from '../components/SystemUserHeaderBottomBar.js';
import SystemUsersTable from '../components/SystemUsersTable.js';
import SystemUserService from '../services/SystemUserService.js';
import { USER_MANAGER_ROOT_PATH } from '../utils/UserManagerConstants.js';

const calculatePages = (pagesCount) => {
    return pagesCount ? [...Array(pagesCount).keys()].map(x => x + 1) : [];
};

export default class SystemUsersPage extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            users: [],
            numberOfPages: 0,
            numberOfFoundUsers: null,
            isLoadingUsers: false,
            params: {
                pageNumber: 1
            },
            requestedParams: {},
            loadedParams: {}
        };

        this.onHashChange = this.onHashChange.bind(this);
        this.onPageChange = this.onPageChange.bind(this);
        this.refreshUsers = this.refreshUsers.bind(this);
        this.onParamsChange = this.onParamsChange.bind(this);
    }

    componentDidMount() {
        window.addEventListener('hashchange', this.onHashChange);
        this.loadDataFromUrl();
        window.addEventListener(AUTH_CONTEXT_UPDATED, () => {
            this.loadDataFromUrl();
        });
    }

    componentDidUpdate(prevProps, prevState) {
        const { params } = this.state;
        if (JSON.stringify(params) !== JSON.stringify(prevState.params)) {
            const pageNumber = params.pageNumber > 1 ? params.pageNumber : undefined;
            const filterParams = {
                ...this.state.params,
                pageNumber: pageNumber
            };
            setUrlParamValues(filterParams);
        }
    }

    componentWillUnmount() {
        window.removeEventListener('hashchange', this.onHashChange);
    }

    onHashChange() {
        this.loadDataFromUrl();
    }

    loadDataFromUrl() {
        const paramsConfig = {
            pageNumber: { type: 'number' }
        }
        const params = {
            ...getUrlParamValues(
                ['filter', 'pageNumber', 'sortDirection'],
                paramsConfig
            )
        };
        this.onParamsChange({
            ...params
        })
    }

    getUsers() {
        if (this.state.isLoadingUsers) return;

        const onSuccess = (data = { users: [] }) => {
            this.setState(
                (prevState) => ({
                    isLoadingUsers: false,
                    loadedParams: prevState.requestedParams,
                    numberOfFoundUsers: data.numberOfFoundUsers || 0,
                    numberOfPages: data.numberOfPages,
                    requestedParams: {},
                    users: data.users
                }),
                () => {
                    if (JSON.stringify(this.state.loadedParams) !== JSON.stringify(this.state.params)) {
                        this.getUsers();
                    }
                }
            );
        };

        const onFailure = () =>
            this.setState((prevState) => ({
                isLoadingUsers: false,
                params: prevState.loadedParams,
                requestedParams: {}
            }));

        this.setState(
            (prevState) => ({
                isLoadingUsers: true,
                requestedParams: prevState.params
            }),
            () => SystemUserService.getSystemUsers(this.state.params, onSuccess, onFailure)
        );
    }

    refreshUsers() {
        this.getUsers();
    }

    onPageChange(event, newPage) {
        this.setState(
            (prevState) => ({ params: { ...prevState.params, pageNumber: newPage } }),
            () => this.getUsers()
        );
    }

    onParamsChange(params) {
        const pageNumber = params.pageNumber ? params.pageNumber : 1;
        this.setState(
            (prevState) => ({ params: { ...prevState.params, ...params, pageNumber: pageNumber } }),
            () => this.getUsers()
        );
    }

    render() {
        const actions = (
            <ButtonGroup>
                <Button onClick={() => this.userCreateModal.open()}>Create System User</Button>
            </ButtonGroup>
        );

        return (
            <>
                <PageHeader
                    actions={actions}
                    bottomBar={
                        <SystemUserHeaderBottomBar
                            onParamsChange={this.onParamsChange}
                            params={this.state.params}
                        />
                    }
                    breadcrumbs={
                        <Breadcrumbs breadcrumbs={[
                            { text: 'User Manager', path: USER_MANAGER_ROOT_PATH, reactPath: '' },
                            { text: 'System Users', reactPath: '/system-users' }
                        ]} />
                    }
                >
                    System Users
                </PageHeader>
                <TableItemsCountInfo
                    isHidden={this.state.numberOfFoundUsers === null}
                    itemName='system user'
                    numberOfFoundItems={this.state.numberOfFoundUsers}
                />
                <SystemUsersTable
                    users={this.state.users}
                    isLoading={this.state.isLoadingUsers}
                    isInitialized={this.state.numberOfFoundUsers != null}
                    refreshUsers={this.refreshUsers}
                    onAuthorizableAction={this.props.onAuthorizableAction}
                    extraActions={this.props.extraActions}
                />
                {this.state.numberOfPages > 1 &&
                    <PaginationContainer>
                        <LoadingWrapper isLoading={this.state.isLoadingUsers}>
                            <Pagination
                                pages={calculatePages(this.state.numberOfPages)}
                                selectedIndex={this.state.params.pageNumber - 1}
                                onChange={this.onPageChange}
                            />
                        </LoadingWrapper>
                    </PaginationContainer>
                }
                <SystemUserFormModal
                    ref={(element) => this.userCreateModal = element}
                    onSaveSuccess={(data) => {
                        this.refreshUsers();
                        if (this.props.onAuthorizableAction) {
                            this.props.onAuthorizableAction('create', data);
                        }
                    }}
                />
            </>
        );
    }
}
