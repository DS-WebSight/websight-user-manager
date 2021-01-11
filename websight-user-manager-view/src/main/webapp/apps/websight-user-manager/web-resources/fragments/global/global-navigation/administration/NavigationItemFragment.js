import React from 'react';
import { LinkItem } from '@atlaskit/menu';

import { NavigationItemIcon } from 'websight-admin/Icons';

import { USER_MANAGER_ROOT_PATH } from '../../../../utils/UserManagerConstants.js';

export default class NavigationItemFragment extends React.Component {
    render() {
        return (
            <LinkItem
                href={USER_MANAGER_ROOT_PATH}
                elemBefore={<NavigationItemIcon className='material-icons'>person</NavigationItemIcon>}
            >
                User Manager
            </LinkItem>
        );
    }
}
