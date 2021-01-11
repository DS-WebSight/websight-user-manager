import React from 'react';
import styled from 'styled-components';

import BasicPermissions from './BasicPermissions.js';
import AdvancedPermissions from './AdvancedPermissions.js';

const PermissionsDetailsContainer = styled.div`
    display: block;
    width: 100%;
    margin-top: 10px;
`;

export default class PermissionsDetails extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            advancedView: false
        }
        this.onViewChange = this.onViewChange.bind(this);
    }

    onViewChange(isAdvanced) {
        this.setState({
            advancedView: isAdvanced
        })
    }

    render() {
        const { advancedView } = this.state;
        const { authorizableId } = this.props;

        return (
            <PermissionsDetailsContainer>
                {!advancedView && (
                    <BasicPermissions
                        authorizableId={authorizableId}
                        changeView={this.onViewChange}
                    />
                )}
                {advancedView && (
                    <AdvancedPermissions
                        authorizableId={authorizableId}
                        changeView={this.onViewChange}
                    />
                )}
            </PermissionsDetailsContainer>
        );
    }
}