package com.example.greetingcard.ui.authentication

object Validator {


    fun validateFirstName(fName: String): ValidationResult {
        return ValidationResult(
            (!fName.isNullOrEmpty() && fName.length >= 2)
        )

    }

    fun validateLastName(lName: String): ValidationResult {
        return ValidationResult(
            (!lName.isNullOrEmpty() && lName.length >= 2)
        )
    }

    fun validateEmail(email: String): ValidationResult {
        return ValidationResult(
            (!email.isNullOrEmpty() && isValidEmail(email))
        )
    }

    fun isValidEmail(email: String): Boolean {
        //val emailRegex = "^[A-Za-z0-9+_.-]@[A-Za-z0-9-]+.[A-Za-z0-9-]+\$"
        //return email.matches(emailRegex.toRegex())
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    fun validatePassword(password: String): ValidationResult {
        return ValidationResult(
            (!password.isNullOrEmpty() && password.length >= 6)
        )
    }

}

data class ValidationResult(
    val status: Boolean = false
)