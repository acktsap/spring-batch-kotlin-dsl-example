package acktsap.sample.step.simplestep.chunksize

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.extensions.kotlindsl.configuration.BatchDsl
import org.springframework.batch.extensions.kotlindsl.configuration.EnableBatchDsl
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
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
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .chunk<Int, Int>(3)
                    .reader(
                        object : ItemReader<Int> {
                            private var count = 0

                            override fun read(): Int? {
                                return if (count < 5) {
                                    count++
                                } else {
                                    null
                                }
                            }
                        }
                    )
                    .processor(ItemProcessor { item -> item })
                    .writer {
                        println("write $it")
                    }
                    .faultTolerant()
                    .retry(RuntimeException::class.java)
                    .retryLimit(2)
                    .exceptionHandler { _, _ ->
                        // do something
                    }
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                step("testStep") {
                    chunk<Int, Int>(3) {
                        reader(
                            object : ItemReader<Int> {
                                private var count = 0

                                override fun read(): Int? {
                                    return if (count < 5) {
                                        count++
                                    } else {
                                        null
                                    }
                                }
                            }
                        )
                        processor { item -> item }
                        writer {
                            println("write $it")
                        }
                        exceptionHandler { _, _ ->
                            // do something
                        }
                        faultTolerant {
                            retry(RuntimeException::class)
                            retryLimit(2)
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
