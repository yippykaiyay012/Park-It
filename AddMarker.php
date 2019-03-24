<?php
    $con=mysqli_connect("mysql4.000webhost.com","a3460617_parkit","thePassword","a3460617_parkit");
    
    $title = $_POST["title"];
    $snippet = $_POST["snippet"];
    $position = $_POST["position"];
    
    
    $statement = mysqli_prepare($con, "INSERT INTO pins (title, snippet, position) VALUES (?, ?, ?)");
    mysqli_stmt_bind_param($statement, "sss", $title, $snippet, $position);
    mysqli_stmt_execute($statement);
    
    mysqli_stmt_close($statement);
    
    mysqli_close($con);
?>
