import { colors } from 'websight-admin/theme';

import { USER_MANAGER_ROOT_PATH } from '../../../../utils/UserManagerConstants.js';

const NavigationItemFragment = {
    title: 'User Manager',
    img: 'person_outline',
    color: colors.red,
    description: 'Work with users and groups. Manage their permissions and track authentication activities.',
    href: USER_MANAGER_ROOT_PATH
}

export default NavigationItemFragment;