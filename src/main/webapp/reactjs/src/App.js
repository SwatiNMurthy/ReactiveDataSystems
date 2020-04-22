import React from 'react';
import logo from './logo.svg';
import './App.css';

import {Container, Row ,Col} from 'react-bootstrap';
import {BrowserRouter as Router, Switch, Route} from 'react-router-dom';

import NavigationBar from './components/NavigationBar';
import Welcome from './components/Welcome';
import Footer from './components/Footer';
import Train from './components/Train';
import TrainList from './components/TrainList';

function App() {
	const marginTop = {
			marginTop: "20px"
	};
  return (
    <Router>
    	<NavigationBar/>
    	<Container>
    		<Row>
    			<Col lg={12} style={marginTop}>
    				<Switch>
    					<Route path="/" exact component={Welcome}/>
    					<Route path="/add" exact component={Train}/>
    					<Route path="/edit/:id" exact component={Train}/>
    					<Route path="/list" exact component={TrainList}/>
    				</Switch>
    			</Col>
    		</Row>
    	</Container>
    	<Footer/>
    </Router>
  );
}

export default App;
