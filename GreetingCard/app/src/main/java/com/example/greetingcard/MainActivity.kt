package com.example.greetingcard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.greetingcard.ui.theme.GreetingCardTheme
import java.io.File


class MainActivity : ComponentActivity(), ActivityCompat.OnRequestPermissionsResultCallback {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GreetingCardTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MyApp(
                        //startDestination = MyAppScreen.Home.name,
                        //createAccount = { ("pippo").createAccount("pippo", "pluto") },
                        //firebaseAuth = auth
                    )
                    /*if(checkCurrentUser() == true) {
                        MyApp(
                            startDestination = MyAppScreen.Home.name,
                            //createAccount = { ("pippo").createAccount("pippo", "pluto") },
                            //firebaseAuth = auth
                        )
                    }
                    else {
                        MyApp(
                            startDestination = MyAppScreen.Login.name,
                            //firebaseAuth = auth
                        )
                    }*/
                }
            }
        }


        if (!allRuntimePermissionsGranted()) {
            getRuntimePermissions()
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        //clearApplicationData()
    }

    private fun clearApplicationData() {
        val cache = cacheDir
        val appDir = File(cache.parent)
        //deleteDir(cache)

        if (appDir.exists()) {
            val children = appDir.list()
            for (s in children) {
                //if (s != "lib" && s != "databases") {
                if (s == "cache" || s == "code_cache") {
                    deleteDir(File(appDir, s))
                    Log.i(
                        "TAG",
                        "**************** File /data/data/APP_PACKAGE/$s DELETED *******************"
                    )
                }
            }
        }

    }

    private fun deleteDir(dir: File): Boolean {
        if (dir != null && dir.isDirectory) {
            //Log.d("","DELETING ${dir}")
            val children = dir.list()
            //Log.d("","CHILDREN ${children}")
            for (i in 0 until children.size) {
                //Log.d("","DELETING ${children[i]}")
                val success = deleteDir(File(dir, children[i]))
                /*Log.i(
                    "TAG",
                    "**************** File /data/data/APP_PACKAGE/${dir}/${children[i]} DELETED *******************"
                )*/
                if (!success) {
                    return false
                }
            }
        }
        return dir.delete()
    }


    private fun allRuntimePermissionsGranted(): Boolean {
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUESTS
            )
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission granted: $permission")
            return true
        }
        Log.i(TAG, "Permission NOT granted: $permission")
        return false
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUESTS = 1

        private val REQUIRED_RUNTIME_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
    }


}
