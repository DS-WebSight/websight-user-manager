import { optionData } from 'websight-autosuggestion-esm/utils/AuthorizableAutosuggestionUtil';

export const autosuggestionOptionsToArray = (options = []) => {
    return options.map(option => {
        const { data } = option;
        return [option.value, data.type, data.displayName];
    })
}

export const arrayToAutosuggestionOptions = (groups = []) => {
    return groups.map(dataArray => {
        const [value, type, displayName] = dataArray;
        if (!value || !type || !displayName) {
            return null;
        }
        return optionData({
            data: {
                type: type,
                displayName: displayName
            },
            value: value
        })
    }).filter(option => option);
}