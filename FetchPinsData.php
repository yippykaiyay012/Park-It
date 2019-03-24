<?php
    $con=mysqli_connect("mysql4.000webhost.com","a3460617_parkit","thePassword","a3460617_parkit");
      
  
    
    $statement = mysqli_prepare($con, "SELECT * FROM pins");
    mysqli_stmt_execute($statement);
    
    mysqli_stmt_store_result($statement);
    mysqli_stmt_bind_result($statement, $title, $snippet, $position);
    
    
    $pins = array();
    
    while(mysqli_stmt_fetch($statement)){
        $pins["title"] = $title;
        $pins["snippet"] = $snippet;
        $pins["position"] = $position;   
    	echo json_encode($pins);     
    }
    
    
    mysqli_close($con);
?>
