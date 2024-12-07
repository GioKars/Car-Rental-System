import React from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebase';  // Import Firebase authentication
import { signOut } from 'firebase/auth'; // Firebase sign-out function
import '../styles/Navbar.css'; // Use .css extension for CSS files

const Navbar = () => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await signOut(auth); // Sign out the user
      navigate('/'); // Redirect to the login page
    } catch (error) {
      console.error("Error signing out:", error);
    }
  };

  const handleHome = async () => {
    try {
      navigate('/dashboard'); // Redirect to the login page
    } catch (error) {
      console.error("Error dashboard:", error);
    }
  };

  return (
    <nav className="navbar">
      <div className="navbar-actions">
        <button className="home-button" onClick={handleHome}>Home</button>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
};

export default Navbar;
