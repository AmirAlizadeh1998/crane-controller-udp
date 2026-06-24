package com.tivanco.hymaco.activityAndApplication

//import android.os.Bundle
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.tivanco.hymaco.activityAndApplication.ui.theme.TestApplicationTheme
//import com.tivanco.hymaco.appUI.mapScreen.MapScreen
//import com.tivanco.hymaco.utils.SharedRepository
//
//class MapActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            TestApplicationTheme {
//                val craneLocation by SharedRepository.locationState.collectAsStateWithLifecycle()
//
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    MapScreen(
//                        modifier = Modifier.padding(innerPadding),
//                        lat = craneLocation.lat,
//                        lng = craneLocation.lng,
//                        alt = craneLocation.alt,
//                        speed = craneLocation.speed
//                    )
//                }
//            }
//        }
//    }
//}