import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { db } from '../firebase'; // Firebase import
import { doc, getDoc, setDoc } from 'firebase/firestore'; // Firestore functions import
import '../styles/ReviewImages.css'; // Custom styling for image review

const ReviewImages = () => {
  const { userId } = useParams(); // Get userId from URL params
  const [imageFiles, setImageFiles] = useState([]); // State for storing image files
  const [imageStatus, setImageStatus] = useState({}); // State for image approval status
  const [loading, setLoading] = useState(true); // State for loading state
  const [error, setError] = useState(''); // State for error messages
  const [userStatus, setUserStatus] = useState('Pending'); // State for user status from Firebase
  const navigate = useNavigate(); // Initialize navigate function for routing

  useEffect(() => {
    const fetchUserDataAndImages = async () => {
      try {
        // Fetch user status from Firebase
        const userDocRef = doc(db, 'Users', userId);
        const userSnap = await getDoc(userDocRef);

        if (userSnap.exists()) {
          const userData = userSnap.data();
          setUserStatus(userData.status || 'Pending'); // Set the status from Firebase
        } else {
          setError('User not found');
        }

        // Fetch image files from backend
        const response = await fetch(`http://localhost/list_files.php?userId=${userId}`);
        const files = await response.json(); // Parse the response as JSON
        setImageFiles(files); // Set the fetched files into state
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Failed to load data.');
      } finally {
        setLoading(false);
      }
    };

    fetchUserDataAndImages();
  }, [userId]);

  const handleApprove = () => {
    const updatedStatus = imageFiles.reduce((acc, file) => {
      acc[file] = 'Approved';
      return acc;
    }, {});
    setImageStatus(updatedStatus); // Update all images to approved status
  };

  const handleReject = () => {
    const updatedStatus = imageFiles.reduce((acc, file) => {
      acc[file] = 'Rejected';
      return acc;
    }, {});
    setImageStatus(updatedStatus); // Update all images to rejected status
  };

  const saveReview = async () => {
    try {
      const userDocRef = doc(db, 'Users', userId);
      const status = Object.values(imageStatus).includes('Rejected') ? 'Rejected' : 'Approved';
      const allowImageReupload = status === 'Rejected';

      await setDoc(
        userDocRef,
        { 
          status: status,
          allowImageReupload: allowImageReupload,
        },
        { merge: true }
      );

      alert('Review status saved successfully.');
      navigate(`/user/${userId}`);
    } catch (err) {
      console.error('Error saving review status:', err);
      alert('Failed to save review status. Please try again.');
    }
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div className="review-images-container">
      <h2>Review User Images</h2>
      <p>User Status: {userStatus}</p> {/* Display status from Firebase */}
      <div className="images-grid">
        {imageFiles.length > 0 ? (
          imageFiles.map((file, index) => (
            <div key={index} className="image-item">
              <img
                src={`http://localhost/uploads/${userId}/${file}`}
                alt={`User upload ${index + 1}`}
                loading="lazy"
              />
              <p>Status: {imageStatus[file] || userStatus}</p> {/* Display dynamic status */}
            </div>
          ))
        ) : (
          <p>No images to review.</p>
        )}
      </div>

      <div className="approve-button">
        <button onClick={handleApprove}>Approve</button>
      </div>
      <div className="reject-button">
        <button onClick={handleReject}>Reject</button>
      </div>

      <button onClick={saveReview}>Save Review</button>
    </div>
  );
};

export default ReviewImages;
