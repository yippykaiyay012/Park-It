<?php
    $con=mysqli_connect("mysql4.000webhost.com","a3460617_parkit","password","a3460617_parkit");
      

    $statement = mysqli_prepare($con, "SELECT title, snippet, position FROM pins");
    
    mysqli_stmt_execute($statement);

    mysqli_stmt_store_result($statement);
    mysqli_stmt_bind_result($statement, $title, $snippet, $position);

    
    $pins = array();
    

    while(mysqli_stmt_fetch($statement)){
        $row_array["title"] = $title;
        $row_array["snippet"] = $snippet;
        $row_array["position"] = $position;

        array_push($pins, $row_array);
    }

    echo json_encode($pins);

    
    mysqli_close($con);
?>
