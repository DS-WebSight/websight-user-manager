import React from 'react';
import { Link, Route } from 'react-router-dom';

import { ConnectedItem, MenuSection } from '@atlaskit/navigation-next';

import RecentActions from './RecentActions.js';

const RoutedLinkItem = ({ itemComponent: Component = ConnectedItem, to, ...props }) => {
    return (
        <Route
            render={() => (
                <Component
                    component={({ children, className }) => (
                        <Link className={className} to={to}>
                            {children}
                        </Link>
                    )}
                    isSelected={location.hash === ('#' + to) || location.hash.startsWith('#' + to + '/')}
                    {...props}
                />
            )}
        />
    );
}

export default function NavigationMenu(props) {
    return (
        <MenuSection>
            {({ className }) => (
                <div className={className}>
                    <RoutedLinkItem before={() => <i className='material-icons'>person</i>} text='Users' to={'/users'} />
                    <RoutedLinkItem before={() => <i className='material-icons'>people</i>} text='Groups' to={'/groups'} />
                    <RoutedLinkItem before={() => <i className='material-icons'>settings</i>} text='System Users' to={'/system-users'} />
                    <RecentActions recentAuthorizables={props.recentAuthorizables} />
                </div>
            )}
        </MenuSection>
    );
}