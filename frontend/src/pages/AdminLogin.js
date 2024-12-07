import { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth'; // Firebase Authentication import
import { useNavigate } from 'react-router-dom'; // To navigate after successful login
import { auth } from '../firebase'; // Firebase initialization (make sure it's set up)
import '../styles/AdminLogin.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // Handle login on form submit
  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // Sign in using Firebase Authentication
      await signInWithEmailAndPassword(auth, email, password);

      // After successful login, navigate to the dashboard
      navigate('/dashboard');
    } catch (error) {
      console.error('Login Error:', error);
      setError('Invalid email or password');
    }
  };

  return (
    <div className="container">
      <h2>Admin Login</h2>
      <form onSubmit={handleLogin} className="form">
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          className="input"
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          className="input"
        />
        <button type="submit" className="button">Login</button>
        {error && <p className="error">{error}</p>}
      </form>
    </div>
  );
  
};

export default Login;
