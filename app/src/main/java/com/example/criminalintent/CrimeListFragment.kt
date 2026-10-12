package com.example.criminalintent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class CrimeListFragment : Fragment() {

    private lateinit var crimeAdapter: CrimeAdapter
    private lateinit var repository: CrimeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = CrimeRepository.getInstance(
            requireContext().applicationContext
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_crime_list,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.crime_recycler_view
            )


        crimeAdapter = CrimeAdapter(emptyList()) { crime ->

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    CrimeDetailFragment.newInstance(crime.id)
                )
                .addToBackStack(null)
                .commit()
        }


        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerView.adapter = crimeAdapter

        view.findViewById<Button>(
            R.id.add_crime_button
        ).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                repository.addCrime(
                    Crime(title = "New Crime")
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                repository.getCrimes().collect { crimes ->
                    crimeAdapter.updateCrimes(crimes)
                }
            }
        }
    }
}
