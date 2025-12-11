package com.lutukai.simpletodoapp

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * An empty activity annotated with @AndroidEntryPoint for fragment testing with Hilt.
 * This activity is used by launchFragmentInHiltContainer to host test fragments.
 */
@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity()