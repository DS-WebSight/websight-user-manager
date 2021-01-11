import React from 'react';

import { DetailsFieldContainer, DetailsLabelContainer, SectionContainer } from './Containers.js';

const AuthorizableInfoSection = (props) => {
    const authorizable = props.authorizable;
    const information = [
        { label: 'ID', value: authorizable.id },
        { label: 'UUID', value: authorizable.uuid },
        { label: 'Path', value: authorizable.path }
    ];
    return (
        <SectionContainer>
            <h4>{props.title}</h4>
            {information.map((entry) =>
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
    )
}

export default AuthorizableInfoSection;
