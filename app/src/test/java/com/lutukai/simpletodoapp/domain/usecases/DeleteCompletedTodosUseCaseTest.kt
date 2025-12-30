package com.lutukai.simpletodoapp.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.lutukai.simpletodoapp.domain.repository.TodoRepository
import com.lutukai.simpletodoapp.util.Result
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
    fun `invoke returns Success with count of deleted todos`() = runTest {
        coEvery { repository.deleteCompletedTodos() } returns Result.Success(5)

        val result = useCase()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(5)
        coVerify { repository.deleteCompletedTodos() }
    }

    @Test
    fun `invoke returns Success with zero when no completed todos exist`() = runTest {
        coEvery { repository.deleteCompletedTodos() } returns Result.Success(0)

        val result = useCase()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(0)
    }

    @Test
    fun `invoke returns Failure from repository`() = runTest {
        val error = RuntimeException("Delete failed")
        coEvery { repository.deleteCompletedTodos() } returns Result.Failure(error)

        val result = useCase()

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        assertThat((result as Result.Failure).message).isEqualTo("Delete failed")
    }
}
