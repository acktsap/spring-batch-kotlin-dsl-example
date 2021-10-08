package acktsap.sample.job.flowjob.flowtransition.bean

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.extensions.kotlindsl.configuration.BatchDsl
import org.springframework.batch.extensions.kotlindsl.configuration.EnableBatchDsl
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
        flow("testFlow") {
            step("testStep") {
                tasklet { _, _ ->
                    println("run testTasklet")
                    throw IllegalStateException().apply { stackTrace = arrayOf() }
                }
            }
        }
    }

    @Bean
    fun transitionStep(batch: BatchDsl): Step = batch {
        step("transitionStep") {
            tasklet { _, _ ->
                println("run transitionTasklet")
                throw IllegalStateException().apply { stackTrace = arrayOf() }
            }
        }
    }

    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
        @Qualifier("testFlow") testFlow: Flow,
        @Qualifier("transitionStep") transitionStep: Step,
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(testFlow)
            .on("COMPLETED").to(transitionStep)
            .from(testFlow).on("TEST").fail()
            .on("*").stop()
            .end()
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            flows {
                flowBean("testFlow") {
                    on("COMPLETED") {
                        stepBean("transitionStep")
                    }
                    on("TEST") {
                        fail()
                    }
                    on("*") {
                        stop()
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
