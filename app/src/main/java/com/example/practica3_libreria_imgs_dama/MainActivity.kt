package com.example.practica3_libreria_imgs_dama

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.io.IOException
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var url = "https://plus.unsplash.com/premium_photo-1710965560034-778eedc929ff?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8bXVuZG8lMjBoZXJtb3NvfGVufDB8fDB8fHww"

    /* DECLARACION DE VARIABLES DE VINCULACION CON LA VISTA*/
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRotate: Button
    private lateinit var btnZoom: Button
    private lateinit var btnMirror: Button
    private lateinit var btnGrayscale: Button
    private lateinit var btnCrop: Button
    private lateinit var btnDowload: Button

    private lateinit var btnSeleccionarImagen: Button


    private var currentRotation = 0f
    private var currentScale = 1f
    private var isMirrored = false
    private var isGrayscale = false
    private var isCropped = false

    private val REQUEST_CODE_GALLERY = 100
    private val REQUEST_PERMISSION = 101
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Inicializacion de componentes
        setInitComponents()
        //Cargar Imagen inicial
        loadPreviewImage()
        //Funcion de botones
        setAcctionsEvents()

    }

    private fun setInitComponents() {
        imageView = findViewById(R.id.imageView)
        progressBar = findViewById(R.id.progressBar)
        btnRotate = findViewById(R.id.btnRotate)
        btnZoom = findViewById(R.id.btnZoom)
        btnMirror = findViewById(R.id.btnMirror)
        btnGrayscale = findViewById(R.id.btnGrayscale)
        btnCrop = findViewById(R.id.btnCrop)
        btnDowload = findViewById(R.id.btnDownload)

        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen)

    }

    /* esta función es responsable de mostrar por primera vez nuestra imagen en la aplicación*/
    private fun loadPreviewImage() {
        progressBar.visibility = View.VISIBLE

        Picasso.get().load(url).into(imageView, object : Callback {
            override fun onSuccess() {
                progressBar.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        })

    }
/*esta función es responsable de establecer el
comportamiento de los botones en nuestra aplicación*/
    private fun setAcctionsEvents() {
        btnRotate.setOnClickListener {
            currentRotation += 90f
            applyTransformation()
        }

        btnZoom.setOnClickListener {
            currentScale += 0.5f
            applyTransformation()
        }

        btnMirror.setOnClickListener {
            isMirrored = !isMirrored
            applyTransformation()
        }

        btnGrayscale.setOnClickListener {
            isGrayscale = !isGrayscale
            applyTransformation()
        }

        btnCrop.setOnClickListener {
            isCropped = !isCropped
            applyTransformation()
        }

        btnDowload.setOnClickListener {
            saveImageToGallery()
        }

    btnSeleccionarImagen.setOnClickListener {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*" // Opcional, filtra solo imágenes
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }


}

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun saveImageToGallery() {
        imageView.buildDrawingCache()

        val bitmap = imageView.drawingCache

        if (bitmap != null) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Imagen_editada_practica3_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val contentResolver = applicationContext.contentResolver
            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let { uri ->
                try {
                    val outPutStream = contentResolver.openOutputStream(uri)
                    outPutStream?.let { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        stream.flush()
                        stream.close()

                        Toast.makeText(this, "Imagen Guardada en la galeria", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al Guardar la imagen en la galeria", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "No se pudo guardar la imagen ", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No se pudo obtener la imagen", Toast.LENGTH_SHORT).show()
        }

    }

    /* Para rotaciones */
    inner class RotationTransformation(private  val rotation: Float) : Transformation {
        override fun transform(source: Bitmap) : Bitmap {
            val matrix = Matrix()
            matrix.postRotate(rotation)
            val rotateBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

            if (source != rotateBitmap) {
                source.recycle()
            }

            return rotateBitmap
        }

        override fun key(): String  = "rotation$rotation"
    }

    /* Zoom de la imagen  */
    inner class ScaleTransformation(private val scale: Float) : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            val scaleBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

            if (source != scaleBitmap) {
                source.recycle()
            }

            return scaleBitmap
        }

        override fun key(): String = "scale$scale"
    }

    /* reflejar la imagen */
    inner class MirrorTransformation(private val mirror: Boolean) : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            if (!mirror) return source

            val matrix = Matrix()
            matrix.postScale(-1f, 1f, source.width / 2f, source.height / 2f)

            val mirrorBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

            if (source != mirrorBitmap) {
                source.recycle()
            }

            return mirrorBitmap
        }

        override fun key(): String = "mirror$mirror"
    }

    /*Filtro de escala de grises*/
    inner class GrayscaleTransformation(private val grayscale: Boolean) : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            if (!grayscale) return source

            val with = source.width
            val height = source.height
            val grayscaleBitmap = Bitmap.createBitmap(with, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(grayscaleBitmap)
            val paint = Paint()

            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(colorMatrix)
            paint.colorFilter = filter

            canvas.drawBitmap(source, 0f, 0f, paint)

            if (source != grayscaleBitmap) {
                source.recycle()
            }

            return grayscaleBitmap
        }

        override fun key(): String = "grayscale$grayscale"
    }

    /*recortar imagen*/
    inner class CropTransformation(private val crop: Boolean) : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            if (!crop) return source

            val size = minOf(source.width, source.height)
            val x = (source.width - size) / 2
            val y = (source.height - size) / 2

            val croppedBitmap = Bitmap.createBitmap(source, x, y, size, size)

            if (source != croppedBitmap) {
                source.recycle()
            }

            return croppedBitmap
        }

        override fun key(): String = "crop$crop"
    }

    private fun applyTransformation() {
        progressBar.visibility = View.VISIBLE

        Picasso.get().load(url)
            .transform(RotationTransformation(currentRotation))
            .transform(ScaleTransformation(currentScale))
            .transform(MirrorTransformation(isMirrored))
            .transform(GrayscaleTransformation(isGrayscale))
            .transform(CropTransformation(isCropped))
            .into(imageView, object : Callback {
                override fun onSuccess() {
                    progressBar.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            })
    }

}