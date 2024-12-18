package com.xcape.simplemmomod.data.smmo_tasks

import android.util.Log
import com.xcape.simplemmomod.common.Constants.APP_TAG
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.domain.smmo_tasks.AutoSMMOLogger
import kotlinx.coroutines.*
import javax.inject.Inject

class AutoSMMOLoggerImpl @Inject constructor(
    private val userRepository: UserRepository
) : AutoSMMOLogger {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isLoggingEnabled = true  // Flag para controlar se o log está habilitado

    init {
        // Iniciar a limpeza automática dos logs a cada X segundos
        iniciarLimpezaAutomatica()
    }

    override suspend fun log(message: String, tag: String, user: User?): User {
        if (!isLoggingEnabled) return user ?: User() // Não registra se o log estiver desabilitado

        var newUser = when(user) {
            null -> userRepository.getLoggedInUser()!!
            else -> user
        }

        Log.d(APP_TAG, message)
        newUser = newUser.copy(travelLog = "${newUser.travelLog}\n$message")
        userRepository.updateUser(user = newUser)

        return newUser
    }

    // Função para limpar os logs
    suspend fun clearLogs(user: User?) {
        var newUser = when(user) {
            null -> userRepository.getLoggedInUser()!!
            else -> user
        }

        newUser = newUser.copy(travelLog = "")
        userRepository.updateUser(user = newUser)

        Log.d(APP_TAG, "Logs limpos")
    }

    // Função para iniciar a limpeza automática de logs
    private fun iniciarLimpezaAutomatica() {
        coroutineScope.launch {
            while (true) {
                delay(60000)  // Intervalo de 60 segundos
                clearLogs(null)  // Limpa os logs

                // Desabilita o log temporariamente
                isLoggingEnabled = false
                Log.d(APP_TAG, "Logs limpos")

                // Aguarda 5 segundos antes de reabilitar o log
                delay(5000)
                // Reabilita o log
                isLoggingEnabled = true
            }
        }
    }
}
