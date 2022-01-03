package com.dialogs

import android.app.Activity
import android.app.AlertDialog
import com.tony_fire.descorderkotlin.databinding.ProgressDialogLayoutBinding
import com.tony_fire.descorderkotlin.databinding.SignDialogBinding

object ProgressDialog {
    fun createProgressDialog(act:Activity): AlertDialog {
        val dialogBuilder = AlertDialog.Builder(act)
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        dialogBuilder.setView(view)
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}