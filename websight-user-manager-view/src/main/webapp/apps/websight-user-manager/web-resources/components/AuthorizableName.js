import React from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components';

import { colors } from 'websight-admin/theme';

import { getAuthorizableHref } from '../utils/AuthorizableUtil.js';

const NameContainer = styled.span`
    align-items: center;
    display: flex;
    min-width: 200px;
`;

const NameIconContainer = styled.div`
    display: inline;
    margin-right: 8px;
`;

const nameIconStyle = {
    color: colors.grey,
    verticalAlign: 'middle'
}

const AuthorizableName = (props) => {
    const { authorizable, enabledAware } = props;
    const name = authorizable.displayName || authorizable.id
    return (
        <NameContainer>
            <NameIconContainer>
                <i className='material-icons' style={nameIconStyle}>{props.icon}</i>
            </NameIconContainer>
            <Link to={getAuthorizableHref(authorizable)}>
                {!enabledAware || authorizable.enabled ? name : <del>{name}</del>}
            </Link>
        </NameContainer>
    )
}

export default AuthorizableName;
