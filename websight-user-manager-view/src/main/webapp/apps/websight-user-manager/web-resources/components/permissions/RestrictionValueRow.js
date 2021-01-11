import React from 'react';
import Button from '@atlaskit/button';
import styled from 'styled-components';

import InlineInput from 'websight-admin/components/InlineInput';

const RestrictionValueRowContainer = styled.div`
    display: flex;
`;

const RestrictionValueInlineTextFieldContainer = styled.div`
    margin: -10px 20px 0;
    flex: 1 1 auto;
`;

const CloseButtonContainer = styled.div`
    padding-top: 2px;
`;

const RemoveIconContainer = styled.i`
    font-size: 20px;
    margin-top: 6px;
`;

const restrictionValueRemoveButtonStyle = {
    backgroundColor: 'white',
    marginRight: '30px',
    padding: 0
};

export default class RestrictionValueRow extends React.PureComponent {
    shouldComponentUpdate(nextProps) {
        return !(this.props.value === nextProps.value && this.props.placeholder === nextProps.placeholder);
    }

    render() {
        const { placeholder } = this.props;

        return (
            <RestrictionValueRowContainer>
                <RestrictionValueInlineTextFieldContainer>
                    <InlineInput
                        value={this.props.value}
                        onValueChange={value => this.props.onChange(value)}
                        placeholder={placeholder === '' ? '' : 'Restriction value'}
                        maxWidth='350px'
                    />
                </RestrictionValueInlineTextFieldContainer>
                <CloseButtonContainer>
                    <Button
                        spacing='compact'
                        style={restrictionValueRemoveButtonStyle}
                        onClick={this.props.onRemove}
                        title='remove'>
                        <RemoveIconContainer className='material-icons'>close</RemoveIconContainer>
                    </Button>
                </CloseButtonContainer>
            </RestrictionValueRowContainer>
        )
    }
}