package com.Accaunt_helper

import android.util.Log
import android.widget.Toast
import com.utils.FirebaseAuthConstans
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.tony_fire.descorderkotlin.MainActivity
import com.tony_fire.descorderkotlin.R
import java.lang.Exception

class AccauntHelper(act: MainActivity) {
    private val act = act
    private lateinit var signInClient: GoogleSignInClient


    fun SignUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()
                ?.addOnCompleteListener { // Удаляем анонимного пользователя до того как регистрируемся
                        task ->
                    if (task.isSuccessful) {
                        act.mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    SignUpWithEmailSuccess(task.result?.user!!)

                                } else {

                                    SignUpWithEmailExceptions(task.exception!!, email, password)

                                }
                            }

                    } else {
                        Toast.makeText(act, "Smth Wrong", Toast.LENGTH_SHORT).show()
                    }

                }

        }


    }

    private fun SignUpWithEmailSuccess(user: FirebaseUser) { // Функция для успешного входа, чтобы разгрузить основную функцию
        SendEmailVerify(user)
        act.uiUpdate(user)
    }

    private fun SignUpWithEmailExceptions(
        e: Exception,
        email: String,
        password: String
    ) { // Функция отслеживает ошибки при регистрации, чтобы разгрузить основную функцию
        if (e is FirebaseAuthUserCollisionException) {
            val exception = e as FirebaseAuthUserCollisionException
            if (exception.errorCode == FirebaseAuthConstans.ERROR_EMAIL_ALREADY_IN_USE) {

                linkEmailtoG(email, password)

            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            val exception =
                e as FirebaseAuthInvalidCredentialsException
            if (exception.errorCode == FirebaseAuthConstans.ERROR_INVALID_EMAIL) {
                Toast.makeText(
                    act,
                    FirebaseAuthConstans.ERROR_INVALID_EMAIL,
                    Toast.LENGTH_LONG
                ).show()

            } else if (e is FirebaseAuthWeakPasswordException) {
                val exception =
                    e as FirebaseAuthWeakPasswordException
                if (exception.errorCode == FirebaseAuthConstans.ERROR_INVALID_EMAIL) {
                    Toast.makeText(
                        act,
                        FirebaseAuthConstans.ERROR_WEAK_PASSWORD,
                        Toast.LENGTH_LONG
                    ).show()


                }
            }

        }
    }

    private fun SigninWithEmailExceptions(e: Exception, email: String, password: String) {
        Log.d("MyLog", "Exeption :${e}")
        //Toast.makeText(act, act.resources.getString(R.string.error2),Toast.LENGTH_LONG).show()
        if (e is FirebaseAuthInvalidCredentialsException) {
            val exception = e as FirebaseAuthInvalidCredentialsException
            if (exception.errorCode == FirebaseAuthConstans.ERROR_WRONG_PASSWORD) {
                Toast.makeText(
                    act,
                    FirebaseAuthConstans.ERROR_WRONG_PASSWORD,
                    Toast.LENGTH_LONG
                ).show()

            } else if (e is FirebaseAuthInvalidCredentialsException) {
                val exception =
                    e as FirebaseAuthInvalidCredentialsException
                if (exception.errorCode == FirebaseAuthConstans.ERROR_INVALID_EMAIL) {
                    Toast.makeText(
                        act,
                        FirebaseAuthConstans.ERROR_INVALID_EMAIL,
                        Toast.LENGTH_LONG
                    ).show()
                } else if (e is FirebaseAuthInvalidUserException) {
                    val exception = e as FirebaseAuthInvalidUserException
                    if (exception.errorCode == FirebaseAuthConstans.ERROR_USER_NOT_FOUND) {
                        Toast.makeText(
                            act,
                            FirebaseAuthConstans.ERROR_USER_NOT_FOUND,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }
        }

    }

    fun SignINWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                act.uiUpdate(task.result?.user)
                                Toast.makeText(
                                    act,
                                    act.resources.getString(R.string.sucess_login),
                                    Toast.LENGTH_LONG
                                ).show()

                            } else {

                                SigninWithEmailExceptions(task.exception!!, email, password)
                            }
                        }
                } else {
                    Toast.makeText(act, "Smth Wrong", Toast.LENGTH_SHORT).show()
                }
            }

        }


    }

    private fun SendEmailVerify(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.email_veify),
                    Toast.LENGTH_LONG
                ).show()

            } else {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.email_veify_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun linkEmailtoG(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (act.mAuth.currentUser != null) {
            act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        act,
                        act.resources.getString(R.string.link_done),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


        } else {
            Toast.makeText(act, act.resources.getString(R.string.same_accaunt), Toast.LENGTH_LONG)
                .show()
        }

    }


    private fun getSigninGoogleClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(act, gso)

    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.mAuth.currentUser?.delete()
            ?.addOnCompleteListener { task ->  // Удаляем анонимного пользователя, когда уже входим в аккаунт
                if (task.isSuccessful) {
                    act.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(act, "signInDone", Toast.LENGTH_LONG).show()
                            act.uiUpdate(task.result?.user)
                        } else {
                            Log.d("MyLog", "GoogleSignExeption :${task.exception}")
                        }
                    }
                }
            }


    }

    fun signInWithGoogle() {
        signInClient = getSigninGoogleClient()
        val intent = signInClient.signInIntent
        act.googleSignLauncher.launch(intent)

    }

    fun signOutWithGoogle() {
        signInClient = getSigninGoogleClient()
        signInClient.signOut()


    }

    fun signInAnonimus(listener: AccountListener) { // Регистрация анонимного пользователя, передаем наш listener
        act.mAuth.signInAnonymously()
            .addOnCompleteListener { // Происходит регистрация, также добавляется слушатель для успешности проверки
                    task ->
                if (task.isSuccessful) {
                    listener.onComplete()
                    Toast.makeText(act, "Вы вошли как аноним", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(act, "Ошибка", Toast.LENGTH_SHORT).show()
                }
            }

    }

    interface AccountListener { // Интерфейс который будет следить за тем когда регистрация как анонимный пользователь будет завершена
        fun onComplete()
    }


}