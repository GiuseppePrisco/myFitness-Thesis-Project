package com.example.greetingcard.ui.authentication.signup

sealed class SignupUiEvent{

    data class FirstNameChanged(val firstName:String) : SignupUiEvent()
    data class LastNameChanged(val lastName:String) : SignupUiEvent()
    data class EmailChanged(val email:String): SignupUiEvent()
    data class PasswordChanged(val password: String) : SignupUiEvent()

    object RegisterButtonClicked : SignupUiEvent()
}