package ee.ttu.schedule

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.vadimstrukov.ttuschedule.R
import ee.ttu.schedule.fragment.ChangeScheduleFragment
import ee.ttu.schedule.fragment.ScheduleFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nextFragment(ScheduleFragment.newInstance(1))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
             R.id.nav_day-> nextFragment(ScheduleFragment.newInstance(1))
             R.id.nav_days-> nextFragment(ScheduleFragment.newInstance(2))
             R.id.nav_refresh-> nextFragment(ChangeScheduleFragment())
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun nextFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
        }.commit()
    }
}