package com.avito.android.runner.annotation.resolver

import java.lang.reflect.Method


object Annotations {

    /**
     * Get unique (by annotation class) list of runtime retained annotations from (class + method).filter(subset)
     * Method annotations have priority over class annotations.
     * For example, if you have class with method like that:
     *
     * @Annotation1
     * @Annotation2("class")
     * class ClassWithAnnotations {
     *
     *     @Annotation2("method")
     *     @Annotation3
     *     fun method() {}
     * }
     *
     * you will have result:
     *
     * listOf<Annotation>(
     *     Annotation2("method"), <- from method
     *     Annotation3,           <- from method
     *     Annotation1()          <- from class
     * )
     */
    fun getAnnotationsSubset(
        aClass: Class<*>,
        method: Method? = null,
        vararg subset: Class<out Annotation>
    ): List<Annotation> {
        val methodAnnotations = method
            ?.annotations
            ?.map {
                UniqueByClassCollectionItemWrapper<Annotation>(
                    it
                )
            }
            ?: emptyList()
        val classAnnotations = aClass
            .annotations
            .map {
                UniqueByClassCollectionItemWrapper<Annotation>(
                    it
                )
            }

        return methodAnnotations.toSet().plus(classAnnotations)
            .map { it.value }
            .filterBy(*subset)
    }

    private fun Collection<Annotation>.filterBy(vararg subset: Class<out Annotation>): List<Annotation> {
        return filter { methodAnnotation ->
            subset.any { methodAnnotation.annotationClass.java.isAssignableFrom(it) }
        }
    }
}

private class UniqueByClassCollectionItemWrapper<T : Any>(
    val value: T
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is UniqueByClassCollectionItemWrapper<*>) {
            return false
        }

        return value::class == other.value::class
    }

    override fun hashCode(): Int {
        return value::class.java.hashCode()
    }
}
