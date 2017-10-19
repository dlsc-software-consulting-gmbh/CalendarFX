/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.ical.iter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.google.ical.util.DTBuilder;
import com.google.ical.util.TimeUtils;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.Frequency;
import com.google.ical.values.RRule;
import com.google.ical.values.TimeValue;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;

import junit.framework.TestCase;

/**
 * simulate a large number of monkeys banging on a calendar.
 *
 * @author mikesamuel@gmail.com (Mike Samuel)
 */
public class MonkeyKeyboardTest extends TestCase {

  static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");

  static final Timer timer = new Timer();

  long seed;
  Random rnd;
  DateValue refDate;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    seed = null != rnd ? rnd.nextLong() : System.currentTimeMillis();
    System.out.println("RANDOM SEED " + seed + " : " + getName());
    rnd = new Random(seed);
    refDate = new DTBuilder(1900 + rnd.nextInt(200), 1, rnd.nextInt(366) + 1)
        .toDate();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  // @TODO: put back in, sometimes fails

//  public void testTimeGoesForward() throws Throwable {
//    for (int i = 0; i < 1000; ++i) {
//      System.err.print("<");
//      DumpStackTask task = dumpStackIfRunsTooLong();
//
//      boolean timed = rnd.nextBoolean();
//      RRule rrule = monkeySeeRRule(timed, false);
//      task.rrule = rrule;
//      boolean needsTime = rrule.getFreq().compareTo(Frequency.DAILY) < 0
//          || rrule.getByHour().length != 0
//          || rrule.getByMinute().length != 0
//          || rrule.getBySecond().length != 0;
//      DateValue dtStart = monkeySeeDateValue(needsTime || maybeNot(timed));
//      task.dtStart = task.dtStart;
//
//      RecurrenceIterator it;
//      try {
//        it = RecurrenceIteratorFactory.createRecurrenceIterator(
//            rrule, dtStart, PST);
//      } catch (Throwable th) {
//        // if we can't create it, don't worry.  This is only testing order of
//        // results.
//        task.cancel();
//        continue;
//      }
//
//      try {
//        int tries = 200;
//        DateValue last = null;
//        while (it.hasNext() && --tries >= 0) {
//          DateValue dv = it.next();
//          assertTrue("rule = " + rrule.toIcal() + ", last=" + last + ", current=" + dv,
//                     null == last || last.compareTo(dv) < 0);
//          last = dv;
//        }
//      } catch (Throwable th) {
//        task.cancel();
//        System.err.println("\n" + rrule.toIcal() + " / " + dtStart);
//        throw th;
//      }
//      task.cancel();
//      System.err.print(">");
//    }
//  }

  public void testNoExceptionsThrownOnWelformedRrules() throws Throwable {
    final int nRuns = 1000;
    final int countLimit = 2000;
    int totalGened = 0;  // total number of instances generated.
    Histogram histogram = new Histogram(nRuns);
    for (int i = 0; i < nRuns; ++i) {
      System.err.print("<");
      DumpStackTask task = dumpStackIfRunsTooLong();
      boolean timed = rnd.nextBoolean();
      RRule rrule = monkeySeeRRule(timed, true);
      task.rrule = rrule;
      DateValue dtStart = monkeySeeDateValue(maybeNot(timed));
      task.dtStart = dtStart;
      try {
        RecurrenceIterator it = RecurrenceIteratorFactory
            .createRecurrenceIterator(rrule, dtStart, PST);
        int tries = countLimit;
        long t0 = System.nanoTime();
        while (it.hasNext() && --tries >= 0) {
          it.next();
        }
        long dt = System.nanoTime() - t0;
        histogram.addSample(dt / 1000);  // nanoseconds -> microseconds
        totalGened += countLimit - tries;
      } catch (Throwable th) {
        task.cancel();
        System.err.println("\n" + rrule.toIcal() + " / " + dtStart);
        throw th;
      }
      task.cancel();
      System.err.print(">");
    }
    // to see this, run the test with --nooutputredirect
    histogram.dump(16, "microseconds");
    System.out.println("totalGenerated=" + totalGened);
  }

  /*
   * Do not test frequencies smaller than daily.
   */
  static final Frequency[] FREQS = new Frequency[]{Frequency.DAILY, Frequency.WEEKLY, Frequency.MONTHLY, Frequency.YEARLY};

  static final Weekday[] WDAYS = Weekday.values();

