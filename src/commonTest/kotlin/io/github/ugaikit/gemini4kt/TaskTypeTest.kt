package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

class TaskTypeTest {
    @Test
    fun taskTypeContainsExpectedEntries() {
        assertEquals(TaskType.TASK_TYPE_UNSPECIFIED, TaskType.entries.first())
        assertEquals(TaskType.CLUSTERING, TaskType.entries.last())
        assertEquals(6, TaskType.entries.size)
    }
}
