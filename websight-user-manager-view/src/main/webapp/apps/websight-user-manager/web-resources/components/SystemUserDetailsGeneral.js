import React from 'react';

import AuthorizablesTags from './AuthorizablesTags.js';
import AuthorizableInfoSection from './AuthorizableInfoSection.js';
import {
    DetailsFieldContainer,
    DetailsLabelContainer,
    DetailsTagsContainer,
    SectionContainer,
    SectionsContainer,
    SectionsGroupContainer
} from './Containers.js';

export default class SystemUserDetailsGeneral extends React.Component {
    render() {
        const { user } = this.props;

        const details = [
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
                    : ''
            }
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

        return (
            <SectionsContainer>
                <SectionsGroupContainer>
                    <AuthorizableInfoSection
                        title='System user'
                        authorizable={user}
                    />
                    {details.map((x) => x.value).filter((x) => x).length ? detailsSection : ''}
                </SectionsGroupContainer>
            </SectionsContainer>
        );
    }
}
