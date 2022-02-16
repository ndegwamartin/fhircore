package org.smartregister.fhircore.engine.workflow

/**
 * A workflow point represents a feature in FHIRCore that can be started and stopped. For example
 * this can be launching an activity or fragment, starting a background service, executing a
 * background task etc.
 */
interface WorkflowPoint {

  /**
   * A default function for starting the next workflow. Override this method to provide
   * functionality for starting the next workflow e.g. launching an activity or starting a
   * background task.
   */
  fun executeNextWorkflow() {}

  /**
   * A default function for executing the previous workflow. Override this method to provide
   * functionality for executing the previous workflow e.g. navigating to the previous activity.
   */
  fun executePreviousWorkflow() {}
}
