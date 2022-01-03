package com.dialogs

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.Accaunt_helper.AccauntHelper
import com.tony_fire.descorderkotlin.MainActivity
import com.tony_fire.descorderkotlin.R
import com.tony_fire.descorderkotlin.databinding.SignDialogBinding

class Dialog_helper( val act:MainActivity) {
     val accaunthelper = AccauntHelper(act)
    fun createSignDialog(index: Int) {
        val dialogBuilder = AlertDialog.Builder(act)
        val binding = SignDialogBinding.inflate(act.layoutInflater)
        val view = binding.root
        dialogBuilder.setView(view)
        setDialogState(index, binding)
        val dialog = dialogBuilder.create()
        binding.bSignInUp.setOnClickListener {
            setOnClickSignUpIn(index, binding, dialog)
        }
        binding.bForgetPass.setOnClickListener {
            setOnCLickForgetPass(binding, dialog)
        }
        binding.bGoogleSign.setOnClickListener {
            accaunthelper.signInWithGoogle()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setOnCLickForgetPass(rootDialogElement: SignDialogBinding, dialog: AlertDialog?) {
        if (rootDialogElement.tvEmail.text.isNotEmpty()) {
            act.mAuth.sendPasswordResetEmail(rootDialogElement.tvEmail.text.toString()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(act, "СМС С ВООСТАНОВЛЕНИЕМ ОТПРАВЛЕНО", Toast.LENGTH_LONG).show()
                    dialog?.dismiss()
                }
            }
            dialog?.dismiss()
        } else {
            rootDialogElement.tvForgetpasswordTitile.visibility = View.VISIBLE

        }


    }

    private fun setOnClickSignUpIn(index: Int, rootDialogElement: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()
        if(index == DialogConst.SIGN_UP_STATE){
            accaunthelper.SignUpWithEmail(rootDialogElement.tvEmail.text.toString(),
                    rootDialogElement.tvPassword.text.toString())
        }else{
            accaunthelper.SignINWithEmail(rootDialogElement.tvEmail.text.toString(),
                    rootDialogElement.tvPassword.text.toString())

        }

    }

    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) {
        if(index == DialogConst.SIGN_UP_STATE){
            rootDialogElement.tvSignTitle.text = act.resources.getString(R.string.sign_up_text)
            rootDialogElement.bSignInUp.text = act.resources.getString(R.string.sign_up_text1)

        }else{
            rootDialogElement.tvSignTitle.text = act.resources.getString(R.string.sign_in_text)
            rootDialogElement.bSignInUp.text = act.resources.getString(R.string.sign_in_text1)
            rootDialogElement.bForgetPass.visibility = View.VISIBLE

        }

    }

}



















