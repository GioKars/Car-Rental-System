<?php
if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_FILES['image']) && isset($_POST['userId'])) {
    // Retrieve userId from POST data
    $userId = $_POST['userId']; 

    // Debugging the userId (optional)
    file_put_contents('debug.log', "User ID: " . $userId . "\n", FILE_APPEND);

    // Define the target directory for user-specific folder
    $target_dir = "uploads/$userId/"; // Folder named by userId

    // Check if the directory exists, if not, create it
    if (!is_dir($target_dir)) {
        mkdir($target_dir, 0777, true); // Create directory with permissions
    }

    // Define the target file path
    $target_file = $target_dir . basename($_FILES["image"]["name"]);

    // Optional: Check if the file is an image
    $check = getimagesize($_FILES["image"]["tmp_name"]);
    if ($check !== false) {
        // Move the uploaded file to the target directory
        if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
            // Successfully uploaded
            echo json_encode([
                "status" => "success",
                "message" => "The file " . htmlspecialchars(basename($_FILES["image"]["name"])) . " has been uploaded successfully."
            ]);
        } else {
            // Error during upload
            http_response_code(500);
            echo json_encode([
                "status" => "error",
                "message" => "Sorry, there was an error uploading your file."
            ]);
        }
    } else {
        // The file is not a valid image
        http_response_code(400);
        echo json_encode([
            "status" => "error",
            "message" => "File is not a valid image."
        ]);
    }
} else {
    // Missing parameters (either file or userId)
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "No file uploaded or invalid request."
    ]);
}
?>
