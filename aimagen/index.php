<?php



// Verificar si hay un archivo enviado

// Obtener información del archivo
$fileName = basename($_FILES['file']['name']);
$filePath = $_FILES['file']['tmp_name'];

// Obtener fecha actual para crear la estructura de carpetas
$currentDate = getdate();
$year = $currentDate['year'];
$month = str_pad($currentDate['mon'], 2, '0', STR_PAD_LEFT);
$day = str_pad($currentDate['mday'], 2, '0', STR_PAD_LEFT);

// Crear la ruta completa donde se guardará el archivo
$destinationDirectory = 'storage/' . $year . '/' . $month . '/' . $day . '/';
if (!file_exists($destinationDirectory)) {
    mkdir($destinationDirectory, 0777, true); // Crear directorios recursivamente
}
$destinationPath = $destinationDirectory . $fileName;

    
// Mover el archivo al destino final
if (move_uploaded_file($filePath, $destinationPath)) {
    echo "Archivo subido exitosamente a: " . $destinationPath;
    file_put_contents('1.log', date('H:i:s d/m/Y').'Archivo subido exitosamente a:', FILE_APPEND );
} else {
	file_put_contents('1.log', date('H:i:s d/m/Y').'Error al mover el archivo a la carpeta destino', FILE_APPEND );
    echo "Error al mover el archivo a la carpeta destino";
}

