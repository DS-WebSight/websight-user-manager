import React from 'react';
import Tooltip from '@atlaskit/tooltip';

import { colors } from 'websight-admin/theme';
import { WhiteTooltip } from 'websight-admin/Tooltips';

const infoTooltipIconStyle = {
    color: colors.grey,
    position: 'absolute',
    fontSize: '17px',
    cursor: 'pointer',
    marginTop: '2px'
};

export default class PermissionDetails extends React.Component {

    restrictionsList(item) {
        return Object.entries(item.restrictions).map(([key, values]) => {
            return (<div key={`${item.authorizableId}-${key}`}>{item.authorizableId} ({item.allow ? 'allow' : 'deny'} - {key}:{values.join(', ')})</div>)
        })
    }

    infoTooltipContent(data, header) {
        const restrictions = (item) =>
            this.restrictionsList(item);

        const generalInfo = (item) =>
            <div key={item.authorizableId}>
                {item.group ? 'Group' : 'User'} &apos;{item.authorizableId}&apos; ({item.allow ? 'allow' : 'deny'})
            </div>

        return (<>
            <h4>{header}</h4>
            {
                data.map((dataItem) =>
                    dataItem.restrictions && Object.keys(dataItem.restrictions).length ?
                        restrictions(dataItem)
                        :
                        generalInfo(dataItem)
                )
            }
        </>)
    }

    render() {
        const { row, field, authorizableId } = this.props;
        let { effective, ineffective } = row.declaredActions[field] || {};

        const filterAuthorizable = (declaredActions) => {
            return declaredActions ? Object.values(declaredActions).filter(value => value.authorizableId !== authorizableId) : {};
        }

        effective = filterAuthorizable(effective);
        ineffective = filterAuthorizable(ineffective);

        const info = (
            <>
                { effective && effective.length ? this.infoTooltipContent(effective, 'Effective') : '' }
                { ineffective && ineffective.length ? this.infoTooltipContent(ineffective, 'Ineffective') : '' }
            </>
        )
        return (
            <>
                {
                    !effective.length && !ineffective.length  ? '' :
                        (<Tooltip delay={0} component={WhiteTooltip} tag='span' content={info}>
                            <i className='material-icons-outlined' style={infoTooltipIconStyle}>info</i>
                        </Tooltip>)
                }
            </>
        )
    }
}