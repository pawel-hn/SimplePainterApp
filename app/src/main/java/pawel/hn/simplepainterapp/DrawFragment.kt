package pawel.hn.simplepainterapp

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import pawel.hn.simplepainterapp.databinding.FragmentDrawBinding
import kotlin.math.roundToInt

class DrawFragment : Fragment(R.layout.fragment_draw) {

    lateinit var binding: FragmentDrawBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("PHN", "onViewCreated called")

        binding = FragmentDrawBinding.bind(view)

        binding.apply {
            customView.setBrushSize(resources.getFloat(R.dimen.brush_size_small))
            btnBrushSize.setOnClickListener {
                showBrushSizeDialogNew()
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
               if(customView.drawPaths.isEmpty()) {
                   Toast.makeText(requireContext(), getString(R.string.toast_clear), Toast.LENGTH_SHORT).show()
               } else {
                   AlertDialog.Builder(requireContext())
                       .setTitle(getString(R.string.clear_all))
                       .setMessage(getString(R.string.clear_all_warning))
                       .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                           customView.clearAll()
                           dialog.dismiss()
                       }
                       .setNegativeButton(getString(R.string.no)){ dialog, _ ->
                           dialog.dismiss()
                       }
                       .show()
               }
            }
        }
       //super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        // super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showBrushSizeDialogNew() {
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


        Log.d("PHN", "size: $size")
        Log.d("PHN", "size: $sizeDp")
        Log.d("PHN", "factor: $factor")

        sizeSlider.addOnChangeListener { _, value, _ ->

            imageParams.height = (value * factor).toInt()
            imageParams.width = (value * factor).toInt()
            imageBrush.layoutParams = imageParams

            binding.customView.setBrushSize(value)
        }

        dialog.show()
    }


}
