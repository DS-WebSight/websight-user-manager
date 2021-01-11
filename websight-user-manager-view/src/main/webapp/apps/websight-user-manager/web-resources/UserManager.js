import React from 'react';
import { HashRouter, Redirect, Route, Switch } from 'react-router-dom';

import {
    ContainerHeader, HeaderSection,
    LayoutManager, NavigationProvider
} from '@atlaskit/navigation-next';

import { AvatarIcon } from 'websight-admin/Icons';
import { PageContentContainer } from 'websight-admin/Containers';
import GlobalNavigation from 'websight-admin/GlobalNavigation';
import Footer from 'websight-admin/Footer';

import UsersPage from './pages/UsersPage.js';
import UserDetailsPage from './pages/UserDetailsPage.js';
import GroupsPage from './pages/GroupsPage.js';
import GroupDetailsPage from './pages/GroupDetailsPage.js';
import SystemUsersPage from './pages/SystemUsersPage.js';
import SystemUserDetailsPage from './pages/SystemUserDetailsPage.js';
import NavigationMenu from './components/NavigationMenu.js';
import { getAuthorizableDisplayName } from './utils/AuthorizableUtil.js';
import RecentAuthorizablesService from './services/RecentAuthorizablesService.js';

const NavigationHeader = () => (
    <HeaderSection>
        {({ css }) => (
            <div style={{ ...css, paddingBottom: 20 }}>
                <ContainerHeader
                    before={() => (
                        <AvatarIcon className='material-icons'>
                            person_outline
                        </AvatarIcon>
                    )}
                    href={window.location.pathname}
                    text='User Manager'
                />
            </div>
        )}
    </HeaderSection>
)

export default class UserManager extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            recentAuthorizables: RecentAuthorizablesService.loadRecentAuthorizablesFromLocalStorage()
        };

        this.onAuthorizableAction = this.onAuthorizableAction.bind(this);
    }

    onAuthorizableAction(actionType, authorizable = {}) {
        const { id, type, enabled } = authorizable;
        if (actionType === 'out-of-date-view') {
            this.setState(prevState => {
                const recentAuthorizables = RecentAuthorizablesService.removeFromRecent(id, this.adjustType(type), prevState.recentAuthorizables);
                return { recentAuthorizables };
            });
        } else {
            this.setState(prevState => {
                const recentAuthorizables = RecentAuthorizablesService.updateRecent(
                    {
                        id,
                        displayName: getAuthorizableDisplayName(authorizable),
                        time: Date.now(),
                        disabled: enabled === false
                    },
                    this.adjustType(type),
                    actionType,
                    prevState.recentAuthorizables
                );
                return { recentAuthorizables };
            });
        }
    }

    adjustType(type) {
        if (type === 'system_user') {
            return 'system-user'
        }
        return type;
    }

    render() {
        const { recentAuthorizables } = this.state;

        return (
            <HashRouter>
                <NavigationProvider>
                    <LayoutManager
                        globalNavigation={GlobalNavigation}
                        productNavigation={() => null}
                        containerNavigation={() => (
                            <>
                                <NavigationHeader />
                                <NavigationMenu
                                    key='leftNavigation'
                                    recentAuthorizables={recentAuthorizables}
                                />
                            </>
                        )}
                        experimental_horizontalGlobalNav
                    >
                        <PageContentContainer>
                            <Switch>
                                <Route exact path='/' render={() => <Redirect to='/users' />} />
                                <Route
                                    path={['/users/:userId/:tabName', '/users/:userId']}
                                    render={(routeProps) =>
                                        <UserDetailsPage {...routeProps} onAuthorizableAction={this.onAuthorizableAction} />
                                    }
                                />
                                <Route
                                    path={'/users'}
                                    render={(routeProps) =>
                                        <UsersPage {...routeProps} onAuthorizableAction={this.onAuthorizableAction} />
                                    }
                                />
                                <Route
                                    path={['/groups/:groupId/:tabName', '/groups/:groupId']}
                                    render={(routeProps) =>
                                        <GroupDetailsPage {...routeProps} onAuthorizableAction={this.onAuthorizableAction} />
                                    }
                                />
                                <Route
                                    path={'/groups'}
                                    render={(routeProps) =>
                                        <GroupsPage {...routeProps} onAuthorizableAction={this.onAuthorizableAction} />
                                    }
                                />
                                <Route path={['/system-users/:userId/:tabName', '/system-users/:userId']}
                                    render={(routeProps) =>
                                        <SystemUserDetailsPage {...routeProps} onAuthorizableAction={this.onAuthorizableAction} />
                                    }
                                />
                                <Route
                                    path={'/system-users'}
                                    render={(routeProps) =>
                                        <SystemUsersPage {...routeProps} onAuthorizableAction={this.onAuthorizableAction} />
                                    }
                                />
                            </Switch>
                        </PageContentContainer>
                        <Footer />
                    </LayoutManager>
                </NavigationProvider>
            </HashRouter>
        );
    }
}
