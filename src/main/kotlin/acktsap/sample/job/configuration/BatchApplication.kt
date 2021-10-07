/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package acktsap.sample.job.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
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
    @Bean
    fun beforeJob(jobBuilderFactory: JobBuilderFactory, stepBuilderFactory: StepBuilderFactory): Job {
        return jobBuilderFactory.get("beforeJob")
            // configuration
            .preventRestart()
            .validator { /* do something */ }

            .start(
                stepBuilderFactory.get("testStep")
                    .tasklet { _, _ ->
                        println("run testTasklet")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            // configuration
            preventRestart()
            validator { /* do something */ }

            steps {
                step("testStep") {
                    tasklet { _, _ ->
                        println("run testTasklet")
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
