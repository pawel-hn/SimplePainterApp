package pawel.hn.simplepainterapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_SimplePainterApp)
        setContentView(R.layout.activity_main)

        val appBarConfiguration = AppBarConfiguration.Builder(R.id.drawFragment).build()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}