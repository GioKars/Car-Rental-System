<?php
header("Access-Control-Allow-Origin: http://localhost:3000");  // Allow requests from React app
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

$userId = $_GET['userId'];  // Get the userId from the query parameters
$dir = "uploads/$userId";   // Path to the user-specific folder

// Check if the directory exists
if (is_dir($dir)) {
    $files = scandir($dir);  // Get all files in the directory
    $imageFiles = [];

    // Filter the files to include only image files
    foreach ($files as $file) {
        $filePath = "$dir/$file";
        // Check if the file is an image (optional: you can check file extensions like .jpg, .jpeg, .png)
        if (in_array(strtolower(pathinfo($file, PATHINFO_EXTENSION)), ['jpg', 'jpeg', 'png', 'gif'])) {
            $imageFiles[] = $file;  // Add to the image files array
        }
    }

    // Return the image files as JSON
    echo json_encode($imageFiles);
} else {
    // Return an empty array or an error message if the directory doesn't exist
    echo json_encode([]);
}
?>
