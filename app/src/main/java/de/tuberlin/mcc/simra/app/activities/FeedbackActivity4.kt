package de.tuberlin.mcc.simra.app.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import de.tuberlin.mcc.simra.app.BuildConfig
import de.tuberlin.mcc.simra.app.R
import de.tuberlin.mcc.simra.app.databinding.ActivityFeedbackBinding

class FeedbackActivity4 : AppCompatActivity() {
    /**
     * Layout Binding.
     */
    var binding: ActivityFeedbackBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_feedback);
        binding = ActivityFeedbackBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        toolbar.subtitle = ""
        binding!!.toolbar.toolbarTitle.setText(R.string.title_activity_about_simra)
        binding!!.toolbar.backButton.setOnClickListener { _: View? -> finish() }
        val items = resources.getStringArray(R.array.ContactItems)
        binding!!.listView.adapter = ArrayAdapter(
            this@FeedbackActivity4,
            android.R.layout.simple_list_item_1, items
        )
        binding!!.listView.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                var intent: Intent? = null
                when (position) {
                    0 -> {
                        intent = Intent(this@FeedbackActivity4, WebActivity::class.java)
                        intent.putExtra("URL", getString(R.string.link_simra_Page))
                    }

                    1 -> {
                        intent = Intent(Intent.ACTION_SEND)
                        intent.type = "message/rfc822"
                        intent.putExtra(
                            Intent.EXTRA_EMAIL,
                            arrayOf(getString(R.string.feedbackReceiver))
                        )
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackHeader))
                        intent.putExtra(
                            Intent.EXTRA_TEXT,
                            getString(R.string.feedbackReceiver) + System.lineSeparator()
                                    + "App Version: " + BuildConfig.VERSION_CODE + System.lineSeparator() + "Android Version: "
                        )
                        try {
                            startActivity(Intent.createChooser(intent, "Send mail..."))
                        } catch (ex: ActivityNotFoundException) {
                            Toast.makeText(
                                this@FeedbackActivity4,
                                "There are no email clients installed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    2 -> {
                        intent = Intent(Intent.ACTION_VIEW)
                        //intent = new Intent(SocialMediaActivity.this, WebActivity.class);
                        intent.data = Uri.parse(getString(R.string.link_to_twitter))
                        startActivity(intent)
                    }

                    3 -> {
                        intent = Intent(Intent.ACTION_VIEW)
                        //intent = new Intent(SocialMediaActivity.this, WebActivity.class);
                        intent.data = Uri.parse(getString(R.string.link_to_instagram))
                        startActivity(intent)
                    }

                    else -> Toast.makeText(
                        this@FeedbackActivity4,
                        R.string.notReady,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                intent?.let { startActivity(it) }
            }
    }
}