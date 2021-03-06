package com.yasin.handzap.ui.formList

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.yasin.handzap.Handzap
import com.yasin.handzap.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_form_list.*
import javax.inject.Inject

/**
 * Created by Yasin on 24/1/20.
 */
class FormListFragment : Fragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var formsViewModel: FormsViewModel
    private val formListAdapter : FormListAdapter by lazy { FormListAdapter() }
    private lateinit var disposable : Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        Handzap.getApp(requireContext()).mainComponent?.injectFormListFragment(this)
        super.onCreate(savedInstanceState)
        configureViewModel()
    }

    private fun configureViewModel() {
        formsViewModel = ViewModelProviders.of(requireActivity(),viewModelFactory).get(FormsViewModel::class.java)
    }

    private fun attachFormListObserver() {
        formsViewModel.getForms().observe(this, Observer {
            if(it.isEmpty()) {
                formListAdapter.submitList(listOf())
                empty_string.visibility = View.VISIBLE
            }else {
                empty_string.visibility = View.INVISIBLE
                formListAdapter.submitList(it)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_form_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        init()
    }

    private fun init() {
        rv_forms.setHasFixedSize(true)
        rv_forms.adapter = formListAdapter
        attachFormListObserver()
        observeOptionsClick()
    }

    private fun observeOptionsClick() {
        disposable = formListAdapter.clickObserver
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    formsViewModel.setDeleteItemId(it.first)
                    findNavController().navigate(R.id.action_formListFragment_to_deleteFormBottomSheet)
                }, {
                    Log.e("Click_Observer_Error", it.toString())
                }
            )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_main,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.dispose()
    }
}