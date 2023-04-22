package com.th.chapter_11.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.th.chapter_11.R
import com.th.chapter_11.viewmodel.ShelterViewModel

class FragmentSplitOne : Fragment(){

    companion object {
        const val SHELTER = "shelter"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_split_0,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TH: binds ViewModel to Activity's life cycle.
        // TH: helps fragments of same Activity communicate with each other.
        // TH: deploys singleton pattern for ViewModel, shared by fragments.
        val shelterViewModel = ViewModelProvider(requireActivity()).get(ShelterViewModel::class.java)

        /*
        // TH: updates LiveData, to be observed by Activity (i.e. ShelterActivity).
        shelterViewModel.increaseTotalAgain()
        Log.i("FragmentSplitOne:onViewCreated:it", shelterViewModel.getTotal().value.toString())
        */

        view.findViewById<TextView>(R.id.fragment_split_0_text_view).text = "fragment_split_1"
    }

}