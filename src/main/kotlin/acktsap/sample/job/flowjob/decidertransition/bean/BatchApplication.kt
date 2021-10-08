package acktsap.sample.job.flowjob.decidertransition.bean

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
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
    fun testDecider(batch: BatchDsl): JobExecutionDecider = JobExecutionDecider { _, _ ->
        println("run testDecider")
        FlowExecutionStatus.FAILED
    }

    @Bean
    fun transitionStep(batch: BatchDsl): Step = batch {
        step("transitionStep") {
            tasklet { _, _ ->
                println("run transitionTasklet")
                RepeatStatus.FINISHED
            }
        }
    }

    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
        @Qualifier("testDecider") testDecider: JobExecutionDecider,
        @Qualifier("transitionStep") transitionStep: Step,
    ): Job {
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
        job("afterJob") {
            flows {
                step("testStep") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
                deciderBean("testDecider") {
                    on("COMPLETED") {
                        end()
                    }
                    on("FAILED") {
                        stepBean("transitionStep")
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
