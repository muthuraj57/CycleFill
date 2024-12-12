/* $Id$ */
package com.muthuraj.cycle.fill.di

import me.tatarka.inject.annotations.Scope

/**
 * Created by Muthuraj on 07/12/24.
 */
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope