import React from 'react';
import Tag from '@atlaskit/tag';
import Tooltip from '@atlaskit/tooltip';

import { colors } from 'websight-admin/theme';
import { TagIcon } from 'websight-admin/Icons';
import { WhiteTooltip } from 'websight-admin/Tooltips';

import { getAuthorizableHash } from '../utils/AuthorizableUtil.js';
import { PRINCIPAL_EVERYONE } from '../utils/UserManagerConstants.js';

const infoTooltipIconStyle = {
    position: 'absolute',
    fontSize: '17px',
    marginTop: '2px',
    marginLeft: '4px'
};

const seeAllLinkStyle = {
    marginLeft: '5px',
    color: colors.grey,
    cursor: 'pointer'
}

const COLLAPSED_TAGS_LIMIT = 3;

export default class AuthorizablesTags extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            expanded: false
        }
    }

    toggle() {
        this.setState((prevState) => ({ expanded: !prevState.expanded }));
    }

    render() {
        const Tags = () => {
            let authorizables = this.props.authorizables;
            if (this.props.expandable && !this.state.expanded) {
                authorizables = authorizables.slice(0, COLLAPSED_TAGS_LIMIT);
            }

            return authorizables.map((authorizable, index) => (
                <>
                    {this.props.onePerLine && index > 0 && <br />}
                    <Tag
                        key={authorizable.id}
                        text={authorizable.displayName || authorizable.id}
                        color={colors.grey}
                        elemBefore={<TagIcon className='material-icons'>{authorizable.icon}</TagIcon>}
                        href={getAuthorizableHash(authorizable)}
                    />
                </>
            ));
        }

        if (this.props.authorizableId === PRINCIPAL_EVERYONE) {
            return (
                <span style={{ color: colors.grey, marginLeft: '4px' }}>
                    <Tooltip
                        delay={0}
                        component={WhiteTooltip}
                        tag='span'
                        content={'Group contains all users and groups'}
                    >
                        <>
                            <i>everyone</i>
                            <i className='material-icons-outlined' style={infoTooltipIconStyle}>info</i>
                        </>
                    </Tooltip>
                </span>
            )
        }

        return (
            <>
                <Tags />
                {this.props.expandable && this.props.authorizables.length > COLLAPSED_TAGS_LIMIT && (
                    <>
                        <br />
                        <a style={seeAllLinkStyle} onClick={() => this.toggle()}>
                            {this.state.expanded ? 'See less' : 'See all'}
                        </a>
                    </>
                )}
            </>
        )
    }

}
