package com.fastkantin.finalproject.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.databinding.FragmentSearchBinding
import com.fastkantin.finalproject.ui.menu.MenuAdapter
import com.fastkantin.finalproject.ui.menu.MenuDetailActivity
import com.fastkantin.finalproject.viewmodel.MenuViewModel

class SearchFragment : Fragment() {

    companion object {
        private const val TAG = "SearchFragment"
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuViewModel: MenuViewModel
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var categoryAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupViewModel()
            setupRecyclerView()
            setupSearchView()
            setupCategorySpinner()
            observeViewModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error loading search page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        try {
            menuViewModel = ViewModelProvider(this)[MenuViewModel::class.java]
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up ViewModel", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            menuAdapter = MenuAdapter { menu ->
                // Handle menu click - navigate to detail dengan null safety
                try {
                    Log.d(TAG, "Menu clicked in search: ID=${menu.menu_id}, Name=${menu.name}")

                    if (menu.menu_id > 0 && context != null && isAdded) {
                        val intent = Intent(requireContext(), MenuDetailActivity::class.java).apply {
                            putExtra("menu_id", menu.menu_id)
                            putExtra("menu", menu) // Pass entire menu object
                        }
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "Invalid menu data or context: menuId=${menu.menu_id}, context=${context}, isAdded=$isAdded")
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Error: Data menu tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling menu click in search", e)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error membuka detail menu", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.rvSearchResults.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = menuAdapter
                setHasFixedSize(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupSearchView() {
        try {
            binding.etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    try {
                        val query = s.toString().trim()
                        Log.d(TAG, "Search query: $query")
                        menuViewModel.searchMenu(query)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in search text watcher", e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up search view", e)
        }
    }

    private fun setupCategorySpinner() {
        try {
            categoryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                mutableListOf<String>()
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            binding.spinnerCategory.adapter = categoryAdapter

            binding.spinnerCategory.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    try {
                        val selectedCategory = categoryAdapter.getItem(position) ?: "Semua"
                        Log.d(TAG, "Category selected: $selectedCategory")
                        menuViewModel.getMenuByCategory(selectedCategory)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in category selection", e)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up category spinner", e)
        }
    }

    private fun observeViewModel() {
        try {
            menuViewModel.searchResults.observe(viewLifecycleOwner) { menus ->
                try {
                    Log.d(TAG, "Search results received: ${menus?.size ?: 0} menus")

                    if (!menus.isNullOrEmpty()) {
                        menuAdapter.submitList(menus)
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvSearchResults.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvSearchResults.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating search results", e)
                }
            }

            menuViewModel.categories.observe(viewLifecycleOwner) { categories ->
                try {
                    Log.d(TAG, "Categories received: ${categories?.size ?: 0}")

                    if (!categories.isNullOrEmpty()) {
                        categoryAdapter.clear()
                        categoryAdapter.addAll(categories)
                        categoryAdapter.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating categories", e)
                }
            }

            menuViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                try {
                    binding.progressBar.visibility = if (isLoading == true) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating loading state", e)
                }
            }

            menuViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
                try {
                    if (!message.isNullOrEmpty() && isAdded) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing error message", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
