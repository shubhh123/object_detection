package org.tensorflow.lite.examples.objectdetection

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentCameraBinding
import org.tensorflow.lite.task.vision.detector.Detection
import java.io.InputStream

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString("imageUri")?.let {
            imageUri = Uri.parse(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this
        )

        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
        val imageRotation = getImageRotation(imageUri)
        objectDetectorHelper.detect(bitmap, imageRotation)
    }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        if (results != null) {
            binding.overlay.setResults(results, imageHeight, imageWidth)
        }
        binding.overlay.invalidate()
    }

    override fun onError(error: String) {
        // Handle error
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getImageRotation(uri: Uri): Int {
        val ei = ExifInterface(requireContext().contentResolver.openInputStream(uri)!!)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
