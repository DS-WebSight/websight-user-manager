import React from 'react';
import Button from '@atlaskit/button';
import Select from '@atlaskit/select';
import styled from 'styled-components';

import RestrictionValueRow from './RestrictionValueRow.js';

const REP_GLOB = 'rep:glob';
const SINGLE_VALUE_TYPES = ['', REP_GLOB];

const RestrictionContainer = styled.div`
    margin-bottom: 16px;
    margin-top: 4px;
    border-bottom: 1px solid #e4e2e2;
    width: 100%;
`;

const RestrictionTypeContainer = styled.div`
    display: flex;
    margin: 0 0 5px 24px;
    align-items: center;
`;

const RestrictionSelectContainer = styled.div`
    flex: 0 0 150px;
    margin-right: 4px;
`;

const RestrictionFirstValueContainer = styled.div`
    margin: 2px 0 0 -2px;
    width: 100%;
`;

const RestrictionNextValueContainer = styled.div`
    margin: 0 0 5px 176px;
`;

const AddValueButtonContainer = styled.div`
    margin-left: 172px;
`;

const stringsToOptions = (privileges) => {
    return (privileges || []).map(privilege => ({ label: privilege, value: privilege }));
}

export default class RestrictionRow extends React.PureComponent {
    updateValue(indexToUpdate, newValue) {
        const updatedValues = this.props.restriction.values
            .map((value, index) => index === indexToUpdate ? newValue : value);
        this.props.onChange({ ...this.props.restriction, values: [...updatedValues] });
    }

    removeValue(indexToRemove) {
        const updatedValues = this.props.restriction.values.filter((value, index) => index !== indexToRemove);
        this.props.onChange({ ...this.props.restriction, values: updatedValues });
    }

    onSelectChange(newType) {
        SINGLE_VALUE_TYPES.includes(newType) ?
            this.props.onChange({ type: newType, values: [this.props.restriction.values[0]] }) :
            this.props.onChange({ ...this.props.restriction, type: newType })
    }

    removeFirstRestrictionValue() {
        const { values, onRemove } = this.props;
        values.length > 1 ? this.removeValue(0) : onRemove();
    }

    render() {
        const { values, remainingRestrictionTypes, restriction } = this.props;

        const isMultiValue = !SINGLE_VALUE_TYPES.includes(restriction.type);

        return (
            <RestrictionContainer>
                <RestrictionTypeContainer>
                    <RestrictionSelectContainer>
                        <Select
                            className='single-select'
                            classNamePrefix='react-select'
                            spacing='compact'
                            isRequired
                            options={stringsToOptions(remainingRestrictionTypes)}
                            placeholder='Select Type'
                            menuPortalTarget={document.body}
                            styles={{
                                menuPortal: base => ({
                                    ...base,
                                    zIndex: 9999
                                })
                            }}
                            onChange={newType => this.onSelectChange(newType.value)}
                            value={restriction.type ? { label: restriction.type, value: restriction.type } : undefined}
                        />
                    </RestrictionSelectContainer>
                    {(values && values.length > 0) &&
                        <RestrictionFirstValueContainer>
                            <RestrictionValueRow
                                value={values[0]}
                                onChange={newValue => this.updateValue(0, newValue)}
                                onRemove={() => this.removeFirstRestrictionValue()}
                                placeholder={REP_GLOB === restriction.type ? '' : undefined}
                            />
                        </RestrictionFirstValueContainer>
                    }
                </RestrictionTypeContainer>
                {(values || []).map((value, index) => {
                    if (index !== 0) {
                        return (
                            <RestrictionNextValueContainer>
                                <RestrictionValueRow
                                    key={index}
                                    value={value}
                                    onChange={newValue => this.updateValue(index, newValue)}
                                    onRemove={() => this.removeValue(index)}
                                    placeholder={REP_GLOB === restriction.type ? '' : undefined}
                                />
                            </RestrictionNextValueContainer>
                        )
                    }
                })}
                {(isMultiValue) && (
                    <AddValueButtonContainer>
                        <Button
                            spacing='compact'
                            onClick={() => this.props.onChange({ ...restriction, values: [...values, ''] })}
                            style={{ margin: '0 0 10px 24px' }}
                        >
                            Add Value
                        </Button>
                    </AddValueButtonContainer>
                )}
            </RestrictionContainer>
        )
    }
}