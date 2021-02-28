import React from 'react';
import Select, { CheckboxSelect } from '@atlaskit/select';

import {
    FilterOptionsContainer, FilterPatternContainer,
    FilterSortByContainer, HeaderFiltersContainer
} from 'websight-admin/Containers';
import GroupAutosuggestion from 'websight-autosuggestion-esm/GroupAutosuggestion';
import { debounce } from 'websight-admin/Utils';
import StatefulFilterInput from 'websight-admin/components/StatefulFilterInput';

const filterOptions = [
    { label: 'Enabled', value: 'enabled', params: { enabled: true } },
    { label: 'Disabled', value: 'disabled', params: { enabled: false } },
    { label: 'Ever logged In', value: 'everLoggedIn', params: { everLoggedIn: true } },
    { label: 'Never logged In', value: 'neverLoggedIn', params: { everLoggedIn: false } }
]

const clearedFilterOptionsValues = {
    enabled: undefined,
    everLoggedIn: undefined
}

const sortByOptions = [
    { label: 'Username [A-Z]', value: { sortBy: 'id', sortDirection: 'ASC' } },
    { label: 'Username [Z-A]', value: { sortBy: 'id', sortDirection: 'DESC' } },
    { label: 'Last name [A-Z]', value: { sortBy: 'lastName', sortDirection: 'ASC' } },
    { label: 'Last name [Z-A]', value: { sortBy: 'lastName', sortDirection: 'DESC' } },
    { label: 'Logins count [0-9]', value: { sortBy: 'loginCount', sortDirection: 'ASC' } },
    { label: 'Logins count [9-0]', value: { sortBy: 'loginCount', sortDirection: 'DESC' } },
    { label: 'Last logged in [0-9]', value: { sortBy: 'lastLoggedIn', sortDirection: 'ASC' } },
    { label: 'Last logged in [9-0]', value: { sortBy: 'lastLoggedIn', sortDirection: 'DESC' } }
]

export default class UserHeaderBottomBar extends React.Component {
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

    get filterOptionsValue() {
        return filterOptions.filter((option) => {
            const isEnabled = option.params.enabled !== undefined &&
                option.params.enabled === this.props.params.enabled;
            const everLoggedIn = option.params.everLoggedIn !== undefined &&
                option.params.everLoggedIn === this.props.params.everLoggedIn;

            return isEnabled || everLoggedIn;
        });
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
                        value={params.groups}
                        isMulti
                        spacing="compact"
                        placeholder='In group'
                        onChange={(selectedOptions) => {
                            this.onChange({ groups: (selectedOptions || []) });
                        }}
                    />
                </FilterOptionsContainer>
                <FilterOptionsContainer>
                    <CheckboxSelect
                        isMulti
                        isSearchable={false}
                        options={filterOptions}
                        onChange={(selectedOptions) => {
                            const selectedParams = {};
                            if (selectedOptions && selectedOptions.length) {
                                Object.assign(selectedParams, ...selectedOptions.map((option) => option.params));
                            }
                            this.onChange({ ...clearedFilterOptionsValues, ...selectedParams });
                        }}
                        placeholder="Choose an option"
                        spacing="compact"
                        value={this.filterOptionsValue}
                    />
                </FilterOptionsContainer>
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
