import { useState, useEffect } from 'react';
import { db } from '../firebase'; 
import { doc, getDoc, collection, getDocs, query, where, setDoc } from 'firebase/firestore'; 
import { useParams, useNavigate } from 'react-router-dom';
import '../styles/UserDetails.css'; 

const UserDetails = () => {
  const { userId } = useParams();
  const [user, setUser] = useState(null);
  const [rentalHistory, setRentalHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [banned, setBanned] = useState(false);
  const [status, setStatus] = useState('');
  const [showAllRentals, setShowAllRentals] = useState(false); // State to toggle rental visibility
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const userDoc = doc(db, 'Users', userId);
        const userSnap = await getDoc(userDoc);

        if (!userSnap.exists()) {
          setError('User not found');
          setLoading(false);
          return;
        }

        const userData = userSnap.data();
        setUser(userData);
        setBanned(userData.banned || false); 
        setStatus(userData.status || 'Pending');

        const rentalsRef = collection(db, 'rentals');
        const rentalQuery = query(rentalsRef, where('userId', '==', userId));
        const rentalSnapshot = await getDocs(rentalQuery);

        if (!rentalSnapshot.empty) {
          const rentalList = rentalSnapshot.docs.map((doc) => ({
            id: doc.id,
            ...doc.data(),
          }));
          setRentalHistory(rentalList);
        } else {
          setRentalHistory([]);
        }
      } catch (error) {
        console.error('Error fetching user data or rental history:', error);
        setError('Error fetching data. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [userId]);

  const handleBanToggle = async () => {
    try {
      const userDocRef = doc(db, 'Users', userId);
      await setDoc(
        userDocRef,
        { banned: !banned }, 
        { merge: true }
      );

      setBanned(!banned);
      alert(`User has been ${!banned ? 'banned' : 'unbanned'}.`);
    } catch (error) {
      console.error('Error updating ban status:', error);
      alert('Failed to update ban status. Please try again.');
    }
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div className="user-details-container">
      <div className="user-details-card">
        <div className="user-details-header">
          <h2>User Details</h2>
          <p>Manage and review user information</p>
        </div>

        <div className="user-details-section">
          <h3>Basic Information</h3>
          <ul>
            <li><span>First Name:</span> {user?.firstName || 'N/A'}</li>
            <li><span>Last Name:</span> {user?.lastName || 'N/A'}</li>
            <li><span>Email:</span> {user?.email || 'N/A'}</li>
            <li><span>Phone:</span> {user?.phone || 'N/A'}</li>
            <li><span>User Status:</span> {status || 'Pending'}</li>
            <li><span>Account Status:</span> {banned ? 'Banned' : 'Active'}</li>
          </ul>
        </div>

        <div className="user-details-section rental-history">
          <h3>Rental History</h3>
          <ul>
            {rentalHistory.length === 0 ? (
              <li>No rental history available.</li>
            ) : (
              rentalHistory.slice(0, showAllRentals ? rentalHistory.length : 3).map((rental, index) => (
                <li key={index}>
                  <span>Car Model:</span> {rental.carModel}<br />
                  <span>Start Date:</span> {new Date(rental.startDate.seconds * 1000).toLocaleDateString()}<br />
                  <span>End Date:</span> {new Date(rental.endDate.seconds * 1000).toLocaleDateString()}<br />
                  <span>Total Price:</span> {rental.totalPrice + "$" || 'N/A'}
                </li>
              ))
            )}
          </ul>
          {rentalHistory.length > 3 && (
            <button
              onClick={() => setShowAllRentals(!showAllRentals)}
              className="toggle-rentals-button"
            >
              {showAllRentals ? 'Show Less' : 'Show More'}
            </button>
          )}
        </div>

        <div className="user-details-buttons">
          <button onClick={() => navigate('/dashboard')}>Go Back</button>
          <button onClick={() => navigate(`/edit-user/${userId}`)} className="edit-link">Edit User</button>
          <button onClick={() => navigate(`/review-images/${userId}`)} className="review-link">Review Images</button>
          <button onClick={handleBanToggle} className="ban-toggle-button">
            {banned ? 'Unban User' : 'Ban User'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserDetails;
