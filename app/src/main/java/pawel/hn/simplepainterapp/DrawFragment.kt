package pawel.hn.simplepainterapp

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import pawel.hn.simplepainterapp.databinding.FragmentDrawBinding

class DrawFragment : Fragment(R.layout.fragment_draw) {

    lateinit var binding: FragmentDrawBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("PHN", "onViewCreated called")

        binding = FragmentDrawBinding.bind(view)

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
        super.onViewCreated(view, savedInstanceState)
    }

    private fun showBrushSizeDialog() {
        val dialog = Dialog(requireContext()).apply {

            setContentView(R.layout.dialog_brush_size)

            //button brush small
            findViewById<ImageButton>(R.id.btn_small).setOnClickListener {
                binding.customView.setBrushSize(resources.getFloat(R.dimen.brush_size_small))
                dismiss()
            }

            //button brush medium
            findViewById<ImageButton>(R.id.btn_medium).setOnClickListener {
                binding.customView.setBrushSize(resources.getFloat(R.dimen.brush_size_medium))
                dismiss()
            }

            //button brush big
            findViewById<ImageButton>(R.id.btn_big).setOnClickListener {
                binding.customView.setBrushSize(resources.getFloat(R.dimen.brush_size_big))
                dismiss()
            }
        }
        dialog.show()
    }
}
