import React from 'react';
import Tooltip from '@atlaskit/tooltip';

import AuthorizableInfoSection from './AuthorizableInfoSection.js';
import AuthorizablesTags from './AuthorizablesTags.js';
import {
    DetailsFieldContainer,
    DetailsLabelContainer,
    DetailsTagsContainer,
    SectionContainer,
    SectionsContainer,
    SectionsGroupContainer
} from './Containers.js';

export default class UserDetailsGeneral extends React.Component {
    render() {
        const { user } = this.props;
        const details = [
            { label: 'Full name', value: user.displayName },
            { label: 'Email', value: user.email },
            {
                label: 'Groups',
                value: (user.groups || []).length
                    ? [(
                        <DetailsTagsContainer key='groups'>
                            <AuthorizablesTags
                                authorizables={user.groups}
                                onePerLine
                            />
                        </DetailsTagsContainer>
                    )]
                    : []
            }
        ];

        const statistics = [
            { label: 'Login count', value: user.loginCount },
            { label: 'Last login', value: user.lastLoggedIn, tooltip: user.lastLoggedInRelative }
        ];

        const detailsSection = (
            <SectionContainer>
                <h4>Details</h4>
                {details.map((entry) =>
                    ((entry.value || []).length
                        ? (
                            <DetailsFieldContainer>
                                <DetailsLabelContainer>{entry.label}:</DetailsLabelContainer>
                                {entry.value}
                            </DetailsFieldContainer>
                        )
                        : '')
                )}
            </SectionContainer>
        );

        const statisticsSection = (
            <SectionContainer>
                <h4>Statistics</h4>
                {statistics.map((entry) =>
                    (entry.value ?
                        <DetailsFieldContainer>
                            <DetailsLabelContainer>
                                {entry.label}:
                            </DetailsLabelContainer>
                            {entry.tooltip ? (
                                <Tooltip content={entry.value} tag='span'>
                                    <span>{entry.tooltip}</span>
                                </Tooltip>) : entry.value}
                        </DetailsFieldContainer> : '')
                )}
            </SectionContainer>
        );

        return (
            <SectionsContainer>
                <SectionsGroupContainer>
                    <AuthorizableInfoSection
                        title='User'
                        authorizable={user}
                    />
                    {detailsSection}
                </SectionsGroupContainer>
                {statistics.map((x) => x.value).filter((x) => x).length ? statisticsSection : ''}
            </SectionsContainer>
        );
    }
}
