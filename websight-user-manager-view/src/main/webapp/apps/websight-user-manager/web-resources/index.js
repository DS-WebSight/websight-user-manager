import React from 'react';
import ReactDOM from 'react-dom';

import 'websight-admin/GlobalStyle';

import UserManager from './UserManager.js';

class App extends React.Component {
    render() {
        return (
            <UserManager />
        );
    }
}

ReactDOM.render(<App/>, document.getElementById('app-root'));