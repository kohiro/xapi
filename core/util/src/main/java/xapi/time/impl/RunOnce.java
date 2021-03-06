package xapi.time.impl;

import static xapi.time.X_Time.threadStart;
import xapi.time.api.Moment;
import xapi.util.api.ReceivesValue;

public class RunOnce {

  private static final Runnable NO_OP = new Runnable() {
    @Override
    public void run() {}
  };

  @SuppressWarnings("rawtypes")
  private static final ReceivesValue NO_OP_RECEIVER = new ReceivesValue.NoOp();

  public static Runnable runOnce(final Runnable job) {
    return runOnce(job, false);
  }

  public static Runnable runOnce(final Runnable job, final boolean oncePerMoment) {
    if (oncePerMoment) {
      return new Runnable() {
        RunOnce lock = new RunOnce();
        @Override
        public void run() {
          if (lock.shouldRun(oncePerMoment)) {
            job.run();
          }
        }
      };
    } else {
      return new Runnable() {
        Runnable once = job;
        @Override
        public void run() {
          once.run();
          once = NO_OP;
        }
      };
    }
  }

  public static <X> ReceivesValue<X> setOnce(final ReceivesValue<X> job) {
    return setOnce(job, false);
  }

  public static <X> ReceivesValue<X> setOnce(final ReceivesValue<X> job, final boolean oncePerMoment) {
    if (oncePerMoment) {
      return new ReceivesValue<X>() {
        RunOnce lock = new RunOnce();
        @Override
        public void set(X from) {
          if (lock.shouldRun(oncePerMoment)) {
            job.set(from);
          }
        }
      };
    } else {
      return new ReceivesValue<X>() {
        ReceivesValue<X> once = job;
        @Override
        @SuppressWarnings("unchecked")
        public void set(X from) {
          once.set(from);
          once = NO_OP_RECEIVER;
        }
      };
    }
  }

  private Moment once;

  public boolean shouldRun(boolean oncePerMoment) {
    if (once == null) {
      synchronized (this) {
        if (once != null) {
          return false;
        }
        once = threadStart();
        return true;
      }
    } else {
      if (oncePerMoment) {
        //do not run more than once per tick of X_Time.
        //this allows you to call this method as many times as you want,
        //but only perform the heavyweight operation of synchronization once.
        Moment now = threadStart();
        synchronized (this) {
          if (once.equals(now)){
            return false;
          }
          once = now;
        }
        return true;
      } else {
        return false;
      }
    }
  }

}
