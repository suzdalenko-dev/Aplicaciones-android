<?php
# WORKING WITH YESTARDAY'S FILES
$date_last_day = explode('-', date('Y-m-d', strtotime('-1 day')));
$oneDayAgo = 'storage/'.$date_last_day[0].'/'.$date_last_day[1].'/'.$date_last_day[2].'/';
if(!file_exists($oneDayAgo)){ 
    echo json_encode(['res'=>'error', 'message'=>'Directorio del dia pasado no existe '.$oneDayAgo]);
    die();
}
$num_files = 0;
$files = scandir($oneDayAgo);
$file_name = '2020-01-01-01-01-01-001.jpg';
$url_file_last_day  = '';
foreach ($files as $file) {
    if ($file === '.' || $file === '..') { continue; }
    if($file > $file_name){
        $file_name = $file;
        $url_file_last_day  = $oneDayAgo.$file;
    }
    $num_files++;
}
if($num_files == 0){
    echo json_encode(['res'=>'error', 'message'=>'No existen arhivos de ayer '.$oneDayAgo]);
    die();
}



# WORKING WITH TODAS'S FILES
$todayDirectory = 'storage/'.date('Y').'/'.date('m').'/'.date('d').'/';
if(!file_exists($todayDirectory)){ 
    echo json_encode(['res'=>'error', 'message'=>'Directorio de hoy no existe '.$todayDirectory ]); 
    die();
}
$num_files = 0;
$files = scandir($todayDirectory);
$file_name       = '2020-01-01-01-01-01-001.jpg';
$url_file_today  = '';
foreach ($files as $file) {
    if ($file === '.' || $file === '..') { continue; }
    if($file > $file_name){
        $file_name = $file;
        $url_file_today  = $todayDirectory.$file;
    }
    $num_files++;
}

if($num_files == 0){
    echo json_encode(['res'=>'error', 'message'=>'No existen arhivos de hoy '.$todayDirectory]);
    die();
}




# RESUTLS
echo json_encode([
    'res'=>'ok',
    'last_day'=>'https://intranet.froxa.net/aimagen/'.$url_file_last_day,
    'today'=>'https://intranet.froxa.net/aimagen/'.$url_file_today
]);


