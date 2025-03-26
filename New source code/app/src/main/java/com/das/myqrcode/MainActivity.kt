package com.das.myqrcode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.das.myqrcode.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)




//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home,
//                R.id.nav_gallery)
//        )
//        setupActionBarWithNavController(
//            findNavController(R.id.nav_host_fragment_activity_main),
//            appBarConfiguration
//        )
//        binding.navView.setupWithNavController(findNavController(R.id.nav_host_fragment_activity_main))

    }





}