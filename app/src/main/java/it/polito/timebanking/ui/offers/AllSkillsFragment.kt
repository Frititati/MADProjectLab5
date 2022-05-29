package it.polito.timebanking.ui.offers

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentSkillListBinding
import it.polito.timebanking.model.skill.SkillData
import it.polito.timebanking.model.skill.SkillViewModel

class AllSkillsFragment : Fragment() {

    private var _binding: FragmentSkillListBinding? = null
    private val binding get() = _binding!!
    private var skillListAdapter = AllSkillsAdapter()
    private val vm by viewModels<SkillViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        _binding = FragmentSkillListBinding.inflate(inflater, container, false)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val dialog = AlertDialog.Builder(context)
                val dialogView = layoutInflater.inflate(R.layout.dialog_generic, null)
                dialog.setTitle("Are you sure you want to quit?")
                dialog.setView(dialogView)
                dialog.setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }

                dialog.setNegativeButton("NO") { _, _ ->
                }
                dialog.create().show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.skillListRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.skillListRecycler.adapter = skillListAdapter
        vm.get().observe(viewLifecycleOwner) {
            skillListAdapter.setSkills(it as MutableList<Pair<String, SkillData>>)
            binding.nothingToShow.isVisible = it.isEmpty()
            binding.nothingToShow.text = resources.getString(R.string.no_skills)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}