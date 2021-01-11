import React from 'react';
import Button, { ButtonGroup } from '@atlaskit/button';
import Pagination from '@atlaskit/pagination';
import PageHeader from '@atlaskit/page-header';

import Breadcrumbs from 'websight-admin/Breadcrumbs';
import { LoadingWrapper } from 'websight-admin/Wrappers';
import { PaginationContainer } from 'websight-admin/Containers';
import TableItemsCountInfo from 'websight-admin/components/TableItemsCountInfo';
import { getUrlParamValues, setUrlParamValues } from 'websight-admin/services/SearchParamsService';
import { AUTH_CONTEXT_UPDATED } from 'websight-rest-atlaskit-client/RestClient';

import GroupFormModal from '../components/GroupFormModal.js';
import GroupHeaderBottomBar from '../components/GroupHeaderBottomBar.js';
import GroupsTable from '../components/GroupsTable.js';
import GroupService from '../services/GroupService.js';
import { USER_MANAGER_ROOT_PATH } from '../utils/UserManagerConstants.js';
import { arrayToAutosuggestionOptions, autosuggestionOptionsToArray } from '../utils/AutosuggestionUtil.js';

const calculatePages = (pagesCount) => {
    return pagesCount ? [...Array(pagesCount).keys()].map(x => x + 1) : [];
}

export default class GroupsPage extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            groups: [],
            numberOfFoundGroups: null,
            numberOfPages: 0,
            isLoadingGroups: false,
            params: {
                pageNumber: 1
            },
            requestedParams: {},
            loadedParams: {}
        };

        this.onHashChange = this.onHashChange.bind(this);
        this.onPageChange = this.onPageChange.bind(this);
        this.refreshGroups = this.refreshGroups.bind(this);
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
            const urlParamsConfig = { parentGroups: { separator: '|' }, members: { separator: '|' } };
            const filterParams = {
                ...this.state.params,
                pageNumber: pageNumber,
                parentGroups: autosuggestionOptionsToArray(this.state.params.parentGroups),
                members: autosuggestionOptionsToArray(this.state.params.members)
            };
            setUrlParamValues(filterParams, urlParamsConfig);
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
            parentGroups: { isArray: true, separator: '|' },
            members: { isArray: true, separator: '|' },
            pageNumber: { type: 'number' }
        }
        const params = getUrlParamValues(['filter', 'parentGroups', 'members', 'pageNumber', 'sortBy', 'sortDirection'], paramsConfig);
        this.onParamsChange({
            ...params,
            parentGroups: arrayToAutosuggestionOptions(params.parentGroups),
            members: arrayToAutosuggestionOptions(params.members)
        });
    }

    getGroups() {
        if (this.state.isLoadingGroups) return;

        const onSuccess = (data = { groups: [] }) => {
            this.setState(
                (prevState) => ({
                    isLoadingGroups: false,
                    loadedParams: prevState.requestedParams,
                    numberOfFoundGroups: data.numberOfFoundGroups || 0,
                    numberOfPages: data.numberOfPages,
                    requestedParams: {},
                    groups: data.groups
                }),
                () => {
                    if (JSON.stringify(this.state.loadedParams) !== JSON.stringify(this.state.params)) {
                        this.getGroups();
                    }
                }
            );
        };

        const onFailure = () =>
            this.setState((prevState) => ({
                isLoadingGroups: false,
                params: prevState.loadedParams,
                requestedParams: {}
            }));

        this.setState(
            (prevState) => ({
                isLoadingGroups: true,
                requestedParams: prevState.params
            }),
            () => GroupService.getGroups(this.state.params, onSuccess, onFailure)
        );
    }

    refreshGroups() {
        this.getGroups();
    }

    onPageChange(event, newPage) {
        this.setState(
            (prevState) => ({ params: { ...prevState.params, pageNumber: newPage } }),
            () => this.getGroups()
        );
    }

    onParamsChange(params) {
        const pageNumber = params.pageNumber ? params.pageNumber : 1;
        this.setState(
            (prevState) => ({ params: { ...prevState.params, ...params, pageNumber: pageNumber } }),
            () => this.getGroups()
        );
    }

    render() {
        const actions = (
            <ButtonGroup>
                <Button onClick={() => this.groupCreateModal.open()}>Create Group</Button>
            </ButtonGroup>
        );

        return (
            <>
                <PageHeader
                    actions={actions}
                    bottomBar={
                        <GroupHeaderBottomBar
                            onParamsChange={this.onParamsChange}
                            params={this.state.params}
                        />
                    }
                    breadcrumbs={
                        <Breadcrumbs breadcrumbs={[
                            { text: 'User Manager', path: USER_MANAGER_ROOT_PATH, reactPath: '' },
                            { text: 'Groups', reactPath: '/groups' }
                        ]} />
                    }
                >
                    Groups
                </PageHeader>
                <TableItemsCountInfo
                    isHidden={this.state.numberOfFoundGroups === null}
                    itemName='group'
                    numberOfFoundItems={this.state.numberOfFoundGroups}
                />
                <GroupsTable
                    groups={this.state.groups}
                    isLoading={this.state.isLoadingGroups}
                    isInitialized={this.state.numberOfFoundGroups != null}
                    refreshGroups={this.refreshGroups}
                    onAuthorizableAction={this.props.onAuthorizableAction}
                />
                {this.state.numberOfPages > 1 &&
                    <PaginationContainer>
                        <LoadingWrapper isLoading={this.state.isLoadingGroups}>
                            <Pagination
                                pages={calculatePages(this.state.numberOfPages)}
                                selectedIndex={this.state.params.pageNumber - 1}
                                onChange={this.onPageChange}
                            />
                        </LoadingWrapper>
                    </PaginationContainer>
                }
                <GroupFormModal
                    ref={(element) => this.groupCreateModal = element}
                    onSaveSuccess={(data) => {
                        this.refreshGroups();
                        if (this.props.onAuthorizableAction) {
                            this.props.onAuthorizableAction('create', data);
                        }
                    }}
                    create={true}
                />
            </>
        );
    }
}
