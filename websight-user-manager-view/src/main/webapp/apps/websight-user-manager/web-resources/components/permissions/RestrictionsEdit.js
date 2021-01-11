import React from 'react';
import Button from '@atlaskit/button';
import Tooltip from '@atlaskit/tooltip';
import styled from 'styled-components';

import RestrictionRow from './RestrictionRow.js';

const RestrictionContainer = styled.div`
    display: flex;
`;

const calculateRemainingRestrictionTypes = (restrictions, allTypes) => {
    const typesAlreadyUsed = restrictions.map(restriction => restriction.type);
    return (allTypes || []).filter(type => !typesAlreadyUsed.includes(type));
}

const emptyRestriction = {
    type: '',
    values: ['']
}

export default class RestrictionsEdit extends React.Component {

    constructor(props) {
        super(props);
        this.addRestriction = this.addRestriction.bind(this);
    }

    addRestriction() {
        const { restrictions, onChange, availableRestrictionTypes } = this.props;
        const remainingRestrictionTypes = calculateRemainingRestrictionTypes(restrictions, availableRestrictionTypes);
        if (remainingRestrictionTypes.length) {
            if (remainingRestrictionTypes.length === 1) {
                onChange([...restrictions, { type: remainingRestrictionTypes[0], values: [''] }]);
            } else {
                onChange([...restrictions, emptyRestriction]);
            }
        }
    }

    updateRestriction(indexToUpdate, restrictionData) {
        const { restrictions, onChange } = this.props;
        const updatedRestrictions = restrictions
            .map((restriction, index) => index === indexToUpdate ? restrictionData : restriction);
        onChange(updatedRestrictions);
    }

    removeRestriction(indexToDelete) {
        const { restrictions, onChange } = this.props;
        const updatedRestrictions = restrictions.filter((restriction, index) => index !== indexToDelete);
        onChange(updatedRestrictions);
    }

    render() {
        const { restrictions, availableRestrictionTypes } = this.props;

        const remainingRestrictionTypes = calculateRemainingRestrictionTypes(restrictions, availableRestrictionTypes);

        return (
            <>
                {restrictions.map((restriction, index) => {
                    return (
                        <RestrictionRow
                            key={index}
                            restriction={restriction}
                            values={restriction.values}
                            onChange={newRestriction => this.updateRestriction(index, newRestriction)}
                            onRemove={() => this.removeRestriction(index)}
                            remainingRestrictionTypes={remainingRestrictionTypes}
                        />
                    )
                })}

                <RestrictionContainer>
                    {restrictions.length < availableRestrictionTypes.length ?
                        <Button
                            onClick={this.addRestriction}
                        >
                            Add Restriction
                        </Button> :
                        <Tooltip content='No more restrictions to add'>
                            <Button isDisabled>Add Restriction</Button>
                        </Tooltip>
                    }
                </RestrictionContainer>
            </>
        )
    }
}