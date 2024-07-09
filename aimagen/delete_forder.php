<?php

$array_3_month = explode('-', date('Y-m', strtotime('-3 month')));
$array_4_month = explode('-', date('Y-m', strtotime('-4 month')));
$array_5_month = explode('-', date('Y-m', strtotime('-5 month')));

$date_3_month = 'storage/'.$array_3_month[0].'/'.$array_3_month[1].'/';
$date_4_month = 'storage/'.$array_4_month[0].'/'.$array_4_month[1].'/';
$date_5_month = 'storage/'.$array_5_month[0].'/'.$array_5_month[1].'/';

if(file_exists($date_3_month)){ system("rm -rf ".escapeshellarg($date_3_month)); }
if(file_exists($date_4_month)){ system("rm -rf ".escapeshellarg($date_4_month)); }
if(file_exists($date_5_month)){ system("rm -rf ".escapeshellarg($date_5_month)); }

# $urls = [$date_3_month, $date_4_month, $date_5_month];
# $days = ['01','02','03','04','05','06','07','08','09','10','11','12','13','14','15','16','17','18','19','20','21','22','23','24','25','26','27','28','29','30','31','32','33'];
# 
# 
# foreach($urls as $u){
# 	foreach ($days as $d) {
#  		$delete_url = $u.$d;
# 		echo $delete_url.'<br>';
# 
# 		if(file_exists($delete_url)){
# 			echo '<br>EXISTE '. $delete_url.'<br>';
# 			$current_files = array_diff(scandir($delete_url), array('.', '..'));
# 			array_map('unlink', glob("$delete_url/*.*"));
# 			rmdir($delete_url);
# 		}	
# 
# 		if(file_exists($delete_url)){
# 			system("rm -rf ".escapeshellarg($delete_url));
# 		}
# 	}
# }
# 
# echo json_encode([
# 	$date_3_month,
# 	$date_4_month,
# 	$date_3_month
# ]);