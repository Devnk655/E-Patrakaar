package com.example.e_patrakaar.view.fragment.main

import android.app.ProgressDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.e_patrakaar.R
import com.example.e_patrakaar.databinding.CustomBottomSheetBinding
import com.example.e_patrakaar.databinding.FragmentProfileBinding
import com.example.e_patrakaar.model.Collection
import com.example.e_patrakaar.view.OnItemClickListener
import com.example.e_patrakaar.view.WrapContentStaggeredGridLayoutManager
import com.example.e_patrakaar.view.activity.MainActivity
import com.example.e_patrakaar.view.adapter.CollectionAdapter
import com.example.e_patrakaar.view.adapter.LatestTechAdapter
import com.example.e_patrakaar.viewmodel.RandomNewsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProfileFragment : Fragment(), OnItemClickListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var list: List<Collection>
    private var savedNews = ArrayList<Collection>()
    private lateinit var randomNewsViewModel: RandomNewsViewModel
    private lateinit var adapterSavedNews: LatestTechAdapter
    private lateinit var progressBar: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedNews = ArrayList()
        randomNewsViewModel = ViewModelProvider(this)[RandomNewsViewModel::class.java]
        randomNewsViewModel.getNewsFromAPI()

        Glide.with(requireActivity()).load(R.drawable.pic).circleCrop().into(binding.ivImage)

        list = listOf(
            Collection("Politics", "150 saved posts", R.drawable.protwo),
            Collection("Sports", "200 saved posts", R.drawable.proone),
            Collection("Animals and Birds", "4 saved posts", R.drawable.prothree),
            Collection("Videos", "23 saved posts", R.drawable.profour),
        )

        binding.rvSaved.layoutManager =
            WrapContentStaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        binding.rvSaved.adapter = CollectionAdapter(this@ProfileFragment, list, this)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.profile_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.hamburger -> {
                        showBottomSheetDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        (activity as MainActivity).supportActionBar!!.title = binding.tvName.text

        binding.btnEditInfo.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_navigation_edit_profile)
        }
    }

    //Changes for database
    private fun randomNewsViewModelObserver() {
        randomNewsViewModel.randomNewsResponse.observe(
            viewLifecycleOwner
        ) {
            it?.let {
                val random = (0..100).random()
                if (it.articles.size >= random + 10) {
                    for (i in random..random + 10) {
                        val e = it.articles[i]
                        savedNews.add(Collection(e.title, e.description, e.urlToImage))
                        adapterSavedNews.setData(savedNews)
                    }
                } else {
                    for (i in random..random + 1) {
                        val e = it.articles[i]
                        savedNews.add(Collection(e.title, e.description, e.urlToImage))
                        adapterSavedNews.setData(savedNews)
                    }
                }
                progressBar.dismiss()
            }
        }

        randomNewsViewModel.randomNewsLoadingError.observe(
            viewLifecycleOwner
        ) {
            it?.let {
                progressBar.dismiss()
            }
        }

        randomNewsViewModel.loadRandomNews.observe(
            viewLifecycleOwner
        ) {
            it?.let {
                if (it) {
                    progressBar.show()
                } else {
                    progressBar.dismiss()
                }
            }
        }
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(requireActivity())
        val view = CustomBottomSheetBinding.inflate(layoutInflater)

        view.tvYourActivity.setOnClickListener {
            Toast.makeText(requireActivity(), "Your activity is accessed.", Toast.LENGTH_SHORT)
                .show()
            dialog.dismiss()
        }
        view.tvArchive.setOnClickListener {
            Toast.makeText(requireActivity(), "Archive is accessed.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        view.tvPolicy.setOnClickListener {
            Toast.makeText(requireActivity(), "Privacy Policy is accessed.", Toast.LENGTH_SHORT)
                .show()
            dialog.dismiss()
        }
        view.tvHelp.setOnClickListener {
            Toast.makeText(requireActivity(), "Help is accessed.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        view.tvTheme.setOnClickListener {
            Toast.makeText(requireActivity(), "Theme is accessed.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        view.tvSettings.setOnClickListener {
            Toast.makeText(requireActivity(), "Settings is accessed.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        view.tvLogout.setOnClickListener {
            Toast.makeText(requireActivity(), "Logout is requested.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.setContentView(view.root)
        dialog.show()
    }

    private fun newsDetails(news: Collection) {
        findNavController().navigate(
            ProfileFragmentDirections.actionNavigationProfileToNavigationExpandedNews(
                news
            )
        )
    }

    override fun onItemClick(news: Collection) {
        newsDetails(news)
    }

    override fun onItemClickReturnViewHolder(viewHolder: RecyclerView.ViewHolder) {
        if ((viewHolder as CollectionAdapter.ViewHolder).rvSavedNews.visibility == View.VISIBLE) {
            viewHolder.rvSavedNews.visibility = View.GONE
            return
        }
        viewHolder.rvSavedNews.visibility = View.INVISIBLE
        adapterSavedNews = LatestTechAdapter(this, savedNews, this)
        progressBar = ProgressDialog(requireActivity())
        progressBar.setMessage("Loading news..")
        progressBar.show()
        randomNewsViewModelObserver()
        viewHolder.rvSavedNews.layoutManager =
            WrapContentStaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        viewHolder.rvSavedNews.adapter = adapterSavedNews
        viewHolder.rvSavedNews.visibility = View.VISIBLE
        //adapterSavedNews.notifyItemChanged(viewHolder.adapterPosition)
        progressBar.dismiss()
    }

}