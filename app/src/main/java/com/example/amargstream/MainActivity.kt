package com.example.amargstream

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.amargstream.adapters.CategoryAdapter
import com.example.amargstream.adapters.SectionSongListAdapter
import com.example.amargstream.databinding.ActivityMainBinding
import com.example.amargstream.models.CategoryModel
import com.example.amargstream.models.SongModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCategories()
        setUpSection("section_1", binding.section1MainLayout, binding.section1Title, binding.section1RecyclerView)
        setUpSection("section_2", binding.section2MainLayout, binding.section2Title, binding.section2RecyclerView)
        setUpSection("section_3", binding.section3MainLayout, binding.section3Title, binding.section3RecyclerView)
        setUpMostlyPlayed("mostly_played", binding.mostlyPlayedMainLayout, binding.mostlyPlayedTitle, binding.mostlyPlayedRecyclerView)

        binding.optionBtn.setOnClickListener {
            showPopUpMenu()
        }
    }

    private fun showPopUpMenu() {
        val popupMenu = PopupMenu(this, binding.optionBtn)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.option_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    logout()
                    true
                }
            }
            false
        }
    }

    private fun logout() {
        MyExoPlayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, SignInActivity :: class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        showPlayerView()
    }

    private fun showPlayerView() {
        binding.playerView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity :: class.java))
        }
        MyExoPlayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = it.title
            Glide.with(binding.songCoverImageView)
                .load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                )
                .into(binding.songCoverImageView)
        } ?: kotlin.run {
            binding.playerView.visibility = View.INVISIBLE
        }
    }

    //categories
    private fun getCategories() {
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel :: class.java)
                setUpCategoryRecyclerView(categoryList)
            }
    }

    private fun setUpCategoryRecyclerView(categoryList: List<CategoryModel>) {
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    //sections
    private fun setUpSection(id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel :: class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.adapter = SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener {
                        SongsListActivity.category = section
                        startActivity(Intent(this@MainActivity, SongsListActivity :: class.java))
                    }
                }
            }
    }

    private fun setUpMostlyPlayed(id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                // get most played
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count", Query.Direction.DESCENDING)
                    .limit(3)
                    .get().addOnSuccessListener { songListSnapShot ->
                        val songsModelList = songListSnapShot.toObjects<SongModel>()
                        val songsIdList = songsModelList.map {
                            it.id
                        }.toList()

                        val section = it.toObject(CategoryModel :: class.java)
                        section?.apply {
                            section.songs = songsIdList
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                            recyclerView.adapter = SectionSongListAdapter(songs)
                            mainLayout.setOnClickListener {
                                SongsListActivity.category = section
                                startActivity(Intent(this@MainActivity, SongsListActivity :: class.java))
                            }
                        }
                    }
            }
    }
}