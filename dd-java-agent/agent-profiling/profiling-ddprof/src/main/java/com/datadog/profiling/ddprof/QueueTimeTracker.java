package com.datadog.profiling.ddprof;

import datadog.trace.api.profiling.QueueTiming;
import java.lang.ref.WeakReference;

public class QueueTimeTracker implements QueueTiming {

  private final DatadogProfiler profiler;
  private final Thread origin;
  private final long startTicks;
  private WeakReference<Object> weakTask;
  // FIXME this can be eliminated by altering the instrumentation
  //  since it is known when the item is polled from the queue
  private Class<?> scheduler;

  public QueueTimeTracker(DatadogProfiler profiler, long startTicks) {
    this.profiler = profiler;
    this.origin = Thread.currentThread();
    // TODO get this from JFR if available instead of making a JNI call
    this.startTicks = startTicks;
  }

  @Override
  public void setTask(Object task) {
    this.weakTask = new WeakReference<>(task);
  }

  @Override
  public void setScheduler(Class<?> scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public void close() {
    assert weakTask != null && scheduler != null;
    Object task = this.weakTask.get();
    if (task != null) {
      // indirection reduces shallow size of the tracker instance
      profiler.recordQueueTimeEvent(startTicks, task, scheduler, origin);
    }
  }
}
