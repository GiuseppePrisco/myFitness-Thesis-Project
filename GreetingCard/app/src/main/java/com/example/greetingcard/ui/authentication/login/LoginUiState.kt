package com.example.greetingcard.ui.authentication.login

data class LoginUiState(
    var email  :String = "",
    var password  :String = "",

    var emailError :Boolean = false,
    var passwordError : Boolean = false

)