  /**
   * produce a random recurrence rule so that we can compare the behavior of two
   * implementations, and generate rules that might cause exceptions or slowness
   * in a single implementation.
   * @param timed should any date values have times associated with them.
   * @param wellFormed should the numeric values all be in the appropriate
   *   ranges, and other constraints specified in the spec hold?
   */
  RRule monkeySeeRRule(boolean timed, boolean wellFormed) {
    RRule rrule = new RRule();
    rrule.setName(rnd.nextInt(4) < 1 ? "EXRULE" : "RRULE");
    // pick a frequency
    // This is stretching the definition of well formed, but we don't do
    // more frequently than daily.
    // TODO: allow more frequently than DAILY. BEFORE SUBMIT
    Frequency freq = FREQS[rnd.nextInt(FREQS.length)];
    rrule.setFreq(freq);

    if (rnd.nextBoolean()) {
      // biased towards the beginning of the week
      rrule.setWkSt(WDAYS[((int) Math.abs(rnd.nextGaussian() / 3) * 7) % 7]);
    }

    if (rnd.nextBoolean()) {
      if (rnd.nextBoolean()) {
        rrule.setCount(1 + ((int) (Math.abs(rnd.nextGaussian() * 10))));
      } else {
        rrule.setUntil(monkeySeeDateValue(maybeNot(timed)));
      }
    }

    rrule.setInterval(
        (int) Math.min(0, Math.abs(rnd.nextGaussian() - .5) * 3) + 1);

    int sparsity = 2 + rnd.nextInt(6);

    boolean allowsByDay = freq.compareTo(Frequency.WEEKLY) >= 0;
    if (0 == rnd.nextInt(sparsity) && maybeNot(allowsByDay)) {
      rrule.setByDay(Arrays.asList(monkeySeeWeekdayNumList(freq)));
    }

    if (0 == rnd.nextInt(sparsity)) {
      rrule.setByMonth(monkeySeeIntArray(1, 12, 3, false));
    }

    if (0 == rnd.nextInt(sparsity)) {
      rrule.setByMonthDay(monkeySeeIntArray(-31, 31, 6, false));
    }

    boolean allowWeekNo = maybeNot(Frequency.MONTHLY.compareTo(freq) <= 0);
    if (allowWeekNo && 0 == rnd.nextInt(sparsity)) {
      boolean useYearly = Frequency.YEARLY == freq;
      if (!wellFormed) { useYearly = maybeNot(useYearly); }
      int mag = useYearly ? 52 : 5;
      rrule.setByWeekNo(monkeySeeIntArray(-mag, mag, 1, false));
    }

    if (0 == rnd.nextInt(sparsity)) {
      rrule.setByYearDay(monkeySeeIntArray(-366, 366, 6, false));
    }

    if (0 == rnd.nextInt(sparsity * 2)) {
      rrule.setByHour(monkeySeeIntArray(0, 23, 3, true));
    }

    if (0 == rnd.nextInt(sparsity * 2)) {
      rrule.setByMinute(monkeySeeIntArray(0, 60, 3, true));
    }

    if (0 == rnd.nextInt(sparsity * 2)) {
      rrule.setBySecond(monkeySeeIntArray(0, 60, 3, true));
    }

    boolean largestSetPlural = false;
    int setMag = 10;
    if (0 != rrule.getByMonth().length) {
      largestSetPlural = rrule.getByMonth().length > 1;
    } else if (0 != rrule.getByWeekNo().length) {
      largestSetPlural = rrule.getByWeekNo().length > 1;
    } else if (0 != rrule.getByYearDay().length) {
      largestSetPlural = rrule.getByYearDay().length > 1;
    } else if (0 != rrule.getByMonthDay().length) {
      largestSetPlural = rrule.getByMonthDay().length > 1;
    } else if (0 != rrule.getByDay().size()) {
      List<WeekdayNum> days = rrule.getByDay();
      largestSetPlural = days.size() > 1 || 0 == days.get(0).num;
    } else if (0 != rrule.getByHour().length) {
      largestSetPlural = rrule.getByHour().length > 1;
    } else if (0 != rrule.getByMinute().length) {
      largestSetPlural = rrule.getByMinute().length > 1;
    } else if (0 != rrule.getBySecond().length) {
      largestSetPlural = rrule.getBySecond().length > 1;
    }

    boolean allowSetPos =
      wellFormed ? largestSetPlural : maybeNot(largestSetPlural);
    if (allowSetPos && 0 == rnd.nextInt(4)) {
      rrule.setBySetPos(monkeySeeIntArray(-setMag, setMag, 3, false));
    }

    return rrule;
  }

  /** b, or, with a 5% probability, the inverse of b. */
  boolean maybeNot(boolean b) {
    return b ^ 0 == rnd.nextInt(20);
  }

