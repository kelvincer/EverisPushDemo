package com.home.everispushdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        get_token.setOnClickListener {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    Log.d(TAG, "token: $token")
                    Toast.makeText(baseContext, token, Toast.LENGTH_LONG).show()
                })
        }

        btn_clear.setOnClickListener {
            data_value.text = ""
            title_value.text = ""
            body_value.text = ""
            intent_value.text = ""
        }

        checkBoxSports.isChecked = requestSubscription("Sports");

        checkBoxSports.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                subscribeTheme("Sports", isChecked)
            })

        intent_value.text = intent.getStringExtra("value")
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(messageReceiver, IntentFilter("Message-Push-Event"));
        registerReceiver(dataMessageReceiver, IntentFilter("Data-Push-Event"))
    }

    override fun onPause() {
        super.onPause()
        //unregisterReceiver(messageReceiver)
        //unregisterReceiver(dataMessageReceiver)
    }

    private val dataMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            data_value.text = intent.getStringExtra("value")
        }
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            title_value.text = intent.getStringExtra("title")
            body_value.text = intent.getStringExtra("body")
        }
    }

    private fun subscribeTheme(theme: String, subscribe: Boolean) {
        if (subscribe) {
            FirebaseMessaging.getInstance().subscribeToTopic(theme).addOnCompleteListener {
                var msg = "Success Subscription"
                if (!it.isSuccessful) {
                    msg = "Failed Subscription"
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                saveSubscription(theme, subscribe)
            }
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(theme).addOnCompleteListener {
                var msg = "Success UnSubscription"
                if (!it.isSuccessful) {
                    msg = "Failed UnSubscription"
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                saveSubscription(theme, subscribe)
            }
        }
    }

    private fun saveSubscription(theme: String, subscribe: Boolean) {
        val prefs = applicationContext.getSharedPreferences("Themes", Context.MODE_PRIVATE);
        val editor = prefs.edit();
        editor.putBoolean(theme, subscribe);
        editor.apply();
    }

    private fun requestSubscription(theme: String): Boolean {
        val prefs = applicationContext.getSharedPreferences("Themes", Context.MODE_PRIVATE)
        return prefs.getBoolean(theme, false)
    }
}