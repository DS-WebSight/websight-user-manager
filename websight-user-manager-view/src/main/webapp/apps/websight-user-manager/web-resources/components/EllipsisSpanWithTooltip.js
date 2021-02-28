import React from 'react';
import Tooltip from '@atlaskit/tooltip';

export default class EllipsisSpanWithTooltip extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            overflowActive: false
        };
    }

    componentDidMount() {
        this.setState({ overflowActive: this.isEllipsisActive(this.spanContainer) });
    }

    isEllipsisActive(element) {
        return element.offsetHeight < element.scrollHeight || element.offsetWidth < element.scrollWidth;
    }

    render() {
        const { overflowActive } = this.state;

        return (
            <div
                style={{
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden'
                }}
                ref={ref => (this.spanContainer = ref)}
            >
                {overflowActive ?
                    <Tooltip content={this.props.text} tag='span'>
                        <span>{this.props.text}</span>
                    </Tooltip> :
                    <span>{this.props.text}</span>
                }
            </div>
        );
    }
}