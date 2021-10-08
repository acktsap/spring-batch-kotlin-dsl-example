package acktsap.sample.step.flowstep.variable

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
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
        val flowStep = stepBuilderFactory.get("flowStep")
            .tasklet { _, _ ->
                println("run flowStep")
                RepeatStatus.FINISHED
            }
            .build()

        return jobBuilderFactory.get("beforeJob")
            .start(
                FlowBuilder<Flow>("testFlow")
                    .start(flowStep)
                    .on("COMPLETED").fail()
                    .from(flowStep).on("*").stop()
                    .build()
            )
            .end()
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        val flowStep = batch {
            step("flowStep") {
                tasklet { _, _ ->
                    println("run flowStep")
                    RepeatStatus.FINISHED
                }
            }
        }

        job("afterJob") {
            steps {
                step("testStep") {
                    flow("testFlow") {
                        step(flowStep) {
                            on("COMPLETED") {
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
    }
}

fun main() {
    runApplication<BatchApplication>()
}