  int[] monkeySeeIntArray(int min, int max, int magnitude, boolean allowZero) {
    int n = Math.max(0, (int) ((rnd.nextGaussian() + 1) * magnitude));
    int[] ints = new int[n];
    for (int i = n; --i >= 0;) {
      ints[i] = rnd.nextInt(max - min + 1) + min;
      if (!allowZero && 0 == ints[i]) {
        // if it's zero twice, then let it through.
        // This is a testcase after all.
        ints[i] = rnd.nextInt(max - min) + min;
      }
    }
    return ints;
  }

  WeekdayNum[] monkeySeeWeekdayNumList(Frequency freq) {
    int numRange;
    switch (freq) {
      case WEEKLY:
        numRange = 0;
        break;
      case MONTHLY:
        numRange = 5;
        break;
      case YEARLY:
        numRange = 53;
        break;
      default:
        numRange = 0;
        break;
    }
    int n = 1 + (int) (Math.abs(rnd.nextGaussian() * 2));
    WeekdayNum[] wdays = new WeekdayNum[n];
    for (int i = n; --i >= 0;) {
      int num = numRange != 0 && rnd.nextInt(10) < 2
                ? rnd.nextInt(numRange)
                : 0;
      Weekday wday = WDAYS[rnd.nextInt(7)];
      wdays[i] = new WeekdayNum(num, wday);
    }
    return wdays;
  }

  DateValue monkeySeeDateValue(boolean timed) {
    double daysFromNow = (10 * rnd.nextGaussian()) + 10;  // bias towards future
    if (timed) {
      return TimeUtils.add(
          refDate, new DateTimeValueImpl(
              0, 0, 0, 0, 0, (int) (daysFromNow * 24 * 60 * 60)));
    } else {
      assert !(refDate instanceof TimeValue);
      return TimeUtils.add(refDate, new DateValueImpl(0, 0, (int) daysFromNow));
    }
  }

  private DumpStackTask dumpStackIfRunsTooLong() {
    DumpStackTask task = new DumpStackTask();
    timer.schedule(task, 10000);
    return task;
  }

  /** generate a batch of 10000 random recurrence rules. */
  public static void main(String[] args) throws Exception {
    MonkeyKeyboardTest mkt = new MonkeyKeyboardTest();
    mkt.setUp();
    for (int i = 0; i < 10000; ++i) {
      boolean timed = mkt.rnd.nextBoolean();
      RRule rrule = mkt.monkeySeeRRule(timed, false);
      DateValue dtstart = mkt.monkeySeeDateValue(timed);
      System.out.println("BEGIN:VEVENT\nDTSTART:" + dtstart
                         + "\n" + rrule.toIcal() + "\nEND:VEVENT\n");
    }
  }
}

class Histogram {
  long[] samples;
  int n;

  Histogram(int capacity) {
    samples = new long[capacity];
  }

  void addSample(long sample) {
    samples[n++] = sample;
  }

  void dump(int nSlots, String units) {
    if (n < 2) { throw new IllegalStateException(); }
    Arrays.sort(samples);
    long min = samples[0],
         max = samples[samples.length - 1];
    double[] logs = new double[n];
    // Math.log is semimonotonic so this should yield a sorted array
    for (int i = n; --i >= 0;) {
      logs[i] = Math.log(samples[i] - min + 1d);
    }
    double logmin = logs[0],  // should be 0 or very close
           logmax = logs[logs.length - 1];
    double slotRange = (logmax - logmin) / nSlots;
    int[] countPerSlot = new int[nSlots];
    int totalCount = n;
    for (int i = n; --i >= 0;) {
      int slot = Math.min(nSlots - 1, (int) ((logs[i] - logmin) / slotRange));
      ++countPerSlot[slot];
    }
    double[] slotBrackets = new double[nSlots + 1];
    for (int i = 0; i <= nSlots; ++i) {
      slotBrackets[i] = Math.exp(logmin + slotRange * i) - 1 + min;
    }

    System.out.println("All measurements in " + units);
    for (int i = 0; i < nSlots; ++i) {
      System.out.println(
          String.format(
              "(%08.2f - %08.2f) | %-36s : %d",
              slotBrackets[i],
              slotBrackets[i + 1],
              "**************************************************".substring(
                  0, (countPerSlot[i] * 36) / totalCount),
              countPerSlot[i]));
    }
    System.out.println("min=" + min + ", max=" + max +
                       ", median=" + samples[samples.length / 2]);
  }

}

class DumpStackTask extends TimerTask {
  RRule rrule;
  DateValue dtStart;
  final Thread thread = Thread.currentThread();

  @Override
  public void run() {
    System.err.println(
        "\nTime out of " + (rrule != null ? rrule.toIcal() : "<null>")
        + " / " + dtStart);
    for (StackTraceElement el : thread.getStackTrace()) {
      System.err.println("\t" + el.toString());
    }
    System.err.println();
  }
}
