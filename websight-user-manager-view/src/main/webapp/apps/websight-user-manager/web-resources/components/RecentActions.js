import React from 'react';

import { GroupHeading, Item, ItemPrimitive } from '@atlaskit/navigation-next';
import { getUrlHashValue } from 'websight-admin/services/SearchParamsService';
import { colors } from 'websight-admin/theme';

import { getAuthorizableTypeFromHash, getIconNameForAuthorizableType } from '../utils/AuthorizableUtil.js';
import { toRelative } from '../utils/DateFormatter.js';

const actionTypeToLabel = {
    create: 'Created',
    read: 'Viewed',
    edit: 'Edited',
    delete: 'Deleted',
    disable: 'Disabled',
    enable: 'Enabled'
}

export default class RecentActions extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            authorizableType: getAuthorizableTypeFromHash(getUrlHashValue())
        };

        this.onHashChange = this.onHashChange.bind(this);
        this.updateAuthorizableType = this.updateAuthorizableType.bind(this);
    }

    componentDidMount() {
        window.addEventListener('hashchange', this.onHashChange);
    }

    componentWillUnmount() {
        window.removeEventListener('hashchange', this.onHashChange);
    }

    onHashChange() {
        this.updateAuthorizableType();
    }

    updateAuthorizableType() {
        this.setState({
            authorizableType: getAuthorizableTypeFromHash(getUrlHashValue())
        });
    }

    renderAuthorizables(authorizables = [], icon, type) {
        return authorizables.map(authorizable =>
            authorizable.actionType === 'delete' ?
                <ItemPrimitive
                    before={() => <i className='material-icons' style={{ color: colors.grey }}>{icon}</i>}
                    key={authorizable.id}
                    text={<div style={{ color: colors.grey }}>{authorizable.displayName}</div>}
                    subText={authorizable.actionType ? this.prepareSubText(authorizable.actionType, authorizable.time) : undefined}
                    styles={styles => ({
                        ...styles,
                        itemBase: { ...styles.itemBase, cursor: 'default' }
                    })}
                /> :
                <Item
                    before={() => <i className='material-icons'>{icon}</i>}
                    key={authorizable.id}
                    text={authorizable.disabled ? <del>{authorizable.displayName}</del> : authorizable.displayName}
                    href={`#/${type}s/${encodeURIComponent(authorizable.id)}`}
                    subText={authorizable.actionType ? this.prepareSubText(authorizable.actionType, authorizable.time) : undefined}
                />
        )
    }

    prepareSubText(actionType, time) {
        const relativeTime = toRelative(time);
        return `${actionTypeToLabel[actionType]} ${relativeTime ? relativeTime : ''}`;
    }

    render() {
        const { recentAuthorizables } = this.props;
        const { authorizableType } = this.state;

        const authorizablesToRender = this.renderAuthorizables(
            recentAuthorizables[authorizableType],
            getIconNameForAuthorizableType(authorizableType),
            authorizableType
        );

        return (
            authorizablesToRender.length > 0 && (
                <>
                    <GroupHeading>Recently visited</GroupHeading>
                    {authorizablesToRender}
                </>
            )
        );
    }
}