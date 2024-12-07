import React, { useState, useEffect } from 'react';
import { db } from '../firebase'; // Import your Firebase setup
import { doc, getDoc, updateDoc } from 'firebase/firestore';
import { useParams, useNavigate } from 'react-router-dom';
import '../styles/EditUser.css'; // Adjusted to the relative path

const EditUser = () => {
  const { userId } = useParams(); // Get the userId from the URL
  const navigate = useNavigate();
  const [userData, setUserData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const userDoc = doc(db, 'Users', userId);
        const userSnapshot = await getDoc(userDoc);
        if (userSnapshot.exists()) {
          setUserData(userSnapshot.data());
        } else {
          setError('User not found');
        }
      } catch (err) {
        setError('Error fetching user data');
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [userId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUserData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const userDoc = doc(db, 'Users', userId);
      await updateDoc(userDoc, userData); // Update the user document in Firestore
      navigate(`/user/${userId}`); // Redirect to user details page after update
    } catch (err) {
      setError('Error updating user data');
    }
  };

  const handleCancel = () => {
    // Navigate back to the user details page when "Cancel" is clicked
    navigate(`/user/${userId}`);
  };

  if (loading) return <p>Loading...</p>;

  return (
    <div className="edit-user-container">
      <div className="edit-user-card">
        <div className="edit-user-header">
          <h2>Edit User</h2>
          <p>Update the details for the user</p>
        </div>

        {error && <p className="error-message">{error}</p>}

        <form onSubmit={handleSubmit} className="edit-user-form">
          <div className="form-group">
            <label>First Name:</label>
            <input
              type="text"
              name="firstName"
              value={userData.firstName}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Last Name:</label>
            <input
              type="text"
              name="lastName"
              value={userData.lastName}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Email:</label>
            <input
              type="email"
              name="email"
              value={userData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Phone:</label>
            <input
              type="text"
              name="phone"
              value={userData.phone}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-buttons">
            <button type="submit">Save Changes</button>
            <button type="button" onClick={handleCancel}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditUser;
