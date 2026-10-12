package com.example.criminalintent
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import java.util.UUID
import   android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File




class CrimeDetailFragment : Fragment() {

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val currentCrime = crime ?: return@registerForActivityResult

        if (success) {
            currentCrime.photoFileName = "IMG_${currentCrime.id}.jpg"
            pendingPhotoName = currentCrime.photoFileName
            viewLifecycleOwner.lifecycleScope.launch {
                repository.updateCrime(currentCrime)
            }

            view?.findViewById<ImageView>(R.id.crime_photo)
                ?.setImageURI(photoUri)
        }
    }


    private lateinit var repository: CrimeRepository
    private var crime: Crime? = null

private var pendingPhotoName: String? = null
    private var photoUri: Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = CrimeRepository.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_crime_detail,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val titleEdit =
            view.findViewById<EditText>(R.id.crime_title_edit)
        val dateButton =
            view.findViewById<Button>(R.id.crime_date_button)
        val solvedCheck =
            view.findViewById<CheckBox>(R.id.crime_solved_checkbox)
        val deleteButton =
            view.findViewById<Button>(R.id.crime_delete_button)

        val crimeId = requireArguments()
            .getString(ARG_CRIME_ID)
            ?.let(UUID::fromString) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val loadedCrime = repository.getCrime(crimeId) ?: return@launch
            crime = loadedCrime

            titleEdit.setText(loadedCrime.title)
            solvedCheck.isChecked = loadedCrime.isSolved
            dateButton.text = DateFormat.getDateInstance()
                .format(loadedCrime.date)
        }

        titleEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveChanges(
                    titleEdit.text.toString(),
                    solvedCheck.isChecked
                )
            }
        }

        solvedCheck.setOnCheckedChangeListener { _, isChecked ->
            saveChanges(titleEdit.text.toString(), isChecked)
        }

        dateButton.setOnClickListener {
            val currentCrime = crime ?: return@setOnClickListener
            val calendar = Calendar.getInstance().apply {
                time = currentCrime.date
            }

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    currentCrime.date = calendar.time
                    dateButton.text = DateFormat.getDateInstance()
                        .format(currentCrime.date)
                    saveCrime()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        deleteButton.setOnClickListener {

            val photoButton =
                view.findViewById<Button>(R.id.crime_photo_button)

            photoButton.setOnClickListener {
                val currentCrime = crime ?: return@setOnClickListener

                val photoName = "IMG_${currentCrime.id}.jpg"
                pendingPhotoName = photoName

                val photoDir = File(
                    requireContext().filesDir,
                    "crime_photos"
                )
                photoDir.mkdirs()

                val photoFile = File(photoDir, photoName)

                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    photoFile
                )

                takePicture.launch(photoUri!!)
            }


            val shareButton =
                view.findViewById<Button>(R.id.crime_share_button)

            shareButton.setOnClickListener {
                val currentCrime = crime ?: return@setOnClickListener

                val report = """
        Crime Report

        Title: ${titleEdit.text}
        Date: ${DateFormat.getDateInstance().format(currentCrime.date)}
        Solved: ${if (solvedCheck.isChecked) "Yes" else "No"}
    """.trimIndent()

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, report)
                    putExtra(Intent.EXTRA_SUBJECT, "Crime Report")
                }

                startActivity(
                    Intent.createChooser(shareIntent, "Share Crime Report")
                )
            }

            val currentCrime = crime ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                repository.deleteCrime(currentCrime)
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun saveChanges(title: String, solved: Boolean) {
        val currentCrime = crime ?: return
        currentCrime.title = title
        currentCrime.isSolved = solved
        saveCrime()
    }

    private fun saveCrime() {
        val currentCrime = crime ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            repository.updateCrime(currentCrime)
        }
    }

    companion object {
        private const val ARG_CRIME_ID = "crime_id"

        fun newInstance(crimeId: UUID): CrimeDetailFragment {
            return CrimeDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CRIME_ID, crimeId.toString())
                }
            }
        }
    }
}
