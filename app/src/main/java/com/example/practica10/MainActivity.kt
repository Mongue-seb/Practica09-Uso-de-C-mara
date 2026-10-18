package com.example.practica10
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCamaraScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCamaraScreen() {
    val context = LocalContext.current

    // Estados para guardar el URI de la foto y saber si ya se tomó una
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var photoTaken by remember { mutableStateOf(false) }

    // Generador de un archivo temporal vacío donde la cámara guardará la foto
    fun crearArchivoUri(context: Context): Uri {
        val tempFile = File.createTempFile("IMG_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Debe coincidir con el AndroidManifest
            tempFile
        )
    }

    // Contrato para abrir la cámara y tomar la foto (TakePicture)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            photoTaken = success
        }
    )

    // Contrato para pedir el permiso de la cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Si da el permiso, creamos el archivo vacío y abrimos la cámara
                val uri = crearArchivoUri(context)
                imageUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Práctica de Cámara") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 5. Renderizar la fotografía capturada
            if (photoTaken && imageUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Fotografía capturada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Espacio vacío o instrucciones si no hay foto
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Presiona el botón para tomar una foto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Botón de acción para solicitar permiso y activar cámara
            Button(
                onClick = {
                    val permisoEstado = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permisoEstado == PackageManager.PERMISSION_GRANTED) {
                        // Si ya tiene el permiso, abrimos la cámara directo
                        val uri = crearArchivoUri(context)
                        imageUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        // Si no lo tiene, lanzamos la solicitud de permiso
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tomar Fotografía")
            }
        }
    }
}