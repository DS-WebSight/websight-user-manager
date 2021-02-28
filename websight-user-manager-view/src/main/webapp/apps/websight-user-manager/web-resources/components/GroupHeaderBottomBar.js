import React from 'react';
import Select from '@atlaskit/select';

import {
    FilterOptionsContainer, FilterPatternContainer,
    FilterSortByContainer, HeaderFiltersContainer
} from 'websight-admin/Containers';
import AuthorizableAutosuggestion from 'websight-autosuggestion-esm/AuthorizableAutosuggestion';
import GroupAutosuggestion from 'websight-autosuggestion-esm/GroupAutosuggestion';
import { debounce } from 'websight-admin/Utils';
import StatefulFilterInput from 'websight-admin/components/StatefulFilterInput';

const sortByOptions = [
    { label: 'ID [A-Z]', value: { sortBy: 'id', sortDirection: 'ASC' } },
    { label: 'ID [Z-A]', value: { sortBy: 'id', sortDirection: 'DESC' } },
    { label: 'Name [A-Z]', value: { sortBy: 'displayName', sortDirection: 'ASC' } },
    { label: 'Name [Z-A]', value: { sortBy: 'displayName', sortDirection: 'DESC' } }
];

export default class GroupHeaderBottomBar extends React.Component {
    constructor(props) {
        super(props);

        this.onChange = this.onChange.bind(this);
    }

    onChange(props) {
        this.props.onParamsChange(props);
    }

    get sortByValue() {
        return sortByOptions.find((option) =>
            option.value.sortBy === this.props.params.sortBy &&
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
                <FilterOptionsContainer>
                    <GroupAutosuggestion
                        value={params.parentGroups}
                        isMulti
                        spacing="compact"
                        placeholder='In group'
                        onChange={(selectedOptions) => {
                            this.onChange({ parentGroups: (selectedOptions || []) });
                        }}
                    />
                </FilterOptionsContainer>
                <FilterOptionsContainer>
                    <AuthorizableAutosuggestion
                        value={params.members}
                        isMulti
                        noOptionsMessage={(inputValue) => `No members found for "${inputValue}"`}
                        noOptionEmptyMessage='Start typing to find a member'
                        spacing="compact"
                        placeholder='With member'
                        onChange={(selectedOptions) => {
                            this.onChange({ members: (selectedOptions || []) });
                        }}
                    />
                </FilterOptionsContainer>
                <FilterSortByContainer>
                    <Select
                        onChange={(selectedOption) => {
                            this.onChange({
                                ...selectedOption.value
                            })
                        }}
                        options={sortByOptions}
                        placeholder='Sort By'
                        spacing='compact'
                        value={this.sortByValue}
                    />
                </FilterSortByContainer>
            </HeaderFiltersContainer>
        )
    }
}
