package acktsap.sample.step.flowstep.bean

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.extensions.kotlindsl.configuration.BatchDsl
import org.springframework.batch.extensions.kotlindsl.configuration.EnableBatchDsl
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
@EnableBatchDsl
class BatchApplication {
    // common
    @Bean
    fun testFlow(batch: BatchDsl): Flow = batch {
        val innerStep = batch {
            step("innerStep") {
                tasklet { _, _ ->
                    println("run innerStep")
                    RepeatStatus.FINISHED
                }
            }
        }

        flow("testFlow") {
            step(innerStep) {
                on("COMPLETED") {
                    fail()
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
        @Qualifier("testFlow") testFlow: Flow,
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("flowStep")
                    .flow(testFlow)
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                step("innerStep") {
                    flowBean("testFlow")
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
