import React from 'react';
import Select from '@atlaskit/select';

import {
    FilterPatternContainer,
    FilterSortByContainer,
    HeaderFiltersContainer
} from 'websight-admin/Containers';
import { debounce } from 'websight-admin/Utils';
import StatefulFilterInput from 'websight-admin/components/StatefulFilterInput';

const sortByOptions = [
    { label: 'ID [A-Z]', value: { sortDirection: 'ASC' } },
    { label: 'ID [Z-A]', value: { sortDirection: 'DESC' } }
]

export default class SystemUserHeaderBottomBar extends React.Component {
    constructor(props) {
        super(props);

        this.onChange = this.onChange.bind(this);
    }

    onChange(props) {
        this.props.onParamsChange(props);
    }

    get sortByValue() {
        return sortByOptions.find((option) =>
            option.value.sortDirection === this.props.params.sortDirection
        );
    }

    render() {
        const { params } = this.props;

        return (
            <HeaderFiltersContainer>
                <FilterPatternContainer>
                    <StatefulFilterInput
                        onClear={() => {
                            this.onChange({ filter: '' });
                        }}
                        onChange={(event) => {
                            const targetValue = event.target.value;
                            this.debounceFilterTimerId = debounce(() => this.onChange({ filter: targetValue }), this.debounceFilterTimerId);
                        }}
                        value={params.filter}
                    />
                </FilterPatternContainer>
                <FilterSortByContainer>
                    <Select
                        placeholder='Sort By'
                        spacing='compact'
                        options={sortByOptions}
                        value={this.sortByValue}
                        onChange={(selectedOption) => {
                            this.onChange({
                                ...selectedOption.value
                            })
                        }}
                    />
                </FilterSortByContainer>
            </HeaderFiltersContainer>
        )
    }
}
