package com.lutukai.simpletodoapp.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteCompletedTodosUseCaseTest {

    private lateinit var repository: TodoRepository
    private lateinit var useCase: DeleteCompletedTodosUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteCompletedTodosUseCase(repository)
    }

    @Test
    fun `invoke returns count of deleted todos`() = runTest {
        coEvery { repository.deleteCompletedTodos() } returns 5

        val result = useCase()

        assertThat(result).isEqualTo(5)
        coVerify { repository.deleteCompletedTodos() }
    }

    @Test
    fun `invoke returns zero when no completed todos exist`() = runTest {
        coEvery { repository.deleteCompletedTodos() } returns 0

        val result = useCase()

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        val error = RuntimeException("Delete failed")
        coEvery { repository.deleteCompletedTodos() } throws error

        var thrownError: Throwable? = null
        try {
            useCase()
        } catch (e: Exception) {
            thrownError = e
        }

        assertThat(thrownError).isInstanceOf(RuntimeException::class.java)
        assertThat(thrownError?.message).isEqualTo("Delete failed")
    }
}
