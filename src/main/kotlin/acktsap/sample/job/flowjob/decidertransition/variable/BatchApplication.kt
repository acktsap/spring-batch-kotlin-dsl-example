package acktsap.sample.job.flowjob.decidertransition.variable

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
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
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
    ): Job {
        val testDecider = JobExecutionDecider { _, _ ->
            println("run testDecider")
            FlowExecutionStatus.FAILED
        }
        val transitionStep = stepBuilderFactory.get("transitionStep")
            .tasklet { _, _ ->
                println("run transitionTasklet")
                RepeatStatus.FINISHED
            }
            .build()
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory
                    .get("testStep")
                    .tasklet { _, _ -> RepeatStatus.FINISHED }
                    .build()
            )
            .next(testDecider)
            .on("COMPLETED").end()
            .from(testDecider).on("FAILED").to(transitionStep)
            .on("*").stop()
            .end()
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        val testDecider = JobExecutionDecider { _, _ ->
            println("run testDecider")
            FlowExecutionStatus.FAILED
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    println("run transitionTasklet")
                    RepeatStatus.FINISHED
                }
            }
        }
        job("afterJob") {
            flows {
                step("testStep") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
                decider(testDecider) {
                    on("COMPLETED") {
                        end()
                    }
                    on("FAILED") {
                        step(transitionStep)
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
