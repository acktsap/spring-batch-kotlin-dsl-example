package acktsap.sample.step.taskletstep.bean

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.extensions.kotlindsl.configuration.BatchDsl
import org.springframework.batch.extensions.kotlindsl.configuration.EnableBatchDsl
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
@EnableBatchDsl
class BatchApplication {
    // before
    // none

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                step("testStep") {
                    taskletBean("testTasklet")
                }
            }
        }
    }

    @Bean
    fun testTasklet(): Tasklet = Tasklet { _, _ ->
        println("run testTasklet")
        RepeatStatus.FINISHED
    }
}

fun main() {
    runApplication<BatchApplication>()
}
