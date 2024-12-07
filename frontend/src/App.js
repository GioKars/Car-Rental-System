import { BrowserRouter as Router, Route, Routes, useLocation } from 'react-router-dom';
import Login from './pages/AdminLogin';
import Dashboard from './pages/Dashboard';
import UserDetails from './pages/UserDetails'; // Import UserDetails component
import EditUser from './pages/EditUser'; // Import UserDetails component
import ReviewImages from './pages/ReviewImages';
import Navbar from './components/Navbar'; // Import Navbar component

function App() {
  return (
    <Router>
      <ConditionalNavbar /> {/* Conditionally render the Navbar here */}
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/user/:userId" element={<UserDetails />} /> {/* User details page route */}
        <Route path="/edit-user/:userId" element={<EditUser />} />
        <Route path="/review-images/:userId" element={<ReviewImages />} />
      </Routes>
    </Router>
  );
}

function ConditionalNavbar() {
  const location = useLocation(); // Get the current location (route)
  
  // Conditionally render Navbar only for routes other than "/"
  return location.pathname !== "/" && <Navbar />;
}

export default App;
