<?php

// Verificar si hay un archivo enviado

// Obtener información del archivo
$fileName = basename($_FILES['file']['name']);
$filePath = $_FILES['file']['tmp_name'];
if (strtolower(pathinfo($fileName, PATHINFO_EXTENSION)) != 'jpg') {
    echo "Error PATHINFO_EXTENSION";
    die();
}

// Crear la ruta completa donde se guardará el archivo
$destinationDirectory = 'storage/'.date('Y').'/'.date('m').'/'.date('d').'/';
if (!file_exists($destinationDirectory)) { mkdir($destinationDirectory, 0777, true); }
$destinationPath = $destinationDirectory.$fileName;

// Mover el archivo al destino final
if (move_uploaded_file($filePath, $destinationPath)) {
    echo "Archivo subido exitosamente a: " . $destinationPath;
    # file_put_contents('1.log', date('H:i:s d/m/Y').'Archivo subido exitosamente a:'.$destinationDirectory.PHP_EQL, FILE_APPEND ); cc
} else {
	# file_put_contents('1.log', date('H:i:s d/m/Y').'Error al mover el archivo a la carpeta destino'.PHP_EQL , FILE_APPEND );
    echo "Error al mover el archivo a la carpeta destino";
}

