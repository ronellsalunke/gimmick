package at.sphericalk.gidget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import at.sphericalk.gidget.utils.Constants

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authIntent = Intent(Intent.ACTION_VIEW, Constants.OAUTH_URL.toUri())
        startActivity(authIntent)
        return
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val uri = intent.data
        val returnIntent = Intent().apply { data = intent.data }

        if (uri.toString().startsWith(Constants.REDIRECT_URI)) {
            setResult(Activity.RESULT_OK, returnIntent)
        } else {
            setResult(Activity.RESULT_CANCELED, returnIntent)
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
