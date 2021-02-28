import React from 'react';
import styled from 'styled-components';

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

const DetailsLabelContainerStyled = styled(DetailsLabelContainer)`
    width: 100px;
`;

export default class GroupDetailsGeneral extends React.Component {
    render() {
        const { group } = this.props;
        const details = [
            { label: 'Display name', value: group.displayName },
            { label: 'Description', value: group.description },
            {
                label: 'Parent groups',
                value: (group.parentGroups || []).length
                    ? [(
                        <DetailsTagsContainer key='parentGroups'>
                            <AuthorizablesTags
                                authorizables={group.parentGroups}
                                onePerLine
                            />
                        </DetailsTagsContainer>
                    )]
                    : []
            },
            {
                label: 'Members',
                value: (group.members || []).length
                    ? [(
                        <DetailsTagsContainer key='members'>
                            <AuthorizablesTags
                                authorizableId={group.id}
                                authorizables={group.members}
                                onePerLine
                            />
                        </DetailsTagsContainer>
                    )]
                    : []
            }
        ];

        const detailsSection = (
            <SectionContainer>
                <h4>Details</h4>
                {details.map((entry) =>
                    ((entry.value || []).length
                        ? (
                            <DetailsFieldContainer>
                                <DetailsLabelContainerStyled>{entry.label}:</DetailsLabelContainerStyled>
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
                        title='Group'
                        authorizable={group}
                    />
                    {detailsSection}
                </SectionsGroupContainer>
            </SectionsContainer>
        );
    }
}
