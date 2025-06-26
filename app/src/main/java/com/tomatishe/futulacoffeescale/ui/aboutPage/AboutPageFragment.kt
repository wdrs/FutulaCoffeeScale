package com.tomatishe.futulacoffeescale.ui.aboutPage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tomatishe.futulacoffeescale.databinding.FragmentAboutPageBinding

class AboutPageFragment : Fragment() {

    private var _binding: FragmentAboutPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var appVersionText: TextView
    private lateinit var appRepoLink: TextView
    private lateinit var appIdeaLink: TextView
    private lateinit var appAuthorLink: TextView
    private lateinit var appRepoLayout: ConstraintLayout
    private lateinit var appIdeaLayout: ConstraintLayout
    private lateinit var appAuthorLayout: ConstraintLayout

    data class AppVersion(
        val versionName: String,
        val versionNumber: Long,
    )

    data class gitReleaseInfo(val assets_url: String, val tag_name: String)

    private fun getAppVersion(
        context: Context,
    ): AppVersion? {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
            AppVersion(
                versionName = packageInfo.versionName.toString(),
                versionNumber = PackageInfoCompat.getLongVersionCode(packageInfo),
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun newInstance() = AboutPageFragment()
    }

    private val viewModel: AboutPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutPageBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val currentAppVersion: String =
            (getAppVersion(requireContext())?.versionName ?: "0.0.0").toString()

        appRepoLayout = binding.appRepoLayout
        appIdeaLayout = binding.appIdeaLayout
        appAuthorLayout = binding.appAuthorLayout

        appRepoLink = binding.appRepoLink
        appIdeaLink = binding.appIdeaLink
        appAuthorLink = binding.appAuthorLink

        appVersionText = binding.appVersionText

        Handler(Looper.getMainLooper()).post {
            appVersionText.text = currentAppVersion
        }

        appRepoLayout.setOnClickListener {
            val appRepoUrl = appRepoLink.text.toString()
            val appRepoUrlIntent = Intent(
                Intent.ACTION_VIEW,
                appRepoUrl.toUri()
            )
            startActivity(appRepoUrlIntent)
        }

        appIdeaLayout.setOnClickListener {
            val appIdeaUrl = appIdeaLink.text.toString()
            val appIdeaUrlIntent = Intent(
                Intent.ACTION_VIEW,
                appIdeaUrl.toUri()
            )
            startActivity(appIdeaUrlIntent)
        }

        appAuthorLayout.setOnClickListener {
            val appAuthorUrl = appAuthorLink.text.toString()
            val appAuthorUrlIntent = Intent(
                Intent.ACTION_VIEW,
                appAuthorUrl.toUri()
            )
            startActivity(appAuthorUrlIntent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}