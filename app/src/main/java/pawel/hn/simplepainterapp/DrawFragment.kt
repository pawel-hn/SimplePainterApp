package pawel.hn.simplepainterapp

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pawel.hn.simplepainterapp.databinding.FragmentDrawBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.roundToInt


const val MIME_TYPE = "image/jpg"
const val MY_AUTHORITY = "pawel.hn.simplepainterapp.fileprovider"


class DrawFragment : Fragment(R.layout.fragment_draw) {

    private lateinit var binding: FragmentDrawBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var loadImage: ActivityResultLauncher<String>
    var imageSave = false
    private lateinit var imageBitmap: Bitmap
    private var uriPath: Any? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDrawBinding.bind(view)
        loadImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.imageviewBackground.setImageURI(it)
        }
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "No permission", Toast.LENGTH_SHORT).show()
                }
            }
        setUi(binding)
        setHasOptionsMenu(true)
    }

    private fun setUi(binding: FragmentDrawBinding) {
        binding.apply {
            customView.setBrushSize(resources.getFloat(R.dimen.brush_size_small))
            btnBrushSize.setOnClickListener {
                showBrushSizeDialog()
            }

            colorSlider.setGradient(resources.getIntArray(R.array.colors), 500)

            colorSlider.setListener { _, color ->
                val hexColor = "#" + Integer.toHexString(color)
                customView.setColor(hexColor)
            }

            btnUndo.setOnClickListener {
                customView.undo()
            }

            btnClear.setOnClickListener {
                if (customView.drawPaths.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_clear),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.clear_all))
                        .setMessage(getString(R.string.clear_all_warning))
                        .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                            customView.clearAll()
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_load -> {
                loadBackgroundButtonResponse()
            }
            R.id.menu_clear -> {
                binding.imageviewBackground.setImageDrawable(null)
            }
            R.id.menu_save -> {
                saveButtonResponse()
            }
            R.id.menu_share -> {
                shareImage()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun shareImage() {
        if (imageSave) {
            Log.d("PHN", "path share $uriPath")
            Log.d("PHN", "path share string ${uriPath.toString()}")
//            MediaScannerConnection.scanFile(
//                requireContext(), arrayOf(uriPath.toString()), arrayOf(MIME_TYPE)) { _, uri ->
//                val shareIntent = Intent().apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_STREAM, uri)
//                    type = MIME_TYPE
//                }
//                Log.d("PHN", "media uri: $uri")
//                startActivity(Intent.createChooser(shareIntent, "Share image"))
//            }

            Log.d("PHN", "path share string ${convertPath(uriPath.toString())}")


            val contextExternalPics = context?.getExternalFilesDir(DIRECTORY_PICTURES)
            Log.d("PHN", "contextExternalPics $contextExternalPics")

            val file = File((uriPath as Uri).path!!)
            Log.d("PHN", "abs ${file.absolutePath}")
            val uriFileProvider = FileProvider.getUriForFile(requireContext(), MY_AUTHORITY, file)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uriFileProvider)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                type = MIME_TYPE
            }
            startActivity(Intent.createChooser(shareIntent, "Share image"))

        } else {

            Toast.makeText(requireContext(), "Save image before sharing!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loadBackgroundButtonResponse() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadImage.launch("image/*")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission for access to phone storage")
                    .setMessage("Permission required for background image")
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun saveButtonResponse() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    saveImageToMemory(requireContext(), binding.frameLayoutDraw)
                }
                Toast.makeText(requireContext(), "Image saved", Toast.LENGTH_SHORT).show()
                imageSave = true
            }

            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission for access to phone storage")
                    .setMessage("Permission required for saving image")
                    .show()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun saveImageToMemory(context: Context, view: View) {
        var pathDir = ""
        val pathCont: String
        val title = "SimpleDraw_${System.currentTimeMillis()}.jpg"
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, title)
                put(MediaStore.Images.Media.DISPLAY_NAME, title)
                put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE)
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000L)
            }
            val contentResolver = context.applicationContext.contentResolver
            uriPath =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)


            fos = uriPath?.let { contentResolver.openOutputStream(it as Uri) }
            pathCont = uriPath.toString()
            Log.d("PHN", "path cont: $pathCont")
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(dir, title)
            uriPath = image.absolutePath
            pathDir = image.absolutePath
            fos = FileOutputStream(image)
        }
        Log.d("PHN", "path dir: $pathDir")
        imageBitmap = getBitmapFromView(view)
        fos.use {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitMap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitMap)
        val image = view.background
        if (image != null) {
            image.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitMap
    }


    private fun showBrushSizeDialog() {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_brush)
        }
        val sizeSlider = dialog.findViewById<Slider>(R.id.brush_size_slider)
        val imageBrush = dialog.findViewById<ImageView>(R.id.image_brush)
        val imageParams = imageBrush.layoutParams

        val size = binding.customView.getBrushSize()
        val factor = resources.displayMetrics.density
        val sizeDp = size / factor

        sizeSlider.value = sizeDp.roundToInt().toFloat()
        imageParams.height = size.toInt()
        imageParams.width = size.toInt()
        imageBrush.layoutParams = imageParams

        sizeSlider.addOnChangeListener { _, value, _ ->
            imageParams.height = (value * factor).toInt()
            imageParams.width = (value * factor).toInt()
            imageBrush.layoutParams = imageParams
            binding.customView.setBrushSize(value)
        }
        dialog.show()
    }


    private fun convertPath(path: String): String = path.substring(path.indexOf(':') + 1)


}


