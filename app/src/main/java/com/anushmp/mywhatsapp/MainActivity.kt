package com.anushmp.mywhatsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.anushmp.mywhatsapp.ui.chat.ChatScreen
import com.anushmp.mywhatsapp.ui.chat.ChatViewModel
import com.anushmp.mywhatsapp.ui.theme.MyWhatsappTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportMessages(it) } }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importMessages(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWhatsappTheme {
                ChatScreen(
                    viewModel = viewModel,
                    onExport = { exportLauncher.launch("mywhatsapp_backup.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json")) }
                )
            }
        }
    }
}
