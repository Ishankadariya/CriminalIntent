package com.example.criminalintent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat

class CrimeAdapter(
    private var crimes: List<Crime>,
    private val onCrimeClick: (Crime) -> Unit
) : RecyclerView.Adapter<CrimeAdapter.CrimeViewHolder>() {

    class CrimeViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val title: TextView =
            view.findViewById(R.id.crime_title)

        val date: TextView =
            view.findViewById(R.id.crime_date)

        val solved: CheckBox =
            view.findViewById(R.id.crime_solved)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CrimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_crime, parent, false)

        return CrimeViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CrimeViewHolder,
        position: Int
    ) {
        val crime = crimes[position]

        holder.title.text = crime.title
        holder.date.text =
            DateFormat.getDateInstance().format(crime.date)
        holder.solved.isChecked = crime.isSolved

        holder.itemView.setOnClickListener {
            onCrimeClick(crime)
        }
    }

    override fun getItemCount(): Int = crimes.size

    fun updateCrimes(newCrimes: List<Crime>) {
        crimes = newCrimes
        notifyDataSetChanged()
    }
}
